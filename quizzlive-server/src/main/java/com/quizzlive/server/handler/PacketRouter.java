package com.quizzlive.server.handler;

import com.quizzlive.server.dao.QuestionDAO;
import com.quizzlive.server.dao.StatisticDAO;
import com.quizzlive.server.network.ClientSession;
import com.quizzlive.server.network.SessionManager;
import com.quizzlive.server.service.*;
import com.quizzlive.shared.dto.*;
import com.quizzlive.shared.protocol.OpCode;
import com.quizzlive.shared.protocol.Packet;
import com.quizzlive.shared.util.JsonUtil;

import java.util.List;

/**
 * LỚP ĐIỀU PHỐI GÓI TIN MẠNG (PACKET ROUTER)
 * Nhận gói tin từ ClientSession -> Giải mã payload -> Gọi Service tương ứng
 */
public class PacketRouter {
    private final AuthService authService = new AuthService();
    private final IQTestService iqTestService = new IQTestService();
    private final TournamentService tournamentService = new TournamentService();
    private final StatisticDAO statisticDAO = new StatisticDAO();
    private final QuestionDAO questionDAO = new QuestionDAO();
    private final SessionManager sessionManager;

    public PacketRouter(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
        ChallengeService.getInstance().setSessionManager(sessionManager);
    }

