package com.example.intelligent_course_scheduler.config;

import com.example.intelligent_course_scheduler.security.jwt.JwtAuthenticationEntryPoint; // 将会创建
import com.example.intelligent_course_scheduler.security.jwt.JwtAuthenticationFilter;
import com.example.intelligent_course_scheduler.service.impl.UserDetailsServiceImpl; // 确保路径正确
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
// import org.springframework.security.config.Customizer; // 如果之前添加了 .httpBasic(Customizer.withDefaults())

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class SecurityConfig {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    // (可选) 自定义认证入口点，用于处理未认证访问受保护资源时的响应
    // 如果不定义，Spring Security 会有默认处理 (例如重定向到登录页或返回默认的401/403页面)
    // 对于REST API，我们通常希望返回JSON格式的错误信息。
    @Autowired
    private JwtAuthenticationEntryPoint unauthorizedHandler; // 我们接下来会创建这个类

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // 禁用CSRF
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(unauthorizedHandler) // 设置自定义的认证入口点
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 无状态Session
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/auth/**").permitAll() // 登录、注册等认证相关接口
                        .requestMatchers("/public/**", "/error/**").permitAll() // 公共资源或错误页面
                        // Swagger (如果后续使用，根据实际路径配置)
                        // .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .anyRequest().authenticated() // 其他所有请求都需要认证
                )
                .userDetailsService(userDetailsService); // 设置 UserDetailsService

        // 在 UsernamePasswordAuthenticationFilter 之前添加 JWT 过滤器
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // 如果之前为了调试添加了 .httpBasic()，现在有了JWT过滤器，可以考虑是否移除或保留它作为备用/调试方式
        // .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}