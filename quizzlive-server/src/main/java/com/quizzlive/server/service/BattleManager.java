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
     * TODO: Điều hướng câu trả lời từ người chơi vào đúng phòng đấu
     */
    public void handleAnswerSubmit(long userId, long matchId, Long selectedAnswerId, int responseTime) {
        BattleRoom room = activeRooms.get(matchId);
        if (room != null) {
            room.submitAnswer(userId, selectedAnswerId, responseTime);
        }
    }

    /**
     * TODO: Xử lý ngắt kết nối
     */
    public void handleUserDisconnected(long userId) {
        // TODO: Xóa người chơi khỏi phòng và xử lý thắng thua do bỏ cuộc
    }
}
