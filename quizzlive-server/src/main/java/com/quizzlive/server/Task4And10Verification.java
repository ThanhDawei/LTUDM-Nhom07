package com.quizzlive.server;

import com.google.gson.reflect.TypeToken;
import com.quizzlive.server.dao.QuestionDAO;
import com.quizzlive.server.dao.StatisticDAO;
import com.quizzlive.server.dao.UserDAO;
import com.quizzlive.server.database.DatabaseManager;
import com.quizzlive.server.entity.Question;
import com.quizzlive.server.entity.Topic;
import com.quizzlive.server.entity.User;
import com.quizzlive.server.handler.PacketRouter;
import com.quizzlive.server.network.ClientSession;
import com.quizzlive.server.network.SessionManager;
import com.quizzlive.shared.dto.LeaderboardDTO;
import com.quizzlive.shared.dto.TopicDTO;
import com.quizzlive.shared.protocol.OpCode;
import com.quizzlive.shared.protocol.Packet;
import com.quizzlive.shared.util.JsonUtil;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp kiểm thử tự động toàn diện cho:
 * - TASK 4: Chọn chủ đề & độ khó (TOPIC_LIST_REQ / TOPIC_LIST_RESP, lọc câu hỏi)
 * - TASK 10: Bảng xếp hạng & Lịch sử thi đấu (LEADERBOARD_REQ / MATCH_HISTORY_REQ)
 * Cấu hình: KHÔNG DÙNG HÀM BĂM VÀ MÃ HÓA, truyền trực tiếp chuỗi JSON qua Socket TCP.
 */
public class Task4And10Verification {
    private static int totalTests = 0;
    private static int passedTests = 0;
    private static int failedTests = 0;

    static class MockSession extends ClientSession {
        private final List<Packet> capturedPackets = new ArrayList<>();

        public MockSession(long userId, String username, SessionManager sm) {
            super(null, sm, null);
            setUserId(userId);
            setUsername(username);
        }

        @Override
        public void sendPacket(Packet packet) {
            if (packet != null) {
                capturedPackets.add(packet);
            }
        }

        public Packet getLastPacket() {
            return capturedPackets.isEmpty() ? null : capturedPackets.get(capturedPackets.size() - 1);
        }

        public void clear() {
            capturedPackets.clear();
        }

        @Override
        public void close() {}
    }

