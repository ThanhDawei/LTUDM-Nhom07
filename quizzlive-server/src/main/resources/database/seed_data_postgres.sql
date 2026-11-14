-- =========================================================================
-- QUIZZLIVE SEED DATA - DÀNH RIÊNG CHO POSTGRESQL (CẬP NHẬT ĐẦY ĐỦ 40 CÂU HỎI)
-- =========================================================================

-- 1. Cấu hình trận đấu mặc định
INSERT INTO CAU_HINH_TRAN (MaCauHinh, TenCauHinh, SoCauHoi, ThoiGianMoiCau, DiemCoBan, ThuongTheoThoiGian, DiemSai, DiemKhongTraLoi, TrangThai)
VALUES 
(1, 'Cấu hình tiêu chuẩn (5 câu - 10s)', 5, 10, 100, TRUE, 0, 0, TRUE),
(2, 'Cấu hình thi đấu giải (10 câu - 15s)', 10, 15, 150, TRUE, 0, 0, TRUE)
ON CONFLICT (MaCauHinh) DO NOTHING;

-- 3. Danh mục Chủ đề (4 chủ đề tiêu chuẩn)
INSERT INTO CHU_DE (MaChuDe, TenChuDe, MoTa, TrangThai) VALUES
(1, 'Khoa học Tự nhiên', 'Toán học, Vật lý, Hóa học, Sinh học, Thiên văn học', TRUE),
(2, 'Lịch sử & Địa lý', 'Lịch sử thế giới, Việt Nam và địa lý toàn cầu', TRUE),
(3, 'Công nghệ & Máy tính', 'Lập trình, mạng máy tính, hệ điều hành, CSDL', TRUE),
(4, 'Tư duy IQ & Logic', 'Dãy số quy luật, suy luận logic, đố mẹo trí tuệ', TRUE)
ON CONFLICT (MaChuDe) DO NOTHING;

-- 4. Người dùng mẫu (Mật khẩu: 123456 được băm bằng PBKDF2WithHmacSHA256 kết hợp Salt)
INSERT INTO NGUOI_DUNG (MaNguoiDung, TenDangNhap, Salt, Hash, Email, TrangThai, TrangThaiOnline, LanDangNhapCuoi) VALUES
(1, 'player_nam', 'IIvHlqogYEJQTlCRXiDQMA==', 'USwHwVf/nLngAGpzDU91GdGxqELvXevk8EE96Hh13UU=', 'nam@quizzlive.com', 'ACTIVE', FALSE, CURRENT_TIMESTAMP),
(2, 'player_hoa', 'B074Lvs+duOzHmRZ14Z7ag==', 'eO9QbcSZ7FRNMDXDVFAVYElh8+FHsd9EeFb3dsaf+WY=', 'hoa@quizzlive.com', 'ACTIVE', FALSE, CURRENT_TIMESTAMP),
(3, 'player_tuan', 'R/CS+KydehuAVEyzSeLllg==', '2XStwJPdKc4xVXIHJwh3dxgt6NZB2X+cwwtCa3ymRlY=', 'tuan@quizzlive.com', 'ACTIVE', FALSE, CURRENT_TIMESTAMP),
(4, 'player_lan', 'Yw+vT70sv8CYPS/arfielg==', 'R7FWecLTUCKCwVwoMcUy/DTbPCi/Y/VntRNzA7zGpi8=', 'lan@quizzlive.com', 'ACTIVE', FALSE, CURRENT_TIMESTAMP),
(5, 'player_dung', 'cd+cZMvFQv4Jb1v/sm4qGQ==', 'ci059vgTvFDb6aaKAmj1pnGePDtKT3LnLQC0IfUbY/M=', 'dung@quizzlive.com', 'ACTIVE', FALSE, CURRENT_TIMESTAMP),
(6, 'player_minh', 'FkvT1I+YYcsusZVanE4ziA==', 'LOmpHN4opNB3mCiYyDUrbuJ5hH2sybu2l0qTut0vMzM=', 'minh@quizzlive.com', 'ACTIVE', FALSE, CURRENT_TIMESTAMP),
(7, 'player_vy', '4rVzwTVKj30CO9qQGUsHbg==', 'NGDNoE21d1z+4ji0u5IShKNfVTqIrMc8XeVnaZgoI6Y=', 'vy@quizzlive.com', 'ACTIVE', FALSE, CURRENT_TIMESTAMP),
(8, 'player_khanh', 'H6ZIukn7jIWDomWllPON4g==', 'Ra5PXhEhQQs2+s3DfQpsRNeiCmEHaPLrx5y60fLIFss=', 'khanh@quizzlive.com', 'ACTIVE', FALSE, CURRENT_TIMESTAMP)
ON CONFLICT (MaNguoiDung) DO NOTHING;

INSERT INTO HO_SO_NGUOI_DUNG (MaNguoiDung, HoTen, AnhDaiDien, GioiThieu, DiemIQ, TongSoTran, TongSoTranThang, TongSoTranThua, TongSoHoa, ChuoiThangHienTai, ChuoiThangDaiNhat, TongDiem, TongSoCau, TongCauDung, DiemCaoNhat) VALUES
(1, 'Nguyễn Văn Nam', 'avatar1.png', 'Thích tranh tài tri thức', 125.00, 20, 15, 5, 0, 4, 7, 18500, 100, 82, 980),
(2, 'Trần Thị Hoa', 'avatar2.png', 'Đam mê lập trình mạng', 130.50, 25, 20, 5, 0, 6, 9, 24600, 125, 110, 1000),
(3, 'Lê Anh Tuấn', 'avatar3.png', 'Cao thủ trắc nghiệm', 118.00, 15, 8, 7, 0, 1, 3, 9800, 75, 45, 850),
(4, 'Phạm Ngọc Lan', 'avatar4.png', 'Newbie học hỏi', 110.00, 10, 4, 6, 0, 0, 2, 4500, 50, 28, 600),
(5, 'Nguyễn Tiến Dũng', 'avatar5.png', 'Chiến thần logic', 128.00, 30, 22, 8, 0, 5, 10, 28900, 150, 126, 1000),
(6, 'Lê Hoàng Minh', 'avatar6.png', 'Kỹ sư phần mềm tương lai', 121.00, 18, 12, 6, 0, 2, 5, 14200, 90, 70, 920),
(7, 'Đỗ Thảo Vy', 'avatar7.png', 'Vua tốc độ phản xạ', 135.00, 40, 34, 6, 0, 8, 12, 38500, 200, 178, 1000),
(8, 'Vũ Quốc Khánh', 'avatar8.png', 'Thử thách mọi giới hạn', 115.00, 12, 6, 6, 0, 1, 3, 7200, 60, 38, 780)
ON CONFLICT (MaNguoiDung) DO NOTHING;

-- 5. Ngân hàng Câu hỏi & Đáp án (40 câu hỏi phân bổ đều 4 chủ đề và 3 cấp độ EASY/MEDIUM/HARD)
INSERT INTO CAU_HOI (MaCauHoi, MaChuDe, LoaiCauHoi, DoKho, NoiDung, HinhAnh, GiaiThich, TrangThai) VALUES
-- =================== CHỦ ĐỀ 1: KHOA HỌC TỰ NHIÊN (1 - 10) ===================
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

-- =================== CHỦ ĐỀ 2: LỊCH SỬ & ĐỊA LÝ (11 - 20) ===================
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

-- =================== CHỦ ĐỀ 3: CÔNG NGHỆ & MÁY TÍNH (21 - 30) ===================
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

-- =================== CHỦ ĐỀ 4: TƯ DUY IQ & LOGIC (31 - 40) - LOẠI IQ_TEST ===================
(31, 4, 'IQ_TEST', 'EASY', 'Tìm số tiếp theo trong dãy số quy luật nhân đôi: 2, 4, 8, 16, 32, ...?', NULL, 'Quy luật mỗi số sau gấp đôi số trước nó: 32 x 2 = 64.', 'ACTIVE'),
(32, 4, 'IQ_TEST', 'EASY', 'Một con ốc sên leo cây cao 10m. Ban ngày leo 3m, ban đêm tụt xuống 2m. Sau bao nhiêu ngày ốc sên chạm đỉnh cây?', NULL, 'Mỗi ngày đêm tiến được 1m. Sau 7 ngày ốc sên lên 7m, ngày thứ 8 leo thêm 3m là chạm đỉnh 10m.', 'ACTIVE'),
(33, 4, 'IQ_TEST', 'EASY', 'Tìm số còn thiếu trong dãy số Fibonacci nổi tiếng: 1, 1, 2, 3, 5, 8, 13, ...?', NULL, 'Quy luật số sau bằng tổng hai số liền trước: 8 + 13 = 21.', 'ACTIVE'),
(34, 4, 'IQ_TEST', 'EASY', 'Nếu tất cả hình vuông đều là hình chữ nhật, và tất cả hình chữ nhật đều là hình tứ giác. Mệnh đề nào sau đây đúng?', NULL, 'Theo tính chất bắc cầu logic, tất cả các hình vuông chắc chắn đều là hình tứ giác.', 'ACTIVE'),
(35, 4, 'IQ_TEST', 'MEDIUM', 'Một gia đình có 3 người con gái, mỗi người con gái đều có đúng 1 người anh trai. Hỏi gia đình đó có tất cả bao nhiêu người con?', NULL, 'Tất cả 3 người con gái đều chung một người anh trai duy nhất. Tổng số con là 3 gái + 1 trai = 4 người con.', 'ACTIVE'),
(36, 4, 'IQ_TEST', 'MEDIUM', 'Đồng hồ chỉ đúng 3 giờ 15 phút. Góc hợp bởi kim giờ và kim phút tại thời điểm đó là bao nhiêu độ?', NULL, 'Mỗi phút kim giờ chạy 0.5 độ. Sau 15 phút kim giờ nhích thêm 15 * 0.5 = 7.5 độ so với vị trí số 3.', 'ACTIVE'),
(37, 4, 'IQ_TEST', 'MEDIUM', 'Tìm số tiếp theo trong dãy số: 3, 5, 9, 17, 33, ...?', NULL, 'Khoảng cách giữa các số tăng theo cấp số nhân: +2, +4, +8, +16. Số tiếp theo cách 33 là +32: 33 + 32 = 65.', 'ACTIVE'),
(38, 4, 'IQ_TEST', 'HARD', 'Nếu "ngày hôm qua" là "ngày mai" của thứ Hai, thì "ngày mai" sẽ là "ngày hôm qua" của thứ mấy?', NULL, 'Ngày mai của thứ Hai là thứ Ba -> Hôm qua là thứ Ba -> Hôm nay là thứ Tư -> Ngày mai là thứ Năm -> Thứ Năm là hôm qua của thứ Sáu.', 'ACTIVE'),
(39, 4, 'IQ_TEST', 'HARD', 'Trong một cuộc thi chạy marathon tốc độ cao, nếu bạn vượt qua người đang ở vị trí thứ nhì, bạn sẽ ở vị trí thứ mấy?', NULL, 'Khi bạn vượt qua người thứ nhì, bạn sẽ thế chỗ của người đó và chính bạn trở thành người đứng thứ nhì.', 'ACTIVE'),
(40, 4, 'IQ_TEST', 'HARD', 'Mua một món đồ giá 60 đồng, bán 70 đồng. Mua lại giá 80 đồng, bán tiếp giá 90 đồng. Tổng kết lại bạn có lãi hay lỗ bao nhiêu?', NULL, 'Lần 1 lãi: 70 - 60 = 10 đồng. Lần 2 lãi: 90 - 80 = 10 đồng. Tổng lợi nhuận cả hai lần giao dịch là lãi 20 đồng.', 'ACTIVE')
ON CONFLICT (MaCauHoi) DO NOTHING;

