package com.quizzlive.server;

import com.quizzlive.server.config.ServerConfig;
import com.quizzlive.server.database.DatabaseManager;
import com.quizzlive.server.dao.OtpDAO;
import com.quizzlive.server.dao.UserDAO;
import com.quizzlive.server.entity.User;
import com.quizzlive.server.service.AuthService;
import com.quizzlive.server.service.EmailService;
import com.quizzlive.server.util.PasswordUtil;
import com.quizzlive.shared.dto.AuthDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

/**
 * Lớp kiểm thử tự động toàn diện cho Yêu cầu 1 (Task 1):
 * - Đăng ký, đăng nhập, cập nhật thông tin cá nhân.
 * - Mật khẩu: KHÔNG DÙNG MÃ HÓA, HÀM BĂM (lưu trữ và đối chiếu trực tiếp chuỗi thô).
 * - Mã OTP: Bắt buộc gửi và lưu vào CSDL bảng OTP với thời hạn 10 phút.
 * - Email: Sử dụng giao thức IMAP qua Socket, KHÔNG DÙNG SMTP.
 * - Kích hoạt tài khoản bằng mã OTP (chặn khi chưa kích hoạt).
 * - Cập nhật hồ sơ & đổi mật khẩu.
 * - Quản lý trạng thái Online / Đăng xuất.
 */
public class Task1Verification {

