package com.quizzlive.client.util;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Tiện ích vẽ và tạo hình ảnh trực quan cho các câu đố IQ (Yêu cầu 11: Test IQ bằng hình ảnh).
 * Tự động tạo đồ họa vector sắc nét (đồng hồ quay, ma trận hình học, cán cân, dãy số logic).
 */
public class IQImageUtil {

    public static ImageIcon getIQImage(String imageName, String content, int width, int height) {
        // 1. Thử nạp từ file hình ảnh thực tế trên máy / thư mục data
        if (imageName != null && !imageName.trim().isEmpty()) {
            String imgTrim = imageName.trim();
            File[] candidates = new File[]{
                    new File(imgTrim),
                    new File("data/" + imgTrim),
                    new File("data/images/" + imgTrim),
                    new File("../data/" + imgTrim),
                    new File("quizzlive-client/src/main/resources/images/" + imgTrim),
                    new File("quizzlive-client/src/main/resources/" + imgTrim)
            };
            for (File f : candidates) {
                if (f.exists() && f.isFile()) {
                    try {
                        BufferedImage orig = javax.imageio.ImageIO.read(f);
                        if (orig != null) {
                            Image scaled = orig.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                            return new ImageIcon(scaled);
                        }
                    } catch (Exception ignored) {}
                }
            }

            // Thử nạp từ classpath resources
            try (java.io.InputStream is = IQImageUtil.class.getResourceAsStream("/images/" + imgTrim)) {
                if (is != null) {
                    BufferedImage orig = javax.imageio.ImageIO.read(is);
                    if (orig != null) {
                        return new ImageIcon(orig.getScaledInstance(width, height, Image.SCALE_SMOOTH));
                    }
                }
            } catch (Exception ignored) {}
        }

        // 2. Tạo hình ảnh đồ họa thông minh dựa trên nội dung câu đố
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Nền thẻ câu đố
        GradientPaint bgGradient = new GradientPaint(0, 0, new Color(248, 250, 253), 0, height, new Color(238, 242, 248));
        g2.setPaint(bgGradient);
        g2.fill(new RoundRectangle2D.Double(0, 0, width, height, 16, 16));

        g2.setColor(new Color(215, 225, 235));
        g2.setStroke(new BasicStroke(1.5f));
        g2.draw(new RoundRectangle2D.Double(1, 1, width - 2, height - 2, 16, 16));

        if (imageName != null && imageName.contains("completed")) {
            drawCompletedGraphic(g2, width, height);
        } else if ((content != null && (content.contains("xoay") || content.contains("kim đồng hồ") || content.contains("góc"))) ||
                (imageName != null && imageName.contains("pattern_01"))) {
            drawClockRotationGraphic(g2, width, height);
        } else if ((content != null && (content.contains("ma trận") || content.contains("mảnh ghép") || content.contains("hình học"))) ||
                (imageName != null && imageName.contains("pattern_02"))) {
            drawShapeMatrixGraphic(g2, width, height);
        } else if (content != null && (content.contains("cân") || content.contains("tiền"))) {
            drawBalanceScaleGraphic(g2, width, height);
        } else if (content != null && (content.contains("dãy số") || content.contains("Fibonacci") || content.contains("chính phương"))) {
            drawNumberSequenceGraphic(g2, content, width, height);
        } else {
            drawGeneralLogicGraphic(g2, content, width, height);
        }

        g2.dispose();
        return new ImageIcon(img);
    }

