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


-- BỔ SUNG NGÂN HÀNG CÂU HỎI ĐẦY ĐỦ 4 CHỦ ĐỀ & 3 ĐỘ KHÓ (YÊU CẦU 4 & 12)
INSERT INTO `CAU_HOI` (`MaCauHoi`, `MaChuDe`, `LoaiCauHoi`, `DoKho`, `NoiDung`, `HinhAnh`, `GiaiThich`, `TrangThai`) VALUES (9, 1, 'STANDARD', 'EASY', 'Công thức hóa học của nước là gì?', NULL, 'Nước là hợp chất tạo bởi 2 nguyên tử Hidro và 1 nguyên tử Oxy (H2O).', 'ACTIVE') ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);
INSERT INTO `DAP_AN` (`MaDapAn`, `MaCauHoi`, `KyHieu`, `NoiDung`, `LaDapAnDung`) VALUES
(33, 9, 'A', 'H2O', TRUE),
(34, 9, 'B', 'CO2', FALSE),
(35, 9, 'C', 'NaCl', FALSE),
(36, 9, 'D', 'O2', FALSE)
ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);

INSERT INTO `CAU_HOI` (`MaCauHoi`, `MaChuDe`, `LoaiCauHoi`, `DoKho`, `NoiDung`, `HinhAnh`, `GiaiThich`, `TrangThai`) VALUES (10, 1, 'STANDARD', 'EASY', 'Hành tinh nào nằm gần Mặt Trời nhất trong Hệ Mặt Trời?', NULL, 'Sao Thủy (Mercury) là hành tinh gần Mặt Trời nhất.', 'ACTIVE') ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);
INSERT INTO `DAP_AN` (`MaDapAn`, `MaCauHoi`, `KyHieu`, `NoiDung`, `LaDapAnDung`) VALUES
(37, 10, 'A', 'Sao Kim', FALSE),
(38, 10, 'B', 'Sao Hỏa', FALSE),
(39, 10, 'C', 'Sao Thủy', TRUE),
(40, 10, 'D', 'Trái Đất', FALSE)
ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);

INSERT INTO `CAU_HOI` (`MaCauHoi`, `MaChuDe`, `LoaiCauHoi`, `DoKho`, `NoiDung`, `HinhAnh`, `GiaiThich`, `TrangThai`) VALUES (11, 1, 'STANDARD', 'EASY', 'Động vật nào sau đây là loài thú có vú duy nhất biết bay lượn?', NULL, 'Dơi là loài thú có vú duy nhất có cấu tạo cánh để bay.', 'ACTIVE') ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);
INSERT INTO `DAP_AN` (`MaDapAn`, `MaCauHoi`, `KyHieu`, `NoiDung`, `LaDapAnDung`) VALUES
(41, 11, 'A', 'Chim cánh cụt', FALSE),
(42, 11, 'B', 'Dơi', TRUE),
(43, 11, 'C', 'Đà điểu', FALSE),
(44, 11, 'D', 'Sóc bay', FALSE)
ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);

INSERT INTO `CAU_HOI` (`MaCauHoi`, `MaChuDe`, `LoaiCauHoi`, `DoKho`, `NoiDung`, `HinhAnh`, `GiaiThich`, `TrangThai`) VALUES (12, 1, 'STANDARD', 'EASY', 'Bộ phận nào của cây xanh chủ yếu thực hiện quá trình quang hợp?', NULL, 'Lá cây chứa nhiều lục lạp với chất diệp lục giúp hấp thụ ánh sáng mặt trời để quang hợp.', 'ACTIVE') ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);
INSERT INTO `DAP_AN` (`MaDapAn`, `MaCauHoi`, `KyHieu`, `NoiDung`, `LaDapAnDung`) VALUES
(45, 12, 'A', 'Rễ cây', FALSE),
(46, 12, 'B', 'Thân cây', FALSE),
(47, 12, 'C', 'Lá cây', TRUE),
(48, 12, 'D', 'Hoa', FALSE)
ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);

INSERT INTO `CAU_HOI` (`MaCauHoi`, `MaChuDe`, `LoaiCauHoi`, `DoKho`, `NoiDung`, `HinhAnh`, `GiaiThich`, `TrangThai`) VALUES (13, 1, 'STANDARD', 'EASY', 'Nhiệt độ sôi của nước tinh khiết ở điều kiện áp suất tiêu chuẩn là bao nhiêu?', NULL, 'Ở áp suất 1 atm tiêu chuẩn, nước nguyên chất sôi ở 100 độ C.', 'ACTIVE') ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);
INSERT INTO `DAP_AN` (`MaDapAn`, `MaCauHoi`, `KyHieu`, `NoiDung`, `LaDapAnDung`) VALUES
(49, 13, 'A', '50 độ C', FALSE),
(50, 13, 'B', '80 độ C', FALSE),
(51, 13, 'C', '100 độ C', TRUE),
(52, 13, 'D', '120 độ C', FALSE)
ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);

INSERT INTO `CAU_HOI` (`MaCauHoi`, `MaChuDe`, `LoaiCauHoi`, `DoKho`, `NoiDung`, `HinhAnh`, `GiaiThich`, `TrangThai`) VALUES (14, 1, 'STANDARD', 'MEDIUM', 'Cơ quan nào trong cơ thể người có chức năng lọc máu và tạo ra nước tiểu?', NULL, 'Thận là cơ quan thuộc hệ tiết niệu có chức năng lọc máu bài tiết các chất cặn bã.', 'ACTIVE') ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);
INSERT INTO `DAP_AN` (`MaDapAn`, `MaCauHoi`, `KyHieu`, `NoiDung`, `LaDapAnDung`) VALUES
(53, 14, 'A', 'Gan', FALSE),
(54, 14, 'B', 'Thận', TRUE),
(55, 14, 'C', 'Tim', FALSE),
(56, 14, 'D', 'Dạ dày', FALSE)
ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);

INSERT INTO `CAU_HOI` (`MaCauHoi`, `MaChuDe`, `LoaiCauHoi`, `DoKho`, `NoiDung`, `HinhAnh`, `GiaiThich`, `TrangThai`) VALUES (15, 1, 'STANDARD', 'MEDIUM', 'Kim loại duy nhất nào ở thể lỏng trong điều kiện nhiệt độ phòng bình thường?', NULL, 'Thủy ngân (Hg) là kim loại duy nhất tồn tại ở thể lỏng ở nhiệt độ thường.', 'ACTIVE') ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);
INSERT INTO `DAP_AN` (`MaDapAn`, `MaCauHoi`, `KyHieu`, `NoiDung`, `LaDapAnDung`) VALUES
(57, 15, 'A', 'Thủy ngân (Hg)', TRUE),
(58, 15, 'B', 'Sắt (Fe)', FALSE),
(59, 15, 'C', 'Nhôm (Al)', FALSE),
(60, 15, 'D', 'Vàng (Au)', FALSE)
ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);

