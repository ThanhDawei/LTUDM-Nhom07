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
        String sql = "SELECT * FROM CHU_DE WHERE TrangThai = TRUE";
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

    public List<Question> getRandomQuestions(Integer topicId, String difficulty, int count) {
        List<Question> questions = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM CAU_HOI WHERE TrangThai = 'ACTIVE' AND LoaiCauHoi = 'STANDARD' ");
        if (topicId != null && topicId > 0) {
            sql.append("AND MaChuDe = ").append(topicId).append(" ");
        }
        if (difficulty != null && !difficulty.equalsIgnoreCase("ALL")) {
            sql.append("AND DoKho = '").append(difficulty).append("' ");
        }
        sql.append("ORDER BY RAND() LIMIT ?");

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setInt(1, count);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Question q = mapQuestion(rs);
                    q.setAnswers(getAnswersForQuestion(conn, q.getQuestionId()));
                    questions.add(q);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return questions;
    }

    /**
     * Lấy ngẫu nhiên 1 câu hỏi IQ hình ảnh (Yêu cầu 11).
     * Tương thích 100% với cả MySQL, PostgreSQL và H2 mà không gặp lỗi hàm RAND()/RANDOM().
     */
    public Question getRandomIQQuestion() {
        List<Question> list = new ArrayList<>();
        String sql = "SELECT * FROM CAU_HOI WHERE LoaiCauHoi = 'IQ_IMAGE' AND TrangThai = 'ACTIVE'";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Question q = mapQuestion(rs);
                q.setAnswers(getAnswersForQuestion(conn, q.getQuestionId()));
                list.add(q);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (list.isEmpty()) {
            // Câu hỏi dự phòng mẫu nếu CSDL chưa nạp câu hỏi IQ
            Question fallback = new Question();
            fallback.setQuestionId(999L);
            fallback.setTopicId(4);
            fallback.setQuestionType("IQ_IMAGE");
            fallback.setDifficulty("MEDIUM");
            fallback.setContent("Quan sát quy luật xoay của kim và chọn hình còn thiếu ở vị trí dấu (?):");
            fallback.setImage("iq_pattern_01.png");
            fallback.setExplanation("Hình xoay theo chiều kim đồng hồ góc 45 độ ở mỗi bước.");

            List<Answer> answers = new ArrayList<>();
            Answer a1 = new Answer(); a1.setAnswerId(901L); a1.setQuestionId(999L); a1.setKeySymbol("A"); a1.setContent("Hình số 1 (Góc 0 độ)"); a1.setImage("iq_ans_01_a.png"); a1.setCorrect(false);
            Answer a2 = new Answer(); a2.setAnswerId(902L); a2.setQuestionId(999L); a2.setKeySymbol("B"); a2.setContent("Hình số 2 (Góc 45 độ)"); a2.setImage("iq_ans_01_b.png"); a2.setCorrect(true);
            Answer a3 = new Answer(); a3.setAnswerId(903L); a3.setQuestionId(999L); a3.setKeySymbol("C"); a3.setContent("Hình số 3 (Góc 90 độ)"); a3.setImage("iq_ans_01_c.png"); a3.setCorrect(false);
            Answer a4 = new Answer(); a4.setAnswerId(904L); a4.setQuestionId(999L); a4.setKeySymbol("D"); a4.setContent("Hình số 4 (Góc 135 độ)"); a4.setImage("iq_ans_01_d.png"); a4.setCorrect(false);
            answers.add(a1); answers.add(a2); answers.add(a3); answers.add(a4);
            fallback.setAnswers(answers);
            return fallback;
        }

        int randomIndex = (int) (Math.random() * list.size());
        return list.get(randomIndex);
    }

    /**
     * Kiểm tra đáp án người chơi chọn có chính xác hay không (Yêu cầu 11)
     */
    public boolean checkAnswer(Long questionId, Long answerId) {
        if (questionId == null || answerId == null) return false;
        if (questionId == 999L) return answerId == 902L;

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
        if (questionId == null || questionId == 999L) return "MEDIUM";
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
