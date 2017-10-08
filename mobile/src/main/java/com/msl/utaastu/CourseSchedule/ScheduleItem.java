package com.msl.utaastu.CourseSchedule;

import android.support.annotation.Keep;

/**
 * Created by Malek Shefat on 6/16/2017.
 */

@Keep
public class ScheduleItem {

    private String name = "", place = "", time = "", code = "", section = "";

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    @Override
    public String toString() {
        return name + ", " + code;
    }
}
