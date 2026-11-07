-- =========================================================================
-- QUIZZLIVE SEED DATA - DỮ LIỆU KHỞI TẠO MẪU CHO NHÓM 07 (MYSQL)
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
(1, 'Khoa học Tự nhiên', 'Toán học, Vật lý, Hóa học, Sinh học, Thiên văn học', TRUE),
(2, 'Lịch sử & Địa lý', 'Lịch sử thế giới, Việt Nam và địa lý toàn cầu', TRUE),
(3, 'Công nghệ & Máy tính', 'Lập trình, mạng máy tính, hệ điều hành, CSDL', TRUE),
(4, 'Tư duy IQ & Logic', 'Dãy số quy luật, suy luận logic, đố mẹo trí tuệ', TRUE)
ON DUPLICATE KEY UPDATE `TenChuDe` = VALUES(`TenChuDe`);

-- 4. Người dùng mẫu (Requirement 1, 10, 16) - 8 người chơi
INSERT INTO `NGUOI_DUNG` (`MaNguoiDung`, `TenDangNhap`, `MatKhau`, `Email`, `TrangThai`, `TrangThaiOnline`, `LanDangNhapCuoi`) VALUES
(1, 'player_nam', '123456', 'nam@quizzlive.com', 'ACTIVE', FALSE, NOW()),
(2, 'player_hoa', '123456', 'hoa@quizzlive.com', 'ACTIVE', FALSE, NOW()),
(3, 'player_tuan', '123456', 'tuan@quizzlive.com', 'ACTIVE', FALSE, NOW()),
(4, 'player_lan', '123456', 'lan@quizzlive.com', 'ACTIVE', FALSE, NOW()),
(5, 'player_dung', '123456', 'dung@quizzlive.com', 'ACTIVE', FALSE, NOW()),
(6, 'player_minh', '123456', 'minh@quizzlive.com', 'ACTIVE', FALSE, NOW()),
(7, 'player_vy', '123456', 'vy@quizzlive.com', 'ACTIVE', FALSE, NOW()),
(8, 'player_khanh', '123456', 'khanh@quizzlive.com', 'ACTIVE', FALSE, NOW())
ON DUPLICATE KEY UPDATE `TenDangNhap` = VALUES(`TenDangNhap`);

INSERT INTO `HO_SO_NGUOI_DUNG` (`MaNguoiDung`, `HoTen`, `AnhDaiDien`, `GioiThieu`, `DiemIQ`, `TongSoTran`, `TongSoTranThang`, `TongSoTranThua`, `TongSoHoa`, `ChuoiThangHienTai`, `ChuoiThangDaiNhat`, `TongDiem`, `TongSoCau`, `TongCauDung`, `DiemCaoNhat`) VALUES
(1, 'Nguyễn Văn Nam', 'avatar1.png', 'Thích tranh tài tri thức', 125.00, 20, 15, 5, 0, 4, 7, 18500, 100, 82, 980),
(2, 'Trần Thị Hoa', 'avatar2.png', 'Đam mê lập trình mạng', 130.50, 25, 20, 5, 0, 6, 9, 24600, 125, 110, 1000),
(3, 'Lê Anh Tuấn', 'avatar3.png', 'Cao thủ trắc nghiệm', 118.00, 15, 8, 7, 0, 1, 3, 9800, 75, 45, 850),
(4, 'Phạm Ngọc Lan', 'avatar4.png', 'Newbie học hỏi', 110.00, 10, 4, 6, 0, 0, 2, 4500, 50, 28, 600),
(5, 'Nguyễn Tiến Dũng', 'avatar5.png', 'Chiến thần logic', 128.00, 30, 22, 8, 0, 5, 10, 28900, 150, 126, 1000),
(6, 'Lê Hoàng Minh', 'avatar6.png', 'Kỹ sư phần mềm tương lai', 121.00, 18, 12, 6, 0, 2, 5, 14200, 90, 70, 920),
(7, 'Đỗ Thảo Vy', 'avatar7.png', 'Vua tốc độ phản xạ', 135.00, 40, 34, 6, 0, 8, 12, 38500, 200, 178, 1000),
(8, 'Vũ Quốc Khánh', 'avatar8.png', 'Thử thách mọi giới hạn', 115.00, 12, 6, 6, 0, 1, 3, 7200, 60, 38, 780)
ON DUPLICATE KEY UPDATE `HoTen` = VALUES(`HoTen`);

