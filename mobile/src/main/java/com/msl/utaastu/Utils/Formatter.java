package com.msl.utaastu.Utils;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by MSL dev on 4/15/2016.
 */
public class Formatter {

    public static String FormatDates(Calendar date) {
        String formatted_date = "just now";

        date.setTimeZone(TimeZone.getTimeZone("GMT"));
        Calendar localTime = Calendar.getInstance(TimeZone.getTimeZone("GMT"));

        long difference = localTime.getTimeInMillis() - date.getTimeInMillis();

        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;
        long monthsInMilli = daysInMilli * 30;
        long yearsInMilli = monthsInMilli * 12;
        long yYears = Math.round(difference / yearsInMilli);
        long mMonths = Math.round(difference / monthsInMilli);
        long dDays = difference / daysInMilli;
        long hHours = difference / hoursInMilli;
        long mMinutes = difference / minutesInMilli;

        if (yYears == 0 && mMonths == 0 && dDays == 0 && hHours == 0 && (mMinutes > 0 && mMinutes < 60)) {
            formatted_date = (mMinutes == 1 ? "a min" : mMinutes + " mins") + " ago";
        } else if (yYears == 0 && mMonths == 0 && dDays == 0 && hHours > 0) {
            formatted_date = (hHours == 1 ? "an hour" : hHours + " hrs") + " ago";
        } else if (yYears == 0 && mMonths == 0 && dDays > 0) {
            if (dDays == 1) {
                formatted_date = "a day ago";
            } else if (dDays < 7) {
                formatted_date = dDays + " days ago";
            } else if ((int) Math.round(dDays / 7.0) == 1) {
                formatted_date = "a week ago";
            } else {
                formatted_date = ((int) Math.round(dDays / 7.0)) + " weeks ago";
            }
        } else if (yYears == 0 && mMonths > 0) {
            if (mMonths == 1) {
                formatted_date = "a month ago";
            } else {
                formatted_date = mMinutes + " months ago";
            }
        } else if (yYears > 0) {
            if (yYears == 1) {
                formatted_date = "a year ago";
            } else {
                formatted_date = yYears + " years ago";
            }
        }
        return formatted_date;
    }
}
