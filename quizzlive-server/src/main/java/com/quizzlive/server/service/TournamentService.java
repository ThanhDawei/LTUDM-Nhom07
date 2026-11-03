package com.quizzlive.server.service;

import com.quizzlive.server.dao.TournamentDAO;
import com.quizzlive.shared.dto.TournamentDTO;

import java.util.Collections;
import java.util.List;

/**
 * Service xử lý Giải đấu (Tournament)
 * Phụ trách Yêu cầu 9: Giải đấu: tổ chức giải nhiều người theo vòng/bảng, có
 * bảng xếp hạng của giải.
 */
public class TournamentService {

    private final TournamentDAO tournamentDAO = new TournamentDAO();

    /**
     * Lấy danh sách các giải đấu đang và sắp diễn ra từ bảng GIAI_DAU
     */
    public TournamentDTO.TournamentItem[] getActiveTournaments() {
        List<TournamentDTO.TournamentItem> list = tournamentDAO.getActiveTournaments();
        return list.toArray(new TournamentDTO.TournamentItem[0]);
    }

    /**
     * Đăng ký người chơi vào giải đấu (lưu vào bảng XEP_HANG_GIAI)
     */
    public boolean registerTournament(long tournamentId, long userId) {
        // 1. Kiểm tra xem người này đã đăng ký giải đấu này chưa
        if (tournamentDAO.isUserRegistered(tournamentId, userId)) {
            System.out.println(
                    "[TournamentService] Người dùng " + userId + " đã đăng ký giải " + tournamentId + " trước đó rồi.");
            return false;
        }

        // 2. Tiến hành đăng ký
        return tournamentDAO.registerParticipant(tournamentId, userId);
    }

    /**
     * Bốc thăm chia bảng/vòng ngẫu nhiên cho giải đấu
     */
    public void generateBrackets(long tournamentId) {
        List<Long> participants = tournamentDAO.getParticipantUserIds(tournamentId);
        if (participants == null || participants.size() < 2) {
            System.out.println("[TournamentService] Giải " + tournamentId + " chưa đủ thí sinh để chia bảng.");
            return;
        }

        // 1. Xáo trộn ngẫu nhiên danh sách thí sinh (Bốc thăm công bằng)
        Collections.shuffle(participants);

        // 2. Phân chia thí sinh vào các bảng đấu (Bảng A, B, C, D... mỗi bảng tối đa 4
        // người)
        String[] groupNames = { "Bảng A", "Bảng B", "Bảng C", "Bảng D" };
        for (int i = 0; i < participants.size(); i++) {
            String assignedGroup = groupNames[(i / 4) % groupNames.length];
            tournamentDAO.updateParticipantGroup(tournamentId, participants.get(i), assignedGroup);
        }

        System.out.println("[TournamentService] Đã bốc thăm chia bảng thành công cho giải " + tournamentId + " ("
                + participants.size() + " thí sinh)");
    }

    /**
     * Lấy bảng xếp hạng điểm số của giải đấu (bảng XEP_HANG_GIAI)
     */
    public TournamentDTO.StandingItem[] getTournamentStandings(long tournamentId) {
        List<TournamentDTO.StandingItem> list = tournamentDAO.getTournamentStandings(tournamentId);
        return list.toArray(new TournamentDTO.StandingItem[0]);
    }

    /**
     * Cập nhật điểm cho thí sinh khi trận đấu trong giải kết thúc
     */
    public void recordMatchResult(long tournamentId, long userId, boolean isWin, boolean isDraw) {
        tournamentDAO.updateStandingMatchResult(tournamentId, userId, isWin, isDraw);
    }
}