-- 5. Ngân hàng Câu hỏi & Đáp án (40 câu hỏi phân bổ đều 4 chủ đề và 3 cấp độ EASY/MEDIUM/HARD)
INSERT INTO `CAU_HOI` (`MaCauHoi`, `MaChuDe`, `LoaiCauHoi`, `DoKho`, `NoiDung`, `HinhAnh`, `GiaiThich`, `TrangThai`) VALUES
-- CHỦ ĐỀ 1: KHOA HỌC TỰ NHIÊN (1 - 10)
(1, 1, 'STANDARD', 'EASY', 'Hành tinh nào trong Hệ Mặt Trời được mệnh danh là "Hành tinh Đỏ"?', NULL, 'Sao Hỏa (Mars) có màu đỏ do bề mặt chứa nhiều oxit sắt (rỉ sét).', 'ACTIVE'),
(2, 1, 'STANDARD', 'EASY', 'Khí nào chiếm tỉ lệ phần trăm cao nhất trong bầu khí quyển Trái Đất?', NULL, 'Khí Nitơ (N2) chiếm xấp xỉ 78% thể tích khí quyển, tiếp theo là Oxy chiếm 21%.', 'ACTIVE'),
(3, 1, 'STANDARD', 'EASY', 'Ký hiệu hóa học của nguyên tố Vàng trong bảng tuần hoàn là gì?', NULL, 'Vàng có ký hiệu hóa học là Au (bắt nguồn từ từ Aurum trong tiếng Latinh).', 'ACTIVE'),
(4, 1, 'STANDARD', 'EASY', 'Đơn vị đo cường độ dòng điện trong hệ đo lường quốc tế (SI) là gì?', NULL, 'Cường độ dòng điện được đo bằng Ampe (A), đặt theo tên nhà vật lý André-Marie Ampère.', 'ACTIVE'),
(5, 1, 'STANDARD', 'MEDIUM', 'Vận tốc ánh sáng truyền trong môi trường chân không xấp xỉ bằng bao nhiêu?', NULL, 'Vận tốc ánh sáng trong chân không là c ≈ 300.000 km/s (chính xác là 299.792.458 m/s).', 'ACTIVE'),
(6, 1, 'STANDARD', 'MEDIUM', 'Bào quan nào trong tế bào sinh vật nhân thực được ví như "nhà máy sản xuất năng lượng"?', NULL, 'Ti thể (Mitochondria) là nơi diễn ra quá trình hô hấp tế bào và tổng hợp năng lượng ATP.', 'ACTIVE'),
(7, 1, 'STANDARD', 'MEDIUM', 'Kim loại duy nhất tồn tại ở thể lỏng ở điều kiện nhiệt độ phòng tiêu chuẩn là gì?', NULL, 'Thủy ngân (Hg) là kim loại duy nhất ở thể lỏng ở nhiệt độ thường (nhiệt độ nóng chảy là -38.83 °C).', 'ACTIVE'),
(8, 1, 'STANDARD', 'HARD', 'Nhà bác học nào là tác giả của Thuyết tương đối hẹp và Thuyết tương đối rộng?', NULL, 'Albert Einstein công bố Thuyết tương đối hẹp năm 1905 và Thuyết tương đối rộng năm 1915.', 'ACTIVE'),
(9, 1, 'STANDARD', 'HARD', 'Hạt nào sau đây không mang điện tích cấu tạo nên hạt nhân nguyên tử?', NULL, 'Neutron (Nơ-tron) là hạt trung hòa điện tích trong hạt nhân nguyên tử.', 'ACTIVE'),
(10, 1, 'STANDARD', 'HARD', 'Chất nào chiếm tỉ lệ khối lượng lớn nhất cấu tạo nên cơ thể người trưởng thành?', NULL, 'Nước (H2O) chiếm khoảng 60% đến 70% khối lượng cơ thể người trưởng thành.', 'ACTIVE'),

