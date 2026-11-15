package com.quizzlive.server.service;

import com.quizzlive.server.network.ClientSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Quản lý tập trung các phòng đấu 1v1 đang diễn ra
 * Phụ trách Yêu cầu 14: Điều phối trận đấu, đồng bộ hai người chơi
 */
public class BattleManager {
    private static final BattleManager instance = new BattleManager();
    private final Map<Long, BattleRoom> activeRooms = new ConcurrentHashMap<>();
    private final AtomicLong matchIdGenerator = new AtomicLong(1000);

    private BattleManager() {}

    public static BattleManager getInstance() {
        return instance;
    }

    /**
     * TODO: Tạo phòng thi đấu mới và bắt đầu trận
     */
    public BattleRoom createBattle(ClientSession p1, ClientSession p2, Integer topicId, String difficulty) {
        long matchId = matchIdGenerator.incrementAndGet();
        BattleRoom room = new BattleRoom(matchId, p1, p2, topicId, difficulty);
        activeRooms.put(matchId, room);
        room.start();
        return room;
    }

    /**
     * Kiểm tra xem người chơi có đang trong trận đấu 1v1 chưa kết thúc hay không
     */
    public boolean isUserInBattle(long userId) {
        for (BattleRoom room : activeRooms.values()) {
            if (!room.isMatchFinished()) {
                if ((room.getPlayer1() != null && room.getPlayer1().getUserId() != null && room.getPlayer1().getUserId() == userId) ||
                    (room.getPlayer2() != null && room.getPlayer2().getUserId() != null && room.getPlayer2().getUserId() == userId)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**

     * Điều hướng câu trả lời từ người chơi vào đúng phòng đấu
     */
    public void handleAnswerSubmit(long userId, long matchId, Long selectedAnswerId, int responseTime) {
        BattleRoom room = activeRooms.get(matchId);
        if (room != null) {
            room.submitAnswer(userId, selectedAnswerId, responseTime);
        }
    }

    /**
     * Xóa phòng đấu khỏi danh sách active
     */
    public void removeBattle(long matchId) {
        activeRooms.remove(matchId);
    }

    /**
     * Xử lý khi người chơi bấm nút Đầu Hàng
     */
    public void handleSurrender(long userId, long matchId) {
        BattleRoom room = activeRooms.get(matchId);
        if (room == null) {
            for (BattleRoom r : activeRooms.values()) {
                if ((r.getPlayer1() != null && r.getPlayer1().getUserId() != null && r.getPlayer1().getUserId() == userId) ||
                    (r.getPlayer2() != null && r.getPlayer2().getUserId() != null && r.getPlayer2().getUserId() == userId)) {
                    room = r;
                    break;
                }
            }
        }
        if (room != null) {
            activeRooms.remove(room.getMatchId());
            room.handleSurrender(userId);
        }
    }

    /**
     * Xử lý khi người chơi mất kết nối đột ngột
     */
    public void handleUserDisconnected(long userId) {
        for (BattleRoom room : activeRooms.values()) {
            if ((room.getPlayer1() != null && room.getPlayer1().getUserId() != null && room.getPlayer1().getUserId() == userId) ||
                (room.getPlayer2() != null && room.getPlayer2().getUserId() != null && room.getPlayer2().getUserId() == userId)) {
                activeRooms.remove(room.getMatchId());
                room.handleDisconnect(userId);
                break;
            }
        }
    }
}
