package com.example.intelligent_course_scheduler.Controller;

import com.example.intelligent_course_scheduler.payload.SemesterDto;
import com.example.intelligent_course_scheduler.service.SemesterService;
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

import java.util.ArrayList; // 虽然现在只处理一个sortParam，但保留List<Sort.Order>结构以备未来扩展多重排序
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/semesters")
public class SemesterController {

    private static final Logger logger = LoggerFactory.getLogger(SemesterController.class);

    @Autowired
    private SemesterService semesterService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SemesterDto> createSemester(@Valid @RequestBody SemesterDto semesterDto) {
        logger.info("Request to create semester: {} - {}", semesterDto.getAcademicYear(), semesterDto.getName());
        SemesterDto createdSemester = semesterService.createSemester(semesterDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSemester);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SemesterDto> getSemesterById(@PathVariable Long id) {
        logger.info("Request to get semester by ID: {}", id);
        return semesterService.getSemesterById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    logger.warn("Semester with ID: {} not found.", id);
                    return ResponseEntity.notFound().build();
                });
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<SemesterDto>> getAllSemesters(
//            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
//            @RequestParam(name = "sort", required = false) String sortParam
    ) { // 接收单个 sort 字符串

        logger.info("Request to get all semesters (simplified). Page: {}, Size: {}", page, size);

        Pageable pageable;
        // 使用一个固定的、绝对安全的默认排序
        logger.info("Applying fixed default sort for debugging: academicYear, DESC then name, ASC");
        List<Sort.Order> defaultOrders = new ArrayList<>();
        defaultOrders.add(Sort.Order.desc("academicYear"));
        defaultOrders.add(Sort.Order.asc("name"));
        pageable = PageRequest.of(page, size, Sort.by(defaultOrders));

//        if (StringUtils.hasText(sortParam)) {
//            // sortParam 应该是 "property,direction" 格式 e.g., "academicYear,desc"
//            String[] parts = sortParam.split(",", 2); // 最多分割成2部分
//            String property = parts[0].trim();
//
//            if (StringUtils.hasText(property)) {
//                Sort.Direction direction = Sort.Direction.ASC; // 默认升序
//                if (parts.length > 1 && StringUtils.hasText(parts[1].trim())) {
//                    try {
//                        direction = Sort.Direction.fromString(parts[1].trim().toUpperCase());
//                    } catch (IllegalArgumentException e) {
//                        logger.warn("Invalid sort direction provided: '{}' for property '{}'. Defaulting to ASC.", parts[1], property);
//                    }
//                }
//                orders.add(new Sort.Order(direction, property));
//                logger.info("Applying user-defined sort order for semesters: {}", orders);
//                pageable = PageRequest.of(page, size, Sort.by(orders));
//            } else {
//                // 属性名为空，应用默认排序
//                logger.warn("Invalid sort property in parameter: '{}'. Applying default sort.", sortParam);
//                List<Sort.Order> defaultOrders = new ArrayList<>();
//                defaultOrders.add(Sort.Order.desc("academicYear"));
//                defaultOrders.add(Sort.Order.asc("name"));
//                pageable = PageRequest.of(page, size, Sort.by(defaultOrders));
//            }
//        } else {
//            // 没有提供 sort 参数，应用默认排序
//            logger.info("No sort parameter provided for semesters. Applying default sort: academicYear, DESC then name, ASC");
//            List<Sort.Order> defaultOrders = new ArrayList<>();
//            defaultOrders.add(Sort.Order.desc("academicYear"));
//            defaultOrders.add(Sort.Order.asc("name"));
//            pageable = PageRequest.of(page, size, Sort.by(defaultOrders));
//        }

        return ResponseEntity.ok(semesterService.getAllSemesters(pageable, null));
    }

    @GetMapping("/list")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<SemesterDto>> getAllSemestersList() {
        logger.info("Request to get all semesters list (unpaged).");
        return ResponseEntity.ok(semesterService.getAllSemestersList());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SemesterDto> updateSemester(@PathVariable Long id, @Valid @RequestBody SemesterDto semesterDto) {
        logger.info("Request to update semester with ID: {}", id);
        SemesterDto updatedSemester = semesterService.updateSemester(id, semesterDto);
        return ResponseEntity.ok(updatedSemester);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteSemester(@PathVariable Long id) {
        logger.info("Request to delete semester with ID: {}", id);
        semesterService.deleteSemester(id);
        return ResponseEntity.ok("学期 ID: " + id + " 已成功删除。");
    }
}