-- CHỦ ĐỀ 2: LỊCH SỬ & ĐỊA LÝ (11 - 20)
(11, 2, 'STANDARD', 'EASY', 'Chiến thắng Điện Biên Phủ "lừng lẫy năm châu, chấn động địa cầu" diễn ra vào năm nào?', NULL, 'Chiến dịch Điện Biên Phủ kết thúc thắng lợi hoàn toàn vào ngày 7 tháng 5 năm 1954.', 'ACTIVE'),
(12, 2, 'STANDARD', 'EASY', 'Đỉnh núi Fansipan - "nóc nhà Đông Dương" nằm ở độ cao bao nhiêu mét so với mực nước biển?', NULL, 'Fansipan có độ cao chính xác là 3.143 mét, thuộc dãy núi Hoàng Liên Sơn.', 'ACTIVE'),
(13, 2, 'STANDARD', 'EASY', 'Sông Mê Kông bắt nguồn từ quốc gia nào trước khi chảy qua nhiều nước Đông Nam Á?', NULL, 'Sông Mê Kông bắt nguồn từ cao nguyên Tây Tạng thuộc lãnh thổ Trung Quốc.', 'ACTIVE'),
(14, 2, 'STANDARD', 'EASY', 'Đại Kim Tự Tháp Giza là di sản văn hóa thế giới cổ đại thuộc quốc gia nào?', NULL, 'Quần thể kim tự tháp Giza nằm ở ngoại ô Cairo, thuộc đất nước Ai Cập.', 'ACTIVE'),
(15, 2, 'STANDARD', 'MEDIUM', 'Bản Tuyên ngôn Độc lập khai sinh nước Việt Nam Dân chủ Cộng hòa được Chủ tịch Hồ Chí Minh đọc vào ngày nào?', NULL, 'Ngày 2 tháng 9 năm 1945 tại Quảng trường Ba Đình lịch sử, Hà Nội.', 'ACTIVE'),
(16, 2, 'STANDARD', 'MEDIUM', 'Quốc gia nào có diện tích lãnh thổ tự nhiên lớn nhất thế giới hiện nay?', NULL, 'Nga là quốc gia lớn nhất thế giới với diện tích hơn 17 triệu km2, trải dài qua 2 châu lục.', 'ACTIVE'),
(17, 2, 'STANDARD', 'MEDIUM', 'Cuộc Chiến tranh Thế giới thứ hai (WWII) chính thức kết thúc vào năm nào?', NULL, 'Chiến tranh thế giới thứ hai kết thúc vào ngày 2 tháng 9 năm 1945 sau khi Nhật Bản ký văn kiện đầu hàng.', 'ACTIVE'),
(18, 2, 'STANDARD', 'HARD', 'Eo biển Malacca là tuyến đường hàng hải huyết mạch quốc tế kết nối Ấn Độ Dương với đại dương nào?', NULL, 'Eo biển Malacca nối liền Ấn Độ Dương và Thái Bình Dương (thông qua Biển Đông).', 'ACTIVE'),
(19, 2, 'STANDARD', 'HARD', 'Triều đại phong kiến nào trị vì lâu nhất trong lịch sử Việt Nam?', NULL, 'Triều Hậu Lê (bao gồm Lê sơ và Lê Trung Hưng) tồn tại khoảng 355 năm (1428 - 1789).', 'ACTIVE'),
(20, 2, 'STANDARD', 'HARD', 'Quốc gia nào sở hữu đường bờ biển dài nhất thế giới?', NULL, 'Canada sở hữu đường bờ biển dài nhất thế giới với tổng chiều dài hơn 202.080 km.', 'ACTIVE'),

