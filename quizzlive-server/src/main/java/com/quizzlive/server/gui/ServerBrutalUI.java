package com.quizzlive.server.gui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Bộ component UI chuẩn Neo-Brutalist dành riêng cho Server Admin GUI
 * (Nền trắng viền đen, đổ bóng cứng, không phụ thuộc module client)
 */
public class ServerBrutalUI {

    public static final Color COLOR_BG = new Color(244, 244, 245);
    public static final Color COLOR_CARD = Color.WHITE;
    public static final Color COLOR_BLACK = new Color(9, 9, 11);
    public static final Color COLOR_MUTED = new Color(113, 113, 122);

    public static final Color COLOR_PRIMARY_BG = new Color(37, 99, 235);
    public static final Color COLOR_SUCCESS_BG = new Color(22, 163, 74);
    public static final Color COLOR_SUCCESS_LIGHT = new Color(220, 252, 231);
    public static final Color COLOR_SUCCESS_TEXT = new Color(21, 128, 61);

    public static final Color COLOR_DANGER_BORDER = new Color(220, 38, 38);
    public static final Color COLOR_DANGER_LIGHT = new Color(254, 226, 226);
    public static final Color COLOR_DANGER_TEXT = new Color(185, 28, 28);

    public static final Color COLOR_AMBER_LIGHT = new Color(254, 243, 199);
    public static final Color COLOR_AMBER_TEXT = new Color(146, 64, 14);

    public static final Color COLOR_INFO_LIGHT = new Color(239, 246, 255);
    public static final Color COLOR_INFO_TEXT = new Color(29, 78, 216);

    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_BODY_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL_BOLD = new Font("Segoe UI", Font.BOLD, 12);

    /**
     * Card bo góc viền đen 2px kèm bóng cứng
     */
    public static class CardPanel extends JPanel {
        private final int arc;
        private final int shadowSize;

        public CardPanel() {
            this(14, 4);
        }

        public CardPanel(int arc, int shadowSize) {
            this.arc = arc;
            this.shadowSize = shadowSize;
            setOpaque(false);
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth() - shadowSize - 3;
            int h = getHeight() - shadowSize - 3;

            g2.setColor(COLOR_BLACK);
            g2.fillRoundRect(shadowSize + 1, shadowSize + 1, w, h, arc, arc);

            g2.setColor(COLOR_CARD);
            g2.fillRoundRect(1, 1, w, h, arc, arc);

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
     * Nút bấm Neo-Brutalist tự vẽ, không bị lỗi màu trên Windows LookAndFeel
     */
    public static class BrutalButton extends JButton {
        public enum Style { PRIMARY, OUTLINE, DANGER, SUCCESS, DARK }

        private final Style style;
        private final int arc = 10;
        private boolean isHover = false;
        private boolean isPressed = false;

        public BrutalButton(String text, Style style) {
            super(text);
            this.style = style;
            setFont(FONT_SMALL_BOLD);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(9, 16, 11, 18));

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

            int shadow = isPressed ? 1 : (isHover ? 4 : 3);
            int offset = isPressed ? 2 : 0;
            int w = getWidth() - 5;
            int h = getHeight() - 5;

            Color bg;
            Color fg;
            Color borderColor = COLOR_BLACK;
            Color shadowColor = COLOR_BLACK;

            switch (style) {
                case PRIMARY:
                    bg = isHover ? new Color(29, 78, 216) : COLOR_PRIMARY_BG;
                    fg = Color.WHITE;
                    break;
                case SUCCESS:
                    bg = isHover ? new Color(21, 128, 61) : COLOR_SUCCESS_BG;
                    fg = Color.WHITE;
                    break;
                case DANGER:
                    bg = isHover ? COLOR_DANGER_LIGHT : Color.WHITE;
                    fg = COLOR_DANGER_TEXT;
                    borderColor = COLOR_DANGER_BORDER;
                    shadowColor = COLOR_DANGER_BORDER;
                    break;
                case DARK:
                    bg = isHover ? new Color(39, 39, 42) : COLOR_BLACK;
                    fg = Color.WHITE;
                    shadowColor = COLOR_PRIMARY_BG;
                    break;
                case OUTLINE:
                default:
                    bg = isHover ? COLOR_BG : Color.WHITE;
                    fg = COLOR_BLACK;
                    break;
            }

            g2.setColor(shadowColor);
            g2.fillRoundRect(1 + shadow, 1 + shadow, w, h, arc, arc);

            g2.setColor(bg);
            g2.fillRoundRect(1 + offset, 1 + offset, w, h, arc, arc);

            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(2.0f));
            g2.drawRoundRect(1 + offset, 1 + offset, w, h, arc, arc);

            FontMetrics fm = g2.getFontMetrics(getFont());
            int textX = offset + (w - fm.stringWidth(getText())) / 2 + 1;
            int textY = offset + (h - fm.getHeight()) / 2 + fm.getAscent() + 1;
            g2.setFont(getFont());
            g2.setColor(fg);
            g2.drawString(getText(), textX, textY);

            g2.dispose();
        }
    }

