package com.msl.utaastu.Services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.msl.utaastu.Application.MyApplication;
import com.msl.utaastu.CourseSchedule.TimeItem;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Malek Shefat on 7/7/2017.
 */

public class AlarmManagerHelper {

    private static SimpleDateFormat sdf = new SimpleDateFormat("EEEEE, HH:mm", Locale.US);
    private static List<String> days = new ArrayList<>(
            Arrays.asList("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
    );

    public static void setAlarm(Context c, boolean cancel) throws ParseException {
        List<TimeItem> items = MyApplication.getWritableLecturesTimesDatabase().getAllItems();
        Calendar now = Calendar.getInstance();
        for (TimeItem item : items) {
            String[] times = item.getTime().split(" - ");
            String start = item.getDay() + ", " + times[0];
            String end = item.getDay() + ", " + times[1];
            Date startDate = sdf.parse(start);
            Date endDate = sdf.parse(end);
            Calendar calendar_start = Calendar.getInstance();
            calendar_start.setTime(startDate);
            calendar_start.set(Calendar.YEAR, now.get(Calendar.YEAR));
            calendar_start.set(Calendar.MONTH, now.get(Calendar.MONTH));
            calendar_start.set(Calendar.WEEK_OF_MONTH, now.get(Calendar.WEEK_OF_MONTH));
            calendar_start.set(Calendar.DAY_OF_WEEK, days.indexOf(item.getDay()) + 1);
            if (calendar_start.getTimeInMillis() < now.getTimeInMillis()) {
                calendar_start.set(Calendar.WEEK_OF_MONTH, now.get(Calendar.WEEK_OF_MONTH) + 1);
            }
            if (cancel)
                cancelAll(c, getCharsAsInt(calendar_start), 0);
            else
                setStartTime(c, calendar_start, getCharsAsInt(calendar_start));
            Calendar calendar_end = Calendar.getInstance();
            calendar_end.setTime(endDate);
            calendar_end.set(Calendar.YEAR, now.get(Calendar.YEAR));
            calendar_end.set(Calendar.MONTH, now.get(Calendar.MONTH));
            calendar_end.set(Calendar.WEEK_OF_MONTH, calendar_start.get(Calendar.WEEK_OF_MONTH));
            calendar_end.set(Calendar.DAY_OF_WEEK, days.indexOf(item.getDay()) + 1);
            if (cancel)
                cancelAll(c, getCharsAsInt(calendar_end), 1);
            else
                setFinishTime(c, calendar_end, getCharsAsInt(calendar_end));
        }
    }

    private static void setStartTime(Context context, Calendar start, int id) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmBroadCast.class);
        intent.setAction("com.msl.utaa.audio.silence");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, id,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        manager.setRepeating(AlarmManager.RTC_WAKEUP, start.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY * 7, pendingIntent);
    }

    private static void setFinishTime(Context context, Calendar end, int id) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmBroadCast.class);
        intent.setAction("com.msl.utaa.audio.normal");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, id,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        manager.setRepeating(AlarmManager.RTC_WAKEUP, end.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY * 7, pendingIntent);
    }

    private static void cancelAll(Context context, int id, int i) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmBroadCast.class);
        if (i == 1)
            intent.setAction("com.msl.utaa.audio.normal");
        else
            intent.setAction("com.msl.utaa.audio.silence");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, id,
                intent, PendingIntent.FLAG_CANCEL_CURRENT);
        manager.cancel(pendingIntent);
    }

    private static int getCharsAsInt(Calendar calendar) {
        return Integer.parseInt(calendar.get(Calendar.DAY_OF_WEEK) + "" + calendar.get(Calendar.HOUR_OF_DAY) +
                "" + calendar.get(Calendar.MINUTE));
    }
}
