package com.quizzlive.server.service;

import com.quizzlive.server.network.ClientSession;
import com.quizzlive.server.network.SessionManager;
import com.quizzlive.shared.dto.ChallengeDTO;

/**
 * Service xử lý Thách đấu bạn bè
 * Phụ trách Yêu cầu 3: Thách đấu bạn bè: mời trực tiếp một người đang online vào trận.
 */
public class ChallengeService {
    private static final ChallengeService instance = new ChallengeService();

    private ChallengeService() {}

    public static ChallengeService getInstance() {
        return instance;
    }

    /**
     * TODO: Dev Nhóm 7 triển khai
     * 1. Tìm ClientSession của đối thủ trong SessionManager theo targetUserId
     * 2. Nếu đối thủ đang online: lưu lời mời (vào Map/DB) và gửi CHALLENGE_INVITE_NOTIFY cho đối thủ
     * 3. Nếu không online: báo lỗi về cho người gửi
     */
    public void sendChallenge(ClientSession sender, ChallengeDTO.InviteRequest req, SessionManager sessionManager) {
        // TODO: Viết code gửi lời mời thách đấu
    }

    /**
     * TODO: Dev Nhóm 7 triển khai
     * 1. Khi đối thủ chọn Đồng ý (accepted = true): Tạo phòng đấu 1v1 (BattleRoom), gửi MATCH_FOUND_NOTIFY cho cả 2 người
     * 2. Khi đối thủ chọn Từ chối (accepted = false): Gửi thông báo từ chối về cho người thách đấu
     */
    public void handleResponse(ClientSession responder, ChallengeDTO.RespondRequest req) {
        // TODO: Viết code xử lý chấp nhận/từ chối lời mời
    }
}
