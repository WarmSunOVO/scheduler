package com.example.intelligent_course_scheduler; // 替换为你的包名

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/api/hello")
    public String sayHello() {
        return "Hello from Spring Boot Backend!";
    }
}