package com.quizzlive.server.service;

import com.quizzlive.shared.dto.AuthDTO;

/**
 * Service xử lý Xác thực & Tài khoản người dùng
 * Phụ trách Yêu cầu 1: Đăng ký, đăng nhập, cập nhật thông tin cá nhân, xác thực email bằng OTP (10 phút)
 */
public class AuthService {

    /**
     * TODO: Dev Nhóm 7 triển khai
     * 1. Kiểm tra username, email trùng lặp trong CSDL
     * 2. Tạo tài khoản với trạng thái PENDING
     * 3. Sinh mã OTP 6 chữ số ngẫu nhiên, lưu vào bảng OTP với hạn 10 phút
     * 4. Gửi email OTP (qua SMTP hoặc log console)
     */
    public AuthDTO.AuthResponse register(AuthDTO.RegisterRequest req) {
        // TODO: Viết code xử lý đăng ký tại đây
        throw new UnsupportedOperationException("Chưa triển khai - Dành cho Dev");
    }

    /**
     * TODO: Dev Nhóm 7 triển khai
     * 1. Tìm mã OTP hợp lệ trong bảng OTP theo email và purpose = 'REGISTER'
     * 2. Nếu khớp và chưa hết hạn (< 10 phút): cập nhật trạng thái user thành 'ACTIVE'
     */
    public AuthDTO.AuthResponse verifyOtp(AuthDTO.VerifyOtpRequest req) {
        // TODO: Viết code xác thực OTP tại đây
        throw new UnsupportedOperationException("Chưa triển khai - Dành cho Dev");
    }

    /**
     * TODO: Dev Nhóm 7 triển khai
     * 1. Kiểm tra thông tin đăng nhập trong bảng NGUOI_DUNG
     * 2. Kiểm tra tài khoản có bị khóa không (TrangThai = 'BLOCKED' - Yêu cầu 15)
     * 3. Kiểm tra đã xác thực OTP chưa (TrangThai = 'PENDING')
     * 4. Cập nhật TrangThaiOnline = TRUE, LanDangNhapCuoi = NOW()
     */
    public AuthDTO.AuthResponse login(AuthDTO.LoginRequest req) {
        // TODO: Viết code xử lý đăng nhập tại đây
        throw new UnsupportedOperationException("Chưa triển khai - Dành cho Dev");
    }

    /**
     * TODO: Cập nhật TrangThaiOnline = FALSE khi user đăng xuất
     */
    public void logout(Long userId) {
        // TODO: Viết code xử lý đăng xuất tại đây
    }
}