INSERT INTO `CAU_HOI` (`MaCauHoi`, `MaChuDe`, `LoaiCauHoi`, `DoKho`, `NoiDung`, `HinhAnh`, `GiaiThich`, `TrangThai`) VALUES (16, 1, 'STANDARD', 'MEDIUM', 'Đơn vị đo công suất tiêu chuẩn trong hệ đo lường quốc tế (SI) là gì?', NULL, 'Watt (W) là đơn vị đo công suất trong hệ SI, đặt theo tên nhà phát minh James Watt.', 'ACTIVE') ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);
INSERT INTO `DAP_AN` (`MaDapAn`, `MaCauHoi`, `KyHieu`, `NoiDung`, `LaDapAnDung`) VALUES
(61, 16, 'A', 'Joule (J)', FALSE),
(62, 16, 'B', 'Watt (W)', TRUE),
(63, 16, 'C', 'Newton (N)', FALSE),
(64, 16, 'D', 'Pascal (Pa)', FALSE)
ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);

INSERT INTO `CAU_HOI` (`MaCauHoi`, `MaChuDe`, `LoaiCauHoi`, `DoKho`, `NoiDung`, `HinhAnh`, `GiaiThich`, `TrangThai`) VALUES (17, 1, 'STANDARD', 'HARD', 'Vận tốc của ánh sáng truyền trong môi trường chân không xấp xỉ bằng bao nhiêu?', NULL, 'Vận tốc ánh sáng trong chân không là c xấp xỉ bằng 300.000 km/s (chính xác là 299.792.458 m/s).', 'ACTIVE') ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);
INSERT INTO `DAP_AN` (`MaDapAn`, `MaCauHoi`, `KyHieu`, `NoiDung`, `LaDapAnDung`) VALUES
(65, 17, 'A', '150.000 km/s', FALSE),
(66, 17, 'B', '300.000 km/s', TRUE),
(67, 17, 'C', '450.000 km/s', FALSE),
(68, 17, 'D', '600.000 km/s', FALSE)
ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);

INSERT INTO `CAU_HOI` (`MaCauHoi`, `MaChuDe`, `LoaiCauHoi`, `DoKho`, `NoiDung`, `HinhAnh`, `GiaiThich`, `TrangThai`) VALUES (18, 1, 'STANDARD', 'HARD', 'Định luật vạn vật hấp dẫn được khám phá và công bố bởi nhà bác học vĩ đại nào?', NULL, 'Isaac Newton là người phát hiện và thiết lập định luật vạn vật hấp dẫn vào năm 1687.', 'ACTIVE') ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);
INSERT INTO `DAP_AN` (`MaDapAn`, `MaCauHoi`, `KyHieu`, `NoiDung`, `LaDapAnDung`) VALUES
(69, 18, 'A', 'Albert Einstein', FALSE),
(70, 18, 'B', 'Isaac Newton', TRUE),
(71, 18, 'C', 'Galileo Galilei', FALSE),
(72, 18, 'D', 'Nikola Tesla', FALSE)
ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);

INSERT INTO `CAU_HOI` (`MaCauHoi`, `MaChuDe`, `LoaiCauHoi`, `DoKho`, `NoiDung`, `HinhAnh`, `GiaiThich`, `TrangThai`) VALUES (19, 1, 'STANDARD', 'HARD', 'Nguyên tố kim loại nào có ký hiệu hóa học là Au trong bảng tuần hoàn Mendeleev?', NULL, 'Au là viết tắt của từ Aurum tiếng Latinh nghĩa là Vàng.', 'ACTIVE') ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);
INSERT INTO `DAP_AN` (`MaDapAn`, `MaCauHoi`, `KyHieu`, `NoiDung`, `LaDapAnDung`) VALUES
(73, 19, 'A', 'Bạc', FALSE),
(74, 19, 'B', 'Vàng', TRUE),
(75, 19, 'C', 'Đồng', FALSE),
(76, 19, 'D', 'Bạch kim', FALSE)
ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);

INSERT INTO `CAU_HOI` (`MaCauHoi`, `MaChuDe`, `LoaiCauHoi`, `DoKho`, `NoiDung`, `HinhAnh`, `GiaiThich`, `TrangThai`) VALUES (20, 1, 'STANDARD', 'HARD', 'Quá trình phân chia tế bào sinh dưỡng ở sinh vật nhân thực tạo ra 2 tế bào con giống hệt tế bào mẹ gọi là gì?', NULL, 'Nguyên phân (Mitosis) là hình thức phân bào sinh dưỡng giữ nguyên bộ nhiễm sắc thể 2n.', 'ACTIVE') ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);
INSERT INTO `DAP_AN` (`MaDapAn`, `MaCauHoi`, `KyHieu`, `NoiDung`, `LaDapAnDung`) VALUES
(77, 20, 'A', 'Giảm phân', FALSE),
(78, 20, 'B', 'Nguyên phân', TRUE),
(79, 20, 'C', 'Thụ tinh', FALSE),
(80, 20, 'D', 'Đột biến gene', FALSE)
ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);

INSERT INTO `CAU_HOI` (`MaCauHoi`, `MaChuDe`, `LoaiCauHoi`, `DoKho`, `NoiDung`, `HinhAnh`, `GiaiThich`, `TrangThai`) VALUES (21, 1, 'STANDARD', 'HARD', 'Khối lượng riêng của nước nguyên chất đạt giá trị cực đại ở nhiệt độ bao nhiêu?', NULL, 'Nước có hiện tượng dãn nở nghịch thường, đạt khối lượng riêng lớn nhất (1 g/cm3) ở 4 độ C.', 'ACTIVE') ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);
INSERT INTO `DAP_AN` (`MaDapAn`, `MaCauHoi`, `KyHieu`, `NoiDung`, `LaDapAnDung`) VALUES
(81, 21, 'A', '0 độ C', FALSE),
(82, 21, 'B', '4 độ C', TRUE),
(83, 21, 'C', '10 độ C', FALSE),
(84, 21, 'D', '100 độ C', FALSE)
ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);

INSERT INTO `CAU_HOI` (`MaCauHoi`, `MaChuDe`, `LoaiCauHoi`, `DoKho`, `NoiDung`, `HinhAnh`, `GiaiThich`, `TrangThai`) VALUES (22, 2, 'STANDARD', 'EASY', 'Thủ đô của nước Cộng hòa Xã hội Chủ nghĩa Việt Nam là thành phố nào?', NULL, 'Hà Nội là thủ đô nghìn năm văn hiến của nước CHXHCN Việt Nam.', 'ACTIVE') ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);
INSERT INTO `DAP_AN` (`MaDapAn`, `MaCauHoi`, `KyHieu`, `NoiDung`, `LaDapAnDung`) VALUES
(85, 22, 'A', 'Đà Nẵng', FALSE),
(86, 22, 'B', 'Hà Nội', TRUE),
(87, 22, 'C', 'TP. Hồ Chí Minh', FALSE),
(88, 22, 'D', 'Hải Phòng', FALSE)
ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);

INSERT INTO `CAU_HOI` (`MaCauHoi`, `MaChuDe`, `LoaiCauHoi`, `DoKho`, `NoiDung`, `HinhAnh`, `GiaiThich`, `TrangThai`) VALUES (23, 2, 'STANDARD', 'EASY', 'Đỉnh núi nào cao nhất thế giới và được mệnh danh là Nóc nhà của Trái Đất?', NULL, 'Đỉnh Everest thuộc dãy Himalaya với độ cao 8.848,86m là đỉnh núi cao nhất thế giới.', 'ACTIVE') ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);
INSERT INTO `DAP_AN` (`MaDapAn`, `MaCauHoi`, `KyHieu`, `NoiDung`, `LaDapAnDung`) VALUES
(89, 23, 'A', 'Phú Sĩ', FALSE),
(90, 23, 'B', 'Fansipan', FALSE),
(91, 23, 'C', 'Everest', TRUE),
(92, 23, 'D', 'K2', FALSE)
ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);

