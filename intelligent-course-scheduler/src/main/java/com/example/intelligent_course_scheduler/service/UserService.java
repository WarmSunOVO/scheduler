package com.example.intelligent_course_scheduler.service;

import com.example.intelligent_course_scheduler.entity.Role;
import com.example.intelligent_course_scheduler.entity.User;
import com.example.intelligent_course_scheduler.payload.UserCreateRequest;
import com.example.intelligent_course_scheduler.payload.UserUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface UserService {
    User createUser(UserCreateRequest userCreateRequest);
    Optional<User> getUserById(Long id);
    Optional<User> getUserByUsername(String username);
    Page<User> getAllUsers(String usernameQuery, Boolean enabledQuery, Pageable pageable);
    User updateUser(Long id, UserUpdateRequest userUpdateRequest);
    void deleteUser(Long id);
    List<Role> getAllRoles(); // 获取所有角色
    // (可选) 修改用户密码等其他操作
}