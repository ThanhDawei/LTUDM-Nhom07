package com.quizzlive.server.network;

import com.quizzlive.server.dao.UserDAO;
import com.quizzlive.server.handler.PacketRouter;
import com.quizzlive.server.security.ServerSecurityManager;
import com.quizzlive.server.service.BattleManager;
import com.quizzlive.server.service.MatchmakingService;
import com.quizzlive.shared.protocol.OpCode;
import com.quizzlive.shared.protocol.Packet;
import com.quizzlive.shared.util.HybridCryptoUtil;
import com.quizzlive.shared.util.JsonUtil;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.PrivateKey;
import java.security.PublicKey;

public class ClientSession implements Runnable {
    private final Socket socket;
    private final SessionManager sessionManager;
    private final PacketRouter router;
    private ObjectInputStream input;
    private ObjectOutputStream output;

    private Long userId;
    private String username;
    private volatile boolean running = true;

    // Quản lý mã hóa lai cho phiên kết nối
    private SecretKey sessionAesKey;
    private volatile boolean handshakeCompleted = false;

    public ClientSession(Socket socket, SessionManager sessionManager, PacketRouter router) {
        this.socket = socket;
        this.sessionManager = sessionManager;
        this.router = router;
    }

    @Override
    public void run() {
        try {
            output = new ObjectOutputStream(socket.getOutputStream());
            output.flush();
            input = new ObjectInputStream(socket.getInputStream());

            // 1. Thực hiện bắt tay mã hóa lai (RSA-2048 + AES-256 GCM)
            if (!performHandshake()) {
                System.err.println("[Server] Bắt tay mã hóa thất bại với client " + getRemoteAddress());
                return;
            }

            // 2. Vòng lặp nhận gói tin từ Client
            while (running) {
                Packet packet = (Packet) input.readObject();
                if (packet != null) {
                    // Nếu là gói tin mã hóa AES, tiến hành giải mã trước khi đưa vào Router
                    if (packet.getOpCode() == OpCode.ENCRYPTED_PACKET && sessionAesKey != null) {
                        try {
                            String decryptedJson = HybridCryptoUtil.decryptAES(packet.getPayloadJson(), sessionAesKey);
                            packet = JsonUtil.fromJson(decryptedJson, Packet.class);
                        } catch (Exception ex) {
                            System.err.println("[Server] Lỗi giải mã AES từ client " + getRemoteAddress() + ": " + ex.getMessage());
                            continue;
                        }
                    }

                    if (packet != null) {
                        try {
                            router.route(this, packet);
                        } catch (Throwable t) {
                            System.err.println("[Server] Ngoại lệ khi xử lý gói tin OpCode " 
                                    + packet.getOpCode() + " từ client " + getRemoteAddress() + ": " + t.getMessage());
                            t.printStackTrace();
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("[Server] Session ended for: " + getRemoteAddress());
        } finally {
            cleanup();
        }
    }

    /**
     * Bắt tay trao đổi khóa mã hóa lai:
     * - Server gửi RSA Public Key (2048 bit)
     * - Client sinh khóa phiên AES-256 ngẫu nhiên, mã hóa bằng RSA Public Key và gửi lên
     * - Server dùng RSA Private Key giải mã lấy AES Key
     * - Server gửi phản hồi xác nhận HANDSHAKE_SUCCESS (mã hóa AES)
     */
    private boolean performHandshake() {
        try {
            // Bước 1: Gửi RSA Public Key của Server
            PublicKey serverPubKey = ServerSecurityManager.getInstance().getPublicKey();
            String pubKeyBase64 = HybridCryptoUtil.encodeRSAPublicKey(serverPubKey);
            output.writeObject(new Packet(OpCode.HANDSHAKE_INIT, pubKeyBase64));
            output.flush();

            // Bước 2: Nhận khóa phiên AES từ Client (được mã hóa bằng RSA Public Key)
            Packet exchangePacket = (Packet) input.readObject();
            if (exchangePacket == null || exchangePacket.getOpCode() != OpCode.HANDSHAKE_KEY_EXCHANGE) {
                System.err.println("[Server] Gói tin bắt tay không hợp lệ từ " + getRemoteAddress());
                return false;
            }

            PrivateKey serverPrivKey = ServerSecurityManager.getInstance().getPrivateKey();
            byte[] rawAesKey = HybridCryptoUtil.decryptRSA(exchangePacket.getPayloadJson(), serverPrivKey);
            this.sessionAesKey = HybridCryptoUtil.decodeAESKey(rawAesKey);

            // Bước 3: Phản hồi xác nhận thành công (mã hóa bằng chính khóa AES phiên)
            String encryptedAck = HybridCryptoUtil.encryptAES("HANDSHAKE_ESTABLISHED", this.sessionAesKey);
            output.writeObject(new Packet(OpCode.HANDSHAKE_SUCCESS, encryptedAck));
            output.flush();

            this.handshakeCompleted = true;
            System.out.println("[Server] [MÃ HÓA LAI] Bắt tay thành công với " + getRemoteAddress() 
                    + " (RSA-2048 Handshake -> Kênh mã hóa AES-256 GCM đã kích hoạt)");
            return true;
        } catch (Exception e) {
            System.err.println("[Server] Lỗi trong quá trình bắt tay bảo mật với " + getRemoteAddress() + ": " + e.getMessage());
            return false;
        }
    }

    public synchronized void sendPacket(Packet packet) {
        try {
            if (output != null && !socket.isClosed()) {
                // Tự động mã hóa toàn bộ gói tin bằng AES-256 GCM nếu đã bắt tay thành công
                if (handshakeCompleted && sessionAesKey != null 
                        && packet.getOpCode() != OpCode.HANDSHAKE_INIT 
                        && packet.getOpCode() != OpCode.HANDSHAKE_SUCCESS) {
                    String rawJson = JsonUtil.toJson(packet);
                    String encrypted = HybridCryptoUtil.encryptAES(rawJson, sessionAesKey);
                    Packet securePacket = new Packet(OpCode.ENCRYPTED_PACKET, encrypted);
                    output.writeObject(securePacket);
                } else {
                    output.writeObject(packet);
                }
                output.flush();
            }
        } catch (IOException e) {
            System.err.println("[Server] Error sending packet to " + username + ": " + e.getMessage());
        }
    }

    private void cleanup() {
        running = false;
        handshakeCompleted = false;
        sessionAesKey = null;
        if (userId != null) {
            MatchmakingService.getInstance().leaveQueue(userId);
            BattleManager.getInstance().handleUserDisconnected(userId);
            new UserDAO().updateOnlineStatus(userId, false);
        }
        sessionManager.removeSession(this);
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ignored) {}
    }

    public void close() {
        cleanup();
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public boolean isConnected() {
        return running && socket != null && !socket.isClosed() && socket.isConnected();
    }

    public String getRemoteAddress() {
        return socket != null ? socket.getRemoteSocketAddress().toString() : "Unknown";
    }
}
