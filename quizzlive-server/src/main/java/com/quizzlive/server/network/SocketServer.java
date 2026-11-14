package com.quizzlive.server.network;

import com.quizzlive.server.config.ServerConfig;
import com.quizzlive.server.dao.UserDAO;
import com.quizzlive.server.handler.PacketRouter;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLContext;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SocketServer {
    private final int port;
    private final SessionManager sessionManager = new SessionManager();
    private final PacketRouter router = new PacketRouter(sessionManager);
    private final ExecutorService threadPool = Executors.newFixedThreadPool(ServerConfig.getThreadPoolSize());
    private volatile boolean isRunning = false;
    private ServerSocket serverSocket;

    public SocketServer(int port) {
        this.port = port;
    }

    public void start() {
        isRunning = true;
        try {
            // Reset trạng thái online cho toàn bộ người dùng khi khởi động lại máy chủ
            new UserDAO().resetAllOnlineStatus();
            // Dọn dẹp tài khoản test rác để chỉ hiển thị tài khoản mẫu và tài khoản thật do người dùng đăng ký
            new UserDAO().cleanupTestAccounts();

            ServerSocketFactory factory;
            if (ServerConfig.isSslEnabled()) {
                System.out.println("[Quizzlive Server] Enabling SSL/TLS Encryption...");
                SSLContext sslContext = SSLContextFactory.createSSLContext(
                        ServerConfig.getKeyStorePath(),
                        ServerConfig.getKeyStorePassword()
                );
                factory = sslContext.getServerSocketFactory();
            } else {
                factory = ServerSocketFactory.getDefault();
            }

            serverSocket = factory.createServerSocket(port);
            System.out.println("[Quizzlive Server] Started listening on port " + port
                    + " (SSL: " + ServerConfig.isSslEnabled() + ")");

            while (isRunning) {
                Socket clientSocket = serverSocket.accept();
                ClientSession session = new ClientSession(clientSocket, sessionManager, router);
                sessionManager.addSession(session);
                threadPool.execute(session);
            }
        } catch (Exception e) {
            if (isRunning) {
                System.err.println("[Quizzlive Server] Socket error: " + e.getMessage());
            }
        } finally {
            stop();
        }
    }

    public void stop() {
        isRunning = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (Exception ignored) {}
        threadPool.shutdown();
        System.out.println("[Quizzlive Server] Stopped.");
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public boolean isRunning() {
        return isRunning;
    }
}
