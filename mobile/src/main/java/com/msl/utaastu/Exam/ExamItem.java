package com.msl.utaastu.Exam;

import android.support.annotation.Keep;

/**
 * Created by Malek Shefat on 6/30/2017.
 */

@Keep
public class ExamItem {

    private String code = "", date = "", time = "", place = "";
    private boolean isElective;

    public boolean isElective() {
        return isElective;
    }

    public ExamItem setElective(boolean elective) {
        isElective = elective;
        return this;
    }

    public String getCode() {
        return code;
    }

    public ExamItem setCode(String code) {
        this.code = code;
        return this;
    }

    public String getDate() {
        return date;
    }

    public ExamItem setDate(String date) {
        this.date = date;
        return this;
    }

    public String getTime() {
        return time;
    }

    public ExamItem setTime(String time) {
        this.time = time;
        return this;
    }

    public String getPlace() {
        return place;
    }

    public ExamItem setPlace(String place) {
        this.place = place;
        return this;
    }
}
