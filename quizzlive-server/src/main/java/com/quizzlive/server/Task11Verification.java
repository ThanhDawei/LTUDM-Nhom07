package com.quizzlive.server;

import com.quizzlive.server.dao.QuestionDAO;
import com.quizzlive.server.dao.UserDAO;
import com.quizzlive.server.database.DatabaseManager;
import com.quizzlive.server.entity.Answer;
import com.quizzlive.server.entity.Question;
import com.quizzlive.server.entity.UserProfile;
import com.quizzlive.server.network.ClientSession;
import com.quizzlive.server.network.SessionManager;
import com.quizzlive.server.service.AuthService;
import com.quizzlive.server.service.IQTestService;
import com.quizzlive.shared.dto.AuthDTO;
import com.quizzlive.shared.dto.BattleDTO;
import com.quizzlive.shared.dto.IQTestDTO;
import com.quizzlive.shared.protocol.OpCode;
import com.quizzlive.shared.protocol.Packet;
import com.quizzlive.shared.util.JsonUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Kiểm thử tự động toàn diện cho Yêu cầu 11 (Task 11):
 * - Lấy câu hỏi IQ hình ảnh (LoaiCauHoi = 'IQ_IMAGE')
 * - Truyền dữ liệu trực tiếp dạng JSON (không dùng băm và mã hóa)
 * - Chấm điểm đáp án IQ
 * - Cập nhật điểm IQ vào hồ sơ người dùng (HO_SO_NGUOI_DUNG.DiemIQ)
 */
public class Task11Verification {

