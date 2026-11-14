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

import java.time.LocalDate;
import java.util.ArrayList;

/**
 * Service xử lý bài kiểm tra IQ (5 câu / ngày):
 * - Trích xuất từ bảng CAU_HOI với loại là IQ_TEST
 * - Người dùng chỉ có thể trả lời 5 câu hỏi IQ test trong ngày
 * - Khi trả lời (dù đúng hay sai), skip ngay qua câu khác
 * - Chỉ sau khi trả lời đủ 5 câu hỏi mới kết toán điểm IQ:
 *   5 câu tổng 1 điểm IQ, mỗi câu sai trừ 0.2 điểm (Delta = max(0, 1.0 - soCauSai * 0.2))
 */
public class IQTestService {
    private static final int MAX_DAILY_QUESTIONS = 5;
    private final QuestionDAO questionDAO = new QuestionDAO();
    private final UserDAO userDAO = new UserDAO();

    public void sendRandomIQQuestion(ClientSession session) {
        sendRandomIQQuestion(session, null, null);
    }

    public void sendRandomIQQuestion(ClientSession session, Integer topicId, String difficulty) {
        if (session == null) return;

        Long userId = session.getUserId();
        LocalDate today = LocalDate.now();

        // 1. Kiểm tra xem người dùng đã hoàn thành và kết toán 5 câu hôm nay chưa
        if (userId != null && questionDAO.isDailySettled(userId, today)) {
            double currentIq = 100.0;
            UserProfile p = userDAO.getUserProfile(userId);
            if (p != null && p.getIqScore() != null) currentIq = p.getIqScore();

            IQTestDTO.IQQuestionNotify notify = new IQTestDTO.IQQuestionNotify();
            notify.questionId = -1L;
            notify.isDailyCompleted = true;
            notify.currentQuestionIndex = MAX_DAILY_QUESTIONS;
            notify.maxDailyQuestions = MAX_DAILY_QUESTIONS;
            notify.content = "Hôm nay bạn đã hoàn thành bài Test IQ (5/5 câu). Điểm IQ hiện tại: " + currentIq + ".\nVui lòng quay lại vào ngày mai!";
            notify.dailyMessage = "Đã hoàn thành 5/5 câu hỏi IQ hôm nay";
            notify.options = new ArrayList<>();
            session.sendPacket(new Packet(OpCode.IQ_QUESTION_NOTIFY, JsonUtil.toJson(notify)));
            return;
        }

        // 2. Kiểm tra số câu đã làm trong ngày
        int answeredToday = (userId != null) ? questionDAO.countTodayQuestions(userId, today) : 0;
        if (answeredToday >= MAX_DAILY_QUESTIONS) {
            // Đã trả lời đủ 5 câu nhưng chưa kết toán -> tiến hành kết toán ngay
            finalizeDailyIQ(session, userId, today);
            return;
        }

        // 3. Lấy câu hỏi IQ_TEST tiếp theo (loại trừ các câu đã làm hôm nay)
        Question q = questionDAO.getRandomIQQuestion(userId, today);
        if (q == null) {
            IQTestDTO.IQQuestionNotify notify = new IQTestDTO.IQQuestionNotify();
            notify.questionId = -1L;
            notify.content = "Hiện chưa có thêm câu hỏi IQ_TEST trong hệ thống. Vui lòng thử lại sau!";
            notify.options = new ArrayList<>();
            session.sendPacket(new Packet(OpCode.IQ_QUESTION_NOTIFY, JsonUtil.toJson(notify)));
            return;
        }

        int questionIndex = answeredToday + 1;
        IQTestDTO.IQQuestionNotify notify = new IQTestDTO.IQQuestionNotify();
        notify.questionId = q.getQuestionId();
        notify.content = q.getContent();
        notify.imageUrl = q.getImage();
        notify.topicName = (q.getTopicName() != null) ? q.getTopicName() : "Tư duy IQ & Logic";
        notify.difficulty = (q.getDifficulty() != null) ? q.getDifficulty() : "MEDIUM";
        notify.currentQuestionIndex = questionIndex;
        notify.maxDailyQuestions = MAX_DAILY_QUESTIONS;
        notify.isDailyCompleted = false;
        notify.dailyMessage = "Câu hỏi IQ " + questionIndex + "/" + MAX_DAILY_QUESTIONS + " trong ngày";
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

        session.sendPacket(new Packet(OpCode.IQ_QUESTION_NOTIFY, JsonUtil.toJson(notify)));
    }

