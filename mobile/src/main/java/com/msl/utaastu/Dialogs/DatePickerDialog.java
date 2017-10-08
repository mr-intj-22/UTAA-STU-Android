package com.msl.utaastu.Dialogs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;

import com.msl.utaastu.Exam.ExamBottomSheet;
import com.msl.utaastu.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Created by Malek Shefat on 6/15/2017.
 */

public class DatePickerDialog extends DialogFragment implements View.OnClickListener {

    private DatePicker datePicker;
    private Button pick, cancel;
    private SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM", Locale.US);
    private String date_text = "";
    private Calendar date;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.dialog_date_picker, container, false);
        Bundle args = getArguments();
        date_text = args.getString("date");
        datePicker = root.findViewById(R.id.exam_date_picker);
        date = Calendar.getInstance();
        long two_months = TimeUnit.DAYS.toMillis(60);
        datePicker.setMaxDate(date.getTimeInMillis() + two_months);
        datePicker.setMinDate(date.getTimeInMillis());
        try {
            date.setTime(sdf.parse(date_text));
            datePicker.init(datePicker.getYear(), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH), null);
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
                date.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
                date_text = sdf.format(date.getTime());
                if (getTargetFragment() instanceof ExamBottomSheet)
                    ((ExamBottomSheet) getTargetFragment()).setDate(date_text);
                break;
        }
        dismiss();
    }
}
