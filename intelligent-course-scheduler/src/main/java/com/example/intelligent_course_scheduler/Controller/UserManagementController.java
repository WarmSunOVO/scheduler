package com.example.intelligent_course_scheduler.Controller;

import com.example.intelligent_course_scheduler.entity.Role;
import com.example.intelligent_course_scheduler.entity.User;
import com.example.intelligent_course_scheduler.payload.RoleResponse;
import com.example.intelligent_course_scheduler.payload.UserCreateRequest;
import com.example.intelligent_course_scheduler.payload.UserResponse;
import com.example.intelligent_course_scheduler.payload.UserUpdateRequest;
import com.example.intelligent_course_scheduler.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserManagementController {

    private static final Logger logger = LoggerFactory.getLogger(UserManagementController.class);

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserCreateRequest userCreateRequest) {
        logger.info("Request to create user with username: {}", userCreateRequest.getUsername());
        User newUser = userService.createUser(userCreateRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.fromUser(newUser));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        logger.info("Request to get user by ID: {}", id);
        return userService.getUserById(id)
                .map(user -> {
                    logger.info("Found user with ID: {}", id);
                    return ResponseEntity.ok(UserResponse.fromUser(user));
                })
                .orElseGet(() -> {
                    logger.warn("User with ID: {} not found.", id);
                    return ResponseEntity.notFound().build();
                });
    }

    @GetMapping
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(name = "sort", required = false) String sortParam) { // 接收单个 sort 字符串

        logger.info("Request to get all users. Page: {}, Size: {}, SortParam: '{}', UsernameQuery: {}, EnabledQuery: {}",
                page, size, sortParam, username, enabled);

        Pageable pageable;
        List<Sort.Order> orders = new ArrayList<>();

        if (StringUtils.hasText(sortParam)) {
            String[] parts = sortParam.split(",", 2);
            String property = parts[0].trim();
            if (StringUtils.hasText(property)) {
                Sort.Direction direction = Sort.Direction.ASC;
                if (parts.length > 1 && StringUtils.hasText(parts[1].trim())) {
                    try {
                        direction = Sort.Direction.fromString(parts[1].trim().toUpperCase());
                    } catch (IllegalArgumentException e) {
                        logger.warn("Invalid sort direction provided: '{}' for property '{}'. Defaulting to ASC.", parts[1], property);
                    }
                }
                orders.add(new Sort.Order(direction, property));
            } else {
                logger.warn("Invalid sort property in parameter: '{}'.", sortParam);
            }
        }

        if (!orders.isEmpty()) {
            logger.info("Applying user-defined sort orders: {}", orders);
            pageable = PageRequest.of(page, size, Sort.by(orders));
        } else {
            logger.info("No valid sort parameter or empty. Applying default sort: id, DESC");
            pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        }

        Page<User> userPage = userService.getAllUsers(username, enabled, pageable);
        return ResponseEntity.ok(userPage.map(UserResponse::fromUser));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest userUpdateRequest) {
        logger.info("Request to update user with ID: {}", id);
        User updatedUser = userService.updateUser(id, userUpdateRequest);
        return ResponseEntity.ok(UserResponse.fromUser(updatedUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        logger.info("Request to delete user with ID: {}", id);
        userService.deleteUser(id);
        return ResponseEntity.ok("用户 ID: " + id + " 已成功删除。");
    }

    @GetMapping("/roles")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<RoleResponse>> getAllRoles() {
        logger.info("Request to get all roles.");
        List<Role> roles = userService.getAllRoles();
        List<RoleResponse> roleResponses = roles.stream().map(RoleResponse::fromRole).collect(Collectors.toList());
        return ResponseEntity.ok(roleResponses);
    }
}