-- =========================================================================
-- QUIZZLIVE SEED DATA - DÀNH RIÊNG CHO POSTGRESQL
-- =========================================================================

-- 1. Tài khoản Quản trị viên
INSERT INTO QUAN_TRI_VIEN (MaQuanTri, TenDangNhap, MatKhau, HoTen, VaiTro, TrangThai)
VALUES (1, 'admin', 'admin123', 'Quản Trị Viên Hệ Thống', 'ADMIN', TRUE)
ON CONFLICT (MaQuanTri) DO UPDATE SET HoTen = EXCLUDED.HoTen;

-- 2. Cấu hình trận đấu mặc định
INSERT INTO CAU_HINH_TRAN (MaCauHinh, TenCauHinh, SoCauHoi, ThoiGianMoiCau, DiemCoBan, ThuongTheoThoiGian, DiemSai, DiemKhongTraLoi, TrangThai)
VALUES 
(1, 'Cấu hình tiêu chuẩn (5 câu - 10s)', 5, 10, 100, TRUE, 0, 0, TRUE),
(2, 'Cấu hình thi đấu giải (10 câu - 15s)', 10, 15, 150, TRUE, 0, 0, TRUE)
ON CONFLICT (MaCauHinh) DO NOTHING;

-- 3. Danh mục Chủ đề
INSERT INTO CHU_DE (MaChuDe, TenChuDe, MoTa, TrangThai) VALUES
(1, 'Khoa học Tự nhiên', 'Toán học, Vật lý, Hóa học, Sinh học', TRUE),
(2, 'Lịch sử & Địa lý', 'Lịch sử thế giới, Việt Nam và địa lý toàn cầu', TRUE),
(3, 'Công nghệ & Máy tính', 'Lập trình, mạng máy tính, công nghệ 4.0', TRUE),
(4, 'Tư duy IQ & Logic', 'Câu hỏi trắc nghiệm IQ hình ảnh và logic', TRUE)
ON CONFLICT (MaChuDe) DO NOTHING;

-- 4. Người dùng mẫu
INSERT INTO NGUOI_DUNG (MaNguoiDung, TenDangNhap, MatKhau, Email, TrangThai, TrangThaiOnline, LanDangNhapCuoi) VALUES
(1, 'player_nam', '123456', 'nam@quizzlive.com', 'ACTIVE', FALSE, CURRENT_TIMESTAMP),
(2, 'player_hoa', '123456', 'hoa@quizzlive.com', 'ACTIVE', FALSE, CURRENT_TIMESTAMP),
(3, 'player_tuan', '123456', 'tuan@quizzlive.com', 'ACTIVE', FALSE, CURRENT_TIMESTAMP),
(4, 'player_lan', '123456', 'lan@quizzlive.com', 'ACTIVE', FALSE, CURRENT_TIMESTAMP)
ON CONFLICT (MaNguoiDung) DO NOTHING;

INSERT INTO HO_SO_NGUOI_DUNG (MaNguoiDung, HoTen, AnhDaiDien, GioiThieu, DiemIQ, TongSoTran, TongSoTranThang, TongSoTranThua, ChuoiThangHienTai, ChuoiThangDaiNhat, TongDiem) VALUES
(1, 'Nguyễn Văn Nam', 'avatar1.png', 'Thích tranh tài tri thức', 125.00, 20, 15, 5, 4, 7, 18500),
(2, 'Trần Thị Hoa', 'avatar2.png', 'Đam mê lập trình mạng', 130.50, 25, 20, 5, 6, 9, 24600),
(3, 'Lê Anh Tuấn', 'avatar3.png', 'Cao thủ trắc nghiệm', 118.00, 15, 8, 7, 1, 3, 9800),
(4, 'Phạm Ngọc Lan', 'avatar4.png', 'Newbie học hỏi', 110.00, 10, 4, 6, 0, 2, 4500)
ON CONFLICT (MaNguoiDung) DO NOTHING;

INSERT INTO THONG_KE_NGUOI_CHOI (MaNguoiDung, TongSoTran, TongSoThang, TongSoThua, TongSoHoa, TongSoCau, TongCauDung, TongDiem, ChuoiThangDaiNhat, DiemCaoNhat) VALUES
(1, 20, 15, 5, 0, 100, 82, 18500, 7, 980),
(2, 25, 20, 5, 0, 125, 110, 24600, 9, 1000),
(3, 15, 8, 7, 0, 75, 45, 9800, 3, 850),
(4, 10, 4, 6, 0, 50, 28, 4500, 2, 600)
ON CONFLICT (MaNguoiDung) DO NOTHING;

-- 5. Ngân hàng Câu hỏi & Đáp án
INSERT INTO CAU_HOI (MaCauHoi, MaChuDe, LoaiCauHoi, DoKho, NoiDung, HinhAnh, GiaiThich, TrangThai) VALUES
(1, 3, 'STANDARD', 'EASY', 'Giao thức nào sau đây hoạt động ở tầng Transport và cung cấp truyền dữ liệu tin cậy?', NULL, 'TCP là giao thức hướng kết nối và đảm bảo toàn vẹn dữ liệu.', 'ACTIVE'),
(2, 3, 'STANDARD', 'EASY', 'Cổng mặc định của giao thức HTTP là bao nhiêu?', NULL, 'HTTP mặc định chạy trên cổng 80.', 'ACTIVE'),
(3, 1, 'STANDARD', 'MEDIUM', 'Hành tinh nào trong Hệ Mặt Trời được mệnh danh là Hành tinh Đỏ?', NULL, 'Sao Hỏa (Mars) có màu đỏ do bề mặt chứa nhiều oxit sắt.', 'ACTIVE'),
(4, 4, 'IQ_IMAGE', 'MEDIUM', 'Quan sát quy luật xoay của hình và chọn hình còn thiếu ở vị trí dấu hỏi chấm (?)', 'iq_pattern_01.png', 'Hình xoay theo chiều kim đồng hồ góc 45 độ.', 'ACTIVE')
ON CONFLICT (MaCauHoi) DO NOTHING;

INSERT INTO DAP_AN (MaDapAn, MaCauHoi, KyHieu, NoiDung, LaDapAnDung) VALUES
(1, 1, 'A', 'TCP', TRUE),
(2, 1, 'B', 'UDP', FALSE),
(3, 1, 'C', 'IP', FALSE),
(4, 1, 'D', 'HTTP', FALSE),
(5, 2, 'A', '21', FALSE),
(6, 2, 'B', '80', TRUE),
(7, 2, 'C', '443', FALSE),
(8, 2, 'D', '8080', FALSE),
(9, 3, 'A', 'Sao Kim', FALSE),
(10, 3, 'B', 'Sao Hỏa', TRUE),
(11, 3, 'C', 'Sao Mộc', FALSE),
(12, 3, 'D', 'Sao Thủy', FALSE),
(13, 4, 'A', 'Hình số 1', FALSE),
(14, 4, 'B', 'Hình số 2', TRUE),
(15, 4, 'C', 'Hình số 3', FALSE),
(16, 4, 'D', 'Hình số 4', FALSE)
ON CONFLICT (MaDapAn) DO NOTHING;
