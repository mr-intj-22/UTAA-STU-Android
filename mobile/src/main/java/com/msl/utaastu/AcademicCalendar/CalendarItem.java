package com.msl.utaastu.AcademicCalendar;

import android.support.annotation.Keep;

/**
 * Created by Malek Shefat on 7/15/2017.
 */

@Keep
public class CalendarItem {

    private String date, event, title;
    private int type;

    public String getDate() {
        return date;
    }

    public CalendarItem setDate(String date) {
        this.date = date;
        return this;
    }

    public String getEvent() {
        return event;
    }

    public String getTitle() {
        return title;
    }

    public CalendarItem setTitle(String title) {
        this.title = title;
        return this;
    }

    public int getType() {
        return type;
    }

    public CalendarItem setType(int type) {
        this.type = type;
        return this;
    }

    public CalendarItem setEvent(String event) {
        this.event = event;

        return this;
    }
}
