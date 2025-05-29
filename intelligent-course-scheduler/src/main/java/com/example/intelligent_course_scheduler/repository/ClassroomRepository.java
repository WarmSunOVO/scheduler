package com.example.intelligent_course_scheduler.repository; // 替换为你的包名

import com.example.intelligent_course_scheduler.entity.Classroom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor; // 用于动态条件查询
import org.springframework.stereotype.Repository;

import java.util.Optional;

//创建数据访问层
@Repository // 可选注解，Spring Boot 启动时会自动扫描 JpaRepository 的子接口
public interface ClassroomRepository extends JpaRepository<Classroom, Long>, JpaSpecificationExecutor<Classroom> {

    // Spring Data JPA 会根据方法名自动生成查询实现
    // 例如，根据教室编号查询教室
    Optional<Classroom> findByRoomNumber(String roomNumber);

    // JpaRepository 已经提供了以下常用方法:
    // - save(S entity) / saveAll(Iterable<S> entities): 保存或更新实体
    // - findById(ID id): 根据ID查询实体
    // - existsById(ID id): 判断实体是否存在
    // - findAll(): 查询所有实体
    // - findAll(Sort sort): 查询所有实体并排序
    // - findAll(Pageable pageable): 分页查询所有实体
    // - count(): 统计实体数量
    // - deleteById(ID id): 根据ID删除实体
    // - delete(S entity): 删除实体
    // ...等等

    // JpaSpecificationExecutor 接口提供了使用 Specification 进行复杂动态查询的能力
    // 例如：findAll(Specification<T> spec, Pageable pageable)
}