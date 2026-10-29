package com.quizzlive.server.service;

import com.quizzlive.server.dao.UserDAO;
import com.quizzlive.server.entity.UserProfile;
import com.quizzlive.server.network.ClientSession;
import com.quizzlive.shared.dto.MatchmakingDTO;
import com.quizzlive.shared.protocol.OpCode;
import com.quizzlive.shared.protocol.Packet;
import com.quizzlive.shared.util.JsonUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Service xử lý Hàng đợi Ghép cặp tự động
 * Phụ trách của Người 2: Yêu cầu 2 (Ghép cặp tự động trong 60 giây, timeout về
 * sảnh chờ)
 */
public class MatchmakingService {
    private static final MatchmakingService instance = new MatchmakingService();

    private final Queue<QueueTicket> waitingQueue = new ConcurrentLinkedQueue<>();// Hàng đợi thread-safe chứa danh sách
                                                                                  // người đang chờ tìm trận

    // Scheduler chạy định kỳ mỗi 1 giây để quét hàng đợi
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private final UserDAO userDAO = new UserDAO();

    public static final int TIMEOUT_SECONDS = 60;// giới hạn thời gian

    private MatchmakingService() {
        scheduler.scheduleAtFixedRate(this::processQueue, 1, 1, TimeUnit.SECONDS);// quét cặp thi đấu quét 1s 1 lần
    }

    public static MatchmakingService getInstance() {
        return instance;
    }

    // Vé biểu diễn một người chơi đang xếp hàng chờ ghép cặp
    public static class QueueTicket {
        private final ClientSession session;
        private final long userId;
        private final String username;
        private final Integer topicId;
        private final String difficulty;
        private final long joinTimeMillis;

        public QueueTicket(ClientSession session, Integer topicId, String difficulty) {
            this.session = session;
            this.userId = session.getUserId() != null ? session.getUserId() : 0L;
            this.username = session.getUsername() != null ? session.getUsername() : "Player";
            this.topicId = topicId;
            this.difficulty = difficulty != null ? difficulty.toUpperCase() : "MEDIUM";
            this.joinTimeMillis = System.currentTimeMillis();
        }

        public ClientSession getSession() {
            return session;
        }

        public long getUserId() {
            return userId;
        }

        public String getUsername() {
            return username;
        }

        public Integer getTopicId() {
            return topicId;
        }

        public String getDifficulty() {
            return difficulty;
        }

        public long getJoinTimeMillis() {
            return joinTimeMillis;
        }
    }

    // Thêm người chơi vào hàng đợi tìm trận
    public synchronized void joinQueue(ClientSession session, Integer topicId, String difficulty) {
        if (session == null || session.getUserId() == null) {
            return;
        }

        // Kiểm tra xem người chơi này đã có trong hàng đợi chưa
        for (QueueTicket ticket : waitingQueue) {
            if (ticket.getUserId() == session.getUserId()) {
                System.out.println("[Matchmaking] User " + session.getUsername() + " đã có trong hàng đợi.");// (tránh
                                                                                                             // spam tìm
                                                                                                             // trận)
                return;
            }
        }

        QueueTicket ticket = new QueueTicket(session, topicId, difficulty);
        waitingQueue.add(ticket);
        System.out.println("[Matchmaking] User " + session.getUsername() + " vào hàng đợi (Topic: "
                + topicId + ", Độ khó: " + difficulty + "). Tổng chờ: " + waitingQueue.size());

        // Phản hồi về client báo đã vào hàng đợi thành công
        session.sendPacket(
                new Packet(OpCode.QUEUE_JOIN_RESP, "{\"success\":true,\"message\":\"Đang tìm đối thủ...\"}"));

        // Kích hoạt quét ngay để nếu có người chờ sẵn thì ghép luôn không cần đợi hết 1
        // giây
        processQueue();
    }

    // Hủy tìm trận khi người chơi bấm nút "Hủy" hoặc mất kết nối mạng
    public synchronized void leaveQueue(Long userId) {
        if (userId == null)
            return;
        boolean removed = waitingQueue.removeIf(ticket -> ticket.getUserId() == userId);
        if (removed) {
            System.out.println("[Matchmaking] User ID " + userId + " đã rời hàng đợi.");
        }
    }

