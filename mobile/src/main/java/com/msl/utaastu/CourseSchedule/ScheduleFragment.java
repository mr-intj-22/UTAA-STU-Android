package com.msl.utaastu.CourseSchedule;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.msl.utaastu.Activities.MainActivity;
import com.msl.utaastu.Application.MyApplication;
import com.msl.utaastu.R;
import com.msl.utaastu.Services.AlarmManagerHelper;
import com.msl.utaastu.Utils.ShareImage;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import static com.msl.utaastu.Firebase.FirebaseConstants.COURSE_KEY;

/**
 * Created by Malek Shefat on 6/16/2017.
 */

public class ScheduleFragment extends Fragment implements ValueEventListener, View.OnClickListener {

    private FirebaseDatabase database = MyApplication.getDatabase();
    private FirebaseUser user = MyApplication.getUser();
    private DatabaseReference lectures;

    private RecyclerView recyclerView;
    private ScheduleAdapter adapter;
    private ProgressBar progressBar;

    private View emptyView;
    private Button fillButton;

    private List<ScheduleItem> lecturesList = new ArrayList<>();
    private List<DayItem> days = new ArrayList<>();

    //  tasks
    private ShareImage shareImageAsync;
    private LoadDataAsync loadDataAsync;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.course_fragment, container, false);

        lectures = database.getReference(COURSE_KEY).child(user.getUid());

        progressBar = root.findViewById(R.id.progressbar);
        emptyView = root.findViewById(R.id.emptyView);
        fillButton = root.findViewById(R.id.fill);
        fillButton.setOnClickListener(this);

        ((FloatingActionButton) root.findViewById(R.id.fab_load)).hide();

        recyclerView = root.findViewById(R.id.course_recycler);
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(manager);
        adapter = new ScheduleAdapter(getActivity());

        lectures.addValueEventListener(this);

        setHasOptionsMenu(true);
        return root;
    }

    private void getData(DataSnapshot dataSnapshot) {
        boolean firstTime = MyApplication.getWritableLecturesTimesDatabase().getAllItems().size() == 0;
        for (DataSnapshot day : dataSnapshot.getChildren()) {    // get the days one by one
            int items = 0;
            DayItem dayItem = new DayItem();
            dayItem.setDay(day.getKey().substring(1));
            for (DataSnapshot lecture : day.getChildren()) {    // get the lectures one by one
                ScheduleItem item = lecture.getValue(ScheduleItem.class);
                lecturesList.add(item);
                String course_id = item.getCode();
                String course_time = item.getTime();
                if (!MyApplication.getWritableCoursesDatabase().isExist(course_id))
                    MyApplication.getWritableCoursesDatabase().addString(course_id);
                TimeItem timeItem = new TimeItem().setDay(dayItem.getDay()).setTime(course_time);
                if (!MyApplication.getWritableLecturesTimesDatabase().isExist(timeItem))
                    MyApplication.getWritableLecturesTimesDatabase().addItem(timeItem);
                items++;
            }
            dayItem.setLectures(items);
            days.add(dayItem);
        }
        if (firstTime && MyApplication.isAlarmSet()) {
            if (getActivity() != null) {
                NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !notificationManager.isNotificationPolicyAccessGranted()) {
                    //  ask permission to Access Do Not Disturb state !
                    Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                    startActivity(intent);
                }
            }
            try {
                AlarmManagerHelper.setAlarm(getContext(), false);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.course_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_schedule:
                showFragment(new EditScheduleFragment());
                break;
            case R.id.share_schedule:
                if (lecturesList.size() > 0) {
                    stopAsyncTasks();
                    shareImageAsync = new ShareImage(getActivity(), recyclerView);
                    shareImageAsync.execute();
                } else {
                    Snackbar.make(recyclerView, R.string.nothing_to_share, Snackbar.LENGTH_SHORT)
                            .setAction(R.string.edit_schedule, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    showFragment(new EditScheduleFragment());
                                }
                            })
                            .show();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
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
                    if (getActivity() != null)
                        getActivity().setTitle(newTitle);

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

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        if (dataSnapshot.getChildrenCount() == 0) {
            progressBar.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            stopAsyncTasks();
            loadDataAsync = new LoadDataAsync();
            loadDataAsync.execute(dataSnapshot);
        }
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fill:
                showFragment(new EditScheduleFragment());
                break;
        }
    }

    private class LoadDataAsync extends AsyncTask<DataSnapshot, Void, Void> {

        @Override
        protected Void doInBackground(DataSnapshot... dataSnapshots) {
            if (isCancelled())
                return null;
            getData(dataSnapshots[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (isCancelled())
                return;
            progressBar.setVisibility(View.GONE);
            if (lecturesList.size() > 0) {
                adapter.setData(lecturesList, days);
                recyclerView.setAdapter(adapter);
                emptyView.setVisibility(View.GONE);
            }
        }
    }

    private void stopAsyncTasks() {
        if (shareImageAsync != null && shareImageAsync.getStatus() == AsyncTask.Status.RUNNING)
            shareImageAsync.cancel(true);
        shareImageAsync = null;
        if (loadDataAsync != null && loadDataAsync.getStatus() == AsyncTask.Status.RUNNING)
            loadDataAsync.cancel(true);
        loadDataAsync = null;
    }

    @Override
    public void onDestroy() {
        stopAsyncTasks();
        super.onDestroy();
    }
}
