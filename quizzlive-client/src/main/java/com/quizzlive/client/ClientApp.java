package com.quizzlive.client;

import com.quizzlive.client.config.ClientConfig;
import com.quizzlive.client.network.SocketClient;
import com.quizzlive.shared.protocol.OpCode;
import com.quizzlive.shared.protocol.Packet;

public class ClientApp {
    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println("      QUIZZLIVE CLIENT - STARTING        ");
        System.out.println("=========================================");

        try {
            SocketClient client = new SocketClient(
                    ClientConfig.getHost(),
                    ClientConfig.getPort(),
                    ClientConfig.isSslEnabled()
            );
            client.connect();

            // Send demo login request
            Packet loginPacket = new Packet(OpCode.LOGIN_REQ, "{\"username\":\"Player_One\"}");
            client.sendPacket(loginPacket);

            // Keep main thread alive for demo
            Thread.sleep(5000);

        } catch (Exception e) {
            System.err.println("[Client] Failed to start client: " + e.getMessage());
        }
    }
}
