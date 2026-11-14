package com.quizzlive.server.service;

import com.quizzlive.server.dao.ChallengeDAO;
import com.quizzlive.server.dao.UserDAO;
import com.quizzlive.server.entity.UserProfile;
import com.quizzlive.server.network.ClientSession;
import com.quizzlive.server.network.SessionManager;
import com.quizzlive.shared.dto.ChallengeDTO;
import com.quizzlive.shared.dto.MatchmakingDTO;
import com.quizzlive.shared.protocol.OpCode;
import com.quizzlive.shared.protocol.Packet;
import com.quizzlive.shared.util.JsonUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Service xử lý Thách đấu bạn bè 1v1 trực tiếp
 * Phụ trách Yêu cầu 3: Thách đấu bạn bè: mời trực tiếp một người đang online vào trận.
 * Cấu hình: KHÔNG DÙNG BĂM VÀ MÃ HÓA, giao tiếp bằng gói tin JSON thuần túy qua Socket TCP.
 */
public class ChallengeService {
    private static final ChallengeService instance = new ChallengeService();

    private final Map<Long, ChallengeInvitation> activeInvitations = new ConcurrentHashMap<>();
    private final AtomicLong invitationIdSeq = new AtomicLong(100);
    private final ChallengeDAO challengeDAO = new ChallengeDAO();
    private final UserDAO userDAO = new UserDAO();
    private SessionManager sessionManager;

    public static class ChallengeInvitation {
        public long invitationId;
        public ClientSession senderSession;
        public long senderUserId;
        public String senderUsername;
        public ClientSession targetSession;
        public long targetUserId;
        public String targetUsername;
        public Integer topicId;
        public String difficulty;
        public long createdAt;
        public String status; // PENDING, ACCEPTED, REJECTED, EXPIRED
    }

    private ChallengeService() {}

    public static ChallengeService getInstance() {
        return instance;
    }

    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public Map<Long, ChallengeInvitation> getActiveInvitations() {
        return activeInvitations;
    }

    /**
     * Gửi lời mời thách đấu từ người chơi A tới người chơi B đang online:
     * 1. Kiểm tra tính hợp lệ (không tự thách đấu, đối thủ phải đang online)
     * 2. Lưu lời mời vào CSDL (bảng LOI_MOI_THACH_DAU) và bộ nhớ RAM
     * 3. Gửi gói tin CHALLENGE_INVITE_NOTIFY cho đối thủ
     */
    public void sendChallenge(ClientSession sender, ChallengeDTO.InviteRequest req, SessionManager sessionManager) {
        if (sessionManager != null) {
            this.sessionManager = sessionManager;
        }

        if (sender == null || sender.getUserId() == null || req == null) {
            return;
        }

        // 1. Kiểm tra: Không được tự thách đấu chính mình
        if (sender.getUserId() == req.targetUserId) {
            sendResponse(sender, false, "Bạn không thể tự thách đấu chính mình!");
            return;
        }

        // 2. Kiểm tra nếu chính người gửi đang trong trận đấu
        if (BattleManager.getInstance().isUserInBattle(sender.getUserId())) {
            sendResponse(sender, false, "Bạn đang tham gia một trận đấu khác!");
            return;
        }

        // 3. Tìm ClientSession của đối thủ trong SessionManager
        ClientSession targetSession = (this.sessionManager != null)
                ? this.sessionManager.getSessionByUserId(req.targetUserId)
                : null;

        if (targetSession == null || targetSession.getUserId() == null) {
            sendResponse(sender, false, "Người chơi này hiện không online hoặc vừa thoát game!");
            return;
        }

        // 4. Kiểm tra đối thủ có đang trong trận đấu khác không
        if (BattleManager.getInstance().isUserInBattle(req.targetUserId)) {
            sendResponse(sender, false, "Người chơi này đang bận thi đấu một trận khác!");
            return;
        }

        // 5. Chuẩn hóa độ khó và chủ đề
        String difficulty = normalizeDifficulty(req.difficulty);
        Integer topicId = (req.topicId != null && req.topicId > 0) ? req.topicId : null;

        // 6. Kiểm tra xem đã có lời mời PENDING còn hạn giữa 2 người hay chưa
        for (ChallengeInvitation existing : activeInvitations.values()) {
            if ("PENDING".equals(existing.status) &&
                    existing.senderUserId == sender.getUserId() &&
                    existing.targetUserId == req.targetUserId &&
                    (System.currentTimeMillis() - existing.createdAt < 60000)) {
                sendResponse(sender, false, "Bạn đã gửi một lời mời cho người chơi này rồi. Vui lòng đợi họ phản hồi!");
                return;
            }
        }

        // 7. Lưu lời mời vào CSDL (LOI_MOI_THACH_DAU)
        long dbId = challengeDAO.saveInvitation(sender.getUserId(), req.targetUserId, "PENDING");
        long invitationId = (dbId > 0) ? dbId : invitationIdSeq.incrementAndGet();

        // 8. Lưu vào bộ nhớ RAM
        ChallengeInvitation invitation = new ChallengeInvitation();
        invitation.invitationId = invitationId;
        invitation.senderSession = sender;
        invitation.senderUserId = sender.getUserId();
        invitation.senderUsername = sender.getUsername();
        invitation.targetSession = targetSession;
        invitation.targetUserId = req.targetUserId;
        invitation.targetUsername = targetSession.getUsername();
        invitation.topicId = topicId;
        invitation.difficulty = difficulty;
        invitation.createdAt = System.currentTimeMillis();
        invitation.status = "PENDING";
        activeInvitations.put(invitationId, invitation);

        // 9. Lấy thông tin hồ sơ của người gửi để gửi cho đối thủ hiển thị
        UserProfile senderProfile = userDAO.getUserProfile(sender.getUserId());
        String senderFullName = (senderProfile != null && senderProfile.getFullName() != null)
                ? senderProfile.getFullName()
                : sender.getUsername();

        // 10. Gửi gói tin CHALLENGE_INVITE_NOTIFY cho đối thủ
        ChallengeDTO.InviteNotify notify = new ChallengeDTO.InviteNotify();
        notify.invitationId = invitationId;
        notify.fromUserId = sender.getUserId();
        notify.fromUsername = sender.getUsername();
        notify.fromFullName = senderFullName;
        notify.fromAvatar = (senderProfile != null && senderProfile.getAvatar() != null)
                ? senderProfile.getAvatar() : "avatar1.png";
        notify.fromIqScore = (senderProfile != null) ? senderProfile.getIqScore() : null;
        notify.topicId = topicId;
        notify.difficulty = difficulty;
        targetSession.sendPacket(new Packet(OpCode.CHALLENGE_INVITE_NOTIFY, JsonUtil.toJson(notify)));

        System.out.println("[ChallengeService] User " + sender.getUsername() +
                " đã gửi lời mời thách đấu tới " + targetSession.getUsername() + " (InvID=" + invitationId + ")");
    }