-- Chèn Đáp Án tương ứng cho 40 câu hỏi (160 đáp án A, B, C, D)
INSERT INTO DAP_AN (MaDapAn, MaCauHoi, KyHieu, NoiDung, LaDapAnDung) VALUES
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
ON CONFLICT (MaDapAn) DO NOTHING;

-- BỔ SUNG NGÂN HÀNG CÂU HỎI ĐẦY ĐỦ 4 CHỦ ĐỀ & 3 ĐỘ KHÓ (YÊU CẦU 4 & 12)
INSERT INTO CAU_HOI (MaCauHoi, MaChuDe, LoaiCauHoi, DoKho, NoiDung, HinhAnh, GiaiThich, TrangThai) VALUES (9, 1, 'STANDARD', 'EASY', 'Công thức hóa học của nước là gì?', NULL, 'Nước là hợp chất tạo bởi 2 nguyên tử Hidro và 1 nguyên tử Oxy (H2O).', 'ACTIVE') ON CONFLICT (MaCauHoi) DO NOTHING;
INSERT INTO DAP_AN (MaDapAn, MaCauHoi, KyHieu, NoiDung, LaDapAnDung) VALUES
(33, 9, 'A', 'H2O', TRUE),
(34, 9, 'B', 'CO2', FALSE),
(35, 9, 'C', 'NaCl', FALSE),
(36, 9, 'D', 'O2', FALSE)
ON CONFLICT (MaDapAn) DO NOTHING;

INSERT INTO CAU_HOI (MaCauHoi, MaChuDe, LoaiCauHoi, DoKho, NoiDung, HinhAnh, GiaiThich, TrangThai) VALUES (10, 1, 'STANDARD', 'EASY', 'Hành tinh nào nằm gần Mặt Trời nhất trong Hệ Mặt Trời?', NULL, 'Sao Thủy (Mercury) là hành tinh gần Mặt Trời nhất.', 'ACTIVE') ON CONFLICT (MaCauHoi) DO NOTHING;
INSERT INTO DAP_AN (MaDapAn, MaCauHoi, KyHieu, NoiDung, LaDapAnDung) VALUES
(37, 10, 'A', 'Sao Kim', FALSE),
(38, 10, 'B', 'Sao Hỏa', FALSE),
(39, 10, 'C', 'Sao Thủy', TRUE),
(40, 10, 'D', 'Trái Đất', FALSE)
ON CONFLICT (MaDapAn) DO NOTHING;

INSERT INTO CAU_HOI (MaCauHoi, MaChuDe, LoaiCauHoi, DoKho, NoiDung, HinhAnh, GiaiThich, TrangThai) VALUES (11, 1, 'STANDARD', 'EASY', 'Động vật nào sau đây là loài thú có vú duy nhất biết bay lượn?', NULL, 'Dơi là loài thú có vú duy nhất có cấu tạo cánh để bay.', 'ACTIVE') ON CONFLICT (MaCauHoi) DO NOTHING;
INSERT INTO DAP_AN (MaDapAn, MaCauHoi, KyHieu, NoiDung, LaDapAnDung) VALUES
(41, 11, 'A', 'Chim cánh cụt', FALSE),
(42, 11, 'B', 'Dơi', TRUE),
(43, 11, 'C', 'Đà điểu', FALSE),
(44, 11, 'D', 'Sóc bay', FALSE)
ON CONFLICT (MaDapAn) DO NOTHING;

INSERT INTO CAU_HOI (MaCauHoi, MaChuDe, LoaiCauHoi, DoKho, NoiDung, HinhAnh, GiaiThich, TrangThai) VALUES (12, 1, 'STANDARD', 'EASY', 'Bộ phận nào của cây xanh chủ yếu thực hiện quá trình quang hợp?', NULL, 'Lá cây chứa nhiều lục lạp với chất diệp lục giúp hấp thụ ánh sáng mặt trời để quang hợp.', 'ACTIVE') ON CONFLICT (MaCauHoi) DO NOTHING;
INSERT INTO DAP_AN (MaDapAn, MaCauHoi, KyHieu, NoiDung, LaDapAnDung) VALUES
(45, 12, 'A', 'Rễ cây', FALSE),
(46, 12, 'B', 'Thân cây', FALSE),
(47, 12, 'C', 'Lá cây', TRUE),
(48, 12, 'D', 'Hoa', FALSE)
ON CONFLICT (MaDapAn) DO NOTHING;

INSERT INTO CAU_HOI (MaCauHoi, MaChuDe, LoaiCauHoi, DoKho, NoiDung, HinhAnh, GiaiThich, TrangThai) VALUES (13, 1, 'STANDARD', 'EASY', 'Nhiệt độ sôi của nước tinh khiết ở điều kiện áp suất tiêu chuẩn là bao nhiêu?', NULL, 'Ở áp suất 1 atm tiêu chuẩn, nước nguyên chất sôi ở 100 độ C.', 'ACTIVE') ON CONFLICT (MaCauHoi) DO NOTHING;
INSERT INTO DAP_AN (MaDapAn, MaCauHoi, KyHieu, NoiDung, LaDapAnDung) VALUES
(49, 13, 'A', '50 độ C', FALSE),
(50, 13, 'B', '80 độ C', FALSE),
(51, 13, 'C', '100 độ C', TRUE),
(52, 13, 'D', '120 độ C', FALSE)
ON CONFLICT (MaDapAn) DO NOTHING;

INSERT INTO CAU_HOI (MaCauHoi, MaChuDe, LoaiCauHoi, DoKho, NoiDung, HinhAnh, GiaiThich, TrangThai) VALUES (14, 1, 'STANDARD', 'MEDIUM', 'Cơ quan nào trong cơ thể người có chức năng lọc máu và tạo ra nước tiểu?', NULL, 'Thận là cơ quan thuộc hệ tiết niệu có chức năng lọc máu bài tiết các chất cặn bã.', 'ACTIVE') ON CONFLICT (MaCauHoi) DO NOTHING;
INSERT INTO DAP_AN (MaDapAn, MaCauHoi, KyHieu, NoiDung, LaDapAnDung) VALUES
(53, 14, 'A', 'Gan', FALSE),
(54, 14, 'B', 'Thận', TRUE),
(55, 14, 'C', 'Tim', FALSE),
(56, 14, 'D', 'Dạ dày', FALSE)
ON CONFLICT (MaDapAn) DO NOTHING;

INSERT INTO CAU_HOI (MaCauHoi, MaChuDe, LoaiCauHoi, DoKho, NoiDung, HinhAnh, GiaiThich, TrangThai) VALUES (15, 1, 'STANDARD', 'MEDIUM', 'Kim loại duy nhất nào ở thể lỏng trong điều kiện nhiệt độ phòng bình thường?', NULL, 'Thủy ngân (Hg) là kim loại duy nhất tồn tại ở thể lỏng ở nhiệt độ thường.', 'ACTIVE') ON CONFLICT (MaCauHoi) DO NOTHING;
INSERT INTO DAP_AN (MaDapAn, MaCauHoi, KyHieu, NoiDung, LaDapAnDung) VALUES
(57, 15, 'A', 'Thủy ngân (Hg)', TRUE),
(58, 15, 'B', 'Sắt (Fe)', FALSE),
(59, 15, 'C', 'Nhôm (Al)', FALSE),
(60, 15, 'D', 'Vàng (Au)', FALSE)
ON CONFLICT (MaDapAn) DO NOTHING;

INSERT INTO CAU_HOI (MaCauHoi, MaChuDe, LoaiCauHoi, DoKho, NoiDung, HinhAnh, GiaiThich, TrangThai) VALUES (16, 1, 'STANDARD', 'MEDIUM', 'Đơn vị đo công suất tiêu chuẩn trong hệ đo lường quốc tế (SI) là gì?', NULL, 'Watt (W) là đơn vị đo công suất trong hệ SI, đặt theo tên nhà phát minh James Watt.', 'ACTIVE') ON CONFLICT (MaCauHoi) DO NOTHING;
INSERT INTO DAP_AN (MaDapAn, MaCauHoi, KyHieu, NoiDung, LaDapAnDung) VALUES
(61, 16, 'A', 'Joule (J)', FALSE),
(62, 16, 'B', 'Watt (W)', TRUE),
(63, 16, 'C', 'Newton (N)', FALSE),
(64, 16, 'D', 'Pascal (Pa)', FALSE)
ON CONFLICT (MaDapAn) DO NOTHING;

INSERT INTO CAU_HOI (MaCauHoi, MaChuDe, LoaiCauHoi, DoKho, NoiDung, HinhAnh, GiaiThich, TrangThai) VALUES (17, 1, 'STANDARD', 'HARD', 'Vận tốc của ánh sáng truyền trong môi trường chân không xấp xỉ bằng bao nhiêu?', NULL, 'Vận tốc ánh sáng trong chân không là c xấp xỉ bằng 300.000 km/s (chính xác là 299.792.458 m/s).', 'ACTIVE') ON CONFLICT (MaCauHoi) DO NOTHING;
INSERT INTO DAP_AN (MaDapAn, MaCauHoi, KyHieu, NoiDung, LaDapAnDung) VALUES
(65, 17, 'A', '150.000 km/s', FALSE),
(66, 17, 'B', '300.000 km/s', TRUE),
(67, 17, 'C', '450.000 km/s', FALSE),
(68, 17, 'D', '600.000 km/s', FALSE)
ON CONFLICT (MaDapAn) DO NOTHING;

