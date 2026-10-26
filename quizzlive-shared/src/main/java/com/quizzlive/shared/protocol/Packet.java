package com.quizzlive.shared.protocol;

import java.io.Serializable;

/**
 * Base Network Packet transferred over sockets
 */
public class Packet implements Serializable {
    private static final long serialVersionUID = 1L;

    private OpCode opCode;
    private String payloadJson;

    public Packet() {}

    public Packet(OpCode opCode, String payloadJson) {
        this.opCode = opCode;
        this.payloadJson = payloadJson;
    }

    public OpCode getOpCode() {
        return opCode;
    }

    public void setOpCode(OpCode opCode) {
        this.opCode = opCode;
    }

    public String getPayloadJson() {
        return payloadJson;
    }

    public void setPayloadJson(String payloadJson) {
        this.payloadJson = payloadJson;
    }
}
