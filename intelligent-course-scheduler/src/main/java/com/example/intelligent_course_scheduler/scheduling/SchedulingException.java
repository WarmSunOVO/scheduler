package com.example.intelligent_course_scheduler.scheduling;

public class SchedulingException extends RuntimeException {
    public SchedulingException(String message) {
        super(message);
    }
    public SchedulingException(String message, Throwable cause) {
        super(message, cause);
    }
}