INSERT INTO CAU_HOI (MaCauHoi, MaChuDe, LoaiCauHoi, DoKho, NoiDung, HinhAnh, GiaiThich, TrangThai) VALUES (18, 1, 'STANDARD', 'HARD', 'Định luật vạn vật hấp dẫn được khám phá và công bố bởi nhà bác học vĩ đại nào?', NULL, 'Isaac Newton là người phát hiện và thiết lập định luật vạn vật hấp dẫn vào năm 1687.', 'ACTIVE') ON CONFLICT (MaCauHoi) DO NOTHING;
INSERT INTO DAP_AN (MaDapAn, MaCauHoi, KyHieu, NoiDung, LaDapAnDung) VALUES
(69, 18, 'A', 'Albert Einstein', FALSE),
(70, 18, 'B', 'Isaac Newton', TRUE),
(71, 18, 'C', 'Galileo Galilei', FALSE),
(72, 18, 'D', 'Nikola Tesla', FALSE)
ON CONFLICT (MaDapAn) DO NOTHING;

INSERT INTO CAU_HOI (MaCauHoi, MaChuDe, LoaiCauHoi, DoKho, NoiDung, HinhAnh, GiaiThich, TrangThai) VALUES (19, 1, 'STANDARD', 'HARD', 'Nguyên tố kim loại nào có ký hiệu hóa học là Au trong bảng tuần hoàn Mendeleev?', NULL, 'Au là viết tắt của từ Aurum tiếng Latinh nghĩa là Vàng.', 'ACTIVE') ON CONFLICT (MaCauHoi) DO NOTHING;
INSERT INTO DAP_AN (MaDapAn, MaCauHoi, KyHieu, NoiDung, LaDapAnDung) VALUES
(73, 19, 'A', 'Bạc', FALSE),
(74, 19, 'B', 'Vàng', TRUE),
(75, 19, 'C', 'Đồng', FALSE),
(76, 19, 'D', 'Bạch kim', FALSE)
ON CONFLICT (MaDapAn) DO NOTHING;

INSERT INTO CAU_HOI (MaCauHoi, MaChuDe, LoaiCauHoi, DoKho, NoiDung, HinhAnh, GiaiThich, TrangThai) VALUES (20, 1, 'STANDARD', 'HARD', 'Quá trình phân chia tế bào sinh dưỡng ở sinh vật nhân thực tạo ra 2 tế bào con giống hệt tế bào mẹ gọi là gì?', NULL, 'Nguyên phân (Mitosis) là hình thức phân bào sinh dưỡng giữ nguyên bộ nhiễm sắc thể 2n.', 'ACTIVE') ON CONFLICT (MaCauHoi) DO NOTHING;
INSERT INTO DAP_AN (MaDapAn, MaCauHoi, KyHieu, NoiDung, LaDapAnDung) VALUES
(77, 20, 'A', 'Giảm phân', FALSE),
(78, 20, 'B', 'Nguyên phân', TRUE),
(79, 20, 'C', 'Thụ tinh', FALSE),
(80, 20, 'D', 'Đột biến gene', FALSE)
ON CONFLICT (MaDapAn) DO NOTHING;

INSERT INTO CAU_HOI (MaCauHoi, MaChuDe, LoaiCauHoi, DoKho, NoiDung, HinhAnh, GiaiThich, TrangThai) VALUES (21, 1, 'STANDARD', 'HARD', 'Khối lượng riêng của nước nguyên chất đạt giá trị cực đại ở nhiệt độ bao nhiêu?', NULL, 'Nước có hiện tượng dãn nở nghịch thường, đạt khối lượng riêng lớn nhất (1 g/cm3) ở 4 độ C.', 'ACTIVE') ON CONFLICT (MaCauHoi) DO NOTHING;
INSERT INTO DAP_AN (MaDapAn, MaCauHoi, KyHieu, NoiDung, LaDapAnDung) VALUES
(81, 21, 'A', '0 độ C', FALSE),
(82, 21, 'B', '4 độ C', TRUE),
(83, 21, 'C', '10 độ C', FALSE),
(84, 21, 'D', '100 độ C', FALSE)
ON CONFLICT (MaDapAn) DO NOTHING;

INSERT INTO CAU_HOI (MaCauHoi, MaChuDe, LoaiCauHoi, DoKho, NoiDung, HinhAnh, GiaiThich, TrangThai) VALUES (22, 2, 'STANDARD', 'EASY', 'Thủ đô của nước Cộng hòa Xã hội Chủ nghĩa Việt Nam là thành phố nào?', NULL, 'Hà Nội là thủ đô nghìn năm văn hiến của nước CHXHCN Việt Nam.', 'ACTIVE') ON CONFLICT (MaCauHoi) DO NOTHING;
INSERT INTO DAP_AN (MaDapAn, MaCauHoi, KyHieu, NoiDung, LaDapAnDung) VALUES
(85, 22, 'A', 'Đà Nẵng', FALSE),
(86, 22, 'B', 'Hà Nội', TRUE),
(87, 22, 'C', 'TP. Hồ Chí Minh', FALSE),
(88, 22, 'D', 'Hải Phòng', FALSE)
ON CONFLICT (MaDapAn) DO NOTHING;

INSERT INTO CAU_HOI (MaCauHoi, MaChuDe, LoaiCauHoi, DoKho, NoiDung, HinhAnh, GiaiThich, TrangThai) VALUES (23, 2, 'STANDARD', 'EASY', 'Đỉnh núi nào cao nhất thế giới và được mệnh danh là Nóc nhà của Trái Đất?', NULL, 'Đỉnh Everest thuộc dãy Himalaya với độ cao 8.848,86m là đỉnh núi cao nhất thế giới.', 'ACTIVE') ON CONFLICT (MaCauHoi) DO NOTHING;
INSERT INTO DAP_AN (MaDapAn, MaCauHoi, KyHieu, NoiDung, LaDapAnDung) VALUES
(89, 23, 'A', 'Phú Sĩ', FALSE),
(90, 23, 'B', 'Fansipan', FALSE),
(91, 23, 'C', 'Everest', TRUE),
(92, 23, 'D', 'K2', FALSE)
ON CONFLICT (MaDapAn) DO NOTHING;

INSERT INTO CAU_HOI (MaCauHoi, MaChuDe, LoaiCauHoi, DoKho, NoiDung, HinhAnh, GiaiThich, TrangThai) VALUES (24, 2, 'STANDARD', 'EASY', 'Việt Nam nằm ở khu vực địa lý nào của châu Á?', NULL, 'Việt Nam nằm ở khu vực Đông Nam Á (Southeast Asia).', 'ACTIVE') ON CONFLICT (MaCauHoi) DO NOTHING;
INSERT INTO DAP_AN (MaDapAn, MaCauHoi, KyHieu, NoiDung, LaDapAnDung) VALUES
(93, 24, 'A', 'Đông Á', FALSE),
(94, 24, 'B', 'Nam Á', FALSE),
(95, 24, 'C', 'Đông Nam Á', TRUE),
(96, 24, 'D', 'Tây Á', FALSE)
ON CONFLICT (MaDapAn) DO NOTHING;

INSERT INTO CAU_HOI (MaCauHoi, MaChuDe, LoaiCauHoi, DoKho, NoiDung, HinhAnh, GiaiThich, TrangThai) VALUES (25, 2, 'STANDARD', 'EASY', 'Quốc gia nào có diện tích lãnh thổ tự nhiên lớn nhất trên thế giới?', NULL, 'Nga là quốc gia rộng lớn nhất thế giới với diện tích hơn 17 triệu km2.', 'ACTIVE') ON CONFLICT (MaCauHoi) DO NOTHING;
INSERT INTO DAP_AN (MaDapAn, MaCauHoi, KyHieu, NoiDung, LaDapAnDung) VALUES
(97, 25, 'A', 'Canada', FALSE),
(98, 25, 'B', 'Hoa Kỳ', FALSE),
(99, 25, 'C', 'Nga', TRUE),
(100, 25, 'D', 'Trung Quốc', FALSE)
ON CONFLICT (MaDapAn) DO NOTHING;

INSERT INTO CAU_HOI (MaCauHoi, MaChuDe, LoaiCauHoi, DoKho, NoiDung, HinhAnh, GiaiThich, TrangThai) VALUES (26, 2, 'STANDARD', 'EASY', 'Chủ tịch Hồ Chí Minh đọc bản Tuyên ngôn Độc lập tại Quảng trường Ba Đình vào ngày tháng năm nào?', NULL, 'Ngày 2 tháng 9 năm 1945, Bác Hồ đọc Tuyên ngôn Độc lập khai sinh nước Việt Nam Dân chủ Cộng hòa.', 'ACTIVE') ON CONFLICT (MaCauHoi) DO NOTHING;
INSERT INTO DAP_AN (MaDapAn, MaCauHoi, KyHieu, NoiDung, LaDapAnDung) VALUES
(101, 26, 'A', '30/04/1975', FALSE),
(102, 26, 'B', '02/09/1945', TRUE),
(103, 26, 'C', '19/08/1945', FALSE),
(104, 26, 'D', '07/05/1954', FALSE)
ON CONFLICT (MaDapAn) DO NOTHING;

INSERT INTO CAU_HOI (MaCauHoi, MaChuDe, LoaiCauHoi, DoKho, NoiDung, HinhAnh, GiaiThich, TrangThai) VALUES (27, 2, 'STANDARD', 'MEDIUM', 'Con sông nào dài nhất thế giới chảy qua khu vực lục địa châu Phi?', NULL, 'Sông Nile với chiều dài khoảng 6.650 km là dòng sông dài nhất thế giới.', 'ACTIVE') ON CONFLICT (MaCauHoi) DO NOTHING;
INSERT INTO DAP_AN (MaDapAn, MaCauHoi, KyHieu, NoiDung, LaDapAnDung) VALUES
(105, 27, 'A', 'Sông Amazon', FALSE),
(106, 27, 'B', 'Sông Nile', TRUE),
(107, 27, 'C', 'Sông Mê Kông', FALSE),
(108, 27, 'D', 'Sông Dương Tử', FALSE)
ON CONFLICT (MaDapAn) DO NOTHING;

