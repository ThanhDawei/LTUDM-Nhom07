package com.quizzlive.shared.util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Base64;

/**
 * Tiện ích chuyển đổi hai chiều giữa Hình Ảnh (File/Resource) và Chuỗi Mã Hóa Base64.
 * Đảm bảo mọi hình ảnh khi lưu xuống CSDL đều được chuyển đổi thành chuỗi Base64
 * và Client có thể giải mã hiển thị trực tiếp.
 */
public class ImageBase64Util {

    /**
     * Chuyển đổi một đường dẫn file hoặc tên resource thành chuỗi Base64 dạng data URI:
     * "data:image/png;base64,..."
     */
    public static String toBase64(String pathOrData) {
        if (pathOrData == null || pathOrData.trim().isEmpty()) {
            return null;
        }
        String trimmed = pathOrData.trim();

        // 1. Nếu đã là chuỗi Base64 Data URI thì giữ nguyên
        if (trimmed.startsWith("data:image/") && trimmed.contains(";base64,")) {
            return trimmed;
        }

        // 2. Nếu đã là chuỗi Base64 thô dài
        if (isLikelyRawBase64(trimmed)) {
            return "data:image/png;base64," + trimmed;
        }

        // 3. Thử đọc từ File hệ thống cục bộ
        File directFile = new File(trimmed);
        if (directFile.exists() && directFile.isFile()) {
            return fileToBase64(directFile);
        }

        // Thử tìm trong các thư mục phổ biến: data/, data/avatars/, data/images/
        String[] prefixes = {"data/", "data/avatars/", "data/images/", "../data/", "../data/avatars/", "quizzlive-client/src/main/resources/avatars/", "quizzlive-client/src/main/resources/images/"};
        for (String p : prefixes) {
            File f = new File(p + trimmed);
            if (f.exists() && f.isFile()) {
                return fileToBase64(f);
            }
        }

        // 4. Thử đọc từ Classpath Resources (/avatars/ hoặc /images/)
        String resName = trimmed;
        int lastSlash = Math.max(resName.lastIndexOf('/'), resName.lastIndexOf('\\'));
        if (lastSlash >= 0) {
            resName = resName.substring(lastSlash + 1);
        }

        byte[] resBytes = readResourceBytes("/avatars/" + resName);
        if (resBytes == null) {
            resBytes = readResourceBytes("/images/" + resName);
        }
        if (resBytes == null) {
            resBytes = readResourceBytes("/" + resName);
        }

        if (resBytes != null && resBytes.length > 0) {
            String mime = getMimeTypeFromFilename(resName);
            return "data:" + mime + ";base64," + Base64.getEncoder().encodeToString(resBytes);
        }

        // Nếu không đọc được file, trả về nguyên bản chuỗi
        return trimmed;
    }

    /**
     * Chuyển đổi File ảnh thành chuỗi Base64 Data URI
     */
    public static String fileToBase64(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            return null;
        }
        try {
            byte[] bytes = Files.readAllBytes(file.toPath());
            String mime = getMimeTypeFromFilename(file.getName());
            return "data:" + mime + ";base64," + Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            System.err.println("[ImageBase64Util] Lỗi đọc file to Base64: " + e.getMessage());
            return null;
        }
    }

    /**
     * Giải mã chuỗi Base64 Data URI thành BufferedImage để hiển thị trên UI
     */
    public static BufferedImage base64ToImage(String base64Str) {
        if (base64Str == null || base64Str.trim().isEmpty()) {
            return null;
        }
        String clean = base64Str.trim();
        int commaIdx = clean.indexOf(',');
        if (clean.startsWith("data:") && commaIdx >= 0) {
            clean = clean.substring(commaIdx + 1);
        }
        try {
            byte[] bytes = Base64.getDecoder().decode(clean);
            return ImageIO.read(new ByteArrayInputStream(bytes));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Kiểm tra xem chuỗi có phải là ảnh Base64 hợp lệ không
     */
    public static boolean isBase64Image(String str) {
        if (str == null || str.trim().isEmpty()) return false;
        String s = str.trim();
        return (s.startsWith("data:image/") && s.contains(";base64,")) || isLikelyRawBase64(s);
    }

    private static boolean isLikelyRawBase64(String s) {
        if (s.length() < 100) return false;
        // Kiểm tra một số ký tự đầu xem có phải bảng mã Base64 không
        for (int i = 0; i < Math.min(s.length(), 60); i++) {
            char c = s.charAt(i);
            if (!Character.isLetterOrDigit(c) && c != '+' && c != '/' && c != '=' && c != '\r' && c != '\n') {
                return false;
            }
        }
        return true;
    }

    private static byte[] readResourceBytes(String resPath) {
        try (InputStream is = ImageBase64Util.class.getResourceAsStream(resPath)) {
            if (is != null) {
                return is.readAllBytes();
            }
        } catch (Exception ignored) {}
        return null;
    }

    private static String getMimeTypeFromFilename(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".bmp")) return "image/bmp";
        if (lower.endsWith(".webp")) return "image/webp";
        return "image/png";
    }
}
