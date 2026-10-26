package com.quizzlive.client.config;

import java.io.InputStream;
import java.util.Properties;

public class ClientConfig {
    private static String host = "127.0.0.1";
    private static int port = 8888;
    private static boolean sslEnabled = false;

    static {
        loadConfig();
    }

    private static void loadConfig() {
        try (InputStream input = ClientConfig.class.getClassLoader().getResourceAsStream("client.properties")) {
            if (input != null) {
                Properties prop = new Properties();
                prop.load(input);
                host = prop.getProperty("server.host", "127.0.0.1");
                port = Integer.parseInt(prop.getProperty("server.port", "8888"));
                sslEnabled = Boolean.parseBoolean(prop.getProperty("server.ssl.enabled", "false"));
            }
        } catch (Exception e) {
            System.out.println("[ClientConfig] Could not load client.properties, using defaults.");
        }
    }

    public static String getHost() { return host; }
    public static int getPort() { return port; }
    public static boolean isSslEnabled() { return sslEnabled; }
}
