package com.quizzlive.server.util;

/**
 * Tiện ích xử lý và đối chiếu mật khẩu người dùng.
 * Cấu hình: KHÔNG DÙNG BĂM BCRYPT theo yêu cầu của đồ án (lưu và so sánh mật khẩu trực tiếp).
 * Vẫn hỗ trợ đối chiếu với các tài khoản đã tạo trước đó nếu có.
 */
public class PasswordUtil {

    /**
     * Xử lý mật khẩu trước khi lưu CSDL:
     * Không dùng băm BCrypt, lưu mật khẩu trực tiếp.
     */
    public static String hashPassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Mật khẩu không được để trống");
        }
        return password;
    }

    /**
     * Xác thực mật khẩu đăng nhập với mật khẩu lưu trong CSDL.
     * Đối chiếu trực tiếp chuỗi mật khẩu (không dùng BCrypt).
     */
    public static boolean verifyPassword(String password, String storedPassword) {
        if (password == null || storedPassword == null || storedPassword.isEmpty()) {
            return false;
        }

        // Đối chiếu trực tiếp (Không dùng băm BCrypt)
        if (password.equals(storedPassword)) {
            return true;
        }

        // Tương thích ngược nếu trong DB từng lưu chuỗi băm PBKDF2
        if (storedPassword.startsWith("PBKDF2$")) {
            try {
                String[] parts = storedPassword.split("\\$");
                if (parts.length == 4) {
                    int iterations = Integer.parseInt(parts[1]);
                    byte[] salt = java.util.HexFormat.of().parseHex(parts[2]);
                    byte[] expectedHash = java.util.HexFormat.of().parseHex(parts[3]);

                    javax.crypto.spec.PBEKeySpec spec = new javax.crypto.spec.PBEKeySpec(
                            password.toCharArray(), salt, iterations, expectedHash.length * 8
                    );
                    javax.crypto.SecretKeyFactory skf = javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
                    byte[] actualHash = skf.generateSecret(spec).getEncoded();
                    return java.security.MessageDigest.isEqual(expectedHash, actualHash);
                }
            } catch (Exception ignored) {
            }
        }

        return false;
    }
}
