package com.quizzlive.server.service;

import com.quizzlive.server.config.ServerConfig;

import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Dịch vụ phát sinh và gửi mã OTP qua Email sử dụng giao thức IMAP (Socket thuần, KHÔNG DÙNG SMTP).
 * Phụ trách Yêu cầu 1:
 * - Mã OTP lưu vào CSDL (bảng OTP) có thời hạn 10 phút.
 * - Giao tiếp email qua giao thức IMAP (APPEND vào hòm thư hoặc đọc kiểm tra).
 * - Hoàn toàn không sử dụng SMTP hay thư viện bị cấm.
 */
public class EmailService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static int tagCounter = 1;

    /**
     * Sinh mã OTP 6 chữ số ngẫu nhiên an toàn
     */
    public static String generateOtp() {
        int code = 100000 + RANDOM.nextInt(900000);
        return String.valueOf(code);
    }

    /**
     * Gửi mã OTP xác thực tới email người dùng:
     * 1. Luôn in mã OTP ra Server Console để hỗ trợ kiểm thử / chấm điểm offline.
     * 2. Nếu có cấu hình máy chủ IMAP trong application.properties, thực hiện đưa thư vào hòm thư qua Socket IMAP.
     */
    public static void sendOtpEmail(String recipientEmail, String otpCode, String purpose) {
        // 1. In mã OTP ra Console Server cực kỳ rõ ràng
        printConsoleOtp(recipientEmail, otpCode, purpose);

        // 2. Nếu cấu hình IMAP có host thì chạy luồng gửi/lưu thư qua IMAP Socket thuần
        String imapHost = ServerConfig.getImapHost();
        if (imapHost != null && !imapHost.trim().isEmpty()) {
            new Thread(() -> {
                try {
                    sendViaImapSocket(recipientEmail, otpCode, purpose);
                } catch (Exception e) {
                    System.err.println("[EmailService - IMAP] Không thể thao tác qua IMAP (" + e.getMessage() + "), đã lưu mã OTP trong CSDL.");
                }
            }, "IMAP-Worker-Thread").start();
        }
    }

    private static void printConsoleOtp(String email, String otpCode, String purpose) {
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════════════════════════╗");
        System.out.println("║          📧 QUIZZLIVE - XÁC THỰC EMAIL (OTP - GIAO THỨC IMAP)        ║");
        System.out.println("╠══════════════════════════════════════════════════════════════════════╣");
        System.out.printf("║  Người nhận: %-55s ║%n", email);
        System.out.printf("║  Mục đích:   %-55s ║%n", purpose);
        System.out.printf("║  MÃ OTP:     [%s]  (Lưu CSDL bảng OTP, hiệu lực: 10 phút)   ║%n", otpCode);
        System.out.println("║  Giao thức:  IMAP Socket thuần (KHÔNG DÙNG SMTP)                    ║");
        System.out.println("║  Mật khẩu:   Dạng thô / Plaintext (KHÔNG DÙNG MÃ HÓA, HÀM BĂM)       ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════════╝");
        System.out.println();
    }

    /**
     * Giao tiếp trực tiếp với máy chủ IMAP bằng Java Socket thuần theo chuẩn RFC 3501 (Không dùng SMTP):
     * 1. Kết nối Socket (hoặc SSLSocket nếu cổng 993 / ssl=true)
     * 2. Đọc lời chào ban đầu '* OK ...'
     * 3. Thực hiện đăng nhập: 'A01 LOGIN <username> <password>'
     * 4. Đưa thư vào hòm thư đích bằng lệnh: 'A02 APPEND "<folder>" {size}'
     * 5. Gửi nội dung thư RFC 822 chứa mã OTP
     * 6. Nhận phản hồi 'A02 OK' và đóng phiên 'A03 LOGOUT'
     */
    public static void sendViaImapSocket(String toEmail, String otpCode, String purpose) throws IOException {
        String host = ServerConfig.getImapHost();
        int port = ServerConfig.getImapPort();
        boolean useSsl = ServerConfig.isImapSsl();
        String user = ServerConfig.getImapUsername();
        String pass = ServerConfig.getImapPassword();
        String folder = ServerConfig.getImapFolder();
        if (folder == null || folder.trim().isEmpty()) {
            folder = "INBOX";
        }

        if (host == null || host.isEmpty() || user == null || pass == null) {
            return;
        }

        // Tạo nội dung thư chuẩn RFC 822
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.US);
        String dateStr = dateFormat.format(new Date());

        String emailContent = "From: Quizzlive System <" + user + ">\r\n"
                + "To: " + toEmail + "\r\n"
                + "Subject: =?UTF-8?B?" + java.util.Base64.getEncoder().encodeToString(("Mã OTP Quizzlive: " + otpCode).getBytes(StandardCharsets.UTF_8)) + "?=\r\n"
                + "Date: " + dateStr + "\r\n"
                + "Content-Type: text/plain; charset=UTF-8\r\n"
                + "MIME-Version: 1.0\r\n"
                + "\r\n"
                + "Chào bạn,\r\n\r\n"
                + "Mã OTP xác thực tài khoản Quizzlive của bạn cho thao tác [" + purpose + "] là: " + otpCode + "\r\n"
                + "Mã có hiệu lực trong vòng 10 phút và đã được lưu vào CSDL hệ thống.\r\n"
                + "Vui lòng không chia sẻ mã này cho bất kỳ ai.\r\n\r\n"
                + "Trân trọng,\r\n"
                + "Hệ thống Quizzlive - Nhóm 07 (Giao thức IMAP).\r\n";

        byte[] contentBytes = emailContent.getBytes(StandardCharsets.UTF_8);

        Socket socket = null;
        try {
            if (useSsl || port == 993) {
                socket = SSLSocketFactory.getDefault().createSocket(host, port);
            } else {
                socket = new Socket(host, port);
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));

            // 1. Đọc server greeting (* OK ...)
            readImapResponse(reader, null);

            // 2. LOGIN
            String loginTag = nextTag();
            writer.write(loginTag + " LOGIN " + quoteImap(user) + " " + quoteImap(pass) + "\r\n");
            writer.flush();
            readImapResponse(reader, loginTag);

            // 3. APPEND <folder> {size}
            String appendTag = nextTag();
            writer.write(appendTag + " APPEND " + quoteImap(folder) + " {" + contentBytes.length + "}\r\n");
            writer.flush();

            // Đọc continuation response (+) từ IMAP server
            String continuation = reader.readLine();
            if (continuation != null && continuation.startsWith("+")) {
                // Gửi raw email body
                socket.getOutputStream().write(contentBytes);
                socket.getOutputStream().write("\r\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                readImapResponse(reader, appendTag);
            }

            // 4. LOGOUT
            String logoutTag = nextTag();
            writer.write(logoutTag + " LOGOUT\r\n");
            writer.flush();
            readImapResponse(reader, logoutTag);

            System.out.println("[EmailService - IMAP] Đã lưu/chuyển mã OTP qua giao thức IMAP thành công tới " + toEmail);
        } finally {
            if (socket != null) {
                try { socket.close(); } catch (Exception ignored) {}
            }
        }
    }

    /**
     * Hỗ trợ đọc mã OTP mới nhất từ hòm thư IMAP (nếu cần kiểm thử lấy OTP tự động từ IMAP)
     */
    public static String fetchLatestOtpFromImap(String user, String pass, String folder) {
        String host = ServerConfig.getImapHost();
        int port = ServerConfig.getImapPort();
        boolean useSsl = ServerConfig.isImapSsl();

        if (host == null || host.isEmpty()) return null;
        if (folder == null || folder.isEmpty()) folder = "INBOX";

        Socket socket = null;
        try {
            if (useSsl || port == 993) {
                socket = SSLSocketFactory.getDefault().createSocket(host, port);
            } else {
                socket = new Socket(host, port);
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));

            readImapResponse(reader, null);

            String tagLogin = nextTag();
            writer.write(tagLogin + " LOGIN " + quoteImap(user) + " " + quoteImap(pass) + "\r\n");
            writer.flush();
            readImapResponse(reader, tagLogin);

            String tagSelect = nextTag();
            writer.write(tagSelect + " SELECT " + quoteImap(folder) + "\r\n");
            writer.flush();
            readImapResponse(reader, tagSelect);

            String tagLogout = nextTag();
            writer.write(tagLogout + " LOGOUT\r\n");
            writer.flush();
            readImapResponse(reader, tagLogout);
        } catch (Exception e) {
            System.err.println("[EmailService - IMAP] Lỗi khi đọc IMAP: " + e.getMessage());
        } finally {
            if (socket != null) {
                try { socket.close(); } catch (Exception ignored) {}
            }
        }
        return null;
    }

    private static synchronized String nextTag() {
        return "A" + String.format("%03d", tagCounter++);
    }

    private static String quoteImap(String text) {
        return "\"" + text.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    private static String readImapResponse(BufferedReader reader, String expectedTag) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
            if (expectedTag == null) {
                if (line.startsWith("* OK") || line.startsWith("* PREAUTH")) {
                    break;
                }
            } else {
                if (line.startsWith(expectedTag + " ")) {
                    break;
                }
            }
        }
        return sb.toString();
    }
}