INSERT INTO CAU_HOI (MaCauHoi, MaChuDe, LoaiCauHoi, DoKho, NoiDung, HinhAnh, GiaiThich, TrangThai) VALUES (28, 2, 'STANDARD', 'MEDIUM', 'Vị vua nào của triều Nguyễn đã sáng lập triều đại và quyết định định đô tại Huế?', NULL, 'Vua Gia Long (Nguyễn Ánh) lên ngôi năm 1802 và định đô tại Phú Xuân (Huế).', 'ACTIVE') ON CONFLICT (MaCauHoi) DO NOTHING;
INSERT INTO DAP_AN (MaDapAn, MaCauHoi, KyHieu, NoiDung, LaDapAnDung) VALUES
(109, 28, 'A', 'Vua Gia Long', TRUE),
(110, 28, 'B', 'Vua Minh Mạng', FALSE),
(111, 28, 'C', 'Vua Tự Đức', FALSE),
(112, 28, 'D', 'Vua Bảo Đại', FALSE)
ON CONFLICT (MaDapAn) DO NOTHING;

INSERT INTO CAU_HOI (MaCauHoi, MaChuDe, LoaiCauHoi, DoKho, NoiDung, HinhAnh, GiaiThich, TrangThai) VALUES (29, 2, 'STANDARD', 'MEDIUM', 'Vịnh biển nào tại Việt Nam được UNESCO hai lần vinh danh là Di sản Thiên nhiên Thế giới?', NULL, 'Vịnh Hạ Long (Quảng Ninh) được UNESCO công nhận Di sản Thế giới vào năm 1994 và 2000.', 'ACTIVE') ON CONFLICT (MaCauHoi) DO NOTHING;
INSERT INTO DAP_AN (MaDapAn, MaCauHoi, KyHieu, NoiDung, LaDapAnDung) VALUES
(113, 29, 'A', 'Vịnh Cam Ranh', FALSE),
(114, 29, 'B', 'Vịnh Hạ Long', TRUE),
(115, 29, 'C', 'Vịnh Nha Trang', FALSE),
(116, 29, 'D', 'Vịnh Xuân Đài', FALSE)
ON CONFLICT (MaDapAn) DO NOTHING;

INSERT INTO CAU_HOI (MaCauHoi, MaChuDe, LoaiCauHoi, DoKho, NoiDung, HinhAnh, GiaiThich, TrangThai) VALUES (30, 2, 'STANDARD', 'MEDIUM', 'Quần thể Kim tự tháp Giza và tượng Nhân sư nổi tiếng thuộc quốc gia nào?', NULL, 'Kim tự tháp Giza là kỳ quan cổ đại nổi tiếng nằm ở Ai Cập (Egypt).', 'ACTIVE') ON CONFLICT (MaCauHoi) DO NOTHING;
INSERT INTO DAP_AN (MaDapAn, MaCauHoi, KyHieu, NoiDung, LaDapAnDung) VALUES
(117, 30, 'A', 'Hy Lạp', FALSE),
(118, 30, 'B', 'Ý', FALSE),
(119, 30, 'C', 'Ai Cập', TRUE),
(120, 30, 'D', 'Thổ Nhĩ Kỳ', FALSE)
ON CONFLICT (MaDapAn) DO NOTHING;

INSERT INTO CAU_HOI (MaCauHoi, MaChuDe, LoaiCauHoi, DoKho, NoiDung, HinhAnh, GiaiThich, TrangThai) VALUES (31, 2, 'STANDARD', 'HARD', 'Chiến tranh thế giới thứ hai (World War II) chính thức kết thúc vào năm nào?', NULL, 'Chiến tranh thế giới thứ hai chính thức kết thúc vào năm 1945 sau khi phát xít Nhật đầu hàng.', 'ACTIVE') ON CONFLICT (MaCauHoi) DO NOTHING;
INSERT INTO DAP_AN (MaDapAn, MaCauHoi, KyHieu, NoiDung, LaDapAnDung) VALUES
(121, 31, 'A', '1939', FALSE),
(122, 31, 'B', '1941', FALSE),
(123, 31, 'C', '1945', TRUE),
(124, 31, 'D', '1950', FALSE)
ON CONFLICT (MaDapAn) DO NOTHING;

INSERT INTO CAU_HOI (MaCauHoi, MaChuDe, LoaiCauHoi, DoKho, NoiDung, HinhAnh, GiaiThich, TrangThai) VALUES (32, 2, 'STANDARD', 'HARD', 'Hiệp định Genève về chấm dứt chiến sự và lập lại hòa bình ở Đông Dương được ký kết năm nào?', NULL, 'Hiệp định Genève được ký kết vào ngày 21/7/1954 sau thắng lợi Điện Biên Phủ.', 'ACTIVE') ON CONFLICT (MaCauHoi) DO NOTHING;
INSERT INTO DAP_AN (MaDapAn, MaCauHoi, KyHieu, NoiDung, LaDapAnDung) VALUES
(125, 32, 'A', '1946', FALSE),
(126, 32, 'B', '1954', TRUE),
(127, 32, 'C', '1968', FALSE),
(128, 32, 'D', '1973', FALSE)
ON CONFLICT (MaDapAn) DO NOTHING;

INSERT INTO CAU_HOI (MaCauHoi, MaChuDe, LoaiCauHoi, DoKho, NoiDung, HinhAnh, GiaiThich, TrangThai) VALUES (33, 2, 'STANDARD', 'HARD', 'Hoang mạc cát nóng nào có diện tích lớn nhất trên thế giới?', NULL, 'Sahara là hoang mạc nóng lớn nhất thế giới bao phủ phần lớn Bắc Phi với hơn 9 triệu km2.', 'ACTIVE') ON CONFLICT (MaCauHoi) DO NOTHING;
INSERT INTO DAP_AN (MaDapAn, MaCauHoi, KyHieu, NoiDung, LaDapAnDung) VALUES
(129, 33, 'A', 'Hoang mạc Gobi', FALSE),
(130, 33, 'B', 'Hoang mạc Sahara', TRUE),
(131, 33, 'C', 'Hoang mạc Kalahari', FALSE),
(132, 33, 'D', 'Hoang mạc Atacama', FALSE)
ON CONFLICT (MaDapAn) DO NOTHING;

INSERT INTO CAU_HOI (MaCauHoi, MaChuDe, LoaiCauHoi, DoKho, NoiDung, HinhAnh, GiaiThich, TrangThai) VALUES (34, 2, 'STANDARD', 'HARD', 'Kênh đào Panama nhân tạo nối liền hai đại dương lớn nào trên Trái Đất?', NULL, 'Kênh đào Panama cắt ngang eo đất Panama nối liền Đại Tây Dương và Thái Bình Dương.', 'ACTIVE') ON CONFLICT (MaCauHoi) DO NOTHING;
INSERT INTO DAP_AN (MaDapAn, MaCauHoi, KyHieu, NoiDung, LaDapAnDung) VALUES
(133, 34, 'A', 'Đại Tây Dương và Thái Bình Dương', TRUE),
(134, 34, 'B', 'Thái Bình Dương và Ấn Độ Dương', FALSE),
(135, 34, 'C', 'Bắc Băng Dương và Đại Tây Dương', FALSE),
(136, 34, 'D', 'Ấn Độ Dương và Địa Trung Hải', FALSE)
ON CONFLICT (MaDapAn) DO NOTHING;

INSERT INTO CAU_HOI (MaCauHoi, MaChuDe, LoaiCauHoi, DoKho, NoiDung, HinhAnh, GiaiThich, TrangThai) VALUES (35, 3, 'STANDARD', 'EASY', 'CPU trong máy tính là từ viết tắt của thuật ngữ tiếng Anh nào?', NULL, 'CPU là viết tắt của Central Processing Unit (Bộ xử lý trung tâm).', 'ACTIVE') ON CONFLICT (MaCauHoi) DO NOTHING;
INSERT INTO DAP_AN (MaDapAn, MaCauHoi, KyHieu, NoiDung, LaDapAnDung) VALUES
(137, 35, 'A', 'Central Processing Unit', TRUE),
(138, 35, 'B', 'Computer Power Unit', FALSE),
(139, 35, 'C', 'Control Program Unit', FALSE),
(140, 35, 'D', 'Core Processor Universal', FALSE)
ON CONFLICT (MaDapAn) DO NOTHING;

INSERT INTO CAU_HOI (MaCauHoi, MaChuDe, LoaiCauHoi, DoKho, NoiDung, HinhAnh, GiaiThich, TrangThai) VALUES (36, 3, 'STANDARD', 'EASY', 'Thiết bị ngoại vi nào sau đây dùng để nhập dữ liệu văn bản vào máy tính?', NULL, 'Bàn phím (Keyboard) là thiết bị nhập liệu văn bản cơ bản nhất.', 'ACTIVE') ON CONFLICT (MaCauHoi) DO NOTHING;
INSERT INTO DAP_AN (MaDapAn, MaCauHoi, KyHieu, NoiDung, LaDapAnDung) VALUES
(141, 36, 'A', 'Màn hình', FALSE),
(142, 36, 'B', 'Bàn phím', TRUE),
(143, 36, 'C', 'Loa âm thanh', FALSE),
(144, 36, 'D', 'Máy in', FALSE)
ON CONFLICT (MaDapAn) DO NOTHING;

INSERT INTO CAU_HOI (MaCauHoi, MaChuDe, LoaiCauHoi, DoKho, NoiDung, HinhAnh, GiaiThich, TrangThai) VALUES (37, 3, 'STANDARD', 'EASY', 'Đơn vị nhỏ nhất dùng để đo lường dung lượng thông tin số trong máy tính là gì?', NULL, 'Bit (viết tắt của Binary Digit) có giá trị 0 hoặc 1 là đơn vị thông tin nhỏ nhất.', 'ACTIVE') ON CONFLICT (MaCauHoi) DO NOTHING;
INSERT INTO DAP_AN (MaDapAn, MaCauHoi, KyHieu, NoiDung, LaDapAnDung) VALUES
(145, 37, 'A', 'Byte', FALSE),
(146, 37, 'B', 'Bit', TRUE),
(147, 37, 'C', 'Kilobyte', FALSE),
(148, 37, 'D', 'Megabyte', FALSE)
ON CONFLICT (MaDapAn) DO NOTHING;

