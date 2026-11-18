package cuong.com.studentservice.controller;

import cuong.com.studentservice.dto.CourseClient;
import cuong.com.studentservice.dto.CourseDTO;
import cuong.com.studentservice.dto.EnrollRequest;
import cuong.com.studentservice.entity.Student;
import cuong.com.studentservice.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {
    private final StudentRepository studentRepository;
    private final CourseClient courseClient;

    @PostMapping("/enroll")
    public ResponseEntity<?> erollCourse(@RequestBody EnrollRequest request){
        Student student = studentRepository.findById(request.getStudentId()).orElse(null);
        if (student==null){
            return ResponseEntity.badRequest().body("Không tìm thấy sinh viên");
        }

        CourseDTO course;
        try {
            course = courseClient.getCourseById(request.getCourseId());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi gọi Course Service: " + e.getMessage());
        }

        if (course==null || course.getCapacity()<=0){
            return ResponseEntity.badRequest().body("Khoá học đã hết chỗ (capcacity <=0)!");

        }

        if (student.getEnrolledCourses()==null){
            student.setEnrolledCourses(new ArrayList<>());

        }
        if (student.getEnrolledCourses().contains(request.getCourseId())){
            return ResponseEntity.badRequest().body("Sinh viên đã đăng ký khóa học này rồi!");
        }

        courseClient.decreaseCapacity(request.getCourseId());
        student.getEnrolledCourses().add(request.getCourseId());
        studentRepository.save(student);
        return  ResponseEntity.ok(student);
    }

    @PostMapping
    public  Student createStudent (@RequestBody Student student){
        return studentRepository.save(student);
    }
}