-- CHỦ ĐỀ 3: CÔNG NGHỆ & MÁY TÍNH (21 - 30)
(21, 3, 'STANDARD', 'EASY', 'Giao thức nào sau đây hoạt động ở tầng Transport và cung cấp việc truyền dữ liệu tin cậy (reliable)?', NULL, 'TCP (Transmission Control Protocol) là giao thức hướng kết nối và đảm bảo toàn vẹn dữ liệu.', 'ACTIVE'),
(22, 3, 'STANDARD', 'EASY', 'Cổng mạng (Port) mặc định tiêu chuẩn của giao thức HTTP là bao nhiêu?', NULL, 'HTTP mặc định sử dụng cổng 80, trong khi HTTPS sử dụng cổng 443.', 'ACTIVE'),
(23, 3, 'STANDARD', 'EASY', 'Ngôn ngữ lập trình Java được phát triển ban đầu bởi công ty nào vào năm 1995?', NULL, 'Java được khởi xướng bởi James Gosling và phát triển tại Sun Microsystems (sau này được Oracle mua lại).', 'ACTIVE'),
(24, 3, 'STANDARD', 'EASY', 'Thiết bị mạng nào có chức năng định tuyến và chuyển tiếp các gói tin giữa các mạng khác nhau?', NULL, 'Router (bộ định tuyến) làm việc ở tầng Network để chuyển tiếp gói tin giữa các mạng IP.', 'ACTIVE'),
(25, 3, 'STANDARD', 'MEDIUM', 'Bốn nguyên lý cốt lõi của Lập trình hướng đối tượng (OOP) gồm những gì?', NULL, 'Bốn tính chất cơ bản là: Đóng gói (Encapsulation), Kế thừa (Inheritance), Đa hình (Polymorphism) và Trừu tượng (Abstraction).', 'ACTIVE'),
(26, 3, 'STANDARD', 'MEDIUM', 'Lệnh SQL nào được sử dụng để truy vấn và trích xuất dữ liệu từ các bảng trong cơ sở dữ liệu quan hệ?', NULL, 'Lệnh SELECT được sử dụng phổ biến nhất để truy vấn dữ liệu từ các bảng.', 'ACTIVE'),
(27, 3, 'STANDARD', 'MEDIUM', 'Giao thức mã hóa an toàn HTTPS sử dụng cổng mặc định nào để trao đổi dữ liệu qua SSL/TLS?', NULL, 'HTTPS (HTTP Secure) chạy trên cổng mặc định 443.', 'ACTIVE'),
(28, 3, 'STANDARD', 'HARD', 'Mô hình tham chiếu mạng chuẩn OSI (Open Systems Interconnection) bao gồm bao nhiêu tầng?', NULL, 'Mô hình OSI chuẩn gồm 7 tầng: Physical, Data Link, Network, Transport, Session, Presentation, Application.', 'ACTIVE'),
(29, 3, 'STANDARD', 'HARD', 'Độ phức tạp thời gian trung bình (Average-case Time Complexity) của thuật toán QuickSort là bao nhiêu?', NULL, 'Thuật toán sắp xếp nhanh QuickSort có độ phức tạp trung bình là O(n log n).', 'ACTIVE'),
(30, 3, 'STANDARD', 'HARD', 'Giao thức phân giải tên miền DNS sử dụng cổng bao nhiêu và giao thức truyền tải mặc định nào cho truy vấn thông thường?', NULL, 'DNS sử dụng cổng 53 và mặc định giao thức UDP cho các truy vấn phân giải tên miền nhanh chóng.', 'ACTIVE'),

