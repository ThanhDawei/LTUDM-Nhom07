package com.quizzlive.client.util;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Tiện ích quản lý và hiển thị Ảnh Đại Diện (Avatar) trực quan cho Client Quizzlive.
 * Hỗ trợ nạp ảnh PNG từ resources hoặc tự động vẽ đồ họa vector 2D sắc nét (hình tròn, gradient, biểu tượng).
 */
public class AvatarUtil {
    private static final Map<String, Image> CACHE = new HashMap<>();

    public static final String[] AVAILABLE_AVATARS = {
            "avatar1.png", // Học giả tri thức (Xanh dương)
            "avatar2.png", // Nữ vương trắc nghiệm (Hồng tím)
            "avatar3.png", // Tia chớp tốc độ (Cam vàng)
            "avatar4.png", // Ngôi sao trí tuệ (Xanh lục)
            "avatar5.png"  // Cúp vô địch (Đỏ ruby)
    };

    /**
     * Lấy ImageIcon hình tròn theo kích thước mong muốn (size x size)
     */
    public static ImageIcon getAvatarIcon(String avatarName, int size) {
        if (avatarName == null || avatarName.trim().isEmpty()) {
            avatarName = "avatar1.png";
        }
        avatarName = avatarName.trim().toLowerCase();
        if (!avatarName.endsWith(".png")) {
            avatarName = avatarName + ".png";
        }

        String cacheKey = avatarName + "_" + size;
        if (CACHE.containsKey(cacheKey)) {
            return new ImageIcon(CACHE.get(cacheKey));
        }

        BufferedImage baseImage = loadOrGenerateAvatar(avatarName, size);
        CACHE.put(cacheKey, baseImage);
        return new ImageIcon(baseImage);
    }

    private static BufferedImage loadOrGenerateAvatar(String name, int size) {
        // 1. Thử nạp từ resources
        try (InputStream is = AvatarUtil.class.getResourceAsStream("/avatars/" + name)) {
            if (is != null) {
                BufferedImage original = ImageIO.read(is);
                if (original != null) {
                    return makeCircularAvatar(original, size);
                }
            }
        } catch (Exception ignored) {}

        // 2. Thử nạp từ file hệ thống cục bộ
        try {
            File localFile = new File("quizzlive-client/src/main/resources/avatars/" + name);
            if (localFile.exists()) {
                BufferedImage original = ImageIO.read(localFile);
                if (original != null) {
                    return makeCircularAvatar(original, size);
                }
            }
        } catch (Exception ignored) {}

        // 3. Nếu chưa có ảnh, tự động vẽ Avatar Vector 2D tuyệt đẹp
        return createVectorAvatar(name, size);
    }

    /**
     * Bo tròn ảnh thành hình tròn kèm viền thanh lịch
     */
    private static BufferedImage makeCircularAvatar(BufferedImage src, int size) {
        BufferedImage output = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = output.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // Tạo clip hình tròn
        g2.setClip(new Ellipse2D.Float(1, 1, size - 2, size - 2));
        g2.drawImage(src, 0, 0, size, size, null);
        g2.setClip(null);

        // Vẽ viền tròn mềm mại
        g2.setColor(new Color(255, 255, 255, 200));
        g2.setStroke(new BasicStroke(2.0f));
        g2.drawOval(1, 1, size - 3, size - 3);

        g2.dispose();
        return output;
    }

    /**
     * Tạo Avatar Vector đồ họa cao cấp 2D theo phong cách phẳng (Material Gradient + Icon)
     */
    public static BufferedImage createVectorAvatar(String name, int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        Color c1, c2;
        String symbol;
        switch (name) {
            case "avatar2.png":
                c1 = new Color(142, 36, 170); // Tím
                c2 = new Color(233, 30, 99);  // Hồng
                symbol = "👑";
                break;
            case "avatar3.png":
                c1 = new Color(239, 108, 0);  // Cam đậm
                c2 = new Color(255, 179, 0);  // Vàng
                symbol = "⚡";
                break;
            case "avatar4.png":
                c1 = new Color(46, 125, 50);  // Xanh lá đậm
                c2 = new Color(0, 150, 136);  // Ngọc bích
                symbol = "⭐";
                break;
            case "avatar5.png":
                c1 = new Color(198, 40, 40);  // Đỏ ruby
                c2 = new Color(255, 112, 67); // Cam đỏ
                symbol = "🏆";
                break;
            default: // avatar1.png
                c1 = new Color(21, 101, 192); // Xanh dương
                c2 = new Color(0, 180, 216);  // Cyan
                symbol = "🎓";
                break;
        }

        // Vẽ nền Gradient hình tròn
        GradientPaint gp = new GradientPaint(0, 0, c1, size, size, c2);
        g2.setPaint(gp);
        g2.fillOval(2, 2, size - 4, size - 4);

        // Vẽ viền tròn trắng
        g2.setColor(new Color(255, 255, 255, 220));
        g2.setStroke(new BasicStroke(Math.max(1.5f, size / 32f)));
        g2.drawOval(2, 2, size - 5, size - 5);

        // Vẽ biểu tượng icon trung tâm
        int fontSize = (int) (size * 0.48);
        Font font = new Font("Segoe UI Emoji", Font.PLAIN, fontSize);
        g2.setFont(font);
        FontMetrics fm = g2.getFontMetrics();

        // Nếu font không hỗ trợ emoji thì vẽ chữ A1-A5
        if (font.canDisplayUpTo(symbol) != -1) {
            font = new Font("Segoe UI", Font.BOLD, (int) (size * 0.42));
            g2.setFont(font);
            fm = g2.getFontMetrics();
            symbol = name.replace("avatar", "P").replace(".png", "");
        }

        int x = (size - fm.stringWidth(symbol)) / 2;
        int y = ((size - fm.getHeight()) / 2) + fm.getAscent();

        // Bóng chữ mờ
        g2.setColor(new Color(0, 0, 0, 70));
        g2.drawString(symbol, x + 1, y + 1);

        // Chữ chính
        g2.setColor(Color.WHITE);
        g2.drawString(symbol, x, y);

        g2.dispose();
        return img;
    }

    /**
     * Khởi tạo và ghi file PNG ra thư mục resources để ứng dụng luôn có sẵn file ảnh thực tế
     */
    public static void generateAndSaveAllAvatars(File targetDir) {
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }
        for (String avName : AVAILABLE_AVATARS) {
            try {
                File file = new File(targetDir, avName);
                BufferedImage img = createVectorAvatar(avName, 128);
                ImageIO.write(img, "PNG", file);
                System.out.println("[AvatarUtil] Đã tạo file ảnh đại diện thực tế: " + file.getAbsolutePath());
            } catch (Exception e) {
                System.err.println("[AvatarUtil] Lỗi ghi avatar: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        File resDir = new File("quizzlive-client/src/main/resources/avatars");
        generateAndSaveAllAvatars(resDir);
    }
}
