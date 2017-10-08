package com.msl.utaastu.Exam;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.msl.utaastu.Activities.MainActivity;
import com.msl.utaastu.Application.MyApplication;
import com.msl.utaastu.CourseSchedule.EditScheduleFragment;
import com.msl.utaastu.Dialogs.DatePickerDialog;
import com.msl.utaastu.Dialogs.TimePickerDialog;
import com.msl.utaastu.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.msl.utaastu.Firebase.FirebaseConstants.DEPARTMENTS_NODE;
import static com.msl.utaastu.Firebase.FirebaseConstants.EDITABLE_NODE;
import static com.msl.utaastu.Firebase.FirebaseConstants.ELECTIVES_NODE;
import static com.msl.utaastu.Firebase.FirebaseConstants.EXAMS_KEY;

/**
 * Created by Malek Shefat on 6/15/2017.
 */

public class ExamBottomSheet extends BottomSheetDialogFragment implements ExamAdapter.ClickListener,
        CompoundButton.OnCheckedChangeListener, Toolbar.OnMenuItemClickListener, View.OnClickListener {

    private FirebaseDatabase database = MyApplication.getDatabase();
    private DatabaseReference examRef;

    private SimpleDateFormat sdf_date = new SimpleDateFormat("EEE, dd MMM", Locale.US);
    private SimpleDateFormat sdf_time = new SimpleDateFormat("HH:mm", Locale.US);

    private Toolbar toolbar;

    private RecyclerView recyclerView;
    private ExamAdapter adapter;
    private EditText timeText;
    private EditText dateText;
    private SwitchCompat editSwitcher;
    private View progressBar;

    private Paint p = new Paint();

    private String department = MyApplication.readDepartment();
    private boolean editable;
    private ArrayList<ExamItem> exams = new ArrayList<>();
    private List<String> courses = new ArrayList<>();
    private List<String> exams_string = new ArrayList<>();
    private List<String> departments = new ArrayList<>();

    private ExamsAsync examsAsync;
    private ElectivesAsync electivesAsync;
    private MenuItem menuItem;

    private Activity activity;

    @Override
    public void setupDialog(Dialog dialog, int style) {
        View root = View.inflate(getActivity(), R.layout.exam_bottom_sheet, null);
        dialog.setContentView(root);

        department = MyApplication.readDepartment();
        activity = getActivity();
        courses = MyApplication.getWritableCoursesDatabase().getAllStrings();

        examRef = database.getReference(EXAMS_KEY);

        toolbar = root.findViewById(R.id.toolbar);
        toolbar.setTitle(department);
        toolbar.inflateMenu(R.menu.exam_menu);
        toolbar.setOnMenuItemClickListener(this);
        menuItem = toolbar.getMenu().findItem(R.id.save_changes);
        progressBar = root.findViewById(R.id.progressbar_horizontal);
        editSwitcher = root.findViewById(R.id.editable);
        editSwitcher.setOnCheckedChangeListener(this);

        View emptyView = root.findViewById(R.id.emptyView);
        Button editSchedule = root.findViewById(R.id.edit_schedule);
        editSchedule.setOnClickListener(this);

        recyclerView = root.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new ExamAdapter(getActivity());
        adapter.setClickListener(this);

        getDepartments();
        getEditable();
        if (courses.size() > 0) {
            getExams();
        } else {
            editSwitcher.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        }
    }

    private void uploadExams() {
        DatabaseReference examDepartmentRef = examRef.child(DEPARTMENTS_NODE);
        for (int i = 0; i < exams.size(); i++) {
            String key = exams.get(i).getCode();
            if (exams.get(i).isElective()) {
                examDepartmentRef.child(ELECTIVES_NODE).child(key).setValue(exams.get(i));
            } else {
                examDepartmentRef.child(department).child(key).setValue(exams.get(i));
            }
        }
        dismiss();
    }

    private void getDepartments() {
        departments = MyApplication.getWritableDepartmentsDatabase().getAllStrings();
        if (departments.isEmpty()) {
            database.getReference(DEPARTMENTS_NODE).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String departments = dataSnapshot.getValue(String.class);
                    String[] departments_list = departments.split(", ");
                    if (departments_list.length > 0)
                        MyApplication.getWritableDepartmentsDatabase().deleteAllData();
                    for (String department : departments_list) {
                        MyApplication.getWritableDepartmentsDatabase().addString(department);
                    }
                    ExamBottomSheet.this.departments = MyApplication.getWritableDepartmentsDatabase().getAllStrings();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
    }

    private void getEditable() {
        examRef.child(EDITABLE_NODE).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    editable = dataSnapshot.getValue(Boolean.class);
                    editSwitcher.setEnabled(editable);
                    if (!editable)
                        editSwitcher.setText(getString(R.string.not_editable));
                    else
                        editSwitcher.setText(getString(R.string.editable));
                    menuItem.setVisible(editable);
                } catch (NullPointerException e) {
                    Log.d("UTAA-3", e.getMessage());
                    Toast.makeText(getActivity(), R.string.error, Toast.LENGTH_LONG).show();
                    dismiss();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getExams() {
        examRef.child(DEPARTMENTS_NODE).child(department).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                examsAsync = new ExamsAsync();
                examsAsync.execute(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getExams(DataSnapshot dataSnapshot) {
        for (DataSnapshot exam : dataSnapshot.getChildren()) {   //  exam by exam
            if (courses.contains(exam.getKey())) {
                ExamItem item = exam.getValue(ExamItem.class);
                exams.add(item);
                exams_string.add(item.getCode());
            }
            if (courses.size() == exams.size())
                break;
        }
    }

    private void getElectives() {
        examRef.child(DEPARTMENTS_NODE).child(ELECTIVES_NODE).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                electivesAsync = new ElectivesAsync();
                electivesAsync.execute(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getElectives(DataSnapshot dataSnapshot) {
        for (DataSnapshot elective : dataSnapshot.getChildren()) {   //  elective by elective
            if (courses.contains(elective.getKey())) {
                ExamItem item = elective.getValue(ExamItem.class);
                item.setElective(true);
                exams.add(item);
                exams_string.add(item.getCode());
            }
            if (courses.size() == exams.size())
                break;
        }
    }

    private void initCourses() {
        for (String course : courses) {
            if (!exams_string.contains(course))
                exams.add(new ExamItem().setCode(course));
        }
        setRecyclerView();
    }

    private void setRecyclerView() {
        adapter.setData(editSwitcher.isChecked(), exams);
        recyclerView.setAdapter(adapter);
        progressBar.setVisibility(View.GONE);
    }

    private void sort() {
        Collections.sort(exams, new Comparator<ExamItem>() {
            public int compare(ExamItem o1, ExamItem o2) {
                if (getDate(o1.getDate(), o1.getTime()) == null || getDate(o2.getDate(), o2.getTime()) == null)
                    return 0;
                return getDate(o1.getDate(), o1.getTime()).compareTo(getDate(o2.getDate(), o2.getTime()));
            }
        });
    }

    private Date getDate(String d, String t) {
        Calendar date = Calendar.getInstance();
        Calendar time = Calendar.getInstance();
        try {
            date.setTime(sdf_date.parse(d));
            time.setTime(sdf_time.parse(t));
            date.set(Calendar.HOUR_OF_DAY, time.get(Calendar.HOUR_OF_DAY));
            date.set(Calendar.MINUTE, time.get(Calendar.MINUTE));
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new Date();
    }

    @Override
    public void onDateClick(EditText dateText) {
        this.dateText = dateText;
        FragmentManager manager = getActivity().getSupportFragmentManager();
        DatePickerDialog datePickerDialog = new DatePickerDialog();
        datePickerDialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogTheme);
        datePickerDialog.setTargetFragment(this, 0);
        Bundle args = new Bundle();
        args.putString("date", dateText.getText().toString());
        datePickerDialog.setArguments(args);
        datePickerDialog.show(manager, "datePick");
    }

    @Override
    public void onTimeClick(EditText timeText) {
        this.timeText = timeText;
        FragmentManager manager = getActivity().getSupportFragmentManager();
        TimePickerDialog timePickerDialog = new TimePickerDialog();
        timePickerDialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogTheme);
        Bundle args = new Bundle();
        String time_text = timeText.getText().toString().replace(" ", "");
        if (TextUtils.isEmpty(timeText.getText()))
            time_text = "08:40";
        args.putString("start", time_text);
        timePickerDialog.setTargetFragment(this, 0);
        timePickerDialog.setArguments(args);
        timePickerDialog.show(manager, "timePick");
    }

    public void setTime(String time) {
        timeText.setText(time);
    }

    public void setDate(String date) {
        dateText.setText(date);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        adapter.setData(isChecked, exams);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_changes:
                if (editSwitcher.isEnabled())
                    uploadExams();
                break;
        }
        return true;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.edit_schedule:
                showFragment(new EditScheduleFragment());
                dismiss();
                break;
        }
    }

    private class ExamsAsync extends AsyncTask<DataSnapshot, Void, Void> {

        @Override
        protected Void doInBackground(DataSnapshot... params) {
            getExams(params[0]);
            if (courses.size() == exams.size())
                sort();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (exams.size() == 0 || courses.size() > exams.size())
                getElectives();
            else {
                setRecyclerView();
            }
        }
    }

    private class ElectivesAsync extends AsyncTask<DataSnapshot, Void, Void> {

        @Override
        protected Void doInBackground(DataSnapshot... params) {
            getElectives(params[0]);
            sort();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (exams.size() != courses.size()) {
                initCourses();
            } else {
                setRecyclerView();
            }
        }
    }

    private void stopAsyncTasks() {
        if (examsAsync != null && examsAsync.getStatus() == AsyncTask.Status.RUNNING)
            examsAsync.cancel(true);
        examsAsync = null;
        if (electivesAsync != null && electivesAsync.getStatus() == AsyncTask.Status.RUNNING)
            electivesAsync.cancel(true);
        electivesAsync = null;
    }

    @Override
    public void onDestroy() {
        stopAsyncTasks();
        super.onDestroy();
    }

    private void showFragment(Fragment fragment) {
        ((MainActivity) getActivity()).setButtonBarItemsEnabled(false);
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_down)
                .replace(R.id.container, fragment)
                .commit();
        animateTitleChange(R.string.course_table_edit);
    }

    private void animateTitleChange(final int newTitle) {
        final View view = getToolbarTitle();

        if (view instanceof TextView) {
            AlphaAnimation fadeOut = new AlphaAnimation(1f, 0f);
            fadeOut.setDuration(250);
            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (activity != null)
                        activity.setTitle(newTitle);

                    AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
                    fadeIn.setDuration(250);
                    view.startAnimation(fadeIn);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            view.startAnimation(fadeOut);
        }
    }

    private View getToolbarTitle() {
        int childCount = ((MainActivity) getActivity()).getToolbar().getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = ((MainActivity) getActivity()).getToolbar().getChildAt(i);
            if (child instanceof TextView) {
                return child;
            }
        }

        return new View(getActivity());
    }
}