INSERT INTO `CAU_HOI` (`MaCauHoi`, `MaChuDe`, `LoaiCauHoi`, `DoKho`, `NoiDung`, `HinhAnh`, `GiaiThich`, `TrangThai`) VALUES (24, 2, 'STANDARD', 'EASY', 'Việt Nam nằm ở khu vực địa lý nào của châu Á?', NULL, 'Việt Nam nằm ở khu vực Đông Nam Á (Southeast Asia).', 'ACTIVE') ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);
INSERT INTO `DAP_AN` (`MaDapAn`, `MaCauHoi`, `KyHieu`, `NoiDung`, `LaDapAnDung`) VALUES
(93, 24, 'A', 'Đông Á', FALSE),
(94, 24, 'B', 'Nam Á', FALSE),
(95, 24, 'C', 'Đông Nam Á', TRUE),
(96, 24, 'D', 'Tây Á', FALSE)
ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);

INSERT INTO `CAU_HOI` (`MaCauHoi`, `MaChuDe`, `LoaiCauHoi`, `DoKho`, `NoiDung`, `HinhAnh`, `GiaiThich`, `TrangThai`) VALUES (25, 2, 'STANDARD', 'EASY', 'Quốc gia nào có diện tích lãnh thổ tự nhiên lớn nhất trên thế giới?', NULL, 'Nga là quốc gia rộng lớn nhất thế giới với diện tích hơn 17 triệu km2.', 'ACTIVE') ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);
INSERT INTO `DAP_AN` (`MaDapAn`, `MaCauHoi`, `KyHieu`, `NoiDung`, `LaDapAnDung`) VALUES
(97, 25, 'A', 'Canada', FALSE),
(98, 25, 'B', 'Hoa Kỳ', FALSE),
(99, 25, 'C', 'Nga', TRUE),
(100, 25, 'D', 'Trung Quốc', FALSE)
ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);

INSERT INTO `CAU_HOI` (`MaCauHoi`, `MaChuDe`, `LoaiCauHoi`, `DoKho`, `NoiDung`, `HinhAnh`, `GiaiThich`, `TrangThai`) VALUES (26, 2, 'STANDARD', 'EASY', 'Chủ tịch Hồ Chí Minh đọc bản Tuyên ngôn Độc lập tại Quảng trường Ba Đình vào ngày tháng năm nào?', NULL, 'Ngày 2 tháng 9 năm 1945, Bác Hồ đọc Tuyên ngôn Độc lập khai sinh nước Việt Nam Dân chủ Cộng hòa.', 'ACTIVE') ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);
INSERT INTO `DAP_AN` (`MaDapAn`, `MaCauHoi`, `KyHieu`, `NoiDung`, `LaDapAnDung`) VALUES
(101, 26, 'A', '30/04/1975', FALSE),
(102, 26, 'B', '02/09/1945', TRUE),
(103, 26, 'C', '19/08/1945', FALSE),
(104, 26, 'D', '07/05/1954', FALSE)
ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);

INSERT INTO `CAU_HOI` (`MaCauHoi`, `MaChuDe`, `LoaiCauHoi`, `DoKho`, `NoiDung`, `HinhAnh`, `GiaiThich`, `TrangThai`) VALUES (27, 2, 'STANDARD', 'MEDIUM', 'Con sông nào dài nhất thế giới chảy qua khu vực lục địa châu Phi?', NULL, 'Sông Nile với chiều dài khoảng 6.650 km là dòng sông dài nhất thế giới.', 'ACTIVE') ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);
INSERT INTO `DAP_AN` (`MaDapAn`, `MaCauHoi`, `KyHieu`, `NoiDung`, `LaDapAnDung`) VALUES
(105, 27, 'A', 'Sông Amazon', FALSE),
(106, 27, 'B', 'Sông Nile', TRUE),
(107, 27, 'C', 'Sông Mê Kông', FALSE),
(108, 27, 'D', 'Sông Dương Tử', FALSE)
ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);

INSERT INTO `CAU_HOI` (`MaCauHoi`, `MaChuDe`, `LoaiCauHoi`, `DoKho`, `NoiDung`, `HinhAnh`, `GiaiThich`, `TrangThai`) VALUES (28, 2, 'STANDARD', 'MEDIUM', 'Vị vua nào của triều Nguyễn đã sáng lập triều đại và quyết định định đô tại Huế?', NULL, 'Vua Gia Long (Nguyễn Ánh) lên ngôi năm 1802 và định đô tại Phú Xuân (Huế).', 'ACTIVE') ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);
INSERT INTO `DAP_AN` (`MaDapAn`, `MaCauHoi`, `KyHieu`, `NoiDung`, `LaDapAnDung`) VALUES
(109, 28, 'A', 'Vua Gia Long', TRUE),
(110, 28, 'B', 'Vua Minh Mạng', FALSE),
(111, 28, 'C', 'Vua Tự Đức', FALSE),
(112, 28, 'D', 'Vua Bảo Đại', FALSE)
ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);

INSERT INTO `CAU_HOI` (`MaCauHoi`, `MaChuDe`, `LoaiCauHoi`, `DoKho`, `NoiDung`, `HinhAnh`, `GiaiThich`, `TrangThai`) VALUES (29, 2, 'STANDARD', 'MEDIUM', 'Vịnh biển nào tại Việt Nam được UNESCO hai lần vinh danh là Di sản Thiên nhiên Thế giới?', NULL, 'Vịnh Hạ Long (Quảng Ninh) được UNESCO công nhận Di sản Thế giới vào năm 1994 và 2000.', 'ACTIVE') ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);
INSERT INTO `DAP_AN` (`MaDapAn`, `MaCauHoi`, `KyHieu`, `NoiDung`, `LaDapAnDung`) VALUES
(113, 29, 'A', 'Vịnh Cam Ranh', FALSE),
(114, 29, 'B', 'Vịnh Hạ Long', TRUE),
(115, 29, 'C', 'Vịnh Nha Trang', FALSE),
(116, 29, 'D', 'Vịnh Xuân Đài', FALSE)
ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);

INSERT INTO `CAU_HOI` (`MaCauHoi`, `MaChuDe`, `LoaiCauHoi`, `DoKho`, `NoiDung`, `HinhAnh`, `GiaiThich`, `TrangThai`) VALUES (30, 2, 'STANDARD', 'MEDIUM', 'Quần thể Kim tự tháp Giza và tượng Nhân sư nổi tiếng thuộc quốc gia nào?', NULL, 'Kim tự tháp Giza là kỳ quan cổ đại nổi tiếng nằm ở Ai Cập (Egypt).', 'ACTIVE') ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);
INSERT INTO `DAP_AN` (`MaDapAn`, `MaCauHoi`, `KyHieu`, `NoiDung`, `LaDapAnDung`) VALUES
(117, 30, 'A', 'Hy Lạp', FALSE),
(118, 30, 'B', 'Ý', FALSE),
(119, 30, 'C', 'Ai Cập', TRUE),
(120, 30, 'D', 'Thổ Nhĩ Kỳ', FALSE)
ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);

