package com.quizzlive.server.handler;

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
    private final SessionManager sessionManager;

    public PacketRouter(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public void route(ClientSession session, Packet packet) {
        if (packet == null || packet.getOpCode() == null) return;
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
            case LOGIN_REQ: {
                AuthDTO.LoginRequest req = JsonUtil.fromJson(json, AuthDTO.LoginRequest.class);
                AuthDTO.AuthResponse resp = authService.login(req);
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
                        session.getUserId(), req.matchId, req.selectedAnswerId, req.responseTimeSeconds
                );
                break;
            }

            // --- 5. TEST IQ TRONG LÚC CHỜ (Yêu cầu 11) ---
            case IQ_TEST_START_REQ: {
                iqTestService.sendRandomIQQuestion(session);
                break;
            }
            case IQ_SUBMIT_ANSWER_REQ: {
                IQTestDTO.IQSubmitRequest req = JsonUtil.fromJson(json, IQTestDTO.IQSubmitRequest.class);
                iqTestService.handleIQSubmit(session, req);
                break;
            }

            // --- 6. GIẢI ĐẤU (Yêu cầu 9) ---
            case TOURNAMENT_LIST_REQ: {
                TournamentDTO.TournamentItem[] list = tournamentService.getActiveTournaments();
                session.sendPacket(new Packet(OpCode.TOURNAMENT_LIST_RESP, JsonUtil.toJson(list)));
                break;
            }

            default:
                System.out.println("[PacketRouter] Chưa xử lý OpCode: " + op);
                break;
        }
    }
}
