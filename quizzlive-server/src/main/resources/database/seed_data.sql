-- =========================================================================
-- QUIZZLIVE SEED DATA - DỮ LIỆU KHỞI TẠO MẪU CHO NHÓM 07
-- =========================================================================
USE `quizzlive_db`;

-- 1. Tài khoản Quản trị viên (Admin GUI)
INSERT INTO `QUAN_TRI_VIEN` (`MaQuanTri`, `TenDangNhap`, `MatKhau`, `HoTen`, `VaiTro`, `TrangThai`)
VALUES 
(1, 'admin', 'admin123', 'Quản Trị Viên Hệ Thống', 'ADMIN', TRUE)
ON DUPLICATE KEY UPDATE `HoTen` = VALUES(`HoTen`);

-- 2. Cấu hình trận đấu mặc định (Requirement 5, 13)
INSERT INTO `CAU_HINH_TRAN` (`MaCauHinh`, `TenCauHinh`, `SoCauHoi`, `ThoiGianMoiCau`, `DiemCoBan`, `ThuongTheoThoiGian`, `DiemSai`, `DiemKhongTraLoi`, `TrangThai`)
VALUES 
(1, 'Cấu hình tiêu chuẩn (5 câu - 10s)', 5, 10, 100, TRUE, 0, 0, TRUE),
(2, 'Cấu hình thi đấu giải (10 câu - 15s)', 10, 15, 150, TRUE, 0, 0, TRUE)
ON DUPLICATE KEY UPDATE `TenCauHinh` = VALUES(`TenCauHinh`);

-- 3. Danh mục Chủ đề (Requirement 4)
INSERT INTO `CHU_DE` (`MaChuDe`, `TenChuDe`, `MoTa`, `TrangThai`) VALUES
(1, 'Khoa học Tự nhiên', 'Toán học, Vật lý, Hóa học, Sinh học', TRUE),
(2, 'Lịch sử & Địa lý', 'Lịch sử thế giới, Việt Nam và địa lý toàn cầu', TRUE),
(3, 'Công nghệ & Máy tính', 'Lập trình, mạng máy tính, công nghệ 4.0', TRUE),
(4, 'Tư duy IQ & Logic', 'Câu hỏi trắc nghiệm IQ hình ảnh và logic', TRUE)
ON DUPLICATE KEY UPDATE `TenChuDe` = VALUES(`TenChuDe`);

-- 4. Người dùng mẫu (Requirement 1, 10, 16)
INSERT INTO `NGUOI_DUNG` (`MaNguoiDung`, `TenDangNhap`, `MatKhau`, `Email`, `TrangThai`, `TrangThaiOnline`, `LanDangNhapCuoi`) VALUES
(1, 'player_nam', '123456', 'nam@quizzlive.com', 'ACTIVE', FALSE, NOW()),
(2, 'player_hoa', '123456', 'hoa@quizzlive.com', 'ACTIVE', FALSE, NOW()),
(3, 'player_tuan', '123456', 'tuan@quizzlive.com', 'ACTIVE', FALSE, NOW()),
(4, 'player_lan', '123456', 'lan@quizzlive.com', 'ACTIVE', FALSE, NOW())
ON DUPLICATE KEY UPDATE `TenDangNhap` = VALUES(`TenDangNhap`);

INSERT INTO `HO_SO_NGUOI_DUNG` (`MaNguoiDung`, `HoTen`, `AnhDaiDien`, `GioiThieu`, `DiemIQ`, `TongSoTran`, `TongSoTranThang`, `TongSoTranThua`, `ChuoiThangHienTai`, `ChuoiThangDaiNhat`, `TongDiem`) VALUES
(1, 'Nguyễn Văn Nam', 'avatar1.png', 'Thích tranh tài tri thức', 125.00, 20, 15, 5, 4, 7, 18500),
(2, 'Trần Thị Hoa', 'avatar2.png', 'Đam mê lập trình mạng', 130.50, 25, 20, 5, 6, 9, 24600),
(3, 'Lê Anh Tuấn', 'avatar3.png', 'Cao thủ trắc nghiệm', 118.00, 15, 8, 7, 1, 3, 9800),
(4, 'Phạm Ngọc Lan', 'avatar4.png', 'Newbie học hỏi', 110.00, 10, 4, 6, 0, 2, 4500)
ON DUPLICATE KEY UPDATE `HoTen` = VALUES(`HoTen`);

