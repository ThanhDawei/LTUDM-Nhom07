package com.quizzlive.server.service;

import com.quizzlive.server.dao.OtpDAO;
import com.quizzlive.server.dao.UserDAO;
import com.quizzlive.server.entity.User;
import com.quizzlive.server.entity.UserProfile;
import com.quizzlive.server.util.PasswordUtil;
import com.quizzlive.shared.dto.AuthDTO;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Service xử lý Xác thực & Quản lý Tài khoản người dùng
 * Phụ trách Yêu cầu 1: Đăng ký, đăng nhập, cập nhật thông tin cá nhân, xác thực email bằng OTP (thời hạn 10 phút)
 * Đáp ứng Tiêu chuẩn A3: Mật khẩu lưu ở dạng băm có salt (PBKDF2).
 */
public class AuthService {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");
    private final UserDAO userDAO = new UserDAO();
    private final OtpDAO otpDAO = new OtpDAO();

    /**
     * Xử lý Đăng ký tài khoản (Yêu cầu 1):
     * 1. Kiểm tra tính hợp lệ dữ liệu (username, password, email, họ tên)
     * 2. Kiểm tra trùng lặp username, email
     * 3. Băm mật khẩu có Salt (PBKDF2)
     * 4. Tạo tài khoản với trạng thái PENDING
     * 5. Sinh mã OTP 6 số, lưu CSDL với thời hạn 10 phút
     * 6. Gửi OTP qua Email / Server Console
     */
    public AuthDTO.AuthResponse register(AuthDTO.RegisterRequest req) {
        AuthDTO.AuthResponse resp = new AuthDTO.AuthResponse();

        if (req == null) {
            resp.success = false;
            resp.message = "Dữ liệu đăng ký không hợp lệ!";
            return resp;
        }

        String username = req.username != null ? req.username.trim() : "";
        String password = req.password != null ? req.password.trim() : "";
        String email = req.email != null ? req.email.trim().toLowerCase() : "";
        String fullName = req.fullName != null ? req.fullName.trim() : "";

        // Kiểm tra ràng buộc
        if (username.length() < 3) {
            resp.success = false;
            resp.message = "Tên đăng nhập phải có tối thiểu 3 ký tự!";
            return resp;
        }
        if (password.length() < 6) {
            resp.success = false;
            resp.message = "Mật khẩu phải có tối thiểu 6 ký tự!";
            return resp;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            resp.success = false;
            resp.message = "Địa chỉ email không đúng định dạng!";
            return resp;
        }
        if (fullName.isEmpty()) {
            resp.success = false;
            resp.message = "Họ và tên không được để trống!";
            return resp;
        }

        // Kiểm tra tên đăng nhập đã tồn tại chưa
        User existingUser = userDAO.findByUsername(username);
        if (existingUser != null) {
            resp.success = false;
            resp.message = "Tên đăng nhập '" + username + "' đã được sử dụng!";
            return resp;
        }

        // Kiểm tra email đã đăng ký chưa
        User existingEmail = userDAO.findByEmail(email);
        if (existingEmail != null) {
            if ("ACTIVE".equalsIgnoreCase(existingEmail.getStatus())) {
                resp.success = false;
                resp.message = "Địa chỉ email '" + email + "' đã được sử dụng bởi tài khoản khác!";
                return resp;
            } else if ("BLOCKED".equalsIgnoreCase(existingEmail.getStatus())) {
                resp.success = false;
                resp.message = "Tài khoản gắn với email này đã bị khóa!";
                return resp;
            } else {
                // Tài khoản PENDING: Cập nhật lại mật khẩu và gửi mã OTP mới
                String passHash = PasswordUtil.hashPassword(password);
                userDAO.updatePendingUser(existingEmail.getUserId(), passHash, fullName);

                String otpCode = EmailService.generateOtp();
                otpDAO.saveOtp(existingEmail.getUserId(), otpCode, "REGISTER", 10);
                EmailService.sendOtpEmail(email, otpCode, "Kích hoạt tài khoản");

                resp.success = true;
                resp.userId = existingEmail.getUserId();
                resp.username = existingEmail.getUsername();
                resp.message = "Mã OTP mới đã được gửi tới email '" + email + "' (hiệu lực 10 phút). Vui lòng nhập để kích hoạt!";
                return resp;
            }
        }

        // Băm mật khẩu có Salt (PBKDF2)
        String passwordHash = PasswordUtil.hashPassword(password);

        // Tạo tài khoản trạng thái PENDING
        Long userId = userDAO.createUser(username, passwordHash, email, fullName);
        if (userId == null) {
            resp.success = false;
            resp.message = "Đăng ký thất bại do lỗi hệ thống CSDL!";
            return resp;
        }

        // Sinh mã OTP 6 số và lưu vào CSDL với hạn 10 phút
        String otpCode = EmailService.generateOtp();
        otpDAO.saveOtp(userId, otpCode, "REGISTER", 10);

        // Gửi email OTP (hoặc log ra Server Console)
        EmailService.sendOtpEmail(email, otpCode, "Kích hoạt tài khoản");

        resp.success = true;
        resp.userId = userId;
        resp.username = username;
        resp.message = "Đăng ký thành công! Mã OTP đã gửi tới email '" + email + "' (hiệu lực 10 phút).";
        return resp;
    }

