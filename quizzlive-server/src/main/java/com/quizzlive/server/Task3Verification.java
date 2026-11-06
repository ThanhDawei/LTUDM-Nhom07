package com.quizzlive.server;

import com.quizzlive.server.dao.ChallengeDAO;
import com.quizzlive.server.dao.FriendDAO;
import com.quizzlive.server.dao.UserDAO;
import com.quizzlive.server.database.DatabaseManager;
import com.quizzlive.server.entity.User;
import com.quizzlive.server.network.ClientSession;
import com.quizzlive.server.network.SessionManager;
import com.quizzlive.server.service.BattleManager;
import com.quizzlive.server.service.ChallengeService;
import com.quizzlive.shared.dto.ChallengeDTO;
import com.quizzlive.shared.dto.MatchmakingDTO;
import com.quizzlive.shared.protocol.OpCode;
import com.quizzlive.shared.protocol.Packet;
import com.quizzlive.shared.util.JsonUtil;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp kiểm thử tự động toàn diện cho TASK 3:
 * "ChallengeService gửi và nhận lời mời 1v1 trên LobbyPanel không dùng hàm băm và mã hóa"
 */
public class Task3Verification {
    private static int totalTests = 0;
    private static int passedTests = 0;
    private static int failedTests = 0;

    /**
     * Mock ClientSession thu thập các gói tin gửi qua Socket để kiểm tra kết quả
     */
    static class MockClientSession extends ClientSession {
        private final List<Packet> sentPackets = new ArrayList<>();

        public MockClientSession(long userId, String username, SessionManager sessionManager) {
            super(null, sessionManager, null);
            setUserId(userId);
            setUsername(username);
        }

        @Override
        public void sendPacket(Packet packet) {
            if (packet != null) {
                sentPackets.add(packet);
            }
        }

        public List<Packet> getSentPackets() {
            return sentPackets;
        }

        public Packet getLastPacket() {
            return sentPackets.isEmpty() ? null : sentPackets.get(sentPackets.size() - 1);
        }

        public void clearPackets() {
            sentPackets.clear();
        }

        @Override
        public void close() {}
    }

