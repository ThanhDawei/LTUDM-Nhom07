package com.quizzlive.server.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

/**
 * Tiện ích băm và xác thực mật khẩu người dùng sử dụng thuật toán PBKDF2 kết hợp Salt.
 * Thuật toán: PBKDF2WithHmacSHA256 với 10,000 vòng lặp (iterations), độ dài khóa 256 bits.
 */
public class PasswordUtil {

    private static final int ITERATIONS = 10000;
    private static final int KEY_LENGTH = 256;
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Tạo một chuỗi Salt ngẫu nhiên 16 bytes (128-bit) mã hóa dạng Base64.
     */
    public static String generateSalt() {
        byte[] salt = new byte[16];
        SECURE_RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * Băm mật khẩu bằng phương pháp PBKDF2 kết hợp Salt.
     *
     * @param password Mật khẩu người dùng nhập
     * @param saltBase64 Chuỗi Salt ngẫu nhiên (Base64)
     * @return Chuỗi băm kết quả (Base64)
     */
    public static String hashPassword(String password, String saltBase64) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Mật khẩu không được để trống!");
        }
        if (saltBase64 == null || saltBase64.isEmpty()) {
            throw new IllegalArgumentException("Salt không được để trống!");
        }
        try {
            byte[] saltBytes = Base64.getDecoder().decode(saltBase64);
            KeySpec spec = new PBEKeySpec(password.toCharArray(), saltBytes, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] hashBytes = factory.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Lỗi thuật toán mã hóa PBKDF2: " + e.getMessage(), e);
        }
    }

    /**
     * Xác thực mật khẩu:
     * Tính toán giá trị băm từ mật khẩu nhập vào và Salt lưu trong CSDL,
     * sau đó so sánh bằng phép toán so sánh an toàn (Constant-time comparison).
     *
     * @param password Mật khẩu người dùng gửi lên
     * @param saltBase64 Salt tương ứng của tài khoản trong CSDL
     * @param expectedHashBase64 Giá trị Hash lưu trong CSDL
     * @return true nếu mật khẩu chính xác, ngược lại false
     */
    public static boolean verifyPassword(String password, String saltBase64, String expectedHashBase64) {
        if (password == null || saltBase64 == null || expectedHashBase64 == null) {
            return false;
        }
        try {
            String computedHash = hashPassword(password, saltBase64);
            byte[] a = Base64.getDecoder().decode(computedHash);
            byte[] b = Base64.getDecoder().decode(expectedHashBase64);
            return MessageDigest.isEqual(a, b);
        } catch (Exception e) {
            return false;
        }
    }
}
