package com.quizzlive.server.dao;

import com.quizzlive.server.config.ServerConfig;
import com.quizzlive.server.database.DatabaseManager;
import com.quizzlive.server.entity.Answer;
import com.quizzlive.server.entity.Question;
import com.quizzlive.server.entity.Topic;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class QuestionDAO {

    private String getRandomFunction() {
        return (ServerConfig.getDbUrl() != null && ServerConfig.getDbUrl().contains("postgresql")) ? "RANDOM()" : "RAND()";
    }

    public List<Topic> getAllTopics() {
        List<Topic> topics = new ArrayList<>();
        String sql = "SELECT * FROM CHU_DE WHERE TrangThai = TRUE ORDER BY MaChuDe ASC";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Topic t = new Topic(
                        rs.getInt("MaChuDe"),
                        rs.getString("TenChuDe"),
                        rs.getString("MoTa"),
                        rs.getBoolean("TrangThai")
                );
                topics.add(t);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return topics;
    }

    public String getTopicNameById(Integer topicId) {
        if (topicId == null || topicId == 0) return "Tất cả chủ đề";
        String sql = "SELECT TenChuDe FROM CHU_DE WHERE MaChuDe = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, topicId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("TenChuDe");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Chủ đề #" + topicId;
    }

    /**
     * Lấy danh sách câu hỏi ngẫu nhiên theo Chủ đề và Độ khó (Yêu cầu 4, 7).
     * Có cơ chế bù đắp thông minh: nếu số câu hỏi khớp chính xác chưa đủ count,
     * tự động bổ sung câu hỏi cùng chủ đề/cùng độ khó mà không làm gián đoạn trận đấu.
     */
    public List<Question> getRandomQuestions(Integer topicId, String difficulty, int count) {
        List<Question> questions = new ArrayList<>();
java.util.Set<Long> chosenIds = new java.util.HashSet<>();

        try (Connection conn = DatabaseManager.getConnection()) {
            // Lần 1: Lấy câu hỏi thỏa mãn đúng cả topicId và difficulty
            fetchQuestions(conn, topicId, difficulty, count, chosenIds, questions);

            // Lần 2 (nếu chưa đủ và có chọn topicId): Bổ sung thêm câu hỏi cùng chủ đề (khác độ khó)
            if (questions.size() < count && topicId != null && topicId > 0) {
                fetchQuestions(conn, topicId, null, count - questions.size(), chosenIds, questions);
            }

            // Lần 3 (nếu vẫn chưa đủ và có chọn difficulty): Bổ sung câu hỏi cùng độ khó (khác chủ đề)
            if (questions.size() < count && difficulty != null && !difficulty.equalsIgnoreCase("ALL")) {
                fetchQuestions(conn, null, difficulty, count - questions.size(), chosenIds, questions);
            }

            // Lần 4 (dự phòng cuối): Lấy câu hỏi trắc nghiệm bất kỳ còn lại trong ngân hàng
            if (questions.size() < count) {
                fetchQuestions(conn, null, null, count - questions.size(), chosenIds, questions);
            }

            // Nạp đáp án cho từng câu hỏi
            for (Question q : questions) {
                q.setAnswers(getAnswersForQuestion(conn, q.getQuestionId()));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return questions;
    }

    private void fetchQuestions(Connection conn, Integer topicId, String difficulty, int limit,
                                java.util.Set<Long> chosenIds, List<Question> resultList) throws SQLException {
        if (limit <= 0) return;
        StringBuilder sql = new StringBuilder(
                "SELECT c.*, d.TenChuDe FROM CAU_HOI c " +
                "LEFT JOIN CHU_DE d ON c.MaChuDe = d.MaChuDe " +
                "WHERE c.TrangThai = 'ACTIVE' AND c.LoaiCauHoi = 'STANDARD' "
        );
        if (topicId != null && topicId > 0) {
            sql.append("AND c.MaChuDe = ").append(topicId).append(" ");
        }
        if (difficulty != null && !difficulty.equalsIgnoreCase("ALL")) {
            sql.append("AND c.DoKho = '").append(difficulty.toUpperCase()).append("' ");
        }
        if (!chosenIds.isEmpty()) {
            sql.append("AND c.MaCauHoi NOT IN (");
            int i = 0;
            for (Long id : chosenIds) {
                if (i++ > 0) sql.append(",");
                sql.append(id);
            }
            sql.append(") ");
        }
        boolean isPg = false;
        try {
            isPg = conn.getMetaData().getDatabaseProductName().toLowerCase().contains("postgresql");
        } catch (Exception ignored) {}
        sql.append(isPg ? "ORDER BY RANDOM() LIMIT ?" : "ORDER BY RAND() LIMIT ?");

        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Question q = mapQuestion(rs);
                    q.setTopicName(rs.getString("TenChuDe"));
                    chosenIds.add(q.getQuestionId());
                    resultList.add(q);
                }
            }
        }
    }

    /**
     * Tự động khởi tạo bảng LICHSU_TEST_IQ và KET_TOAN_IQ_NGAY nếu chưa tồn tại
     */
    public void ensureIQTableExists() {
        String sqlLichSu = "CREATE TABLE IF NOT EXISTS LICHSU_TEST_IQ (" +
                "MaLichSu BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "MaNguoiDung BIGINT NOT NULL, " +
                "MaCauHoi BIGINT NOT NULL, " +
                "LaDung BOOLEAN NOT NULL DEFAULT FALSE, " +
                "NgayLam DATE NOT NULL, " +
                "ThoiGianLam TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";

        String sqlKetToan = "CREATE TABLE IF NOT EXISTS KET_TOAN_IQ_NGAY (" +
                "MaNguoiDung BIGINT NOT NULL, " +
                "NgayKetToan DATE NOT NULL, " +
                "SoCauDung INT NOT NULL DEFAULT 0, " +
                "SoCauSai INT NOT NULL DEFAULT 0, " +
                "DiemIQNhan DECIMAL(5,2) NOT NULL DEFAULT 0.00, " +
                "ThoiGian TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "PRIMARY KEY (MaNguoiDung, NgayKetToan))";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sqlLichSu);
            stmt.executeUpdate(sqlKetToan);
            try { stmt.executeUpdate("ALTER TABLE LICHSU_TEST_IQ ADD COLUMN IF NOT EXISTS LaDung BOOLEAN DEFAULT FALSE"); } catch (Exception ignored) {}
            try { stmt.executeUpdate("ALTER TABLE LICHSU_TEST_IQ ADD COLUMN IF NOT EXISTS NgayLam DATE DEFAULT CURRENT_DATE"); } catch (Exception ignored) {}
            try { stmt.executeUpdate("ALTER TABLE LICHSU_TEST_IQ ADD COLUMN IF NOT EXISTS ThoiGianLam TIMESTAMP DEFAULT CURRENT_TIMESTAMP"); } catch (Exception ignored) {}
            try { stmt.executeUpdate("UPDATE CAU_HOI SET LoaiCauHoi = 'IQ_TEST' WHERE UPPER(TRIM(LoaiCauHoi)) = 'IQ_IMAGE'"); } catch (Exception ignored) {}
        } catch (SQLException ignored) {}
    }

    /**
     * Kiểm tra người dùng đã kết toán điểm IQ hôm nay chưa
     */
    public boolean isDailySettled(long userId, LocalDate date) {
        ensureIQTableExists();
        String sql = "SELECT COUNT(*) FROM KET_TOAN_IQ_NGAY WHERE MaNguoiDung = ? AND NgayKetToan = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setDate(2, java.sql.Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Đếm số câu hỏi IQ người chơi đã trả lời trong ngày hôm nay
     */
    public int countTodayQuestions(long userId, LocalDate date) {
        ensureIQTableExists();
        String sql = "SELECT COUNT(*) FROM LICHSU_TEST_IQ WHERE MaNguoiDung = ? AND NgayLam = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setDate(2, java.sql.Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Đếm số câu hỏi IQ người chơi trả lời ĐÚNG trong ngày hôm nay
     */
    public int countTodayCorrectQuestions(long userId, LocalDate date) {
        ensureIQTableExists();
        String sql = "SELECT COUNT(*) FROM LICHSU_TEST_IQ WHERE MaNguoiDung = ? AND NgayLam = ? AND LaDung = TRUE";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setDate(2, java.sql.Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Lưu kết quả trả lời từng câu IQ trong ngày
     */
    public void recordIQAnswer(long userId, long questionId, boolean isCorrect, LocalDate date) {
        ensureIQTableExists();
        String sql = "INSERT INTO LICHSU_TEST_IQ (MaNguoiDung, MaCauHoi, LaDung, NgayLam, ThoiGianLam) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, questionId);
            ps.setBoolean(3, isCorrect);
            ps.setDate(4, java.sql.Date.valueOf(date));
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Lưu kết toán điểm IQ trong ngày (5 câu/ngày)
     */
    public void saveDailySettlement(long userId, LocalDate date, int correct, int wrong, double deltaIq) {
        ensureIQTableExists();
        String sql = "INSERT INTO KET_TOAN_IQ_NGAY (MaNguoiDung, NgayKetToan, SoCauDung, SoCauSai, DiemIQNhan, ThoiGian) " +
                "VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP) " +
                "ON DUPLICATE KEY UPDATE SoCauDung = VALUES(SoCauDung), SoCauSai = VALUES(SoCauSai), DiemIQNhan = VALUES(DiemIQNhan)";
        if (ServerConfig.getDbUrl() != null && ServerConfig.getDbUrl().contains("postgresql")) {
            sql = "INSERT INTO KET_TOAN_IQ_NGAY (MaNguoiDung, NgayKetToan, SoCauDung, SoCauSai, DiemIQNhan, ThoiGian) " +
                    "VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP) " +
                    "ON CONFLICT (MaNguoiDung, NgayKetToan) DO UPDATE SET SoCauDung = EXCLUDED.SoCauDung, SoCauSai = EXCLUDED.SoCauSai, DiemIQNhan = EXCLUDED.DiemIQNhan";
        }
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setDate(2, java.sql.Date.valueOf(date));
            ps.setInt(3, correct);
            ps.setInt(4, wrong);
            ps.setDouble(5, deltaIq);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Lấy ngẫu nhiên câu hỏi trực tiếp từ CSDL (bảng CAU_HOI) với loại là IQ_TEST.
     * Loại trừ các câu hỏi người dùng đã trả lời trong ngày hôm nay.
     */
    public Question getRandomIQQuestion(Long userId, LocalDate date) {
        ensureIQTableExists();
        Question selected = null;
        String randFunc = getRandomFunction();

        // Ưu tiên: Lấy câu hỏi IQ_TEST chưa làm hôm nay (CHỈ trích xuất câu hỏi có LoaiCauHoi = 'IQ_TEST')
        StringBuilder sql = new StringBuilder("SELECT c.*, d.TenChuDe FROM CAU_HOI c " +
                "LEFT JOIN CHU_DE d ON c.MaChuDe = d.MaChuDe " +
                "WHERE c.TrangThai = 'ACTIVE' AND UPPER(TRIM(c.LoaiCauHoi)) = 'IQ_TEST' ");
        List<Object> params = new ArrayList<>();

        if (userId != null && userId > 0 && date != null) {
            sql.append("AND c.MaCauHoi NOT IN (SELECT MaCauHoi FROM LICHSU_TEST_IQ WHERE MaNguoiDung = ? AND NgayLam = ?) ");
            params.add(userId);
            params.add(java.sql.Date.valueOf(date));
        }

        sql.append("ORDER BY ").append(randFunc).append(" LIMIT 1");

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                Object p = params.get(i);
                if (p instanceof Long) ps.setLong(i + 1, (Long) p);
                else if (p instanceof java.sql.Date) ps.setDate(i + 1, (java.sql.Date) p);
                else ps.setString(i + 1, p.toString());
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    selected = mapQuestion(rs);
                    selected.setTopicName(rs.getString("TenChuDe"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Nếu đã làm hết tất cả các câu hỏi IQ_TEST hôm nay, vẫn CHỈ trích xuất câu hỏi có LoaiCauHoi = 'IQ_TEST'
        if (selected == null) {
            String fallbackSql = "SELECT c.*, d.TenChuDe FROM CAU_HOI c " +
                    "LEFT JOIN CHU_DE d ON c.MaChuDe = d.MaChuDe " +
                    "WHERE c.TrangThai = 'ACTIVE' AND UPPER(TRIM(c.LoaiCauHoi)) = 'IQ_TEST' ORDER BY " + randFunc + " LIMIT 1";
            try (Connection conn = DatabaseManager.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(fallbackSql)) {
                if (rs.next()) {
                    selected = mapQuestion(rs);
                    selected.setTopicName(rs.getString("TenChuDe"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // Nạp 4 đáp án A, B, C, D cho câu hỏi được chọn
        if (selected != null) {
            try (Connection conn = DatabaseManager.getConnection()) {
                selected.setAnswers(getAnswersForQuestion(conn, selected.getQuestionId()));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return selected;
    }

    public Question getRandomIQQuestion(Long userId) {
        return getRandomIQQuestion(userId, LocalDate.now());
    }

    public Question getRandomIQQuestion() {
        return getRandomIQQuestion(null, LocalDate.now());
    }

    /**
     * Kiểm tra đáp án người chơi chọn có chính xác hay không (Yêu cầu 11)
     */
    public boolean checkAnswer(Long questionId, Long answerId) {
        if (questionId == null || answerId == null) return false;

        String sql = "SELECT LaDapAnDung FROM DAP_AN WHERE MaCauHoi = ? AND MaDapAn = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, questionId);
            ps.setLong(2, answerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("LaDapAnDung");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Lấy độ khó câu hỏi để cộng điểm IQ hợp lý
     */
    public String getQuestionDifficulty(Long questionId) {
        if (questionId == null) return "MEDIUM";
        String sql = "SELECT DoKho FROM CAU_HOI WHERE MaCauHoi = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, questionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("DoKho");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "MEDIUM";
    }

    public List<Question> getAllQuestionsForAdmin(Integer topicId, String difficulty) {
        List<Question> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT c.*, d.TenChuDe FROM CAU_HOI c LEFT JOIN CHU_DE d ON c.MaChuDe = d.MaChuDe WHERE 1=1 "
        );
        if (topicId != null && topicId > 0) {
            sql.append("AND c.MaChuDe = ").append(topicId).append(" ");
        }
        if (difficulty != null && !difficulty.equalsIgnoreCase("ALL")) {
            sql.append("AND c.DoKho = '").append(difficulty).append("' ");
        }
        sql.append("ORDER BY c.MaCauHoi DESC");

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql.toString())) {
            while (rs.next()) {
                Question q = mapQuestion(rs);
                q.setTopicName(rs.getString("TenChuDe"));
                q.setAnswers(getAnswersForQuestion(conn, q.getQuestionId()));
                list.add(q);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Long saveQuestionWithAnswers(Question q) {
        String sqlQ = "INSERT INTO CAU_HOI (MaChuDe, LoaiCauHoi, DoKho, NoiDung, HinhAnh, GiaiThich, TrangThai, NgayTao) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, NOW())";
        String sqlAns = "INSERT INTO DAP_AN (MaCauHoi, KyHieu, NoiDung, HinhAnh, LaDapAnDung) VALUES (?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);

            long questionId = 0;
            try (PreparedStatement ps = conn.prepareStatement(sqlQ, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, q.getTopicId() != null ? q.getTopicId() : 1);
                ps.setString(2, q.getQuestionType() != null ? q.getQuestionType() : "STANDARD");
                ps.setString(3, q.getDifficulty() != null ? q.getDifficulty() : "MEDIUM");
                ps.setString(4, q.getContent());
                String qImageB64 = (q.getImage() != null && !q.getImage().trim().isEmpty()) ? com.quizzlive.shared.util.ImageBase64Util.toBase64(q.getImage().trim()) : null;
                ps.setString(5, qImageB64);
                ps.setString(6, q.getExplanation());
                ps.setString(7, q.getStatus() != null ? q.getStatus() : "ACTIVE");
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        questionId = rs.getLong(1);
                    }
                }
            }

            for (Answer a : q.getAnswers()) {
                try (PreparedStatement psA = conn.prepareStatement(sqlAns)) {
                    psA.setLong(1, questionId);
                    psA.setString(2, a.getKeySymbol());
                    psA.setString(3, a.getContent());
                    String aImageB64 = (a.getImage() != null && !a.getImage().trim().isEmpty()) ? com.quizzlive.shared.util.ImageBase64Util.toBase64(a.getImage().trim()) : null;
                    psA.setString(4, aImageB64);
                    psA.setBoolean(5, a.isCorrect());
                    psA.executeUpdate();
                }
            }

            conn.commit();
            return questionId;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
        return null;
    }

    public void deleteQuestion(Long questionId) {
        String sql = "DELETE FROM CAU_HOI WHERE MaCauHoi = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, questionId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private List<Answer> getAnswersForQuestion(Connection conn, Long questionId) throws SQLException {
        List<Answer> answers = new ArrayList<>();
        String sql = "SELECT * FROM DAP_AN WHERE MaCauHoi = ? ORDER BY KyHieu ASC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, questionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Answer a = new Answer();
                    a.setAnswerId(rs.getLong("MaDapAn"));
                    a.setQuestionId(rs.getLong("MaCauHoi"));
                    a.setKeySymbol(rs.getString("KyHieu"));
                    a.setContent(rs.getString("NoiDung"));
                    a.setImage(rs.getString("HinhAnh"));
                    a.setCorrect(rs.getBoolean("LaDapAnDung"));
                    answers.add(a);
                }
            }
        }
        return answers;
    }

    private Question mapQuestion(ResultSet rs) throws SQLException {
        Question q = new Question();
        q.setQuestionId(rs.getLong("MaCauHoi"));
        q.setTopicId(rs.getInt("MaChuDe"));
        q.setQuestionType(rs.getString("LoaiCauHoi"));
        q.setDifficulty(rs.getString("DoKho"));
        q.setContent(rs.getString("NoiDung"));
        q.setImage(rs.getString("HinhAnh"));
        q.setExplanation(rs.getString("GiaiThich"));
        q.setStatus(rs.getString("TrangThai"));
        return q;
    }
}
