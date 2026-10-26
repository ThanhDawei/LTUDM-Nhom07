# QUIZZLIVE - HỆ THỐNG TRANH TÀI KIẾN THỨC
### Đồ án môn học: Lập trình ứng dụng mạng (Mã HP: 841438)
**Trường Đại học Sài Gòn (SGU) - Khoa Công nghệ Thông tin**  
**Nhóm thực hiện**: Nhóm 07  
**Đề tài 09**: Tranh tài kiến thức (Thi đấu trắc nghiệm thời gian thực)

---

## 📖 Giới thiệu dự án
**Quizzlive** là một hệ thống thi đấu trắc nghiệm trực tuyến thời gian thực (Real-time 1v1 Quiz Battle) được phát triển bằng ngôn ngữ **Java** với kiến trúc **Maven Multi-Module**. Hệ thống hỗ trợ ghép cặp ngẫu nhiên trong 60 giây, thách đấu bạn bè trực tiếp khi online, thi đấu đối kháng thời gian thực 5 câu hỏi (đếm ngược 10 giây/câu), kiểm tra IQ hình ảnh trong sảnh chờ, tổ chức giải đấu theo vòng/bảng và giao diện đồ họa (GUI) quản trị câu hỏi và thống kê người dùng dành cho Server.

Dự án tuân thủ nghiêm ngặt **100% quy định kỹ thuật của môn học**:
- Giao tiếp Client - Server được lập trình **trực tiếp bằng Java Sockets thuần (`java.net.Socket`, `java.net.ServerSocket`)**.
- Nhóm **tự thiết kế giao thức mạng**: Định dạng gói tin (`Packet.java`), danh sách lệnh (`OpCode.java`), đóng gói dữ liệu JSON bằng **Gson**.
- **Xử lý đa luồng (Multithreading)**: Thread Pool (`ExecutorService`), hẹn giờ `ScheduledExecutorService` cho từng trận đấu, đảm bảo phục vụ đồng thời nhiều cặp đấu.
- **TUYỆT ĐỐI KHÔNG SỬ DỤNG** các framework mạng bị cấm như: Netty, gRPC, Apache MINA, KryoNet, WebSocket, Spring Web, HTTP REST API, Java RMI.
- **Client hoàn toàn KHÔNG kết nối trực tiếp CSDL**: Mọi thao tác đều thông qua tầng Server xử lý tập trung.

---

## 📋 Bảng Đối Chiếu 16 Yêu Cầu Chức Năng (Tiêu chí chấm điểm A1)

