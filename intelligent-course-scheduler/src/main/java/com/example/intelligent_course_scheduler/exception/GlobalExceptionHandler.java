package com.example.intelligent_course_scheduler.exception; // 你的包名

import org.slf4j.Logger; // <--- 导入 SLF4J Logger
import org.slf4j.LoggerFactory; // <--- 导入 SLF4J LoggerFactory
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.validation.FieldError; // 确保导入 FieldError

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    // 声明并初始化 Logger 实例
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 处理 IllegalArgumentException (常用于参数校验失败或业务规则冲突)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {

        logger.warn("Handling IllegalArgumentException: {}", ex.getMessage()); // 使用 logger

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");
        body.put("message", ex.getMessage());
        body.put("path", request.getDescription(false).replace("uri=", ""));

        if (ex.getMessage() != null && (ex.getMessage().toLowerCase().contains("not found") ||
                ex.getMessage().toLowerCase().contains("未找到"))) {
            body.put("status", HttpStatus.NOT_FOUND.value());
            body.put("error", "Not Found");
            return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    // 处理 @Valid 校验失败的 MethodArgumentNotValidException
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, WebRequest request) {

        logger.warn("Handling MethodArgumentNotValidException for path: {}", request.getDescription(false).replace("uri=", ""));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Validation Error");

        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());
        body.put("messages", errors);
        body.put("path", request.getDescription(false).replace("uri=", ""));

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }


    // 处理所有其他未捕获的异常 (作为最后的防线)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllOtherExceptions(
            Exception ex, WebRequest request) {

        // 使用我们声明的 logger 实例
        logger.error("GlobalExceptionHandler caught a general exception: ", ex); // <--- 这里之前报错

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", "Internal Server Error");
        body.put("message", "发生了一个内部错误，请联系管理员。");
        body.put("path", request.getDescription(false).replace("uri=", ""));

        // ex.printStackTrace(); // 开发时可以保留，但生产环境应依赖日志框架记录详细信息

        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}