package com.quizzlive.client.service;

import com.quizzlive.client.network.SocketClient;
import com.quizzlive.shared.protocol.OpCode;
import com.quizzlive.shared.protocol.Packet;

/**
 * Service phía Client xử lý Giải đấu (Tournament)
 * Phụ trách Yêu cầu 9
 */
public class ClientTournamentService {
    private final SocketClient socketClient = SocketClient.getInstance();

    /**
     * TODO: Dev Client gọi hàm này khi người chơi vào tab Giải đấu để lấy danh sách các giải đấu hiện có
     */
    public void requestTournamentList() {
        socketClient.sendPacket(new Packet(OpCode.TOURNAMENT_LIST_REQ, "{}"));
    }

    /**
     * TODO: Dev Client gọi hàm này khi người chơi bấm nút "Đăng ký tham gia giải đấu"
     */
    public void joinTournament(long tournamentId) {
        socketClient.sendPacket(new Packet(OpCode.TOURNAMENT_JOIN_REQ, "{\"tournamentId\":" + tournamentId + "}"));
    }

    /**
     * TODO: Dev Client gọi hàm này để xem thông tin chi tiết bảng xếp hạng / nhánh đấu của giải
     */
    public void requestTournamentStandings(long tournamentId) {
        socketClient.sendPacket(new Packet(OpCode.TOURNAMENT_STANDINGS_REQ, "{\"tournamentId\":" + tournamentId + "}"));
    }
}
