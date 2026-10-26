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

    public Question getRandomIQQuestion() {
        String sql = "SELECT * FROM CAU_HOI WHERE LoaiCauHoi = 'IQ_IMAGE' AND TrangThai = 'ACTIVE' ORDER BY RAND() LIMIT 1";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                Question q = mapQuestion(rs);
                q.setAnswers(getAnswersForQuestion(conn, q.getQuestionId()));
                return q;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
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