    private static void drawClockRotationGraphic(Graphics2D g2, int width, int height) {
        int boxW = 80;
        int boxH = 80;
        int startX = (width - (boxW * 4 + 45)) / 2;
        int y = (height - boxH) / 2 + 8;

        // Tiêu đề quy luật
        g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
        g2.setColor(new Color(41, 128, 185));
        g2.drawString("QUY LUẬT GÓC XOAY THEO CHIỀU KIM ĐỒNG HỒ (45° / BƯỚC)", startX, y - 18);

        int[] angles = {0, 45, 90, -1}; // -1 đại diện cho ?
        String[] labels = {"Bước 1: 0°", "Bước 2: 45°", "Bước 3: 90°", "Bước 4: [ ? ]"};

        for (int i = 0; i < 4; i++) {
            int bx = startX + i * (boxW + 15);

            // Khung tròn đồng hồ
            g2.setColor(Color.WHITE);
            g2.fill(new RoundRectangle2D.Double(bx, y, boxW, boxH, 12, 12));
            g2.setColor(i == 3 ? new Color(230, 126, 34) : new Color(189, 195, 199));
            g2.setStroke(new BasicStroke(i == 3 ? 2.0f : 1.2f));
            g2.draw(new RoundRectangle2D.Double(bx, y, boxW, boxH, 12, 12));

            int cx = bx + boxW / 2;
            int cy = y + boxH / 2;

            if (i < 3) {
                // Vẽ mặt đồng hồ nhỏ
                g2.setColor(new Color(240, 243, 246));
                g2.fill(new Ellipse2D.Double(cx - 28, cy - 28, 56, 56));
                g2.setColor(new Color(200, 205, 215));
                g2.draw(new Ellipse2D.Double(cx - 28, cy - 28, 56, 56));

                // Vẽ kim chỉ góc
                double rad = Math.toRadians(angles[i] - 90);
                int kx = (int) (cx + 22 * Math.cos(rad));
                int ky = (int) (cy + 22 * Math.sin(rad));

                g2.setColor(new Color(192, 57, 43));
                g2.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(cx, cy, kx, ky);

                g2.setColor(new Color(44, 62, 80));
                g2.fill(new Ellipse2D.Double(cx - 3, cy - 3, 6, 6));
            } else {
                // Ô dấu hỏi chấm
                g2.setColor(new Color(230, 126, 34));
                g2.setFont(new Font("Segoe UI", Font.BOLD, 28));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString("?", cx - fm.stringWidth("?") / 2, cy + fm.getAscent() / 2 - 2);
            }

            // Nhãn phía dưới
            g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
            g2.setColor(i == 3 ? new Color(230, 126, 34) : new Color(127, 140, 141));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(labels[i], bx + (boxW - fm.stringWidth(labels[i])) / 2, y + boxH + 14);

            // Mũi tên liên kết
            if (i < 3) {
                g2.setColor(new Color(52, 152, 219));
                g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                g2.drawString("➜", bx + boxW + 2, cy + 5);
            }
        }
    }

    private static void drawShapeMatrixGraphic(Graphics2D g2, int width, int height) {
        int startX = (width - 320) / 2;
        int startY = (height - 110) / 2;

        g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
        g2.setColor(new Color(142, 68, 173));
        g2.drawString("MA TRẬN QUY LUẬT HÌNH HỌC (SỐ CẠNH ĐA GIÁC TĂNG DẦN)", startX, startY - 8);

        String[] row1 = {"△ (3 cạnh)", "▢ (4 cạnh)", "⬠ (5 cạnh)"};
        String[] row2 = {"⬠ (5 cạnh)", "▢ (4 cạnh)", "[ ? ]"};

        int cardW = 95;
        int cardH = 38;

        for (int i = 0; i < 3; i++) {
            // Hàng 1
            drawMiniCard(g2, startX + i * 110, startY + 5, cardW, cardH, row1[i], new Color(41, 128, 185));
            // Hàng 2
            boolean isMissing = (i == 2);
            drawMiniCard(g2, startX + i * 110, startY + 52, cardW, cardH, row2[i], isMissing ? new Color(230, 126, 34) : new Color(39, 174, 96));
        }
    }

    private static void drawBalanceScaleGraphic(Graphics2D g2, int width, int height) {
        int cx = width / 2;
        int cy = height / 2 + 10;

        g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
        g2.setColor(new Color(230, 126, 34));
        g2.drawString("CÂN ĐĨA LOGIC: TỐI THIỂU SỐ LẦN ĐỂ XÁC ĐỊNH ĐỒNG GIẢ", cx - 180, cy - 40);

        // Trụ cân
        g2.setColor(new Color(127, 140, 141));
        g2.setStroke(new BasicStroke(4.0f));
        g2.drawLine(cx, cy - 25, cx, cy + 30);
        g2.drawLine(cx - 30, cy + 30, cx + 30, cy + 30);

        // Đòn cân
        g2.setStroke(new BasicStroke(3.0f));
        g2.drawLine(cx - 100, cy - 25, cx + 100, cy - 25);

        // Đĩa trái & đĩa phải
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawLine(cx - 100, cy - 25, cx - 120, cy);
        g2.drawLine(cx - 100, cy - 25, cx - 80, cy);
        g2.drawArc(cx - 125, cy - 5, 50, 20, 180, 180);

        g2.drawLine(cx + 100, cy - 25, cx + 80, cy);
        g2.drawLine(cx + 100, cy - 25, cx + 120, cy);
        g2.drawArc(cx + 75, cy - 5, 50, 20, 180, 180);

        // Đồng xu
        g2.setColor(new Color(241, 196, 15));
        g2.fillOval(cx - 110, cy - 3, 14, 14);
        g2.fillOval(cx - 95, cy - 3, 14, 14);

        g2.setColor(new Color(230, 126, 34));
        g2.fillOval(cx + 85, cy - 3, 14, 14);
        g2.fillOval(cx + 100, cy - 3, 14, 14);
    }