    public void route(ClientSession session, Packet packet) {
        if (packet == null || packet.getOpCode() == null)
            return;
        OpCode op = packet.getOpCode();
        String json = packet.getPayloadJson();

        switch (op) {
            // --- 1. AUTHENTICATION & PROFILE (Yêu cầu 1) ---
            case REGISTER_REQ: {
                AuthDTO.RegisterRequest req = JsonUtil.fromJson(json, AuthDTO.RegisterRequest.class);
                AuthDTO.AuthResponse resp = authService.register(req);
                session.sendPacket(new Packet(OpCode.REGISTER_RESP, JsonUtil.toJson(resp)));
                break;
            }
            case VERIFY_OTP_REQ: {
                AuthDTO.VerifyOtpRequest req = JsonUtil.fromJson(json, AuthDTO.VerifyOtpRequest.class);
                AuthDTO.AuthResponse resp = authService.verifyOtp(req);
                session.sendPacket(new Packet(OpCode.VERIFY_OTP_RESP, JsonUtil.toJson(resp)));
                break;
            }
            case RESEND_OTP_REQ: {
                AuthDTO.VerifyOtpRequest req = JsonUtil.fromJson(json, AuthDTO.VerifyOtpRequest.class);
                AuthDTO.AuthResponse resp = authService.resendOtp(req != null ? req.email : null);
                session.sendPacket(new Packet(OpCode.RESEND_OTP_RESP, JsonUtil.toJson(resp)));
                break;
            }
            case LOGIN_REQ: {
                AuthDTO.LoginRequest req = JsonUtil.fromJson(json, AuthDTO.LoginRequest.class);
                AuthDTO.AuthResponse resp = authService.login(req, sessionManager);
                if (resp != null && resp.success) {
                    session.setUserId(resp.userId);
                    session.setUsername(resp.username);
                }
                session.sendPacket(new Packet(OpCode.LOGIN_RESP, JsonUtil.toJson(resp)));
                break;
            }
            case LOGOUT_REQ: {
                authService.logout(session.getUserId());
                session.setUserId(null);
                session.setUsername(null);
                session.sendPacket(new Packet(OpCode.LOGOUT_RESP, "{\"success\":true}"));
                break;
            }
            case UPDATE_PROFILE_REQ: {
                AuthDTO.UpdateProfileRequest req = JsonUtil.fromJson(json, AuthDTO.UpdateProfileRequest.class);
                AuthDTO.AuthResponse resp = authService.updateProfile(session.getUserId(), req);
                session.sendPacket(new Packet(OpCode.UPDATE_PROFILE_RESP, JsonUtil.toJson(resp)));
                break;
            }
            case ONLINE_USERS_REQ: {
                List<AuthDTO.AuthResponse> onlineUsers = authService.getOnlineUsers(session.getUserId());
                session.sendPacket(new Packet(OpCode.ONLINE_USERS_RESP, JsonUtil.toJson(onlineUsers)));
                break;
            }

            // --- 2. MATCHMAKING 60S (Yêu cầu 2, 4) ---
            case QUEUE_JOIN_REQ: {
                MatchmakingDTO.QueueRequest req = JsonUtil.fromJson(json, MatchmakingDTO.QueueRequest.class);
                MatchmakingService.getInstance().joinQueue(session, req.topicId, req.difficulty);
                break;
            }
            case QUEUE_LEAVE_REQ: {
                MatchmakingService.getInstance().leaveQueue(session.getUserId());
                session.sendPacket(new Packet(OpCode.QUEUE_LEAVE_RESP, "{\"success\":true}"));
                break;
            }

            // --- 3. THÁCH ĐẤU BẠN BÈ (Yêu cầu 3) ---
            case CHALLENGE_INVITE_REQ: {
                ChallengeDTO.InviteRequest req = JsonUtil.fromJson(json, ChallengeDTO.InviteRequest.class);
                ChallengeService.getInstance().sendChallenge(session, req, sessionManager);
                break;
            }
            case CHALLENGE_ACCEPT_REQ: {
                ChallengeDTO.RespondRequest req = JsonUtil.fromJson(json, ChallengeDTO.RespondRequest.class);
                req.accepted = true;
                ChallengeService.getInstance().handleResponse(session, req);
                break;
            }
            case CHALLENGE_REJECT_REQ: {
                ChallengeDTO.RespondRequest req = JsonUtil.fromJson(json, ChallengeDTO.RespondRequest.class);
                req.accepted = false;
                ChallengeService.getInstance().handleResponse(session, req);
                break;
            }

            // --- 4. TRẬN ĐẤU REALTIME 1V1 (Yêu cầu 5, 6, 7, 8) ---
            case BATTLE_SUBMIT_ANSWER_REQ: {
                BattleDTO.AnswerSubmitRequest req = JsonUtil.fromJson(json, BattleDTO.AnswerSubmitRequest.class);
                BattleManager.getInstance().handleAnswerSubmit(
                        session.getUserId(), req.matchId, req.selectedAnswerId, req.responseTimeSeconds);
                break;
            }
            case BATTLE_SURRENDER_REQ: {
                com.google.gson.JsonObject obj = JsonUtil.fromJson(json, com.google.gson.JsonObject.class);
                long matchId = (obj != null && obj.has("matchId")) ? obj.get("matchId").getAsLong() : 0L;
                BattleManager.getInstance().handleSurrender(session.getUserId(), matchId);
                break;
            }

            // --- 5. TEST IQ TRONG LÚC CHỜ (Yêu cầu 11) ---
            case IQ_TEST_START_REQ: {
                Integer topicId = null;
                String difficulty = null;
                try {
                    if (json != null && !json.trim().isEmpty() && !json.equals("{}")) {
                        IQTestDTO.IQStartRequest req = JsonUtil.fromJson(json, IQTestDTO.IQStartRequest.class);
                        if (req != null) {
                            topicId = req.topicId;
                            difficulty = req.difficulty;
                        }
                    }
                } catch (Exception ignored) {}
                iqTestService.sendRandomIQQuestion(session, topicId, difficulty);
                break;
            }
            case IQ_SUBMIT_ANSWER_REQ: {
                IQTestDTO.IQSubmitRequest req = JsonUtil.fromJson(json, IQTestDTO.IQSubmitRequest.class);
                iqTestService.handleIQSubmit(session, req);
                break;
            }

            // --- 6. GIẢI ĐẤU (Yêu cầu 9) ---
            case TOURNAMENT_LIST_REQ: {// lấy dsach giải đấu
                TournamentDTO.TournamentItem[] list = tournamentService.getActiveTournaments();
                session.sendPacket(new Packet(OpCode.TOURNAMENT_LIST_RESP, JsonUtil.toJson(list)));
                break;
            }
            case TOURNAMENT_JOIN_REQ: {// đk tham gia
                com.google.gson.JsonObject obj = JsonUtil.fromJson(json, com.google.gson.JsonObject.class);
                long tournamentId = (obj != null && obj.has("tournamentId")) ? obj.get("tournamentId").getAsLong() : 1L;
                boolean success = tournamentService.registerTournament(tournamentId, session.getUserId());
                String msg = success ? "Đăng ký tham gia giải đấu thành công!"
                        : "Bạn đã đăng ký giải đấu này rồi hoặc không thể đăng ký!";
                session.sendPacket(new Packet(OpCode.TOURNAMENT_JOIN_RESP,
                        "{\"success\":" + success + ",\"message\":\"" + msg + "\"}"));
                break;
            }
            case TOURNAMENT_STANDINGS_REQ: {// xem chi tiết giải
                com.google.gson.JsonObject obj = JsonUtil.fromJson(json, com.google.gson.JsonObject.class);
                long tournamentId = (obj != null && obj.has("tournamentId")) ? obj.get("tournamentId").getAsLong() : 1L;
                TournamentDTO.StandingItem[] standings = tournamentService.getTournamentStandings(tournamentId);
                TournamentDTO.TournamentDetailResponse detail = new TournamentDTO.TournamentDetailResponse();
                detail.standings = java.util.Arrays.asList(standings);
                session.sendPacket(new Packet(OpCode.TOURNAMENT_STANDINGS_RESP, JsonUtil.toJson(detail)));
                break;
            }
            // --- 7. BẢNG XẾP HẠNG & LỊCH SỬ (Yêu cầu 10) ---
            case LEADERBOARD_REQ: {
                Long currentUserId = session.getUserId();
                try {
                    if (json != null && json.contains("userId")) {
                        com.google.gson.JsonObject obj = JsonUtil.fromJson(json, com.google.gson.JsonObject.class);
                        if (obj != null && obj.has("userId") && !obj.get("userId").isJsonNull()) {
                            currentUserId = obj.get("userId").getAsLong();
                        }
                    }
                } catch (Exception ignored) {}
                LeaderboardDTO.LeaderboardResponse resp = statisticDAO.getLeaderboardResponse(currentUserId, 20);
                session.sendPacket(new Packet(OpCode.LEADERBOARD_RESP, JsonUtil.toJson(resp)));
                break;
            }
            case MATCH_HISTORY_REQ: {
                long targetUserId = session.getUserId() != null ? session.getUserId() : 0L;
                try {
                    if (json != null && json.contains("userId")) {
                        com.google.gson.JsonObject obj = JsonUtil.fromJson(json, com.google.gson.JsonObject.class);
                        if (obj != null && obj.has("userId")) {
                            targetUserId = obj.get("userId").getAsLong();
                        }
                    }
                } catch (Exception ignored) {
                }
                List<LeaderboardDTO.MatchHistoryItem> history = statisticDAO.getMatchHistory(targetUserId, 20);
                session.sendPacket(new Packet(OpCode.MATCH_HISTORY_RESP, JsonUtil.toJson(history)));
                break;
            }

            // --- 8. TOPIC / CHỦ ĐỀ & CẤU HÌNH (Yêu cầu 4) ---
            case TOPIC_LIST_REQ: {
                List<com.quizzlive.server.entity.Topic> topics = questionDAO.getAllTopics();
                java.util.List<TopicDTO> topicDTOs = new java.util.ArrayList<>();
                if (topics != null) {
                    for (com.quizzlive.server.entity.Topic t : topics) {
                        topicDTOs.add(new TopicDTO(t.getTopicId(), t.getTopicName(), t.getDescription()));
                    }
                }
                session.sendPacket(new Packet(OpCode.TOPIC_LIST_RESP, JsonUtil.toJson(topicDTOs)));
                break;
            }

            default:
                System.out.println("[PacketRouter] Chưa xử lý OpCode: " + op);
                break;
        }
    }
}