| STT | Phía | Nội dung yêu cầu chức năng | Vị trí File / Class phụ trách |
|:---:|:---:|---|---|
| **1** | Client/Server | Đăng ký tài khoản, đăng nhập, cập nhật thông tin cá nhân, **xác thực email bằng mã OTP** (thời hạn 10 phút). | `com.quizzlive.server.service.AuthService`<br>`com.quizzlive.server.dao.OtpDAO`<br>`com.quizzlive.shared.dto.AuthDTO` |
| **2** | Client/Server | **Ghép cặp tự động trong 60 giây**; hết thời gian chưa ghép được thì gửi thông báo đưa người chơi về phòng chờ. | `com.quizzlive.server.service.MatchmakingService`<br>`com.quizzlive.shared.dto.MatchmakingDTO` |
| **3** | Client/Server | **Thách đấu bạn bè**: Mời trực tiếp một người đang online vào trận đấu 1v1. | `com.quizzlive.server.service.ChallengeService`<br>`com.quizzlive.shared.dto.ChallengeDTO` |
| **4** | Client | **Chọn chủ đề và độ khó** câu hỏi trước khi thi đấu (EASY, MEDIUM, HARD). | `com.quizzlive.server.dao.QuestionDAO`<br>`com.quizzlive.shared.dto.TopicDTO` |
| **5** | Client/Server | Mỗi trận 5 câu (cấu hình được), mỗi câu 4 đáp án, tối đa 10 giây; **điểm tính theo thời gian còn lại**; trả lời sai hoặc không trả lời được 0 điểm. | `com.quizzlive.server.service.BattleRoom`<br>`com.quizzlive.server.dao.MatchConfigDAO` |
| **6** | Client/Server | **Chuyển câu khi CẢ HAI đã trả lời hoặc HẾT 10 giây**; khi đó mỗi người thấy lựa chọn của đối thủ và đáp án đúng. | `BattleRoom.submitAnswer()`<br>`BattleRoom.onBothAnswered()` |
| **7** | Client/Server | Câu hỏi trong một trận không trùng nhau; **thứ tự câu hỏi và thứ tự đáp án được xáo trộn ngẫu nhiên** ở mỗi ván đấu. | `QuestionDAO.getRandomQuestions()`<br>`BattleRoom.nextQuestion()` |
| **8** | Client/Server | **Kết quả trận**: Sau 5 câu, ai điểm cao hơn thắng (cập nhật thành tích, chuỗi thắng/thua vào CSDL). | `BattleRoom.finishMatch()`<br>`UserDAO.updateMatchResult()` |
| **9** | Client/Server | **Giải đấu**: Tổ chức giải nhiều người theo vòng/bảng, có bảng xếp hạng của giải đấu. | `com.quizzlive.server.service.TournamentService`<br>`com.quizzlive.shared.dto.TournamentDTO` |
| **10** | Client/Server | **Xếp hạng chung**: Xem thứ hạng, chuỗi thắng/thua dài nhất, tỉ lệ thắng/thua; xem lịch sử trận đấu. | `com.quizzlive.server.dao.StatisticDAO`<br>`com.quizzlive.shared.dto.LeaderboardDTO` |
| **11** | Client/Server | **Test IQ bằng hình ảnh trong lúc chờ ghép cặp**; không dùng để xếp hạng; điểm IQ được lưu vào hồ sơ cá nhân. | `com.quizzlive.server.service.IQTestService`<br>`com.quizzlive.shared.dto.IQTestDTO` |
| **12** | Server | **GUI quản trị**: Thêm, sửa, xóa câu hỏi trong ngân hàng câu hỏi (phân loại theo chủ đề/độ khó, có hình ảnh). | `com.quizzlive.server.gui.ServerMainFrame`<br>*(Tab 2: Ngân hàng câu hỏi)* |
| **13** | Server | **GUI quản trị**: Cấu hình số câu hỏi mỗi trận (mặc định 5), thời gian mỗi câu (mặc định 10s) và điểm trận đấu. | `com.quizzlive.server.gui.ServerMainFrame`<br>*(Tab 3: Cấu hình trận đấu)* |
| **14** | Server | **Điều phối ghép cặp, trận đấu và giải đấu**; đồng bộ trạng thái giữa hai người chơi. | `com.quizzlive.server.service.BattleManager`<br>`com.quizzlive.server.service.BattleRoom` |
| **15** | Server | Thống kê tổng số người chơi, số đang online; **Khóa / Mở khóa người dùng**. | `com.quizzlive.server.gui.ServerMainFrame`<br>*(Tab 1: Quản lý người dùng)* |
| **16** | Server | **Truy vấn cơ bản**: Người tham gia nhiều trận nhất, người thắng nhiều nhất, người có chuỗi thắng dài nhất. | `com.quizzlive.server.gui.ServerMainFrame`<br>*(Tab 4: Thống kê & Báo cáo)* |

---

## 🏗️ Kiến Trúc Thư Mục Dự Án (Project Structure)

