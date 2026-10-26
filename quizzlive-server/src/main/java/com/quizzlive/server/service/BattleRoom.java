package com.quizzlive.server.service;

import com.quizzlive.server.network.ClientSession;

/**
 * Phòng thi đấu 1v1 thời gian thực
 * Phụ trách:
 * - Yêu cầu 5: Mỗi trận 5 câu (cấu hình được), mỗi câu 4 đáp án, tối đa 10s; điểm tính theo thời gian còn lại; sai/không trả lời 0 điểm.
 * - Yêu cầu 6: Chuyển câu khi CẢ HAI đã trả lời hoặc HẾT 10 giây; khi đó mỗi người thấy lựa chọn của đối thủ và đáp án đúng.
 * - Yêu cầu 7: Câu hỏi trong một trận không trùng nhau; thứ tự câu và đáp án được xáo trộn.
 * - Yêu cầu 8: Kết quả trận: sau 5 câu, ai điểm cao hơn thắng.
 * - Yêu cầu 14: Điều phối trận đấu, đồng bộ 2 người chơi.
 */
public class BattleRoom {
    private final long matchId;
    private final ClientSession player1;
    private final ClientSession player2;

    public BattleRoom(long matchId, ClientSession player1, ClientSession player2) {
        this.matchId = matchId;
        this.player1 = player1;
        this.player2 = player2;
    }

    /**
     * TODO: Dev Nhóm 7 triển khai
     * 1. Lấy danh sách 5 câu hỏi ngẫu nhiên từ QuestionDAO
     * 2. Bắt đầu vòng thi đấu, gửi BATTLE_START_NOTIFY cho cả 2 người chơi
     */
    public void start() {
        // TODO: Viết code khởi chạy trận đấu
    }

    /**
     * TODO: Dev Nhóm 7 triển khai
     * 1. Xáo trộn thứ tự 4 đáp án ngẫu nhiên (Yêu cầu 7)
     * 2. Gửi gói BATTLE_QUESTION_NOTIFY cho 2 người chơi
     * 3. Bật bộ đếm ngược 10 giây (Timer)
     */
    public synchronized void nextQuestion() {
        // TODO: Viết code phát câu hỏi kế tiếp
    }

    /**
     * TODO: Dev Nhóm 7 triển khai
     * 1. Nhận đáp án người chơi gửi lên
     * 2. Tính điểm: nếu đúng -> Điểm = Điểm cơ bản + Điểm thưởng theo thời gian còn lại (Yêu cầu 5)
     * 3. Kiểm tra điều kiện đồng bộ (Yêu cầu 6): Nếu CẢ HAI đã trả lời -> Dừng timer 10s và gọi onBothAnswered()
     */
    public synchronized void submitAnswer(long userId, Long selectedAnswerId, int responseTime) {
        // TODO: Viết code chấm điểm câu trả lời phía server
    }

    /**
     * TODO: Dev Nhóm 7 triển khai
     * Khi hết 10 giây hoặc cả 2 đã trả lời xong:
     * Gửi gói tin BATTLE_ROUND_RESULT_NOTIFY công bố lựa chọn của cả 2 bên và đáp án đúng (Yêu cầu 6)
     * Chờ 2-3 giây rồi gọi nextQuestion() hoặc finishMatch()
     */
    private synchronized void onBothAnswered() {
        // TODO: Viết code công bố kết quả vòng đấu
    }

    /**
     * TODO: Dev Nhóm 7 triển khai
     * Sau khi hoàn thành 5 câu:
     * 1. So sánh tổng điểm 2 bên để xác định người thắng / hòa (Yêu cầu 8)
     * 2. Cập nhật thành tích vào CSDL (bảng HO_SO_NGUOI_DUNG, TRAN_DAU, NGUOI_CHOI_TRAN)
     * 3. Gửi BATTLE_FINAL_RESULT_NOTIFY cho cả 2 người
     */
    private synchronized void finishMatch() {
        // TODO: Viết code kết thúc trận đấu và lưu CSDL
    }

    /**
     * TODO: Xử lý khi 1 người chơi bị ngắt kết nối giữa chừng
     */
    public void handleDisconnect(long disconnectedUserId) {
        // TODO: Xử lý người thoát trận giữa chừng
    }

    public long getMatchId() { return matchId; }
    public ClientSession getPlayer1() { return player1; }
    public ClientSession getPlayer2() { return player2; }
}
