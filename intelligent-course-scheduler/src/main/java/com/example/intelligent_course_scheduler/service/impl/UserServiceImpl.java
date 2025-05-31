package com.example.intelligent_course_scheduler.service.impl;

import com.example.intelligent_course_scheduler.entity.Role;
import com.example.intelligent_course_scheduler.entity.User;
import com.example.intelligent_course_scheduler.payload.UserCreateRequest;
import com.example.intelligent_course_scheduler.payload.UserUpdateRequest;
import com.example.intelligent_course_scheduler.repository.RoleRepository;
import com.example.intelligent_course_scheduler.repository.UserRepository;
import com.example.intelligent_course_scheduler.service.UserService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public User createUser(UserCreateRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("错误：用户名 '" + request.getUsername() + "' 已被使用！");
        }
        if (StringUtils.hasText(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("错误：邮箱 '" + request.getEmail() + "' 已被使用！");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setEnabled(request.getEnabled() != null ? request.getEnabled() : true); // 默认为true

        Set<Role> roles = new HashSet<>();
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            for (String roleName : request.getRoles()) {
                Role role = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new RuntimeException("错误：找不到角色: " + roleName));
                roles.add(role);
            }
        } else {
            // (可选) 如果没有指定角色，可以分配一个默认角色，例如 ROLE_STUDENT
            // Role defaultRole = roleRepository.findByName("ROLE_STUDENT")
            // .orElseThrow(() -> new RuntimeException("Error: Default role ROLE_STUDENT not found."));
            // roles.add(defaultRole);
        }
        user.setRoles(roles);
        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> getAllUsers(String usernameQuery, Boolean enabledQuery, Pageable pageable) {
        Specification<User> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(usernameQuery)) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("username")), "%" + usernameQuery.toLowerCase() + "%"));
            }
            if (enabledQuery != null) {
                predicates.add(criteriaBuilder.equal(root.get("enabled"), enabledQuery));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        return userRepository.findAll(spec, pageable);
    }

    @Override
    @Transactional
    public User updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("错误：找不到ID为 " + id + " 的用户"));

        if (StringUtils.hasText(request.getEmail()) && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                // 需要确保这个邮箱不是属于其他用户的
                Optional<User> userWithSameEmail = userRepository.findByEmail(request.getEmail());
                if(userWithSameEmail.isPresent() && !userWithSameEmail.get().getId().equals(id)){
                    throw new IllegalArgumentException("错误：邮箱 '" + request.getEmail() + "' 已被其他用户使用！");
                }
            }
            user.setEmail(request.getEmail());
        }

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getEnabled() != null) {
            user.setEnabled(request.getEnabled());
        }
        if (request.getLocked() != null) {
            user.setLocked(request.getLocked());
        }

        if (request.getRoles() != null) { // 允许清空角色
            Set<Role> roles = new HashSet<>();
            if (!request.getRoles().isEmpty()) {
                for (String roleName : request.getRoles()) {
                    Role role = roleRepository.findByName(roleName)
                            .orElseThrow(() -> new RuntimeException("错误：找不到角色: " + roleName));
                    roles.add(role);
                }
            }
            user.setRoles(roles);
        }
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("错误：找不到ID为 " + id + " 的用户，无法删除");
        }
        // (可选) 防止删除当前登录用户或最后一个管理员
        userRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }
}