INSERT INTO `CAU_HOI` (`MaCauHoi`, `MaChuDe`, `LoaiCauHoi`, `DoKho`, `NoiDung`, `HinhAnh`, `GiaiThich`, `TrangThai`) VALUES (31, 2, 'STANDARD', 'HARD', 'Chiến tranh thế giới thứ hai (World War II) chính thức kết thúc vào năm nào?', NULL, 'Chiến tranh thế giới thứ hai chính thức kết thúc vào năm 1945 sau khi phát xít Nhật đầu hàng.', 'ACTIVE') ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);
INSERT INTO `DAP_AN` (`MaDapAn`, `MaCauHoi`, `KyHieu`, `NoiDung`, `LaDapAnDung`) VALUES
(121, 31, 'A', '1939', FALSE),
(122, 31, 'B', '1941', FALSE),
(123, 31, 'C', '1945', TRUE),
(124, 31, 'D', '1950', FALSE)
ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);

INSERT INTO `CAU_HOI` (`MaCauHoi`, `MaChuDe`, `LoaiCauHoi`, `DoKho`, `NoiDung`, `HinhAnh`, `GiaiThich`, `TrangThai`) VALUES (32, 2, 'STANDARD', 'HARD', 'Hiệp định Genève về chấm dứt chiến sự và lập lại hòa bình ở Đông Dương được ký kết năm nào?', NULL, 'Hiệp định Genève được ký kết vào ngày 21/7/1954 sau thắng lợi Điện Biên Phủ.', 'ACTIVE') ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);
INSERT INTO `DAP_AN` (`MaDapAn`, `MaCauHoi`, `KyHieu`, `NoiDung`, `LaDapAnDung`) VALUES
(125, 32, 'A', '1946', FALSE),
(126, 32, 'B', '1954', TRUE),
(127, 32, 'C', '1968', FALSE),
(128, 32, 'D', '1973', FALSE)
ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);

INSERT INTO `CAU_HOI` (`MaCauHoi`, `MaChuDe`, `LoaiCauHoi`, `DoKho`, `NoiDung`, `HinhAnh`, `GiaiThich`, `TrangThai`) VALUES (33, 2, 'STANDARD', 'HARD', 'Hoang mạc cát nóng nào có diện tích lớn nhất trên thế giới?', NULL, 'Sahara là hoang mạc nóng lớn nhất thế giới bao phủ phần lớn Bắc Phi với hơn 9 triệu km2.', 'ACTIVE') ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);
INSERT INTO `DAP_AN` (`MaDapAn`, `MaCauHoi`, `KyHieu`, `NoiDung`, `LaDapAnDung`) VALUES
(129, 33, 'A', 'Hoang mạc Gobi', FALSE),
(130, 33, 'B', 'Hoang mạc Sahara', TRUE),
(131, 33, 'C', 'Hoang mạc Kalahari', FALSE),
(132, 33, 'D', 'Hoang mạc Atacama', FALSE)
ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);

INSERT INTO `CAU_HOI` (`MaCauHoi`, `MaChuDe`, `LoaiCauHoi`, `DoKho`, `NoiDung`, `HinhAnh`, `GiaiThich`, `TrangThai`) VALUES (34, 2, 'STANDARD', 'HARD', 'Kênh đào Panama nhân tạo nối liền hai đại dương lớn nào trên Trái Đất?', NULL, 'Kênh đào Panama cắt ngang eo đất Panama nối liền Đại Tây Dương và Thái Bình Dương.', 'ACTIVE') ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);
INSERT INTO `DAP_AN` (`MaDapAn`, `MaCauHoi`, `KyHieu`, `NoiDung`, `LaDapAnDung`) VALUES
(133, 34, 'A', 'Đại Tây Dương và Thái Bình Dương', TRUE),
(134, 34, 'B', 'Thái Bình Dương và Ấn Độ Dương', FALSE),
(135, 34, 'C', 'Bắc Băng Dương và Đại Tây Dương', FALSE),
(136, 34, 'D', 'Ấn Độ Dương và Địa Trung Hải', FALSE)
ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);

INSERT INTO `CAU_HOI` (`MaCauHoi`, `MaChuDe`, `LoaiCauHoi`, `DoKho`, `NoiDung`, `HinhAnh`, `GiaiThich`, `TrangThai`) VALUES (35, 3, 'STANDARD', 'EASY', 'CPU trong máy tính là từ viết tắt của thuật ngữ tiếng Anh nào?', NULL, 'CPU là viết tắt của Central Processing Unit (Bộ xử lý trung tâm).', 'ACTIVE') ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);
INSERT INTO `DAP_AN` (`MaDapAn`, `MaCauHoi`, `KyHieu`, `NoiDung`, `LaDapAnDung`) VALUES
(137, 35, 'A', 'Central Processing Unit', TRUE),
(138, 35, 'B', 'Computer Power Unit', FALSE),
(139, 35, 'C', 'Control Program Unit', FALSE),
(140, 35, 'D', 'Core Processor Universal', FALSE)
ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);

INSERT INTO `CAU_HOI` (`MaCauHoi`, `MaChuDe`, `LoaiCauHoi`, `DoKho`, `NoiDung`, `HinhAnh`, `GiaiThich`, `TrangThai`) VALUES (36, 3, 'STANDARD', 'EASY', 'Thiết bị ngoại vi nào sau đây dùng để nhập dữ liệu văn bản vào máy tính?', NULL, 'Bàn phím (Keyboard) là thiết bị nhập liệu văn bản cơ bản nhất.', 'ACTIVE') ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);
INSERT INTO `DAP_AN` (`MaDapAn`, `MaCauHoi`, `KyHieu`, `NoiDung`, `LaDapAnDung`) VALUES
(141, 36, 'A', 'Màn hình', FALSE),
(142, 36, 'B', 'Bàn phím', TRUE),
(143, 36, 'C', 'Loa âm thanh', FALSE),
(144, 36, 'D', 'Máy in', FALSE)
ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);

INSERT INTO `CAU_HOI` (`MaCauHoi`, `MaChuDe`, `LoaiCauHoi`, `DoKho`, `NoiDung`, `HinhAnh`, `GiaiThich`, `TrangThai`) VALUES (37, 3, 'STANDARD', 'EASY', 'Đơn vị nhỏ nhất dùng để đo lường dung lượng thông tin số trong máy tính là gì?', NULL, 'Bit (viết tắt của Binary Digit) có giá trị 0 hoặc 1 là đơn vị thông tin nhỏ nhất.', 'ACTIVE') ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);
INSERT INTO `DAP_AN` (`MaDapAn`, `MaCauHoi`, `KyHieu`, `NoiDung`, `LaDapAnDung`) VALUES
(145, 37, 'A', 'Byte', FALSE),
(146, 37, 'B', 'Bit', TRUE),
(147, 37, 'C', 'Kilobyte', FALSE),
(148, 37, 'D', 'Megabyte', FALSE)
ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);