    private static void drawNumberSequenceGraphic(Graphics2D g2, String content, int width, int height) {
        int cx = width / 2;
        int cy = height / 2;

        g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
        g2.setColor(new Color(39, 174, 96));
        g2.drawString("QUY LUẬT DÃY SỐ LOGIC TIẾP DIỄN", (width - 230) / 2, cy - 25);

        // Vẽ chuỗi thẻ số
        String[] samples = {"Step 1", "Step 2", "Step 3", "Step 4", "[ ? ]"};
        if (content != null && content.contains(",")) {
            String sub = content.substring(content.indexOf(":") + 1);
            if (sub.contains("?")) sub = sub.replace("?", "").replace("...", "");
            String[] parts = sub.trim().split(",");
            if (parts.length >= 3) {
                samples = new String[Math.min(parts.length + 1, 5)];
                for (int i = 0; i < samples.length - 1; i++) {
                    samples[i] = parts[i].trim();
                }
                samples[samples.length - 1] = "[ ? ]";
            }
        }

        int startX = (width - (samples.length * 60 + (samples.length - 1) * 12)) / 2;
        for (int i = 0; i < samples.length; i++) {
            int x = startX + i * 72;
            boolean isTarget = i == samples.length - 1;

            g2.setColor(Color.WHITE);
            g2.fill(new RoundRectangle2D.Double(x, cy - 5, 60, 36, 10, 10));
            g2.setColor(isTarget ? new Color(230, 126, 34) : new Color(41, 128, 185));
            g2.setStroke(new BasicStroke(isTarget ? 2.0f : 1.2f));
            g2.draw(new RoundRectangle2D.Double(x, cy - 5, 60, 36, 10, 10));

            g2.setFont(new Font("Segoe UI", Font.BOLD, isTarget ? 14 : 13));
            g2.setColor(isTarget ? new Color(230, 126, 34) : new Color(44, 62, 80));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(samples[i], x + (60 - fm.stringWidth(samples[i])) / 2, cy + 18);

            if (i < samples.length - 1) {
                g2.setColor(new Color(189, 195, 199));
                g2.drawString("➜", x + 62, cy + 18);
            }
        }
    }

    private static void drawGeneralLogicGraphic(Graphics2D g2, String content, int width, int height) {
        int cx = width / 2;
        int cy = height / 2;

        g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
        g2.setColor(new Color(41, 128, 185));
        g2.drawString("🧩 BÀI TOÁN TƯ DUY LOGIC & TRÍ TUỆ HÌNH HỌC", (width - 320) / 2, cy - 15);

        g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        g2.setColor(new Color(127, 140, 141));
        String text = (content != null && content.length() > 50) ? (content.substring(0, 48) + "...") : content;
        if (text != null) {
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(text, (width - fm.stringWidth(text)) / 2, cy + 8);
        }

        g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
        g2.setColor(new Color(230, 126, 34));
        g2.drawString("💡 [ Lập luận logic ] ➜ [ Suy đoán ] ➜ [ Chọn phương án đúng ]", (width - 440) / 2, cy + 32);
    }

    private static void drawCompletedGraphic(Graphics2D g2, int width, int height) {
        int cx = width / 2;
        int cy = height / 2;

        g2.setFont(new Font("Segoe UI", Font.BOLD, 22));
        g2.setColor(new Color(212, 175, 55));
        g2.drawString("🏆 HOÀN THÀNH TẤT CẢ CÂU ĐỐ IQ!", (width - 360) / 2, cy - 5);

        g2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        g2.setColor(new Color(39, 174, 96));
        g2.drawString("Bạn đã xuất sắc chinh phục toàn bộ câu đố IQ trong ngân hàng câu hỏi.", (width - 440) / 2, cy + 22);
    }

    private static void drawMiniCard(Graphics2D g2, int x, int y, int w, int h, String text, Color accent) {
        g2.setColor(Color.WHITE);
        g2.fill(new RoundRectangle2D.Double(x, y, w, h, 8, 8));
        g2.setColor(accent);
        g2.setStroke(new BasicStroke(1.2f));
        g2.draw(new RoundRectangle2D.Double(x, y, w, h, 8, 8));

        g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(text, x + (w - fm.stringWidth(text)) / 2, y + 23);
    }
}

