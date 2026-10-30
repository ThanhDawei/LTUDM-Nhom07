package com.quizzlive.client.service;

import com.quizzlive.client.network.SocketClient;
import com.quizzlive.shared.dto.MatchmakingDTO;
import com.quizzlive.shared.protocol.OpCode;
import com.quizzlive.shared.protocol.Packet;
import com.quizzlive.shared.util.JsonUtil;

/**
 * Service phía Client xử lý Hàng đợi Ghép cặp tự động
 * Phụ trách Yêu cầu 2, 4
 */
public class ClientMatchmakingService {
    private final SocketClient socketClient = SocketClient.getInstance();

    /**
     * TODO: Dev Client gọi hàm này khi người dùng chọn Chủ đề, Độ khó và bấm "Tìm Trận"
     */
    public void joinQueue(Integer topicId, String difficulty) {
        MatchmakingDTO.QueueRequest req = new MatchmakingDTO.QueueRequest();
        req.topicId = topicId;
        req.difficulty = difficulty;
        socketClient.sendPacket(new Packet(OpCode.QUEUE_JOIN_REQ, JsonUtil.toJson(req)));
    }

    /**
     * TODO: Dev Client gọi hàm này khi người dùng bấm nút "Hủy Tìm Kiếm"
     */
    public void leaveQueue() {
        socketClient.sendPacket(new Packet(OpCode.QUEUE_LEAVE_REQ, "{}"));
    }

    /**
     * TODO: Dev Client gọi hàm này khi vào sảnh chờ để nạp danh sách chủ đề thi đấu (Yêu cầu 4)
     */
    public void requestTopicList() {
        socketClient.sendPacket(new Packet(OpCode.TOPIC_LIST_REQ, "{}"));
    }
}