INSERT INTO `CAU_HOI` (`MaCauHoi`, `MaChuDe`, `LoaiCauHoi`, `DoKho`, `NoiDung`, `HinhAnh`, `GiaiThich`, `TrangThai`) VALUES (38, 3, 'STANDARD', 'MEDIUM', 'Hệ thống phân giải tên miền DNS (Domain Name System) giữ vai trò chủ chốt nào trên mạng Internet?', NULL, 'DNS đóng vai trò như danh bạ Internet, chuyển đổi tên miền (quizzlive.com) thành địa chỉ IP máy chủ.', 'ACTIVE') ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);
INSERT INTO `DAP_AN` (`MaDapAn`, `MaCauHoi`, `KyHieu`, `NoiDung`, `LaDapAnDung`) VALUES
(149, 38, 'A', 'Mã hóa dữ liệu truyền tải', FALSE),
(150, 38, 'B', 'Phân giải tên miền thành địa chỉ IP', TRUE),
(151, 38, 'C', 'Cấp phát địa chỉ IP động cho máy trạm', FALSE),
(152, 38, 'D', 'Kiểm soát hàng đợi gửi email', FALSE)
ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);

INSERT INTO `CAU_HOI` (`MaCauHoi`, `MaChuDe`, `LoaiCauHoi`, `DoKho`, `NoiDung`, `HinhAnh`, `GiaiThich`, `TrangThai`) VALUES (39, 3, 'STANDARD', 'MEDIUM', 'Bộ nhớ RAM (Random Access Memory) trong kiến trúc máy tính có đặc điểm cơ bản nào?', NULL, 'RAM là bộ nhớ khả biến (volatile), dữ liệu sẽ mất hoàn toàn khi tắt máy hoặc ngắt nguồn điện.', 'ACTIVE') ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);
INSERT INTO `DAP_AN` (`MaDapAn`, `MaCauHoi`, `KyHieu`, `NoiDung`, `LaDapAnDung`) VALUES
(153, 39, 'A', 'Lưu trữ vĩnh viễn không mất dữ liệu', FALSE),
(154, 39, 'B', 'Mất toàn bộ dữ liệu khi ngắt nguồn điện', TRUE),
(155, 39, 'C', 'Chỉ đọc dữ liệu và không thể ghi', FALSE),
(156, 39, 'D', 'Tốc độ truy xuất chậm hơn ổ cứng từ HDD', FALSE)
ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);

INSERT INTO `CAU_HOI` (`MaCauHoi`, `MaChuDe`, `LoaiCauHoi`, `DoKho`, `NoiDung`, `HinhAnh`, `GiaiThich`, `TrangThai`) VALUES (40, 3, 'STANDARD', 'MEDIUM', 'Cổng (port) dịch vụ chuẩn mặc định của giao thức web bảo mật HTTPS là bao nhiêu?', NULL, 'HTTPS chạy trên cổng chuẩn 443, còn HTTP không mã hóa dùng cổng 80.', 'ACTIVE') ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);
INSERT INTO `DAP_AN` (`MaDapAn`, `MaCauHoi`, `KyHieu`, `NoiDung`, `LaDapAnDung`) VALUES
(157, 40, 'A', '80', FALSE),
(158, 40, 'B', '21', FALSE),
(159, 40, 'C', '443', TRUE),
(160, 40, 'D', '8080', FALSE)
ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);

INSERT INTO `CAU_HOI` (`MaCauHoi`, `MaChuDe`, `LoaiCauHoi`, `DoKho`, `NoiDung`, `HinhAnh`, `GiaiThich`, `TrangThai`) VALUES (41, 3, 'STANDARD', 'MEDIUM', 'Ngôn ngữ lập trình nào với slogan Write Once, Run Anywhere được phát triển bởi Sun Microsystems năm 1995?', NULL, 'Java do James Gosling cùng cộng sự phát triển tại Sun Microsystems vào năm 1995.', 'ACTIVE') ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);
INSERT INTO `DAP_AN` (`MaDapAn`, `MaCauHoi`, `KyHieu`, `NoiDung`, `LaDapAnDung`) VALUES
(161, 41, 'A', 'Python', FALSE),
(162, 41, 'B', 'Java', TRUE),
(163, 41, 'C', 'C++', FALSE),
(164, 41, 'D', 'C#', FALSE)
ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);

INSERT INTO `CAU_HOI` (`MaCauHoi`, `MaChuDe`, `LoaiCauHoi`, `DoKho`, `NoiDung`, `HinhAnh`, `GiaiThich`, `TrangThai`) VALUES (42, 3, 'STANDARD', 'MEDIUM', 'Địa chỉ giao thức Internet phiên bản 4 (IPv4) có tổng độ dài bao nhiêu bit?', NULL, 'Địa chỉ IPv4 gồm 32 bit, thường biểu diễn dưới dạng 4 số thập phân cách nhau bởi dấu chấm.', 'ACTIVE') ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);
INSERT INTO `DAP_AN` (`MaDapAn`, `MaCauHoi`, `KyHieu`, `NoiDung`, `LaDapAnDung`) VALUES
(165, 42, 'A', '16 bit', FALSE),
(166, 42, 'B', '32 bit', TRUE),
(167, 42, 'C', '64 bit', FALSE),
(168, 42, 'D', '128 bit', FALSE)
ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);

INSERT INTO `CAU_HOI` (`MaCauHoi`, `MaChuDe`, `LoaiCauHoi`, `DoKho`, `NoiDung`, `HinhAnh`, `GiaiThich`, `TrangThai`) VALUES (43, 3, 'STANDARD', 'HARD', 'Mô hình chuẩn kết nối các hệ thống mở OSI (Open Systems Interconnection) gồm có bao nhiêu tầng?', NULL, 'Mô hình OSI gồm 7 tầng: Vật lý, Liên kết dữ liệu, Mạng, Giao vận, Phiên, Trình diễn, Ứng dụng.', 'ACTIVE') ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);
INSERT INTO `DAP_AN` (`MaDapAn`, `MaCauHoi`, `KyHieu`, `NoiDung`, `LaDapAnDung`) VALUES
(169, 43, 'A', '4 tầng', FALSE),
(170, 43, 'B', '5 tầng', FALSE),
(171, 43, 'C', '7 tầng', TRUE),
(172, 43, 'D', '9 tầng', FALSE)
ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);

INSERT INTO `CAU_HOI` (`MaCauHoi`, `MaChuDe`, `LoaiCauHoi`, `DoKho`, `NoiDung`, `HinhAnh`, `GiaiThich`, `TrangThai`) VALUES (44, 3, 'STANDARD', 'HARD', 'Thuật toán sắp xếp nhanh QuickSort có độ phức tạp thời gian trung bình (Average Case) là bao nhiêu?', NULL, 'QuickSort có thời gian chạy trung bình là O(n log n) với phân hoạch chia để trị hiệu quả.', 'ACTIVE') ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);
INSERT INTO `DAP_AN` (`MaDapAn`, `MaCauHoi`, `KyHieu`, `NoiDung`, `LaDapAnDung`) VALUES
(173, 44, 'A', 'O(n)', FALSE),
(174, 44, 'B', 'O(n log n)', TRUE),
(175, 44, 'C', 'O(n^2)', FALSE),
(176, 44, 'D', 'O(log n)', FALSE)
ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);

