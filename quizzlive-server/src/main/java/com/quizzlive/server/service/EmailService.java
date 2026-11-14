package com.quizzlive.server.service;

import com.quizzlive.server.config.ServerConfig;
import com.quizzlive.shared.dto.EmailPayload;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Locale;

/**
 * Dịch vụ phát sinh và gửi mã OTP qua Email sử dụng cơ chế kết nối trực tiếp qua SMTP (Socket RFC 5321).
 * Đáp ứng yêu cầu:
 * - Khởi tạo payload cho việc gửi OTP (EmailPayload).
 * - Các thông tin về email trong payload được để trống theo yêu cầu.
 * - Cơ chế kết nối trực tiếp qua giao thức SMTP Socket thuần (hỗ trợ STARTTLS cổng 587 và SSL cổng 465).
 */
public class EmailService {
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Sinh mã OTP 6 chữ số ngẫu nhiên an toàn
     */
    public static String generateOtp() {
        int code = 100000 + RANDOM.nextInt(900000);
        return String.valueOf(code);
    }

    /**
     * Gửi mã OTP xác thực tới email người dùng:
     * 1. Khởi tạo EmailPayload với các thông tin về email được để trống theo yêu cầu.
     * 2. In mã OTP ra Console Server hỗ trợ kiểm thử / demo.
     * 3. Thực hiện kết nối trực tiếp qua giao thức SMTP Socket (RFC 5321).
     */
    public static void sendOtpEmail(String recipientEmail, String otpCode, String purpose) {
        // 1. Khởi tạo payload với các thông tin về email máy chủ & người gửi để trống ("")
        EmailPayload payload = new EmailPayload(recipientEmail, otpCode, purpose);

        // Nạp cấu hình từ ServerConfig nếu người dùng có thiết lập trong application.properties
        String cfgHost = ServerConfig.getSmtpHost();
        if (cfgHost != null && !cfgHost.trim().isEmpty()) {
            payload.smtpHost = cfgHost.trim();
            payload.smtpPort = ServerConfig.getSmtpPort();
            payload.useSsl = ServerConfig.isSmtpSsl();
            payload.useStartTls = ServerConfig.isSmtpStartTls();
            payload.senderEmail = ServerConfig.getSmtpUsername();
            payload.senderPassword = ServerConfig.getSmtpPassword();
            payload.fromName = ServerConfig.getSmtpFrom();
        }

        // 2. In mã OTP ra Console Server cực kỳ rõ ràng
        printConsoleOtp(payload);

        // 3. Thực thi kết nối trực tiếp qua giao thức SMTP trên luồng nền
        new Thread(() -> {
            try {
                sendViaDirectSmtp(payload);
            } catch (Exception e) {
                System.err.println("[EmailService - SMTP] Kết nối SMTP trực tiếp: " + e.getMessage()
                        + " (Mã OTP đã được tạo và lưu trữ an toàn trong CSDL bảng OTP).");
            }
        }, "SMTP-Direct-Worker").start();
    }

    /**
     * Cơ chế kết nối trực tiếp tới máy chủ thư qua giao thức SMTP (RFC 5321) bằng Java Socket thuần:
     * - Cổng 587 (STARTTLS) hoặc cổng 465 (SMTPS SSL) hoặc cổng 25 (Standard SMTP)
     * - Trình tự giao thức:
     *   1. Kết nối Socket & đọc mã chào 220
     *   2. Gửi EHLO localhost
     *   3. Nâng cấp bảo mật STARTTLS nếu kích hoạt
     *   4. Xác thực tài khoản AUTH LOGIN (nếu có thông tin đăng nhập)
     *   5. MAIL FROM:<sender>
     *   6. RCPT TO:<recipient>
     *   7. DATA (Gửi MIME message header + body chứa mã OTP)
     *   8. QUIT
     */
    public static void sendViaDirectSmtp(EmailPayload payload) throws IOException {
        if (payload == null) return;

        // Nếu thông tin cấu hình email trong payload để trống:
        if (payload.isEmailConfigEmpty()) {
            System.out.println("[EmailService - SMTP] Payload kết nối trực tiếp qua SMTP được khởi tạo (các thông tin email trong payload để trống).");
            System.out.println("[EmailService - SMTP] Mã OTP [" + payload.otpCode + "] đã được lưu vào CSDL (bảng OTP) cho email: " + payload.recipientEmail);
            return;
        }

        Socket socket = null;
        try {
            if (payload.useSsl || payload.smtpPort == 465) {
                socket = SSLSocketFactory.getDefault().createSocket(payload.smtpHost, payload.smtpPort);
            } else {
                socket = new Socket(payload.smtpHost, payload.smtpPort);
            }
            socket.setSoTimeout(15000);

            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));

            // 1. Đọc server greeting (220)
            readSmtpResponse(reader, 220);

            // 2. Gửi EHLO localhost
            sendSmtpCommand(writer, "EHLO localhost");
            readSmtpResponse(reader, 250);

            // 3. STARTTLS nếu cấu hình yêu cầu
            if (payload.useStartTls && !payload.useSsl && payload.smtpPort != 465) {
                sendSmtpCommand(writer, "STARTTLS");
                readSmtpResponse(reader, 220);

                SSLSocket sslSocket = (SSLSocket) ((SSLSocketFactory) SSLSocketFactory.getDefault())
                        .createSocket(socket, payload.smtpHost, payload.smtpPort, true);
                sslSocket.startHandshake();

                reader = new BufferedReader(new InputStreamReader(sslSocket.getInputStream(), StandardCharsets.UTF_8));
                writer = new BufferedWriter(new OutputStreamWriter(sslSocket.getOutputStream(), StandardCharsets.UTF_8));

                sendSmtpCommand(writer, "EHLO localhost");
                readSmtpResponse(reader, 250);
            }