    /**
     * Nút chuyển Tab Neo-Brutalist tự vẽ (luôn giữ chữ đen rõ nét, không bị Windows L&F đè màu)
     */
    public static class TabButton extends JButton {
        private boolean active;
        private boolean isHover = false;
        private final int arc = 10;

        public TabButton(String text, boolean active) {
            super(text);
            this.active = active;
            setFont(FONT_SMALL_BOLD);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(9, 18, 11, 20));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHover = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHover = false;
                    repaint();
                }
            });
        }

        public void setActive(boolean active) {
            this.active = active;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int shadow = active ? 3 : 0;
            int w = getWidth() - 5;
            int h = getHeight() - 5;

            if (active) {
                g2.setColor(COLOR_BLACK);
                g2.fillRoundRect(1 + shadow, 1 + shadow, w, h, arc, arc);

                g2.setColor(COLOR_INFO_LIGHT);
                g2.fillRoundRect(1, 1, w, h, arc, arc);

                g2.setColor(COLOR_BLACK);
                g2.setStroke(new BasicStroke(2.0f));
                g2.drawRoundRect(1, 1, w, h, arc, arc);
            } else {
                g2.setColor(isHover ? new Color(228, 228, 231) : COLOR_BG);
                g2.fillRoundRect(1, 1, w, h, arc, arc);

                g2.setColor(isHover ? COLOR_BLACK : new Color(212, 212, 216));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, w, h, arc, arc);
            }

            FontMetrics fm = g2.getFontMetrics(getFont());
            int textX = (w - fm.stringWidth(getText())) / 2 + 1;
            int textY = (h - fm.getHeight()) / 2 + fm.getAscent() + 1;
            g2.setFont(getFont());
            g2.setColor(active ? COLOR_BLACK : COLOR_MUTED);
            g2.drawString(getText(), textX, textY);

            g2.dispose();
        }
    }

    /**
     * Badge trạng thái bo tròn
     */
    public static JLabel createBadge(String text, Color bg, Color fg, Color borderColor) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(fg);
        lbl.setBackground(bg);
        lbl.setOpaque(true);
        Border line = BorderFactory.createLineBorder(borderColor, 1, true);
        Border pad = BorderFactory.createEmptyBorder(4, 10, 4, 10);
        lbl.setBorder(BorderFactory.createCompoundBorder(line, pad));
        return lbl;
    }

    public static void styleTextField(JTextField field) {
        field.setFont(FONT_BODY);
        field.setBackground(Color.WHITE);
        field.setForeground(COLOR_BLACK);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BLACK, 2),
                BorderFactory.createEmptyBorder(7, 10, 7, 10)
        ));
    }

    public static void styleComboBox(JComboBox<?> box) {
        box.setFont(FONT_SMALL_BOLD);
        box.setBackground(Color.WHITE);
        box.setForeground(COLOR_BLACK);
        box.setBorder(BorderFactory.createLineBorder(COLOR_BLACK, 2));
        box.setPreferredSize(new Dimension(box.getPreferredSize().width + 16, 36));
    }

    public static void styleTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setRowHeight(34);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(228, 228, 231));
        table.setSelectionBackground(COLOR_INFO_LIGHT);
        table.setSelectionForeground(COLOR_BLACK);
        table.setFillsViewportHeight(true);
        table.setIntercellSpacing(new Dimension(12, 0));

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_SMALL_BOLD);
        header.setBackground(COLOR_BG);
        header.setForeground(COLOR_BLACK);
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 38));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, COLOR_BLACK));
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);
    }

    public static JScrollPane wrapScrollTable(JTable table) {
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(COLOR_BLACK, 2));
        scroll.getViewport().setBackground(Color.WHITE);
        return scroll;
    }
}