INSERT INTO `CAU_HOI` (`MaCauHoi`, `MaChuDe`, `LoaiCauHoi`, `DoKho`, `NoiDung`, `HinhAnh`, `GiaiThich`, `TrangThai`) VALUES (45, 3, 'STANDARD', 'HARD', 'Giao thức nào ở tầng Network chịu trách nhiệm báo lỗi và kiểm tra kết nối thông qua lệnh PING?', NULL, 'ICMP (Internet Control Message Protocol) được dùng để gửi thông điệp điều khiển và chẩn đoán mạng (Echo Request/Reply).', 'ACTIVE') ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);
INSERT INTO `DAP_AN` (`MaDapAn`, `MaCauHoi`, `KyHieu`, `NoiDung`, `LaDapAnDung`) VALUES
(177, 45, 'A', 'ARP', FALSE),
(178, 45, 'B', 'ICMP', TRUE),
(179, 45, 'C', 'IGMP', FALSE),
(180, 45, 'D', 'RARP', FALSE)
ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);

INSERT INTO `CAU_HOI` (`MaCauHoi`, `MaChuDe`, `LoaiCauHoi`, `DoKho`, `NoiDung`, `HinhAnh`, `GiaiThich`, `TrangThai`) VALUES (46, 3, 'STANDARD', 'HARD', 'Trong kiến trúc CPU Von Neumann, khối chức năng nào chịu trách nhiệm thực hiện các phép toán số học và logic?', NULL, 'ALU (Arithmetic Logic Unit) là đơn vị số học và logic của vi xử lý.', 'ACTIVE') ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);
INSERT INTO `DAP_AN` (`MaDapAn`, `MaCauHoi`, `KyHieu`, `NoiDung`, `LaDapAnDung`) VALUES
(181, 46, 'A', 'Thanh ghi Register', FALSE),
(182, 46, 'B', 'Khối điều khiển CU', FALSE),
(183, 46, 'C', 'Khối số học logic ALU', TRUE),
(184, 46, 'D', 'Bus hệ thống', FALSE)
ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);

INSERT INTO `CAU_HOI` (`MaCauHoi`, `MaChuDe`, `LoaiCauHoi`, `DoKho`, `NoiDung`, `HinhAnh`, `GiaiThich`, `TrangThai`) VALUES (47, 3, 'STANDARD', 'HARD', 'Giao thức bảo mật TLS/SSL sử dụng mật mã khóa công khai bất đối xứng chủ yếu cho mục đích nào?', NULL, 'TLS dùng mật mã bất đối xứng (như RSA/ECDH) ở bước Handshake để xác thực và trao đổi khóa đối xứng phiên.', 'ACTIVE') ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);
INSERT INTO `DAP_AN` (`MaDapAn`, `MaCauHoi`, `KyHieu`, `NoiDung`, `LaDapAnDung`) VALUES
(185, 47, 'A', 'Bắt tay (Handshake) xác thực và trao đổi khóa phiên', TRUE),
(186, 47, 'B', 'Mã hóa toàn bộ khối lượng dữ liệu truyền qua mạng', FALSE),
(187, 47, 'C', 'Kiểm tra chẵn lẻ kiểm soát lỗi luồng tin', FALSE),
(188, 47, 'D', 'Nén nội dung file truyền tải để tăng tốc độ', FALSE)
ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);

INSERT INTO `CAU_HOI` (`MaCauHoi`, `MaChuDe`, `LoaiCauHoi`, `DoKho`, `NoiDung`, `HinhAnh`, `GiaiThich`, `TrangThai`) VALUES (48, 4, 'STANDARD', 'EASY', 'Tìm số tiếp theo trong dãy số chẵn tăng dần: 2, 4, 6, 8, ...?', NULL, 'Dãy số chẵn liên tiếp, số tiếp theo sau 8 là 10.', 'ACTIVE') ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);
INSERT INTO `DAP_AN` (`MaDapAn`, `MaCauHoi`, `KyHieu`, `NoiDung`, `LaDapAnDung`) VALUES
(189, 48, 'A', '9', FALSE),
(190, 48, 'B', '10', TRUE),
(191, 48, 'C', '12', FALSE),
(192, 48, 'D', '14', FALSE)
ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);

INSERT INTO `CAU_HOI` (`MaCauHoi`, `MaChuDe`, `LoaiCauHoi`, `DoKho`, `NoiDung`, `HinhAnh`, `GiaiThich`, `TrangThai`) VALUES (49, 4, 'STANDARD', 'EASY', 'Một con vịt đi trước hai con vịt, một con vịt đi sau hai con vịt, một con vịt đi giữa hai con vịt. Hỏi có bao nhiêu con vịt?', NULL, 'Ba con vịt xếp thành 1 hàng dọc thỏa mãn mọi dữ kiện đề bài.', 'ACTIVE') ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);
INSERT INTO `DAP_AN` (`MaDapAn`, `MaCauHoi`, `KyHieu`, `NoiDung`, `LaDapAnDung`) VALUES
(193, 49, 'A', '3 con vịt', TRUE),
(194, 49, 'B', '4 con vịt', FALSE),
(195, 49, 'C', '5 con vịt', FALSE),
(196, 49, 'D', '6 con vịt', FALSE)
ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);

INSERT INTO `CAU_HOI` (`MaCauHoi`, `MaChuDe`, `LoaiCauHoi`, `DoKho`, `NoiDung`, `HinhAnh`, `GiaiThich`, `TrangThai`) VALUES (50, 4, 'STANDARD', 'EASY', 'Nếu ngày hôm qua là thứ Năm, thì ngày mai sẽ là thứ mấy?', NULL, 'Hôm qua là thứ Năm thì hôm nay là thứ Sáu, ngày mai sẽ là thứ Bảy.', 'ACTIVE') ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);
INSERT INTO `DAP_AN` (`MaDapAn`, `MaCauHoi`, `KyHieu`, `NoiDung`, `LaDapAnDung`) VALUES
(197, 50, 'A', 'Thứ Sáu', FALSE),
(198, 50, 'B', 'Thứ Bảy', TRUE),
(199, 50, 'C', 'Chủ Nhật', FALSE),
(200, 50, 'D', 'Thứ Hai', FALSE)
ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);

INSERT INTO `CAU_HOI` (`MaCauHoi`, `MaChuDe`, `LoaiCauHoi`, `DoKho`, `NoiDung`, `HinhAnh`, `GiaiThich`, `TrangThai`) VALUES (51, 4, 'STANDARD', 'EASY', 'Trong 4 từ sau đây, từ nào KHÔNG cùng nhóm phân loại với 3 từ còn lại?', NULL, 'Hình tròn, hình vuông, hình tam giác là các hình học, còn Màu đỏ là tên màu sắc.', 'ACTIVE') ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);
INSERT INTO `DAP_AN` (`MaDapAn`, `MaCauHoi`, `KyHieu`, `NoiDung`, `LaDapAnDung`) VALUES
(201, 51, 'A', 'Hình tròn', FALSE),
(202, 51, 'B', 'Hình vuông', FALSE),
(203, 51, 'C', 'Hình tam giác', FALSE),
(204, 51, 'D', 'Màu đỏ', TRUE)
ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);

