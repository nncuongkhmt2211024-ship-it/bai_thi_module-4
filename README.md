Câu 1:
Cơ chế Service Discovery: Các microservice (như student-service, course-service) khi khởi động sẽ tự động đăng ký thông tin mạng của mình (tên service, địa chỉ IP, Port, trạng thái sức khỏe) lên một máy chủ danh bạ trung tâm là Eureka Server (Service Registry) và duy trì kết nối qua cơ chế Heartbeat. Khi API Gateway nhận request từ người dùng, nó chỉ cần định tuyến thông qua tên logic của service (ví dụ: lb://STUDENT-SERVICE), đồng thời tra cứu danh sách địa chỉ thực tế đang hoạt động từ Eureka.

Lý do Gateway không nên gọi trực tiếp IP/Port (Hardcode):

Tính động của hạ tầng (Dynamic IP/Port): Trong môi trường Cloud, Docker hoặc Kubernetes, các container/instance thường xuyên được khởi động lại, di chuyển hoặc cấp phát lại IP/Port mới. Nếu cấu hình cứng (hardcode) IP/Port tại Gateway, hệ thống sẽ bị lỗi kết nối ngay khi địa chỉ thay đổi.

Khó mở rộng và cân bằng tải: Khi chạy nhiều bản sao (instance) của cùng một service trên nhiều port khác nhau, việc gọi cứng một IP/Port duy nhất khiến Gateway không thể tự động chia tải (Load Balancing) đều cho các instance.

Rủi ro điểm lỗi đơn (Single Point of Failure): Nếu một instance bị sập, Gateway gọi trực tiếp IP cũ sẽ trả về lỗi cho người dùng thay vì tự động loại bỏ instance lỗi và chuyển hướng sang instance dự phòng.

Câu 2:
Để mở rộng theo chiều ngang (Horizontal Scaling) cho Student Service mà không cần sửa bất kỳ dòng cấu hình nào ở API Gateway, ta thực hiện theo các bước sau:

Chạy thêm nhiều instance của student-service: Khởi chạy thêm các bản sao (instance) của student-service trên các cổng (port) khác nhau (ví dụ cấu hình server.port=0 để Spring Boot tự động cấp phát cổng ngẫu nhiên không trùng lặp, hoặc chạy nhiều container trên Docker).

Tự động đăng ký cùng một spring.application.name: Tất cả các instance này đều giữ nguyên cấu hình spring.application.name=student-service và trỏ về cùng một Eureka Server. Eureka sẽ gom nhóm toàn bộ các instance này dưới chung một định danh STUDENT-SERVICE.

Cân bằng tải tự động qua Gateway: Trên API Gateway, cấu hình định tuyến đã sử dụng giao thức uri: lb://STUDENT-SERVICE kết hợp với Spring Cloud LoadBalancer. Gateway sẽ tự động cập nhật danh sách các instance mới từ Eureka và phân phối đều lượng request tăng đột biến (theo thuật toán Round-Robin) tới tất cả các instance đang chạy mà không cần khởi động lại hay sửa cấu hình của Gateway.

Câu 3:
1. Giao tiếp đồng bộ qua OpenFeign (REST HTTP):

Cơ chế: Khi nhận yêu cầu đăng ký khóa học, Student Service gửi HTTP request trực tiếp sang Course Service để kiểm tra chỗ trống (capacity > 0) và trừ số lượng chỗ, sau đó chờ phản hồi rồi mới lưu kết quả.

Ưu điểm:

Đơn giản, dễ triển khai và debug: Code ngắn gọn như gọi hàm nội bộ, không cần cài đặt hay vận hành thêm hạ tầng Message Broker.

Phản hồi tức thời: Sinh viên nhận được kết quả ngay lập tức trong một request duy nhất (biết ngay đăng ký thành công hay khóa học đã hết chỗ).

Nhược điểm:

Phụ thuộc chặt (Tight Coupling): Cả hai service phải cùng hoạt động tại thời điểm gọi. Nếu Course Service bị lỗi, bảo trì hoặc phản hồi chậm, Student Service cũng bị treo hoặc báo lỗi theo.

Khả năng chịu tải hạn chế: Khi lượng sinh viên truy cập đăng ký tín chỉ tăng đột biến cùng lúc, các luồng xử lý phải chờ nhau dễ gây nghẽn cổ chai.

2. Giao tiếp bất đồng bộ qua Apache Kafka (Event-Driven):

Cơ chế: Khi sinh viên đăng ký, Student Service chỉ cần đẩy một sự kiện (Event) vào Kafka Topic rồi phản hồi trạng thái tiếp nhận ngay; Course Service sẽ tự động lắng nghe (Consume) topic đó để kiểm tra và cập nhật số lượng chỗ trống độc lập.

Ưu điểm:

Giảm sự phụ thuộc (Loose Coupling): Hai service hoạt động độc lập. Nếu Course Service tạm thời quá tải hoặc ngừng hoạt động, sự kiện vẫn được lưu an toàn trong hàng đợi Kafka để xử lý sau mà không làm sập luồng đăng ký.

Chịu tải cực cao: Kafka đóng vai trò bộ đệm (buffer) giúp hấp thụ lượng lớn request cùng lúc mà không làm quá tải database của Course Service.

Nhược điểm:

Phức tạp về hạ tầng và lập trình: Phải cài đặt, cấu hình và duy trì thêm Kafka/Zookeeper; đồng thời phải xử lý các vấn đề như trùng lặp tin nhắn hoặc cơ chế hoàn tác dữ liệu (Saga pattern) nếu khóa học hết chỗ sau khi đã ghi nhận yêu cầu.

Tính nhất quán sau cùng (gEventual Consistency): Người dùng không biết ngay lập tức kết quả đăng ký thành công hay thất bại tại thời điểm bấm nút mà phải chờ hệ thống xử lý ngầm và kiểm tra lại trạng thái sau.