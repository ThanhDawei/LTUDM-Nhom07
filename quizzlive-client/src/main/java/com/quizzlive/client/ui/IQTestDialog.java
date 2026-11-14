package com.quizzlive.client.ui;

import com.quizzlive.client.service.ClientIQService;
import com.quizzlive.client.util.BrutalUI;
import com.quizzlive.client.util.IQImageUtil;
import com.quizzlive.shared.dto.BattleDTO;
import com.quizzlive.shared.dto.IQTestDTO;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Hộp thoại làm bài Test IQ hàng ngày (5 câu/ngày)
 * - Trích xuất từ bảng CAU_HOI với loại IQ_TEST
 * - Giới hạn 5 câu / ngày / người chơi
 * - Dù đúng hay sai đều skip ngay qua câu khác
 * - Kết toán sau 5 câu: tổng tối đa 1.0 điểm IQ, mỗi câu sai trừ 0.2 điểm
 */
public class IQTestDialog extends JDialog {
    private final MainClientFrame mainFrame;
    private final ClientIQService iqService = new ClientIQService();

    private Long currentQuestionId;
    private JLabel lblTitle;
    private JLabel lblMeta;
    private JPanel pnlQuestionBox;
    private JLabel lblQuestionText;
    private JPanel pnlImageContainer;
    private JLabel lblImage;
    private ButtonGroup bgOptions;
    private JRadioButton rbOptA, rbOptB, rbOptC, rbOptD;
    private JPanel pnlOptA, pnlOptB, pnlOptC, pnlOptD;
    private JLabel lblResult;
    private BrutalUI.BrutalButton btnSubmit;
    private BrutalUI.BrutalButton btnClose;