-- CHỦ ĐỀ 4: TƯ DUY IQ & LOGIC (31 - 40)
(31, 4, 'STANDARD', 'EASY', 'Tìm số tiếp theo trong dãy số quy luật nhân đôi: 2, 4, 8, 16, 32, ...?', NULL, 'Quy luật mỗi số sau gấp đôi số trước nó: 32 x 2 = 64.', 'ACTIVE'),
(32, 4, 'STANDARD', 'EASY', 'Một con ốc sên leo cây cao 10m. Ban ngày leo 3m, ban đêm tụt xuống 2m. Sau bao nhiêu ngày ốc sên chạm đỉnh cây?', NULL, 'Mỗi ngày đêm tiến được 1m. Sau 7 ngày ốc sên lên 7m, ngày thứ 8 leo thêm 3m là chạm đỉnh 10m.', 'ACTIVE'),
(33, 4, 'STANDARD', 'EASY', 'Tìm số còn thiếu trong dãy số Fibonacci nổi tiếng: 1, 1, 2, 3, 5, 8, 13, ...?', NULL, 'Quy luật số sau bằng tổng hai số liền trước: 8 + 13 = 21.', 'ACTIVE'),
(34, 4, 'STANDARD', 'EASY', 'Nếu tất cả hình vuông đều là hình chữ nhật, và tất cả hình chữ nhật đều là hình tứ giác. Mệnh đề nào sau đây đúng?', NULL, 'Theo tính chất bắc cầu logic, tất cả các hình vuông chắc chắn đều là hình tứ giác.', 'ACTIVE'),
(35, 4, 'STANDARD', 'MEDIUM', 'Một gia đình có 3 người con gái, mỗi người con gái đều có đúng 1 người anh trai. Hỏi gia đình đó có tất cả bao nhiêu người con?', NULL, 'Tất cả 3 người con gái đều chung một người anh trai duy nhất. Tổng số con là 3 gái + 1 trai = 4 người con.', 'ACTIVE'),
(36, 4, 'STANDARD', 'MEDIUM', 'Đồng hồ chỉ đúng 3 giờ 15 phút. Góc hợp bởi kim giờ và kim phút tại thời điểm đó là bao nhiêu độ?', NULL, 'Mỗi phút kim giờ chạy 0.5 độ. Sau 15 phút kim giờ nhích thêm 15 * 0.5 = 7.5 độ so với vị trí số 3.', 'ACTIVE'),
(37, 4, 'STANDARD', 'MEDIUM', 'Tìm số tiếp theo trong dãy số: 3, 5, 9, 17, 33, ...?', NULL, 'Khoảng cách giữa các số tăng theo cấp số nhân: +2, +4, +8, +16. Số tiếp theo cách 33 là +32: 33 + 32 = 65.', 'ACTIVE'),
(38, 4, 'STANDARD', 'HARD', 'Nếu "ngày hôm qua" là "ngày mai" của thứ Hai, thì "ngày mai" sẽ là "ngày hôm qua" của thứ mấy?', NULL, 'Ngày mai của thứ Hai là thứ Ba -> Hôm qua là thứ Ba -> Hôm nay là thứ Tư -> Ngày mai là thứ Năm -> Thứ Năm là hôm qua của thứ Sáu.', 'ACTIVE'),
(39, 4, 'STANDARD', 'HARD', 'Trong một cuộc thi chạy marathon tốc độ cao, nếu bạn vượt qua người đang ở vị trí thứ nhì, bạn sẽ ở vị trí thứ mấy?', NULL, 'Khi bạn vượt qua người thứ nhì, bạn sẽ thế chỗ của người đó và chính bạn trở thành người đứng thứ nhì.', 'ACTIVE'),
(40, 4, 'STANDARD', 'HARD', 'Mua một món đồ giá 60 đồng, bán 70 đồng. Mua lại giá 80 đồng, bán tiếp giá 90 đồng. Tổng kết lại bạn có lãi hay lỗ bao nhiêu?', NULL, 'Lần 1 lãi: 70 - 60 = 10 đồng. Lần 2 lãi: 90 - 80 = 10 đồng. Tổng lợi nhuận cả hai lần giao dịch là lãi 20 đồng.', 'ACTIVE')
ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);

-- Chèn Đáp Án tương ứng cho 40 câu hỏi (160 đáp án A, B, C, D)
INSERT INTO `DAP_AN` (`MaDapAn`, `MaCauHoi`, `KyHieu`, `NoiDung`, `LaDapAnDung`) VALUES
-- Câu 1 (Hành tinh Đỏ)
(1, 1, 'A', 'Sao Kim', FALSE),
(2, 1, 'B', 'Sao Hỏa', TRUE),
(3, 1, 'C', 'Sao Mộc', FALSE),
(4, 1, 'D', 'Sao Thủy', FALSE),

-- Câu 2 (Khí chiếm nhiều nhất)
(5, 2, 'A', 'Oxy (O2)', FALSE),
(6, 2, 'B', 'Nitơ (N2)', TRUE),
(7, 2, 'C', 'Cacbonic (CO2)', FALSE),
(8, 2, 'D', 'Argon (Ar)', FALSE),

-- Câu 3 (Vàng)
(9, 3, 'A', 'Ag', FALSE),
(10, 3, 'B', 'Fe', FALSE),
(11, 3, 'C', 'Au', TRUE),
(12, 3, 'D', 'Cu', FALSE),

-- Câu 4 (Cường độ dòng điện)
(13, 4, 'A', 'Ampe (A)', TRUE),
(14, 4, 'B', 'Vôn (V)', FALSE),
(15, 4, 'C', 'Oát (W)', FALSE),
(16, 4, 'D', 'Jun (J)', FALSE),

-- Câu 5 (Vận tốc ánh sáng)
(17, 5, 'A', '150.000 km/s', FALSE),
(18, 5, 'B', '300.000 km/s', TRUE),
(19, 5, 'C', '30.000 km/s', FALSE),
(20, 5, 'D', '3.000.000 km/s', FALSE),

