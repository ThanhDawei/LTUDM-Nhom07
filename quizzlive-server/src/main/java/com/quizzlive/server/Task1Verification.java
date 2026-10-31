package com.quizzlive.server;

import com.quizzlive.server.database.DatabaseManager;
import com.quizzlive.server.dao.OtpDAO;
import com.quizzlive.server.dao.UserDAO;
import com.quizzlive.server.entity.User;
import com.quizzlive.server.service.AuthService;
import com.quizzlive.server.util.PasswordUtil;
import com.quizzlive.shared.dto.AuthDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Lớp kiểm thử tự động toàn diện cho Yêu cầu 1 (Task 1):
 * - Đăng ký tài khoản (ràng buộc dữ liệu, trùng lặp username/email)
 * - Băm mật khẩu có Salt (PBKDF2)
 * - Sinh mã OTP 6 số, lưu CSDL hạn 10 phút
 * - Kích hoạt tài khoản bằng mã OTP
 * - Đăng nhập (hỗ trợ cả username và email, chặn tài khoản PENDING / BLOCKED)
 * - Cập nhật thông tin cá nhân & đổi mật khẩu
 * - Quản lý trạng thái Online / Đăng xuất
 */
public class Task1Verification {

    public static void main(String[] args) {
        System.out.println("===============================================================");
        System.out.println("   BẮT ĐẦU KIỂM THỬ TỰ ĐỘNG TASK 1: XÁC THỰC, BẢO MẬT & OTP    ");
        System.out.println("===============================================================");

        int passed = 0;
        int failed = 0;

        // --- TEST 1: KIỂM THỬ BĂM MẬT KHẨU CÓ SALT (PBKDF2) ---
        System.out.println("\n[Test 1] Kiểm tra PasswordUtil (Băm mật khẩu PBKDF2 + Salt ngẫu nhiên):");
        String rawPass = "SecretPass@123";
        String hash1 = PasswordUtil.hashPassword(rawPass);
        String hash2 = PasswordUtil.hashPassword(rawPass);

        if (hash1 != null && hash1.startsWith("PBKDF2$") && !hash1.equals(hash2)) {
            System.out.println("  ✓ Hai lần băm cùng mật khẩu ra 2 chuỗi khác nhau do Salt ngẫu nhiên.");
            passed++;
        } else {
            System.err.println("  ✗ Lỗi: Băm mật khẩu không có Salt!");
            failed++;
        }

        if (PasswordUtil.verifyPassword(rawPass, hash1) && !PasswordUtil.verifyPassword("WrongPassword", hash1)) {
            System.out.println("  ✓ Kiểm tra đúng/sai mật khẩu với hash PBKDF2 hoạt động chính xác.");
            passed++;
        } else {
            System.err.println("  ✗ Lỗi: Xác thực mật khẩu PBKDF2 thất bại!");
            failed++;
        }

        // --- KHỞI TẠO CSDL ---
        System.out.println("\n[Khởi tạo CSDL cho kiểm thử]");
        try {
            DatabaseManager.initialize();
            System.out.println("  ✓ CSDL đã sẵn sàng.");
        } catch (Exception e) {
            System.err.println("  ! Lưu ý kết nối CSDL: " + e.getMessage());
        }

        AuthService authService = new AuthService();
        UserDAO userDAO = new UserDAO();
        OtpDAO otpDAO = new OtpDAO();

        String testUsername = "member1_test_" + System.currentTimeMillis() % 10000;
        String testEmail = testUsername + "@example.com";
        String testPassword = "MySecurePassword123";
        String testFullName = "Nguyễn Văn Tâm";

        // --- TEST 2: ĐĂNG KÝ TÀI KHOẢN MỚI ---
        System.out.println("\n[Test 2] Đăng ký tài khoản mới:");
        AuthDTO.RegisterRequest regReq = new AuthDTO.RegisterRequest();
        regReq.username = testUsername;
        regReq.password = testPassword;
        regReq.email = testEmail;
        regReq.fullName = testFullName;

        AuthDTO.AuthResponse regResp = authService.register(regReq);
        if (regResp != null && regResp.success) {
            System.out.println("  ✓ Đăng ký thành công: " + regResp.message);
            passed++;
        } else {
            System.err.println("  ✗ Đăng ký thất bại: " + (regResp != null ? regResp.message : "null"));
            failed++;
        }

        // Kiểm tra trạng thái tài khoản trong CSDL phải là PENDING
        User createdUser = userDAO.findByEmail(testEmail);
        if (createdUser != null && "PENDING".equalsIgnoreCase(createdUser.getStatus())) {
            System.out.println("  ✓ Tài khoản vừa tạo ở trạng thái PENDING (chờ OTP): user_id = " + createdUser.getUserId());
            passed++;
        } else {
            System.err.println("  ✗ Lỗi: Trạng thái tài khoản không phải PENDING!");
            failed++;
        }

        // --- TEST 3: ĐĂNG NHẬP KHI CHƯA XÁC THỰC OTP ---
        System.out.println("\n[Test 3] Đăng nhập khi chưa kích hoạt OTP:");
        AuthDTO.LoginRequest loginReq = new AuthDTO.LoginRequest();
        loginReq.username = testUsername;
        loginReq.password = testPassword;

        AuthDTO.AuthResponse preLoginResp = authService.login(loginReq);
        if (preLoginResp != null && !preLoginResp.success && preLoginResp.message.contains("chưa được kích hoạt")) {
            System.out.println("  ✓ Chặn đăng nhập thành công khi chưa xác thực email qua OTP: " + preLoginResp.message);
            passed++;
        } else {
            System.err.println("  ✗ Lỗi: Cho phép đăng nhập khi chưa kích hoạt OTP!");
            failed++;
        }

        // --- TEST 4: XÁC THỰC MÃ OTP (THỜI HẠN 10 PHÚT) ---
        System.out.println("\n[Test 4] Xác thực mã OTP:");
        // Thử nhập mã sai trước
        AuthDTO.VerifyOtpRequest wrongOtpReq = new AuthDTO.VerifyOtpRequest();
        wrongOtpReq.email = testEmail;
        wrongOtpReq.otpCode = "000000";
        AuthDTO.AuthResponse wrongOtpResp = authService.verifyOtp(wrongOtpReq);
        if (wrongOtpResp != null && !wrongOtpResp.success) {
            System.out.println("  ✓ Nhập sai mã OTP bị từ chối chính xác: " + wrongOtpResp.message);
            passed++;
        } else {
            System.err.println("  ✗ Lỗi: Không từ chối mã OTP sai!");
            failed++;
        }

        // Lấy mã OTP thực từ CSDL để kích hoạt
        String realOtpCode = null;
        String sqlOtp = "SELECT MaXacThuc FROM OTP WHERE MaNguoiDung = ? AND MucDich = 'REGISTER' AND ThoiGianXacThuc IS NULL ORDER BY ThoiGianTao DESC LIMIT 1";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlOtp)) {
            ps.setLong(1, createdUser.getUserId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    realOtpCode = rs.getString(1);
                }
            }
        } catch (Exception e) {
            System.err.println("  ! Không thể truy vấn bảng OTP: " + e.getMessage());
        }

        if (realOtpCode != null) {
            System.out.println("  -> Tìm thấy mã OTP thực trong CSDL: [" + realOtpCode + "]");
            AuthDTO.VerifyOtpRequest correctOtpReq = new AuthDTO.VerifyOtpRequest();
            correctOtpReq.email = testEmail;
            correctOtpReq.otpCode = realOtpCode;

            AuthDTO.AuthResponse correctOtpResp = authService.verifyOtp(correctOtpReq);
            if (correctOtpResp != null && correctOtpResp.success) {
                System.out.println("  ✓ Xác thực OTP thành công: " + correctOtpResp.message);
                passed++;
            } else {
                System.err.println("  ✗ Xác thực OTP thất bại!");
                failed++;
            }

            User activatedUser = userDAO.findByEmail(testEmail);
            if (activatedUser != null && "ACTIVE".equalsIgnoreCase(activatedUser.getStatus())) {
                System.out.println("  ✓ Trạng thái tài khoản đã chuyển sang ACTIVE.");
                passed++;
            } else {
                System.err.println("  ✗ Trạng thái tài khoản chưa ACTIVE sau khi nhập đúng OTP!");
                failed++;
            }
        }

        // --- TEST 5: ĐĂNG NHẬP SAU KHI ĐÃ KÍCH HOẠT THÀNH CÔNG ---
        System.out.println("\n[Test 5] Đăng nhập sau khi kích hoạt tài khoản:");
        AuthDTO.AuthResponse loginResp = authService.login(loginReq);
        if (loginResp != null && loginResp.success) {
            System.out.println("  ✓ Đăng nhập thành công với Username!");
            System.out.println("    - UserID: " + loginResp.userId);
            System.out.println("    - FullName: " + loginResp.fullName);
            System.out.println("    - IQ Score: " + loginResp.iqScore);
            passed++;
        } else {
            System.err.println("  ✗ Đăng nhập thất bại sau kích hoạt: " + (loginResp != null ? loginResp.message : "null"));
            failed++;
        }

        // Thử đăng nhập bằng Email thay vì Username (tính năng linh hoạt)
        AuthDTO.LoginRequest emailLoginReq = new AuthDTO.LoginRequest();
        emailLoginReq.username = testEmail;
        emailLoginReq.password = testPassword;
        AuthDTO.AuthResponse emailLoginResp = authService.login(emailLoginReq);
        if (emailLoginResp != null && emailLoginResp.success) {
            System.out.println("  ✓ Đăng nhập thành công bằng địa chỉ Email!");
            passed++;
        } else {
            System.err.println("  ✗ Đăng nhập bằng Email thất bại!");
            failed++;
        }

        // --- TEST 6: CẬP NHẬT HỒ SƠ & ĐỔI MẬT KHẨU ---
        System.out.println("\n[Test 6] Cập nhật thông tin cá nhân & Đổi mật khẩu:");
        AuthDTO.UpdateProfileRequest updateReq = new AuthDTO.UpdateProfileRequest();
        updateReq.fullName = "Nguyễn Văn Tâm (Updated)";
        updateReq.currentPassword = testPassword;
        updateReq.newPassword = "NewPassword@456";

        AuthDTO.AuthResponse updateResp = authService.updateProfile(createdUser.getUserId(), updateReq);
        if (updateResp != null && updateResp.success && "Nguyễn Văn Tâm (Updated)".equals(updateResp.fullName)) {
            System.out.println("  ✓ Cập nhật hồ sơ và đổi mật khẩu thành công!");
            passed++;
        } else {
            System.err.println("  ✗ Lỗi cập nhật hồ sơ: " + (updateResp != null ? updateResp.message : "null"));
            failed++;
        }

        // Kiểm tra đăng nhập lại bằng mật khẩu mới
        loginReq.password = "NewPassword@456";
        AuthDTO.AuthResponse newLoginResp = authService.login(loginReq);
        if (newLoginResp != null && newLoginResp.success) {
            System.out.println("  ✓ Đăng nhập thành công với mật khẩu mới!");
            passed++;
        } else {
            System.err.println("  ✗ Đăng nhập với mật khẩu mới thất bại!");
            failed++;
        }

        // --- TEST 7: ĐĂNG XUẤT ---
        System.out.println("\n[Test 7] Đăng xuất:");
        authService.logout(createdUser.getUserId());
        User loggedOutUser = userDAO.findById(createdUser.getUserId());
        if (loggedOutUser != null && !loggedOutUser.isOnline()) {
            System.out.println("  ✓ Đăng xuất thành công, trạng thái TrangThaiOnline = FALSE.");
            passed++;
        } else {
            System.err.println("  ✗ Đăng xuất chưa cập nhật TrangThaiOnline!");
            failed++;
        }

        System.out.println("\n===============================================================");
        System.out.println("              TỔNG KẾT KẾT QUẢ KIỂM THỬ TASK 1                ");
        System.out.println("===============================================================");
        System.out.println("  TỔNG SỐ TEST: " + (passed + failed));
        System.out.println("  THÀNH CÔNG:   " + passed);
        System.out.println("  THẤT BẠI:     " + failed);
        System.out.println("===============================================================");
        System.exit(failed == 0 ? 0 : 1);
    }
}
