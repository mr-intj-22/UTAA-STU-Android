package com.msl.utaastu.Notifications;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.msl.utaastu.Activities.MainActivity;
import com.msl.utaastu.Application.MyApplication;
import com.msl.utaastu.R;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

import static com.msl.utaastu.Firebase.FirebaseConstants.GENERAL_KEY;
import static com.msl.utaastu.Firebase.FirebaseConstants.NOTIFICATIONS_ITEMS;
import static com.msl.utaastu.Firebase.FirebaseConstants.NOTIFICATIONS_KEY;

/**
 * Created by Malek Shefat on 7/13/2017.
 */

public class NotificationsActivity extends AppCompatActivity implements ValueEventListener, NotificationsAdapter.ClickListener {

    //  Firebase
    private FirebaseUser user = MyApplication.getUser();
    private FirebaseDatabase database = MyApplication.getDatabase();
    private DatabaseReference notifications;

    private RecyclerView recyclerView;
    private NotificationsAdapter adapter;
    private ProgressBar progressBar;
    private View empty;
    private LoadDataAsync loadDataAsync;

    private ArrayList<NotificationItem> notificationItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            for (String key : bundle.keySet()) {
                //if (key.equals("from"))
            }
        }
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        notifications = database.getReference(NOTIFICATIONS_KEY).child(NOTIFICATIONS_ITEMS);

        recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setAutoMeasureEnabled(true);
        recyclerView.setLayoutManager(manager);
        adapter = new NotificationsAdapter(this);
        adapter.setListener(this);
        progressBar = findViewById(R.id.progressbar);
        empty = findViewById(R.id.emptyView);

        notifications.addListenerForSingleValueEvent(this);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    private void getData(DataSnapshot dataSnapshot) {
        for (DataSnapshot notification : dataSnapshot.getChildren()) {    // get the days one by one
            NotificationItem item = notification.getValue(NotificationItem.class);
            notificationItems.add(item);
        }
        Collections.sort(notificationItems, new Comparator<NotificationItem>() {
            DateFormat f = new SimpleDateFormat("yyyy/MM/dd", Locale.US);

            @Override
            public int compare(NotificationItem lhs, NotificationItem rhs) {
                try {
                    return f.parse(rhs.getDate()).compareTo(f.parse(lhs.getDate()));
                } catch (ParseException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        });
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        if (dataSnapshot.getChildrenCount() > 0) {
            stopAsyncTasks();
            loadDataAsync = new LoadDataAsync();
            loadDataAsync.execute(dataSnapshot);
        } else {
            empty.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        empty.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onRemove() {
        notifications.child(user.getUid()).removeValue();
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
            if (notificationItems.size() > 0) {
                adapter.setData(notificationItems);
                recyclerView.setAdapter(adapter);
                empty.setVisibility(View.GONE);
            } else {
                empty.setVisibility(View.VISIBLE);
            }
        }
    }

    private void stopAsyncTasks() {
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
