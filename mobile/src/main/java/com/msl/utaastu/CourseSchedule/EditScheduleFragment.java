package com.msl.utaastu.CourseSchedule;

import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy;
import com.msl.utaastu.Activities.MainActivity;
import com.msl.utaastu.Dialogs.TimePickerDialog;
import com.msl.utaastu.Interfaces.OnBackPressedListener;
import com.msl.utaastu.R;
import com.msl.utaastu.Services.AlarmManagerHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.toptas.fancyshowcase.FancyShowCaseView;

import static android.app.Activity.RESULT_OK;
import static android.support.v7.widget.helper.ItemTouchHelper.DOWN;
import static android.support.v7.widget.helper.ItemTouchHelper.END;
import static android.support.v7.widget.helper.ItemTouchHelper.START;
import static android.support.v7.widget.helper.ItemTouchHelper.UP;
import static com.msl.utaastu.Application.MyApplication.getDatabase;
import static com.msl.utaastu.Application.MyApplication.getUser;
import static com.msl.utaastu.Application.MyApplication.getWritableCoursesDatabase;
import static com.msl.utaastu.Application.MyApplication.getWritableLecturesTimesDatabase;
import static com.msl.utaastu.Application.MyApplication.isAlarmSet;
import static com.msl.utaastu.Firebase.FirebaseConstants.COURSE_KEY;

/**
 * Created by Malek Shefat on 6/16/2017.
 */

