package com.example.intelligent_course_scheduler.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "app_user") // 映射到 app_user 表
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails { // 实现 UserDetails 接口

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false) // 长度由数据库 VARCHAR(255) 定义
    private String password;

    @Column(unique = true, length = 100)
    private String email;

    @Column(name = "full_name", length = 100)
    private String fullName;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(nullable = false)
    private boolean locked = false; // accountNonLocked

    @Column(name = "credentials_expired", nullable = false)
    private boolean credentialsExpired = false; // credentialsNonExpired

    @Column(name = "account_expired", nullable = false)
    private boolean accountExpired = false; // accountNonExpired


    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 多对多关系：一个用户可以有多个角色
    @ManyToMany(fetch = FetchType.EAGER) // EAGER 加载用户时即加载角色信息
    @JoinTable(
            name = "user_role", // 连接表的名字
            joinColumns = @JoinColumn(name = "user_id"), // 当前实体 (User) 在连接表中的外键列
            inverseJoinColumns = @JoinColumn(name = "role_id") // 对方实体 (Role) 在连接表中的外键列
    )
    private Set<Role> roles = new HashSet<>();

    // --- UserDetails 接口实现 ---
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 将角色集合转换为 GrantedAuthority 集合
        return this.roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toSet());
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return !this.accountExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !this.locked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return !this.credentialsExpired;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }
}