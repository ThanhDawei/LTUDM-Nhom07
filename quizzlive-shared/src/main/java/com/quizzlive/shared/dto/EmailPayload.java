package com.quizzlive.shared.dto;

import java.io.Serializable;

/**
 * Payload chứa thông tin cấu hình và dữ liệu gửi email qua cơ chế kết nối trực tiếp SMTP.
 * Đáp ứng yêu cầu: Các thông tin về email trong payload được khởi tạo để trống ("").
 */
public class EmailPayload implements Serializable {
    // 1. Thông tin cấu hình máy chủ SMTP (để trống)
    public String smtpHost = "";
    public int smtpPort = 587;
    public boolean useSsl = false;
    public boolean useStartTls = true;

    // 2. Thông tin tài khoản người gửi (để trống)
    public String senderEmail = "";
    public String senderPassword = "";
    public String fromName = "";

    // 3. Thông tin người nhận và nội dung OTP
    public String recipientEmail = "";
    public String subject = "";
    public String otpCode = "";
    public String purpose = "";
    public String bodyText = "";

    public EmailPayload() {
        this.smtpHost = "";
        this.senderEmail = "";
        this.senderPassword = "";
        this.fromName = "";
    }

    public EmailPayload(String recipientEmail, String otpCode, String purpose) {
        this();
        this.recipientEmail = recipientEmail;
        this.otpCode = otpCode;
        this.purpose = purpose;
        this.subject = "Mã OTP Quizzlive: " + otpCode;
        this.bodyText = "Chào bạn,\n\n"
                + "Mã OTP xác thực tài khoản Quizzlive của bạn cho thao tác [" + purpose + "] là: " + otpCode + "\n"
                + "Mã có hiệu lực trong vòng 10 phút và đã được lưu vào CSDL hệ thống.\n"
                + "Vui lòng không chia sẻ mã này cho bất kỳ ai.\n\n"
                + "Trân trọng,\n"
                + "Hệ thống Quizzlive (Kết nối trực tiếp qua SMTP Socket).";
    }

    public boolean isEmailConfigEmpty() {
        return smtpHost == null || smtpHost.trim().isEmpty()
                || senderEmail == null || senderEmail.trim().isEmpty();
    }
}