-- Câu 6 (Nhà máy năng lượng tế bào)
(21, 6, 'A', 'Ribosome', FALSE),
(22, 6, 'B', 'Nhân tế bào', FALSE),
(23, 6, 'C', 'Ti thể (Mitochondria)', TRUE),
(24, 6, 'D', 'Lục lạp', FALSE),

-- Câu 7 (Kim loại thể lỏng)
(25, 7, 'A', 'Thủy ngân (Hg)', TRUE),
(26, 7, 'B', 'Chì (Pb)', FALSE),
(27, 7, 'C', 'Nhôm (Al)', FALSE),
(28, 7, 'D', 'Kẽm (Zn)', FALSE),

-- Câu 8 (Thuyết tương đối)
(29, 8, 'A', 'Isaac Newton', FALSE),
(30, 8, 'B', 'Albert Einstein', TRUE),
(31, 8, 'C', 'Nikola Tesla', FALSE),
(32, 8, 'D', 'Galileo Galilei', FALSE),

-- Câu 9 (Hạt không mang điện)
(33, 9, 'A', 'Proton', FALSE),
(34, 9, 'B', 'Electron', FALSE),
(35, 9, 'C', 'Neutron', TRUE),
(36, 9, 'D', 'Positron', FALSE),

-- Câu 10 (Chất chiếm khối lượng lớn nhất)
(37, 10, 'A', 'Protein', FALSE),
(38, 10, 'B', 'Chất béo (Lipid)', FALSE),
(39, 10, 'C', 'Canxi', FALSE),
(40, 10, 'D', 'Nước (H2O)', TRUE),

-- Câu 11 (Điện Biên Phủ)
(41, 11, 'A', '1945', FALSE),
(42, 11, 'B', '1954', TRUE),
(43, 11, 'C', '1975', FALSE),
(44, 11, 'D', '1968', FALSE),

-- Câu 12 (Fansipan)
(45, 12, 'A', '2.845m', FALSE),
(46, 12, 'B', '3.014m', FALSE),
(47, 12, 'C', '3.143m', TRUE),
(48, 12, 'D', '3.250m', FALSE),

-- Câu 13 (Sông Mê Kông)
(49, 13, 'A', 'Trung Quốc', TRUE),
(50, 13, 'B', 'Lào', FALSE),
(51, 13, 'C', 'Thái Lan', FALSE),
(52, 13, 'D', 'Myanmar', FALSE),

-- Câu 14 (Kim Tự Tháp Giza)
(53, 14, 'A', 'Hy Lạp', FALSE),
(54, 14, 'B', 'Ấn Độ', FALSE),
(55, 14, 'C', 'Ý', FALSE),
(56, 14, 'D', 'Ai Cập', TRUE),

-- Câu 15 (Tuyên ngôn Độc lập)
(57, 15, 'A', '02/09/1945', TRUE),
(58, 15, 'B', '19/08/1945', FALSE),
(59, 15, 'C', '30/04/1975', FALSE),
(60, 15, 'D', '07/05/1954', FALSE),

-- Câu 16 (Diện tích lớn nhất)
(61, 16, 'A', 'Canada', FALSE),
(62, 16, 'B', 'Nga', TRUE),
(63, 16, 'C', 'Hoa Kỳ', FALSE),
(64, 16, 'D', 'Trung Quốc', FALSE),

-- Câu 17 (Kết thúc Thế chiến 2)
(65, 17, 'A', '1918', FALSE),
(66, 17, 'B', '1939', FALSE),
(67, 17, 'C', '1945', TRUE),
(68, 17, 'D', '1950', FALSE),

-- Câu 18 (Eo biển Malacca)
(69, 18, 'A', 'Thái Bình Dương', TRUE),
(70, 18, 'B', 'Đại Tây Dương', FALSE),
(71, 18, 'C', 'Bắc Băng Dương', FALSE),
(72, 18, 'D', 'Địa Trung Hải', FALSE),

-- Câu 19 (Triều đại lâu nhất)
(73, 19, 'A', 'Triều Lý', FALSE),
(74, 19, 'B', 'Triều Hậu Lê', TRUE),
(75, 19, 'C', 'Triều Trần', FALSE),
(76, 19, 'D', 'Triều Nguyễn', FALSE),