    // Vòng lặp quét hàng đợi định kỳ (Core Logic của Task 2)
    public synchronized void processQueue() {
        if (waitingQueue.isEmpty())
            return;

        long now = System.currentTimeMillis();
        List<QueueTicket> timeoutList = new ArrayList<>();
        List<QueueTicket> activeList = new ArrayList<>();

        // 1. Phân loại vé hết hạn 60 giây và vé còn hiệu lực
        for (QueueTicket ticket : waitingQueue) {
            // Nếu người chơi ngắt kết nối thì bỏ qua
            if (ticket.getSession() == null || ticket.getUserId() == 0) {
                continue;
            }

            long waitSeconds = (now - ticket.getJoinTimeMillis()) / 1000;
            if (waitSeconds >= TIMEOUT_SECONDS) {
                timeoutList.add(ticket);
            } else {
                activeList.add(ticket);
            }
        }

        // 2. XỬ LÝ TIMEOUT 60S (Yêu cầu 2)
        for (QueueTicket timeoutTicket : timeoutList) {
            waitingQueue.remove(timeoutTicket);
            System.out.println("[Matchmaking] Quá 60s không tìm thấy đối thủ cho: " + timeoutTicket.getUsername());

            // Gửi thông báo QUEUE_TIMEOUT_NOTIFY để Client đưa về phòng chờ
            timeoutTicket.getSession().sendPacket(new Packet(
                    OpCode.QUEUE_TIMEOUT_NOTIFY,
                    "{\"message\":\"Không tìm thấy đối thủ phù hợp sau 60 giây. Đã đưa về phòng chờ.\"}"));
        }

        // 3. XỬ LÝ GHÉP CẶP 2 NGƯỜI PHÙ HỢP
        List<QueueTicket> matchedInThisTurn = new ArrayList<>();

        for (int i = 0; i < activeList.size(); i++) {
            QueueTicket t1 = activeList.get(i);
            if (matchedInThisTurn.contains(t1) || !waitingQueue.contains(t1))
                continue;

            for (int j = i + 1; j < activeList.size(); j++) {
                QueueTicket t2 = activeList.get(j);
                if (matchedInThisTurn.contains(t2) || !waitingQueue.contains(t2))
                    continue;

                // Kiểm tra 2 người chơi khác nhau và phù hợp tiêu chí (chủ đề, độ khó)
                if (t1.getUserId() != t2.getUserId() && isCompatible(t1, t2)) {
                    matchedInThisTurn.add(t1);
                    matchedInThisTurn.add(t2);
                    waitingQueue.remove(t1);
                    waitingQueue.remove(t2);

                    System.out
                            .println("[Matchmaking] Ghép thành công: " + t1.getUsername() + " VS " + t2.getUsername());

                    // Khởi tạo phòng thi đấu và gửi thông báo cho cả 2 người chơi
                    handleMatchFound(t1, t2);
                    break;
                }
            }
        }
    }

    // Kiểm tra độ tương thích giữa 2 người chơi (Cùng chủ đề và độ khó)
    private boolean isCompatible(QueueTicket t1, QueueTicket t2) {
        // Cùng độ khó
        boolean diffMatch = Objects.equals(t1.getDifficulty(), t2.getDifficulty());

        // Cùng chủ đề (hoặc một trong 2 người chọn ngẫu nhiên: topicId == null hoặc 0)
        boolean topicMatch = (t1.getTopicId() == null || t1.getTopicId() == 0 ||
                t2.getTopicId() == null || t2.getTopicId() == 0 ||
                Objects.equals(t1.getTopicId(), t2.getTopicId()));

        return diffMatch && topicMatch;
    }

    // Tạo trận đấu và gửi thông báo MATCH_FOUND_NOTIFY cho cả 2 client
    private void handleMatchFound(QueueTicket t1, QueueTicket t2) {
        Integer topicId = t1.getTopicId() != null && t1.getTopicId() > 0 ? t1.getTopicId() : t2.getTopicId();
        String difficulty = t1.getDifficulty();

        // 1. Tạo phòng đấu trong BattleManager
        BattleRoom room = BattleManager.getInstance().createBattle(t1.getSession(), t2.getSession(), topicId,
                difficulty);

        // 2. Lấy thông tin hồ sơ của cả 2 để gửi cho nhau
        UserProfile p1Profile = userDAO.getUserProfile(t1.getUserId());
        UserProfile p2Profile = userDAO.getUserProfile(t2.getUserId());

        // Gửi MATCH_FOUND_NOTIFY cho Player 1 (thông tin đối thủ là Player 2)
        MatchmakingDTO.MatchFoundNotify notifyP1 = new MatchmakingDTO.MatchFoundNotify();
        notifyP1.matchId = room.getMatchId();
        notifyP1.matchCode = "MATCH_" + room.getMatchId();
        notifyP1.opponentId = t2.getUserId();
        notifyP1.opponentUsername = t2.getUsername();
        notifyP1.opponentFullName = (p2Profile != null && p2Profile.getFullName() != null) ? p2Profile.getFullName()
                : t2.getUsername();
        notifyP1.opponentAvatar = (p2Profile != null) ? p2Profile.getAvatar() : null;
        notifyP1.totalQuestions = 5;
        notifyP1.timeLimitSeconds = 10;
        t1.getSession().sendPacket(new Packet(OpCode.MATCH_FOUND_NOTIFY, JsonUtil.toJson(notifyP1)));

        // Gửi MATCH_FOUND_NOTIFY cho Player 2 (thông tin đối thủ là Player 1)
        MatchmakingDTO.MatchFoundNotify notifyP2 = new MatchmakingDTO.MatchFoundNotify();
        notifyP2.matchId = room.getMatchId();
        notifyP2.matchCode = "MATCH_" + room.getMatchId();
        notifyP2.opponentId = t1.getUserId();
        notifyP2.opponentUsername = t1.getUsername();
        notifyP2.opponentFullName = (p1Profile != null && p1Profile.getFullName() != null) ? p1Profile.getFullName()
                : t1.getUsername();
        notifyP2.opponentAvatar = (p1Profile != null) ? p1Profile.getAvatar() : null;
        notifyP2.totalQuestions = 5;
        notifyP2.timeLimitSeconds = 10;
        t2.getSession().sendPacket(new Packet(OpCode.MATCH_FOUND_NOTIFY, JsonUtil.toJson(notifyP2)));
    }
}