```text
Quizzlive/
├── pom.xml                                          # Parent POM quản lý Java 17 và phiên bản thư viện chung
├── README.md                                        # Tài liệu dự án & Hướng dẫn kỹ thuật
│
├── quizzlive-shared/                                # MODULE 1: THƯ VIỆN GIAO THỨC CHUNG (Client & Server)
│   ├── pom.xml
│   └── src/main/java/com/quizzlive/shared/
│       ├── protocol/
│       │   ├── OpCode.java                          # Danh mục hơn 30 mã lệnh mạng (Enum)
│       │   └── Packet.java                          # Đối tượng đóng gói dữ liệu Socket (OpCode + Payload)
│       ├── dto/                                     # Data Transfer Objects (DTO) cho từng gói tin
│       │   ├── AuthDTO.java                         # Đăng ký, Đăng nhập, Xác thực OTP
│       │   ├── MatchmakingDTO.java                  # Hàng đợi tìm trận, Thông báo tìm thấy trận
│       │   ├── ChallengeDTO.java                    # Lời mời thách đấu 1v1 bạn bè
│       │   ├── BattleDTO.java                       # Câu hỏi thi đấu, Gửi đáp án, Kết quả vòng & Chung cuộc
│       │   ├── IQTestDTO.java                       # Câu hỏi test IQ hình ảnh & Cập nhật điểm IQ
│       │   ├── LeaderboardDTO.java                  # Bảng xếp hạng, Top thắng, Chuỗi dài nhất
│       │   ├── TournamentDTO.java                   # Danh sách giải đấu, Bảng đấu, Xếp hạng giải
│       │   └── TopicDTO.java                        # Danh mục chủ đề thi đấu
│       ├── model/
│       │   ├── UserDTO.java                         # Thông tin người chơi truyền mạng
│       │   └── QuestionDTO.java                     # Thông tin câu hỏi truyền mạng
│       └── util/
│           └── JsonUtil.java                        # Tiện ích chuyển đổi JSON tốc độ cao bằng Gson
│
├── quizzlive-server/                                # MODULE 2: PHÍA BACKEND SERVER
│   ├── pom.xml                                      # Quản lý driver CSDL (PostgreSQL, MySQL, HikariCP, H2)
│   ├── Dockerfile                                   # Hỗ trợ đóng gói Container chạy trên VPS / Cloud
│   └── src/main/
│       ├── resources/
│       │   ├── application.properties               # Cấu hình Port Socket, thông số Database
│       │   └── database/
│       │       ├── schema_postgres.sql              # Script tạo 22 bảng CSDL chuẩn cho PostgreSQL
│       │       ├── seed_data_postgres.sql           # Dữ liệu mẫu khởi tạo cho PostgreSQL
│       │       ├── schema.sql                       # Script tạo bảng tương thích MySQL / H2
│       │       └── seed_data.sql                    # Dữ liệu mẫu tương thích MySQL / H2
│       └── java/com/quizzlive/server/
│           ├── ServerApp.java                       # Điểm khởi chạy: Khởi tạo DB -> Mở Socket -> Chạy GUI
│           ├── config/
│           │   └── ServerConfig.java                # Đọc cấu hình từ application.properties
│           ├── database/
│           │   └── DatabaseManager.java             # Quản lý Connection Pool HikariCP & nạp SQL tự động
│           ├── entity/                              # Ánh xạ 1:1 với 22 bảng CSDL
│           │   ├── User.java, UserProfile.java      # NGUOI_DUNG, HO_SO_NGUOI_DUNG
│           │   ├── Topic.java, Question.java        # CHU_DE, CAU_HOI
│           │   ├── Answer.java, MatchConfig.java    # DAP_AN, CAU_HINH_TRAN
│           │   └── Match.java, Tournament.java      # TRAN_DAU, GIAI_DAU
│           ├── dao/                                 # Tầng truy xuất CSDL (JDBC / PreparedStatement)
│           │   ├── UserDAO.java                     # Đăng ký, Đăng nhập, Khóa tài khoản, Cập nhật thành tích
│           │   ├── OtpDAO.java                      # Lưu và kiểm tra mã OTP (hạn 10 phút)
│           │   ├── QuestionDAO.java                 # Lấy câu hỏi ngẫu nhiên theo chủ đề/độ khó, câu hỏi IQ
│           │   ├── MatchConfigDAO.java              # Đọc và cập nhật tham số trận đấu
│           │   └── StatisticDAO.java                # Thống kê online, top người chơi thắng nhiều nhất
│           ├── service/                             # Tầng nghiệp vụ (Dev triển khai code các yêu cầu tại đây)
│           │   ├── AuthService.java                 # // TODO: Xử lý đăng ký, mã OTP, đăng nhập
│           │   ├── MatchmakingService.java          # // TODO: Xử lý hàng đợi ghép cặp 60 giây
│           │   ├── ChallengeService.java            # // TODO: Xử lý lời mời thách đấu trực tiếp
│           │   ├── BattleRoom.java                  # // TODO: Điều phối trận đấu 1v1, đồng bộ 10s/câu
│           │   ├── BattleManager.java               # // TODO: Quản lý các phòng đấu đang hoạt động
│           │   ├── IQTestService.java               # // TODO: Cung cấp bài test IQ hình ảnh lúc chờ
│           │   └── TournamentService.java           # // TODO: Bốc thăm chia bảng giải đấu
│           ├── handler/
│           │   └── PacketRouter.java                # Nhận Packet từ Socket -> Điều hướng tới Service
│           ├── network/
│           │   ├── SocketServer.java                # Lắng nghe TCP Sockets đa luồng (Thread Pool)
│           │   ├── ClientSession.java               # Luồng đọc/ghi riêng biệt cho từng Client
│           │   ├── SessionManager.java              # Quản lý danh sách Client online & Broadcast
│           │   └── SSLContextFactory.java          # Hỗ trợ mã hóa SSL/TLS khi cần
│           └── gui/
│               └── ServerMainFrame.java             # GIAO DIỆN QUẢN TRỊ SERVER (Bắt buộc theo đề bài)
│
└── quizzlive-client/                                # MODULE 3: PHÍA CLIENT (NGƯỜI CHƠI)
    ├── pom.xml
    └── src/main/
        ├── resources/
        │   └── client.properties                    # Cấu hình IP Host Server và Port
        └── java/com/quizzlive/client/
            ├── ClientApp.java                       # Main entry point Client
            ├── config/ClientConfig.java             # Đọc cấu hình Client
            └── network/SocketClient.java            # Mở kết nối Socket TCP & luồng nền lắng nghe Server
```

