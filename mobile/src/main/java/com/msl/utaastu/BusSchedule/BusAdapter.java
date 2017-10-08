package com.msl.utaastu.BusSchedule;

import android.app.Activity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.MarkerOptions;
import com.msl.utaastu.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import static android.support.v7.widget.RecyclerView.ViewHolder;

/**
 * Created by Malek Shefat on 6/19/2017.
 */

public class BusAdapter extends RecyclerView.Adapter<BusAdapter.TimesHolder> {

    private Activity context;
    private LayoutInflater inflater;
    private ArrayList<ArrayList<String>> to = new ArrayList<>();
    private ArrayList<ArrayList<String>> from = new ArrayList<>();
    private ArrayList<MapLocation> mMapLocations = new ArrayList<>();

    private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.US);
    private Calendar now = Calendar.getInstance(Locale.US);

    public BusAdapter(Activity c) {
        this.context = c;
        inflater = LayoutInflater.from(c);
    }

    public void setData(ArrayList<ArrayList<String>> from, ArrayList<ArrayList<String>> to, ArrayList<MapLocation> locations) {
        this.from = from;
        this.to = to;
        this.mMapLocations = locations;
        notifyItemRangeChanged(0, locations.size());
    }

    @Override
    public TimesHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new TimesHolder(inflater.inflate(R.layout.bus_holder_time, parent, false));
    }

    @Override
    public void onBindViewHolder(TimesHolder holder, int position) {
        holder.destination.setText(mMapLocations.get(position).getName());
        holder.fromLayout.removeAllViews();
        holder.toLayout.removeAllViews();
        int next = 0, now_min = now.get(Calendar.MINUTE) + now.get(Calendar.HOUR_OF_DAY) * 60, current_min = 0;
        Calendar current = Calendar.getInstance(Locale.US);
        for (String time : from.get(position)) {
            try {
                current.setTime(sdf.parse(time.substring(0, 4)));
                current_min = current.get(Calendar.MINUTE) + current.get(Calendar.HOUR_OF_DAY) * 60;
            } catch (ParseException e) {
                e.printStackTrace();
            }
            View view = inflater.inflate(R.layout.bus_item_time, null, false);
            TextView textView = view.findViewById(R.id.bus_time);
            textView.setText(time);
            if (now_min > current_min + 2 ||
                    now.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY ||
                    now.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                textView.setTextColor(ContextCompat.getColor(context, R.color.red));
                next++;
            }
            holder.fromLayout.addView(view);
        }
        holder.setCurrent(next, 0);
        next = 0;
        now_min = now.get(Calendar.MINUTE) + now.get(Calendar.HOUR_OF_DAY) * 60;
        current_min = 0;
        current = Calendar.getInstance();
        for (String time : to.get(position)) {
            try {
                current.setTime(sdf.parse(time));
                current_min = current.get(Calendar.MINUTE) + current.get(Calendar.HOUR_OF_DAY) * 60;
            } catch (ParseException e) {
                e.printStackTrace();
            }
            View view = inflater.inflate(R.layout.bus_item_time, null, false);
            TextView textView = view.findViewById(R.id.bus_time);
            textView.setText(time);
            if (now_min > current_min + 2 ||
                    now.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY ||
                    now.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                textView.setTextColor(ContextCompat.getColor(context, R.color.red));
                next++;
            }
            holder.toLayout.addView(view);
        }
        holder.setCurrent(next, 1);

        //  init maps
        MapLocation mapLocation = mMapLocations.get(position);
        holder.setMapLocation(mapLocation);
    }

    @Override
    public int getItemCount() {
        return mMapLocations.size();
    }

    class TimesHolder extends ViewHolder implements OnMapReadyCallback {

        private TextView destination;
        private LinearLayout fromLayout, toLayout;
        private MapView mapView;

        void setCurrent(int current, int what) {
            switch (what) {
                case 0: //From
                    if (current < fromLayout.getChildCount()) {
                        if (current + 1 < fromLayout.getChildCount())
                            current++;
                        View targetView = fromLayout.getChildAt(current);
                        targetView.getParent().requestChildFocus(targetView, targetView);
                    }
                    break;
                case 1: //To
                    if (current < toLayout.getChildCount()) {
                        if (current + 1 < toLayout.getChildCount())
                            current++;
                        View targetView = toLayout.getChildAt(current);
                        targetView.getParent().requestChildFocus(targetView, targetView);
                    }
                    break;
            }
        }

        GoogleMap mGoogleMap;
        MapLocation mMapLocation;

        TimesHolder(View itemView) {
            super(itemView);
            fromLayout = itemView.findViewById(R.id.time_from);
            toLayout = itemView.findViewById(R.id.time_to);
            mapView = itemView.findViewById(R.id.map_view);
            destination = itemView.findViewById(R.id.bus_destination);

            mapView.onCreate(null);
            mapView.getMapAsync(this);
        }

        void setMapLocation(MapLocation mapLocation) {
            mMapLocation = mapLocation;

            // If the map is ready, update its content.
            if (mGoogleMap != null) {
                updateMapContents();
            }
        }

        @Override
        public void onMapReady(GoogleMap googleMap) {
            mGoogleMap = googleMap;

            MapsInitializer.initialize(context);
            googleMap.getUiSettings().setMapToolbarEnabled(false);

            // If we have map data, update the map content.
            if (mMapLocation != null) {
                updateMapContents();
            }
        }

        void updateMapContents() {
            // Since the mapView is re-used, need to remove pre-existing mapView features.
            mGoogleMap.clear();

            mapView.setVisibility(View.VISIBLE);

            // Update the mapView feature data and camera position.
            mGoogleMap.addMarker(new MarkerOptions().position(mMapLocation.getCenter()));

            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(mMapLocation.getCenter(), 10f);
            mGoogleMap.moveCamera(cameraUpdate);
        }

    }
}
