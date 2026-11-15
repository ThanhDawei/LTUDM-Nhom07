package com.quizzlive.server.security;

import com.quizzlive.shared.util.HybridCryptoUtil;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Quản lý khóa bất đối xứng RSA của Server phục vụ mã hóa lai (Hybrid Encryption)
 */
public class ServerSecurityManager {
    private static final ServerSecurityManager INSTANCE = new ServerSecurityManager();

    private final KeyPair rsaKeyPair;

    private ServerSecurityManager() {
        System.out.println("[ServerSecurityManager] Đang sinh cặp khóa RSA-2048 cho Server...");
        long start = System.currentTimeMillis();
        this.rsaKeyPair = HybridCryptoUtil.generateRSAKeyPair();
        long duration = System.currentTimeMillis() - start;
        System.out.println("[ServerSecurityManager] Cặp khóa RSA-2048 đã sẵn sàng (thời gian: " + duration + "ms).");
        System.out.println("[ServerSecurityManager] Sẵn sàng phục vụ bắt tay mã hóa lai (RSA-2048 + AES-256 GCM).");
    }

    public static ServerSecurityManager getInstance() {
        return INSTANCE;
    }

    public PublicKey getPublicKey() {
        return rsaKeyPair.getPublic();
    }

    public PrivateKey getPrivateKey() {
        return rsaKeyPair.getPrivate();
    }
}
