package com.example.intelligent_course_scheduler.scheduling;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;


@Data
@NoArgsConstructor
@EqualsAndHashCode
public class TimeSlot {
    private int dayOfWeek;    // 1-7 (周一到周日)
    private int startPeriod;  // 开始节次 (从1开始)
    private int endPeriod;    // 结束节次

    public TimeSlot(int dayOfWeek, int startPeriod, int sessionLength) {
        this.dayOfWeek = dayOfWeek;
        this.startPeriod = startPeriod;
        this.endPeriod = startPeriod + sessionLength - 1;
    }

    public boolean overlaps(TimeSlot other) {
        if (this.dayOfWeek != other.dayOfWeek) {
            return false;
        }
        return this.startPeriod <= other.endPeriod && other.startPeriod <= this.endPeriod;
    }

    @Override
    public String toString() {
        return String.format("周%d[%d-%d]", dayOfWeek, startPeriod, endPeriod);
    }
}
