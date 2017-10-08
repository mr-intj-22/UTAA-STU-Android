package com.msl.utaastu.BusSchedule;

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

import static com.msl.utaastu.Firebase.FirebaseConstants.BUS_KEY;

/**
 * Created by Malek Shefat on 6/19/2017.
 */

public class BusFragment extends Fragment implements ValueEventListener {

    //  Firebase
    private FirebaseDatabase database = MyApplication.getDatabase();

    private RecyclerView recyclerView;
    private BusAdapter adapter;
    private ProgressBar progressBar;
    private View empty;

    private ArrayList<MapLocation> locations = new ArrayList<>();
    private ArrayList<ArrayList<String>> to = new ArrayList<>();
    private ArrayList<ArrayList<String>> from = new ArrayList<>();

    private LoadDataAsync loadDataAsync;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.bus_fragment, container, false);

        DatabaseReference bus_times = database.getReference(BUS_KEY);

        recyclerView = root.findViewById(R.id.bus_recycler);
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        manager.setAutoMeasureEnabled(true);
        recyclerView.setLayoutManager(manager);
        adapter = new BusAdapter(getActivity());
        progressBar = root.findViewById(R.id.progressbar);
        empty = root.findViewById(R.id.emptyView);

        bus_times.addValueEventListener(this);

        return root;
    }

    private void getData(DataSnapshot dataSnapshot) {
        if (from.size() > 0) from.clear();
        if (to.size() > 0) to.clear();
        if (locations.size() > 0) locations.clear();
        for (DataSnapshot destination : dataSnapshot.getChildren()) {    // get the days one by one
            String name = destination.getKey().substring(1).replace("Sasmaz", "Şaşmaz").replace("Umitkoy", "Ümitköy");
            String[] loc = destination.child("location").getValue(String.class).split(",");
            locations.add(new MapLocation(name, Double.valueOf(loc[0]), Double.valueOf(loc[1])));
            to.add(new ArrayList<String>());
            for (DataSnapshot time : destination.child("to").getChildren()) {    // get the times one by one
                to.get(to.size() - 1).add(time.getValue(String.class));
            }
            from.add(new ArrayList<String>());
            for (DataSnapshot time : destination.child("from").getChildren()) {    // get the times one by one
                from.get(from.size() - 1).add(time.getValue(String.class));
            }
        }
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        if (dataSnapshot.getChildrenCount() > 0) {
            stopAsyncTasks();
            loadDataAsync = new LoadDataAsync();
            loadDataAsync.execute(dataSnapshot);
        }else {
            empty.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        empty.setVisibility(View.VISIBLE);
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
            if (locations.size() > 0) {
                adapter.setData(from, to, locations);
                recyclerView.setAdapter(adapter);
                empty.setVisibility(View.GONE);
            }else {
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