-- Câu 20 (Đường bờ biển dài nhất)
(77, 20, 'A', 'Úc (Australia)', FALSE),
(78, 20, 'B', 'Indonesia', FALSE),
(79, 20, 'C', 'Na Uy', FALSE),
(80, 20, 'D', 'Canada', TRUE),

-- Câu 21 (Giao thức tin cậy)
(81, 21, 'A', 'TCP', TRUE),
(82, 21, 'B', 'UDP', FALSE),
(83, 21, 'C', 'IP', FALSE),
(84, 21, 'D', 'ICMP', FALSE),

-- Câu 22 (Port HTTP)
(85, 22, 'A', '21', FALSE),
(86, 22, 'B', '80', TRUE),
(87, 22, 'C', '443', FALSE),
(88, 22, 'D', '8080', FALSE),

-- Câu 23 (Sun Microsystems - Java)
(89, 23, 'A', 'Microsoft', FALSE),
(90, 23, 'B', 'IBM', FALSE),
(91, 23, 'C', 'Sun Microsystems', TRUE),
(92, 23, 'D', 'Google', FALSE),

-- Câu 24 (Router)
(93, 24, 'A', 'Router', TRUE),
(94, 24, 'B', 'Switch', FALSE),
(95, 24, 'C', 'Hub', FALSE),
(96, 24, 'D', 'Repeater', FALSE),

-- Câu 25 (OOP 4 tính chất)
(97, 25, 'A', 'Tuần tự, Rẽ nhánh, Lặp, Hàm', FALSE),
(98, 25, 'B', 'Đóng gói, Kế thừa, Đa hình, Trừu tượng', TRUE),
(99, 25, 'C', 'Cấu trúc, Module, Đối tượng, Lớp', FALSE),
(100, 25, 'D', 'Giao diện, Biến, Phương thức, Luồng', FALSE),

-- Câu 26 (Truy vấn SELECT)
(101, 26, 'A', 'SELECT', TRUE),
(102, 26, 'B', 'INSERT', FALSE),
(103, 26, 'C', 'UPDATE', FALSE),
(104, 26, 'D', 'DELETE', FALSE),

-- Câu 27 (Port HTTPS)
(105, 27, 'A', '80', FALSE),
(106, 27, 'B', '22', FALSE),
(107, 27, 'C', '443', TRUE),
(108, 27, 'D', '3306', FALSE),

-- Câu 28 (Mô hình OSI)
(109, 28, 'A', '4 tầng', FALSE),
(110, 28, 'B', '7 tầng', TRUE),
(111, 28, 'C', '5 tầng', FALSE),
(112, 28, 'D', '6 tầng', FALSE),

-- Câu 29 (QuickSort Time Complexity)
(113, 29, 'A', 'O(n log n)', TRUE),
(114, 29, 'B', 'O(n^2)', FALSE),
(115, 29, 'C', 'O(n)', FALSE),
(116, 29, 'D', 'O(log n)', FALSE),

-- Câu 30 (DNS Port)
(117, 30, 'A', 'Cổng 25 / TCP', FALSE),
(118, 30, 'B', 'Cổng 110 / TCP', FALSE),
(119, 30, 'C', 'Cổng 23 / UDP', FALSE),
(120, 30, 'D', 'Cổng 53 / UDP', TRUE),

-- Câu 31 (Dãy số nhân đôi)
(121, 31, 'A', '48', FALSE),
(122, 31, 'B', '54', FALSE),
(123, 31, 'C', '64', TRUE),
(124, 31, 'D', '60', FALSE),

-- Câu 32 (Ốc sên leo cây)
(125, 32, 'A', '10 ngày', FALSE),
(126, 32, 'B', '8 ngày', TRUE),
(127, 32, 'C', '9 ngày', FALSE),
(128, 32, 'D', '7 ngày', FALSE),

-- Câu 33 (Fibonacci)
(129, 33, 'A', '21', TRUE),
(130, 33, 'B', '20', FALSE),
(131, 33, 'C', '19', FALSE),
(132, 33, 'D', '24', FALSE),

-- Câu 34 (Logic hình vuông hình chữ nhật)
(133, 34, 'A', 'Mọi hình tứ giác đều là hình vuông', FALSE),
(134, 34, 'B', 'Một số hình chữ nhật không phải là hình tứ giác', FALSE),
(135, 34, 'C', 'Hình vuông không thuộc tập hình chữ nhật', FALSE),
(136, 34, 'D', 'Tất cả hình vuông đều là hình tứ giác', TRUE),

