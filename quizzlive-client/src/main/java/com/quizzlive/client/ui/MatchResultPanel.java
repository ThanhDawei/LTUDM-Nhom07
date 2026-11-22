package com.quizzlive.client.ui;

import com.quizzlive.client.util.AvatarUtil;
import com.quizzlive.client.util.BrutalUI;
import com.quizzlive.shared.dto.BattleDTO;

import javax.swing.*;
import java.awt.*;

/**
 * Giao diện Hiển thị Kết quả Trận đấu 1v1 - Chuẩn phong cách NEO-BRUTALIST
 * Thiết kế Card trắng viền đen 2px, bóng đổ cứng 4px, tương phản cao
 * Phụ trách: Yêu cầu 8 (Tổng kết kết quả trận đấu)
 */
public class MatchResultPanel extends JPanel {
    private final MainClientFrame mainFrame;

    // Header banner
    private JPanel bannerCard;
    private JLabel lblResultBadge;
    private JLabel lblResultTitle;
    private JLabel lblResultSubtitle;

    // Match info tags
    private JLabel lblMatchTag;
    private JLabel lblTopicTag;
    private JLabel lblDifficultyTag;

    // Player 1 (Bạn)
    private BrutalUI.CardPanel cardPlayer1;
    private JLabel lblP1Avatar;
    private JLabel lblP1RoleBadge;
    private JLabel lblP1Name;
    private JLabel lblP1ScoreValue;
    private JLabel lblP1CorrectCount;
    private JLabel lblP1OutcomeBadge;

    // Player 2 (Đối thủ)
    private BrutalUI.CardPanel cardPlayer2;
    private JLabel lblP2Avatar;
    private JLabel lblP2RoleBadge;
    private JLabel lblP2Name;
    private JLabel lblP2ScoreValue;
    private JLabel lblP2CorrectCount;
    private JLabel lblP2OutcomeBadge;

    // Buttons
    private BrutalUI.BrutalButton btnPlayAgain;
    private BrutalUI.BrutalButton btnLeaderboard;
    private BrutalUI.BrutalButton btnLobby;

    public MatchResultPanel(MainClientFrame mainFrame) {
        this.mainFrame = mainFrame;
        initComponents();
    }

    private void initComponents() {
        setLayout(new GridBagLayout());
        setBackground(BrutalUI.COLOR_BG);

        // Container chính giữa màn hình (rộng 880px)
        JPanel centerContainer = new JPanel();
        centerContainer.setLayout(new BoxLayout(centerContainer, BoxLayout.Y_AXIS));
        centerContainer.setOpaque(false);
        centerContainer.setPreferredSize(new Dimension(880, 600));

        // =========================================================================
        // 1. TOP HEADER: BANNER KẾT QUẢ CHUNG CUỘC
        // =========================================================================
        bannerCard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int shadow = 4;
                int arc = 16;
                int w = getWidth() - shadow - 2;
                int h = getHeight() - shadow - 2;

                // Bóng đen
                g2.setColor(BrutalUI.COLOR_BLACK);
                g2.fillRoundRect(shadow + 1, shadow + 1, w, h, arc, arc);

                // Nền
                g2.setColor(getBackground());
                g2.fillRoundRect(1, 1, w, h, arc, arc);

                // Viền đen 2px
                g2.setColor(BrutalUI.COLOR_BLACK);
                g2.setStroke(new BasicStroke(2.0f));
                g2.drawRoundRect(1, 1, w, h, arc, arc);

                g2.dispose();
            }

