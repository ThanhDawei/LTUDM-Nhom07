# QUIZZLIVE - HỆ THỐNG TRANH TÀI KIẾN THỨC (NHÓM 07 - ĐỀ TÀI 09)

Hệ thống thi đấu trắc nghiệm thời gian thực (Real-time Knowledge Quiz Battle) phát triển bằng **Java Multi-Module (Maven)**, hỗ trợ kiến trúc Client-Server đa luồng, Socket TCP thuần của JDK (`java.net.Socket`), mã hóa TLS/SSL, CSDL quan hệ (MySQL / H2) và giao diện Quản trị Server GUI theo đúng tiêu chuẩn môn học Lập trình ứng dụng mạng - Trường ĐH Sài Gòn (SGU).

---

## 📋 Bảng Đối Chiếu 16 Yêu Cầu Chức Năng (Tiêu chí A1)

| STT | Phía | Nội dung yêu cầu | Package / Class phụ trách |
|:---:|:---:|---|---|
| **1** | Client/Server | Đăng ký, đăng nhập, cập nhật thông tin cá nhân, xác thực email bằng OTP (10 phút). | `AuthService`, `UserDAO`, `OtpDAO`, `AuthDTO` |
| **2** | Client/Server | Ghép cặp tự động trong 60 giây; hết 60s chưa ghép được thì đưa về phòng chờ. | `MatchmakingService`, `MatchmakingDTO` |
| **3** | Client/Server | Thách đấu bạn bè: mời trực tiếp một người đang online vào trận. | `ChallengeService`, `ChallengeDTO` |
| **4** | Client | Chọn chủ đề / độ khó câu hỏi trước khi thi. | `QuestionDAO`, `TopicDTO`, `Topic` |
| **5** | Client/Server | Mỗi trận 5 câu (cấu hình được), mỗi câu 4 đáp án, tối đa 10s; tính điểm theo thời gian còn lại; sai/không trả lời được 0 điểm. | `BattleRoom`, `MatchConfigDAO`, `BattleDTO` |
| **6** | Client/Server | Chuyển câu khi CẢ HAI đã trả lời hoặc HẾT 10s; khi đó mỗi người thấy lựa chọn của đối thủ và đáp án đúng. | `BattleRoom.submitAnswer()`, `onBothAnswered()` |
| **7** | Client/Server | Câu hỏi trong một trận không trùng nhau; thứ tự câu và đáp án được xáo trộn. | `QuestionDAO.getRandomQuestions()`, `Collections.shuffle()` |
| **8** | Client/Server | Kết quả trận: sau 5 câu, ai điểm cao hơn thắng (lưu thành tích vào DB). | `BattleRoom.finishMatch()`, `UserDAO.updateMatchResult()` |
| **9** | Client/Server | Giải đấu: tổ chức giải nhiều người theo vòng/bảng, có bảng xếp hạng giải. | `TournamentService`, `TournamentDTO` |
| **10** | Client/Server | Xếp hạng chung: thứ hạng, chuỗi thắng/thua dài nhất, tỉ lệ thắng/thua; xem lịch sử trận. | `StatisticDAO`, `LeaderboardDTO` |
| **11** | Client/Server | Test IQ bằng hình ảnh trong lúc chờ ghép cặp; không dùng xếp hạng; điểm IQ lưu vào hồ sơ. | `IQTestService`, `IQTestDTO`, `CAU_HOI (IQ_IMAGE)` |
| **12** | Server | GUI quản trị: thêm/sửa câu hỏi trong ngân hàng (phân loại theo chủ đề/độ khó). | `ServerMainFrame` (Tab 2: Ngân Hàng Câu Hỏi) |
| **13** | Server | GUI quản trị: cấu hình số câu, thời gian mỗi câu và điểm trận đấu. | `ServerMainFrame` (Tab 3: Cấu Hình Trận Đấu) |
| **14** | Server | Điều phối ghép cặp, trận đấu và giải đấu; đồng bộ hai người chơi. | `BattleManager`, `BattleRoom`, `MatchmakingService` |
| **15** | Server | Thống kê tổng số người chơi, số đang online; khóa người dùng. | `ServerMainFrame` (Tab 1), `StatisticDAO` |
| **16** | Server | Truy vấn cơ bản: người tham gia nhiều trận nhất, thắng nhiều nhất, chuỗi thắng dài nhất. | `ServerMainFrame` (Tab 4), `StatisticDAO` |

---

## 🗄️ Cấu trúc Cơ sở dữ liệu (Database Schema)

CSDL được thiết kế chuẩn xác 100% theo sơ đồ thực thể liên kết (ERD) với 23 bảng:
- **Tài khoản & Thống kê**: `NGUOI_DUNG`, `HO_SO_NGUOI_DUNG`, `OTP`, `THONG_KE_NGUOI_CHOI`, `CHAN_NGUOI_DUNG`, `QUAN_TRI_VIEN`
- **Ngân hàng câu hỏi & Cấu hình**: `CHU_DE`, `CAU_HOI`, `DAP_AN`, `CAU_HINH_TRAN`
- **Trận đấu & Đồng bộ realtime**: `HANG_DOI_GHEP_CAP`, `TRAN_DAU`, `LOI_MOI_THACH_DAU`, `NGUOI_CHOI_TRAN`, `CAU_HOI_TRAN`, `THU_TU_DAP_AN_TRAN`, `TRA_LOI`
- **Giải đấu**: `GIAI_DAU`, `VONG_DAU`, `BANG_DAU`, `TRAN_DAU_GIAI`, `NGUOI_THAM_GIA_GIAI`, `XEP_HANG_GIAI`

Script SQL đầy đủ nằm tại:
- `quizzlive-server/src/main/resources/database/schema.sql` (Tạo bảng, Foreign Keys, Indexes)
- `quizzlive-server/src/main/resources/database/seed_data.sql` (Dữ liệu mẫu)

---

## 🚀 Hướng Dẫn Biên Dịch & Chạy Server

### 1. Biên dịch toàn bộ dự án
```powershell
mvn clean compile
```

### 2. Cấu hình CSDL trong `quizzlive-server/src/main/resources/application.properties`
Mặc định server sẽ tự động kết nối tới MySQL `localhost:3306/quizzlive_db`.  
Nếu máy chưa bật MySQL, hệ thống tự động fallback sang cơ sở dữ liệu nhúng **H2 (in-memory MySQL mode)** và tự động nạp bảng + dữ liệu mẫu.

### 3. Chạy Server
```powershell
mvn -pl quizzlive-server exec:java -Dexec.mainClass="com.quizzlive.server.ServerApp"
```