    public static void main(String[] args) {
        System.out.println("===============================================================");
        System.out.println("    BẮT ĐẦU KIỂM THỬ TASK 11: TEST IQ HÌNH ẢNH TRONG KHI CHỜ   ");
        System.out.println("===============================================================");

        int passed = 0;
        int failed = 0;

        // Khởi tạo CSDL
        try {
            DatabaseManager.initialize();
            System.out.println("  ✓ CSDL đã sẵn sàng.");
        } catch (Exception e) {
            System.err.println("  ! Lỗi kết nối CSDL: " + e.getMessage());
        }

        QuestionDAO questionDAO = new QuestionDAO();
        UserDAO userDAO = new UserDAO();
        IQTestService iqTestService = new IQTestService();

        // --- TEST 1: LẤY CÂU HỎI IQ HÌNH ẢNH TỪ CSDL ---
        System.out.println("\n[Test 1] Lấy câu hỏi IQ hình ảnh (LoaiCauHoi = 'IQ_IMAGE'):");
        Question iqQ = questionDAO.getRandomIQQuestion();
        if (iqQ != null && "IQ_IMAGE".equalsIgnoreCase(iqQ.getQuestionType())) {
            System.out.println("  ✓ Lấy thành công câu hỏi IQ hình ảnh: ID = " + iqQ.getQuestionId());
            System.out.println("    - Nội dung: " + iqQ.getContent());
            System.out.println("    - Ảnh: " + iqQ.getImage());
            System.out.println("    - Số đáp án: " + (iqQ.getAnswers() != null ? iqQ.getAnswers().size() : 0));
            passed++;
        } else {
            System.err.println("  ✗ Lỗi: Không lấy được câu hỏi IQ hình ảnh!");
            failed++;
        }

        // --- TEST 2: KIỂM TRA ĐÁP ÁN ĐÚNG/SAI CỦA CÂU HỎI IQ ---
        System.out.println("\n[Test 2] Kiểm tra logic chấm điểm đáp án:");
        Answer correctAns = null;
        Answer wrongAns = null;
        if (iqQ != null && iqQ.getAnswers() != null) {
            for (Answer a : iqQ.getAnswers()) {
                if (a.isCorrect()) correctAns = a;
                else if (wrongAns == null) wrongAns = a;
            }
        }

        if (correctAns != null && wrongAns != null) {
            boolean checkCorrect = questionDAO.checkAnswer(iqQ.getQuestionId(), correctAns.getAnswerId());
            boolean checkWrong = questionDAO.checkAnswer(iqQ.getQuestionId(), wrongAns.getAnswerId());

            if (checkCorrect && !checkWrong) {
                System.out.println("  ✓ Đáp án đúng [" + correctAns.getKeySymbol() + "] trả về TRUE.");
                System.out.println("  ✓ Đáp án sai [" + wrongAns.getKeySymbol() + "] trả về FALSE.");
                passed++;
            } else {
                System.err.println("  ✗ Lỗi kiểm tra tính đúng sai của đáp án!");
                failed++;
            }
        } else {
            System.err.println("  ✗ Không tìm thấy đủ đáp án đúng/sai để kiểm thử!");
            failed++;
        }

        // --- TEST 3: TẠO TÀI KHOẢN VÀ TEST CHẤM ĐIỂM IQ CẬP NHẬT HỒ SƠ ---
        System.out.println("\n[Test 3] Kiểm tra cập nhật điểm IQ vào hồ sơ người dùng:");
        AuthService authService = new AuthService();
        String username = "iq_tester_" + System.currentTimeMillis() % 10000;
        AuthDTO.RegisterRequest reg = new AuthDTO.RegisterRequest();
        reg.username = username;
        reg.password = "123456";
        reg.email = username + "@example.com";
        reg.fullName = "Người Chơi Test IQ";
        authService.register(reg);

        com.quizzlive.server.entity.User user = userDAO.findByUsername(username);
        if (user != null) {
            userDAO.activateUser(user.getUserId());
            UserProfile initialProfile = userDAO.getUserProfile(user.getUserId());
            double initialIq = initialProfile != null && initialProfile.getIqScore() != null ? initialProfile.getIqScore() : 100.0;
            System.out.println("  ✓ Tài khoản tạo thành công: ID = " + user.getUserId() + ", Điểm IQ ban đầu = " + initialIq);

            // Tạo Mock ClientSession để nhận gói tin
            final List<Packet> capturedPackets = new ArrayList<>();
            ClientSession mockSession = new ClientSession(null, new SessionManager(), null) {
                @Override
                public synchronized void sendPacket(Packet packet) {
                    capturedPackets.add(packet);
                }
            };
            mockSession.setUserId(user.getUserId());
            mockSession.setUsername(user.getUsername());

            // 1. Gửi câu hỏi IQ
            iqTestService.sendRandomIQQuestion(mockSession);
            if (!capturedPackets.isEmpty() && capturedPackets.get(0).getOpCode() == OpCode.IQ_QUESTION_NOTIFY) {
                IQTestDTO.IQQuestionNotify qNotify = JsonUtil.fromJson(capturedPackets.get(0).getPayloadJson(), IQTestDTO.IQQuestionNotify.class);
                System.out.println("  ✓ Gói tin IQ_QUESTION_NOTIFY gửi thành công, ID câu hỏi = " + qNotify.questionId);
                passed++;
            } else {
                System.err.println("  ✗ Lỗi gửi gói tin IQ_QUESTION_NOTIFY!");
                failed++;
            }

            // 2. Nộp đáp án ĐÚNG
            capturedPackets.clear();
            IQTestDTO.IQSubmitRequest submitReq = new IQTestDTO.IQSubmitRequest();
            submitReq.questionId = iqQ.getQuestionId();
            submitReq.selectedAnswerId = correctAns.getAnswerId();

            iqTestService.handleIQSubmit(mockSession, submitReq);

            if (!capturedPackets.isEmpty() && capturedPackets.get(0).getOpCode() == OpCode.IQ_RESULT_NOTIFY) {
                IQTestDTO.IQResultNotify resNotify = JsonUtil.fromJson(capturedPackets.get(0).getPayloadJson(), IQTestDTO.IQResultNotify.class);
                if (resNotify.isCorrect && resNotify.currentIqScore > initialIq) {
                    System.out.println("  ✓ Chấm đúng thành công! Điểm IQ mới = " + resNotify.currentIqScore + " (" + resNotify.message + ")");
                    passed++;
                } else {
                    System.err.println("  ✗ Kết quả chấm điểm đúng không như kỳ vọng!");
                    failed++;
                }
            } else {
                System.err.println("  ✗ Lỗi nhận gói tin IQ_RESULT_NOTIFY!");
                failed++;
            }

            // Kiểm tra điểm IQ đã được lưu vào CSDL (HO_SO_NGUOI_DUNG)
            UserProfile updatedProfile = userDAO.getUserProfile(user.getUserId());
            if (updatedProfile != null && updatedProfile.getIqScore() > initialIq) {
                System.out.println("  ✓ Điểm IQ đã được lưu bền vững vào CSDL (DiemIQ = " + updatedProfile.getIqScore() + ").");
                passed++;
            } else {
                System.err.println("  ✗ Điểm IQ chưa được lưu vào CSDL!");
                failed++;
            }
        }

        System.out.println("\n===============================================================");
        System.out.println("             TỔNG KẾT KẾT QUẢ KIỂM THỬ TASK 11                ");
        System.out.println("===============================================================");
        System.out.println("  TỔNG SỐ TEST: " + (passed + failed));
        System.out.println("  THÀNH CÔNG:   " + passed);
        System.out.println("  THẤT BẠI:     " + failed);
        System.out.println("===============================================================");
        System.exit(failed == 0 ? 0 : 1);
    }
}

