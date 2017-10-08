package com.msl.utaastu.AcademicCalendar;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.msl.utaastu.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Malek Shefat on 7/16/2017.
 */

public class AcademicCalendarAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int TITLE = 0;

    private List<CalendarItem> items = new ArrayList<>();

    private LayoutInflater inflater;

    public AcademicCalendarAdapter(Context c) {
        this.inflater = LayoutInflater.from(c);

    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getType();
    }

    public void setData(List<CalendarItem> items) {
        this.items = items;
        notifyItemRangeChanged(0, items.size());
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TITLE:
                return new TitleHolder(inflater.inflate(R.layout.academic_calendar_title, parent, false));
            default:
                return new ItemHolder(inflater.inflate(R.layout.academic_calendar_item, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof TitleHolder) {
            TitleHolder titleHolder = ((TitleHolder) holder);
            titleHolder.title.setText(items.get(position).getTitle());
        } else {
            ItemHolder itemHolder = ((ItemHolder) holder);
            itemHolder.date.setText(items.get(position).getDate());
            itemHolder.event.setText(items.get(position).getEvent());
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private class TitleHolder extends RecyclerView.ViewHolder {

        private TextView title;

        public TitleHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title);
        }
    }

    private class ItemHolder extends RecyclerView.ViewHolder {

        private TextView date, event;

        public ItemHolder(View itemView) {
            super(itemView);
            date = (TextView) itemView.findViewById(R.id.date);
            event = (TextView) itemView.findViewById(R.id.event);
        }
    }
}