            // 4. AUTH LOGIN (nếu có mật khẩu tài khoản gửi)
            if (payload.senderPassword != null && !payload.senderPassword.trim().isEmpty()) {
                sendSmtpCommand(writer, "AUTH LOGIN");
                readSmtpResponse(reader, 334);

                String encodedUser = Base64.getEncoder().encodeToString(payload.senderEmail.getBytes(StandardCharsets.UTF_8));
                sendSmtpCommand(writer, encodedUser);
                readSmtpResponse(reader, 334);

                String encodedPass = Base64.getEncoder().encodeToString(payload.senderPassword.getBytes(StandardCharsets.UTF_8));
                sendSmtpCommand(writer, encodedPass);
                readSmtpResponse(reader, 235);
            }

            // 5. MAIL FROM
            sendSmtpCommand(writer, "MAIL FROM:<" + payload.senderEmail + ">");
            readSmtpResponse(reader, 250);

            // 6. RCPT TO
            sendSmtpCommand(writer, "RCPT TO:<" + payload.recipientEmail + ">");
            readSmtpResponse(reader, 250);

            // 7. DATA
            sendSmtpCommand(writer, "DATA");
            readSmtpResponse(reader, 354);

            // 8. Gửi MIME Message Body RFC 822
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.US);
            String dateStr = dateFormat.format(new Date());
            String fromDisplay = (payload.fromName != null && !payload.fromName.trim().isEmpty())
                    ? payload.fromName + " <" + payload.senderEmail + ">"
                    : payload.senderEmail;

            String headers = "From: " + fromDisplay + "\r\n"
                    + "To: " + payload.recipientEmail + "\r\n"
                    + "Subject: =?UTF-8?B?" + Base64.getEncoder().encodeToString(payload.subject.getBytes(StandardCharsets.UTF_8)) + "?=\r\n"
                    + "Date: " + dateStr + "\r\n"
                    + "Content-Type: text/plain; charset=UTF-8\r\n"
                    + "MIME-Version: 1.0\r\n\r\n";

            writer.write(headers);
            writer.write(payload.bodyText != null ? payload.bodyText : ("Mã OTP của bạn là: " + payload.otpCode));
            writer.write("\r\n.\r\n");
            writer.flush();
            readSmtpResponse(reader, 250);

            // 9. QUIT
            sendSmtpCommand(writer, "QUIT");
            readSmtpResponse(reader, 221);

            System.out.println("[EmailService - SMTP] Đã gửi mã OTP trực tiếp qua giao thức SMTP thành công tới " + payload.recipientEmail);
        } finally {
            if (socket != null) {
                try { socket.close(); } catch (Exception ignored) {}
            }
        }
    }

    private static void sendSmtpCommand(BufferedWriter writer, String cmd) throws IOException {
        writer.write(cmd + "\r\n");
        writer.flush();
    }

    private static String readSmtpResponse(BufferedReader reader, int expectedCode) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        int code = -1;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
            if (line.length() >= 3) {
                try {
                    code = Integer.parseInt(line.substring(0, 3));
                } catch (NumberFormatException ignored) {}
                if (line.length() == 3 || line.charAt(3) == ' ') {
                    break;
                }
            }
        }
        if (expectedCode > 0 && code != expectedCode) {
            throw new IOException("SMTP Response Error - Expected " + expectedCode + ", but got: " + sb.toString().trim());
        }
        return sb.toString();
    }

    private static void printConsoleOtp(EmailPayload payload) {
        String infoHost = (payload.smtpHost == null || payload.smtpHost.isEmpty()) ? "(Để trống trong Payload)" : payload.smtpHost + ":" + payload.smtpPort;
        String infoSender = (payload.senderEmail == null || payload.senderEmail.isEmpty()) ? "(Để trống trong Payload)" : payload.senderEmail;

        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════════════════════════╗");
        System.out.println("║       💾 QUIZZLIVE - MÃ XÁC THỰC OTP ĐÃ LƯU VÀO CƠ SỞ DỮ LIỆU        ║");
        System.out.println("╠══════════════════════════════════════════════════════════════════════╣");
        System.out.printf("║  Người nhận: %-55s ║%n", payload.recipientEmail);
        System.out.printf("║  Mục đích:   %-55s ║%n", payload.purpose);
        System.out.printf("║  MÃ OTP:     [%s]  (Đã lưu CSDL bảng OTP, hiệu lực: 10 phút) ║%n", payload.otpCode);
        System.out.println("║  Bảng CSDL:  Bảng OTP (MaNguoiDung, MaXacThuc, MucDich, ThoiGianHetHan) ║");
        System.out.println("║  Giao thức:  Kết nối trực tiếp qua SMTP (Socket RFC 5321)           ║");
        System.out.printf("║  Máy chủ:    %-55s ║%n", infoHost);
        System.out.printf("║  Tài khoản:  %-55s ║%n", infoSender);
        System.out.println("╚══════════════════════════════════════════════════════════════════════╝");
        System.out.println();
    }
}