    /**
     * Xác thực mã OTP kích hoạt tài khoản (Yêu cầu 1)
     */
    public AuthDTO.AuthResponse verifyOtp(AuthDTO.VerifyOtpRequest req) {
        AuthDTO.AuthResponse resp = new AuthDTO.AuthResponse();

        if (req == null || req.email == null || req.otpCode == null) {
            resp.success = false;
            resp.message = "Thông tin xác thực OTP không đầy đủ!";
            return resp;
        }

        String email = req.email.trim().toLowerCase();
        String otpCode = req.otpCode.trim();

        User user = userDAO.findByEmail(email);
        if (user == null) {
            resp.success = false;
            resp.message = "Không tìm thấy tài khoản gắn với email '" + email + "'!";
            return resp;
        }

        if ("ACTIVE".equalsIgnoreCase(user.getStatus())) {
            resp.success = true;
            resp.userId = user.getUserId();
            resp.username = user.getUsername();
            resp.message = "Tài khoản đã được kích hoạt trước đó. Bạn có thể đăng nhập ngay!";
            return resp;
        }

        // Kiểm tra mã OTP trong CSDL (hạn 10 phút)
        boolean valid = otpDAO.verifyOtp(user.getUserId(), otpCode, "REGISTER");
        if (!valid) {
            resp.success = false;
            resp.message = "Mã OTP không chính xác hoặc đã hết hạn (quá 10 phút)!";
            return resp;
        }

        // Kích hoạt tài khoản thành ACTIVE
        userDAO.activateUser(user.getUserId());

        resp.success = true;
        resp.userId = user.getUserId();
        resp.username = user.getUsername();
        resp.message = "Kích hoạt tài khoản thành công! Bạn có thể đăng nhập ngay.";
        return resp;
    }