INSERT INTO CAU_HOI (MaCauHoi, MaChuDe, LoaiCauHoi, DoKho, NoiDung, HinhAnh, GiaiThich, TrangThai) VALUES (38, 3, 'STANDARD', 'MEDIUM', 'Hệ thống phân giải tên miền DNS (Domain Name System) giữ vai trò chủ chốt nào trên mạng Internet?', NULL, 'DNS đóng vai trò như danh bạ Internet, chuyển đổi tên miền (quizzlive.com) thành địa chỉ IP máy chủ.', 'ACTIVE') ON CONFLICT (MaCauHoi) DO NOTHING;
INSERT INTO DAP_AN (MaDapAn, MaCauHoi, KyHieu, NoiDung, LaDapAnDung) VALUES
(149, 38, 'A', 'Mã hóa dữ liệu truyền tải', FALSE),
(150, 38, 'B', 'Phân giải tên miền thành địa chỉ IP', TRUE),
(151, 38, 'C', 'Cấp phát địa chỉ IP động cho máy trạm', FALSE),
(152, 38, 'D', 'Kiểm soát hàng đợi gửi email', FALSE)
ON CONFLICT (MaDapAn) DO NOTHING;

INSERT INTO CAU_HOI (MaCauHoi, MaChuDe, LoaiCauHoi, DoKho, NoiDung, HinhAnh, GiaiThich, TrangThai) VALUES (39, 3, 'STANDARD', 'MEDIUM', 'Bộ nhớ RAM (Random Access Memory) trong kiến trúc máy tính có đặc điểm cơ bản nào?', NULL, 'RAM là bộ nhớ khả biến (volatile), dữ liệu sẽ mất hoàn toàn khi tắt máy hoặc ngắt nguồn điện.', 'ACTIVE') ON CONFLICT (MaCauHoi) DO NOTHING;
INSERT INTO DAP_AN (MaDapAn, MaCauHoi, KyHieu, NoiDung, LaDapAnDung) VALUES
(153, 39, 'A', 'Lưu trữ vĩnh viễn không mất dữ liệu', FALSE),
(154, 39, 'B', 'Mất toàn bộ dữ liệu khi ngắt nguồn điện', TRUE),
(155, 39, 'C', 'Chỉ đọc dữ liệu và không thể ghi', FALSE),
(156, 39, 'D', 'Tốc độ truy xuất chậm hơn ổ cứng từ HDD', FALSE)
ON CONFLICT (MaDapAn) DO NOTHING;

INSERT INTO CAU_HOI (MaCauHoi, MaChuDe, LoaiCauHoi, DoKho, NoiDung, HinhAnh, GiaiThich, TrangThai) VALUES (40, 3, 'STANDARD', 'MEDIUM', 'Cổng (port) dịch vụ chuẩn mặc định của giao thức web bảo mật HTTPS là bao nhiêu?', NULL, 'HTTPS chạy trên cổng chuẩn 443, còn HTTP không mã hóa dùng cổng 80.', 'ACTIVE') ON CONFLICT (MaCauHoi) DO NOTHING;
INSERT INTO DAP_AN (MaDapAn, MaCauHoi, KyHieu, NoiDung, LaDapAnDung) VALUES
(157, 40, 'A', '80', FALSE),
(158, 40, 'B', '21', FALSE),
(159, 40, 'C', '443', TRUE),
(160, 40, 'D', '8080', FALSE)
ON CONFLICT (MaDapAn) DO NOTHING;

INSERT INTO CAU_HOI (MaCauHoi, MaChuDe, LoaiCauHoi, DoKho, NoiDung, HinhAnh, GiaiThich, TrangThai) VALUES (41, 3, 'STANDARD', 'MEDIUM', 'Ngôn ngữ lập trình nào với slogan Write Once, Run Anywhere được phát triển bởi Sun Microsystems năm 1995?', NULL, 'Java do James Gosling cùng cộng sự phát triển tại Sun Microsystems vào năm 1995.', 'ACTIVE') ON CONFLICT (MaCauHoi) DO NOTHING;
INSERT INTO DAP_AN (MaDapAn, MaCauHoi, KyHieu, NoiDung, LaDapAnDung) VALUES
(161, 41, 'A', 'Python', FALSE),
(162, 41, 'B', 'Java', TRUE),
(163, 41, 'C', 'C++', FALSE),
(164, 41, 'D', 'C#', FALSE)
ON CONFLICT (MaDapAn) DO NOTHING;

INSERT INTO CAU_HOI (MaCauHoi, MaChuDe, LoaiCauHoi, DoKho, NoiDung, HinhAnh, GiaiThich, TrangThai) VALUES (42, 3, 'STANDARD', 'MEDIUM', 'Địa chỉ giao thức Internet phiên bản 4 (IPv4) có tổng độ dài bao nhiêu bit?', NULL, 'Địa chỉ IPv4 gồm 32 bit, thường biểu diễn dưới dạng 4 số thập phân cách nhau bởi dấu chấm.', 'ACTIVE') ON CONFLICT (MaCauHoi) DO NOTHING;
INSERT INTO DAP_AN (MaDapAn, MaCauHoi, KyHieu, NoiDung, LaDapAnDung) VALUES
(165, 42, 'A', '16 bit', FALSE),
(166, 42, 'B', '32 bit', TRUE),
(167, 42, 'C', '64 bit', FALSE),
(168, 42, 'D', '128 bit', FALSE)
ON CONFLICT (MaDapAn) DO NOTHING;

INSERT INTO CAU_HOI (MaCauHoi, MaChuDe, LoaiCauHoi, DoKho, NoiDung, HinhAnh, GiaiThich, TrangThai) VALUES (43, 3, 'STANDARD', 'HARD', 'Mô hình chuẩn kết nối các hệ thống mở OSI (Open Systems Interconnection) gồm có bao nhiêu tầng?', NULL, 'Mô hình OSI gồm 7 tầng: Vật lý, Liên kết dữ liệu, Mạng, Giao vận, Phiên, Trình diễn, Ứng dụng.', 'ACTIVE') ON CONFLICT (MaCauHoi) DO NOTHING;
INSERT INTO DAP_AN (MaDapAn, MaCauHoi, KyHieu, NoiDung, LaDapAnDung) VALUES
(169, 43, 'A', '4 tầng', FALSE),
(170, 43, 'B', '5 tầng', FALSE),
(171, 43, 'C', '7 tầng', TRUE),
(172, 43, 'D', '9 tầng', FALSE)
ON CONFLICT (MaDapAn) DO NOTHING;

INSERT INTO CAU_HOI (MaCauHoi, MaChuDe, LoaiCauHoi, DoKho, NoiDung, HinhAnh, GiaiThich, TrangThai) VALUES (44, 3, 'STANDARD', 'HARD', 'Thuật toán sắp xếp nhanh QuickSort có độ phức tạp thời gian trung bình (Average Case) là bao nhiêu?', NULL, 'QuickSort có thời gian chạy trung bình là O(n log n) với phân hoạch chia để trị hiệu quả.', 'ACTIVE') ON CONFLICT (MaCauHoi) DO NOTHING;
INSERT INTO DAP_AN (MaDapAn, MaCauHoi, KyHieu, NoiDung, LaDapAnDung) VALUES
(173, 44, 'A', 'O(n)', FALSE),
(174, 44, 'B', 'O(n log n)', TRUE),
(175, 44, 'C', 'O(n^2)', FALSE),
(176, 44, 'D', 'O(log n)', FALSE)
ON CONFLICT (MaDapAn) DO NOTHING;

INSERT INTO CAU_HOI (MaCauHoi, MaChuDe, LoaiCauHoi, DoKho, NoiDung, HinhAnh, GiaiThich, TrangThai) VALUES (45, 3, 'STANDARD', 'HARD', 'Giao thức nào ở tầng Network chịu trách nhiệm báo lỗi và kiểm tra kết nối thông qua lệnh PING?', NULL, 'ICMP (Internet Control Message Protocol) được dùng để gửi thông điệp điều khiển và chẩn đoán mạng (Echo Request/Reply).', 'ACTIVE') ON CONFLICT (MaCauHoi) DO NOTHING;
INSERT INTO DAP_AN (MaDapAn, MaCauHoi, KyHieu, NoiDung, LaDapAnDung) VALUES
(177, 45, 'A', 'ARP', FALSE),
(178, 45, 'B', 'ICMP', TRUE),
(179, 45, 'C', 'IGMP', FALSE),
(180, 45, 'D', 'RARP', FALSE)
ON CONFLICT (MaDapAn) DO NOTHING;

INSERT INTO CAU_HOI (MaCauHoi, MaChuDe, LoaiCauHoi, DoKho, NoiDung, HinhAnh, GiaiThich, TrangThai) VALUES (46, 3, 'STANDARD', 'HARD', 'Trong kiến trúc CPU Von Neumann, khối chức năng nào chịu trách nhiệm thực hiện các phép toán số học và logic?', NULL, 'ALU (Arithmetic Logic Unit) là đơn vị số học và logic của vi xử lý.', 'ACTIVE') ON CONFLICT (MaCauHoi) DO NOTHING;
INSERT INTO DAP_AN (MaDapAn, MaCauHoi, KyHieu, NoiDung, LaDapAnDung) VALUES
(181, 46, 'A', 'Thanh ghi Register', FALSE),
(182, 46, 'B', 'Khối điều khiển CU', FALSE),
(183, 46, 'C', 'Khối số học logic ALU', TRUE),
(184, 46, 'D', 'Bus hệ thống', FALSE)
ON CONFLICT (MaDapAn) DO NOTHING;

INSERT INTO CAU_HOI (MaCauHoi, MaChuDe, LoaiCauHoi, DoKho, NoiDung, HinhAnh, GiaiThich, TrangThai) VALUES (47, 3, 'STANDARD', 'HARD', 'Giao thức bảo mật TLS/SSL sử dụng mật mã khóa công khai bất đối xứng chủ yếu cho mục đích nào?', NULL, 'TLS dùng mật mã bất đối xứng (như RSA/ECDH) ở bước Handshake để xác thực và trao đổi khóa đối xứng phiên.', 'ACTIVE') ON CONFLICT (MaCauHoi) DO NOTHING;
INSERT INTO DAP_AN (MaDapAn, MaCauHoi, KyHieu, NoiDung, LaDapAnDung) VALUES
(185, 47, 'A', 'Bắt tay (Handshake) xác thực và trao đổi khóa phiên', TRUE),
(186, 47, 'B', 'Mã hóa toàn bộ khối lượng dữ liệu truyền qua mạng', FALSE),
(187, 47, 'C', 'Kiểm tra chẵn lẻ kiểm soát lỗi luồng tin', FALSE),
(188, 47, 'D', 'Nén nội dung file truyền tải để tăng tốc độ', FALSE)
ON CONFLICT (MaDapAn) DO NOTHING;

