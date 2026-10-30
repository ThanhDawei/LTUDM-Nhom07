package com.quizzlive.client.service;

import com.quizzlive.client.network.SocketClient;
import com.quizzlive.shared.dto.ChallengeDTO;
import com.quizzlive.shared.protocol.OpCode;
import com.quizzlive.shared.protocol.Packet;
import com.quizzlive.shared.util.JsonUtil;

/**
 * Service phía Client xử lý Thách đấu bạn bè
 * Phụ trách Yêu cầu 3
 */
public class ClientChallengeService {
    private final SocketClient socketClient = SocketClient.getInstance();

    /**
     * TODO: Dev Client gọi hàm này khi bấm nút "Thách đấu" một người đang online
     */
    public void sendChallenge(long targetUserId, Integer topicId, String difficulty) {
        ChallengeDTO.InviteRequest req = new ChallengeDTO.InviteRequest();
        req.targetUserId = targetUserId;
        req.topicId = topicId;
        req.difficulty = difficulty;
        socketClient.sendPacket(new Packet(OpCode.CHALLENGE_INVITE_REQ, JsonUtil.toJson(req)));
    }

    /**
     * TODO: Dev Client gọi hàm này khi người dùng bấm "Chấp nhận" lời mời thách đấu
     */
    public void acceptChallenge(long invitationId) {
        ChallengeDTO.RespondRequest req = new ChallengeDTO.RespondRequest();
        req.invitationId = invitationId;
        req.accepted = true;
        socketClient.sendPacket(new Packet(OpCode.CHALLENGE_ACCEPT_REQ, JsonUtil.toJson(req)));
    }

    /**
     * TODO: Dev Client gọi hàm này khi người dùng bấm "Từ chối" lời mời thách đấu
     */
    public void rejectChallenge(long invitationId) {
        ChallengeDTO.RespondRequest req = new ChallengeDTO.RespondRequest();
        req.invitationId = invitationId;
        req.accepted = false;
        socketClient.sendPacket(new Packet(OpCode.CHALLENGE_REJECT_REQ, JsonUtil.toJson(req)));
    }
}
