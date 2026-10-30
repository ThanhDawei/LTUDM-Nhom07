package com.quizzlive.client.service;

import com.quizzlive.client.network.SocketClient;
import com.quizzlive.shared.dto.AuthDTO;
import com.quizzlive.shared.protocol.OpCode;
import com.quizzlive.shared.protocol.Packet;
import com.quizzlive.shared.util.JsonUtil;

/**
 * Service phía Client xử lý Đăng ký, Đăng nhập, Xác thực OTP
 * Phụ trách Yêu cầu 1
 */
public class ClientAuthService {
    private final SocketClient socketClient = SocketClient.getInstance();

    /**
     * TODO: Dev Client gọi hàm này khi người dùng nhấn nút "Đăng Nhập"
     */
    public void sendLogin(String username, String password) {
        AuthDTO.LoginRequest req = new AuthDTO.LoginRequest();
        req.username = username;
        req.password = password;
        socketClient.sendPacket(new Packet(OpCode.LOGIN_REQ, JsonUtil.toJson(req)));
    }

    /**
     * TODO: Dev Client gọi hàm này khi người dùng nhấn nút "Đăng Ký"
     */
    public void sendRegister(String username, String password, String email, String fullName) {
        AuthDTO.RegisterRequest req = new AuthDTO.RegisterRequest();
        req.username = username;
        req.password = password;
        req.email = email;
        req.fullName = fullName;
        socketClient.sendPacket(new Packet(OpCode.REGISTER_REQ, JsonUtil.toJson(req)));
    }

    /**
     * TODO: Dev Client gọi hàm này khi người dùng nhập mã OTP để kích hoạt tài khoản
     */
    public void sendVerifyOtp(String email, String otpCode) {
        AuthDTO.VerifyOtpRequest req = new AuthDTO.VerifyOtpRequest();
        req.email = email;
        req.otpCode = otpCode;
        socketClient.sendPacket(new Packet(OpCode.VERIFY_OTP_REQ, JsonUtil.toJson(req)));
    }

    /**
     * TODO: Dev Client gọi hàm này khi người dùng cập nhật thông tin cá nhân (Yêu cầu 1)
     */
    public void sendUpdateProfile(String fullName, String currentPassword, String newPassword, String avatarUrl) {
        AuthDTO.UpdateProfileRequest req = new AuthDTO.UpdateProfileRequest();
        req.fullName = fullName;
        req.currentPassword = currentPassword;
        req.newPassword = newPassword;
        req.avatarUrl = avatarUrl;
        socketClient.sendPacket(new Packet(OpCode.UPDATE_PROFILE_REQ, JsonUtil.toJson(req)));
    }

    /**
     * TODO: Dev Client gọi hàm này để lấy danh sách người dùng đang online (Yêu cầu 3)
     */
    public void requestOnlineUsers() {
        socketClient.sendPacket(new Packet(OpCode.ONLINE_USERS_REQ, "{}"));
    }

    /**
     * TODO: Dev Client gọi hàm này khi đăng xuất
     */
    public void sendLogout() {
        socketClient.sendPacket(new Packet(OpCode.LOGOUT_REQ, "{}"));
    }

    /**
     * TODO: Dev Client gọi hàm này để lấy thông tin hồ sơ người dùng
     */
    public void requestProfile() {
        socketClient.sendPacket(new Packet(OpCode.GET_PROFILE_REQ, "{}"));
    }
}
