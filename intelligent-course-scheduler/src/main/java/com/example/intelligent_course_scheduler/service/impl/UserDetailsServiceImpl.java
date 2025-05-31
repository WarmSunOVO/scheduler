package com.example.intelligent_course_scheduler.service.impl;

import com.example.intelligent_course_scheduler.entity.User;
import com.example.intelligent_course_scheduler.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("userDetailsService") // 给Bean一个明确的名字，方便引用
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional(readOnly = true) // 保持事务只读
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 从数据库中根据用户名查找用户
        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with username: " + username)
                );

        // User 实体类已经实现了 UserDetails 接口，可以直接返回
        // Spring Security 会使用 User 对象中的 getPassword() 和 getAuthorities() 等方法
        return user;
    }
}