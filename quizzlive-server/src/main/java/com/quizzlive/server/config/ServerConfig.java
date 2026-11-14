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

    // SMTP Settings (Kết nối trực tiếp qua SMTP)
    private static String smtpHost = "";
    private static int smtpPort = 587;
    private static boolean smtpSsl = false;
    private static boolean smtpStartTls = true;
    private static String smtpUsername = "";
    private static String smtpPassword = "";
    private static String smtpFrom = "";

    // Giữ tương thích IMAP nếu cần
    private static String imapHost = "";
    private static int imapPort = 993;
    private static boolean imapSsl = true;
    private static String imapUsername = "";
    private static String imapPassword = "";
    private static String imapFolder = "INBOX";

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

                smtpHost = prop.getProperty("mail.smtp.host", "");
                smtpPort = Integer.parseInt(prop.getProperty("mail.smtp.port", "587"));
                smtpSsl = Boolean.parseBoolean(prop.getProperty("mail.smtp.ssl", "false"));
                smtpStartTls = Boolean.parseBoolean(prop.getProperty("mail.smtp.starttls", "true"));
                smtpUsername = prop.getProperty("mail.smtp.username", "");
                smtpPassword = prop.getProperty("mail.smtp.password", "");
                smtpFrom = prop.getProperty("mail.smtp.from", "");

                imapHost = prop.getProperty("mail.imap.host", "");
                imapPort = Integer.parseInt(prop.getProperty("mail.imap.port", "993"));
                imapSsl = Boolean.parseBoolean(prop.getProperty("mail.imap.ssl", "true"));
                imapUsername = prop.getProperty("mail.imap.username", "");
                imapPassword = prop.getProperty("mail.imap.password", "");
                imapFolder = prop.getProperty("mail.imap.folder", "INBOX");
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

    public static String getSmtpHost() { return smtpHost; }
    public static int getSmtpPort() { return smtpPort; }
    public static boolean isSmtpSsl() { return smtpSsl; }
    public static boolean isSmtpStartTls() { return smtpStartTls; }
    public static String getSmtpUsername() { return smtpUsername; }
    public static String getSmtpPassword() { return smtpPassword; }
    public static String getSmtpFrom() { return smtpFrom; }

    public static String getImapHost() { return imapHost; }
    public static int getImapPort() { return imapPort; }
    public static boolean isImapSsl() { return imapSsl; }
    public static String getImapUsername() { return imapUsername; }
    public static String getImapPassword() { return imapPassword; }
    public static String getImapFolder() { return imapFolder; }
}
