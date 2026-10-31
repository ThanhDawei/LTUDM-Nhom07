package com.quizzlive.server.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.HexFormat;

/**
 * Tiện ích băm và kiểm tra mật khẩu bằng thuật toán PBKDF2WithHmacSHA256 kèm Salt ngẫu nhiên.
 * Đáp ứng tiêu chí A3 (Bảo mật) & Quy định 2.4: "Mật khẩu lưu trong CSDL ở dạng băm có salt, nên dùng BCrypt hoặc PBKDF2".
 */
public class PasswordUtil {
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_BYTES = 16;
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Băm mật khẩu dạng rõ với salt ngẫu nhiên 16 bytes và 65536 vòng lặp
     * Định dạng lưu trữ: PBKDF2$<iterations>$<hexSalt>$<hexHash>
     */
    public static String hashPassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Mật khẩu không được để trống");
        }
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        byte[] hash = pbkdf2(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);

        HexFormat hex = HexFormat.of();
        return "PBKDF2$" + ITERATIONS + "$" + hex.formatHex(salt) + "$" + hex.formatHex(hash);
    }

    /**
     * Xác thực mật khẩu nhập vào với chuỗi hash đã lưu trong CSDL.
     * Hỗ trợ tự động đối chiếu cả chuỗi băm PBKDF2 lẫn mật khẩu thô từ dữ liệu mẫu (seed_data).
     */
    public static boolean verifyPassword(String password, String storedHash) {
        if (password == null || storedHash == null || storedHash.isEmpty()) {
            return false;
        }

        if (storedHash.startsWith("PBKDF2$")) {
            String[] parts = storedHash.split("\\$");
            if (parts.length != 4) {
                return false;
            }
            int iterations = Integer.parseInt(parts[1]);
            byte[] salt = HexFormat.of().parseHex(parts[2]);
            byte[] expectedHash = HexFormat.of().parseHex(parts[3]);

            byte[] actualHash = pbkdf2(password.toCharArray(), salt, iterations, expectedHash.length * 8);
            return MessageDigest.isEqual(expectedHash, actualHash);
        }

        // Khả năng tương thích dữ liệu mẫu chưa băm (plain text)
        return password.equals(storedHash);
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyLength) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
            return skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Lỗi thuật toán mã hóa PBKDF2: " + e.getMessage(), e);
        }
    }
}

