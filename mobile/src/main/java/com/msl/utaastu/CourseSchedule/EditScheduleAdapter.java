package com.msl.utaastu.CourseSchedule;

import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.msl.utaastu.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Malek Shefat on 6/16/2017.
 */

public class EditScheduleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int HEADER = 0;
    private final int DAY = 1;
    private final int COLORS = 2;
    private final int ITEM = 3;
    private final int SHADOW = 4;
    private final int BUTTON = 5;

    private List<ScheduleItem> scheduleItems = new ArrayList<>();
    private List<DayItem> dayItems = new ArrayList<>();
    private Context c;
    private LayoutInflater inflater;
    private int prev, prev_older, day;  //  day start from 1;
    private ClickListener clickListener;

    private boolean edited = false;

    public EditScheduleAdapter(Context c) {
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
        } else if (isButton(position)) {
            return BUTTON;
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
            case BUTTON:
                return new ButtonHolder(inflater.inflate(R.layout.course_item_add, parent, false));
            default:
                return new ItemHolder(inflater.inflate(R.layout.course_item_edit, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderHolder) {   // here's our header

        } else if (holder instanceof DayHolder) {   // here's our day
            ((DayHolder) holder).day.setText(dayItems.get(day - 1).getDay());
        } else if (holder instanceof ColorsHolder) {  // here's our colorful header

        } else if (holder instanceof ItemHolder) {   // here's our item
            int pos = position - (day * 4 - 1);
            ((ItemHolder) holder).section.setText(scheduleItems.get(pos).getSection());
            ((ItemHolder) holder).name.setText(scheduleItems.get(pos).getName());
            ((ItemHolder) holder).code.setText(scheduleItems.get(pos).getCode());
            ((ItemHolder) holder).place.setText(scheduleItems.get(pos).getPlace());
            ((ItemHolder) holder).time.setText(scheduleItems.get(pos).getTime());
        } else if (holder instanceof ShadowHolder) {  // here's our shadow

        } else {  // here's our add button

        }
    }

    @Override
    public int getItemCount() {
        return 1 + scheduleItems.size() + dayItems.size() * 4;
    }

    // View Holders

    private class HeaderHolder extends RecyclerView.ViewHolder {

        View card;

        public HeaderHolder(View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.ad_container);
            card.setVisibility(View.GONE);
        }
    }

    public class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        AppCompatEditText code, name, place, section;
        EditText time;

        public ItemHolder(View itemView) {
            super(itemView);
            section = itemView.findViewById(R.id.lecture_section);
            name = itemView.findViewById(R.id.lecture_name);
            code = itemView.findViewById(R.id.lecture_code);
            place = itemView.findViewById(R.id.lecture_place);
            time = itemView.findViewById(R.id.lecture_time);
            section.setCustomSelectionActionModeCallback(new android.view.ActionMode.Callback() {
                @Override
                public boolean onCreateActionMode(android.view.ActionMode mode, Menu menu) {
                    return false;
                }

                @Override
                public boolean onPrepareActionMode(android.view.ActionMode mode, Menu menu) {
                    return false;
                }

                @Override
                public boolean onActionItemClicked(android.view.ActionMode mode, MenuItem item) {
                    return false;
                }

                @Override
                public void onDestroyActionMode(android.view.ActionMode mode) {

                }
            });
            name.setCustomSelectionActionModeCallback(new android.view.ActionMode.Callback() {
                @Override
                public boolean onCreateActionMode(android.view.ActionMode mode, Menu menu) {
                    return false;
                }

                @Override
                public boolean onPrepareActionMode(android.view.ActionMode mode, Menu menu) {
                    return false;
                }

                @Override
                public boolean onActionItemClicked(android.view.ActionMode mode, MenuItem item) {
                    return false;
                }

                @Override
                public void onDestroyActionMode(android.view.ActionMode mode) {

                }
            });
            code.setCustomSelectionActionModeCallback(new android.view.ActionMode.Callback() {
                @Override
                public boolean onCreateActionMode(android.view.ActionMode mode, Menu menu) {
                    return false;
                }

                @Override
                public boolean onPrepareActionMode(android.view.ActionMode mode, Menu menu) {
                    return false;
                }

                @Override
                public boolean onActionItemClicked(android.view.ActionMode mode, MenuItem item) {
                    return false;
                }

                @Override
                public void onDestroyActionMode(android.view.ActionMode mode) {

                }
            });
            place.setCustomSelectionActionModeCallback(new android.view.ActionMode.Callback() {
                @Override
                public boolean onCreateActionMode(android.view.ActionMode mode, Menu menu) {
                    return false;
                }

                @Override
                public boolean onPrepareActionMode(android.view.ActionMode mode, Menu menu) {
                    return false;
                }

                @Override
                public boolean onActionItemClicked(android.view.ActionMode mode, MenuItem item) {
                    return false;
                }

                @Override
                public void onDestroyActionMode(android.view.ActionMode mode) {

                }
            });
            section.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    //'.', '#', '$', '[', or ']'
                    if (s.toString().contains("#") || s.toString().contains("$") ||
                            s.toString().contains("[") || s.toString().contains("]") ||
                            s.toString().contains(".")) {
                        section.setText(section.getText().delete(start, start + count));
                        section.setSelection(section.getText().length() - 1);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                    int day = getDay(getAdapterPosition());
                    int pos = getAdapterPosition() - (day * 4 - 1);
                    if (!edited)
                        edited = true;
                    scheduleItems.get(pos).setSection(s.toString());
                }
            });
            name.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    //'#', '$', '[', or ']'
                    if (s.toString().contains("#") || s.toString().contains("$") ||
                            s.toString().contains("[") || s.toString().contains("]") ||
                            s.toString().contains(".")) {
                        name.setText(name.getText().delete(start, start + count));
                        name.setSelection(name.getText().length() - 1);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                    int day = getDay(getAdapterPosition());
                    int pos = getAdapterPosition() - (day * 4 - 1);
                    if (!edited)
                        edited = true;
                    scheduleItems.get(pos).setName(s.toString());
                }
            });
            code.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    //'#', '$', '[', or ']'
                    if (s.toString().contains("#") || s.toString().contains("$") ||
                            s.toString().contains("[") || s.toString().contains("]") ||
                            s.toString().contains(".")) {
                        code.setText(code.getText().delete(start, start + count));
                        code.setSelection(code.getText().length() - 1);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                    int day = getDay(getAdapterPosition());
                    int pos = getAdapterPosition() - (day * 4 - 1);
                    if (!edited)
                        edited = true;
                    scheduleItems.get(pos).setCode(s.toString());
                }
            });
            place.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    //'#', '$', '[', or ']'
                    if (s.toString().contains("#") || s.toString().contains("$") ||
                            s.toString().contains("[") || s.toString().contains("]") ||
                            s.toString().contains(".")) {
                        place.setText(place.getText().delete(start, start + count));
                        place.setSelection(place.getText().length() - 1);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                    int day = getDay(getAdapterPosition());
                    int pos = getAdapterPosition() - (day * 4 - 1);
                    if (!edited)
                        edited = true;
                    scheduleItems.get(pos).setPlace(s.toString());
                }
            });
            time.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    //'#', '$', '[', or ']'
                    if (s.toString().contains("#") || s.toString().contains("$") ||
                            s.toString().contains("[") || s.toString().contains("]") ||
                            s.toString().contains(".")) {
                        time.setText(time.getText().delete(start, start + count));
                        time.setSelection(time.getText().length() - 1);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                    int day = getDay(getAdapterPosition());
                    int pos = getAdapterPosition() - (day * 4 - 1);
                    if (!edited)
                        edited = true;
                    scheduleItems.get(pos).setTime(s.toString());
                }
            });
            time.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (clickListener != null)
                clickListener.onTimePicked((EditText) v);
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

    private class ButtonHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        Button add;

        public ButtonHolder(View itemView) {
            super(itemView);
            add = itemView.findViewById(R.id.add);
            add.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            addItem(getAdapterPosition());
        }
    }

    // Calculations

    private void setDay(int pos) {
        prev = 0;
        day = 0;
        boolean found = false;
        while (!found) {
            day++;
            found = (pos > ((day - 1) * 4 + prev) && pos <= (day * 4 + prev + dayItems.get(day - 1).getLectures()));
            prev_older = prev;
            prev += dayItems.get(day - 1).getLectures();
        }
    }

    private int getDay(int pos) {
        prev = 0;
        int day = 0;
        boolean found = false;
        while (!found) {
            day++;
            found = (pos > ((day - 1) * 4 + prev) && pos <= (day * 4 + prev + dayItems.get(day - 1).getLectures()));
            prev_older = prev;
            prev += dayItems.get(day - 1).getLectures();
        }
        return day;
    }

    private boolean isDay(int pos) {
        return pos == 1 || (day > 1 && pos == ((day - 1) * 4 + 1 + prev_older));
    }

    private boolean isColors(int pos) {
        return pos == 2 || (day > 1 && pos == ((day - 1) * 4 + 2 + prev_older));
    }

    private boolean isShadow(int pos) {
        return pos == (day * 4 - 1 + prev);
    }

    private boolean isButton(int pos) {
        return pos == (day * 4 + prev);
    }

    // clickListener

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public interface ClickListener {
        void onTimePicked(EditText time);
    }

    //  add / remove / drage

    private void addItem(int pos) {
        if (!edited)
            edited = true;
        int day = getDay(pos);
        int position = pos - (day * 4 - 1);
        scheduleItems.add(position - 1, new ScheduleItem());
        dayItems.get(day - 1).increaseLectures();
        notifyItemInserted(pos - 1);
    }

    public void removeItem(int pos, RecyclerView rv) {
        if (!edited)
            edited = true;
        int day = getDay(pos);
        int position = pos - (day * 4 - 1);
        if (position < 0)
            return;
        scheduleItems.remove(position);
        dayItems.get(day - 1).decreaseLectures();
        notifyItemRemoved(pos);
    }

    public void moveItem(int from, int to) {
        if (!edited)
            edited = true;
        if (getItemViewType(to) != ITEM)
            return;
        int dayFrom = getDay(from);
        int dayTo = getDay(to);
        if (dayFrom != dayTo) {
            dayItems.get(dayFrom - 1).decreaseLectures();
            dayItems.get(dayTo - 1).increaseLectures();
        }
        int positionFrom = from - (dayFrom * 4 - 1);
        int positionTo = to - (dayTo * 4 - 1);
        ScheduleItem item = scheduleItems.get(positionFrom);
        scheduleItems.remove(positionFrom);
        scheduleItems.add(positionTo, item);
        notifyItemMoved(from, to);
    }

    //  setters and getters

    public List<ScheduleItem> getScheduleItems() {
        return scheduleItems;
    }

    public List<DayItem> getDayItems() {
        return dayItems;
    }

    public boolean isEdited() {
        return edited;
    }

    public void setEdited(boolean edited) {
        this.edited = edited;
    }
}