---

## 🗄️ Thiết Kế Cơ Sở Dữ Liệu (22 Bảng Chuẩn ERD)

Toàn bộ CSDL được xây dựng bám sát 100% theo sơ đồ quan hệ thực thể (ERD) được duyệt:
1. **Phân hệ Tài khoản & Thống kê**:
   - `NGUOI_DUNG`: Tài khoản, mật khẩu, email, trạng thái (`ACTIVE`, `BLOCKED`, `PENDING`), online.
   - `HO_SO_NGUOI_DUNG`: Họ tên, avatar, giới thiệu, điểm IQ, tổng trận, thắng, thua, chuỗi thắng hiện tại/dài nhất, tổng điểm.
   - `OTP`: Mã xác thực 6 số, mục đích (`REGISTER`, `RESET_PASSWORD`), thời gian tạo và hết hạn (10 phút).
   - `THONG_KE_NGUOI_CHOI`: Tổng số trận, số trận thắng/thua/hòa, tổng số câu đúng, điểm cao nhất.
   - `QUAN_TRI_VIEN`: Tài khoản quản trị viên đăng nhập vào Server GUI.
2. **Phân hệ Ngân hàng Câu hỏi & Cấu hình**:
   - `CHU_DE`: Danh mục chủ đề (Khoa học, Lịch sử, Công nghệ, Tư duy IQ...).
   - `CAU_HOI`: Ngân hàng câu hỏi, độ khó (`EASY`, `MEDIUM`, `HARD`), loại (`STANDARD`, `IQ_IMAGE`), hình ảnh đính kèm.
   - `DAP_AN`: 4 lựa chọn A, B, C, D cho từng câu hỏi, đánh dấu đáp án đúng.
   - `CAU_HINH_TRAN`: Số câu (5), thời gian mỗi câu (10s), điểm cơ bản (100), thưởng tốc độ (`TRUE`).
3. **Phân hệ Ghép Cặp & Trận Đấu Realtime**:
   - `HANG_DOI_GHEP_CAP`: Hàng đợi tìm trận, thời gian vào, hạn 60s, chủ đề, độ khó.
   - `TRAN_DAU`: Thông tin trận đấu, mã code, trạng thái (`PENDING`, `PLAYING`, `FINISHED`), người thắng.
   - `LOI_MOI_THACH_DAU`: Lời mời thách đấu bạn bè trực tiếp, thời gian gửi, trạng thái chấp nhận/từ chối.
   - `NGUOI_CHOI_TRAN`: 2 người chơi trong trận, vị trí (Player 1 / Player 2), điểm số, xếp hạng.
   - `CAU_HOI_TRAN`: 5 câu hỏi được chọn ngẫu nhiên cho trận đấu.
   - `THU_TU_DAP_AN_TRAN`: Thứ tự 4 đáp án sau khi xáo trộn cho từng câu.
   - `TRA_LOI`: Lịch sử câu trả lời của từng người, thời gian phản hồi (ms), đúng/sai, số điểm nhận được.
4. **Phân hệ Giải Đấu (Tournament)**:
   - `GIAI_DAU`: Giải đấu nhiều người, mô tả, số người tối đa, thời gian bắt đầu/kết thúc.
   - `VONG_DAU`: Vòng bảng, tứ kết, bán kết, chung kết.
   - `BANG_DAU`: Bảng A, B, C, D...
   - `TRAN_DAU_GIAI`: Liên kết các trận đấu thuộc giải đấu.
   - `NGUOI_THAM_GIA_GIAI`: Danh sách thí sinh tham gia giải đấu và hạt giống.
   - `XEP_HANG_GIAI`: Điểm số, hiệu số, vị trí xếp hạng trong giải.

---