INSERT INTO CAU_HOI (MaCauHoi, MaChuDe, LoaiCauHoi, DoKho, NoiDung, HinhAnh, GiaiThich, TrangThai) VALUES (48, 4, 'STANDARD', 'EASY', 'Tìm số tiếp theo trong dãy số chẵn tăng dần: 2, 4, 6, 8, ...?', NULL, 'Dãy số chẵn liên tiếp, số tiếp theo sau 8 là 10.', 'ACTIVE') ON CONFLICT (MaCauHoi) DO NOTHING;
INSERT INTO DAP_AN (MaDapAn, MaCauHoi, KyHieu, NoiDung, LaDapAnDung) VALUES
(189, 48, 'A', '9', FALSE),
(190, 48, 'B', '10', TRUE),
(191, 48, 'C', '12', FALSE),
(192, 48, 'D', '14', FALSE)
ON CONFLICT (MaDapAn) DO NOTHING;

INSERT INTO CAU_HOI (MaCauHoi, MaChuDe, LoaiCauHoi, DoKho, NoiDung, HinhAnh, GiaiThich, TrangThai) VALUES (49, 4, 'STANDARD', 'EASY', 'Một con vịt đi trước hai con vịt, một con vịt đi sau hai con vịt, một con vịt đi giữa hai con vịt. Hỏi có bao nhiêu con vịt?', NULL, 'Ba con vịt xếp thành 1 hàng dọc thỏa mãn mọi dữ kiện đề bài.', 'ACTIVE') ON CONFLICT (MaCauHoi) DO NOTHING;
INSERT INTO DAP_AN (MaDapAn, MaCauHoi, KyHieu, NoiDung, LaDapAnDung) VALUES
(193, 49, 'A', '3 con vịt', TRUE),
(194, 49, 'B', '4 con vịt', FALSE),
(195, 49, 'C', '5 con vịt', FALSE),
(196, 49, 'D', '6 con vịt', FALSE)
ON CONFLICT (MaDapAn) DO NOTHING;

INSERT INTO CAU_HOI (MaCauHoi, MaChuDe, LoaiCauHoi, DoKho, NoiDung, HinhAnh, GiaiThich, TrangThai) VALUES (50, 4, 'STANDARD', 'EASY', 'Nếu ngày hôm qua là thứ Năm, thì ngày mai sẽ là thứ mấy?', NULL, 'Hôm qua là thứ Năm thì hôm nay là thứ Sáu, ngày mai sẽ là thứ Bảy.', 'ACTIVE') ON CONFLICT (MaCauHoi) DO NOTHING;
INSERT INTO DAP_AN (MaDapAn, MaCauHoi, KyHieu, NoiDung, LaDapAnDung) VALUES
(197, 50, 'A', 'Thứ Sáu', FALSE),
(198, 50, 'B', 'Thứ Bảy', TRUE),
(199, 50, 'C', 'Chủ Nhật', FALSE),
(200, 50, 'D', 'Thứ Hai', FALSE)
ON CONFLICT (MaDapAn) DO NOTHING;

INSERT INTO CAU_HOI (MaCauHoi, MaChuDe, LoaiCauHoi, DoKho, NoiDung, HinhAnh, GiaiThich, TrangThai) VALUES (51, 4, 'STANDARD', 'EASY', 'Trong 4 từ sau đây, từ nào KHÔNG cùng nhóm phân loại với 3 từ còn lại?', NULL, 'Hình tròn, hình vuông, hình tam giác là các hình học, còn Màu đỏ là tên màu sắc.', 'ACTIVE') ON CONFLICT (MaCauHoi) DO NOTHING;
INSERT INTO DAP_AN (MaDapAn, MaCauHoi, KyHieu, NoiDung, LaDapAnDung) VALUES
(201, 51, 'A', 'Hình tròn', FALSE),
(202, 51, 'B', 'Hình vuông', FALSE),
(203, 51, 'C', 'Hình tam giác', FALSE),
(204, 51, 'D', 'Màu đỏ', TRUE)
ON CONFLICT (MaDapAn) DO NOTHING;

INSERT INTO CAU_HOI (MaCauHoi, MaChuDe, LoaiCauHoi, DoKho, NoiDung, HinhAnh, GiaiThich, TrangThai) VALUES (52, 4, 'STANDARD', 'EASY', 'Tìm số tiếp theo trong dãy số cộng 5: 5, 10, 15, 20, ...?', NULL, 'Mỗi số cách nhau 5 đơn vị: 20 + 5 = 25.', 'ACTIVE') ON CONFLICT (MaCauHoi) DO NOTHING;
INSERT INTO DAP_AN (MaDapAn, MaCauHoi, KyHieu, NoiDung, LaDapAnDung) VALUES
(205, 52, 'A', '22', FALSE),
(206, 52, 'B', '25', TRUE),
(207, 52, 'C', '30', FALSE),
(208, 52, 'D', '35', FALSE)
ON CONFLICT (MaDapAn) DO NOTHING;

INSERT INTO CAU_HOI (MaCauHoi, MaChuDe, LoaiCauHoi, DoKho, NoiDung, HinhAnh, GiaiThich, TrangThai) VALUES (53, 4, 'IQ_TEST', 'MEDIUM', 'Tìm số tiếp theo trong dãy số chính phương: 1, 4, 9, 16, 25, ...?', NULL, 'Dãy bình phương: 1^2, 2^2, 3^2, 4^2, 5^2, tiếp theo là 6^2 = 36.', 'ACTIVE') ON CONFLICT (MaCauHoi) DO NOTHING;
INSERT INTO DAP_AN (MaDapAn, MaCauHoi, KyHieu, NoiDung, LaDapAnDung) VALUES
(209, 53, 'A', '30', FALSE),
(210, 53, 'B', '36', TRUE),
(211, 53, 'C', '49', FALSE),
(212, 53, 'D', '64', FALSE)
ON CONFLICT (MaDapAn) DO NOTHING;

INSERT INTO CAU_HOI (MaCauHoi, MaChuDe, LoaiCauHoi, DoKho, NoiDung, HinhAnh, GiaiThich, TrangThai) VALUES (54, 4, 'IQ_TEST', 'MEDIUM', 'Dãy số Fibonacci: 1, 1, 2, 3, 5, 8, ... Số tiếp theo là số nào?', NULL, 'Mỗi số bằng tổng 2 số liền trước: 5 + 8 = 13.', 'ACTIVE') ON CONFLICT (MaCauHoi) DO NOTHING;
INSERT INTO DAP_AN (MaDapAn, MaCauHoi, KyHieu, NoiDung, LaDapAnDung) VALUES
(213, 54, 'A', '11', FALSE),
(214, 54, 'B', '12', FALSE),
(215, 54, 'C', '13', TRUE),
(216, 54, 'D', '15', FALSE)
ON CONFLICT (MaDapAn) DO NOTHING;

INSERT INTO CAU_HOI (MaCauHoi, MaChuDe, LoaiCauHoi, DoKho, NoiDung, HinhAnh, GiaiThich, TrangThai) VALUES (55, 4, 'IQ_TEST', 'MEDIUM', 'Nếu tất cả loài cá đều sống dưới nước, và cá heo là loài cá (theo logic giả định), thì kết luận nào đúng?', NULL, 'Theo tam đoạn luận diễn dịch, cá heo phải sống dưới nước.', 'ACTIVE') ON CONFLICT (MaCauHoi) DO NOTHING;
INSERT INTO DAP_AN (MaDapAn, MaCauHoi, KyHieu, NoiDung, LaDapAnDung) VALUES
(217, 55, 'A', 'Cá heo có thể sống trên cạn', FALSE),
(218, 55, 'B', 'Cá heo sống dưới nước', TRUE),
(219, 55, 'C', 'Cá heo không biết bơi', FALSE),
(220, 55, 'D', 'Không thể suy luận được', FALSE)
ON CONFLICT (MaDapAn) DO NOTHING;

INSERT INTO CAU_HOI (MaCauHoi, MaChuDe, LoaiCauHoi, DoKho, NoiDung, HinhAnh, GiaiThich, TrangThai) VALUES (56, 4, 'IQ_TEST', 'MEDIUM', 'Vào lúc đồng hồ chỉ đúng 3 giờ 00 phút, góc tạo bởi kim giờ và kim phút bằng bao nhiêu độ?', NULL, 'Kim phút ở số 12, kim giờ ở số 3, góc tạo thành là đúng 1/4 hình tròn = 90 độ.', 'ACTIVE') ON CONFLICT (MaCauHoi) DO NOTHING;
INSERT INTO DAP_AN (MaDapAn, MaCauHoi, KyHieu, NoiDung, LaDapAnDung) VALUES
(221, 56, 'A', '45 độ', FALSE),
(222, 56, 'B', '60 độ', FALSE),
(223, 56, 'C', '90 độ', TRUE),
(224, 56, 'D', '120 độ', FALSE)
ON CONFLICT (MaDapAn) DO NOTHING;

INSERT INTO CAU_HOI (MaCauHoi, MaChuDe, LoaiCauHoi, DoKho, NoiDung, HinhAnh, GiaiThich, TrangThai) VALUES (57, 4, 'IQ_TEST', 'MEDIUM', 'Một bể nước 100 lít có vòi bơm vào 10 lít/phút và vòi xả ra 5 lít/phút. Hỏi sau bao lâu bể đầy nước?', NULL, 'Tốc độ tăng thực tế là 10 - 5 = 5 lít/phút. Để đầy 100 lít cần: 100 / 5 = 20 phút.', 'ACTIVE') ON CONFLICT (MaCauHoi) DO NOTHING;
INSERT INTO DAP_AN (MaDapAn, MaCauHoi, KyHieu, NoiDung, LaDapAnDung) VALUES
(225, 57, 'A', '10 phút', FALSE),
(226, 57, 'B', '15 phút', FALSE),
(227, 57, 'C', '20 phút', TRUE),
(228, 57, 'D', '25 phút', FALSE)
ON CONFLICT (MaDapAn) DO NOTHING;

