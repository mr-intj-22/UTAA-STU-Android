package com.msl.utaastu.CourseSchedule;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.NativeExpressAdView;
import com.msl.utaastu.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Malek Shefat on 6/16/2017.
 */

public class ScheduleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int HEADER = 0;
    private final int DAY = 1;
    private final int COLORS = 2;
    private final int ITEM = 3;
    private final int SHADOW = 4;

    private List<ScheduleItem> scheduleItems = new ArrayList<>();
    private List<DayItem> dayItems = new ArrayList<>();
    private Context c;
    private LayoutInflater inflater;
    private int prev, prev_older, day;  //  day start from 1;
    private AdRequest request;

    public ScheduleAdapter(Context c) {
        this.c = c;
        inflater = LayoutInflater.from(c);
    }

    public void setData(List<ScheduleItem> scheduleItems, List<DayItem> dayItems) {
        this.scheduleItems = scheduleItems;
        this.dayItems = dayItems;
        notifyItemRangeChanged(0, scheduleItems.size() + dayItems.size() * 4);
    }

    @Override
    public int getItemViewType(int position) {
        if (position > 0)
            setDay(position);
        if (position == 0) {
            return HEADER;
        } else if (isDay(position)) {
            return DAY;
        } else if (isColors(position)) {
            return COLORS;
        } else if (isShadow(position)) {
            return SHADOW;
        }
        return ITEM;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case HEADER:
                return new HeaderHolder(inflater.inflate(R.layout.course_recycler_header, parent, false));
            case DAY:
                return new DayHolder(inflater.inflate(R.layout.course_item_day, parent, false));
            case COLORS:
                return new ColorsHolder(inflater.inflate(R.layout.course_item_colors, parent, false));
            case SHADOW:
                return new ShadowHolder(inflater.inflate(R.layout.course_item_shadow, parent, false));
            default:
                return new ItemHolder(inflater.inflate(R.layout.course_item, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderHolder) {   // here's our header
            final HeaderHolder headerHolder = (HeaderHolder) holder;
            if (request == null) {
                request = new AdRequest.Builder()
                        .build();
                headerHolder.nativeExpressAdView.loadAd(request);
            }
        } else if (holder instanceof DayHolder) {   // here's our day
            ((DayHolder) holder).day.setText(dayItems.get(day - 1).getDay());
        } else if (holder instanceof ColorsHolder) {  // here's our colorful header

        } else if (holder instanceof ItemHolder) {   // here's our item
            int pos = position - (day * 3);
            ((ItemHolder) holder).section.setText(scheduleItems.get(pos).getSection());
            ((ItemHolder) holder).name.setText(scheduleItems.get(pos).getName());
            ((ItemHolder) holder).code.setText(scheduleItems.get(pos).getCode());
            ((ItemHolder) holder).place.setText(scheduleItems.get(pos).getPlace());
            ((ItemHolder) holder).time.setText(scheduleItems.get(pos).getTime());
        } else if (holder instanceof ShadowHolder) {  // here's our shadow

        }
    }

    @Override
    public int getItemCount() {
        return 1 + scheduleItems.size() + dayItems.size() * 3;
    }

    // View Holders

    public class HeaderHolder extends RecyclerView.ViewHolder {

        private NativeExpressAdView nativeExpressAdView;
        private CardView cardView;

        public HeaderHolder(View itemView) {
            super(itemView);
            nativeExpressAdView = itemView.findViewById(R.id.native_ads);
            cardView = itemView.findViewById(R.id.ad_container);
            nativeExpressAdView.setAdListener(
                    new AdListener() {
                        @Override
                        public void onAdFailedToLoad(int i) {
                            super.onAdFailedToLoad(i);
                            cardView.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAdLoaded() {
                            super.onAdLoaded();
                            if (cardView.getVisibility() != View.VISIBLE)
                                cardView.setVisibility(View.VISIBLE);
                        }
                    });
        }
    }

    public class ItemHolder extends RecyclerView.ViewHolder {

        TextView code, name, place, section;
        TextView time;

        public ItemHolder(View itemView) {
            super(itemView);
            section = itemView.findViewById(R.id.lecture_section);
            name = itemView.findViewById(R.id.lecture_name);
            code = itemView.findViewById(R.id.lecture_code);
            place = itemView.findViewById(R.id.lecture_place);
            time = itemView.findViewById(R.id.lecture_time);
        }
    }

    private class DayHolder extends RecyclerView.ViewHolder {

        private TextView day;

        public DayHolder(View itemView) {
            super(itemView);
            day = itemView.findViewById(R.id.schedule_day);
        }
    }

    private class ShadowHolder extends RecyclerView.ViewHolder {

        public ShadowHolder(View itemView) {
            super(itemView);
        }
    }

    private class ColorsHolder extends RecyclerView.ViewHolder {

        public ColorsHolder(View itemView) {
            super(itemView);
        }
    }

    // Calculations

    private void setDay(int pos) {
        prev = 0;
        day = 0;
        boolean found = false;
        while (!found) {
            day++;
            found = (pos > ((day - 1) * 3 + prev) && pos <= (day * 3 + prev + dayItems.get(day - 1).getLectures()));
            prev_older = prev;
            prev += dayItems.get(day - 1).getLectures();
        }
    }

    private boolean isDay(int pos) {
        return pos == 1 || (day > 1 && pos == ((day - 1) * 3 + 1 + prev_older));
    }

    private boolean isColors(int pos) {
        return pos == 2 || (day > 1 && pos == ((day - 1) * 3 + 2 + prev_older));
    }

    private boolean isShadow(int pos) {
        return pos == (day * 3 + prev);
    }
}
