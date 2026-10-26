package com.quizzlive.server.service;

import com.quizzlive.server.network.ClientSession;
import com.quizzlive.shared.dto.IQTestDTO;

/**
 * Service xử lý bài kiểm tra IQ hình ảnh trong lúc chờ ghép cặp
 * Phụ trách Yêu cầu 11: Test IQ bằng hình ảnh trong lúc chờ ghép cặp; không dùng để xếp hạng; điểm IQ lưu vào hồ sơ.
 */
public class IQTestService {

    /**
     * TODO: Dev Nhóm 7 triển khai
     * 1. Lấy 1 câu hỏi IQ từ CSDL (bảng CAU_HOI WHERE LoaiCauHoi = 'IQ_IMAGE')
     * 2. Gửi gói tin IQ_QUESTION_NOTIFY cho client đang trong hàng đợi
     */
    public void sendRandomIQQuestion(ClientSession session) {
        // TODO: Viết code lấy và gửi câu hỏi IQ
    }

    /**
     * TODO: Dev Nhóm 7 triển khai
     * 1. Kiểm tra đáp án người chơi chọn
     * 2. Tính lại điểm IQ và lưu vào bảng HO_SO_NGUOI_DUNG (DiemIQ)
     * 3. Gửi thông báo kết quả IQ_RESULT_NOTIFY về cho client
     */
    public void handleIQSubmit(ClientSession session, IQTestDTO.IQSubmitRequest req) {
        // TODO: Viết code chấm điểm IQ và cập nhật hồ sơ
    }
}