INSERT INTO `CAU_HOI` (`MaCauHoi`, `MaChuDe`, `LoaiCauHoi`, `DoKho`, `NoiDung`, `HinhAnh`, `GiaiThich`, `TrangThai`) VALUES (52, 4, 'STANDARD', 'EASY', 'Tìm số tiếp theo trong dãy số cộng 5: 5, 10, 15, 20, ...?', NULL, 'Mỗi số cách nhau 5 đơn vị: 20 + 5 = 25.', 'ACTIVE') ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);
INSERT INTO `DAP_AN` (`MaDapAn`, `MaCauHoi`, `KyHieu`, `NoiDung`, `LaDapAnDung`) VALUES
(205, 52, 'A', '22', FALSE),
(206, 52, 'B', '25', TRUE),
(207, 52, 'C', '30', FALSE),
(208, 52, 'D', '35', FALSE)
ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);

INSERT INTO `CAU_HOI` (`MaCauHoi`, `MaChuDe`, `LoaiCauHoi`, `DoKho`, `NoiDung`, `HinhAnh`, `GiaiThich`, `TrangThai`) VALUES (53, 4, 'STANDARD', 'MEDIUM', 'Tìm số tiếp theo trong dãy số chính phương: 1, 4, 9, 16, 25, ...?', NULL, 'Dãy bình phương: 1^2, 2^2, 3^2, 4^2, 5^2, tiếp theo là 6^2 = 36.', 'ACTIVE') ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);
INSERT INTO `DAP_AN` (`MaDapAn`, `MaCauHoi`, `KyHieu`, `NoiDung`, `LaDapAnDung`) VALUES
(209, 53, 'A', '30', FALSE),
(210, 53, 'B', '36', TRUE),
(211, 53, 'C', '49', FALSE),
(212, 53, 'D', '64', FALSE)
ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);

INSERT INTO `CAU_HOI` (`MaCauHoi`, `MaChuDe`, `LoaiCauHoi`, `DoKho`, `NoiDung`, `HinhAnh`, `GiaiThich`, `TrangThai`) VALUES (54, 4, 'STANDARD', 'MEDIUM', 'Dãy số Fibonacci: 1, 1, 2, 3, 5, 8, ... Số tiếp theo là số nào?', NULL, 'Mỗi số bằng tổng 2 số liền trước: 5 + 8 = 13.', 'ACTIVE') ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);
INSERT INTO `DAP_AN` (`MaDapAn`, `MaCauHoi`, `KyHieu`, `NoiDung`, `LaDapAnDung`) VALUES
(213, 54, 'A', '11', FALSE),
(214, 54, 'B', '12', FALSE),
(215, 54, 'C', '13', TRUE),
(216, 54, 'D', '15', FALSE)
ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);

INSERT INTO `CAU_HOI` (`MaCauHoi`, `MaChuDe`, `LoaiCauHoi`, `DoKho`, `NoiDung`, `HinhAnh`, `GiaiThich`, `TrangThai`) VALUES (55, 4, 'STANDARD', 'MEDIUM', 'Nếu tất cả loài cá đều sống dưới nước, và cá heo là loài cá (theo logic giả định), thì kết luận nào đúng?', NULL, 'Theo tam đoạn luận diễn dịch, cá heo phải sống dưới nước.', 'ACTIVE') ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);
INSERT INTO `DAP_AN` (`MaDapAn`, `MaCauHoi`, `KyHieu`, `NoiDung`, `LaDapAnDung`) VALUES
(217, 55, 'A', 'Cá heo có thể sống trên cạn', FALSE),
(218, 55, 'B', 'Cá heo sống dưới nước', TRUE),
(219, 55, 'C', 'Cá heo không biết bơi', FALSE),
(220, 55, 'D', 'Không thể suy luận được', FALSE)
ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);

INSERT INTO `CAU_HOI` (`MaCauHoi`, `MaChuDe`, `LoaiCauHoi`, `DoKho`, `NoiDung`, `HinhAnh`, `GiaiThich`, `TrangThai`) VALUES (56, 4, 'STANDARD', 'MEDIUM', 'Vào lúc đồng hồ chỉ đúng 3 giờ 00 phút, góc tạo bởi kim giờ và kim phút bằng bao nhiêu độ?', NULL, 'Kim phút ở số 12, kim giờ ở số 3, góc tạo thành là đúng 1/4 hình tròn = 90 độ.', 'ACTIVE') ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);
INSERT INTO `DAP_AN` (`MaDapAn`, `MaCauHoi`, `KyHieu`, `NoiDung`, `LaDapAnDung`) VALUES
(221, 56, 'A', '45 độ', FALSE),
(222, 56, 'B', '60 độ', FALSE),
(223, 56, 'C', '90 độ', TRUE),
(224, 56, 'D', '120 độ', FALSE)
ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);

INSERT INTO `CAU_HOI` (`MaCauHoi`, `MaChuDe`, `LoaiCauHoi`, `DoKho`, `NoiDung`, `HinhAnh`, `GiaiThich`, `TrangThai`) VALUES (57, 4, 'STANDARD', 'MEDIUM', 'Một bể nước 100 lít có vòi bơm vào 10 lít/phút và vòi xả ra 5 lít/phút. Hỏi sau bao lâu bể đầy nước?', NULL, 'Tốc độ tăng thực tế là 10 - 5 = 5 lít/phút. Để đầy 100 lít cần: 100 / 5 = 20 phút.', 'ACTIVE') ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);
INSERT INTO `DAP_AN` (`MaDapAn`, `MaCauHoi`, `KyHieu`, `NoiDung`, `LaDapAnDung`) VALUES
(225, 57, 'A', '10 phút', FALSE),
(226, 57, 'B', '15 phút', FALSE),
(227, 57, 'C', '20 phút', TRUE),
(228, 57, 'D', '25 phút', FALSE)
ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);

INSERT INTO `CAU_HOI` (`MaCauHoi`, `MaChuDe`, `LoaiCauHoi`, `DoKho`, `NoiDung`, `HinhAnh`, `GiaiThich`, `TrangThai`) VALUES (58, 4, 'STANDARD', 'HARD', 'Tìm số còn thiếu theo quy luật tích hai số liên tiếp: 2, 6, 12, 20, 30, ...?', NULL, 'Quy luật: 1*2=2, 2*3=6, 3*4=12, 4*5=20, 5*6=30, tiếp theo là 6*7 = 42.', 'ACTIVE') ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);
INSERT INTO `DAP_AN` (`MaDapAn`, `MaCauHoi`, `KyHieu`, `NoiDung`, `LaDapAnDung`) VALUES
(229, 58, 'A', '40', FALSE),
(230, 58, 'B', '42', TRUE),
(231, 58, 'C', '44', FALSE),
(232, 58, 'D', '48', FALSE)
ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);