INSERT INTO `THONG_KE_NGUOI_CHOI` (`MaNguoiDung`, `TongSoTran`, `TongSoThang`, `TongSoThua`, `TongSoHoa`, `TongSoCau`, `TongCauDung`, `TongDiem`, `ChuoiThangDaiNhat`, `DiemCaoNhat`) VALUES
(1, 20, 15, 5, 0, 100, 82, 18500, 7, 980),
(2, 25, 20, 5, 0, 125, 110, 24600, 9, 1000),
(3, 15, 8, 7, 0, 75, 45, 9800, 3, 850),
(4, 10, 4, 6, 0, 50, 28, 4500, 2, 600)
ON DUPLICATE KEY UPDATE `TongSoTran` = VALUES(`TongSoTran`);

-- 5. Ngân hàng Câu hỏi & Đáp án (Requirement 5, 7, 11, 12)
-- Câu 1: Chủ đề Công nghệ (MaChuDe = 3)
INSERT INTO `CAU_HOI` (`MaCauHoi`, `MaChuDe`, `LoaiCauHoi`, `DoKho`, `NoiDung`, `HinhAnh`, `GiaiThich`, `TrangThai`) VALUES
(1, 3, 'STANDARD', 'EASY', 'Giao thức nào sau đây hoạt động ở tầng Transport và cung cấp truyền dữ liệu tin cậy?', NULL, 'TCP (Transmission Control Protocol) là giao thức hướng kết nối và đảm bảo toàn vẹn dữ liệu.', 'ACTIVE');
INSERT INTO `DAP_AN` (`MaDapAn`, `MaCauHoi`, `KyHieu`, `NoiDung`, `LaDapAnDung`) VALUES
(1, 1, 'A', 'TCP', TRUE),
(2, 1, 'B', 'UDP', FALSE),
(3, 1, 'C', 'IP', FALSE),
(4, 1, 'D', 'HTTP', FALSE);

-- Câu 2: Chủ đề Công nghệ (MaChuDe = 3)
INSERT INTO `CAU_HOI` (`MaCauHoi`, `MaChuDe`, `LoaiCauHoi`, `DoKho`, `NoiDung`, `HinhAnh`, `GiaiThich`, `TrangThai`) VALUES
(2, 3, 'STANDARD', 'EASY', 'Cổng mặc định của giao thức HTTP là bao nhiêu?', NULL, 'HTTP mặc định chạy trên cổng 80, trong khi HTTPS dùng cổng 443.', 'ACTIVE');
INSERT INTO `DAP_AN` (`MaDapAn`, `MaCauHoi`, `KyHieu`, `NoiDung`, `LaDapAnDung`) VALUES
(5, 2, 'A', '21', FALSE),
(6, 2, 'B', '80', TRUE),
(7, 2, 'C', '443', FALSE),
(8, 2, 'D', '8080', FALSE);

-- Câu 3: Chủ đề Khoa học (MaChuDe = 1)
INSERT INTO `CAU_HOI` (`MaCauHoi`, `MaChuDe`, `LoaiCauHoi`, `DoKho`, `NoiDung`, `HinhAnh`, `GiaiThich`, `TrangThai`) VALUES
(3, 1, 'STANDARD', 'MEDIUM', 'Hành tinh nào trong Hệ Mặt Trời được mệnh danh là Hành tinh Đỏ?', NULL, 'Sao Hỏa (Mars) có màu đỏ do bề mặt chứa nhiều oxit sắt.', 'ACTIVE');
INSERT INTO `DAP_AN` (`MaDapAn`, `MaCauHoi`, `KyHieu`, `NoiDung`, `LaDapAnDung`) VALUES
(9, 3, 'A', 'Sao Kim', FALSE),
(10, 3, 'B', 'Sao Hỏa', TRUE),
(11, 3, 'C', 'Sao Mộc', FALSE),
(12, 3, 'D', 'Sao Thủy', FALSE);

-- Câu 4: Chủ đề Khoa học (MaChuDe = 1)
INSERT INTO `CAU_HOI` (`MaCauHoi`, `MaChuDe`, `LoaiCauHoi`, `DoKho`, `NoiDung`, `HinhAnh`, `GiaiThich`, `TrangThai`) VALUES
(4, 1, 'STANDARD', 'MEDIUM', 'Khí nào chiếm tỉ lệ cao nhất trong khí quyển Trái Đất?', NULL, 'Khí Nitơ (N2) chiếm khoảng 78% thể tích bầu khí quyển.', 'ACTIVE');
INSERT INTO `DAP_AN` (`MaDapAn`, `MaCauHoi`, `KyHieu`, `NoiDung`, `LaDapAnDung`) VALUES
(13, 4, 'A', 'Oxy', FALSE),
(14, 4, 'B', 'Nitơ', TRUE),
(15, 4, 'C', 'Cacbonic', FALSE),
(16, 4, 'D', 'Argon', FALSE);

