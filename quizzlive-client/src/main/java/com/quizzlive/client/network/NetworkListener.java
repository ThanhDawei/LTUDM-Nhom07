package com.quizzlive.client.network;

import com.quizzlive.shared.protocol.Packet;

/**
 * Interface lắng nghe các sự kiện mạng từ SocketClient
 * (Áp dụng Observer Pattern)
 */
public interface NetworkListener {
    void onPacketReceived(Packet packet);
    default void onConnected() {}
    default void onDisconnected() { onDisconnected("Mất kết nối tới máy chủ."); }
    default void onDisconnected(String reason) {}
    default void onError(Throwable cause) {}
}