    /**
     * Tiếp nhận câu trả lời cho câu hỏi IQ:
     * - Dù đúng hay sai, ghi nhận kết quả và skip ngay sang câu tiếp theo
     * - Chỉ sau khi trả lời đủ 5 câu hỏi mới kết toán điểm IQ:
     *   5 câu tối đa 1.0 điểm, mỗi câu sai trừ 0.2 điểm
     */
    public void handleIQSubmit(ClientSession session, IQTestDTO.IQSubmitRequest req) {
        if (session == null || req == null || req.questionId <= 0) return;

        Long userId = session.getUserId();
        LocalDate today = LocalDate.now();

        if (userId != null && questionDAO.isDailySettled(userId, today)) {
            IQTestDTO.IQResultNotify res = new IQTestDTO.IQResultNotify();
            res.isFinished = true;
            res.message = "Bạn đã hoàn thành 5/5 câu hỏi IQ của ngày hôm nay rồi!";
            session.sendPacket(new Packet(OpCode.IQ_RESULT_NOTIFY, JsonUtil.toJson(res)));
            return;
        }

        int answeredBefore = (userId != null) ? questionDAO.countTodayQuestions(userId, today) : 0;
        if (answeredBefore >= MAX_DAILY_QUESTIONS) {
            finalizeDailyIQ(session, userId, today);
            return;
        }

        // 1. Kiểm tra đáp án câu hỏi
        boolean isCorrect = questionDAO.checkAnswer(req.questionId, req.selectedAnswerId);

        // 2. Ghi nhận kết quả câu hỏi vào bảng LICHSU_TEST_IQ
        if (userId != null) {
            questionDAO.recordIQAnswer(userId, req.questionId, isCorrect, today);
        }

        int answeredAfter = answeredBefore + 1;

        // 3. Nếu chưa đủ 5 câu -> Ghi nhận và SKIP NGAY QUA CÂU KHÁC (chưa kết toán điểm IQ)
        if (answeredAfter < MAX_DAILY_QUESTIONS) {
            IQTestDTO.IQResultNotify res = new IQTestDTO.IQResultNotify();
            res.isFinished = false;
            res.isCorrect = isCorrect;
            res.currentQuestionIndex = answeredAfter;
            res.maxDailyQuestions = MAX_DAILY_QUESTIONS;
            res.message = (isCorrect ? "✅ Câu " + answeredAfter + "/" + MAX_DAILY_QUESTIONS + ": Đúng!" : "❌ Câu " + answeredAfter + "/" + MAX_DAILY_QUESTIONS + ": Sai!")
                    + " Đang chuyển sang câu tiếp theo...";
            session.sendPacket(new Packet(OpCode.IQ_RESULT_NOTIFY, JsonUtil.toJson(res)));

            // Lập tức gửi câu hỏi tiếp theo về cho Client
            sendRandomIQQuestion(session);
            return;
        }

        // 4. Đã hoàn thành câu thứ 5 -> KẾT TOÁN ĐIỂM IQ
        finalizeDailyIQ(session, userId, today);
    }

    /**
     * Kết toán điểm IQ sau khi hoàn thành 5 câu hỏi trong ngày:
     * - 5 câu tổng 1 điểm IQ, mỗi câu sai trừ 0.2 điểm
     * - Điểm nhận được = max(0.0, 1.0 - (soCauSai * 0.2))
     * - Cập nhật điểm IQ vào hồ sơ người dùng và bảng KET_TOAN_IQ_NGAY
     */
    private void finalizeDailyIQ(ClientSession session, Long userId, LocalDate today) {
        int correctCount = (userId != null) ? questionDAO.countTodayCorrectQuestions(userId, today) : 0;
        int wrongCount = MAX_DAILY_QUESTIONS - correctCount;
        if (wrongCount < 0) wrongCount = 0;

        // Công thức: 5 câu tổng 1 điểm IQ, mỗi câu sai trừ 0.2 điểm
        double deltaIq = Math.max(0.0, 1.0 - (wrongCount * 0.2));
        deltaIq = Math.round(deltaIq * 100.0) / 100.0;

        double currentIq = 100.0;
        if (userId != null) {
            UserProfile p = userDAO.getUserProfile(userId);
            if (p != null && p.getIqScore() != null) currentIq = p.getIqScore();
        }

        double newIq = Math.round((currentIq + deltaIq) * 100.0) / 100.0;

        if (userId != null) {
            userDAO.updateIQScore(userId, newIq);
            questionDAO.saveDailySettlement(userId, today, correctCount, wrongCount, deltaIq);
        }

        // Gửi thông báo kết toán về cho Client
        IQTestDTO.IQResultNotify res = new IQTestDTO.IQResultNotify();
        res.isFinished = true;
        res.isCorrect = (correctCount > 0);
        res.currentQuestionIndex = MAX_DAILY_QUESTIONS;
        res.maxDailyQuestions = MAX_DAILY_QUESTIONS;
        res.correctCount = correctCount;
        res.wrongCount = wrongCount;
        res.deltaIq = deltaIq;
        res.currentIqScore = newIq;
        res.message = String.format("🎉 Hoàn thành bài Test IQ hôm nay (5/5 câu)!\n• Đúng: %d/5 | Sai: %d/5 (-%.1f điểm)\n• Kết toán: +%.1f điểm IQ (Điểm mới: %.1f)",
                correctCount, wrongCount, wrongCount * 0.2, deltaIq, newIq);

        session.sendPacket(new Packet(OpCode.IQ_RESULT_NOTIFY, JsonUtil.toJson(res)));

        // Gửi gói tin cập nhật trạng thái đã hoàn thành ngày
        IQTestDTO.IQQuestionNotify finishNotify = new IQTestDTO.IQQuestionNotify();
        finishNotify.questionId = -1L;
        finishNotify.isDailyCompleted = true;
        finishNotify.currentQuestionIndex = MAX_DAILY_QUESTIONS;
        finishNotify.maxDailyQuestions = MAX_DAILY_QUESTIONS;
        finishNotify.content = String.format("Bạn đã hoàn thành 5/5 câu IQ hôm nay. Kết quả: Đúng %d/5, Sai %d/5, +%.1f điểm IQ.",
                correctCount, wrongCount, deltaIq);
        finishNotify.dailyMessage = "Đã hoàn thành 5/5 câu hỏi IQ hôm nay";
        finishNotify.options = new ArrayList<>();
        session.sendPacket(new Packet(OpCode.IQ_QUESTION_NOTIFY, JsonUtil.toJson(finishNotify)));
    }
}
