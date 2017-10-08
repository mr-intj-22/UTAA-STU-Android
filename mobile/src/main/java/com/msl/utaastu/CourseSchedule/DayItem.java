package com.msl.utaastu.CourseSchedule;

import android.support.annotation.Keep;

/**
 * Created by Malek Shefat on 6/16/2017.
 */

@Keep
public class DayItem {
    private String day;
    private int lectures;

    public int getLectures() {
        return lectures;
    }

    public String getDay() {
        return day;
    }

    public DayItem setDay(String day) {
        this.day = day;
        return this;
    }

    public DayItem setLectures(int lectures) {
        this.lectures = lectures;
        return this;
    }

    public void increaseLectures(){
        lectures++;
    }

    public void decreaseLectures(){
        lectures--;
    }
}
