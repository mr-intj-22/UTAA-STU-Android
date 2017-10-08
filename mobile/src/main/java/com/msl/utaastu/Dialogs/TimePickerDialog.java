package com.msl.utaastu.Dialogs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;

import com.msl.utaastu.CourseSchedule.EditScheduleFragment;
import com.msl.utaastu.Exam.ExamBottomSheet;
import com.msl.utaastu.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by Malek Shefat on 6/15/2017.
 */

public class TimePickerDialog extends DialogFragment implements View.OnClickListener {

    private TimePicker timePicker;
    private Button pick, cancel;
    private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.US);
    private String start = "", end = "";
    private Calendar startDate, endDate;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.dialog_time_picker, container, false);
        Bundle args = getArguments();
        start = args.getString("start");
        end = args.getString("end", "00:00");
        timePicker = root.findViewById(R.id.lecture_time_picker);
        timePicker.setIs24HourView(true);
        startDate = Calendar.getInstance();
        endDate = Calendar.getInstance();
        try {
            startDate.setTime(sdf.parse(start));
            timePicker.setCurrentHour(startDate.get(Calendar.HOUR_OF_DAY));
            timePicker.setCurrentMinute(startDate.get(Calendar.MINUTE));
            start = "";
        } catch (ParseException e) {
            e.printStackTrace();
        }
        pick = root.findViewById(R.id.lecture_time_button);
        pick.setOnClickListener(this);
        cancel = root.findViewById(R.id.lecture_time_cancel_button);
        cancel.setOnClickListener(this);
        return root;
    }

    @Override
    public void onStart() {
        getDialog().getWindow().setWindowAnimations(
                R.style.DialogAnimationSlide);
        super.onStart();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.lecture_time_button:
                if (!TextUtils.isEmpty(start)) {
                    end = checkDigit(timePicker.getCurrentHour()) + ":" + checkDigit(timePicker.getCurrentMinute());
                    try {
                        endDate.setTime(sdf.parse(end));
                        timePicker.setCurrentHour(endDate.get(Calendar.HOUR_OF_DAY));
                        timePicker.setCurrentMinute(endDate.get(Calendar.MINUTE));
                        if (endDate.getTimeInMillis() - (50 * 60 * 1000) < startDate.getTimeInMillis()) {
                            Toast.makeText(getContext(), R.string.min_50_mins, Toast.LENGTH_LONG).show();
                        } else {
                            if (getTargetFragment() instanceof EditScheduleFragment)
                                ((EditScheduleFragment) getTargetFragment()).setTime(start + " - " + end);
                            dismiss();
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                } else {
                    start = checkDigit(timePicker.getCurrentHour()) + ":" + checkDigit(timePicker.getCurrentMinute());
                    dismissAndSave();
                    pick.setText(R.string.end_button);
                    try {
                        startDate.setTime(sdf.parse(start));
                        endDate.setTime(sdf.parse(end));
                        timePicker.setCurrentMinute(endDate.get(Calendar.MINUTE));
                        timePicker.setCurrentHour(endDate.get(Calendar.HOUR_OF_DAY));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.lecture_time_cancel_button:
                dismiss();
                break;
        }
    }

    public String checkDigit(int number) {
        return number <= 9 ? "0" + number : String.valueOf(number);
    }

    private void dismissAndSave() {
        if (getTargetFragment() instanceof ExamBottomSheet) {
            ((ExamBottomSheet) getTargetFragment()).setTime(start);
            dismiss();
        }
    }
}
