package com.example.intelligent_course_scheduler.entity; // 替换为你的包名

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
//实体类
@Entity
@Table(name = "classroom") // 显式指定表名，如果省略，JPA会默认使用类名小写
@Data // Lombok 注解，自动生成 Getter, Setter, toString, equals, hashCode 等方法
public class Classroom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 主键自增
    private Long id;

    @Column(name = "room_number", nullable = false, unique = true, length = 50)
    private String roomNumber;

    @Column(nullable = false)
    private Integer capacity; // 对应 INT 类型

    @Column(length = 50)
    private String type; // 教室类型

    @Column(name = "building_name", length = 100)
    private String buildingName;

    @Column(name = "floor_level")
    private Integer floorLevel;

    @Column(length = 20, columnDefinition = "VARCHAR(20) DEFAULT '可用'")
    private String status = "可用"; // 默认为 "可用"

    @Lob // 对于 TEXT 类型，可以使用 @Lob
    @Column(columnDefinition = "TEXT")
    private String remarks;

    @CreationTimestamp // Hibernate 注解，在实体创建时自动设置时间戳
    @Column(name = "created_at", updatable = false) // updatable = false 表示创建后不更新
    private LocalDateTime createdAt;

    @UpdateTimestamp // Hibernate 注解，在实体更新时自动设置时间戳
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 构造函数 (Lombok @Data 会生成无参构造，如果需要有参构造可以自行添加)
    // 例如：
    // public Classroom() {}
    //
    // public Classroom(String roomNumber, Integer capacity, String type) {
    //     this.roomNumber = roomNumber;
    //     this.capacity = capacity;
    //     this.type = type;
    // }

    // Lombok 的 @Data 已经包含了 Getter 和 Setter
    // 如果不使用 Lombok，你需要手动添加所有字段的 getter 和 setter 方法
}