public class EditScheduleFragment extends Fragment implements EditScheduleAdapter.ClickListener,
        OnBackPressedListener, ValueEventListener, View.OnClickListener {

    private final int REQUEST_CODE_PICK_PDF = 102;
    private FirebaseDatabase database = getDatabase();
    private FirebaseUser user = getUser();
    private DatabaseReference lectures;

    private RecyclerView recyclerView;
    private EditScheduleAdapter adapter;

    private FloatingActionButton load_fab;
    private View horizontal_pb;

    private List<ScheduleItem> lecturesList = new ArrayList<>();
    private List<DayItem> days = new ArrayList<>();
    private ArrayList<String> dayList = new ArrayList<>(Arrays.asList("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"));

    private EditText timeText;

    private Paint p = new Paint();

    private ProgressBar progressBar;
    private LoadDataAsync loadDataAsync;
    private GetText getText;

    private String message = null;
    private FancyShowCaseView showCaseView;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.course_fragment, container, false);

        ((MainActivity) getActivity()).setOnBackPressedListener(this);

        progressBar = root.findViewById(R.id.progressbar);

        lectures = database.getReference(COURSE_KEY).child(user.getUid());

        load_fab = root.findViewById(R.id.fab_load);
        load_fab.setOnClickListener(this);
        horizontal_pb = root.findViewById(R.id.progressbar_horizontal);

        recyclerView = root.findViewById(R.id.course_recycler);
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        manager.setAutoMeasureEnabled(true);
        recyclerView.setLayoutManager(manager);
        adapter = new EditScheduleAdapter(getActivity());
        adapter.setClickListener(this);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    load_fab.hide();
                } else {
                    load_fab.show();
                }
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        showCaseView = new FancyShowCaseView.Builder(getActivity())
                .focusOn(load_fab)
                .backgroundColor(getResources().getColor(R.color.color_primary_trans))
                .title(getString(R.string.pick_schedule_pdf))
                .titleStyle(R.style.TipsTitleStyle, -1)
                .showOnce("pick_schedule_pdf")
                .build();
        showCaseView.show();

        lectures.addValueEventListener(this);

        setHasOptionsMenu(true);
        return root;
    }

    private void getData(DataSnapshot dataSnapshot) {
        for (DataSnapshot day : dataSnapshot.getChildren()) {    // get the days one by one
            int items = 0;
            DayItem dayItem = new DayItem();
            dayItem.setDay(day.getKey().substring(1));
            for (DataSnapshot lecture : day.getChildren()) {    // get the lectures one by one
                ScheduleItem item = lecture.getValue(ScheduleItem.class);
                lecturesList.add(item);
                items++;
            }
            dayItem.setLectures(items);
            days.add(dayItem);
        }
        for (String item : dayList) {
            boolean notIn = true;
            for (DayItem day : days) {
                if (day.getDay().equalsIgnoreCase(item)) {
                    notIn = false;
                    break;
                }
            }
            if (notIn)
                days.add(dayList.indexOf(item), new DayItem().setDay(item).setLectures(0));
        }
    }

    @Override
    public void onTimePicked(EditText time) {
        this.timeText = time;
        FragmentManager manager = getActivity().getSupportFragmentManager();
        TimePickerDialog timePickerDialog = new TimePickerDialog();
        timePickerDialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogTheme);
        timePickerDialog.setTargetFragment(this, 0);
        Bundle args = new Bundle();
        String time_text = time.getText().toString().replace(" ", "");
        if (TextUtils.isEmpty(time.getText()))
            time_text = "08:40 - 10:30";
        String[] times = time_text.split("-");
        args.putString("start", times[0]);
        args.putString("end", times[1]);
        timePickerDialog.setArguments(args);
        timePickerDialog.show(manager, "timePick");
    }

    public void setTime(String time) {
        timeText.setText(time);
        adapter.setEdited(true);
    }

    private void initSwipe() {
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback =
                new ItemTouchHelper.SimpleCallback(0, START | END) {

                    @Override
                    public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                        if (viewHolder instanceof EditScheduleAdapter.ItemHolder) {
                            return super.getSwipeDirs(recyclerView, viewHolder);
                        }
                        return 0;
                    }

                    @Override
                    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                        int dragFlags = (viewHolder instanceof EditScheduleAdapter.ItemHolder) ? UP | DOWN : 0;
                        int swipeFlags = (viewHolder instanceof EditScheduleAdapter.ItemHolder) ? START | END : 0;
                        return makeMovementFlags(dragFlags, swipeFlags);
                    }

                    @Override
                    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                        adapter.moveItem(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                        return true;
                    }

                    @Override
                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                        int position = viewHolder.getAdapterPosition();
                        adapter.removeItem(position, recyclerView);
                    }

                    @Override
                    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

                        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {

                            View itemView = viewHolder.itemView;

                            p.setColor(Color.parseColor("#D32F2F"));
                            RectF background = new RectF((float) 0, (float) itemView.getTop(), (float) itemView.getRight(), (float) itemView.getBottom());
                            c.drawRect(background, p);
                        }
                        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                    }
                };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    public void showAlertDialog(Context context, String title, String message) {
        final AlertDialog alertDialog = new AlertDialog.Builder(context).create();

        // Setting Dialog Title
        alertDialog.setTitle(title);

        // Setting Dialog Message
        alertDialog.setMessage(message);

        // Setting OK Button
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.save_changes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
                uploadSchedule();
            }
        });
        // Setting cancel Button
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.discard_changes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
                showFragment(new ScheduleFragment());
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.course_edit_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_changes:
                uploadSchedule();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void uploadSchedule() {
        days = adapter.getDayItems();
        lecturesList = adapter.getScheduleItems();
        lectures.removeValue();
        getWritableCoursesDatabase().deleteAllData();
        getWritableLecturesTimesDatabase().deleteAllData();
        if (isAlarmSet())
            try {
                AlarmManagerHelper.setAlarm(getContext(), true);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        int prev = 0;
        for (int i = 0; i < days.size(); i++) {
            if (days.get(i).getLectures() == 0)
                continue;
            for (int j = 0; j < days.get(i).getLectures(); j++) {
                DatabaseReference lecture = database.getReference(COURSE_KEY).child(user.getUid())
                        .child((i + 1) + days.get(i).getDay()).child("lecture_" + (j + 1));
                lecture.setValue(lecturesList.get(j + prev));
                String course_id = lecturesList.get(j + prev).getCode();
                String course_time = lecturesList.get(j + prev).getTime();
                if (!getWritableCoursesDatabase().isExist(course_id))
                    getWritableCoursesDatabase().addString(course_id);
                if (!TextUtils.isEmpty(course_time))
                    getWritableLecturesTimesDatabase().addItem(new TimeItem().setDay(days.get(i).getDay()).setTime(course_time));
            }
            prev += days.get(i).getLectures();
        }
        if (isAlarmSet() && getContext() != null) {
            NotificationManager notificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !notificationManager.isNotificationPolicyAccessGranted()) {
                //  ask permission to Access Do Not Disturb state !
                Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                startActivity(intent);
            }
            try {
                AlarmManagerHelper.setAlarm(getContext(), false);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        showFragment(new ScheduleFragment());
    }

    @Override
    public void doBack() {
        if (adapter.isEdited()) {
            showAlertDialog(getContext(), getString(R.string.unsaved_work), getString(R.string.changes_warning));
        } else {
            showFragment(new ScheduleFragment());
        }
    }

    private void showFragment(Fragment fragment) {
        ((MainActivity) getActivity()).setOnBackPressedListener(null);
        ((MainActivity) getActivity()).setButtonBarItemsEnabled(true);
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_down)
                .replace(R.id.container, fragment)
                .commit();
        animateTitleChange(R.string.course_table);
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

    private void stopAsyncTasks() {
        if (loadDataAsync != null && loadDataAsync.getStatus() == AsyncTask.Status.RUNNING)
            loadDataAsync.cancel(true);
        loadDataAsync = null;
        if (getText != null && getText.getStatus() == AsyncTask.Status.RUNNING)
            getText.cancel(true);
        getText = null;
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        stopAsyncTasks();
        loadDataAsync = new LoadDataAsync();
        loadDataAsync.execute(dataSnapshot);
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_load:
                pickPDF();
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
            adapter.setData(lecturesList, days);
            recyclerView.setAdapter(adapter);
            initSwipe();
        }
    }

    @Override
    public void onDestroy() {
        if (showCaseView.isShown())
            showCaseView.hide();
        ((MainActivity) getActivity()).setOnBackPressedListener(null);
        stopAsyncTasks();
        super.onDestroy();
    }

    /**
     * Strips the text from a PDF and displays the text on screen
     */
    public List<String> stripText(InputStream pdf) {
        String parsedText = "";
        try {
            PdfReader reader = new PdfReader(pdf);
            int n = reader.getNumberOfPages();
            for (int i = 0; i < n; i++) {
                parsedText = parsedText + PdfTextExtractor.getTextFromPage(reader, i + 1, new SimpleTextExtractionStrategy()).trim() + "\n"; //Extracting the content from the different pages
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Arrays.asList(parsedText.split(System.getProperty("line.separator")));
    }

    private class GetText extends AsyncTask<InputStream, Void, Void> {

        @Override
        protected Void doInBackground(InputStream... params) {

            if (isCancelled())
                return null;

            try {
                loadData(params[0]);
            } catch (Exception e) {
                e.printStackTrace();
                message = getString(R.string.was_not_extracted);
            }
            return null;
        }

        private void loadData(InputStream pdf) {

            List<String> tr_days = Arrays.asList("PAZARTESİ", "SALI", "ÇARŞAMBA", "PERŞEMBE", "CUMA", "CUMARTESİ", "PAZAR");
            List<String> en_days = Arrays.asList("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY");

            List<ScheduleItem> lecturesListTemp = new ArrayList<>();
            List<DayItem> daysTemp = new ArrayList<>();

            List<String> src = stripText(pdf);
            boolean isDay;
            int count = 0;

            DayItem dayItem = null;

            for (int i = 0; i < src.size(); i++) { //  get Schedule line by line
                isDay = tr_days.contains(src.get(i));
                if (isDay) {  //  this is a isDay, we  need this, so grape it;
                    dayItem = new DayItem().setDay(en_days.get(tr_days.indexOf(src.get(i)))).setLectures(0);
                    daysTemp.add(dayItem);
                    count = 0;
                } else if (src.get(i).contains("DERS KODU") || src.get(i).contains("Hava Kurumu") || src.get(i).contains("Ders")) {  //  the headers "we don't need them so skip;
                    continue;
                } else {    //  this is a lecture data; process it;
                    List<String> data = Arrays.asList(src.get(i).split(" "));
                    ScheduleItem item = new ScheduleItem();
                    item.setSection(data.get(0));
                    item.setCode(data.get(1) + " " + data.get(2));
                    item.setPlace(data.get(3));
                    item.setTime(data.get(5).replace("u", "") + " - " + data.get(4).replace("u", ""));
                    lecturesListTemp.add(item);
                    count++;
                }
                if (dayItem != null)
                    dayItem.setLectures(count);
            }
            for (String item : en_days) {
                boolean notIn = true;
                for (DayItem day : daysTemp) {
                    if (day.getDay().equalsIgnoreCase(item)) {
                        notIn = false;
                        break;
                    }
                }
                if (notIn)
                    daysTemp.add(en_days.indexOf(item), new DayItem().setDay(item).setLectures(0));
            }
            if (daysTemp.size() > 0 && lecturesListTemp.size() > 0) {
                days = daysTemp;
                lecturesList = lecturesListTemp;
                message = getString(R.string.extracted_successfully);
            }
        }

        @Override
        protected void onPostExecute(Void mVoid) {
            super.onPostExecute(mVoid);
            if (lecturesList.size() > 0) {
                adapter = new EditScheduleAdapter(getActivity());
                adapter.setClickListener(EditScheduleFragment.this);
                adapter.setData(lecturesList, days);
                recyclerView.setAdapter(adapter);
            }
            Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
            horizontal_pb.setVisibility(View.GONE);
        }
    }

    private void pickPDF() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        startActivityForResult(intent, REQUEST_CODE_PICK_PDF);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_PICK_PDF:
                if (resultCode == RESULT_OK) {
                    horizontal_pb.setVisibility(View.VISIBLE);
                    Uri uri = data.getData();
                    try {
                        String path = uri.getPath();
                        File pdf = new File(path);
                        FileInputStream pdfInputStream = new FileInputStream(pdf);
                        stopAsyncTasks();
                        getText = new GetText();
                        getText.execute(pdfInputStream);
                        return;
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    try {
                        stopAsyncTasks();
                        getText = new GetText();
                        getText.execute(getActivity().getContentResolver().openInputStream(uri));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        message = getString(R.string.not_supported);
                        horizontal_pb.setVisibility(View.GONE);
                        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }
}
