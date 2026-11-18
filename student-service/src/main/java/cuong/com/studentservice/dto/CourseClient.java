package cuong.com.studentservice.dto;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(name = "COURSE-SERVICE")
public interface CourseClient {
    @GetMapping("/api/courses/{courseId}")
    CourseDTO getCourseById(@PathVariable("courseId") Long courseId);

    @PutMapping("/api/courses/{courseId}/decrease")
    void decreaseCapacity(@PathVariable("courseId") Long courseId);
}