    public static void main(String[] args) {
        System.out.println("===============================================================");
        System.out.println("   BẮT ĐẦU KIỂM THỬ TASK 4 & TASK 10 (CHỦ ĐỀ & BXH / LỊCH SỬ)   ");
        System.out.println("   (KHÔNG DÙNG HÀM BĂM VÀ MÃ HÓA - SOCKET TCP JSON THUẦN)      ");
        System.out.println("===============================================================");

        try {
            // 1. Khởi tạo CSDL Test độc lập trong RAM
            DatabaseManager.initializeForTesting();
            System.out.println("  ✓ CSDL Test độc lập trong RAM đã sẵn sàng.");

            SessionManager sessionManager = new SessionManager();
            PacketRouter packetRouter = new PacketRouter(sessionManager);
            QuestionDAO questionDAO = new QuestionDAO();
            StatisticDAO statisticDAO = new StatisticDAO();
            UserDAO userDAO = new UserDAO();

            // Đảm bảo có dữ liệu người chơi mẫu
            User u1 = userDAO.findByUsername("player_nam");
            long namId = (u1 != null) ? u1.getUserId() : 1L;

            User u2 = userDAO.findByUsername("player_hoa");
            long hoaId = (u2 != null) ? u2.getUserId() : 2L;

            MockSession session = new MockSession(namId, "player_nam", sessionManager);
            sessionManager.addSession(session);

            // =========================================================
            // PHẦN 1: KIỂM THỬ TASK 4 - CHỌN CHỦ ĐỀ VÀ ĐỘ KHÓ
            // =========================================================
            System.out.println("\n--- [TASK 4] KIỂM THỬ CHỦ ĐỀ & ĐỘ KHÓ ---");

            // [Test 1] Lấy danh sách chủ đề từ CSDL
            System.out.println("\n[Test 1] Truy vấn danh sách chủ đề từ bảng CHU_DE:");
            List<Topic> topics = questionDAO.getAllTopics();
            check(topics != null && !topics.isEmpty(), "CSDL trả về danh sách chủ đề hợp lệ (Số lượng = " + (topics != null ? topics.size() : 0) + ")");

            boolean hasScience = topics.stream().anyMatch(t -> t.getTopicName().contains("Khoa học"));
            boolean hasHistory = topics.stream().anyMatch(t -> t.getTopicName().contains("Lịch sử"));
            check(hasScience && hasHistory, "Chứa các chủ đề chuẩn: 'Khoa học Tự nhiên', 'Lịch sử & Địa lý'");

            // [Test 2] Giao thức mạng TOPIC_LIST_REQ -> TOPIC_LIST_RESP
            System.out.println("\n[Test 2] Kiểm thử gói tin mạng TOPIC_LIST_REQ qua PacketRouter:");
            session.clear();
            packetRouter.route(session, new Packet(OpCode.TOPIC_LIST_REQ, "{}"));

            Packet topicPkt = session.getLastPacket();
            check(topicPkt != null && topicPkt.getOpCode() == OpCode.TOPIC_LIST_RESP,
                    "Server phản hồi đúng gói tin TOPIC_LIST_RESP (OpCode = 802)");

            Type topicListType = new TypeToken<List<TopicDTO>>() {}.getType();
            List<TopicDTO> topicDTOList = JsonUtil.fromJson(topicPkt.getPayloadJson(), topicListType);
            check(topicDTOList != null && topicDTOList.size() == topics.size(),
                    "Payload JSON chứa đúng số lượng " + topicDTOList.size() + " TopicDTO, không bị mã hóa/băm");

            // [Test 3] Lọc câu hỏi theo chủ đề và độ khó
            System.out.println("\n[Test 3] Kiểm tra lọc ngân hàng câu hỏi theo Chủ đề và Độ khó:");
            List<Question> mediumScience = questionDAO.getRandomQuestions(1, "MEDIUM", 5);
            check(mediumScience != null && !mediumScience.isEmpty(),
                    "Lấy thành công câu hỏi Chủ đề #1 (Khoa học) với độ khó MEDIUM: " + mediumScience.size() + " câu");

            // =========================================================
            // PHẦN 2: KIỂM THỬ TASK 10 - BẢNG XẾP HẠNG & LỊCH SỬ THI ĐẤU
            // =========================================================
            System.out.println("\n--- [TASK 10] KIỂM THỬ BẢNG XẾP HẠNG & LỊCH SỬ THI ĐẤU ---");

            // [Test 4] Truy vấn dữ liệu Bảng xếp hạng (Top điểm, Top chuỗi thắng, Top số trận)
            System.out.println("\n[Test 4] Truy vấn dữ liệu Bảng xếp hạng từ CSDL:");
            LeaderboardDTO.LeaderboardResponse lbResp = statisticDAO.getLeaderboardResponse(10);
            check(lbResp != null && lbResp.topPlayers != null && !lbResp.topPlayers.isEmpty(),
                    "BXH Top Người chơi (Top điểm) trả về dữ liệu (Top 1: " + lbResp.topPlayers.get(0).username + " - " + lbResp.topPlayers.get(0).totalPoints + " điểm)");

            check(lbResp.topStreaks != null && !lbResp.topStreaks.isEmpty(),
                    "BXH Top Chuỗi thắng trả về dữ liệu (Top 1 chuỗi: " + lbResp.topStreaks.get(0).maxStreak + " trận)");

            check(lbResp.mostPlayed != null && !lbResp.mostPlayed.isEmpty(),
                    "BXH Tham gia nhiều nhất trả về dữ liệu (Top 1 số trận: " + lbResp.mostPlayed.get(0).totalMatches + " trận)");

            // [Test 5] Giao thức mạng LEADERBOARD_REQ -> LEADERBOARD_RESP
            System.out.println("\n[Test 5] Kiểm thử gói tin mạng LEADERBOARD_REQ qua PacketRouter:");
            session.clear();
            packetRouter.route(session, new Packet(OpCode.LEADERBOARD_REQ, "{}"));

            Packet lbPkt = session.getLastPacket();
            check(lbPkt != null && lbPkt.getOpCode() == OpCode.LEADERBOARD_RESP,
                    "Server phản hồi đúng gói tin LEADERBOARD_RESP (OpCode = 702)");

            LeaderboardDTO.LeaderboardResponse parsedLb = JsonUtil.fromJson(lbPkt.getPayloadJson(), LeaderboardDTO.LeaderboardResponse.class);
            check(parsedLb != null && parsedLb.topPlayers.size() == lbResp.topPlayers.size(),
                    "Payload JSON phản hồi BXH chính xác, không dùng mã hóa/băm");

            // [Test 6] Lưu trận đấu mới vào CSDL và kiểm tra Lịch sử trận
            System.out.println("\n[Test 6] Lưu kết quả trận đấu 1v1 vào TRAN_DAU và NGUOI_CHOI_TRAN:");
            long testMatchId = 98765L;
            statisticDAO.saveMatchRecord(
                    testMatchId,
                    "MATCH_98765",
                    namId,
                    hoaId,
                    850,
                    650,
                    namId, // Người thắng: Nam
                    1,
                    "HARD"
            );

            // Kiểm tra lịch sử đấu của Nam (Người thắng)
            List<LeaderboardDTO.MatchHistoryItem> namHistory = statisticDAO.getMatchHistory(namId, 10);
            check(namHistory != null && !namHistory.isEmpty(), "Lịch sử đấu của Player Nam chứa dữ liệu");

            LeaderboardDTO.MatchHistoryItem namLast = namHistory.get(0);
            check(namLast.matchId == testMatchId && "WIN".equals(namLast.result) && namLast.myScore == 850 && namLast.opponentScore == 650,
                    "Lịch sử Player Nam: Kết quả = WIN (850 - 650) trước " + namLast.opponentUsername);

            // Kiểm tra lịch sử đấu của Hoa (Người thua)
            List<LeaderboardDTO.MatchHistoryItem> hoaHistory = statisticDAO.getMatchHistory(hoaId, 10);
            check(hoaHistory != null && !hoaHistory.isEmpty(), "Lịch sử đấu của Player Hoa chứa dữ liệu");

            LeaderboardDTO.MatchHistoryItem hoaLast = hoaHistory.get(0);
            check(hoaLast.matchId == testMatchId && "LOSS".equals(hoaLast.result) && hoaLast.myScore == 650 && hoaLast.opponentScore == 850,
                    "Lịch sử Player Hoa: Kết quả = LOSS (650 - 850) trước " + hoaLast.opponentUsername);

            // [Test 7] Giao thức mạng MATCH_HISTORY_REQ -> MATCH_HISTORY_RESP
            System.out.println("\n[Test 7] Kiểm thử gói tin mạng MATCH_HISTORY_REQ qua PacketRouter:");
            session.clear();
            packetRouter.route(session, new Packet(OpCode.MATCH_HISTORY_REQ, "{\"userId\":" + namId + "}"));

            Packet histPkt = session.getLastPacket();
            check(histPkt != null && histPkt.getOpCode() == OpCode.MATCH_HISTORY_RESP,
                    "Server phản hồi đúng gói tin MATCH_HISTORY_RESP (OpCode = 704)");

            Type historyListType = new TypeToken<List<LeaderboardDTO.MatchHistoryItem>>() {}.getType();
            List<LeaderboardDTO.MatchHistoryItem> parsedHistory = JsonUtil.fromJson(histPkt.getPayloadJson(), historyListType);
            check(parsedHistory != null && !parsedHistory.isEmpty() && parsedHistory.get(0).matchId == testMatchId,
                    "Payload JSON phản hồi lịch sử trận đấu chính xác cho Client hiển thị lên LeaderboardPanel");

        } catch (Exception e) {
            e.printStackTrace();
            check(false, "Lỗi ngoại lệ trong quá trình kiểm thử: " + e.getMessage());
        }

        System.out.println("\n===============================================================");
        System.out.println("          TỔNG KẾT KẾT QUẢ KIỂM THỬ TASK 4 & TASK 10           ");
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

