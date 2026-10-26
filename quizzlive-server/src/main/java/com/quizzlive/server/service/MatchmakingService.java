package com.quizzlive.server.service;

import com.quizzlive.server.network.ClientSession;

/**
 * Service xử lý Hàng đợi Ghép cặp tự động
 * Phụ trách Yêu cầu 2: Ghép cặp tự động trong 60 giây; hết thời gian chưa ghép được thì đưa về phòng chờ.
 * Phụ trách Yêu cầu 4: Ghép cặp theo Chủ đề / Độ khó được chọn.
 */
public class MatchmakingService {
    private static final MatchmakingService instance = new MatchmakingService();

    private MatchmakingService() {}

    public static MatchmakingService getInstance() {
        return instance;
    }

    /**
     * TODO: Dev Nhóm 7 triển khai
     * 1. Đưa ClientSession vào hàng đợi (Queue) kèm topicId, difficulty và thời điểm vào
     * 2. Phản hồi QUEUE_JOIN_RESP cho client
     */
    public synchronized void joinQueue(ClientSession session, Integer topicId, String difficulty) {
        // TODO: Viết code thêm người chơi vào hàng đợi
    }

    /**
     * TODO: Dev Nhóm 7 triển khai
     * Hủy tìm trận khi người chơi bấm nút "Hủy tìm kiếm" hoặc mất kết nối
     */
    public synchronized void leaveQueue(Long userId) {
        // TODO: Viết code xóa người chơi khỏi hàng đợi
    }

    /**
     * TODO: Dev Nhóm 7 triển khai (Chạy định kỳ mỗi 1 giây qua Timer/ScheduledExecutorService)
     * 1. Duyệt hàng đợi: Nếu người chơi chờ >= 60 giây -> Xóa khỏi queue, gửi gói tin QUEUE_TIMEOUT_NOTIFY để đưa về phòng chờ (Yêu cầu 2)
     * 2. Nếu tìm thấy 2 người chơi phù hợp (cùng chủ đề/độ khó) -> Ghép cặp, tạo BattleRoom, gửi gói tin MATCH_FOUND_NOTIFY (Yêu cầu 14)
     */
    public synchronized void processQueue() {
        // TODO: Viết logic timer quét hàng đợi 60s và ghép cặp
    }
}
