package com.quizzlive.server.service;

import com.quizzlive.server.config.ServerConfig;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Dịch vụ phát sinh và gửi mã OTP qua Email.
 * Phụ trách Yêu cầu 1: "Xác thực email bằng mã OTP (thời hạn 10 phút) trước khi kích hoạt tài khoản."
 * Hỗ trợ:
 * 1. Log OTP ra console Server cực kỳ rõ ràng để chấm điểm / demo offline không phụ thuộc internet.
 * 2. Gửi thư thực tế qua giao thức SMTP thuần (Socket) nếu được cấu hình trong application.properties.
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
     * Gửi mã OTP xác thực tới email người dùng
     */
    public static void sendOtpEmail(String recipientEmail, String otpCode, String purpose) {
        // 1. Luôn in mã OTP ra Server Console để hỗ trợ kiểm thử và demo tức thì
        printConsoleOtp(recipientEmail, otpCode, purpose);

        // 2. Nếu có cấu hình SMTP thật thì chạy luồng gửi ngầm không làm chậm Client
        if (ServerConfig.getSmtpHost() != null && !ServerConfig.getSmtpHost().trim().isEmpty()) {
            new Thread(() -> {
                try {
                    sendViaSmtpSocket(recipientEmail, otpCode, purpose);
                } catch (Exception e) {
                    System.err.println("[EmailService] Không thể gửi email qua SMTP (" + e.getMessage() + "), đã fallback dùng console.");
                }
            }, "SMTP-Sender-Thread").start();
        }
    }

    private static void printConsoleOtp(String email, String otpCode, String purpose) {
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                📧 QUIZZLIVE - XÁC THỰC EMAIL (OTP)                  ║");
        System.out.println("╠══════════════════════════════════════════════════════════════════════╣");
        System.out.printf("║  Người nhận: %-55s ║%n", email);
        System.out.printf("║  Mục đích:   %-55s ║%n", purpose);
        System.out.printf("║  MÃ OTP:     [%s]  (Thời hạn hiệu lực: 10 phút)       ║%n", otpCode);
        System.out.println("╚══════════════════════════════════════════════════════════════════════╝");
        System.out.println();
    }

    private static void sendViaSmtpSocket(String toEmail, String otpCode, String purpose) throws IOException {
        String host = ServerConfig.getSmtpHost();
        int port = ServerConfig.getSmtpPort();
        String user = ServerConfig.getSmtpUsername();
        String pass = ServerConfig.getSmtpPassword();

        if (host == null || host.isEmpty() || user == null || pass == null) return;

        try (Socket socket = (port == 465)
                ? SSLSocketFactory.getDefault().createSocket(host, port)
                : new Socket(host, port)) {

            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));

            readResponse(reader);
            sendCommand(writer, reader, "EHLO " + host);

            if (port != 465) {
                // STARTTLS nếu dùng cổng 587
                sendCommand(writer, reader, "STARTTLS");
                SSLSocket sslSocket = (SSLSocket) ((SSLSocketFactory) SSLSocketFactory.getDefault())
                        .createSocket(socket, host, port, true);
                sslSocket.startHandshake();
                reader = new BufferedReader(new InputStreamReader(sslSocket.getInputStream(), StandardCharsets.UTF_8));
                writer = new BufferedWriter(new OutputStreamWriter(sslSocket.getOutputStream(), StandardCharsets.UTF_8));
                sendCommand(writer, reader, "EHLO " + host);
            }

            // AUTH LOGIN
            sendCommand(writer, reader, "AUTH LOGIN");
            sendCommand(writer, reader, Base64.getEncoder().encodeToString(user.getBytes(StandardCharsets.UTF_8)));
            sendCommand(writer, reader, Base64.getEncoder().encodeToString(pass.getBytes(StandardCharsets.UTF_8)));

            // MAIL FROM & RCPT TO
            sendCommand(writer, reader, "MAIL FROM:<" + user + ">");
            sendCommand(writer, reader, "RCPT TO:<" + toEmail + ">");
            sendCommand(writer, reader, "DATA");

            // Email Header & Body
            writer.write("From: Quizzlive System <" + user + ">\r\n");
            writer.write("To: " + toEmail + "\r\n");
            writer.write("Subject: =?UTF-8?B?" + Base64.getEncoder().encodeToString("Mã OTP xác thực Quizzlive".getBytes(StandardCharsets.UTF_8)) + "?=\r\n");
            writer.write("Content-Type: text/plain; charset=UTF-8\r\n\r\n");
            writer.write("Chào bạn,\r\n\r\n");
            writer.write("Mã xác thực OTP của bạn cho thao tác [" + purpose + "] là: " + otpCode + "\r\n");
            writer.write("Mã có hiệu lực trong vòng 10 phút. Vui lòng không chia sẻ mã này cho bất kỳ ai.\r\n\r\n");
            writer.write("Trân trọng,\r\nĐội ngũ Quizzlive - Nhóm 07.\r\n");
            writer.write(".\r\n");
            writer.flush();
            readResponse(reader);

            sendCommand(writer, reader, "QUIT");
            System.out.println("[EmailService] Đã gửi mã OTP thực tế qua SMTP tới " + toEmail);
        }
    }

    private static void sendCommand(BufferedWriter writer, BufferedReader reader, String command) throws IOException {
        writer.write(command + "\r\n");
        writer.flush();
        readResponse(reader);
    }

    private static String readResponse(BufferedReader reader) throws IOException {
        String line = reader.readLine();
        while (line != null && line.length() >= 4 && line.charAt(3) == '-') {
            line = reader.readLine();
        }
        return line;
    }
}