            @Override
            public Insets getInsets() {
                return new Insets(14, 20, 18, 20);
            }
        };
        bannerCard.setLayout(new BoxLayout(bannerCard, BoxLayout.Y_AXIS));
        bannerCard.setOpaque(false);
        bannerCard.setBackground(BrutalUI.COLOR_CORRECT_BG);
        bannerCard.setPreferredSize(new Dimension(880, 115));
        bannerCard.setMaximumSize(new Dimension(880, 125));
        bannerCard.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel badgeRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        badgeRow.setOpaque(false);
        lblResultBadge = BrutalUI.createBadge("KẾT QUẢ CHUNG CUỘC · YÊU CẦU 8", new Color(244, 244, 245), BrutalUI.COLOR_BLACK);
        badgeRow.add(lblResultBadge);

        lblResultTitle = new JLabel("CHIẾN THẮNG XUẤT SẮC!", SwingConstants.CENTER);
        lblResultTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblResultTitle.setForeground(BrutalUI.COLOR_CORRECT_TEXT);
        lblResultTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblResultSubtitle = new JLabel("", SwingConstants.CENTER);
        lblResultSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblResultSubtitle.setForeground(new Color(55, 65, 81));
        lblResultSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        bannerCard.add(badgeRow);
        bannerCard.add(Box.createVerticalStrut(6));
        bannerCard.add(lblResultTitle);
        bannerCard.add(Box.createVerticalStrut(4));
        bannerCard.add(lblResultSubtitle);

        centerContainer.add(bannerCard);
        centerContainer.add(Box.createVerticalStrut(14));

        // =========================================================================
        // 2. MATCH INFO TAGS BAR
        // =========================================================================
        JPanel infoTagsRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        infoTagsRow.setOpaque(false);
        infoTagsRow.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblMatchTag = BrutalUI.createBadge("Trận đấu: #101", BrutalUI.COLOR_LIGHT_GRAY, BrutalUI.COLOR_BLACK);
        lblTopicTag = BrutalUI.createBadge("Chủ đề: Đa Lĩnh Vực", BrutalUI.COLOR_LIGHT_GRAY, BrutalUI.COLOR_BLACK);
        lblDifficultyTag = BrutalUI.createBadge("Độ khó: Trung Bình", BrutalUI.COLOR_LIGHT_GRAY, BrutalUI.COLOR_BLACK);
        JLabel lblFormatTag = BrutalUI.createBadge("1v1 Realtime · 5 câu hỏi", BrutalUI.COLOR_LIGHT_GRAY, BrutalUI.COLOR_BLACK);

        infoTagsRow.add(lblMatchTag);
        infoTagsRow.add(lblTopicTag);
        infoTagsRow.add(lblDifficultyTag);
        infoTagsRow.add(lblFormatTag);

        centerContainer.add(infoTagsRow);
        centerContainer.add(Box.createVerticalStrut(14));

        // =========================================================================
        // 3. 2 THẺ ĐẤU THỦ CẠNH NHAU (BẠN VS ĐỐI THỦ)
        // =========================================================================
        JPanel cardsRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        cardsRow.setOpaque(false);
        cardsRow.setAlignmentX(Component.CENTER_ALIGNMENT);

        // THẺ 1: BẠN
        cardPlayer1 = createPlayerCard(true);
        cardsRow.add(cardPlayer1);

        // BADGE "VS" Ở GIỮA
        JPanel vsBox = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BrutalUI.COLOR_BLACK);
                g2.fillOval(2, 2, getWidth() - 4, getHeight() - 4);
                g2.dispose();
            }
        };
        vsBox.setPreferredSize(new Dimension(50, 50));
        vsBox.setMaximumSize(new Dimension(50, 50));
        vsBox.setOpaque(false);
        vsBox.setLayout(new GridBagLayout());
        JLabel lblVs = new JLabel("VS");
        lblVs.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblVs.setForeground(Color.WHITE);
        vsBox.add(lblVs);

        cardsRow.add(vsBox);

        // THẺ 2: ĐỐI THỦ
        cardPlayer2 = createPlayerCard(false);
        cardsRow.add(cardPlayer2);

        centerContainer.add(cardsRow);
        centerContainer.add(Box.createVerticalStrut(20));

        // =========================================================================
        // 4. FOOTER ACTION BUTTONS
        // =========================================================================
        JPanel footerRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 0));
        footerRow.setOpaque(false);
        footerRow.setAlignmentX(Component.CENTER_ALIGNMENT);

        btnPlayAgain = new BrutalUI.BrutalButton("Tiếp Tục Thi Đấu >>", BrutalUI.BrutalButton.Style.PRIMARY, 12);
        btnPlayAgain.setPreferredSize(new Dimension(210, 44));
        btnPlayAgain.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnPlayAgain.addActionListener(e -> {
            if (mainFrame != null) {
                mainFrame.showPanel(MainClientFrame.CARD_LOBBY);
                // Mở sẵn dialog tìm trận ghép cặp
                SwingUtilities.invokeLater(() -> {
                    if (mainFrame.getLobbyPanel() != null) {
                        mainFrame.getLobbyPanel().startMatchmakingAction();
                    }
                });
            }
        });

        btnLeaderboard = new BrutalUI.BrutalButton("Xem Bảng Xếp Hạng", BrutalUI.BrutalButton.Style.OUTLINE, 12);
        btnLeaderboard.setPreferredSize(new Dimension(190, 44));
        btnLeaderboard.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnLeaderboard.addActionListener(e -> {
            if (mainFrame != null) {
                Long uid = mainFrame.getCurrentUserId();
                if (mainFrame.getLeaderboardPanel() != null) {
                    mainFrame.getLeaderboardPanel().refreshData(uid);
                }
                mainFrame.showPanel(MainClientFrame.CARD_LEADERBOARD);
            }
        });

        btnLobby = new BrutalUI.BrutalButton("Quay Về Sảnh Chờ", BrutalUI.BrutalButton.Style.OUTLINE, 12);
        btnLobby.setPreferredSize(new Dimension(170, 44));
        btnLobby.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnLobby.addActionListener(e -> {
            if (mainFrame != null) {
                mainFrame.showPanel(MainClientFrame.CARD_LOBBY);
            }
        });

        footerRow.add(btnPlayAgain);
        footerRow.add(btnLeaderboard);
        footerRow.add(btnLobby);

        centerContainer.add(footerRow);

        add(centerContainer);
    }

    private BrutalUI.CardPanel createPlayerCard(boolean isMe) {
        BrutalUI.CardPanel card = new BrutalUI.CardPanel(16, 4);
        card.setPreferredSize(new Dimension(380, 320));
        card.setMaximumSize(new Dimension(390, 330));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

        // 1. Role Badge + Outcome Badge (Bọc trong FlowLayout để không bị giãn chiều cao)
        JPanel topBadgeRow = new JPanel(new BorderLayout());
        topBadgeRow.setOpaque(false);
        topBadgeRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JLabel roleBadge = BrutalUI.createBadge(
                isMe ? "BẠN" : "ĐỐI THỦ",
                isMe ? new Color(254, 243, 199) : new Color(239, 246, 255),
                isMe ? new Color(146, 64, 14) : new Color(29, 78, 216)
        );

        JLabel outcomeBadge = BrutalUI.createBadge(
                "THẮNG CUỘC",
                BrutalUI.COLOR_CORRECT_BG,
                BrutalUI.COLOR_CORRECT_TEXT
        );

        JPanel leftBadgeBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftBadgeBox.setOpaque(false);
        leftBadgeBox.add(roleBadge);

        JPanel rightBadgeBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightBadgeBox.setOpaque(false);
        rightBadgeBox.add(outcomeBadge);

        topBadgeRow.add(leftBadgeBox, BorderLayout.WEST);
        topBadgeRow.add(rightBadgeBox, BorderLayout.EAST);
        card.add(topBadgeRow);
        card.add(Box.createVerticalStrut(8));

        // 2. Avatar & Name (Căn giữa theo chiều dọc)
        JPanel profileBox = new JPanel();
        profileBox.setLayout(new BoxLayout(profileBox, BoxLayout.Y_AXIS));
        profileBox.setOpaque(false);
        profileBox.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel avatarLabel = new JLabel(AvatarUtil.getAvatarIcon("avatar1.png", 64));
        avatarLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel nameLabel = new JLabel(isMe ? "Bạn" : "Đối thủ", SwingConstants.CENTER);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        nameLabel.setForeground(BrutalUI.COLOR_BLACK);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        profileBox.add(avatarLabel);
        profileBox.add(Box.createVerticalStrut(6));
        profileBox.add(nameLabel);
        card.add(profileBox);
        card.add(Box.createVerticalStrut(10));

        // 3. Giant Score Box
        JPanel scorePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BrutalUI.COLOR_LIGHT_GRAY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(BrutalUI.COLOR_BLACK);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
            }
        };
        scorePanel.setOpaque(false);
        scorePanel.setLayout(new BoxLayout(scorePanel, BoxLayout.Y_AXIS));
        scorePanel.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        scorePanel.setMaximumSize(new Dimension(340, 72));
        scorePanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblScoreTag = new JLabel("TỔNG ĐIỂM SỐ", SwingConstants.CENTER);
        lblScoreTag.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblScoreTag.setForeground(BrutalUI.COLOR_MUTED);
        lblScoreTag.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel scoreVal = new JLabel("0 điểm", SwingConstants.CENTER);
        scoreVal.setFont(new Font("Segoe UI", Font.BOLD, 28));
        scoreVal.setForeground(BrutalUI.COLOR_BLACK);
        scoreVal.setAlignmentX(Component.CENTER_ALIGNMENT);

        scorePanel.add(lblScoreTag);
        scorePanel.add(Box.createVerticalStrut(2));
        scorePanel.add(scoreVal);
        card.add(scorePanel);
        card.add(Box.createVerticalStrut(10));

        // 4. Correct Count Row
        JPanel statsBox = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        statsBox.setOpaque(false);
        statsBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel lblCorrect = new JLabel("Số câu trả lời đúng: 0 / 5");
        lblCorrect.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblCorrect.setForeground(new Color(55, 65, 81));
        statsBox.add(lblCorrect);
        card.add(statsBox);

        if (isMe) {
            lblP1RoleBadge = roleBadge;
            lblP1OutcomeBadge = outcomeBadge;
            lblP1Avatar = avatarLabel;
            lblP1Name = nameLabel;
            lblP1ScoreValue = scoreVal;
            lblP1CorrectCount = lblCorrect;
        } else {
            lblP2RoleBadge = roleBadge;
            lblP2OutcomeBadge = outcomeBadge;
            lblP2Avatar = avatarLabel;
            lblP2Name = nameLabel;
            lblP2ScoreValue = scoreVal;
            lblP2CorrectCount = lblCorrect;
        }

        return card;
    }

    /**
     * Hiển thị kết quả chung cuộc của trận đấu 1v1
     */
    public void displayResult(BattleDTO.FinalResultNotify result,
                              long matchId,
                              String topicName,
                              String difficulty,
                              String oppName,
                              String oppAvatar,
                              int myCorrectCount,
                              int oppCorrectCount) {
        Long myUserId = (mainFrame != null) ? mainFrame.getCurrentUserId() : null;
        boolean isMePlayer1 = (myUserId != null && result.player1 != null && myUserId.equals(result.player1.userId));
        BattleDTO.PlayerRoundInfo me = isMePlayer1 ? result.player1 : result.player2;
        BattleDTO.PlayerRoundInfo opp = isMePlayer1 ? result.player2 : result.player1;

        int myScore = (me != null) ? me.totalScore : 0;
        int oppScore = (opp != null) ? opp.totalScore : 0;

        String myUsername = (me != null && me.username != null) ? me.username
                : ((mainFrame != null && mainFrame.getCurrentUsername() != null) ? mainFrame.getCurrentUsername() : "Bạn");
        String opponentUsername = (opp != null && opp.username != null) ? opp.username
                : ((oppName != null && !oppName.trim().isEmpty()) ? oppName : "Đối thủ");

        // 1. Cập nhật Tags thông tin trận
        lblMatchTag.setText("Trận đấu: #" + (matchId > 0 ? matchId : result.matchId));
        lblTopicTag.setText("Chủ đề: " + (topicName != null ? topicName : "Đa Lĩnh Vực"));
        lblDifficultyTag.setText("Độ khó: " + (difficulty != null ? difficulty : "Trung Bình"));

        // 2. Cập nhật Player 1 (Bạn)
        String myAvatar = "avatar1.png";
        if (mainFrame != null && mainFrame.getLobbyPanel() != null) {
            myAvatar = mainFrame.getLobbyPanel().getCurrentAvatar();
        }
        lblP1Avatar.setIcon(AvatarUtil.getAvatarIcon(myAvatar, 64));
        lblP1Name.setText(myUsername + " (Bạn)");
        lblP1ScoreValue.setText(myScore + " điểm");
        lblP1CorrectCount.setText("Số câu trả lời đúng: " + myCorrectCount + " / 5");

        // 3. Cập nhật Player 2 (Đối thủ)
        lblP2Avatar.setIcon(AvatarUtil.getAvatarIcon(oppAvatar != null ? oppAvatar : "avatar2.png", 64));
        lblP2Name.setText(opponentUsername);
        lblP2ScoreValue.setText(oppScore + " điểm");
        lblP2CorrectCount.setText("Số câu trả lời đúng: " + oppCorrectCount + " / 5");

        // 4. Đánh giá kết quả (Thắng / Thua / Hòa)
        if (result.isDraw) {
            // HÒA
            bannerCard.setBackground(BrutalUI.COLOR_SELECTED_BG);
            lblResultBadge.setText("KẾT QUẢ: BẤT PHÂN THẮNG BẠI");
            lblResultTitle.setText("TRẬN ĐẤU CÂN SỨC - KẾT QUẢ HÒA!");
            lblResultTitle.setForeground(BrutalUI.COLOR_SELECTED_TEXT);
            lblResultSubtitle.setText("<html><div style='text-align:center; color:#374151; font-size:12px; font-family:Segoe UI;'>"
                    + "Hai đấu thủ ngang tài ngang sức với cùng tổng điểm " + myScore + "!"
                    + "</div></html>");

            updateOutcomeBadge(lblP1OutcomeBadge, "HÒA", BrutalUI.COLOR_SELECTED_BG, BrutalUI.COLOR_SELECTED_TEXT);
            updateOutcomeBadge(lblP2OutcomeBadge, "HÒA", BrutalUI.COLOR_SELECTED_BG, BrutalUI.COLOR_SELECTED_TEXT);
            lblP1ScoreValue.setForeground(BrutalUI.COLOR_BLACK);
            lblP2ScoreValue.setForeground(BrutalUI.COLOR_BLACK);
        } else if (result.winnerUserId != null && myUserId != null && result.winnerUserId.equals(myUserId)) {
            // CHIẾN THẮNG
            bannerCard.setBackground(BrutalUI.COLOR_CORRECT_BG);
            lblResultBadge.setText("KẾT QUẢ: CHIẾN THẮNG");
            lblResultTitle.setText("CHIẾN THẮNG XUẤT SẮC!");
            lblResultTitle.setForeground(BrutalUI.COLOR_CORRECT_TEXT);
            lblResultSubtitle.setText("<html><div style='text-align:center; color:#374151; font-size:12px; font-family:Segoe UI;'>"
                    + "Chúc mừng bạn đã giành chiến thắng thuyết phục với " + myScore + " điểm!"
                    + "</div></html>");

            updateOutcomeBadge(lblP1OutcomeBadge, "THẮNG CUỘC", BrutalUI.COLOR_CORRECT_BG, BrutalUI.COLOR_CORRECT_TEXT);
            updateOutcomeBadge(lblP2OutcomeBadge, "Á QUÂN", BrutalUI.COLOR_LIGHT_GRAY, BrutalUI.COLOR_MUTED);
            lblP1ScoreValue.setForeground(BrutalUI.COLOR_CORRECT_TEXT);
            lblP2ScoreValue.setForeground(BrutalUI.COLOR_BLACK);
        } else {
            // THẤT BẠI
            bannerCard.setBackground(BrutalUI.COLOR_WRONG_BG);
            lblResultBadge.setText("KẾT QUẢ: THẤT BẠI");
            lblResultTitle.setText("THẤT BẠI TIẾC NUỐI!");
            lblResultTitle.setForeground(BrutalUI.COLOR_WRONG_TEXT);
            lblResultSubtitle.setText("<html><div style='text-align:center; color:#374151; font-size:12px; font-family:Segoe UI;'>"
                    + "Đối thủ đã giành chiến thắng với " + oppScore + " điểm. Hãy phục thù ở trận sau!"
                    + "</div></html>");

            updateOutcomeBadge(lblP1OutcomeBadge, "Á QUÂN", BrutalUI.COLOR_LIGHT_GRAY, BrutalUI.COLOR_MUTED);
            updateOutcomeBadge(lblP2OutcomeBadge, "THẮNG CUỘC", BrutalUI.COLOR_CORRECT_BG, BrutalUI.COLOR_CORRECT_TEXT);
            lblP1ScoreValue.setForeground(BrutalUI.COLOR_BLACK);
            lblP2ScoreValue.setForeground(BrutalUI.COLOR_CORRECT_TEXT);
        }

        revalidate();
        repaint();
    }

    /**
     * Trường hợp đối thủ thoát mạng hoặc đầu hàng
     */
    public void displayOpponentDisconnected(long matchId, String topic, String diff, String oppName, String oppAvatar, String message) {
        lblMatchTag.setText("Trận đấu: #" + (matchId > 0 ? matchId : "--"));
        lblTopicTag.setText("Chủ đề: " + (topic != null ? topic : "Đa Lĩnh Vực"));
        lblDifficultyTag.setText("Độ khó: " + (diff != null ? diff : "Trung Bình"));

        String myAvatar = "avatar1.png";
        if (mainFrame != null && mainFrame.getLobbyPanel() != null) {
            myAvatar = mainFrame.getLobbyPanel().getCurrentAvatar();
        }
        lblP1Avatar.setIcon(AvatarUtil.getAvatarIcon(myAvatar, 56));
        lblP1Name.setText((mainFrame != null && mainFrame.getCurrentUsername() != null ? mainFrame.getCurrentUsername() : "Bạn") + " (Bạn)");
        lblP1ScoreValue.setText("XỬ THẮNG");
        lblP1CorrectCount.setText("Đối thủ đã rời trận");

        lblP2Avatar.setIcon(AvatarUtil.getAvatarIcon(oppAvatar != null ? oppAvatar : "avatar2.png", 56));
        lblP2Name.setText(oppName != null ? oppName : "Đối thủ");
        lblP2ScoreValue.setText("MẤT KẾT NỐI");
        lblP2CorrectCount.setText("Rời khỏi phòng đấu");

        bannerCard.setBackground(BrutalUI.COLOR_CORRECT_BG);
        lblResultBadge.setText("ĐỐI THỦ RỜI TRẬN");
        lblResultTitle.setText("CHIẾN THẮNG KỸ THUẬT!");
        lblResultTitle.setForeground(BrutalUI.COLOR_CORRECT_TEXT);
        lblResultSubtitle.setText("<html><div style='text-align:center; color:#374151; font-size:12px; font-family:Segoe UI;'>"
                + (message != null ? message : "Đối thủ đã rời trận hoặc đầu hàng. Bạn được xử thắng!")
                + "</div></html>");

        updateOutcomeBadge(lblP1OutcomeBadge, "THẮNG CUỘC", BrutalUI.COLOR_CORRECT_BG, BrutalUI.COLOR_CORRECT_TEXT);
        updateOutcomeBadge(lblP2OutcomeBadge, "BỎ CUỘC", BrutalUI.COLOR_WRONG_BG, BrutalUI.COLOR_WRONG_TEXT);

        revalidate();
        repaint();
    }

    /**
     * Trường hợp người chơi tự đầu hàng
     */
    public void displaySurrenderLose(long matchId, String topic, String diff, String oppName, String oppAvatar) {
        lblMatchTag.setText("Trận đấu: #" + (matchId > 0 ? matchId : "--"));
        lblTopicTag.setText("Chủ đề: " + (topic != null ? topic : "Đa Lĩnh Vực"));
        lblDifficultyTag.setText("Độ khó: " + (diff != null ? diff : "Trung Bình"));

        String myAvatar = "avatar1.png";
        if (mainFrame != null && mainFrame.getLobbyPanel() != null) {
            myAvatar = mainFrame.getLobbyPanel().getCurrentAvatar();
        }
        lblP1Avatar.setIcon(AvatarUtil.getAvatarIcon(myAvatar, 64));
        lblP1Name.setText((mainFrame != null && mainFrame.getCurrentUsername() != null ? mainFrame.getCurrentUsername() : "Bạn") + " (Bạn)");
        lblP1ScoreValue.setText("ĐẦU HÀNG");
        lblP1CorrectCount.setText("Chủ động xin thua");

        lblP2Avatar.setIcon(AvatarUtil.getAvatarIcon(oppAvatar != null ? oppAvatar : "avatar2.png", 64));
        lblP2Name.setText(oppName != null ? oppName : "Đối thủ");
        lblP2ScoreValue.setText("CHIẾN THẮNG");
        lblP2CorrectCount.setText("Đối thủ thắng trận");

        bannerCard.setBackground(BrutalUI.COLOR_WRONG_BG);
        lblResultBadge.setText("ĐÃ ĐẦU HÀNG");
        lblResultTitle.setText("BẠN ĐÃ ĐẦU HÀNG!");
        lblResultTitle.setForeground(BrutalUI.COLOR_WRONG_TEXT);
        lblResultSubtitle.setText("<html><div style='text-align:center; color:#374151; font-size:12px; font-family:Segoe UI;'>"
                + "Bạn đã đầu hàng và bị xử thua trận đấu này. Hãy rút kinh nghiệm cho lần sau!"
                + "</div></html>");

        updateOutcomeBadge(lblP1OutcomeBadge, "ĐẦU HÀNG", BrutalUI.COLOR_WRONG_BG, BrutalUI.COLOR_WRONG_TEXT);
        updateOutcomeBadge(lblP2OutcomeBadge, "THẮNG CUỘC", BrutalUI.COLOR_CORRECT_BG, BrutalUI.COLOR_CORRECT_TEXT);

        revalidate();
        repaint();
    }

    private void updateOutcomeBadge(JLabel badge, String text, Color bg, Color fg) {
        badge.setText(text);
        badge.setBackground(bg);
        badge.setForeground(fg);
    }
}