    public IQTestDialog(MainClientFrame parent) {
        super(parent, "Bài Test IQ Hàng Ngày (5 câu/ngày) - Quizzlive", true);
        this.mainFrame = parent;

        setSize(680, 600);
        setMinimumSize(new Dimension(600, 520));
        setLocationRelativeTo(parent);
        getContentPane().setBackground(BrutalUI.COLOR_BG);
        setLayout(new BorderLayout(14, 14));

        initComponents();

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                if (mainFrame != null) mainFrame.setActiveIQDialog(null);
            }

            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                if (mainFrame != null) mainFrame.setActiveIQDialog(IQTestDialog.this);
                iqService.requestIQQuestion();
            }
        });
    }

    private void initComponents() {
        // Card trung tâm Neo-Brutalist bao trọn giao diện
        BrutalUI.CardPanel mainCard = new BrutalUI.CardPanel(16, 4);
        mainCard.setLayout(new BorderLayout(12, 12));
        mainCard.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        // 1. Header
        JPanel headerPanel = new JPanel(new BorderLayout(10, 6));
        headerPanel.setOpaque(false);

        JPanel headerTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        headerTop.setOpaque(false);
        JLabel badge = BrutalUI.createBadge("🧠 TEST IQ", new Color(254, 240, 138), BrutalUI.COLOR_BLACK);
        lblTitle = new JLabel("Bài Test IQ Trong Ngày (5 câu/ngày)");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(BrutalUI.COLOR_BLACK);
        headerTop.add(badge);
        headerTop.add(lblTitle);

        JLabel lblSub = new JLabel("<html><span style='color:#71717A; font-size:11px;'>Mỗi ngày làm 5 câu. Tổng 1.0 điểm IQ, mỗi câu sai trừ 0.2 điểm. Kết toán sau 5 câu.</span></html>");
        lblMeta = new JLabel("Đang tải câu hỏi từ cơ sở dữ liệu...");
        lblMeta.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblMeta.setForeground(new Color(37, 99, 235));

        headerPanel.add(headerTop, BorderLayout.NORTH);
        JPanel headerBottom = new JPanel(new GridLayout(2, 1, 0, 2));
        headerBottom.setOpaque(false);
        headerBottom.add(lblSub);
        headerBottom.add(lblMeta);
        headerPanel.add(headerBottom, BorderLayout.SOUTH);

        mainCard.add(headerPanel, BorderLayout.NORTH);

        // 2. Nội dung: Khung câu hỏi (chứa text và ảnh nếu có) + 4 Options
        JPanel bodyPanel = new JPanel(new BorderLayout(10, 10));
        bodyPanel.setOpaque(false);

        pnlQuestionBox = new JPanel(new BorderLayout(8, 8));
        pnlQuestionBox.setBackground(Color.WHITE);
        pnlQuestionBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BrutalUI.COLOR_BLACK, 2),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));

        lblQuestionText = new JLabel("Đang tải nội dung câu hỏi...");
        lblQuestionText.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblQuestionText.setForeground(BrutalUI.COLOR_BLACK);
        lblQuestionText.setVerticalAlignment(SwingConstants.TOP);
        pnlQuestionBox.add(lblQuestionText, BorderLayout.NORTH);

        // Khung ảnh quy luật / minh họa (chỉ hiển thị khi câu hỏi có hình ảnh)
        pnlImageContainer = new JPanel(new BorderLayout());
        pnlImageContainer.setBackground(new Color(250, 250, 252));
        pnlImageContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 225), 1),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        pnlImageContainer.setPreferredSize(new Dimension(0, 160));

        lblImage = new JLabel("", SwingConstants.CENTER);
        lblImage.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblImage.setForeground(BrutalUI.COLOR_BLACK);
        pnlImageContainer.add(lblImage, BorderLayout.CENTER);
        pnlQuestionBox.add(pnlImageContainer, BorderLayout.CENTER);

        bodyPanel.add(pnlQuestionBox, BorderLayout.CENTER);

        // Lưới 4 đáp án 2x2
        JPanel optionsGrid = new JPanel(new GridLayout(2, 2, 10, 10));
        optionsGrid.setOpaque(false);

        rbOptA = createRadio();
        rbOptB = createRadio();
        rbOptC = createRadio();
        rbOptD = createRadio();

        bgOptions = new ButtonGroup();
        bgOptions.add(rbOptA);
        bgOptions.add(rbOptB);
        bgOptions.add(rbOptC);
        bgOptions.add(rbOptD);

        pnlOptA = wrapRadio(rbOptA, "A");
        pnlOptB = wrapRadio(rbOptB, "B");
        pnlOptC = wrapRadio(rbOptC, "C");
        pnlOptD = wrapRadio(rbOptD, "D");

        optionsGrid.add(pnlOptA);
        optionsGrid.add(pnlOptB);
        optionsGrid.add(pnlOptC);
        optionsGrid.add(pnlOptD);
        bodyPanel.add(optionsGrid, BorderLayout.SOUTH);

        mainCard.add(bodyPanel, BorderLayout.CENTER);

        // 3. Footer: Kết quả & Nút bấm
        JPanel footerPanel = new JPanel(new BorderLayout(10, 0));
        footerPanel.setOpaque(false);

        lblResult = new JLabel("Chọn 1 đáp án và bấm Nộp để chuyển sang câu tiếp theo.");
        lblResult.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblResult.setForeground(new Color(113, 113, 122));
        footerPanel.add(lblResult, BorderLayout.CENTER);

        JPanel btnGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnGroup.setOpaque(false);

        btnClose = new BrutalUI.BrutalButton("Đóng", BrutalUI.BrutalButton.Style.OUTLINE);
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnClose.addActionListener(e -> dispose());

        btnSubmit = new BrutalUI.BrutalButton("Nộp & Chuyển câu tiếp ➡️", BrutalUI.BrutalButton.Style.PRIMARY);
        btnSubmit.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSubmit.addActionListener(e -> handleSubmit());

        btnGroup.add(btnClose);
        btnGroup.add(btnSubmit);
        footerPanel.add(btnGroup, BorderLayout.EAST);

        mainCard.add(footerPanel, BorderLayout.SOUTH);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        wrapper.add(mainCard, BorderLayout.CENTER);
        add(wrapper, BorderLayout.CENTER);
    }

    private JRadioButton createRadio() {
        JRadioButton rb = new JRadioButton();
        rb.setOpaque(false);
        rb.setFocusPainted(false);
        rb.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return rb;
    }

    private JPanel wrapRadio(JRadioButton rb, String fallback) {
        JPanel card = new JPanel(new BorderLayout(8, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BrutalUI.COLOR_BLACK, 2),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        rb.setText(fallback);
        rb.setFont(new Font("Segoe UI", Font.BOLD, 12));
        rb.setForeground(BrutalUI.COLOR_BLACK);
        card.add(rb, BorderLayout.CENTER);

        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (rb.isEnabled()) rb.setSelected(true);
            }
        });
        return card;
    }

    private void handleSubmit() {
        if (currentQuestionId == null || currentQuestionId <= 0) {
            JOptionPane.showMessageDialog(this, "Không có câu hỏi IQ nào đang mở!");
            return;
        }

        Long selectedAnswerId = null;
        if (rbOptA.isSelected()) selectedAnswerId = (Long) rbOptA.getClientProperty("answerId");
        else if (rbOptB.isSelected()) selectedAnswerId = (Long) rbOptB.getClientProperty("answerId");
        else if (rbOptC.isSelected()) selectedAnswerId = (Long) rbOptC.getClientProperty("answerId");
        else if (rbOptD.isSelected()) selectedAnswerId = (Long) rbOptD.getClientProperty("answerId");

        if (selectedAnswerId == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 đáp án trước khi nộp!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        btnSubmit.setEnabled(false);
        rbOptA.setEnabled(false);
        rbOptB.setEnabled(false);
        rbOptC.setEnabled(false);
        rbOptD.setEnabled(false);
        lblResult.setForeground(Color.GRAY);
        lblResult.setText("Đang gửi đáp án...");

        iqService.submitIQAnswer(currentQuestionId, selectedAnswerId);
    }

    public void renderIQQuestion(IQTestDTO.IQQuestionNotify question) {
        if (question == null) return;
        try {
            this.currentQuestionId = question.questionId;

            if (question.isDailyCompleted || question.questionId == -1L) {
                if (lblTitle != null) lblTitle.setText("🧠 BÀI TEST IQ TRONG NGÀY (5 CÂU/NGÀY)");
                if (lblMeta != null) {
                    lblMeta.setText("Trạng thái: " + (question.dailyMessage != null ? question.dailyMessage : "Đã hoàn thành 5/5 câu hỏi hôm nay"));
                }

                if (pnlQuestionBox != null) {
                    pnlQuestionBox.remove(lblQuestionText);
                    pnlQuestionBox.remove(pnlImageContainer);
                }
                if (pnlImageContainer != null) pnlImageContainer.setVisible(false);
                if (lblImage != null) {
                    lblImage.setIcon(null);
                    lblImage.setText("");
                }

                if (lblQuestionText != null) {
                    lblQuestionText.setText("<html><div style='font-family: Segoe UI; font-size: 14px; font-weight: bold; color: #16a34a; line-height: 1.5; padding: 16px; text-align: center;'>"
                            + (question.content != null ? question.content.replace("\n", "<br>") : "Hôm nay bạn đã hoàn thành đủ 5 câu hỏi IQ. Hãy quay lại vào ngày mai!")
                            + "</div></html>");
                    if (pnlQuestionBox != null) pnlQuestionBox.add(lblQuestionText, BorderLayout.CENTER);
                }

                if (lblResult != null) {
                    lblResult.setText("Hôm nay bạn đã hoàn thành bài Test IQ. Vui lòng quay lại vào ngày mai!");
                    lblResult.setForeground(new Color(22, 163, 74));
                }
                if (btnSubmit != null) {
                    btnSubmit.setEnabled(false);
                    btnSubmit.setText("Đã hoàn thành");
                }
                if (pnlOptA != null) pnlOptA.setVisible(false);
                if (pnlOptB != null) pnlOptB.setVisible(false);
                if (pnlOptC != null) pnlOptC.setVisible(false);
                if (pnlOptD != null) pnlOptD.setVisible(false);

                if (pnlQuestionBox != null) {
                    pnlQuestionBox.revalidate();
                    pnlQuestionBox.repaint();
                }
                return;
            }

            int idx = question.currentQuestionIndex > 0 ? question.currentQuestionIndex : 1;
            int max = question.maxDailyQuestions > 0 ? question.maxDailyQuestions : 5;

            if (lblTitle != null) {
                lblTitle.setText("Bài Test IQ Trong Ngày - Câu " + idx + "/" + max);
            }
            String topicStr = question.topicName != null ? question.topicName : "Tư duy IQ & Logic";
            String diffStr = question.difficulty != null ? question.difficulty : "MEDIUM";
            if (lblMeta != null) {
                lblMeta.setText("📌 Câu " + idx + "/" + max + " trong ngày • Mã #" + question.questionId + " | Chủ đề: " + topicStr + " | Cấp độ: " + diffStr);
            }

            if (btnSubmit != null) {
                btnSubmit.setEnabled(true);
                btnSubmit.setText("Nộp & Chuyển câu tiếp ➡️");
            }
            if (bgOptions != null) bgOptions.clearSelection();

            List<BattleDTO.QuestionAnswerOption> options = question.options;
            JRadioButton[] rbs = new JRadioButton[] { rbOptA, rbOptB, rbOptC, rbOptD };
            JPanel[] pnls = new JPanel[] { pnlOptA, pnlOptB, pnlOptC, pnlOptD };

            for (int i = 0; i < rbs.length; i++) {
                if (rbs[i] != null && pnls[i] != null) {
                    if (options != null && i < options.size()) {
                        BattleDTO.QuestionAnswerOption opt = options.get(i);
                        rbs[i].setText(opt.key + ". " + (opt.content != null ? opt.content : ""));
                        rbs[i].putClientProperty("answerId", opt.answerId);
                        pnls[i].setVisible(true);
                        rbs[i].setEnabled(true);
                    } else {
                        pnls[i].setVisible(false);
                    }
                }
            }

            // Logic hiển thị: Nếu câu hỏi không có ảnh thì CHỈ hiển thị nội dung; Nếu có ảnh thì hiển thị CẢ 2 (nội dung + ảnh)
            boolean hasImage = (question.imageUrl != null && !question.imageUrl.trim().isEmpty());
            if (pnlQuestionBox != null) {
                pnlQuestionBox.remove(lblQuestionText);
                pnlQuestionBox.remove(pnlImageContainer);

                if (hasImage) {
                    if (lblQuestionText != null) {
                        lblQuestionText.setText("<html><div style='font-family: Segoe UI; font-size: 13px; font-weight: bold; color: #2c3e50; line-height: 1.4;'>"
                                + (question.content != null ? question.content : "") + "</div></html>");
                        pnlQuestionBox.add(lblQuestionText, BorderLayout.NORTH);
                    }
                    if (pnlImageContainer != null) {
                        pnlImageContainer.setVisible(true);
                        pnlQuestionBox.add(pnlImageContainer, BorderLayout.CENTER);
                        int w = pnlImageContainer.getWidth() > 300 ? pnlImageContainer.getWidth() : 520;
                        int h = pnlImageContainer.getHeight() > 100 ? pnlImageContainer.getHeight() : 155;
                        ImageIcon icon = IQImageUtil.getIQImage(question.imageUrl, question.content, w, h);
                        if (lblImage != null) {
                            lblImage.setIcon(icon);
                            lblImage.setText("");
                        }
                    }
                } else {
                    if (lblQuestionText != null) {
                        lblQuestionText.setText("<html><div style='font-family: Segoe UI; font-size: 15px; font-weight: bold; color: #1e293b; line-height: 1.6; padding: 12px;'>"
                                + (question.content != null ? question.content : "") + "</div></html>");
                        pnlQuestionBox.add(lblQuestionText, BorderLayout.CENTER);
                    }
                    if (pnlImageContainer != null) {
                        pnlImageContainer.setVisible(false);
                    }
                    if (lblImage != null) {
                        lblImage.setIcon(null);
                        lblImage.setText("");
                    }
                }

                pnlQuestionBox.revalidate();
                pnlQuestionBox.repaint();
            }
        } catch (Exception e) {
            System.err.println("[IQTestDialog] Lỗi render câu hỏi IQ: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void renderIQResult(IQTestDTO.IQResultNotify result) {
        if (result == null) return;

        if (!result.isFinished) {
            lblResult.setForeground(result.isCorrect ? new Color(22, 163, 74) : new Color(220, 38, 38));
            lblResult.setText(result.message);
        } else {
            lblTitle.setText("🎉 ĐÃ HOÀN THÀNH TEST IQ HÔM NAY (5/5 CÂU)");
            lblResult.setForeground(new Color(22, 163, 74));
            lblResult.setText("<html><b style='color:#16a34a;'>Đã kết toán điểm IQ:</b> Đúng " + result.correctCount + "/5, Sai "
                    + result.wrongCount + "/5 (Cộng +" + String.format("%.1f", result.deltaIq) + " IQ). Điểm mới: <b>"
                    + String.format("%.1f", result.currentIqScore) + "</b></html>");
            btnSubmit.setEnabled(false);
            btnSubmit.setText("Đã hoàn thành 5/5 câu");

            if (mainFrame != null && mainFrame.getLobbyPanel() != null) {
                mainFrame.getLobbyPanel().updateIQDisplay(result.currentIqScore);
            }

            JOptionPane.showMessageDialog(this,
                    result.message,
                    "Kết toán Điểm IQ hôm nay (5 câu)",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }
}