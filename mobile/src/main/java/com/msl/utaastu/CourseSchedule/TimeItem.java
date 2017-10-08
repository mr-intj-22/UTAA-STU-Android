package com.msl.utaastu.CourseSchedule;

import android.support.annotation.Keep;

/**
 * Created by Malek Shefat on 7/7/2017.
 */

@Keep
public class TimeItem {

    private String day, time;

    public TimeItem setDay(String day) {
        day = day.toLowerCase();
        day = day.substring(0, 1).toUpperCase() + day.substring(1);
        this.day = day;
        return this;
    }

    public TimeItem setTime(String time) {
        this.time = time;
        return this;
    }

    public String getTime() {
        return time;
    }

    public String getDay() {
        return day;
    }
}
