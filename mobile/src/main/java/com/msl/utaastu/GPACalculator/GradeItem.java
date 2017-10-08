package com.msl.utaastu.GPACalculator;

import android.support.annotation.Keep;

/**
 * Created by Malek Shefat on 6/16/2017.
 */

@Keep
public class GradeItem {

    private String name = "", grade = "", credit = "";

    public String getName() {
        return name;
    }

    public GradeItem setName(String name) {
        this.name = name;
        return this;
    }

    public String getCredit() {
        return credit;
    }

    public GradeItem setCredit(String credit) {
        this.credit = credit;
        return this;
    }

    public String getGrade() {
        return grade;
    }

    public GradeItem setGrade(String grade) {
        this.grade = grade;
        return this;
    }

    @Override
    public String toString() {
        return name + ", " + grade + ", " + credit;
    }
}
