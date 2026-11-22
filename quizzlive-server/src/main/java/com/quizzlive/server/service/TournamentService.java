package com.quizzlive.server.service;

import com.quizzlive.server.dao.TournamentDAO;
import com.quizzlive.server.dao.UserDAO;
import com.quizzlive.server.entity.UserProfile;
import com.quizzlive.server.network.ClientSession;
import com.quizzlive.shared.dto.MatchmakingDTO;
import com.quizzlive.shared.dto.TournamentDTO;
import com.quizzlive.shared.protocol.OpCode;
import com.quizzlive.shared.protocol.Packet;
import com.quizzlive.shared.util.JsonUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service xử lý Giải đấu (Tournament)
 * Phụ trách Yêu cầu 9: Giải đấu: tổ chức giải nhiều người theo vòng/bảng, có
 * bảng xếp hạng của giải.
 */
public class TournamentService {

    private final TournamentDAO tournamentDAO = new TournamentDAO();
    private final UserDAO userDAO = new UserDAO();
    private static final Map<Long, List<ClientSession>> tournamentQueues = new ConcurrentHashMap<>();

    /**
     * Lấy danh sách các giải đấu đang và sắp diễn ra từ bảng GIAI_DAU
     */
    public TournamentDTO.TournamentItem[] getActiveTournaments() {
        List<TournamentDTO.TournamentItem> list = tournamentDAO.getActiveTournaments();
        return list.toArray(new TournamentDTO.TournamentItem[0]);
    }

    /**
     * Đăng ký người chơi vào giải đấu (lưu vào bảng XEP_HANG_GIAI) và tự động bốc thăm chia bảng
     */
    public boolean registerTournament(long tournamentId, long userId) {
        if (tournamentDAO.isTournamentFinished(tournamentId)) {
            return false;
        }
        if (tournamentDAO.isUserRegistered(tournamentId, userId)) {
            System.out.println(
                    "[TournamentService] Người dùng " + userId + " đã đăng ký giải " + tournamentId + " trước đó rồi.");
            return false;
        }

        boolean ok = tournamentDAO.registerParticipant(tournamentId, userId);
        if (ok) {
            generateBrackets(tournamentId);
        }
        return ok;
    }

    /**
     * Ghép cặp thi đấu 1v1 giữa các thí sinh đã đăng ký trong cùng giải đấu
     * Trả về: "FINISHED", "NOT_REGISTERED", "IN_BATTLE", "WAITING", hoặc "MATCHED"
     */
    public String joinTournamentBattleQueue(long tournamentId, ClientSession session) {
        if (session == null || session.getUserId() == null) {
            return "ERROR";
        }
        long userId = session.getUserId();

        if (tournamentDAO.isTournamentFinished(tournamentId)) {
            return "FINISHED";
        }
        if (!tournamentDAO.isUserRegistered(tournamentId, userId)) {
            return "NOT_REGISTERED";
        }
        if (BattleManager.getInstance().isUserInBattle(userId)) {
            return "IN_BATTLE";
        }

        ClientSession opponent = null;
        synchronized (tournamentQueues) {
            List<ClientSession> queue = tournamentQueues.computeIfAbsent(tournamentId, k -> new ArrayList<>());
            Iterator<ClientSession> it = queue.iterator();
            while (it.hasNext()) {
                ClientSession s = it.next();
                if (s == null || s.getUserId() == null || s.getUserId() == userId) {
                    if (s != null && s.getUserId() != null && s.getUserId() == userId) {
                        return "WAITING";
                    }
                    it.remove();
                    continue;
                }
                if (!BattleManager.getInstance().isUserInBattle(s.getUserId())) {
                    opponent = s;
                    it.remove();
                    break;
                }
            }
            if (opponent == null) {
                queue.add(session);
                return "WAITING";
            }
        }

        // Tìm thấy đối thủ trong giải -> Khởi tạo phòng đấu giải
        BattleRoom room = BattleManager.getInstance().createTournamentBattle(opponent, session, tournamentId);
        sendTournamentMatchFound(room, opponent, session);
        sendTournamentMatchFound(room, session, opponent);
        return "MATCHED";
    }

    private void sendTournamentMatchFound(BattleRoom room, ClientSession receiver, ClientSession opponent) {
        UserProfile oppProfile = userDAO.getUserProfile(opponent.getUserId());
        MatchmakingDTO.MatchFoundNotify notify = new MatchmakingDTO.MatchFoundNotify();
        notify.matchId = room.getMatchId();
        notify.matchCode = "TOUR_" + room.getMatchId();
        notify.opponentId = opponent.getUserId();
        notify.opponentUsername = opponent.getUsername();
        notify.opponentFullName = (oppProfile != null && oppProfile.getFullName() != null)
                ? oppProfile.getFullName() : opponent.getUsername();
        notify.opponentAvatar = (oppProfile != null) ? oppProfile.getAvatar() : null;
        notify.totalQuestions = BattleRoom.TOTAL_QUESTIONS;
        notify.timeLimitSeconds = BattleRoom.TIME_LIMIT_SECONDS;
        notify.topicId = null;
        notify.topicName = "Đấu Giải";
        notify.difficulty = "MEDIUM";
        receiver.sendPacket(new Packet(OpCode.MATCH_FOUND_NOTIFY, JsonUtil.toJson(notify)));
    }

    public static void leaveAllQueues(long userId) {
        synchronized (tournamentQueues) {
            for (List<ClientSession> queue : tournamentQueues.values()) {
                queue.removeIf(s -> s == null || s.getUserId() == null || s.getUserId() == userId);
            }
        }
    }

    public static void clearTournamentQueue(long tournamentId) {
        synchronized (tournamentQueues) {
            tournamentQueues.remove(tournamentId);
        }
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