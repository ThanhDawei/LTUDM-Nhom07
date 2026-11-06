package com.quizzlive.server.util;

/**
 * Tiện ích xử lý và đối chiếu mật khẩu người dùng.
 * Yêu cầu: KHÔNG DÙNG MÃ HÓA, HÀM BĂM.
 * Mật khẩu được lưu trữ và đối chiếu trực tiếp dưới dạng chuỗi thô (plain text).
 */
public class PasswordUtil {

    /**
     * Không sử dụng mã hóa hay hàm băm.
     * Lưu trữ mật khẩu dạng chuỗi thô trực tiếp vào CSDL.
     */
    public static String hashPassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Mật khẩu không được để trống");
        }
        return password;
    }

    /**
     * Xác thực mật khẩu:
     * Đối chiếu trực tiếp chuỗi mật khẩu người dùng nhập với mật khẩu lưu trong CSDL (không dùng hàm băm hay mã hóa).
     */
    public static boolean verifyPassword(String password, String storedPassword) {
        if (password == null || storedPassword == null || storedPassword.isEmpty()) {
            return false;
        }
        return password.equals(storedPassword);
    }
}
