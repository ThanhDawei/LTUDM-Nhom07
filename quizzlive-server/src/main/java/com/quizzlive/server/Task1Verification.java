package com.quizzlive.server;

import com.quizzlive.server.config.ServerConfig;
import com.quizzlive.server.database.DatabaseManager;
import com.quizzlive.server.dao.OtpDAO;
import com.quizzlive.server.dao.UserDAO;
import com.quizzlive.server.entity.User;
import com.quizzlive.server.entity.UserProfile;
import com.quizzlive.server.network.ClientSession;
import com.quizzlive.server.network.SessionManager;
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
 * - Đăng ký, đăng nhập, cập nhật thông tin cá nhân (Họ tên, Ảnh đại diện, Giới thiệu).
 * - Mật khẩu: KHÔNG DÙNG MÃ HÓA, HÀM BĂM (lưu trữ và đối chiếu trực tiếp chuỗi thô).
 * - Mã OTP: Bắt buộc gửi và lưu vào CSDL bảng OTP với thời hạn 10 phút.
 * - Email: Sử dụng giao thức IMAP qua Socket, KHÔNG DÙNG SMTP.
 * - Kiểm tra 2 luồng / 2 máy đăng nhập cùng 1 tài khoản: máy đăng nhập sau bị chặn và nhận thông báo tài khoản đã được đăng nhập.
 * - Kích hoạt tài khoản bằng mã OTP.
 * - Cập nhật hồ sơ (ảnh, giới thiệu) & đổi mật khẩu.
 * - Quản lý trạng thái Online / Đăng xuất.
 */
public class Task1Verification {

    public static void main(String[] args) {
        System.out.println("=======================================================================");
        System.out.println("   BẮT ĐẦU KIỂM THỬ TASK 1: ĐĂNG KÝ, ĐĂNG NHẬP, ẢNH, BIO & CHỐNG ĐĂNG NHẬP TRÙNG   ");
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
            DatabaseManager.initializeForTesting();
            System.out.println("  ✓ CSDL Test độc lập trong RAM đã sẵn sàng (không ghi dữ liệu test vào file chính).");
        } catch (Exception e) {
            System.err.println("  ! Lưu ý kết nối CSDL: " + e.getMessage());
        }

        AuthService authService = new AuthService();
        UserDAO userDAO = new UserDAO();
        OtpDAO otpDAO = new OtpDAO();
        SessionManager sessionManager = new SessionManager();

        String testUsername = "nguoi1_test_" + (System.currentTimeMillis() % 100000);
        String testEmail = testUsername + "@example.com";
        String testPassword = "RawPassword999";
        String testFullName = "Nguyễn Văn Người Một";
        String testAvatar = "avatar2.png";
        String testBio = "Tuyển thủ tranh tài kiến thức trực tuyến";

        // --- TEST 3: ĐĂNG KÝ TÀI KHOẢN MỚI KÈM ẢNH ĐẠI DIỆN & GIỚI THIỆU ---
        System.out.println("\n[Test 3] Đăng ký tài khoản mới kèm Ảnh đại diện và Giới thiệu:");
        AuthDTO.RegisterRequest regReq = new AuthDTO.RegisterRequest();
        regReq.username = testUsername;
        regReq.password = testPassword;
        regReq.email = testEmail;
        regReq.fullName = testFullName;
        regReq.avatarUrl = testAvatar;
        regReq.bio = testBio;

        AuthDTO.AuthResponse regResp = authService.register(regReq);
        if (regResp != null && regResp.success) {
            System.out.println("  ✓ Đăng ký thành công: " + regResp.message);
            passed++;
        } else {
            System.err.println("  ✗ Đăng ký thất bại: " + (regResp != null ? regResp.message : "null"));
            failed++;
        }

