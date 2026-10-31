package com.quizzlive.client.handler;

import com.google.gson.reflect.TypeToken;
import com.quizzlive.client.network.NetworkListener;
import com.quizzlive.client.ui.MainClientFrame;
import com.quizzlive.shared.dto.*;
import com.quizzlive.shared.protocol.OpCode;
import com.quizzlive.shared.protocol.Packet;
import com.quizzlive.shared.util.JsonUtil;

import javax.swing.*;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Bộ tiếp nhận và điều hướng toàn bộ gói tin từ Server trả về phía Client.
 * Chuyển giao các sự kiện về giao diện qua Swing EDT (SwingUtilities.invokeLater).
 */
public class ClientPacketHandler implements NetworkListener {
    private final MainClientFrame mainFrame;

    public ClientPacketHandler(MainClientFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    @Override
    public void onConnected() {
        SwingUtilities.invokeLater(() -> {
            System.out.println("[ClientPacketHandler] Đã kết nối TCP thành công đến Server!");
        });
    }

    @Override
    public void onDisconnected(String reason) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(mainFrame, "Mất kết nối tới máy chủ: " + reason, "Mất kết nối", JOptionPane.ERROR_MESSAGE);
            mainFrame.showPanel(MainClientFrame.CARD_LOGIN);
        });
    }

    @Override
    public void onError(Throwable cause) {
        SwingUtilities.invokeLater(() -> {
            System.err.println("[ClientPacketHandler] Lỗi mạng: " + cause.getMessage());
        });
    }

    @Override
    public void onPacketReceived(Packet packet) {
        if (packet == null) return;
        OpCode opCode = packet.getOpCode();
        String json = packet.getPayloadJson();

        SwingUtilities.invokeLater(() -> {
            try {
                handlePacketInEDT(opCode, json);
            } catch (Exception e) {
                System.err.println("[ClientPacketHandler] Lỗi khi xử lý gói tin OpCode " + opCode + ": " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private void handlePacketInEDT(OpCode opCode, String json) {
        switch (opCode) {
            // --- 1. AUTHENTICATION & PROFILE (Yêu cầu 1) ---
            case LOGIN_RESP: {
                AuthDTO.AuthResponse resp = JsonUtil.fromJson(json, AuthDTO.AuthResponse.class);
                if (resp != null && resp.success) {
                    mainFrame.getLobbyPanel().updateUserInfo(resp);
                    mainFrame.getLoginPanel().onLoginResponse(true, resp.message);
                } else {
                    mainFrame.getLoginPanel().onLoginResponse(false, resp != null ? resp.message : "Đăng nhập thất bại");
                }
                break;
            }

            case REGISTER_RESP: {
                AuthDTO.AuthResponse resp = JsonUtil.fromJson(json, AuthDTO.AuthResponse.class);
                mainFrame.getRegisterPanel().onRegisterResponse(resp != null && resp.success, resp != null ? resp.message : "");
                break;
            }

            case VERIFY_OTP_RESP: {
                AuthDTO.AuthResponse resp = JsonUtil.fromJson(json, AuthDTO.AuthResponse.class);
                mainFrame.getRegisterPanel().onVerifyOtpResponse(resp != null && resp.success, resp != null ? resp.message : "");
                break;
            }

            case UPDATE_PROFILE_RESP: {
                AuthDTO.AuthResponse resp = JsonUtil.fromJson(json, AuthDTO.AuthResponse.class);
                if (resp != null) {
                    if (resp.success) {
                        mainFrame.getLobbyPanel().updateUserInfo(resp);
                        JOptionPane.showMessageDialog(mainFrame, resp.message, "Cập nhật hồ sơ", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(mainFrame, resp.message, "Lỗi cập nhật", JOptionPane.ERROR_MESSAGE);
                    }
                }
                break;
            }

            case LOGOUT_RESP: {
                mainFrame.showPanel(MainClientFrame.CARD_LOGIN);
                break;
            }

            case ONLINE_USERS_RESP: {
                Type listType = new TypeToken<List<AuthDTO.AuthResponse>>() {}.getType();
                List<AuthDTO.AuthResponse> users = JsonUtil.fromJson(json, listType);
                mainFrame.getLobbyPanel().updateOnlineUsersList(users);
                break;
            }

            // --- 2. MATCHMAKING (Yêu cầu 2) ---
            case MATCH_FOUND_NOTIFY: {
                mainFrame.getWaitingPanel().stopCountdown();
                JOptionPane.showMessageDialog(mainFrame, "Đã tìm thấy đối thủ! Trận đấu bắt đầu ngay bây giờ.", "Vào Trận!", JOptionPane.INFORMATION_MESSAGE);
                mainFrame.showPanel(MainClientFrame.CARD_BATTLE);
                break;
            }

            case QUEUE_TIMEOUT_NOTIFY: {
                mainFrame.getWaitingPanel().handleQueueTimeout();
                break;
            }

            // --- 3. CHALLENGE (Yêu cầu 3) ---
            case CHALLENGE_INVITE_NOTIFY: {
                ChallengeDTO.InviteNotify invite = JsonUtil.fromJson(json, ChallengeDTO.InviteNotify.class);
                if (invite != null) {
                    mainFrame.getLobbyPanel().showIncomingChallengeDialog(invite);
                }
                break;
            }

            case CHALLENGE_RESP: {
                ChallengeDTO.ChallengeResponse resp = JsonUtil.fromJson(json, ChallengeDTO.ChallengeResponse.class);
                if (resp != null) {
                    JOptionPane.showMessageDialog(mainFrame, resp.message, "Phản hồi thách đấu", JOptionPane.INFORMATION_MESSAGE);
                }
                break;
            }

            // --- 4. BATTLE REALTIME 1V1 (Yêu cầu 5, 6, 7, 8) ---
            case BATTLE_START_NOTIFY: {
                mainFrame.showPanel(MainClientFrame.CARD_BATTLE);
                break;
            }

            case BATTLE_QUESTION_NOTIFY: {
                BattleDTO.BattleQuestionNotify question = JsonUtil.fromJson(json, BattleDTO.BattleQuestionNotify.class);
                if (question != null) {
                    mainFrame.showPanel(MainClientFrame.CARD_BATTLE);
                    mainFrame.getBattlePanel().renderNewQuestion(question);
                }
                break;
            }

            case BATTLE_ROUND_RESULT_NOTIFY: {
                BattleDTO.RoundResultNotify roundResult = JsonUtil.fromJson(json, BattleDTO.RoundResultNotify.class);
                if (roundResult != null) {
                    mainFrame.getBattlePanel().renderRoundResult(roundResult);
                }
                break;
            }

            case BATTLE_FINAL_RESULT_NOTIFY: {
                BattleDTO.FinalResultNotify finalResult = JsonUtil.fromJson(json, BattleDTO.FinalResultNotify.class);
                if (finalResult != null) {
                    mainFrame.getBattlePanel().renderFinalResult(finalResult);
                }
                break;
            }

            // --- 5. TEST IQ TRONG LÚC CHỜ (Yêu cầu 11) ---
            case IQ_QUESTION_NOTIFY: {
                IQTestDTO.IQQuestionNotify iqQ = JsonUtil.fromJson(json, IQTestDTO.IQQuestionNotify.class);
                if (iqQ != null) {
                    mainFrame.getWaitingPanel().renderIQQuestion(iqQ);
                }
                break;
            }

            case IQ_RESULT_NOTIFY: {
                IQTestDTO.IQResultNotify iqRes = JsonUtil.fromJson(json, IQTestDTO.IQResultNotify.class);
                if (iqRes != null) {
                    mainFrame.getWaitingPanel().renderIQResult(iqRes);
                }
                break;
            }

            // --- 6. GIẢI ĐẤU (Yêu cầu 9) ---
            case TOURNAMENT_LIST_RESP: {
                Type listType = new TypeToken<List<TournamentDTO.TournamentItem>>() {}.getType();
                List<TournamentDTO.TournamentItem> list = JsonUtil.fromJson(json, listType);
                mainFrame.getTournamentPanel().renderTournamentList(list);
                break;
            }

            case TOURNAMENT_STANDINGS_RESP: {
                TournamentDTO.TournamentDetailResponse detail = JsonUtil.fromJson(json, TournamentDTO.TournamentDetailResponse.class);
                if (detail != null && detail.standings != null) {
                    mainFrame.getTournamentPanel().renderStandings(detail.standings);
                }
                break;
            }

            // --- 7. BẢNG XẾP HẠNG & LỊCH SỬ (Yêu cầu 10) ---
            case LEADERBOARD_RESP: {
                LeaderboardDTO.LeaderboardResponse lb = JsonUtil.fromJson(json, LeaderboardDTO.LeaderboardResponse.class);
                if (lb != null) {
                    mainFrame.getLeaderboardPanel().renderLeaderboard(lb);
                }
                break;
            }

            case MATCH_HISTORY_RESP: {
                Type listType = new TypeToken<List<LeaderboardDTO.MatchHistoryItem>>() {}.getType();
                List<LeaderboardDTO.MatchHistoryItem> history = JsonUtil.fromJson(json, listType);
                mainFrame.getLeaderboardPanel().renderMatchHistory(history);
                break;
            }

            // --- 8. HỆ THỐNG / LỖI ---
            case SYSTEM_NOTIFICATION_NOTIFY:
            case ERROR_RESP: {
                JOptionPane.showMessageDialog(mainFrame, json, "Thông báo từ Server", JOptionPane.WARNING_MESSAGE);
                break;
            }

            default:
                System.out.println("[ClientPacketHandler] Chưa có handler cho OpCode: " + opCode);
                break;
        }
    }
}