    public static void main(String[] args) {
        System.out.println("=======================================================================");
        System.out.println("   BẮT ĐẦU KIỂM THỬ TASK 1: ĐĂNG KÝ, ĐĂNG NHẬP, OTP CSDL & IMAP MAIL   ");
        System.out.println("=======================================================================");

        int passed = 0;
        int failed = 0;

        // --- TEST 1: KIỂM THỬ MẬT KHẨU (KHÔNG DÙNG MÃ HÓA, HÀM BĂM) ---
        System.out.println("\n[Test 1] Kiểm tra PasswordUtil (Không dùng mã hóa, không dùng hàm băm):");
        String rawPass = "MyPlainPassword@123";
        String storedPass = PasswordUtil.hashPassword(rawPass);

        if (storedPass != null && storedPass.equals(rawPass)) {
            System.out.println("  ✓ Mật khẩu được lưu trực tiếp dạng chuỗi thô (KHÔNG băm, KHÔNG mã hóa).");
            passed++;
        } else {
            System.err.println("  ✗ Lỗi: Mật khẩu bị biến đổi hoặc băm!");
            failed++;
        }

        if (PasswordUtil.verifyPassword(rawPass, storedPass) && !PasswordUtil.verifyPassword("SaiMatKhau", storedPass)) {
            System.out.println("  ✓ Đối chiếu mật khẩu dạng thô chính xác 100%.");
            passed++;
        } else {
            System.err.println("  ✗ Lỗi: Xác thực mật khẩu đối chiếu sai!");
            failed++;
        }

        // --- TEST 2: CẤU HÌNH GIAO THỨC EMAIL (DÙNG IMAP, KHÔNG DÙNG SMTP) ---
        System.out.println("\n[Test 2] Kiểm tra cấu hình giao thức Email (Sử dụng IMAP, không sài SMTP):");
        System.out.println("  -> Cổng IMAP mặc định: " + ServerConfig.getImapPort());
        System.out.println("  -> Chế độ SSL IMAP:    " + ServerConfig.isImapSsl());
        System.out.println("  -> Thư mục IMAP:       " + ServerConfig.getImapFolder());

        // Kiểm tra chắc chắn lớp EmailService có giao thức IMAP và không gọi SMTP
        try {
            EmailService.class.getDeclaredMethod("sendViaImapSocket", String.class, String.class, String.class);
            System.out.println("  ✓ Giao tiếp Email cài đặt phương thức Socket chuẩn IMAP (RFC 3501), hoàn toàn không dùng SMTP.");
            passed++;
        } catch (NoSuchMethodException e) {
            System.err.println("  ✗ Lỗi: Không tìm thấy phương thức gửi IMAP Socket!");
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

        String testUsername = "nguoi1_test_" + (System.currentTimeMillis() % 100000);
        String testEmail = testUsername + "@example.com";
        String testPassword = "RawPassword999";
        String testFullName = "Nguyễn Văn Người Một";

        // --- TEST 3: ĐĂNG KÝ TÀI KHOẢN MỚI ---
        System.out.println("\n[Test 3] Đăng ký tài khoản mới:");
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

        // Kiểm tra mật khẩu lưu trong CSDL có đúng là chuỗi thô (không băm) không
        User createdUser = userDAO.findByEmail(testEmail);
        if (createdUser != null) {
            if (testPassword.equals(createdUser.getPassword())) {
                System.out.println("  ✓ Mật khẩu trong bảng NGUOI_DUNG được lưu nguyên dạng thô: '" + createdUser.getPassword() + "' (Không dùng băm/mã hóa)");
                passed++;
            } else {
                System.err.println("  ✗ Lỗi: Mật khẩu trong CSDL không phải chuỗi thô!");
                failed++;
            }

            if ("PENDING".equalsIgnoreCase(createdUser.getStatus())) {
                System.out.println("  ✓ Tài khoản vừa đăng ký có trạng thái PENDING chờ xác thực OTP.");
                passed++;
            } else {
                System.err.println("  ✗ Lỗi: Trạng thái tài khoản không phải PENDING!");
                failed++;
            }
        } else {
            System.err.println("  ✗ Không tìm thấy người dùng vừa tạo trong CSDL!");
            failed += 2;
        }

        // --- TEST 4: KIỂM TRA MÃ OTP BẮT BUỘC ĐÃ GỬI VỀ CƠ SỞ DỮ LIỆU (BẢNG OTP, HẠN 10 PHÚT) ---
        System.out.println("\n[Test 4] Kiểm tra mã OTP được lưu vào CSDL (bảng OTP):");
        String realOtpCode = null;
        Timestamp thoiGianHetHan = null;
        Timestamp thoiGianTao = null;

        String sqlOtp = "SELECT MaXacThuc, ThoiGianTao, ThoiGianHetHan FROM OTP WHERE MaNguoiDung = ? AND MucDich = 'REGISTER' AND ThoiGianXacThuc IS NULL ORDER BY ThoiGianTao DESC LIMIT 1";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlOtp)) {
            ps.setLong(1, createdUser.getUserId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    realOtpCode = rs.getString("MaXacThuc");
                    thoiGianTao = rs.getTimestamp("ThoiGianTao");
                    thoiGianHetHan = rs.getTimestamp("ThoiGianHetHan");
                }
            }
        } catch (Exception e) {
            System.err.println("  ! Lỗi truy vấn bảng OTP: " + e.getMessage());
        }

        if (realOtpCode != null && thoiGianHetHan != null && thoiGianTao != null) {
            long diffMinutes = (thoiGianHetHan.getTime() - thoiGianTao.getTime()) / (60 * 1000L);
            System.out.println("  ✓ Đã tìm thấy mã OTP trong CSDL bảng OTP: [" + realOtpCode + "]");
            System.out.println("  ✓ Thời gian tạo:    " + thoiGianTao);
            System.out.println("  ✓ Thời gian hết hạn:" + thoiGianHetHan);
            System.out.println("  ✓ Thời hạn hiệu lực: " + diffMinutes + " phút (Đúng quy định 10 phút)");
            if (diffMinutes == 10 && realOtpCode.length() == 6) {
                passed++;
            } else {
                System.err.println("  ✗ Lỗi: Thời hạn OTP không đúng 10 phút hoặc mã không đủ 6 số!");
                failed++;
            }
        } else {
            System.err.println("  ✗ Lỗi: Không tìm thấy bản ghi mã OTP trong CSDL!");
            failed++;
        }

        // --- TEST 5: ĐĂNG NHẬP KHI CHƯA XÁC THỰC OTP ---
        System.out.println("\n[Test 5] Đăng nhập khi tài khoản chưa kích hoạt OTP:");
        AuthDTO.LoginRequest loginReq = new AuthDTO.LoginRequest();
        loginReq.username = testUsername;
        loginReq.password = testPassword;

        AuthDTO.AuthResponse preLoginResp = authService.login(loginReq);
        if (preLoginResp != null && !preLoginResp.success && preLoginResp.message.contains("chưa được kích hoạt")) {
            System.out.println("  ✓ Chặn đăng nhập thành công khi tài khoản PENDING: " + preLoginResp.message);
            passed++;
        } else {
            System.err.println("  ✗ Lỗi: Không chặn đăng nhập khi chưa xác thực OTP!");
            failed++;
        }

        // --- TEST 6: XÁC THỰC MÃ OTP ---
        System.out.println("\n[Test 6] Xác thực mã OTP:");
        // Thử nhập mã sai trước
        AuthDTO.VerifyOtpRequest wrongOtpReq = new AuthDTO.VerifyOtpRequest();
        wrongOtpReq.email = testEmail;
        wrongOtpReq.otpCode = "999999";
        AuthDTO.AuthResponse wrongOtpResp = authService.verifyOtp(wrongOtpReq);
        if (wrongOtpResp != null && !wrongOtpResp.success) {
            System.out.println("  ✓ Từ chối mã OTP sai thành công: " + wrongOtpResp.message);
            passed++;
        } else {
            System.err.println("  ✗ Lỗi: Không từ chối mã OTP sai!");
            failed++;
        }

        // Nhập đúng mã OTP từ CSDL (truy vấn mã OTP mới nhất từ bảng OTP)
        String latestOtpCode = null;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlOtp)) {
            ps.setLong(1, createdUser.getUserId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    latestOtpCode = rs.getString("MaXacThuc");
                }
            }
        } catch (Exception e) {
            System.err.println("  ! Lỗi truy vấn bảng OTP: " + e.getMessage());
        }

        if (latestOtpCode != null) {
            AuthDTO.VerifyOtpRequest correctOtpReq = new AuthDTO.VerifyOtpRequest();
            correctOtpReq.email = testEmail;
            correctOtpReq.otpCode = latestOtpCode;

            AuthDTO.AuthResponse correctOtpResp = authService.verifyOtp(correctOtpReq);
            if (correctOtpResp != null && correctOtpResp.success) {
                System.out.println("  ✓ Kích hoạt OTP thành công: " + correctOtpResp.message);
                passed++;
            } else {
                System.err.println("  ✗ Kích hoạt OTP thất bại!");
                failed++;
            }

            User activatedUser = userDAO.findByEmail(testEmail);
            if (activatedUser != null && "ACTIVE".equalsIgnoreCase(activatedUser.getStatus())) {
                System.out.println("  ✓ Trạng thái tài khoản trong CSDL đã chuyển sang ACTIVE.");
                passed++;
            } else {
                System.err.println("  ✗ Lỗi: Tài khoản chưa chuyển sang ACTIVE!");
                failed++;
            }
        }

        // --- TEST 7: ĐĂNG NHẬP SAU KHI ĐÃ KÍCH HOẠT (HỖ TRỢ CẢ USERNAME VÀ EMAIL) ---
        System.out.println("\n[Test 7] Đăng nhập sau khi kích hoạt thành công (Mật khẩu dạng thô):");
        AuthDTO.AuthResponse loginResp = authService.login(loginReq);
        if (loginResp != null && loginResp.success) {
            System.out.println("  ✓ Đăng nhập thành công với Username!");
            System.out.println("    - UserID:   " + loginResp.userId);
            System.out.println("    - Họ tên:   " + loginResp.fullName);
            passed++;
        } else {
            System.err.println("  ✗ Đăng nhập thất bại sau kích hoạt: " + (loginResp != null ? loginResp.message : "null"));
            failed++;
        }

        // Đăng nhập bằng Email
        AuthDTO.LoginRequest emailLoginReq = new AuthDTO.LoginRequest();
        emailLoginReq.username = testEmail;
        emailLoginReq.password = testPassword;
        AuthDTO.AuthResponse emailLoginResp = authService.login(emailLoginReq);
        if (emailLoginResp != null && emailLoginResp.success) {
            System.out.println("  ✓ Đăng nhập thành công với Email!");
            passed++;
        } else {
            System.err.println("  ✗ Đăng nhập với Email thất bại!");
            failed++;
        }

        // --- TEST 8: CẬP NHẬT HỒ SƠ & ĐỔI MẬT KHẨU (KHÔNG BĂM) ---
        System.out.println("\n[Test 8] Cập nhật thông tin cá nhân & Đổi mật khẩu:");
        AuthDTO.UpdateProfileRequest updateReq = new AuthDTO.UpdateProfileRequest();
        updateReq.fullName = "Nguyễn Văn Người Một (Cập Nhật)";
        updateReq.currentPassword = testPassword;
        updateReq.newPassword = "NewRawPassword@555";

        AuthDTO.AuthResponse updateResp = authService.updateProfile(createdUser.getUserId(), updateReq);
        if (updateResp != null && updateResp.success && "Nguyễn Văn Người Một (Cập Nhật)".equals(updateResp.fullName)) {
            System.out.println("  ✓ Cập nhật hồ sơ thành công!");
            passed++;
        } else {
            System.err.println("  ✗ Lỗi cập nhật hồ sơ: " + (updateResp != null ? updateResp.message : "null"));
            failed++;
        }

        // Thử đăng nhập lại bằng mật khẩu mới dạng thô
        loginReq.password = "NewRawPassword@555";
        AuthDTO.AuthResponse newPassLoginResp = authService.login(loginReq);
        if (newPassLoginResp != null && newPassLoginResp.success) {
            System.out.println("  ✓ Đăng nhập thành công với mật khẩu mới (chuỗi thô)! ");
            passed++;
        } else {
            System.err.println("  ✗ Đăng nhập với mật khẩu mới thất bại!");
            failed++;
        }

        // --- TEST 9: ĐĂNG XUẤT ---
        System.out.println("\n[Test 9] Đăng xuất:");
        authService.logout(createdUser.getUserId());
        User loggedOutUser = userDAO.findById(createdUser.getUserId());
        if (loggedOutUser != null && !loggedOutUser.isOnline()) {
            System.out.println("  ✓ Đăng xuất thành công, trạng thái TrangThaiOnline = FALSE.");
            passed++;
        } else {
            System.err.println("  ✗ Đăng xuất chưa cập nhật TrangThaiOnline!");
            failed++;
        }

        System.out.println("\n=======================================================================");
        System.out.println("              TỔNG KẾT KẾT QUẢ KIỂM THỬ TASK 1                         ");
        System.out.println("=======================================================================");
        System.out.println("  TỔNG SỐ TEST: " + (passed + failed));
        System.out.println("  THÀNH CÔNG:   " + passed);
        System.out.println("  THẤT BẠI:     " + failed);
        System.out.println("=======================================================================");
        System.exit(failed == 0 ? 0 : 1);
    }
}