-- Câu 5: Chủ đề Lịch sử & Địa lý (MaChuDe = 2)
INSERT INTO `CAU_HOI` (`MaCauHoi`, `MaChuDe`, `LoaiCauHoi`, `DoKho`, `NoiDung`, `HinhAnh`, `GiaiThich`, `TrangThai`) VALUES
(5, 2, 'STANDARD', 'MEDIUM', 'Chiến thắng Điện Biên Phủ diễn ra vào năm nào?', NULL, 'Chiến thắng lịch sử Điện Biên Phủ lừng lẫy năm châu chấn động địa cầu vào ngày 7/5/1954.', 'ACTIVE');
INSERT INTO `DAP_AN` (`MaDapAn`, `MaCauHoi`, `KyHieu`, `NoiDung`, `LaDapAnDung`) VALUES
(17, 5, 'A', '1945', FALSE),
(18, 5, 'B', '1954', TRUE),
(19, 5, 'C', '1975', FALSE),
(20, 5, 'D', '1968', FALSE);

-- Câu 6: Chủ đề Lịch sử & Địa lý (MaChuDe = 2)
INSERT INTO `CAU_HOI` (`MaCauHoi`, `MaChuDe`, `LoaiCauHoi`, `DoKho`, `NoiDung`, `HinhAnh`, `GiaiThich`, `TrangThai`) VALUES
(6, 2, 'STANDARD', 'HARD', 'Thành phố nào được coi là thành phố lâu đời nhất thế giới còn có người sinh sống liên tục?', NULL, 'Damascus (thủ đô của Syria) được xem là một trong những thành phố cổ nhất.', 'ACTIVE');
INSERT INTO `DAP_AN` (`MaDapAn`, `MaCauHoi`, `KyHieu`, `NoiDung`, `LaDapAnDung`) VALUES
(21, 6, 'A', 'Athens', FALSE),
(22, 6, 'B', 'Damascus', TRUE),
(23, 6, 'C', 'Rome', FALSE),
(24, 6, 'D', 'Cairo', FALSE);

-- Câu 7: IQ Test Bằng Hình Ảnh (Requirement 11: Test IQ bằng hình ảnh trong lúc chờ ghép cặp)
INSERT INTO `CAU_HOI` (`MaCauHoi`, `MaChuDe`, `LoaiCauHoi`, `DoKho`, `NoiDung`, `HinhAnh`, `GiaiThich`, `TrangThai`) VALUES
(7, 4, 'IQ_IMAGE', 'MEDIUM', 'Quan sát quy luật xoay của hình kim đồng hồ và chọn hình còn thiếu ở vị trí dấu hỏi chấm (?)', 'iq_pattern_01.png', 'Hình xoay theo chiều kim đồng hồ góc 45 độ ở mỗi bước.', 'ACTIVE');
INSERT INTO `DAP_AN` (`MaDapAn`, `MaCauHoi`, `KyHieu`, `NoiDung`, `HinhAnh`, `LaDapAnDung`) VALUES
(25, 7, 'A', 'Hình số 1', 'iq_ans_01_a.png', FALSE),
(26, 7, 'B', 'Hình số 2', 'iq_ans_01_b.png', TRUE),
(27, 7, 'C', 'Hình số 3', 'iq_ans_01_c.png', FALSE),
(28, 7, 'D', 'Hình số 4', 'iq_ans_01_d.png', FALSE);

-- Câu 8: IQ Test Bằng Hình Ảnh
INSERT INTO `CAU_HOI` (`MaCauHoi`, `MaChuDe`, `LoaiCauHoi`, `DoKho`, `NoiDung`, `HinhAnh`, `GiaiThich`, `TrangThai`) VALUES
(8, 4, 'IQ_IMAGE', 'HARD', 'Tìm mảnh ghép logic phù hợp nhất để hoàn thiện ma trận hình học 3x3:', 'iq_pattern_02.png', 'Mỗi hàng là tổng hợp số cạnh tăng dần 3, 4, 5.', 'ACTIVE');
INSERT INTO `DAP_AN` (`MaDapAn`, `MaCauHoi`, `KyHieu`, `NoiDung`, `HinhAnh`, `LaDapAnDung`) VALUES
(29, 8, 'A', 'Hình ngũ giác có chấm tròn giữa', 'iq_ans_02_a.png', TRUE),
(30, 8, 'B', 'Hình tam giác rỗng', 'iq_ans_02_b.png', FALSE),
(31, 8, 'C', 'Hình tròn sọc caro', 'iq_ans_02_c.png', FALSE),
(32, 8, 'D', 'Hình vuông có dấu cộng', 'iq_ans_02_d.png', FALSE);
