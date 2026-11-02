package com.quizzlive.server.service;

import com.quizzlive.shared.dto.TournamentDTO;

/**
 * Service xử lý Giải đấu (Tournament)
 * Phụ trách Yêu cầu 9: Giải đấu: tổ chức giải nhiều người theo vòng/bảng, có bảng xếp hạng của giải.
 */
public class TournamentService {

    /**
     * TODO: Dev Nhóm 7 triển khai
     * Lấy danh sách các giải đấu đang và sắp diễn ra từ bảng GIAI_DAU
     */
    public TournamentDTO.TournamentItem[] getActiveTournaments() {
        // TODO: Viết code lấy danh sách giải đấu
        return new TournamentDTO.TournamentItem[0];
    }

    /**
     * TODO: Dev Nhóm 7 triển khai
     * Đăng ký người chơi vào giải đấu (lưu vào bảng XEP_HANG_GIAI)
     */
    public boolean registerTournament(long tournamentId, long userId) {
        // TODO: Viết code đăng ký giải đấu
        return false;
    }

    /**
     * TODO: Dev Nhóm 7 triển khai
     * Bốc thăm chia bảng/vòng (tạo các trận đấu trong bảng TRAN_DAU với VongDau, BangDau) và bắt đầu giải đấu
     */
    public void generateBrackets(long tournamentId) {
        // TODO: Viết giải thuật chia cặp đấu / bảng đấu
    }

    /**
     * TODO: Dev Nhóm 7 triển khai
     * Lấy bảng xếp hạng điểm số của giải đấu (bảng XEP_HANG_GIAI)
     */
    public TournamentDTO.StandingItem[] getTournamentStandings(long tournamentId) {
        // TODO: Viết code truy vấn bảng xếp hạng giải đấu
        return new TournamentDTO.StandingItem[0];
    }
}