    /**
     * Tiếp nhận phản hồi từ người được thách đấu:
     * - Nếu accepted = true: Tạo phòng đấu 1v1 (BattleRoom), gửi MATCH_FOUND_NOTIFY cho cả 2 người
     * - Nếu accepted = false: Gửi thông báo từ chối về cho người thách đấu
     */
    public void handleResponse(ClientSession responder, ChallengeDTO.RespondRequest req) {
        if (responder == null || req == null) {
            return;
        }

        ChallengeInvitation invitation = activeInvitations.get(req.invitationId);
        if (invitation == null || !"PENDING".equals(invitation.status)) {
            sendResponse(responder, false, "Lời mời thách đấu không tồn tại hoặc đã hết hiệu lực!");
            return;
        }

        // 1. Kiểm tra thời hạn hiệu lực (60 giây)
        if (System.currentTimeMillis() - invitation.createdAt > 60000) {
            invitation.status = "EXPIRED";
            activeInvitations.remove(req.invitationId);
            challengeDAO.updateInvitationStatus(req.invitationId, "EXPIRED");
            sendResponse(responder, false, "Lời mời thách đấu đã hết hạn (quá 60 giây)!");
            return;
        }

        // 2. Trường hợp TỪ CHỐI (accepted = false)
        if (!req.accepted) {
            invitation.status = "REJECTED";
            activeInvitations.remove(req.invitationId);
            challengeDAO.updateInvitationStatus(req.invitationId, "REJECTED");

            sendResponse(responder, true, "Bạn đã từ chối lời mời thách đấu.");

            ClientSession senderSession = getSenderSession(invitation);
            if (senderSession != null) {
                sendResponse(senderSession, false, "Đối thủ [" + responder.getUsername() + "] đã từ chối lời mời thách đấu của bạn!");
            }
            System.out.println("[ChallengeService] User " + responder.getUsername() + " đã từ chối lời mời ID=" + req.invitationId);
            return;
        }

        // 3. Trường hợp ĐỒNG Ý (accepted = true)
        ClientSession senderSession = getSenderSession(invitation);
        if (senderSession == null || senderSession.getUserId() == null) {
            invitation.status = "EXPIRED";
            activeInvitations.remove(req.invitationId);
            challengeDAO.updateInvitationStatus(req.invitationId, "EXPIRED");
            sendResponse(responder, false, "Người thách đấu đã rời khỏi game hoặc mất kết nối!");
            return;
        }

        if (BattleManager.getInstance().isUserInBattle(invitation.senderUserId) ||
            BattleManager.getInstance().isUserInBattle(responder.getUserId())) {
            invitation.status = "EXPIRED";
            activeInvitations.remove(req.invitationId);
            challengeDAO.updateInvitationStatus(req.invitationId, "EXPIRED");
            sendResponse(responder, false, "Một trong hai người chơi đang bận trong một trận đấu khác!");
            sendResponse(senderSession, false, "Kèo thách đấu không thể bắt đầu do có người đang trong trận đấu khác!");
            return;
        }

        invitation.status = "ACCEPTED";
        activeInvitations.remove(req.invitationId);
        challengeDAO.updateInvitationStatus(req.invitationId, "ACCEPTED");

        // Ghi nhận quan hệ thách đấu / bạn bè vào CSDL bảng BAN_BE
        try {
            new com.quizzlive.server.dao.FriendDAO().addFriend(invitation.senderUserId, responder.getUserId());
        } catch (Exception ignored) {}

        System.out.println("[ChallengeService] Kèo thách đấu đã được chấp nhận: " +
                senderSession.getUsername() + " VS " + responder.getUsername());

        // 4. Khởi tạo phòng đấu 1v1 trong BattleManager
        BattleRoom room = BattleManager.getInstance().createBattle(
                senderSession, responder, invitation.topicId, invitation.difficulty
        );

        // 5. Gửi thông báo MATCH_FOUND_NOTIFY cho cả 2 người chơi
        UserProfile senderProfile = userDAO.getUserProfile(senderSession.getUserId());
        UserProfile responderProfile = userDAO.getUserProfile(responder.getUserId());

        com.quizzlive.server.dao.QuestionDAO qDAO = new com.quizzlive.server.dao.QuestionDAO();
        String topicName = qDAO.getTopicNameById(invitation.topicId);

        // Gửi cho người thách đấu (Sender)
        MatchmakingDTO.MatchFoundNotify notifySender = new MatchmakingDTO.MatchFoundNotify();
        notifySender.matchId = room.getMatchId();
        notifySender.matchCode = "CHALLENGE_" + room.getMatchId();
        notifySender.opponentId = responder.getUserId();
        notifySender.opponentUsername = responder.getUsername();
        notifySender.opponentFullName = (responderProfile != null && responderProfile.getFullName() != null)
                ? responderProfile.getFullName() : responder.getUsername();
        notifySender.opponentAvatar = (responderProfile != null) ? responderProfile.getAvatar() : null;
        notifySender.totalQuestions = BattleRoom.TOTAL_QUESTIONS;
        notifySender.timeLimitSeconds = BattleRoom.TIME_LIMIT_SECONDS;
        notifySender.topicId = invitation.topicId;
        notifySender.topicName = topicName;
        notifySender.difficulty = invitation.difficulty;
        senderSession.sendPacket(new Packet(OpCode.MATCH_FOUND_NOTIFY, JsonUtil.toJson(notifySender)));

        // Gửi cho người nhận thách đấu (Responder)
        MatchmakingDTO.MatchFoundNotify notifyResponder = new MatchmakingDTO.MatchFoundNotify();
        notifyResponder.matchId = room.getMatchId();
        notifyResponder.matchCode = "CHALLENGE_" + room.getMatchId();
        notifyResponder.opponentId = senderSession.getUserId();
        notifyResponder.opponentUsername = senderSession.getUsername();
        notifyResponder.opponentFullName = (senderProfile != null && senderProfile.getFullName() != null)
                ? senderProfile.getFullName() : senderSession.getUsername();
        notifyResponder.opponentAvatar = (senderProfile != null) ? senderProfile.getAvatar() : null;
        notifyResponder.totalQuestions = BattleRoom.TOTAL_QUESTIONS;
        notifyResponder.timeLimitSeconds = BattleRoom.TIME_LIMIT_SECONDS;
        notifyResponder.topicId = invitation.topicId;
        notifyResponder.topicName = topicName;
        notifyResponder.difficulty = invitation.difficulty;
        responder.sendPacket(new Packet(OpCode.MATCH_FOUND_NOTIFY, JsonUtil.toJson(notifyResponder)));
    }

    private ClientSession getSenderSession(ChallengeInvitation inv) {
        if (sessionManager != null) {
            ClientSession s = sessionManager.getSessionByUserId(inv.senderUserId);
            if (s != null) return s;
        }
        return inv.senderSession;
    }

    private void sendResponse(ClientSession session, boolean success, String message) {
        if (session == null) return;
        ChallengeDTO.ChallengeResponse resp = new ChallengeDTO.ChallengeResponse();
        resp.success = success;
        resp.message = message;
        session.sendPacket(new Packet(OpCode.CHALLENGE_RESP, JsonUtil.toJson(resp)));
    }

    private String normalizeDifficulty(String diff) {
        if (diff == null) return "MEDIUM";
        String upper = diff.trim().toUpperCase();
        if (upper.contains("DỄ") || upper.equals("EASY")) return "EASY";
        if (upper.contains("KHÓ") || upper.equals("HARD")) return "HARD";
        if (upper.contains("MỌI") || upper.equals("ALL")) return "ALL";
        return "MEDIUM";
    }
}