INSERT INTO `CAU_HOI` (`MaCauHoi`, `MaChuDe`, `LoaiCauHoi`, `DoKho`, `NoiDung`, `HinhAnh`, `GiaiThich`, `TrangThai`) VALUES (59, 4, 'STANDARD', 'HARD', 'Trong một cuộc chạy đua, nếu bạn bứt phá vượt qua người đang chạy ở vị trí thứ nhì, thì hiện tại bạn đang ở vị trí thứ mấy?', NULL, 'Bạn thế chỗ người thứ nhì nên hiện tại bạn đang ở vị trí thứ nhì (người dẫn đầu vẫn đứng thứ nhất).', 'ACTIVE') ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);
INSERT INTO `DAP_AN` (`MaDapAn`, `MaCauHoi`, `KyHieu`, `NoiDung`, `LaDapAnDung`) VALUES
(233, 59, 'A', 'Vị trí thứ nhất', FALSE),
(234, 59, 'B', 'Vị trí thứ nhì', TRUE),
(235, 59, 'C', 'Vị trí thứ ba', FALSE),
(236, 59, 'D', 'Vị trí cuối cùng', FALSE)
ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);

INSERT INTO `CAU_HOI` (`MaCauHoi`, `MaChuDe`, `LoaiCauHoi`, `DoKho`, `NoiDung`, `HinhAnh`, `GiaiThich`, `TrangThai`) VALUES (60, 4, 'STANDARD', 'HARD', 'Tìm số tiếp theo trong dãy số: 3, 5, 9, 17, 33, ...?', NULL, 'Khoảng cách giữa các số tăng gấp đôi: +2, +4, +8, +16, tiếp theo là +32: 33 + 32 = 65.', 'ACTIVE') ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);
INSERT INTO `DAP_AN` (`MaDapAn`, `MaCauHoi`, `KyHieu`, `NoiDung`, `LaDapAnDung`) VALUES
(237, 60, 'A', '55', FALSE),
(238, 60, 'B', '65', TRUE),
(239, 60, 'C', '66', FALSE),
(240, 60, 'D', '70', FALSE)
ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);

INSERT INTO `CAU_HOI` (`MaCauHoi`, `MaChuDe`, `LoaiCauHoi`, `DoKho`, `NoiDung`, `HinhAnh`, `GiaiThich`, `TrangThai`) VALUES (61, 4, 'STANDARD', 'HARD', 'Có 9 đồng tiền vàng giống nhau, trong đó có 1 đồng tiền giả nhẹ hơn. Dùng cân đĩa tối thiểu mấy lần để chắc chắn tìm ra?', NULL, 'Chia làm 3 nhóm (3, 3, 3). Cân lần 1 tìm ra nhóm chứa đồng giả. Chia nhóm 3 thành (1, 1, 1), cân lần 2 tìm ra đồng giả. Tối thiểu 2 lần.', 'ACTIVE') ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);
INSERT INTO `DAP_AN` (`MaDapAn`, `MaCauHoi`, `KyHieu`, `NoiDung`, `LaDapAnDung`) VALUES
(241, 61, 'A', '1 lần', FALSE),
(242, 61, 'B', '2 lần', TRUE),
(243, 61, 'C', '3 lần', FALSE),
(244, 61, 'D', '4 lần', FALSE)
ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);

INSERT INTO `CAU_HOI` (`MaCauHoi`, `MaChuDe`, `LoaiCauHoi`, `DoKho`, `NoiDung`, `HinhAnh`, `GiaiThich`, `TrangThai`) VALUES (62, 4, 'STANDARD', 'HARD', 'Người nông dân qua sông với sói, dê, bắp cải trên thuyền chỉ chở được 1 thứ. Sói ăn dê, dê ăn bắp cải nếu không có người. Thứ nào BẮT BUỘC mang qua sông đầu tiên?', NULL, 'Bắt buộc mang Dê qua trước, vì nếu mang Sói thì Dê ăn Bắp cải, nếu mang Bắp cải thì Sói ăn Dê.', 'ACTIVE') ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);
INSERT INTO `DAP_AN` (`MaDapAn`, `MaCauHoi`, `KyHieu`, `NoiDung`, `LaDapAnDung`) VALUES
(245, 62, 'A', 'Con sói', FALSE),
(246, 62, 'B', 'Con dê', TRUE),
(247, 62, 'C', 'Bắp cải', FALSE),
(248, 62, 'D', 'Để sói và dê ở bờ cũ', FALSE)
ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);



-- 6. Lịch sử trận đấu mẫu (Yêu cầu 10)
INSERT INTO `TRAN_DAU` (`MaTran`, `MaTranCode`, `LoaiTran`, `MaChuDe`, `DoKho`, `TrangThai`, `ThoiGianBatDau`, `ThoiGianKetThuc`, `NguoiThang`) VALUES
(1, 'MATCH_1001', 'RANKED', 1, 'EASY', 'FINISHED', '2026-11-06 14:00:00', '2026-11-06 14:02:15', 2),
(2, 'MATCH_1002', 'RANKED', 2, 'MEDIUM', 'FINISHED', '2026-11-06 15:30:00', '2026-11-06 15:32:10', 1),
(3, 'MATCH_1003', 'RANKED', 3, 'MEDIUM', 'FINISHED', '2026-11-06 16:45:00', '2026-11-06 16:47:05', 1),
(4, 'MATCH_1004', 'CHALLENGE', 4, 'HARD', 'FINISHED', '2026-11-06 18:10:00', '2026-11-06 18:12:30', 2),
(5, 'MATCH_1005', 'RANKED', 3, 'HARD', 'FINISHED', '2026-11-06 20:00:00', '2026-11-06 20:02:40', 3)
ON DUPLICATE KEY UPDATE `TrangThai` = VALUES(`TrangThai`);

INSERT INTO `NGUOI_CHOI_TRAN` (`MaTran`, `MaNguoiDung`, `ViTri`, `Diem`, `SoCauDung`, `SoCauSai`, `SoCauKhongTraLoi`, `XepHang`, `ThoiGianThamGia`, `ThoiGianHoanThanh`) VALUES
(1, 1, 1, 320, 3, 2, 0, 2, '2026-11-06 14:00:00', '2026-11-06 14:02:15'),
(1, 2, 2, 480, 5, 0, 0, 1, '2026-11-06 14:00:00', '2026-11-06 14:02:15'),
(2, 1, 1, 450, 4, 1, 0, 1, '2026-11-06 15:30:00', '2026-11-06 15:32:10'),
(2, 3, 2, 250, 2, 3, 0, 2, '2026-11-06 15:30:00', '2026-11-06 15:32:10'),
(3, 1, 1, 500, 5, 0, 0, 1, '2026-11-06 16:45:00', '2026-11-06 16:47:05'),
(3, 4, 2, 180, 2, 3, 0, 2, '2026-11-06 16:45:00', '2026-11-06 16:47:05'),
(4, 2, 1, 420, 4, 1, 0, 1, '2026-11-06 18:10:00', '2026-11-06 18:12:30'),
(4, 4, 2, 200, 2, 3, 0, 2, '2026-11-06 18:10:00', '2026-11-06 18:12:30'),
(5, 3, 1, 380, 4, 1, 0, 1, '2026-11-06 20:00:00', '2026-11-06 20:02:40'),
(5, 4, 2, 290, 3, 2, 0, 2, '2026-11-06 20:00:00', '2026-11-06 20:02:40')
ON DUPLICATE KEY UPDATE `Diem` = VALUES(`Diem`);
