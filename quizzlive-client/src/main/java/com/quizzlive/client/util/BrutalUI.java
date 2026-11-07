package com.quizzlive.client.util;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Bộ thư viện UI thiết kế chuẩn phong cách NEO-BRUTALIST (Trắng - Đen Chủ Đạo)
 * Phục vụ chuẩn hóa GUI theo file thiết kế master_gui_showcase.html
 */
public class BrutalUI {

    // --- BẢNG MÀU CHỦ ĐẠO (Neo-Brutalist Light Palette) ---
    public static final Color COLOR_BG = new Color(244, 244, 245);          // Nền xám nhạt #F4F4F5
    public static final Color COLOR_CARD = Color.WHITE;                      // Nền Card trắng tinh
    public static final Color COLOR_BLACK = new Color(9, 9, 11);            // Đen tuyền #09090B
    public static final Color COLOR_MUTED = new Color(113, 113, 122);        // Chữ phụ xám #71717A
    public static final Color COLOR_LIGHT_GRAY = new Color(244, 244, 245);   // Xám nhẹ

    // Màu trạng thái đáp án
    public static final Color COLOR_CORRECT_BG = new Color(220, 252, 231);     // Xanh lá pastel #DCFCE7
    public static final Color COLOR_CORRECT_BORDER = new Color(22, 163, 74);   // Viền xanh lá #16A34A
    public static final Color COLOR_CORRECT_TEXT = new Color(21, 128, 61);     // Chữ xanh lá #15803D

    public static final Color COLOR_WRONG_BG = new Color(254, 226, 226);       // Đỏ pastel #FEE2E2
    public static final Color COLOR_WRONG_BORDER = new Color(220, 38, 38);     // Viền đỏ #DC2626
    public static final Color COLOR_WRONG_TEXT = new Color(185, 28, 28);       // Chữ đỏ #B91C1C

    public static final Color COLOR_SELECTED_BG = new Color(254, 243, 199);    // Vàng pastel #FEF3C7
    public static final Color COLOR_SELECTED_BORDER = new Color(217, 119, 6);  // Viền cam #D97706
    public static final Color COLOR_SELECTED_TEXT = new Color(146, 64, 14);    // Chữ cam nâu #92400E

    public static final Color COLOR_INFO_BG = new Color(239, 246, 255);        // Xanh dương pastel #EFF6FF
    public static final Color COLOR_INFO_TEXT = new Color(29, 78, 216);        // Chữ xanh dương #1D4ED8

    // --- FONT CHỮ HIỆN ĐẠI ---
    public static final Font FONT_TITLE_XL = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_BODY_BOLD = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_SMALL_BOLD = new Font("Segoe UI", Font.BOLD, 12);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_TIMER = new Font("Segoe UI", Font.BOLD, 24);

    /**
     * Card bo góc viền đen 2px kèm đổ bóng cứng 4px 4px chuẩn Brutalist
     */
    public static class CardPanel extends JPanel {
        private final int arc;
        private final int shadowSize;

        public CardPanel() {
            this(16, 4);
        }

        public CardPanel(int arc, int shadowSize) {
            this.arc = arc;
            this.shadowSize = shadowSize;
            setOpaque(false);
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth() - shadowSize - 3;
            int h = getHeight() - shadowSize - 3;

            // 1. Vẽ bóng cứng màu đen
            g2.setColor(COLOR_BLACK);
            g2.fillRoundRect(shadowSize + 1, shadowSize + 1, w, h, arc, arc);

            // 2. Vẽ nền Card trắng
            g2.setColor(COLOR_CARD);
            g2.fillRoundRect(1, 1, w, h, arc, arc);

            // 3. Vẽ viền đen 2px
            g2.setColor(COLOR_BLACK);
            g2.setStroke(new BasicStroke(2.0f));
            g2.drawRoundRect(1, 1, w, h, arc, arc);

            g2.dispose();
        }

        @Override
        public Insets getInsets() {
            Insets in = super.getInsets();
            return new Insets(in.top, in.left, in.bottom + shadowSize, in.right + shadowSize);
        }
    }

    /**
     * Nút bấm phong cách Neo-Brutalist viền đen có bóng cứng và hiệu ứng nhấn lún xuống
     */
    public static class BrutalButton extends JButton {
        public enum Style { PRIMARY, OUTLINE, DANGER, SUCCESS }

        private final Style style;
        private final int arc;
        private boolean isHover = false;
        private boolean isPressed = false;

        public BrutalButton(String text, Style style) {
            this(text, style, 12);
        }

