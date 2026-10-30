package com.quizzlive.client.service;

import com.quizzlive.client.network.SocketClient;
import com.quizzlive.shared.dto.IQTestDTO;
import com.quizzlive.shared.protocol.OpCode;
import com.quizzlive.shared.protocol.Packet;
import com.quizzlive.shared.util.JsonUtil;

/**
 * Service phía Client xử lý kiểm tra IQ hình ảnh trong sảnh chờ
 * Phụ trách Yêu cầu 11
 */
public class ClientIQService {
    private final SocketClient socketClient = SocketClient.getInstance();

    /**
     * TODO: Dev Client gọi hàm này khi người chơi bấm nút "Làm bài Test IQ" trong lúc chờ tìm trận
     */
    public void requestIQQuestion() {
        socketClient.sendPacket(new Packet(OpCode.IQ_TEST_START_REQ, "{}"));
    }

    /**
     * TODO: Dev Client gọi hàm này khi người chơi chọn đáp án cho câu đố IQ hình ảnh
     */
    public void submitIQAnswer(long questionId, long selectedAnswerId) {
        IQTestDTO.IQSubmitRequest req = new IQTestDTO.IQSubmitRequest();
        req.questionId = questionId;
        req.selectedAnswerId = selectedAnswerId;
        socketClient.sendPacket(new Packet(OpCode.IQ_SUBMIT_ANSWER_REQ, JsonUtil.toJson(req)));
    }
}