## ⚙️ Hướng Dẫn Cấu Hình Cơ Sở Dữ Liệu

Hệ thống hỗ trợ cả **PostgreSQL** và **MySQL** linh hoạt trong file [`quizzlive-server/src/main/resources/application.properties`](quizzlive-server/src/main/resources/application.properties):

### 1. Nếu sử dụng PostgreSQL (Khuyên dùng)
```properties
db.driver=org.postgresql.Driver
db.url=jdbc:postgresql://localhost:5432/quizzlive_db
db.username=postgres
db.password=ĐIỀN_MẬT_KHẨU_POSTGRES_CỦA_BẠN

db.auto-init=true
```
*Lưu ý: Nếu chưa tạo Database, hãy vào pgAdmin tạo một database mới tên là `quizzlive_db`. Khi Server khởi động, hệ thống sẽ **tự động nạp toàn bộ 22 bảng và dữ liệu mẫu** từ file `schema_postgres.sql`.*

### 2. Nếu sử dụng MySQL (XAMPP / Laragon / Docker)
Chỉ cần bỏ comment phần MySQL trong `application.properties`:
```properties
db.driver=com.mysql.cj.jdbc.Driver
db.url=jdbc:mysql://localhost:3306/quizzlive_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&characterEncoding=UTF-8
db.username=root
db.password=
```

---

## 🚀 Hướng Dẫn Biên Dịch & Khởi Chạy

### 1. Yêu cầu môi trường
- **JDK 17** trở lên (Đã kiểm thử tương thích hoàn hảo trên Java 17, Java 21 và Java 23).
- **Apache Maven 3.8+**.

### 2. Biên dịch toàn bộ dự án
Mở terminal tại thư mục gốc của dự án (`Quizzlive/`) và chạy:
```powershell
mvn clean compile
```

### 3. Khởi chạy Server và Giao diện Quản trị GUI
Chạy lệnh rút gọn đã được cấu hình sẵn:
```powershell
mvn -pl quizzlive-server exec:java
```
*(Nếu muốn truyền tham số trên PowerShell: `mvn -pl quizzlive-server exec:java "-Dexec.mainClass=com.quizzlive.server.ServerApp"`)*

Ngay khi khởi động:
1. `DatabaseManager` kết nối CSDL và tự động kiểm tra/nạp bảng nếu chưa có.
2. `SocketServer` mở cổng lắng nghe `8888`.
3. Cửa sổ **Quizzlive Server Admin GUI** lập tức hiển thị với 4 tab trực quan:
   - **Tab 1: Trạng thái & Người dùng**: Xem số người online, danh sách tài khoản, nút Khóa/Mở khóa tài khoản.
   - **Tab 2: Ngân hàng câu hỏi**: Lọc theo chủ đề/độ khó, thêm và xóa câu hỏi trắc nghiệm.
   - **Tab 3: Cấu hình trận đấu**: Cấu hình số câu (5), thời gian (10s), điểm số, lưu cấu hình vào DB.
   - **Tab 4: Thống kê & Báo cáo**: Top thắng nhiều nhất, Top chuỗi thắng dài nhất, Người tham gia nhiều nhất.

---

## 👨‍💻 Hướng Dẫn Dành Cho Thành Viên Nhóm Phát Triển (Dev Guide)

Dự án đã dựng sẵn khung sườn (Skeleton) và cấu trúc phân tầng rõ ràng. Các bạn dev chỉ cần mở các file sau để hoàn thiện logic nghiệp vụ:

1. **`AuthService.java`**: Viết logic lưu tài khoản, sinh mã OTP 6 số lưu vào bảng `OTP`, kiểm tra mật khẩu khi đăng nhập.
2. **`MatchmakingService.java`**: Viết logic ghép cặp 2 người chơi cùng chủ đề trong 60 giây; quá 60s phát gói `QUEUE_TIMEOUT_NOTIFY`.
3. **`ChallengeService.java`**: Viết logic gửi lời mời thách đấu 1v1 cho người chơi đang online.
4. **`BattleRoom.java`**: Viết logic đếm ngược 10 giây mỗi câu, chấm điểm theo thời gian còn lại, chuyển câu khi cả hai đã trả lời hoặc hết giờ, lưu kết quả chung cuộc vào CSDL.
5. **`quizzlive-client`**: Xây dựng giao diện Client GUI (Java Swing / JavaFX) cho người chơi kết nối Socket tới Server theo các DTO đã định nghĩa sẵn trong `quizzlive-shared`.