        public BrutalButton(String text, Style style, int arc) {
            super(text);
            this.style = style;
            this.arc = arc;
            setFont(FONT_BODY_BOLD);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (isEnabled()) { isHover = true; repaint(); }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHover = false;
                    isPressed = false;
                    repaint();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    if (isEnabled()) { isPressed = true; repaint(); }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    isPressed = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int shadow = isPressed ? 1 : 3;
            int offset = isPressed ? 2 : (isHover ? -1 : 0);

            int x = Math.max(1, 1 + offset);
            int y = Math.max(1, 1 + offset);
            int w = getWidth() - 5;
            int h = getHeight() - 5;

            // Bóng đen
            if (!isPressed) {
                g2.setColor(COLOR_BLACK);
                g2.fillRoundRect(x + shadow, y + shadow, w, h, arc, arc);
            }

            // Màu nền theo style
            Color bg;
            Color textCol;
            Color borderCol = COLOR_BLACK;

            if (!isEnabled()) {
                bg = new Color(228, 228, 231);
                textCol = new Color(161, 161, 170);
            } else {
                switch (style) {
                    case PRIMARY:
                        bg = isHover ? new Color(39, 39, 42) : COLOR_BLACK;
                        textCol = Color.WHITE;
                        break;
                    case DANGER:
                        bg = isHover ? new Color(239, 68, 68) : new Color(220, 38, 38);
                        textCol = Color.WHITE;
                        break;
                    case SUCCESS:
                        bg = isHover ? new Color(34, 197, 94) : new Color(22, 163, 74);
                        textCol = Color.WHITE;
                        break;
                    case OUTLINE:
                    default:
                        bg = isHover ? new Color(244, 244, 245) : Color.WHITE;
                        textCol = COLOR_BLACK;
                        break;
                }
            }

            g2.setColor(bg);
            g2.fillRoundRect(x, y, w, h, arc, arc);

            // Viền
            g2.setColor(borderCol);
            g2.setStroke(new BasicStroke(2.0f));
            g2.drawRoundRect(x, y, w, h, arc, arc);

            // Chữ text
            FontMetrics fm = g2.getFontMetrics(getFont());
            int textX = (getWidth() - fm.stringWidth(getText())) / 2 + (isPressed ? 1 : 0);
            int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2 + (isPressed ? 1 : 0) - 2;

            g2.setFont(getFont());
            g2.setColor(textCol);
            g2.drawString(getText(), textX, textY);

            g2.dispose();
        }
    }

    /**
     * Nút bấm Đáp án A, B, C, D chuyên dụng trong Trận Đấu
     * Hiển thị Badge chữ cái (A/B/C/D) riêng biệt và đổi màu trạng thái mượt mà
     */
    public static class AnswerButton extends JButton {
        public enum State { NORMAL, SELECTED, CORRECT, WRONG }

        private final String key;
        private String contentText = "";
        private State state = State.NORMAL;
        private boolean isHover = false;
        private boolean isPressed = false;