INSERT INTO CAU_HOI (MaCauHoi, MaChuDe, LoaiCauHoi, DoKho, NoiDung, HinhAnh, GiaiThich, TrangThai) VALUES (58, 4, 'IQ_TEST', 'HARD', 'Tìm số còn thiếu theo quy luật tích hai số liên tiếp: 2, 6, 12, 20, 30, ...?', NULL, 'Quy luật: 1*2=2, 2*3=6, 3*4=12, 4*5=20, 5*6=30, tiếp theo là 6*7 = 42.', 'ACTIVE') ON CONFLICT (MaCauHoi) DO NOTHING;
INSERT INTO DAP_AN (MaDapAn, MaCauHoi, KyHieu, NoiDung, LaDapAnDung) VALUES
(229, 58, 'A', '40', FALSE),
(230, 58, 'B', '42', TRUE),
(231, 58, 'C', '44', FALSE),
(232, 58, 'D', '48', FALSE)
ON CONFLICT (MaDapAn) DO NOTHING;

INSERT INTO CAU_HOI (MaCauHoi, MaChuDe, LoaiCauHoi, DoKho, NoiDung, HinhAnh, GiaiThich, TrangThai) VALUES (59, 4, 'IQ_TEST', 'HARD', 'Trong một cuộc chạy đua, nếu bạn bứt phá vượt qua người đang chạy ở vị trí thứ nhì, thì hiện tại bạn đang ở vị trí thứ mấy?', NULL, 'Bạn thế chỗ người thứ nhì nên hiện tại bạn đang ở vị trí thứ nhì (người dẫn đầu vẫn đứng thứ nhất).', 'ACTIVE') ON CONFLICT (MaCauHoi) DO NOTHING;
INSERT INTO DAP_AN (MaDapAn, MaCauHoi, KyHieu, NoiDung, LaDapAnDung) VALUES
(233, 59, 'A', 'Vị trí thứ nhất', FALSE),
(234, 59, 'B', 'Vị trí thứ nhì', TRUE),
(235, 59, 'C', 'Vị trí thứ ba', FALSE),
(236, 59, 'D', 'Vị trí cuối cùng', FALSE)
ON CONFLICT (MaDapAn) DO NOTHING;

INSERT INTO CAU_HOI (MaCauHoi, MaChuDe, LoaiCauHoi, DoKho, NoiDung, HinhAnh, GiaiThich, TrangThai) VALUES (60, 4, 'IQ_TEST', 'HARD', 'Tìm số tiếp theo trong dãy số: 3, 5, 9, 17, 33, ...?', NULL, 'Khoảng cách giữa các số tăng gấp đôi: +2, +4, +8, +16, tiếp theo là +32: 33 + 32 = 65.', 'ACTIVE') ON CONFLICT (MaCauHoi) DO NOTHING;
INSERT INTO DAP_AN (MaDapAn, MaCauHoi, KyHieu, NoiDung, LaDapAnDung) VALUES
(237, 60, 'A', '55', FALSE),
(238, 60, 'B', '65', TRUE),
(239, 60, 'C', '66', FALSE),
(240, 60, 'D', '70', FALSE)
ON CONFLICT (MaDapAn) DO NOTHING;

INSERT INTO CAU_HOI (MaCauHoi, MaChuDe, LoaiCauHoi, DoKho, NoiDung, HinhAnh, GiaiThich, TrangThai) VALUES (61, 4, 'IQ_TEST', 'HARD', 'Có 9 đồng tiền vàng giống nhau, trong đó có 1 đồng tiền giả nhẹ hơn. Dùng cân đĩa tối thiểu mấy lần để chắc chắn tìm ra?', NULL, 'Chia làm 3 nhóm (3, 3, 3). Cân lần 1 tìm ra nhóm chứa đồng giả. Chia nhóm 3 thành (1, 1, 1), cân lần 2 tìm ra đồng giả. Tối thiểu 2 lần.', 'ACTIVE') ON CONFLICT (MaCauHoi) DO NOTHING;
INSERT INTO DAP_AN (MaDapAn, MaCauHoi, KyHieu, NoiDung, LaDapAnDung) VALUES
(241, 61, 'A', '1 lần', FALSE),
(242, 61, 'B', '2 lần', TRUE),
(243, 61, 'C', '3 lần', FALSE),
(244, 61, 'D', '4 lần', FALSE)
ON CONFLICT (MaDapAn) DO NOTHING;

INSERT INTO CAU_HOI (MaCauHoi, MaChuDe, LoaiCauHoi, DoKho, NoiDung, HinhAnh, GiaiThich, TrangThai) VALUES (62, 4, 'IQ_TEST', 'HARD', 'Người nông dân qua sông với sói, dê, bắp cải trên thuyền chỉ chở được 1 thứ. Sói ăn dê, dê ăn bắp cải nếu không có người. Thứ nào BẮT BUỘC mang qua sông đầu tiên?', NULL, 'Bắt buộc mang Dê qua trước, vì nếu mang Sói thì Dê ăn Bắp cải, nếu mang Bắp cải thì Sói ăn Dê.', 'ACTIVE') ON CONFLICT (MaCauHoi) DO NOTHING;
INSERT INTO DAP_AN (MaDapAn, MaCauHoi, KyHieu, NoiDung, LaDapAnDung) VALUES
(245, 62, 'A', 'Con sói', FALSE),
(246, 62, 'B', 'Con dê', TRUE),
(247, 62, 'C', 'Bắp cải', FALSE),
(248, 62, 'D', 'Để sói và dê ở bờ cũ', FALSE)
ON CONFLICT (MaDapAn) DO NOTHING;

-- CÁC CÂU HỎI IQ TEST MỚI BỔ SUNG (63 - 72)
INSERT INTO CAU_HOI (MaCauHoi, MaChuDe, LoaiCauHoi, DoKho, NoiDung, HinhAnh, GiaiThich, TrangThai) VALUES (63, 4, 'IQ_TEST', 'MEDIUM', 'Một khối lập phương 3x3x3 được sơn màu đỏ bên ngoài rồi cắt thành 27 khối nhỏ 1x1x1. Có bao nhiêu khối nhỏ KHÔNG bị sơn mặt nào?', NULL, 'Chỉ có 1 khối duy nhất nằm ở trung tâm của khối lập phương lớn là không tiếp xúc mặt ngoài.', 'ACTIVE') ON CONFLICT (MaCauHoi) DO NOTHING;
INSERT INTO DAP_AN (MaDapAn, MaCauHoi, KyHieu, NoiDung, LaDapAnDung) VALUES
(249, 63, 'A', '0 khối', FALSE),
(250, 63, 'B', '1 khối', TRUE),
(251, 63, 'C', '6 khối', FALSE),
(252, 63, 'D', '8 khối', FALSE)
ON CONFLICT (MaDapAn) DO NOTHING;

INSERT INTO CAU_HOI (MaCauHoi, MaChuDe, LoaiCauHoi, DoKho, NoiDung, HinhAnh, GiaiThich, TrangThai) VALUES (64, 4, 'IQ_TEST', 'EASY', 'Điền số còn thiếu vào dấu chấm hỏi trong dãy số nguyên tố: 2, 3, 5, 7, 11, 13, ...?', NULL, 'Dãy các số nguyên tố tăng dần liên tiếp, sau 13 là số 17.', 'ACTIVE') ON CONFLICT (MaCauHoi) DO NOTHING;
INSERT INTO DAP_AN (MaDapAn, MaCauHoi, KyHieu, NoiDung, LaDapAnDung) VALUES
(253, 64, 'A', '15', FALSE),
(254, 64, 'B', '16', FALSE),
(255, 64, 'C', '17', TRUE),
(256, 64, 'D', '19', FALSE)
ON CONFLICT (MaDapAn) DO NOTHING;

INSERT INTO CAU_HOI (MaCauHoi, MaChuDe, LoaiCauHoi, DoKho, NoiDung, HinhAnh, GiaiThich, TrangThai) VALUES (65, 4, 'IQ_TEST', 'EASY', 'Bố của Nam có 5 người con trai tên là: Mười, Hai mươi, Ba mươi, Bốn mươi. Người con thứ năm tên là gì?', NULL, 'Đề bài nêu rõ ngay từ đầu: "Bố của Nam" nên người con thứ năm chính là Nam.', 'ACTIVE') ON CONFLICT (MaCauHoi) DO NOTHING;
INSERT INTO DAP_AN (MaDapAn, MaCauHoi, KyHieu, NoiDung, LaDapAnDung) VALUES
(257, 65, 'A', 'Năm mươi', FALSE),
(258, 65, 'B', 'Sáu mươi', FALSE),
(259, 65, 'C', 'Nam', TRUE),
(260, 65, 'D', 'Tám mươi', FALSE)
ON CONFLICT (MaDapAn) DO NOTHING;

INSERT INTO CAU_HOI (MaCauHoi, MaChuDe, LoaiCauHoi, DoKho, NoiDung, HinhAnh, GiaiThich, TrangThai) VALUES (66, 4, 'IQ_TEST', 'EASY', 'Có 3 quả táo trên bàn, bạn cầm lấy và mang đi 2 quả. Hỏi BẠN hiện tại có bao nhiêu quả táo?', NULL, 'Bạn cầm đi 2 quả táo nên chính bạn đang có 2 quả táo.', 'ACTIVE') ON CONFLICT (MaCauHoi) DO NOTHING;
INSERT INTO DAP_AN (MaDapAn, MaCauHoi, KyHieu, NoiDung, LaDapAnDung) VALUES
(261, 66, 'A', '1 quả', FALSE),
(262, 66, 'B', '2 quả', TRUE),
(263, 66, 'C', '3 quả', FALSE),
(264, 66, 'D', '0 quả', FALSE)
ON CONFLICT (MaDapAn) DO NOTHING;

INSERT INTO CAU_HOI (MaCauHoi, MaChuDe, LoaiCauHoi, DoKho, NoiDung, HinhAnh, GiaiThich, TrangThai) VALUES (67, 4, 'IQ_TEST', 'HARD', 'Tìm số tiếp theo trong dãy số suy luận logic: 88, 64, 24, ...?', NULL, 'Quy luật tích các chữ số: 8*8=64, 6*4=24, 2*4=8.', 'ACTIVE') ON CONFLICT (MaCauHoi) DO NOTHING;
INSERT INTO DAP_AN (MaDapAn, MaCauHoi, KyHieu, NoiDung, LaDapAnDung) VALUES
(265, 67, 'A', '8', TRUE),
(266, 67, 'B', '12', FALSE),
(267, 67, 'C', '16', FALSE),
(268, 67, 'D', '20', FALSE)
ON CONFLICT (MaDapAn) DO NOTHING;