    /**
     * Xử lý Đăng nhập (Yêu cầu 1):
     * 1. Hỗ trợ đăng nhập bằng tên tài khoản hoặc email
     * 2. Kiểm tra mật khẩu (đối chiếu hash PBKDF2 có salt)
     * 3. Kiểm tra tài khoản có bị khóa không (Yêu cầu 15)
     * 4. Kiểm tra tài khoản đã kích hoạt OTP chưa
     * 5. Cập nhật trạng thái Online
     */
    public AuthDTO.AuthResponse login(AuthDTO.LoginRequest req) {
        AuthDTO.AuthResponse resp = new AuthDTO.AuthResponse();

        if (req == null) {
            resp.success = false;
            resp.message = "Dữ liệu đăng nhập trống!";
            return resp;
        }

        String usernameOrEmail = req.username != null ? req.username.trim() : "";
        String password = req.password != null ? req.password : "";

        if (usernameOrEmail.isEmpty() || password.isEmpty()) {
            resp.success = false;
            resp.message = "Vui lòng nhập tên đăng nhập và mật khẩu!";
            return resp;
        }

        // Tìm tài khoản theo username hoặc email
        User user = userDAO.findByUsername(usernameOrEmail);
        if (user == null) {
            user = userDAO.findByEmail(usernameOrEmail.toLowerCase());
        }

        if (user == null) {
            resp.success = false;
            resp.message = "Tên đăng nhập hoặc mật khẩu không chính xác!";
            return resp;
        }

        // Kiểm tra mật khẩu băm có salt
        boolean passwordOk = PasswordUtil.verifyPassword(password, user.getPassword());
        if (!passwordOk) {
            resp.success = false;
            resp.message = "Tên đăng nhập hoặc mật khẩu không chính xác!";
            return resp;
        }

        // Kiểm tra tài khoản bị khóa (Yêu cầu 15)
        if ("BLOCKED".equalsIgnoreCase(user.getStatus())) {
            resp.success = false;
            resp.message = "Tài khoản của bạn đã bị khóa bởi Quản trị viên!";
            return resp;
        }

        // Kiểm tra tài khoản chưa xác thực OTP
        if ("PENDING".equalsIgnoreCase(user.getStatus())) {
            // Tự động phát sinh và gửi lại mã OTP mới để người dùng kích hoạt
            String newOtp = EmailService.generateOtp();
            otpDAO.saveOtp(user.getUserId(), newOtp, "REGISTER", 10);
            EmailService.sendOtpEmail(user.getEmail(), newOtp, "Kích hoạt tài khoản");

            resp.success = false;
            resp.message = "Tài khoản chưa được kích hoạt qua email! Chúng tôi đã gửi lại mã OTP mới tới: " + user.getEmail();
            return resp;
        }

        // Tự động nâng cấp mật khẩu sang hash PBKDF2 nếu tài khoản dùng seed data cũ
        if (!user.getPassword().startsWith("PBKDF2$")) {
            userDAO.updatePassword(user.getUserId(), PasswordUtil.hashPassword(password));
        }

        // Cập nhật trạng thái Online
        userDAO.updateOnlineStatus(user.getUserId(), true);

        // Lấy thông tin hồ sơ
        UserProfile profile = userDAO.getUserProfile(user.getUserId());

        resp.success = true;
        resp.userId = user.getUserId();
        resp.username = user.getUsername();
        resp.fullName = profile != null && profile.getFullName() != null ? profile.getFullName() : user.getUsername();
        resp.iqScore = profile != null ? profile.getIqScore() : 0.0;
        resp.message = "Đăng nhập thành công!";
        return resp;
    }

    /**
     * Cập nhật thông tin cá nhân / Đổi mật khẩu (Yêu cầu 1)
     */
    public AuthDTO.AuthResponse updateProfile(Long userId, AuthDTO.UpdateProfileRequest req) {
        AuthDTO.AuthResponse resp = new AuthDTO.AuthResponse();

        if (userId == null) {
            resp.success = false;
            resp.message = "Bạn chưa đăng nhập!";
            return resp;
        }

        User user = userDAO.findById(userId);
        if (user == null) {
            resp.success = false;
            resp.message = "Không tìm thấy người dùng!";
            return resp;
        }

        // Nếu người dùng muốn đổi mật khẩu
        if (req.newPassword != null && !req.newPassword.trim().isEmpty()) {
            if (req.currentPassword == null || !PasswordUtil.verifyPassword(req.currentPassword, user.getPassword())) {
                resp.success = false;
                resp.message = "Mật khẩu hiện tại không chính xác!";
                return resp;
            }
            if (req.newPassword.trim().length() < 6) {
                resp.success = false;
                resp.message = "Mật khẩu mới phải có tối thiểu 6 ký tự!";
                return resp;
            }
            userDAO.updatePassword(userId, PasswordUtil.hashPassword(req.newPassword.trim()));
        }

        // Cập nhật họ tên và ảnh đại diện
        if (req.fullName != null && !req.fullName.trim().isEmpty()) {
            userDAO.updateProfile(userId, req.fullName.trim(), req.avatarUrl);
        }

        UserProfile profile = userDAO.getUserProfile(userId);
        resp.success = true;
        resp.userId = userId;
        resp.username = user.getUsername();
        resp.fullName = profile != null ? profile.getFullName() : user.getUsername();
        resp.iqScore = profile != null ? profile.getIqScore() : 0.0;
        resp.message = "Cập nhật hồ sơ thành công!";
        return resp;
    }

    /**
     * Lấy danh sách người dùng đang online (Hỗ trợ Yêu cầu 3 - Thách đấu)
     */
    public List<AuthDTO.AuthResponse> getOnlineUsers(Long excludeUserId) {
        return userDAO.getOnlineUsers(excludeUserId);
    }

    /**
     * Xử lý Đăng xuất
     */
    public void logout(Long userId) {
        if (userId != null) {
            userDAO.updateOnlineStatus(userId, false);
        }
    }
}
