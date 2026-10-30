package com.quizzlive.client.ui;

import com.quizzlive.client.network.SocketClient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Cửa sổ chính phía Client - Quản lý các màn hình giao diện qua CardLayout
 * Đáp ứng Yêu cầu kỹ thuật B.6: "Client bắt buộc có GUI"
 */
public class MainClientFrame extends JFrame {
    public static final String CARD_LOGIN = "LOGIN";
    public static final String CARD_REGISTER = "REGISTER";
    public static final String CARD_LOBBY = "LOBBY";
    public static final String CARD_WAITING = "WAITING";
    public static final String CARD_BATTLE = "BATTLE";
    public static final String CARD_LEADERBOARD = "LEADERBOARD";
    public static final String CARD_TOURNAMENT = "TOURNAMENT";

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel mainCardPanel = new JPanel(cardLayout);

    private LoginPanel loginPanel;
    private RegisterPanel registerPanel;
    private LobbyPanel lobbyPanel;
    private MatchmakingWaitingPanel waitingPanel;
    private BattlePanel battlePanel;
    private LeaderboardPanel leaderboardPanel;
    private TournamentPanel tournamentPanel;

    public MainClientFrame() {
        setTitle("QuizzLive - Tranh Tài Kiến Thức (Nhóm 07 - Đề tài 09)");
        setSize(1000, 700);
        setMinimumSize(new Dimension(850, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int confirm = JOptionPane.showConfirmDialog(
                        MainClientFrame.this,
                        "Bạn có chắc muốn thoát ứng dụng?",
                        "Xác nhận thoát",
                        JOptionPane.YES_NO_OPTION
                );
                if (confirm == JOptionPane.YES_OPTION) {
                    SocketClient.getInstance().disconnect();
                    System.exit(0);
                }
            }
        });

        initComponents();
    }

    private void initComponents() {
        loginPanel = new LoginPanel(this);
        registerPanel = new RegisterPanel(this);
        lobbyPanel = new LobbyPanel(this);
        waitingPanel = new MatchmakingWaitingPanel(this);
        battlePanel = new BattlePanel(this);
        leaderboardPanel = new LeaderboardPanel(this);
        tournamentPanel = new TournamentPanel(this);

        mainCardPanel.add(loginPanel, CARD_LOGIN);
        mainCardPanel.add(registerPanel, CARD_REGISTER);
        mainCardPanel.add(lobbyPanel, CARD_LOBBY);
        mainCardPanel.add(waitingPanel, CARD_WAITING);
        mainCardPanel.add(battlePanel, CARD_BATTLE);
        mainCardPanel.add(leaderboardPanel, CARD_LEADERBOARD);
        mainCardPanel.add(tournamentPanel, CARD_TOURNAMENT);

        add(mainCardPanel);

        // Màn hình ban đầu là Đăng nhập
        showPanel(CARD_LOGIN);
    }

    public void showPanel(String cardName) {
        cardLayout.show(mainCardPanel, cardName);
    }

    // Getters để ClientPacketHandler cập nhật UI
    public LoginPanel getLoginPanel() { return loginPanel; }
    public RegisterPanel getRegisterPanel() { return registerPanel; }
    public LobbyPanel getLobbyPanel() { return lobbyPanel; }
    public MatchmakingWaitingPanel getWaitingPanel() { return waitingPanel; }
    public BattlePanel getBattlePanel() { return battlePanel; }
    public LeaderboardPanel getLeaderboardPanel() { return leaderboardPanel; }
    public TournamentPanel getTournamentPanel() { return tournamentPanel; }
}