INSERT INTO CAU_HOI (MaCauHoi, MaChuDe, LoaiCauHoi, DoKho, NoiDung, HinhAnh, GiaiThich, TrangThai) VALUES (68, 4, 'IQ_TEST', 'EASY', 'Một đoàn tàu điện chạy về hướng Nam với vận tốc 60 km/h, gió thổi về hướng Bắc 30 km/h. Khói của tàu bay về hướng nào?', NULL, 'Tàu điện chạy bằng điện nên hoàn toàn không nhả khói.', 'ACTIVE') ON CONFLICT (MaCauHoi) DO NOTHING;
INSERT INTO DAP_AN (MaDapAn, MaCauHoi, KyHieu, NoiDung, LaDapAnDung) VALUES
(269, 68, 'A', 'Hướng Bắc', FALSE),
(270, 68, 'B', 'Hướng Nam', FALSE),
(271, 68, 'C', 'Hướng Tây', FALSE),
(272, 68, 'D', 'Không có khói', TRUE)
ON CONFLICT (MaDapAn) DO NOTHING;

INSERT INTO CAU_HOI (MaCauHoi, MaChuDe, LoaiCauHoi, DoKho, NoiDung, HinhAnh, GiaiThich, TrangThai) VALUES (69, 4, 'IQ_TEST', 'MEDIUM', 'Tìm đối tượng khác biệt nhất so với các đối tượng còn lại trong nhóm hình học sau:', NULL, 'Hình cầu là hình không gian 3 chiều (3D), các hình còn lại là hình học phẳng 2 chiều (2D).', 'ACTIVE') ON CONFLICT (MaCauHoi) DO NOTHING;
INSERT INTO DAP_AN (MaDapAn, MaCauHoi, KyHieu, NoiDung, LaDapAnDung) VALUES
(273, 69, 'A', 'Tam giác đều', FALSE),
(274, 69, 'B', 'Hình thoi', FALSE),
(275, 69, 'C', 'Hình chữ nhật', FALSE),
(276, 69, 'D', 'Hình cầu', TRUE)
ON CONFLICT (MaDapAn) DO NOTHING;

INSERT INTO CAU_HOI (MaCauHoi, MaChuDe, LoaiCauHoi, DoKho, NoiDung, HinhAnh, GiaiThich, TrangThai) VALUES (70, 4, 'IQ_TEST', 'MEDIUM', 'Nếu 1 con mèo bắt 1 con chuột trong 1 phút, thì 100 con mèo bắt 100 con chuột trong bao lâu?', NULL, 'Mỗi con mèo bắt một con chuột song song cùng lúc, do đó 100 con mèo bắt 100 con chuột cũng chỉ mất đúng 1 phút.', 'ACTIVE') ON CONFLICT (MaCauHoi) DO NOTHING;
INSERT INTO DAP_AN (MaDapAn, MaCauHoi, KyHieu, NoiDung, LaDapAnDung) VALUES
(277, 70, 'A', '1 phút', TRUE),
(278, 70, 'B', '10 phút', FALSE),
(279, 70, 'C', '50 phút', FALSE),
(280, 70, 'D', '100 phút', FALSE)
ON CONFLICT (MaDapAn) DO NOTHING;

INSERT INTO CAU_HOI (MaCauHoi, MaChuDe, LoaiCauHoi, DoKho, NoiDung, HinhAnh, GiaiThich, TrangThai) VALUES (71, 4, 'IQ_TEST', 'HARD', 'Tìm số tiếp theo: 2, 4, 12, 48, 240, ...?', NULL, 'Nhân liên tiếp tăng dần: 2*2=4, 4*3=12, 12*4=48, 48*5=240, tiếp theo 240*6 = 1440.', 'ACTIVE') ON CONFLICT (MaCauHoi) DO NOTHING;
INSERT INTO DAP_AN (MaDapAn, MaCauHoi, KyHieu, NoiDung, LaDapAnDung) VALUES
(281, 71, 'A', '720', FALSE),
(282, 71, 'B', '1200', FALSE),
(283, 71, 'C', '1440', TRUE),
(284, 71, 'D', '1680', FALSE)
ON CONFLICT (MaDapAn) DO NOTHING;

INSERT INTO CAU_HOI (MaCauHoi, MaChuDe, LoaiCauHoi, DoKho, NoiDung, HinhAnh, GiaiThich, TrangThai) VALUES (72, 4, 'IQ_TEST', 'EASY', 'Trong một năm Dương lịch, có những tháng 31 ngày, có những tháng 30 ngày. Có bao nhiêu tháng có ít nhất 28 ngày?', NULL, 'Tất cả 12 tháng trong năm đều có ít nhất 28 ngày.', 'ACTIVE') ON CONFLICT (MaCauHoi) DO NOTHING;
INSERT INTO DAP_AN (MaDapAn, MaCauHoi, KyHieu, NoiDung, LaDapAnDung) VALUES
(285, 72, 'A', '1 tháng', FALSE),
(286, 72, 'B', '2 tháng', FALSE),
(287, 72, 'C', '11 tháng', FALSE),
(288, 72, 'D', '12 tháng', TRUE)
ON CONFLICT (MaDapAn) DO NOTHING;

-- Câu 73: IQ Test Bằng Hình Ảnh (Loại IQ_TEST)
INSERT INTO CAU_HOI (MaCauHoi, MaChuDe, LoaiCauHoi, DoKho, NoiDung, HinhAnh, GiaiThich, TrangThai) VALUES (73, 4, 'IQ_TEST', 'HARD', 'Tìm mảnh ghép logic phù hợp nhất để hoàn thiện ma trận hình học 3x3:', 'iq_pattern_02.png', 'Mỗi hàng là tổng hợp số cạnh tăng dần 3, 4, 5.', 'ACTIVE') ON CONFLICT (MaCauHoi) DO NOTHING;
INSERT INTO DAP_AN (MaDapAn, MaCauHoi, KyHieu, NoiDung, HinhAnh, LaDapAnDung) VALUES
(289, 73, 'A', 'Hình ngũ giác có chấm tròn giữa', 'iq_ans_02_a.png', TRUE),
(290, 73, 'B', 'Hình tam giác rỗng', 'iq_ans_02_b.png', FALSE),
(291, 73, 'C', 'Hình tròn sọc caro', 'iq_ans_02_c.png', FALSE),
(292, 73, 'D', 'Hình vuông có dấu cộng', 'iq_ans_02_d.png', FALSE)
ON CONFLICT (MaDapAn) DO NOTHING;



-- 6. Lịch sử trận đấu mẫu (Yêu cầu 10)
INSERT INTO TRAN_DAU (MaTran, MaTranCode, LoaiTran, MaChuDe, DoKho, TrangThai, ThoiGianBatDau, ThoiGianKetThuc, NguoiThang) VALUES
(1, 'MATCH_1001', 'RANKED', 1, 'EASY', 'FINISHED', '2026-11-06 14:00:00', '2026-11-06 14:02:15', 2),
(2, 'MATCH_1002', 'RANKED', 2, 'MEDIUM', 'FINISHED', '2026-11-06 15:30:00', '2026-11-06 15:32:10', 1),
(3, 'MATCH_1003', 'RANKED', 3, 'MEDIUM', 'FINISHED', '2026-11-06 16:45:00', '2026-11-06 16:47:05', 1),
(4, 'MATCH_1004', 'CHALLENGE', 4, 'HARD', 'FINISHED', '2026-11-06 18:10:00', '2026-11-06 18:12:30', 2),
(5, 'MATCH_1005', 'RANKED', 3, 'HARD', 'FINISHED', '2026-11-06 20:00:00', '2026-11-06 20:02:40', 3)
ON CONFLICT (MaTran) DO NOTHING;

INSERT INTO NGUOI_CHOI_TRAN (MaTran, MaNguoiDung, ViTri, Diem, SoCauDung, SoCauSai, SoCauKhongTraLoi, XepHang, ThoiGianThamGia, ThoiGianHoanThanh) VALUES
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
ON CONFLICT (MaTran, MaNguoiDung) DO NOTHING;

-- 7. Dữ liệu mẫu Giải Đấu & Xếp Hạng Giải (Requirement 9)
INSERT INTO GIAI_DAU (MaGiai, TenGiai, MoTa, TrangThai, SoNguoiToiDa, ThoiGianBatDau) VALUES
(1, 'Đại Chiến Tri Thức SGU 2026', 'Giải đấu trắc nghiệm kiến thức đa lĩnh vực toàn trường', 'UPCOMING', 16, '2026-12-01 09:00:00'),
(2, 'Cúp QuizzLive Mùa Xuân 2026', 'Giải đấu 8 thí sinh tranh tài gay cấn', 'IN_PROGRESS', 8, '2026-11-07 08:00:00')
ON CONFLICT (MaGiai) DO NOTHING;

-- Bảng xếp hạng / Thí sinh tham gia giải đấu số 2 (Đã chia Bảng A và Bảng B đầy đủ)
INSERT INTO XEP_HANG_GIAI (MaGiai, MaNguoiDung, BangDau, HatGiong, SoTran, SoTranThang, SoTranHoa, SoTranThua, TongDiem, ViTri) VALUES
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
ON CONFLICT (MaGiai, MaNguoiDung) DO NOTHING;

-- 8. Đồng bộ tự động các sequence khóa chính sau khi nạp dữ liệu mẫu (Quan trọng cho PostgreSQL)
SELECT setval(pg_get_serial_sequence('CAU_HINH_TRAN', 'macauhinh'), COALESCE((SELECT MAX(MaCauHinh) FROM CAU_HINH_TRAN), 1));
SELECT setval(pg_get_serial_sequence('CHU_DE', 'machude'), COALESCE((SELECT MAX(MaChuDe) FROM CHU_DE), 1));
SELECT setval(pg_get_serial_sequence('NGUOI_DUNG', 'manguoidung'), COALESCE((SELECT MAX(MaNguoiDung) FROM NGUOI_DUNG), 1));
SELECT setval(pg_get_serial_sequence('CAU_HOI', 'macauhoi'), COALESCE((SELECT MAX(MaCauHoi) FROM CAU_HOI), 1));
SELECT setval(pg_get_serial_sequence('DAP_AN', 'madapan'), COALESCE((SELECT MAX(MaDapAn) FROM DAP_AN), 1));
SELECT setval(pg_get_serial_sequence('GIAI_DAU', 'magiai'), COALESCE((SELECT MAX(MaGiai) FROM GIAI_DAU), 1));
SELECT setval(pg_get_serial_sequence('TRAN_DAU', 'matran'), COALESCE((SELECT MAX(MaTran) FROM TRAN_DAU), 1));

