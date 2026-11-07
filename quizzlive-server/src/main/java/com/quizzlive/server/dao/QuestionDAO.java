package com.quizzlive.server.dao;

import com.quizzlive.server.database.DatabaseManager;
import com.quizzlive.server.entity.Answer;
import com.quizzlive.server.entity.Question;
import com.quizzlive.server.entity.Topic;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuestionDAO {

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
     * Tự động khởi tạo bảng LICHSU_TEST_IQ nếu chưa tồn tại
     */
    public void ensureIQTableExists() {
        String sql = "CREATE TABLE IF NOT EXISTS LICHSU_TEST_IQ (" +
                "MaNguoiDung BIGINT NOT NULL, " +
                "MaCauHoi BIGINT NOT NULL, " +
                "DaTangIQ BOOLEAN NOT NULL DEFAULT TRUE, " +
                "ThoiGianLam TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "PRIMARY KEY (MaNguoiDung, MaCauHoi))";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            try { stmt.executeUpdate("ALTER TABLE LICHSU_TEST_IQ ADD COLUMN IF NOT EXISTS DaTangIQ BOOLEAN DEFAULT TRUE"); } catch (Exception ignored) {}
            try { stmt.executeUpdate("ALTER TABLE LICHSU_TEST_IQ ADD COLUMN IF NOT EXISTS ThoiGianLam TIMESTAMP DEFAULT CURRENT_TIMESTAMP"); } catch (Exception ignored) {}
        } catch (SQLException ignored) {}
    }

    /**
     * Lấy ngẫu nhiên câu hỏi trực tiếp từ CSDL (bảng CAU_HOI) cho Task 11.
     * Trích xuất câu hỏi từ bảng CAU_HOI trong CSDL, loại trừ câu hỏi người dùng đã trả lời tăng điểm IQ.
     */
    public Question getRandomIQQuestion(Long userId) {
        ensureIQTableExists();
        List<Question> list = new ArrayList<>();
        // Trích xuất toàn bộ câu hỏi đang hoạt động từ bảng CAU_HOI trong CSDL
        String sql = "SELECT c.*, d.TenChuDe FROM CAU_HOI c " +
                "LEFT JOIN CHU_DE d ON c.MaChuDe = d.MaChuDe " +
                "WHERE c.TrangThai = 'ACTIVE' ";
        if (userId != null && userId > 0) {
            sql += "AND c.MaCauHoi NOT IN (SELECT MaCauHoi FROM LICHSU_TEST_IQ WHERE MaNguoiDung = ? AND DaTangIQ = TRUE) ";
        }
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (userId != null && userId > 0) {
                ps.setLong(1, userId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Question q = mapQuestion(rs);
                    q.setTopicName(rs.getString("TenChuDe"));
                    list.add(q);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Nếu người dùng đã giải đúng tất cả câu hỏi tăng IQ, lấy lại từ ngân hàng câu hỏi CSDL để tiếp tục làm bài
        if (list.isEmpty()) {
            String fallbackSql = "SELECT c.*, d.TenChuDe FROM CAU_HOI c " +
                    "LEFT JOIN CHU_DE d ON c.MaChuDe = d.MaChuDe " +
                    "WHERE c.TrangThai = 'ACTIVE'";
            try (Connection conn = DatabaseManager.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(fallbackSql)) {
                while (rs.next()) {
                    Question q = mapQuestion(rs);
                    q.setTopicName(rs.getString("TenChuDe"));
                    list.add(q);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if (list.isEmpty()) {
            return null;
        }

        // Ưu tiên câu hỏi có hình ảnh hoặc chủ đề tư duy IQ nếu có
        List<Question> iqPreferred = new ArrayList<>();
        for (Question q : list) {
            if ("IQ_IMAGE".equalsIgnoreCase(q.getQuestionType()) || q.getImage() != null || (q.getTopicId() != null && q.getTopicId() == 4)) {
                iqPreferred.add(q);
            }
        }
        Question selected;
        if (!iqPreferred.isEmpty() && Math.random() < 0.75) {
            int randomIndex = (int) (Math.random() * iqPreferred.size());
            selected = iqPreferred.get(randomIndex);
        } else {
            int randomIndex = (int) (Math.random() * list.size());
            selected = list.get(randomIndex);
        }

        // Nạp đáp án độc lập và an toàn cho riêng câu hỏi được chọn
        if (selected != null) {
            try (Connection conn = DatabaseManager.getConnection()) {
                selected.setAnswers(getAnswersForQuestion(conn, selected.getQuestionId()));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return selected;
    }

    public Question getRandomIQQuestion() {
        return getRandomIQQuestion(null);
    }

    /**
     * Kiểm tra xem người dùng đã trả lời đúng tăng điểm IQ câu hỏi này chưa (Yêu cầu 11)
     */
    public boolean hasUserPassedIQQuestion(long userId, long questionId) {
        ensureIQTableExists();
        String sql = "SELECT COUNT(*) FROM LICHSU_TEST_IQ WHERE MaNguoiDung = ? AND MaCauHoi = ? AND DaTangIQ = TRUE";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, questionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Lưu trạng thái người dùng đã trả lời tăng điểm IQ câu hỏi này (Yêu cầu 11)
     */
    public void recordPassedIQQuestion(long userId, long questionId) {
        ensureIQTableExists();
        String checkSql = "SELECT COUNT(*) FROM LICHSU_TEST_IQ WHERE MaNguoiDung = ? AND MaCauHoi = ?";
        String updateSql = "UPDATE LICHSU_TEST_IQ SET DaTangIQ = TRUE, ThoiGianLam = CURRENT_TIMESTAMP WHERE MaNguoiDung = ? AND MaCauHoi = ?";
        String insertSql = "INSERT INTO LICHSU_TEST_IQ (MaNguoiDung, MaCauHoi, DaTangIQ, ThoiGianLam) VALUES (?, ?, TRUE, CURRENT_TIMESTAMP)";
        try (Connection conn = DatabaseManager.getConnection()) {
            boolean exists = false;
            try (PreparedStatement psCheck = conn.prepareStatement(checkSql)) {
                psCheck.setLong(1, userId);
                psCheck.setLong(2, questionId);
                try (ResultSet rsCheck = psCheck.executeQuery()) {
                    if (rsCheck.next()) exists = rsCheck.getInt(1) > 0;
                }
            }
            if (exists) {
                try (PreparedStatement psUp = conn.prepareStatement(updateSql)) {
                    psUp.setLong(1, userId);
                    psUp.setLong(2, questionId);
                    psUp.executeUpdate();
                }
            } else {
                try (PreparedStatement psIn = conn.prepareStatement(insertSql)) {
                    psIn.setLong(1, userId);
                    psIn.setLong(2, questionId);
                    psIn.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
                ps.setString(5, q.getImage());
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
                    psA.setString(4, a.getImage());
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
