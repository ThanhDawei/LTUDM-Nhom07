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
     */
    public void sendRandomIQQuestion(ClientSession session) {
        if (session == null) return;

        Question q = questionDAO.getRandomIQQuestion();
        if (q == null) return;

        IQTestDTO.IQQuestionNotify notify = new IQTestDTO.IQQuestionNotify();
        notify.questionId = q.getQuestionId();
        notify.content = q.getContent();
        notify.imageUrl = q.getImage();
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
     * 4. Gửi kết quả IQ_RESULT_NOTIFY về cho Client
     */
    public void handleIQSubmit(ClientSession session, IQTestDTO.IQSubmitRequest req) {
        if (session == null || req == null) return;

        boolean isCorrect = questionDAO.checkAnswer(req.questionId, req.selectedAnswerId);
        String difficulty = questionDAO.getQuestionDifficulty(req.questionId);

        Long userId = session.getUserId();
        double currentIq = 100.0;
        if (userId != null) {
            UserProfile profile = userDAO.getUserProfile(userId);
            if (profile != null && profile.getIqScore() != null && profile.getIqScore() > 0) {
                currentIq = profile.getIqScore();
            }
        }

        double delta;
        if (isCorrect) {
            if ("HARD".equalsIgnoreCase(difficulty)) delta = 5.0;
            else if ("EASY".equalsIgnoreCase(difficulty)) delta = 2.0;
            else delta = 3.0;
        } else {
            delta = -1.0;
        }

        double newIqScore = Math.round((currentIq + delta) * 10.0) / 10.0;
        if (newIqScore < 70.0) newIqScore = 70.0;
        if (newIqScore > 160.0) newIqScore = 160.0;

        // Lưu điểm IQ vào hồ sơ người dùng (Yêu cầu 11)
        if (userId != null) {
            userDAO.updateIQScore(userId, newIqScore);
        }

        IQTestDTO.IQResultNotify result = new IQTestDTO.IQResultNotify();
        result.currentIqScore = newIqScore;
        result.isCorrect = isCorrect;
        result.message = isCorrect
                ? "Chính xác! Bạn được cộng " + delta + " điểm IQ."
                : "Chưa chính xác! Hãy thử lại câu đố tiếp theo.";

        session.sendPacket(new Packet(OpCode.IQ_RESULT_NOTIFY, JsonUtil.toJson(result)));
    }
}
