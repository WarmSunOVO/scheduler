package com.example.intelligent_course_scheduler.exception; // 替换为你的包名

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice // 声明这是一个控制器增强器，用于全局处理异常
public class GlobalExceptionHandler {

    // 处理自定义的 ResourceNotFoundException (如果定义了的话)
    // @ExceptionHandler(ResourceNotFoundException.class)
    // public ResponseEntity<Object> handleResourceNotFoundException(
    //         ResourceNotFoundException ex, WebRequest request) {
    //
    //     Map<String, Object> body = new LinkedHashMap<>();
    //     body.put("timestamp", LocalDateTime.now());
    //     body.put("message", ex.getMessage());
    //     body.put("path", request.getDescription(false).replace("uri=", ""));
    //
    //     return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    // }

    // 处理 IllegalArgumentException (常用于参数校验失败或业务规则冲突)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");
        body.put("message", ex.getMessage()); // 使用异常的原始消息
        body.put("path", request.getDescription(false).replace("uri=", ""));


        // 根据异常消息特定内容判断是否应该是 NOT_FOUND
        if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("not found") ||
                ex.getMessage() != null && ex.getMessage().toLowerCase().contains("未找到")) {
            body.put("status", HttpStatus.NOT_FOUND.value());
            body.put("error", "Not Found");
            return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    // 处理所有其他未捕获的异常 (作为最后的防线)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllOtherExceptions(
            Exception ex, WebRequest request) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", "Internal Server Error");
        body.put("message", "发生了一个内部错误，请联系管理员。"); // 避免暴露敏感的异常栈信息给客户端
        body.put("path", request.getDescription(false).replace("uri=", ""));

        // 实际项目中，应该在这里记录详细的异常日志 ex.printStackTrace() 或使用日志框架
        ex.printStackTrace(); // 开发时可以打印，生产环境应使用日志框架

        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}