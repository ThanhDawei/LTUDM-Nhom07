package com.quizzlive.client.service;

import com.quizzlive.client.network.SocketClient;
import com.quizzlive.shared.protocol.OpCode;
import com.quizzlive.shared.protocol.Packet;

/**
 * Service phía Client xử lý xem Bảng xếp hạng và Lịch sử thi đấu
 * Phụ trách Yêu cầu 10
 */
public class ClientLeaderboardService {
    private final SocketClient socketClient = SocketClient.getInstance();

    /**
     * TODO: Dev Client gọi hàm này khi mở màn hình Bảng xếp hạng
     * Server sẽ trả về top người thắng nhiều nhất, chuỗi thắng dài nhất, v.v.
     */
    public void requestLeaderboard() {
        socketClient.sendPacket(new Packet(OpCode.LEADERBOARD_REQ, "{}"));
    }

    /**
     * TODO: Dev Client gọi hàm này khi người dùng xem Lịch sử đấu cá nhân
     */
    public void requestMatchHistory(long userId) {
        socketClient.sendPacket(new Packet(OpCode.MATCH_HISTORY_REQ, "{\"userId\":" + userId + "}"));
    }
}