-- Câu 35 (Gia đình có mấy con)
(137, 35, 'A', '6 người con', FALSE),
(138, 35, 'B', '4 người con', TRUE),
(139, 35, 'C', '3 người con', FALSE),
(140, 35, 'D', '5 người con', FALSE),

-- Câu 36 (Góc 3h15)
(141, 36, 'A', '0 độ', FALSE),
(142, 36, 'B', '5 độ', FALSE),
(143, 36, 'C', '7.5 độ', TRUE),
(144, 36, 'D', '15 độ', FALSE),

-- Câu 37 (Dãy số 3, 5, 9, 17, 33...)
(145, 37, 'A', '65', TRUE),
(146, 37, 'B', '66', FALSE),
(147, 37, 'C', '49', FALSE),
(148, 37, 'D', '55', FALSE),

-- Câu 38 (Hôm qua hôm nay thứ mấy)
(149, 38, 'A', 'Thứ Năm', FALSE),
(150, 38, 'B', 'Thứ Sáu', TRUE),
(151, 38, 'C', 'Thứ Bảy', FALSE),
(152, 38, 'D', 'Chủ Nhật', FALSE),

-- Câu 39 (Vượt qua người thứ nhì)
(153, 39, 'A', 'Thứ nhất', FALSE),
(154, 39, 'B', 'Thứ nhì', TRUE),
(155, 39, 'C', 'Thứ ba', FALSE),
(156, 39, 'D', 'Không xác định', FALSE),

-- Câu 40 (Mua bán lãi lỗ)
(157, 40, 'A', 'Hòa vốn', FALSE),
(158, 40, 'B', 'Lãi 10 đồng', FALSE),
(159, 40, 'C', 'Lãi 20 đồng', TRUE),
(160, 40, 'D', 'Lỗ 10 đồng', FALSE)
ON DUPLICATE KEY UPDATE `NoiDung` = VALUES(`NoiDung`);

-- 6. Dữ liệu mẫu Giải Đấu & Xếp Hạng Giải (Requirement 9)
INSERT INTO `GIAI_DAU` (`MaGiai`, `TenGiai`, `MoTa`, `TrangThai`, `SoNguoiToiDa`, `ThoiGianBatDau`) VALUES
(1, 'Đại Chiến Tri Thức SGU 2026', 'Giải đấu trắc nghiệm kiến thức đa lĩnh vực toàn trường', 'UPCOMING', 16, '2026-12-01 09:00:00'),
(2, 'Cúp QuizzLive Mùa Xuân 2026', 'Giải đấu 8 thí sinh tranh tài gay cấn', 'IN_PROGRESS', 8, '2026-11-07 08:00:00')
ON DUPLICATE KEY UPDATE `TenGiai` = VALUES(`TenGiai`);

-- Bảng xếp hạng / Thí sinh tham gia giải đấu số 2 (Đã chia Bảng A và Bảng B)
INSERT INTO `XEP_HANG_GIAI` (`MaGiai`, `MaNguoiDung`, `BangDau`, `HatGiong`, `SoTran`, `SoTranThang`, `SoTranHoa`, `SoTranThua`, `TongDiem`, `ViTri`) VALUES
-- Bảng A
(2, 1, 'Bảng A', 1, 3, 3, 0, 0, 300, 1),
(2, 2, 'Bảng A', 2, 3, 2, 0, 1, 200, 2),
(2, 3, 'Bảng A', 3, 3, 1, 0, 2, 100, 3),
(2, 4, 'Bảng A', 4, 3, 0, 0, 3, 0, 4),
-- Bảng B
(2, 5, 'Bảng B', 1, 3, 2, 1, 0, 250, 1),
(2, 6, 'Bảng B', 2, 3, 2, 0, 1, 200, 2),
(2, 7, 'Bảng B', 3, 3, 1, 1, 1, 150, 3),
(2, 8, 'Bảng B', 4, 3, 0, 0, 3, 0, 4)
ON DUPLICATE KEY UPDATE `TongDiem` = VALUES(`TongDiem`), `ViTri` = VALUES(`ViTri`);
