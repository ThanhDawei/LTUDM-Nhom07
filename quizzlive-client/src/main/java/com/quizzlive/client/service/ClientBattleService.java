package com.quizzlive.client.service;

import com.quizzlive.client.network.SocketClient;
import com.quizzlive.shared.dto.BattleDTO;
import com.quizzlive.shared.protocol.OpCode;
import com.quizzlive.shared.protocol.Packet;
import com.quizzlive.shared.util.JsonUtil;

/**
 * Service phía Client xử lý gửi đáp án và sự kiện trận đấu 1v1
 * Phụ trách Yêu cầu 5, 6, 7, 8
 */
public class ClientBattleService {
    private final SocketClient socketClient = SocketClient.getInstance();

    /**
     * TODO: Dev Client gọi hàm này khi người chơi chọn một đáp án (hoặc hết giờ -> selectedAnswerId = null)
     */
    public void submitAnswer(long matchId, int questionIndex, Long selectedAnswerId, int responseTimeSeconds) {
        BattleDTO.AnswerSubmitRequest req = new BattleDTO.AnswerSubmitRequest();
        req.matchId = matchId;
        req.questionIndex = questionIndex;
        req.selectedAnswerId = selectedAnswerId;
        req.responseTimeSeconds = responseTimeSeconds;
        socketClient.sendPacket(new Packet(OpCode.BATTLE_SUBMIT_ANSWER_REQ, JsonUtil.toJson(req)));
    }

    /**
     * TODO: Dev Client gọi hàm này khi người chơi bấm nút "Đầu hàng" / "Rời trận"
     */
    public void surrender(long matchId) {
        socketClient.sendPacket(new Packet(OpCode.BATTLE_SURRENDER_REQ, "{\"matchId\":" + matchId + "}"));
    }
}
