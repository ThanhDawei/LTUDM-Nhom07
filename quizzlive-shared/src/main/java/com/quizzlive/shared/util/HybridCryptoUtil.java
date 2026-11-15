package com.quizzlive.shared.util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Tiện ích mã hóa lai (Hybrid Encryption) kết hợp RSA-2048 và AES-256 GCM
 * - RSA: Mã hóa bất đối xứng dùng trong pha bắt tay (Handshake) để trao đổi Session Key an toàn.
 * - AES-GCM: Mã hóa đối xứng có xác thực (AEAD) dùng để mã hóa toàn bộ dữ liệu gói tin thời gian thực.
 */
public class HybridCryptoUtil {
    private static final String RSA_ALGORITHM = "RSA";
    private static final String RSA_TRANSFORMATION = "RSA/ECB/PKCS1Padding";
    private static final int RSA_KEY_SIZE = 2048;

    private static final String AES_ALGORITHM = "AES";
    private static final String AES_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int AES_KEY_SIZE = 256;
    private static final int GCM_IV_LENGTH = 12; // 96-bit IV tiêu chuẩn cho AES-GCM
    private static final int GCM_TAG_LENGTH = 128; // 128-bit Authentication Tag

    private static final SecureRandom secureRandom = new SecureRandom();

    // ==========================================
    // 1. RSA ASYMMETRIC CRYPTOGRAPHY (Trao đổi khóa)
    // ==========================================

    /**
     * Sinh cặp khóa RSA-2048 (Public Key & Private Key)
     */
    public static KeyPair generateRSAKeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance(RSA_ALGORITHM);
            keyGen.initialize(RSA_KEY_SIZE, secureRandom);
            return keyGen.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Không tìm thấy thuật toán RSA", e);
        }
    }

    /**
     * Chuyển RSA Public Key thành chuỗi Base64
     */
    public static String encodeRSAPublicKey(PublicKey publicKey) {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    /**
     * Khôi phục RSA Public Key từ chuỗi Base64
     */
    public static PublicKey decodeRSAPublicKey(String base64PublicKey) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(base64PublicKey);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
            return keyFactory.generatePublic(spec);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi giải mã RSA Public Key", e);
        }
    }

    /**
     * Chuyển RSA Private Key thành chuỗi Base64
     */
    public static String encodeRSAPrivateKey(PrivateKey privateKey) {
        return Base64.getEncoder().encodeToString(privateKey.getEncoded());
    }

    /**
     * Khôi phục RSA Private Key từ chuỗi Base64
     */
    public static PrivateKey decodeRSAPrivateKey(String base64PrivateKey) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(base64PrivateKey);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
            return keyFactory.generatePrivate(spec);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi giải mã RSA Private Key", e);
        }
    }

    /**
     * Mã hóa dữ liệu byte bằng RSA Public Key (trả về chuỗi Base64)
     */
    public static String encryptRSA(byte[] data, PublicKey publicKey) {
        try {
            Cipher cipher = Cipher.getInstance(RSA_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encrypted = cipher.doFinal(data);
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi mã hóa RSA", e);
        }
    }

    /**
     * Giải mã dữ liệu chuỗi Base64 bằng RSA Private Key
     */
    public static byte[] decryptRSA(String base64EncryptedData, PrivateKey privateKey) {
        try {
            byte[] data = Base64.getDecoder().decode(base64EncryptedData);
            Cipher cipher = Cipher.getInstance(RSA_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi giải mã RSA", e);
        }
    }

    // ==========================================
    // 2. AES SYMMETRIC CRYPTOGRAPHY (Mã hóa phiên)
    // ==========================================

    /**
     * Sinh khóa phiên ngẫu nhiên AES-256
     */
    public static SecretKey generateAESKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance(AES_ALGORITHM);
            keyGen.init(AES_KEY_SIZE, secureRandom);
            return keyGen.generateKey();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Không tìm thấy thuật toán AES", e);
        }
    }

    /**
     * Khôi phục SecretKey từ mảng byte
     */
    public static SecretKey decodeAESKey(byte[] keyBytes) {
        return new SecretKeySpec(keyBytes, AES_ALGORITHM);
    }

    /**
     * Mã hóa chuỗi văn bản bằng AES-256 GCM (kèm IV ngẫu nhiên)
     * Kết quả là chuỗi Base64 chứa: [12 bytes IV] + [Ciphertext + Auth Tag]
     */
    public static String encryptAES(String plainText, SecretKey key) {
        if (plainText == null) return null;
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);

            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + cipherText.length);
            byteBuffer.put(iv);
            byteBuffer.put(cipherText);

            return Base64.getEncoder().encodeToString(byteBuffer.array());
        } catch (Exception e) {
            throw new RuntimeException("Lỗi mã hóa AES-GCM", e);
        }
    }

    /**
     * Giải mã chuỗi Base64 bằng AES-256 GCM
     */
    public static String decryptAES(String base64CipherText, SecretKey key) {
        if (base64CipherText == null) return null;
        try {
            byte[] cipherData = Base64.getDecoder().decode(base64CipherText);
            ByteBuffer byteBuffer = ByteBuffer.wrap(cipherData);

            byte[] iv = new byte[GCM_IV_LENGTH];
            byteBuffer.get(iv);

            byte[] cipherText = new byte[byteBuffer.remaining()];
            byteBuffer.get(cipherText);

            Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);

            byte[] plainTextBytes = cipher.doFinal(cipherText);
            return new String(plainTextBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi giải mã AES-GCM", e);
        }
    }
}
