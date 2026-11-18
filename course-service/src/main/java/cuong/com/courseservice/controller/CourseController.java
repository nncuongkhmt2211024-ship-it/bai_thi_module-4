package cuong.com.courseservice.controller;

import cuong.com.courseservice.entity.Course;
import cuong.com.courseservice.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseRepository courseRepository;


    @GetMapping("/{courseId}")
    public ResponseEntity<Course> getCourseById(@PathVariable("courseId") Long courseId) {
        return courseRepository.findById(courseId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    @PutMapping("/{courseId}/decrease")
    public ResponseEntity<?> decreaseCapacity(@PathVariable("courseId") Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khóa học"));
        if (course.getCapacity() <= 0) {
            return ResponseEntity.badRequest().body("Khóa học đã hết chỗ");
        }
        course.setCapacity(course.getCapacity() - 1);
        return ResponseEntity.ok(courseRepository.save(course));
    }


    @PostMapping
    public Course createCourse(@RequestBody Course course) {
        return courseRepository.save(course);
    }
}