package com.quizzlive.client.network;

import com.quizzlive.client.config.ClientConfig;
import com.quizzlive.shared.protocol.OpCode;
import com.quizzlive.shared.protocol.Packet;
import com.quizzlive.shared.util.HybridCryptoUtil;
import com.quizzlive.shared.util.JsonUtil;

import javax.crypto.SecretKey;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.PublicKey;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Quản lý kết nối Socket TCP phía Client tích hợp mã hóa lai (Hybrid Encryption: RSA-2048 + AES-256 GCM)
 * Chạy luồng nền (Daemon Thread) chuyên đọc gói tin từ Server
 */
public class SocketClient {
    private static SocketClient instance;

    private final String host;
    private final int port;
    private final boolean sslEnabled;

    private Socket socket;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private volatile boolean connected = false;

    // Quản lý khóa mã hóa lai cho phiên
    private SecretKey sessionAesKey;
    private volatile boolean handshakeCompleted = false;

    private final List<NetworkListener> listeners = new CopyOnWriteArrayList<>();

    public SocketClient(String host, int port, boolean sslEnabled) {
        this.host = host;
        this.port = port;
        this.sslEnabled = sslEnabled;
    }

    public static synchronized SocketClient getInstance() {
        if (instance == null) {
            instance = new SocketClient(
                    ClientConfig.getHost(),
                    ClientConfig.getPort(),
                    ClientConfig.isSslEnabled()
            );
        }
        return instance;
    }

    public void addListener(NetworkListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(NetworkListener listener) {
        listeners.remove(listener);
    }

    public synchronized void connect() throws IOException {
        if (connected) return;

        SocketFactory factory = sslEnabled ? SSLSocketFactory.getDefault() : SocketFactory.getDefault();
        socket = factory.createSocket(host, port);

        output = new ObjectOutputStream(socket.getOutputStream());
        output.flush();
        input = new ObjectInputStream(socket.getInputStream());

        // --- BƯỚC BẮT TAY MÃ HÓA LAI (RSA + AES HANDSHAKE) ---
        if (!performHandshake()) {
            disconnect();
            throw new IOException("Không thể thiết lập kết nối an toàn với máy chủ (Lỗi bắt tay mã hóa lai RSA-2048/AES-256).");
        }

        connected = true;
        System.out.println("[SocketClient] Đã kết nối an toàn tới Server tại " + host + ":" + port 
                + " (Mã hóa lai: RSA-2048 + AES-256-GCM đã kích hoạt)");

        for (NetworkListener l : listeners) {
            l.onConnected();
        }

        // Khởi chạy luồng nền lắng nghe gói tin từ Server
        Thread listenThread = new Thread(this::listenLoop, "Client-Socket-Listener");
        listenThread.setDaemon(true);
        listenThread.start();
    }

    /**
     * Bắt tay mã hóa lai với Server:
     * 1. Đọc RSA Public Key của Server
     * 2. Tự sinh ngẫu nhiên khóa phiên AES-256
     * 3. Dùng RSA Public Key để mã hóa khóa AES và gửi lên Server
     * 4. Nhận và xác thực thông điệp xác nhận từ Server bằng khóa AES
     */
    private boolean performHandshake() {
        try {
            System.out.println("[SocketClient] [MÃ HÓA LAI] Bắt đầu thiết lập kênh bảo mật với máy chủ...");
            
            // Bước 1: Nhận RSA Public Key từ Server
            Packet initPacket = (Packet) input.readObject();
            if (initPacket == null || initPacket.getOpCode() != OpCode.HANDSHAKE_INIT) {
                System.err.println("[SocketClient] Server không gửi gói tin khởi tạo bắt tay.");
                return false;
            }
            PublicKey serverPubKey = HybridCryptoUtil.decodeRSAPublicKey(initPacket.getPayloadJson());

            // Bước 2: Sinh khóa phiên AES-256 ngẫu nhiên
            this.sessionAesKey = HybridCryptoUtil.generateAESKey();

            // Bước 3: Dùng RSA Public Key của Server mã hóa khóa AES
            String encryptedAesKey = HybridCryptoUtil.encryptRSA(this.sessionAesKey.getEncoded(), serverPubKey);

            // Gửi khóa AES đã mã hóa lên Server
            output.writeObject(new Packet(OpCode.HANDSHAKE_KEY_EXCHANGE, encryptedAesKey));
            output.flush();

            // Bước 4: Nhận xác nhận HANDSHAKE_SUCCESS từ Server
            Packet successPacket = (Packet) input.readObject();
            if (successPacket == null || successPacket.getOpCode() != OpCode.HANDSHAKE_SUCCESS) {
                System.err.println("[SocketClient] Server không xác nhận hoàn tất bắt tay.");
                return false;
            }

            // Giải mã phản hồi bằng khóa AES vừa gửi để kiểm chứng kênh bảo mật 2 chiều
            String verifyMsg = HybridCryptoUtil.decryptAES(successPacket.getPayloadJson(), this.sessionAesKey);
            if (!"HANDSHAKE_ESTABLISHED".equals(verifyMsg)) {
                System.err.println("[SocketClient] Thông điệp xác nhận không hợp lệ: " + verifyMsg);
                return false;
            }

            this.handshakeCompleted = true;
            System.out.println("[SocketClient] [MÃ HÓA LAI] Kênh bảo mật kích hoạt thành công (RSA-2048 trao đổi khóa -> AES-256 GCM mã hóa toàn bộ dữ liệu).");
            return true;
        } catch (Exception e) {
            System.err.println("[SocketClient] Lỗi trong quá trình bắt tay bảo mật: " + e.getMessage());
            return false;
        }
    }

    public synchronized void sendPacket(Packet packet) {
        if (!connected || output == null) {
            System.err.println("[SocketClient] Không thể gửi gói tin. Chưa kết nối tới Server.");
            return;
        }
        try {
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
            System.out.println("[SocketClient] Đã gửi gói tin: " + packet.getOpCode() 
                    + (handshakeCompleted ? " (Đã mã hóa AES-256 GCM)" : ""));
        } catch (IOException e) {
            System.err.println("[SocketClient] Lỗi gửi gói tin: " + e.getMessage());
            disconnect();
        }
    }

    private void listenLoop() {
        try {
            while (connected) {
                Packet packet = (Packet) input.readObject();
                if (packet != null) {
                    // Nếu là gói tin mã hóa AES, tiến hành giải mã
                    if (packet.getOpCode() == OpCode.ENCRYPTED_PACKET && sessionAesKey != null) {
                        try {
                            String decryptedJson = HybridCryptoUtil.decryptAES(packet.getPayloadJson(), sessionAesKey);
                            packet = JsonUtil.fromJson(decryptedJson, Packet.class);
                        } catch (Exception ex) {
                            System.err.println("[SocketClient] Lỗi giải mã gói tin nhận từ Server: " + ex.getMessage());
                            continue;
                        }
                    }

                    if (packet != null) {
                        // Bắn sự kiện tới tất cả các listener đã đăng ký
                        for (NetworkListener l : listeners) {
                            try {
                                l.onPacketReceived(packet);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("[SocketClient] Mất kết nối tới Server: " + e.getMessage());
        } finally {
            disconnect();
        }
    }

    public synchronized void disconnect() {
        if (!connected) return;
        connected = false;
        handshakeCompleted = false;
        sessionAesKey = null;
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ignored) {}

        for (NetworkListener l : listeners) {
            l.onDisconnected();
        }
    }

    public boolean isConnected() {
        return connected;
    }

    public boolean isSecureEncrypted() {
        return connected && handshakeCompleted && sessionAesKey != null;
    }
}
