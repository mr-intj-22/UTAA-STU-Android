package com.msl.utaastu.Food;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.msl.utaastu.Application.MyApplication;
import com.msl.utaastu.R;

import java.util.ArrayList;

import static com.msl.utaastu.Firebase.FirebaseConstants.FOOD_KEY;

/**
 * Created by Malek Shefat on 6/19/2017.
 */

public class FoodFragment extends Fragment implements ValueEventListener {

    //  Firebase
    private FirebaseDatabase database = MyApplication.getDatabase();

    private RecyclerView recyclerView;
    private FoodAdapter adapter;
    private ProgressBar progressBar;
    private View empty;

    private ArrayList<FoodItem> table = new ArrayList<>();
    private ArrayList<String> dates = new ArrayList<>();
    private int n;

    private LoadDataAsync loadDataAsync;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.food_fragment, container, false);

        DatabaseReference food_table = database.getReference(FOOD_KEY);

        recyclerView = root.findViewById(R.id.recyclerView);
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        manager.setAutoMeasureEnabled(true);
        recyclerView.setLayoutManager(manager);
        adapter = new FoodAdapter(getActivity());
        progressBar = root.findViewById(R.id.progressbar);
        empty = root.findViewById(R.id.emptyView);


        food_table.addValueEventListener(this);

        return root;
    }

    private void getData(DataSnapshot dataSnapshot) {
        if (dates.size() > 0) dates.clear();
        if (table.size() > 0) table.clear();
        for (DataSnapshot date : dataSnapshot.getChildren()) {    // get the days one by one
            n = (int) date.getChildrenCount();
            dates.add(date.getKey());
            for (DataSnapshot meal : date.getChildren()) {
                FoodItem item = meal.getValue(FoodItem.class);
                table.add(item);
            }
        }
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
            if (table.size() > 0) {
                adapter.setData(table, dates, n);
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
