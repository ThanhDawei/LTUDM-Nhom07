package com.quizzlive.server.service;

import com.quizzlive.server.dao.QuestionDAO;
import com.quizzlive.server.dao.UserDAO;
import com.quizzlive.server.entity.Answer;
import com.quizzlive.server.entity.Question;
import com.quizzlive.server.entity.UserProfile;
import com.quizzlive.server.network.ClientSession;
import com.quizzlive.shared.dto.BattleDTO;
import com.quizzlive.shared.dto.IQTestDTO;
import com.quizzlive.shared.protocol.OpCode;
import com.quizzlive.shared.protocol.Packet;
import com.quizzlive.shared.util.JsonUtil;

import java.util.ArrayList;

/**
 * Service xử lý bài kiểm tra IQ hình ảnh trong lúc chờ ghép cặp.
 * Phụ trách Yêu cầu 11: "Test IQ bằng hình ảnh trong lúc chờ ghép cặp; không dùng để xếp hạng; điểm IQ lưu vào hồ sơ."
 * Cấu hình: KHÔNG DÙNG BĂM VÀ MÃ HÓA, truyền trực tiếp cấu trúc JSON qua Socket thuần.
 */
public class IQTestService {
    private final QuestionDAO questionDAO = new QuestionDAO();
    private final UserDAO userDAO = new UserDAO();

    /**
     * Lấy ngẫu nhiên 1 câu hỏi IQ hình ảnh từ CSDL và gửi gói tin IQ_QUESTION_NOTIFY về cho Client
     * Yêu cầu 11: Câu hỏi nào trả lời tăng IQ rồi thì không làm câu hỏi đó nữa.
     */
    public void sendRandomIQQuestion(ClientSession session) {
        if (session == null) return;

        Long userId = session.getUserId();
        Question q = questionDAO.getRandomIQQuestion(userId);

        if (q == null) {
            // Trường hợp người dùng đã giải đúng tất cả câu hỏi IQ trong hệ thống
            double currentIq = 100.0;
            if (userId != null) {
                UserProfile p = userDAO.getUserProfile(userId);
                if (p != null && p.getIqScore() != null) currentIq = p.getIqScore();
            }
            IQTestDTO.IQQuestionNotify notify = new IQTestDTO.IQQuestionNotify();
            notify.questionId = -1L;
            notify.content = "🎉 Bạn đã xuất sắc giải đúng tất cả câu đố IQ hiện có trong ngân hàng câu hỏi! Điểm IQ hiện tại: " + currentIq;
            notify.imageUrl = "iq_completed.png";
            notify.options = new ArrayList<>();
            session.sendPacket(new Packet(OpCode.IQ_QUESTION_NOTIFY, JsonUtil.toJson(notify)));
            return;
        }

        IQTestDTO.IQQuestionNotify notify = new IQTestDTO.IQQuestionNotify();
        notify.questionId = q.getQuestionId();
        notify.content = q.getContent();
        notify.imageUrl = q.getImage();
        notify.topicName = q.getTopicName() != null ? q.getTopicName() : "Tư duy IQ & Kiến thức";
        notify.difficulty = q.getDifficulty() != null ? q.getDifficulty() : "MEDIUM";
        notify.options = new ArrayList<>();

        if (q.getAnswers() != null) {
            for (Answer a : q.getAnswers()) {
                BattleDTO.QuestionAnswerOption opt = new BattleDTO.QuestionAnswerOption();
                opt.answerId = a.getAnswerId();
                opt.key = a.getKeySymbol();
                opt.content = a.getContent();
                opt.image = a.getImage();
                notify.options.add(opt);
            }
        }

        // Gửi trực tiếp gói tin IQ_QUESTION_NOTIFY qua Socket
        session.sendPacket(new Packet(OpCode.IQ_QUESTION_NOTIFY, JsonUtil.toJson(notify)));
    }

    /**
     * Tiếp nhận và chấm điểm đáp án câu hỏi IQ hình ảnh do người chơi nộp:
     * 1. Đối chiếu đáp án đúng/sai
     * 2. Tính lại điểm IQ theo độ khó (EASY: +2, MEDIUM: +3, HARD: +5, SAI: -1)
     * 3. Lưu điểm IQ vào bảng HO_SO_NGUOI_DUNG (DiemIQ)
     * 4. Đánh dấu câu hỏi đã trả lời tăng IQ để không xuất hiện lại
     * 5. Gửi kết quả IQ_RESULT_NOTIFY về cho Client
     */
    public void handleIQSubmit(ClientSession session, IQTestDTO.IQSubmitRequest req) {
        if (session == null || req == null) return;
        if (req.questionId <= 0) return;

        Long userId = session.getUserId();

        // Kiểm tra xem câu này người dùng đã trả lời tăng IQ trước đó chưa
        if (userId != null && questionDAO.hasUserPassedIQQuestion(userId, req.questionId)) {
            double currentIq = 100.0;
            UserProfile profile = userDAO.getUserProfile(userId);
            if (profile != null && profile.getIqScore() != null) {
                currentIq = profile.getIqScore();
            }
            IQTestDTO.IQResultNotify result = new IQTestDTO.IQResultNotify();
            result.currentIqScore = currentIq;
            result.isCorrect = true;
            result.message = "Câu hỏi này bạn đã giải đúng và được cộng điểm IQ trước đó rồi! Vui lòng làm câu tiếp theo.";
            session.sendPacket(new Packet(OpCode.IQ_RESULT_NOTIFY, JsonUtil.toJson(result)));
            return;
        }

        boolean isCorrect = questionDAO.checkAnswer(req.questionId, req.selectedAnswerId);
        String difficulty = questionDAO.getQuestionDifficulty(req.questionId);

        double currentIq = 100.0;
        if (userId != null) {
            UserProfile profile = userDAO.getUserProfile(userId);
            if (profile != null && profile.getIqScore() != null && profile.getIqScore() > 0) {
                currentIq = profile.getIqScore();
            }
        }

        double delta = 0.0;
        double newIqScore = currentIq;

        if (isCorrect) {
            if ("HARD".equalsIgnoreCase(difficulty)) delta = 5.0;
            else if ("EASY".equalsIgnoreCase(difficulty)) delta = 2.0;
            else delta = 3.0;

            newIqScore = Math.round((currentIq + delta) * 10.0) / 10.0;
            if (newIqScore > 160.0) newIqScore = 160.0;

            // Lưu điểm IQ vào hồ sơ người dùng và đánh dấu đã tăng IQ (Yêu cầu 11)
            if (userId != null) {
                userDAO.updateIQScore(userId, newIqScore);
                questionDAO.recordPassedIQQuestion(userId, req.questionId);
            }
        }
        // Khi trả lời sai: KHÔNG TRỪ IQ, không lưu điểm giảm, cho phép trả lời lại mà không lo bị spam trừ IQ

        IQTestDTO.IQResultNotify result = new IQTestDTO.IQResultNotify();
        result.currentIqScore = newIqScore;
        result.isCorrect = isCorrect;
        result.message = isCorrect
                ? "Chính xác! Bạn được cộng " + delta + " điểm IQ."
                : "Chưa chính xác! Hãy chọn lại đáp án khác (không bị trừ IQ).";

        session.sendPacket(new Packet(OpCode.IQ_RESULT_NOTIFY, JsonUtil.toJson(result)));
    }
}