        public AnswerButton(String key, String text) {
            super();
            this.key = key;
            this.contentText = text;
            setFont(FONT_BODY_BOLD);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(300, 60));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (isEnabled()) { isHover = true; repaint(); }
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    isHover = false;
                    isPressed = false;
                    repaint();
                }
                @Override
                public void mousePressed(MouseEvent e) {
                    if (isEnabled()) { isPressed = true; repaint(); }
                }
                @Override
                public void mouseReleased(MouseEvent e) {
                    isPressed = false;
                    repaint();
                }
            });
        }

        public void setState(State state) {
            this.state = state;
            repaint();
        }

        public void setAnswerContent(String text) {
            this.contentText = text;
            repaint();
        }

        public String getAnswerContent() {
            return contentText;
        }

        public State getAnswerState() {
            return state;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int shadow = (isPressed || state != State.NORMAL) ? 1 : 3;
            int offset = isPressed ? 2 : (isHover && state == State.NORMAL ? -1 : 0);

            int x = Math.max(1, 1 + offset);
            int y = Math.max(1, 1 + offset);
            int w = getWidth() - 6;
            int h = getHeight() - 6;
            int arc = 14;

            // 1. Bóng đen
            if (!isPressed) {
                g2.setColor(COLOR_BLACK);
                g2.fillRoundRect(x + shadow, y + shadow, w, h, arc, arc);
            }

            // 2. Màu nền & viền theo trạng thái
            Color bg = COLOR_CARD;
            Color border = COLOR_BLACK;
            Color textCol = COLOR_BLACK;
            Color badgeBg = COLOR_LIGHT_GRAY;
            Color badgeText = COLOR_BLACK;

            switch (state) {
                case SELECTED: // Đang chọn - màu vàng pastel
                    bg = COLOR_SELECTED_BG;
                    border = COLOR_SELECTED_BORDER;
                    textCol = COLOR_SELECTED_TEXT;
                    badgeBg = Color.WHITE;
                    badgeText = COLOR_SELECTED_TEXT;
                    break;
                case CORRECT: // Đáp án đúng - màu xanh lá pastel
                    bg = COLOR_CORRECT_BG;
                    border = COLOR_CORRECT_BORDER;
                    textCol = COLOR_CORRECT_TEXT;
                    badgeBg = COLOR_CORRECT_BORDER;
                    badgeText = Color.WHITE;
                    break;
                case WRONG: // Đáp án sai - màu đỏ pastel
                    bg = COLOR_WRONG_BG;
                    border = COLOR_WRONG_BORDER;
                    textCol = COLOR_WRONG_TEXT;
                    badgeBg = COLOR_WRONG_BORDER;
                    badgeText = Color.WHITE;
                    break;
                case NORMAL:
                default:
                    if (isHover && isEnabled()) {
                        bg = new Color(248, 250, 252);
                    }
                    break;
            }

            // Vẽ thân nút
            g2.setColor(bg);
            g2.fillRoundRect(x, y, w, h, arc, arc);

            // Vẽ viền
            g2.setColor(border);
            g2.setStroke(new BasicStroke(2.0f));
            g2.drawRoundRect(x, y, w, h, arc, arc);

            // 3. Vẽ ô Badge chữ cái (A, B, C, D) bên trái
            int badgeSize = 34;
            int badgeX = x + 12;
            int badgeY = y + (h - badgeSize) / 2;

            g2.setColor(badgeBg);
            g2.fillRoundRect(badgeX, badgeY, badgeSize, badgeSize, 10, 10);
            g2.setColor(border);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(badgeX, badgeY, badgeSize, badgeSize, 10, 10);

            // Vẽ chữ cái A/B/C/D
            g2.setFont(new Font("Segoe UI", Font.BOLD, 15));
            FontMetrics fmb = g2.getFontMetrics();
            int bx = badgeX + (badgeSize - fmb.stringWidth(key)) / 2;
            int by = badgeY + (badgeSize + fmb.getAscent() - fmb.getDescent()) / 2 - 1;
            g2.setColor(badgeText);
            g2.drawString(key, bx, by);

            // 4. Vẽ Nội dung đáp án (có cắt chuỗi nếu quá dài)
            g2.setFont(FONT_BODY_BOLD);
            g2.setColor(textCol);
            FontMetrics fmt = g2.getFontMetrics();
            int maxTextWidth = w - badgeSize - 40;

            String displayText = contentText;
            if (fmt.stringWidth(displayText) > maxTextWidth) {
                while (displayText.length() > 3 && fmt.stringWidth(displayText + "...") > maxTextWidth) {
                    displayText = displayText.substring(0, displayText.length() - 1);
                }
                displayText += "...";
            }

            int textX = badgeX + badgeSize + 14;
            int textY = y + (h + fmt.getAscent() - fmt.getDescent()) / 2 - 1;
            g2.drawString(displayText, textX, textY);

            g2.dispose();
        }
    }

    /**
     * Thanh đếm ngược thời gian Neo-Brutalist viền đen đổi màu theo thời gian
     */
    public static class BrutalProgressBar extends JProgressBar {
        public BrutalProgressBar(int min, int max) {
            super(min, max);
            setOpaque(false);
            setBorderPainted(false);
            setPreferredSize(new Dimension(300, 16));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth() - 2;
            int h = getHeight() - 2;
            int arc = 8;

            // Nền rãnh xám nhạt
            g2.setColor(new Color(244, 244, 245));
            g2.fillRoundRect(1, 1, w, h, arc, arc);

            // Thanh tiến trình
            double percent = (double) (getValue() - getMinimum()) / (double) (getMaximum() - getMinimum());
            int fillW = (int) (w * Math.max(0, Math.min(1, percent)));

            if (fillW > 0) {
                Color fillCol;
                if (getValue() >= 6) {
                    fillCol = new Color(34, 197, 94); // Xanh lá
                } else if (getValue() >= 3) {
                    fillCol = new Color(249, 115, 22); // Cam
                } else {
                    fillCol = new Color(239, 68, 68); // Đỏ khẩn cấp
                }

                g2.setColor(fillCol);
                g2.fillRoundRect(1, 1, fillW, h, arc, arc);
            }

            // Viền đen 2px
            g2.setColor(COLOR_BLACK);
            g2.setStroke(new BasicStroke(2.0f));
            g2.drawRoundRect(1, 1, w, h, arc, arc);

            g2.dispose();
        }
    }

    /**
     * Huy hiệu Badge dạng viên thuốc bo tròn (Pill Badge)
     */
    public static JLabel createBadge(String text, Color bg, Color textCol) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, getHeight(), getHeight());
                g2.setColor(COLOR_BLACK);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, getHeight(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lbl.setFont(FONT_SMALL_BOLD);
        lbl.setForeground(textCol);
        lbl.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
        return lbl;
    }
}

