package com.quizzlive.server.config;

import java.io.InputStream;
import java.util.Properties;

public class ServerConfig {
    private static int port = 8888;
    private static int threadPoolSize = 50;
    private static boolean sslEnabled = false;
    private static String keyStorePath = "server.keystore";
    private static String keyStorePassword = "changeit";

    private static String dbDriver = "com.mysql.cj.jdbc.Driver";
    private static String dbUrl = "jdbc:mysql://localhost:3306/quizzlive_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&characterEncoding=UTF-8";
    private static String dbUsername = "root";
    private static String dbPassword = "";
    private static boolean dbAutoInit = true;

    private static int defaultQuestionCount = 5;
    private static int defaultTimePerQuestion = 10;
    private static int baseScore = 100;
    private static boolean speedBonusEnabled = true;
    private static int matchmakingTimeoutSeconds = 60;

    static {
        loadConfig();
    }

    private static void loadConfig() {
        try (InputStream input = ServerConfig.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input != null) {
                Properties prop = new Properties();
                prop.load(input);

                port = Integer.parseInt(prop.getProperty("server.port", "8888"));
                threadPoolSize = Integer.parseInt(prop.getProperty("server.thread-pool-size", "50"));
                sslEnabled = Boolean.parseBoolean(prop.getProperty("server.ssl.enabled", "false"));
                keyStorePath = prop.getProperty("server.ssl.key-store-path", "server.keystore");
                keyStorePassword = prop.getProperty("server.ssl.key-store-password", "changeit");

                dbDriver = prop.getProperty("db.driver", "com.mysql.cj.jdbc.Driver");
                dbUrl = prop.getProperty("db.url", dbUrl);
                dbUsername = prop.getProperty("db.username", "root");
                dbPassword = prop.getProperty("db.password", "");
                dbAutoInit = Boolean.parseBoolean(prop.getProperty("db.auto-init", "true"));

                defaultQuestionCount = Integer.parseInt(prop.getProperty("match.default-question-count", "5"));
                defaultTimePerQuestion = Integer.parseInt(prop.getProperty("match.default-time-per-question", "10"));
                baseScore = Integer.parseInt(prop.getProperty("match.base-score", "100"));
                speedBonusEnabled = Boolean.parseBoolean(prop.getProperty("match.speed-bonus-enabled", "true"));
                matchmakingTimeoutSeconds = Integer.parseInt(prop.getProperty("matchmaking.timeout-seconds", "60"));
            }
        } catch (Exception e) {
            System.out.println("[ServerConfig] Could not load application.properties, using defaults.");
        }
    }

    public static int getPort() { return port; }
    public static int getThreadPoolSize() { return threadPoolSize; }
    public static boolean isSslEnabled() { return sslEnabled; }
    public static String getKeyStorePath() { return keyStorePath; }
    public static String getKeyStorePassword() { return keyStorePassword; }

    public static String getDbDriver() { return dbDriver; }
    public static String getDbUrl() { return dbUrl; }
    public static String getDbUsername() { return dbUsername; }
    public static String getDbPassword() { return dbPassword; }
    public static boolean isDbAutoInit() { return dbAutoInit; }

    public static int getDefaultQuestionCount() { return defaultQuestionCount; }
    public static int getDefaultTimePerQuestion() { return defaultTimePerQuestion; }
    public static int getBaseScore() { return baseScore; }
    public static boolean isSpeedBonusEnabled() { return speedBonusEnabled; }
    public static int getMatchmakingTimeoutSeconds() { return matchmakingTimeoutSeconds; }
}