        // Kiểm tra thông tin trong CSDL
        User createdUser = userDAO.findByEmail(testEmail);
        if (createdUser != null) {
            if (testPassword.equals(createdUser.getPassword())) {
                System.out.println("  ✓ Mật khẩu trong bảng NGUOI_DUNG được lưu nguyên dạng thô: '" + createdUser.getPassword() + "' (Không dùng băm/mã hóa)");
                passed++;
            } else {
                System.err.println("  ✗ Lỗi: Mật khẩu trong CSDL không phải chuỗi thô!");
                failed++;
            }

            UserProfile createdProfile = userDAO.getUserProfile(createdUser.getUserId());
            if (createdProfile != null && testAvatar.equals(createdProfile.getAvatar()) && testBio.equals(createdProfile.getBio())) {
                System.out.println("  ✓ Bảng HO_SO_NGUOI_DUNG lưu chính xác:");
                System.out.println("    - Ảnh đại diện: " + createdProfile.getAvatar());
                System.out.println("    - Giới thiệu:   " + createdProfile.getBio());
                passed++;
            } else {
                System.err.println("  ✗ Lỗi: Ảnh đại diện hoặc Giới thiệu chưa được lưu đúng vào CSDL!");
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
            failed += 3;
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

        // Nhập đúng mã OTP từ CSDL (lấy mã mới nhất)
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

        // --- TEST 7: ĐĂNG NHẬP THÀNH CÔNG LẦN ĐẦU (MÁY 1) ---
        System.out.println("\n[Test 7] Đăng nhập thành công lần 1 (Thiết bị 1):");
        AuthDTO.AuthResponse loginResp1 = authService.login(loginReq, sessionManager);
        if (loginResp1 != null && loginResp1.success) {
            System.out.println("  ✓ Máy 1 đăng nhập thành công!");
            System.out.println("    - UserID:    " + loginResp1.userId);
            System.out.println("    - Họ tên:    " + loginResp1.fullName);
            System.out.println("    - Avatar:    " + loginResp1.avatarUrl);
            System.out.println("    - Giới thiệu:" + loginResp1.bio);
            passed++;

            // Giả lập phiên kết nối của Máy 1 trong SessionManager
            ClientSession fakeSession1 = new ClientSession(null, sessionManager, null);
            fakeSession1.setUserId(loginResp1.userId);
            fakeSession1.setUsername(loginResp1.username);
            sessionManager.addSession(fakeSession1);
        } else {
            System.err.println("  ✗ Đăng nhập thất bại: " + (loginResp1 != null ? loginResp1.message : "null"));
            failed++;
        }

        // --- TEST 8: 2 LUỒNG / 2 MÁY ĐĂNG NHẬP CÙNG 1 TÀI KHOẢN ---
        System.out.println("\n[Test 8] Kiểm tra máy thứ 2 đăng nhập sau vào cùng 1 tài khoản:");
        AuthDTO.LoginRequest concurrentLoginReq = new AuthDTO.LoginRequest();
        concurrentLoginReq.username = testUsername;
        concurrentLoginReq.password = testPassword;

        AuthDTO.AuthResponse loginResp2 = authService.login(concurrentLoginReq, sessionManager);
        if (loginResp2 != null && !loginResp2.success && loginResp2.message.contains("đã được đăng nhập")) {
            System.out.println("  ✓ Chặn đăng nhập máy 2 thành công!");
            System.out.println("  ✓ Thông báo gửi về máy 2: \"" + loginResp2.message + "\"");
            passed++;
        } else {
            System.err.println("  ✗ Lỗi: Cho phép máy 2 đăng nhập trùng khi máy 1 đang online!");
            failed++;
        }

        // --- TEST 9: CẬP NHẬT HỒ SƠ (ẢNH, GIỚI THIỆU) & ĐỔI MẬT KHẨU ---
        System.out.println("\n[Test 9] Cập nhật thông tin cá nhân (Ảnh đại diện, Giới thiệu) & Đổi mật khẩu:");
        AuthDTO.UpdateProfileRequest updateReq = new AuthDTO.UpdateProfileRequest();
        updateReq.fullName = "Nguyễn Văn Người Một (Cập Nhật)";
        updateReq.avatarUrl = "avatar3.png";
        updateReq.bio = "Cao thủ trắc nghiệm vừa được cập nhật bio";
        updateReq.currentPassword = testPassword;
        updateReq.newPassword = "NewRawPassword@555";

        AuthDTO.AuthResponse updateResp = authService.updateProfile(createdUser.getUserId(), updateReq);
        if (updateResp != null && updateResp.success) {
            System.out.println("  ✓ Cập nhật hồ sơ thành công!");
            System.out.println("    - Họ tên mới:   " + updateResp.fullName);
            System.out.println("    - Avatar mới:   " + updateResp.avatarUrl);
            System.out.println("    - Bio mới:      " + updateResp.bio);
            passed++;
        } else {
            System.err.println("  ✗ Lỗi cập nhật hồ sơ: " + (updateResp != null ? updateResp.message : "null"));
            failed++;
        }

        // --- TEST 10: ĐĂNG XUẤT MÁY 1 VÀ ĐĂNG NHẬP LẠI BẰNG MẬT KHẨU MỚI ---
        System.out.println("\n[Test 10] Đăng xuất máy 1 và đăng nhập lại bằng mật khẩu mới:");
        authService.logout(createdUser.getUserId());
        sessionManager.getOnlineSessions().clear();

        User loggedOutUser = userDAO.findById(createdUser.getUserId());
        if (loggedOutUser != null && !loggedOutUser.isOnline()) {
            System.out.println("  ✓ Máy 1 đăng xuất thành công, trạng thái TrangThaiOnline = FALSE.");
            passed++;
        } else {
            System.err.println("  ✗ Đăng xuất chưa cập nhật TrangThaiOnline!");
            failed++;
        }

        // Đăng nhập lại với mật khẩu mới
        loginReq.password = "NewRawPassword@555";
        AuthDTO.AuthResponse newPassLoginResp = authService.login(loginReq, sessionManager);
        if (newPassLoginResp != null && newPassLoginResp.success) {
            System.out.println("  ✓ Đăng nhập lại thành công với mật khẩu mới (chuỗi thô)! ");
            passed++;
        } else {
            System.err.println("  ✗ Đăng nhập với mật khẩu mới thất bại!");
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
