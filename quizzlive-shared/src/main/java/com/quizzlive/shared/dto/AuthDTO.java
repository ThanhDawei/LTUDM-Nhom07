package com.quizzlive.shared.dto;

import java.io.Serializable;

public class AuthDTO {
    public static class RegisterRequest implements Serializable {
        public String username;
        public String password;
        public String email;
        public String fullName;
    }

    public static class VerifyOtpRequest implements Serializable {
        public String email;
        public String otpCode;
    }

    public static class LoginRequest implements Serializable {
        public String username;
        public String password;
    }

    public static class UpdateProfileRequest implements Serializable {
        public String fullName;
        public String currentPassword;
        public String newPassword;
        public String avatarUrl;
    }

    public static class AuthResponse implements Serializable {
        public boolean success;
        public String message;
        public Long userId;
        public String username;
        public String fullName;
        public Double iqScore;
    }
}
