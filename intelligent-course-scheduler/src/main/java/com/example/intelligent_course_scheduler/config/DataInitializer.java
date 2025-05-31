package com.example.intelligent_course_scheduler.config; // 或者其他合适的包

import com.example.intelligent_course_scheduler.entity.Role;
import com.example.intelligent_course_scheduler.entity.User;
import com.example.intelligent_course_scheduler.repository.RoleRepository;
import com.example.intelligent_course_scheduler.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder; // 稍后会配置这个Bean
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; // 确保 PasswordEncoder Bean 已经定义

    @Override
    public void run(String... args) throws Exception {
        // 初始化角色
        Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElseGet(() ->
                roleRepository.save(Role.builder().name("ROLE_ADMIN").description("Administrator").build())
        );
        Role teacherRole = roleRepository.findByName("ROLE_TEACHER").orElseGet(() ->
                roleRepository.save(Role.builder().name("ROLE_TEACHER").description("Teacher").build())
        );
        Role studentRole = roleRepository.findByName("ROLE_STUDENT").orElseGet(() ->
                roleRepository.save(Role.builder().name("ROLE_STUDENT").description("Student").build())
        );

        // 初始化管理员用户
        if (!userRepository.existsByUsername("admin")) {
            User adminUser = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123")) // 使用PasswordEncoder加密
                    .email("admin@example.com")
                    .fullName("System Administrator")
                    .enabled(true)
                    .roles(Set.of(adminRole)) // 赋予ADMIN角色
                    .build();
            userRepository.save(adminUser);
            System.out.println("Created admin user with default password 'admin123'");
        }

        // (可选) 初始化一个教师用户
        if (!userRepository.existsByUsername("teacher1")) {
            User teacherUser = User.builder()
                    .username("teacher1")
                    .password(passwordEncoder.encode("teacher123"))
                    .email("teacher1@example.com")
                    .fullName("Default Teacher")
                    .enabled(true)
                    .roles(Set.of(teacherRole))
                    .build();
            userRepository.save(teacherUser);
            System.out.println("Created teacher user 'teacher1' with default password 'teacher123'");
        }
    }
}