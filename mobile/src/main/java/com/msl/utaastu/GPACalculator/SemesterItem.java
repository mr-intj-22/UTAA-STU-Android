package com.msl.utaastu.GPACalculator;

import android.support.annotation.Keep;

/**
 * Created by Malek Shefat on 6/16/2017.
 */

@Keep
public class SemesterItem {
    private String semester;
    private int courses;

    public int getCourses() {
        return courses;
    }

    public String getSemester() {
        return semester;
    }

    public SemesterItem setSemester(String semester) {
        this.semester = semester;
        return this;
    }

    public SemesterItem setCourses(int courses) {
        this.courses = courses;
        return this;
    }

    public void increaseCourses() {
        courses++;
    }

    public void decreaseCourses() {
        courses--;
    }
}
