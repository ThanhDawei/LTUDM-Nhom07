package com.quizzlive.server.dao;

import com.quizzlive.server.database.DatabaseManager;
import com.quizzlive.server.entity.User;
import com.quizzlive.server.entity.UserProfile;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public User findByUsername(String username) {
        String sql = "SELECT * FROM NGUOI_DUNG WHERE TenDangNhap = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public User findByEmail(String email) {
        String sql = "SELECT * FROM NGUOI_DUNG WHERE Email = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public User findById(Long userId) {
        String sql = "SELECT * FROM NGUOI_DUNG WHERE MaNguoiDung = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Long createUser(String username, String password, String email, String fullName) {
        String sqlUser = "INSERT INTO NGUOI_DUNG (TenDangNhap, MatKhau, Email, TrangThai, TrangThaiOnline, NgayTao) VALUES (?, ?, ?, 'PENDING', FALSE, NOW())";
        String sqlProfile = "INSERT INTO HO_SO_NGUOI_DUNG (MaNguoiDung, HoTen) VALUES (?, ?)";
        String sqlStats = "INSERT INTO THONG_KE_NGUOI_CHOI (MaNguoiDung) VALUES (?)";

        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);

            long userId = 0;
            try (PreparedStatement ps = conn.prepareStatement(sqlUser, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, username);
                ps.setString(2, password);
                ps.setString(3, email);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        userId = rs.getLong(1);
                    }
                }
            }

            try (PreparedStatement psProfile = conn.prepareStatement(sqlProfile)) {
                psProfile.setLong(1, userId);
                psProfile.setString(2, fullName);
                psProfile.executeUpdate();
            }

            try (PreparedStatement psStats = conn.prepareStatement(sqlStats)) {
                psStats.setLong(1, userId);
                psStats.executeUpdate();
            }

            conn.commit();
            return userId;
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

    public void activateUser(Long userId) {
        String sql = "UPDATE NGUOI_DUNG SET TrangThai = 'ACTIVE' WHERE MaNguoiDung = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateOnlineStatus(Long userId, boolean isOnline) {
        String sql = "UPDATE NGUOI_DUNG SET TrangThaiOnline = ?, LanDangNhapCuoi = NOW() WHERE MaNguoiDung = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, isOnline);
            ps.setLong(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void blockUser(Long userId, boolean block) {
        String sql = "UPDATE NGUOI_DUNG SET TrangThai = ? WHERE MaNguoiDung = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, block ? "BLOCKED" : "ACTIVE");
            ps.setLong(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public UserProfile getUserProfile(Long userId) {
        String sql = "SELECT * FROM HO_SO_NGUOI_DUNG WHERE MaNguoiDung = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    UserProfile profile = new UserProfile();
                    profile.setUserId(rs.getLong("MaNguoiDung"));
                    profile.setFullName(rs.getString("HoTen"));
                    profile.setAvatar(rs.getString("AnhDaiDien"));
                    profile.setBio(rs.getString("GioiThieu"));
                    profile.setIqScore(rs.getDouble("DiemIQ"));
                    profile.setTotalMatches(rs.getInt("TongSoTran"));
                    profile.setWinMatches(rs.getInt("TongSoTranThang"));
                    profile.setLoseMatches(rs.getInt("TongSoTranThua"));
                    profile.setCurrentStreak(rs.getInt("ChuoiThangHienTai"));
                    profile.setMaxStreak(rs.getInt("ChuoiThangDaiNhat"));
                    profile.setTotalPoints(rs.getLong("TongDiem"));
                    return profile;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateIQScore(Long userId, double newIqScore) {
        String sql = "UPDATE HO_SO_NGUOI_DUNG SET DiemIQ = ? WHERE MaNguoiDung = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, newIqScore);
            ps.setLong(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateMatchResult(Long userId, boolean won, boolean isDraw, int scoreEarned) {
        String sqlProfile = "UPDATE HO_SO_NGUOI_DUNG SET " +
                "TongSoTran = TongSoTran + 1, " +
                "TongSoTranThang = TongSoTranThang + ?, " +
                "TongSoTranThua = TongSoTranThua + ?, " +
                "ChuoiThangHienTai = CASE WHEN ? = 1 THEN ChuoiThangHienTai + 1 ELSE 0 END, " +
                "ChuoiThangDaiNhat = GREATEST(ChuoiThangDaiNhat, CASE WHEN ? = 1 THEN ChuoiThangHienTai + 1 ELSE ChuoiThangDaiNhat END), " +
                "TongDiem = TongDiem + ? " +
                "WHERE MaNguoiDung = ?";

        int winInc = won ? 1 : 0;
        int loseInc = (!won && !isDraw) ? 1 : 0;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlProfile)) {
            ps.setInt(1, winInc);
            ps.setInt(2, loseInc);
            ps.setInt(3, winInc);
            ps.setInt(4, winInc);
            ps.setInt(5, scoreEarned);
            ps.setLong(6, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM NGUOI_DUNG ORDER BY NgayTao DESC";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapUser(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private User mapUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUserId(rs.getLong("MaNguoiDung"));
        u.setUsername(rs.getString("TenDangNhap"));
        u.setPassword(rs.getString("MatKhau"));
        u.setEmail(rs.getString("Email"));
        u.setStatus(rs.getString("TrangThai"));
        u.setOnline(rs.getBoolean("TrangThaiOnline"));
        u.setLastLogin(rs.getTimestamp("LanDangNhapCuoi"));
        u.setCreatedAt(rs.getTimestamp("NgayTao"));
        return u;
    }
}
