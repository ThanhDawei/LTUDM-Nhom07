package com.quizzlive.shared.protocol;

/**
 * Operation Codes (OpCodes) for Quizzlive Network Protocol
 * Specially designed for Nhóm 07 - Đề tài 09: Tranh tài kiến thức (16 yêu cầu)
 */
public enum OpCode {
    // --- 1. AUTHENTICATION & PROFILE (Yêu cầu 1) ---
    REGISTER_REQ(101),
    REGISTER_RESP(102),
    VERIFY_OTP_REQ(103),
    VERIFY_OTP_RESP(104),
    RESEND_OTP_REQ(105),
    RESEND_OTP_RESP(106),
    LOGIN_REQ(107),
    LOGIN_RESP(108),
    LOGOUT_REQ(109),
    LOGOUT_RESP(110),
    GET_PROFILE_REQ(111),
    GET_PROFILE_RESP(112),
    UPDATE_PROFILE_REQ(113),
    UPDATE_PROFILE_RESP(114),
    ONLINE_USERS_REQ(115),
    ONLINE_USERS_RESP(116),

    // --- 2. MATCHMAKING 60S (Yêu cầu 2, 4) ---
    QUEUE_JOIN_REQ(201),
    QUEUE_JOIN_RESP(202),
    QUEUE_LEAVE_REQ(203),
    QUEUE_LEAVE_RESP(204),
    QUEUE_TIMEOUT_NOTIFY(205), // Sau 60s không tìm thấy đưa về phòng chờ
    MATCH_FOUND_NOTIFY(206),   // Tìm thấy đối thủ -> Chuẩn bị vào trận

    // --- 3. THÁCH ĐẤU BẠN BÈ (Yêu cầu 3) ---
    CHALLENGE_INVITE_REQ(301),
    CHALLENGE_INVITE_NOTIFY(302),
    CHALLENGE_ACCEPT_REQ(303),
    CHALLENGE_REJECT_REQ(304),
    CHALLENGE_RESP(305),

    // --- 4. TRẬN ĐẤU REALTIME 1V1 (Yêu cầu 5, 6, 7, 8) ---
    BATTLE_READY_REQ(401),
    BATTLE_START_NOTIFY(402),
    BATTLE_QUESTION_NOTIFY(403),     // Server đẩy câu hỏi mới cho 2 người chơi
    BATTLE_SUBMIT_ANSWER_REQ(404),   // Client gửi đáp án đã chọn + thời gian còn lại
    BATTLE_ROUND_RESULT_NOTIFY(405), // Server công bố lựa chọn của 2 bên + đáp án đúng
    BATTLE_FINAL_RESULT_NOTIFY(406), // Kết quả chung cuộc sau 5 câu
    BATTLE_SURRENDER_REQ(407),
    BATTLE_OPPONENT_DISCONNECTED(408),

    // --- 5. TEST IQ TRONG LÚC CHỜ (Yêu cầu 11) ---
    IQ_TEST_START_REQ(501),
    IQ_QUESTION_NOTIFY(502),
    IQ_SUBMIT_ANSWER_REQ(503),
    IQ_RESULT_NOTIFY(504),

    // --- 6. GIẢI ĐẤU (Yêu cầu 9) ---
    TOURNAMENT_LIST_REQ(601),
    TOURNAMENT_LIST_RESP(602),
    TOURNAMENT_JOIN_REQ(603),
    TOURNAMENT_JOIN_RESP(604),
    TOURNAMENT_STANDINGS_REQ(605),
    TOURNAMENT_STANDINGS_RESP(606),

    // --- 7. BẢNG XẾP HẠNG & LỊCH SỬ (Yêu cầu 10) ---
    LEADERBOARD_REQ(701),
    LEADERBOARD_RESP(702),
    MATCH_HISTORY_REQ(703),
    MATCH_HISTORY_RESP(704),

    // --- 8. TOPIC / CHỦ ĐỀ & CẤU HÌNH (Yêu cầu 4) ---
    TOPIC_LIST_REQ(801),
    TOPIC_LIST_RESP(802),

    // --- 9. HỆ THỐNG / LỖI ---
    SYSTEM_NOTIFICATION_NOTIFY(901),
    USER_BLOCKED_NOTIFY(902),
    ERROR_RESP(999);

    private final int code;

    OpCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static OpCode fromCode(int code) {
        for (OpCode op : OpCode.values()) {
            if (op.code == code) {
                return op;
            }
        }
        return ERROR_RESP;
    }
}
