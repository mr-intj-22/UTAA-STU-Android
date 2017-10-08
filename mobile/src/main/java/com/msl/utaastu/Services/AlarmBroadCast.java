package com.msl.utaastu.Services;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;

import com.msl.utaastu.Application.MyApplication;

import java.text.ParseException;

public class AlarmBroadCast extends BroadcastReceiver {


    private static final String NORMAL = "com.msl.utaa.audio.normal";
    private static final String SILENCE = "com.msl.utaa.audio.silence";
    private static final String BOOT_COMPLETE = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {
        // an Intent broadcast.
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !notificationManager.isNotificationPolicyAccessGranted()){
            //  Can't Access Do Not Disturb state !
            return;
        }
        String action = intent.getAction();
        if (action.equals(NORMAL)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL);
            }else {
                AudioManager audiomanager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                audiomanager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            }
        } else if (action.equals(SILENCE)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE);
            }else {
                AudioManager audiomanager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                audiomanager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
            }
        } else if (action.equals(BOOT_COMPLETE) && MyApplication.isAlarmSet()) {
            try {
                AlarmManagerHelper.setAlarm(context, false);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }
}
