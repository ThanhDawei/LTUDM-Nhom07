package com.quizzlive.server.dao;

import com.quizzlive.server.database.DatabaseManager;
import com.quizzlive.server.entity.MatchConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MatchConfigDAO {

    public MatchConfig getActiveConfig() {
        String sql = "SELECT * FROM CAU_HINH_TRAN WHERE TrangThai = TRUE ORDER BY MaCauHinh ASC LIMIT 1";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return mapConfig(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new MatchConfig(); // return default
    }

    public List<MatchConfig> getAllConfigs() {
        List<MatchConfig> list = new ArrayList<>();
        String sql = "SELECT * FROM CAU_HINH_TRAN ORDER BY MaCauHinh ASC";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapConfig(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void updateConfig(MatchConfig c) {
        String sql = "UPDATE CAU_HINH_TRAN SET TenCauHinh = ?, SoCauHoi = ?, ThoiGianMoiCau = ?, DiemCoBan = ?, ThuongTheoThoiGian = ? WHERE MaCauHinh = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getConfigName());
            ps.setInt(2, c.getQuestionCount());
            ps.setInt(3, c.getTimePerQuestion());
            ps.setInt(4, c.getBaseScore());
            ps.setBoolean(5, c.isSpeedBonus());
            ps.setInt(6, c.getConfigId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private MatchConfig mapConfig(ResultSet rs) throws SQLException {
        MatchConfig c = new MatchConfig();
        c.setConfigId(rs.getInt("MaCauHinh"));
        c.setConfigName(rs.getString("TenCauHinh"));
        c.setQuestionCount(rs.getInt("SoCauHoi"));
        c.setTimePerQuestion(rs.getInt("ThoiGianMoiCau"));
        c.setBaseScore(rs.getInt("DiemCoBan"));
        c.setSpeedBonus(rs.getBoolean("ThuongTheoThoiGian"));
        c.setWrongPenalty(rs.getInt("DiemSai"));
        c.setNoAnswerPenalty(rs.getInt("DiemKhongTraLoi"));
        c.setActive(rs.getBoolean("TrangThai"));
        return c;
    }
}