    public static void main(String[] args) {
        System.out.println("===============================================================");
        System.out.println("   BẮT ĐẦU KIỂM THỬ TASK 3: THÁCH ĐẤU BẠN BÈ 1V1 TRỰC TIẾP     ");
        System.out.println("   (KHÔNG DÙNG HÀM BĂM VÀ MÃ HÓA - SOCKET TCP JSON THUẦN)      ");
        System.out.println("===============================================================");

        try {
            // 1. Khởi tạo CSDL Test độc lập trong RAM
            DatabaseManager.initializeForTesting();
            System.out.println("  ✓ CSDL Test độc lập trong RAM đã sẵn sàng.");

            SessionManager sessionManager = new SessionManager();
            ChallengeService challengeService = ChallengeService.getInstance();
            challengeService.setSessionManager(sessionManager);
            ChallengeDAO challengeDAO = new ChallengeDAO();
            FriendDAO friendDAO = new FriendDAO();
            UserDAO userDAO = new UserDAO();

            // Đảm bảo 2 user tồn tại trong CSDL
            User u1 = userDAO.findByUsername("player_alpha");
            long user1Id;
            if (u1 == null) {
                user1Id = userDAO.createUser("player_alpha", "123456", "alpha@test.com", "Người Chơi Alpha");
                userDAO.activateUser(user1Id);
            } else {
                user1Id = u1.getUserId();
            }

            User u2 = userDAO.findByUsername("player_beta");
            long user2Id;
            if (u2 == null) {
                user2Id = userDAO.createUser("player_beta", "123456", "beta@test.com", "Người Chơi Beta");
                userDAO.activateUser(user2Id);
            } else {
                user2Id = u2.getUserId();
            }

            MockClientSession session1 = new MockClientSession(user1Id, "player_alpha", sessionManager);
            MockClientSession session2 = new MockClientSession(user2Id, "player_beta", sessionManager);

            // --- TEST 1: Ngăn chặn tự thách đấu chính mình ---
            System.out.println("\n[Test 1] Kiểm tra chặn tự thách đấu chính mình:");
            ChallengeDTO.InviteRequest selfReq = new ChallengeDTO.InviteRequest();
            selfReq.targetUserId = user1Id;
            selfReq.topicId = 1;
            selfReq.difficulty = "MEDIUM";

            session1.clearPackets();
            challengeService.sendChallenge(session1, selfReq, sessionManager);

            Packet respPkt = session1.getLastPacket();
            if (respPkt != null && respPkt.getOpCode() == OpCode.CHALLENGE_RESP) {
                ChallengeDTO.ChallengeResponse resp = JsonUtil.fromJson(respPkt.getPayloadJson(), ChallengeDTO.ChallengeResponse.class);
                check(!resp.success && resp.message.contains("không thể tự thách đấu"),
                        "Chặn thành công tự thách đấu: " + resp.message);
            } else {
                check(false, "Không nhận được phản hồi CHALLENGE_RESP khi tự thách đấu!");
            }

            // --- TEST 2: Ngăn chặn thách đấu người chơi offline ---
            System.out.println("\n[Test 2] Kiểm tra chặn thách đấu người chơi offline:");
            ChallengeDTO.InviteRequest offlineReq = new ChallengeDTO.InviteRequest();
            offlineReq.targetUserId = 99999L; // User không online
            offlineReq.topicId = 1;
            offlineReq.difficulty = "EASY";

            session1.clearPackets();
            challengeService.sendChallenge(session1, offlineReq, sessionManager);

            respPkt = session1.getLastPacket();
            if (respPkt != null && respPkt.getOpCode() == OpCode.CHALLENGE_RESP) {
                ChallengeDTO.ChallengeResponse resp = JsonUtil.fromJson(respPkt.getPayloadJson(), ChallengeDTO.ChallengeResponse.class);
                check(!resp.success && resp.message.contains("không online"),
                        "Chặn thành công thách đấu người offline: " + resp.message);
            } else {
                check(false, "Không nhận được phản hồi CHALLENGE_RESP khi đối thủ offline!");
            }

            // --- TEST 3: Gửi lời mời thách đấu hợp lệ tới người online ---
            System.out.println("\n[Test 3] Gửi lời mời thách đấu hợp lệ tới đối thủ online:");
            sessionManager.addSession(session1);
            sessionManager.addSession(session2);

            ChallengeDTO.InviteRequest validReq = new ChallengeDTO.InviteRequest();
            validReq.targetUserId = user2Id;
            validReq.topicId = 2; // Lịch sử & Địa lý
            validReq.difficulty = "Khó"; // Tiếng Việt -> map sang HARD

            session1.clearPackets();
            session2.clearPackets();
            challengeService.sendChallenge(session1, validReq, sessionManager);

            Packet notifyPkt = session2.getLastPacket();
            check(notifyPkt != null && notifyPkt.getOpCode() == OpCode.CHALLENGE_INVITE_NOTIFY,
                    "Đối thủ (Player 2) nhận được gói tin CHALLENGE_INVITE_NOTIFY");

            ChallengeDTO.InviteNotify notify = JsonUtil.fromJson(notifyPkt.getPayloadJson(), ChallengeDTO.InviteNotify.class);
            check(notify.fromUserId == user1Id && "HARD".equalsIgnoreCase(notify.difficulty) && notify.invitationId > 0,
                    "Gói tin notify chứa đúng fromUserId=" + notify.fromUserId + ", difficulty=" + notify.difficulty + ", invId=" + notify.invitationId);

            // Kiểm tra trạng thái trong CSDL
            String dbStatus = challengeDAO.getInvitationStatus(notify.invitationId);
            check("PENDING".equalsIgnoreCase(dbStatus), "CSDL lưu bản ghi LOI_MOI_THACH_DAU trạng thái PENDING");

            // --- TEST 4: Người chơi từ chối lời mời thách đấu ---
            System.out.println("\n[Test 4] Đối thủ từ chối lời mời thách đấu:");
            ChallengeDTO.RespondRequest rejectReq = new ChallengeDTO.RespondRequest();
            rejectReq.invitationId = notify.invitationId;
            rejectReq.accepted = false;

            session1.clearPackets();
            session2.clearPackets();
            challengeService.handleResponse(session2, rejectReq);

            // Kiểm tra phản hồi cho người nhận (Player 2)
            Packet p2RespPkt = session2.getLastPacket();
            check(p2RespPkt != null && p2RespPkt.getOpCode() == OpCode.CHALLENGE_RESP,
                    "Player 2 nhận thông báo đã từ chối");

            // Kiểm tra thông báo cho người gửi (Player 1)
            Packet p1RespPkt = session1.getLastPacket();
            check(p1RespPkt != null && p1RespPkt.getOpCode() == OpCode.CHALLENGE_RESP,
                    "Player 1 nhận thông báo đối thủ đã từ chối lời mời");

            String updatedStatus = challengeDAO.getInvitationStatus(notify.invitationId);
            check("REJECTED".equalsIgnoreCase(updatedStatus),
                    "CSDL cập nhật LOI_MOI_THACH_DAU sang trạng thái REJECTED");

            // --- TEST 5: Người chơi chấp nhận lời mời thách đấu -> Vào trận 1v1 ---
            System.out.println("\n[Test 5] Đối thủ chấp nhận lời mời -> Khởi tạo trận đấu 1v1:");
            ChallengeDTO.InviteRequest invite2 = new ChallengeDTO.InviteRequest();
            invite2.targetUserId = user2Id;
            invite2.topicId = 1;
            invite2.difficulty = "Trung bình";

            session1.clearPackets();
            session2.clearPackets();
            challengeService.sendChallenge(session1, invite2, sessionManager);

            Packet notifyPkt2 = session2.getLastPacket();
            ChallengeDTO.InviteNotify notify2 = JsonUtil.fromJson(notifyPkt2.getPayloadJson(), ChallengeDTO.InviteNotify.class);

            ChallengeDTO.RespondRequest acceptReq = new ChallengeDTO.RespondRequest();
            acceptReq.invitationId = notify2.invitationId;
            acceptReq.accepted = true;

            session1.clearPackets();
            session2.clearPackets();
            challengeService.handleResponse(session2, acceptReq);

            // Kiểm tra Player 1 nhận MATCH_FOUND_NOTIFY
            boolean p1GotMatch = session1.getSentPackets().stream()
                    .anyMatch(p -> p.getOpCode() == OpCode.MATCH_FOUND_NOTIFY);
            check(p1GotMatch, "Player 1 (Người gửi) nhận được MATCH_FOUND_NOTIFY");

            // Kiểm tra Player 2 nhận MATCH_FOUND_NOTIFY
            boolean p2GotMatch = session2.getSentPackets().stream()
                    .anyMatch(p -> p.getOpCode() == OpCode.MATCH_FOUND_NOTIFY);
            check(p2GotMatch, "Player 2 (Người nhận) nhận được MATCH_FOUND_NOTIFY");

            String acceptedStatus = challengeDAO.getInvitationStatus(notify2.invitationId);
            check("ACCEPTED".equalsIgnoreCase(acceptedStatus),
                    "CSDL cập nhật LOI_MOI_THACH_DAU sang trạng thái ACCEPTED");

            // --- TEST 6: Ngăn chặn xử lý lại lời mời đã hoàn tất ---
            System.out.println("\n[Test 6] Kiểm tra phản hồi lời mời đã hết hạn / hoàn tất:");
            session2.clearPackets();
            challengeService.handleResponse(session2, acceptReq);
            Packet dupResp = session2.getLastPacket();
            check(dupResp != null && dupResp.getOpCode() == OpCode.CHALLENGE_RESP,
                    "Báo lỗi khi gửi phản hồi cho lời mời đã kết thúc");

            // --- TEST 7: Kiểm tra nghiệp vụ bảng BAN_BE (FriendDAO) ---
            System.out.println("\n[Test 7] Kiểm tra chức năng bảng BAN_BE (Friend):");
            boolean addFriendSuccess = friendDAO.addFriend(user1Id, user2Id);
            check(addFriendSuccess, "Thêm quan hệ bạn bè giữa User 1 và User 2 thành công");

            boolean isFriend = friendDAO.isFriend(user1Id, user2Id);
            check(isFriend, "FriendDAO.isFriend(user1, user2) trả về TRUE");

            List<Long> friends = friendDAO.getFriendIds(user1Id);
            check(friends.contains(user2Id), "FriendDAO.getFriendIds(user1) chứa ID của User 2");

        } catch (Exception e) {
            e.printStackTrace();
            check(false, "Lỗi ngoại lệ trong quá trình kiểm thử: " + e.getMessage());
        }

        System.out.println("\n===============================================================");
        System.out.println("             TỔNG KẾT KẾT QUẢ KIỂM THỬ TASK 3                  ");
        System.out.println("===============================================================");
        System.out.println("  TỔNG SỐ TEST: " + totalTests);
        System.out.println("  THÀNH CÔNG:   " + passedTests);
        System.out.println("  THẤT BẠI:     " + failedTests);
        System.out.println("===============================================================");

        System.exit(failedTests == 0 ? 0 : 1);
    }

    private static void check(boolean condition, String description) {
        totalTests++;
        if (condition) {
            passedTests++;
            System.out.println("  ✓ PASS: " + description);
        } else {
            failedTests++;
            System.err.println("  ✗ FAIL: " + description);
        }
    }
}
