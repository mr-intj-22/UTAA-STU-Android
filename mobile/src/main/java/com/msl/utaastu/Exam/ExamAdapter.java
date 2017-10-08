package com.msl.utaastu.Exam;

import android.content.Context;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.msl.utaastu.R;

import java.util.ArrayList;

/**
 * Created by Malek Shefat on 6/30/2017.
 */

public class ExamAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int HEADER = 0;
    private final int TITLE = 1;
    private final int ITEM = 2;

    private LayoutInflater inflater;

    private ArrayList<ExamItem> items;
    private boolean editable;

    private ClickListener clickListener;

    public ExamAdapter(Context context) {
        this.inflater = LayoutInflater.from(context);
    }

    public void setData(boolean editable, ArrayList<ExamItem> items) {
        this.items = items;
        this.editable = editable;
        notifyItemRangeChanged(0, 2 + items.size());
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0)
            return HEADER;
        else if (position == 1)
            return TITLE;
        else return ITEM;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case HEADER:
                return new HeaderHolder(inflater.inflate(R.layout.exam_recycler_header, parent, false));
            case TITLE:
                return new TitleHolder(inflater.inflate(R.layout.exam_item_colors, parent, false));
            default:
                return new ItemHolder(inflater.inflate(R.layout.exam_list_item, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemHolder) {
            ItemHolder itemHolder = ((ItemHolder) holder);
            int pos = position - 2;
            itemHolder.code.setText(items.get(pos).getCode());
            itemHolder.date.setText(items.get(pos).getDate());
            itemHolder.time.setText(items.get(pos).getTime());
            itemHolder.place.setText(items.get(pos).getPlace());
            itemHolder.elective.setChecked(items.get(pos).isElective());
        }
    }

    @Override
    public int getItemCount() {
        return 2 + items.size();
    }

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    private class HeaderHolder extends RecyclerView.ViewHolder {

        public HeaderHolder(View itemView) {
            super(itemView);

        }

    }

    private class TitleHolder extends RecyclerView.ViewHolder {

        public TitleHolder(View itemView) {
            super(itemView);
        }
    }

    public class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

        private AppCompatEditText code, place;
        private EditText date, time;
        private AppCompatCheckBox elective;

        public ItemHolder(View itemView) {
            super(itemView);

            code = itemView.findViewById(R.id.code);
            code.setEnabled(editable);
            code.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    int pos = getAdapterPosition() - 2;
                    items.get(pos).setCode(s.toString());
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
            date = itemView.findViewById(R.id.date);
            date.setEnabled(editable);
            date.setOnClickListener(editable ? this : null);
            date.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    int pos = getAdapterPosition() - 2;
                    items.get(pos).setDate(s.toString());
                }
            });
            time = itemView.findViewById(R.id.time);
            time.setOnClickListener(editable ? this : null);
            time.setEnabled(editable);
            time.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    int pos = getAdapterPosition() - 2;
                    items.get(pos).setTime(s.toString());
                }
            });
            place = itemView.findViewById(R.id.place);
            place.setEnabled(editable);
            place.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    int pos = getAdapterPosition() - 2;
                    items.get(pos).setPlace(s.toString());
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
            elective = itemView.findViewById(R.id.elective);
            elective.setOnCheckedChangeListener(this);
            elective.setEnabled(editable);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.date:
                    clickListener.onDateClick(date);
                    break;
                case R.id.time:
                    clickListener.onTimeClick(time);
                    break;
            }
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switch (buttonView.getId()) {
                case R.id.elective:
                    int pos = getAdapterPosition() - 2;
                    if (pos < 0)
                        break;
                    items.get(pos).setElective(isChecked);
                    break;
            }
        }
    }

    public void moveItem(int from, int to) {
        int positionFrom = from - 2;
        int positionTo = to - 2;
        if (getItemViewType(to) != ITEM || positionFrom < 0 || positionTo < 0)
            return;
        ExamItem item = items.get(positionFrom);
        items.remove(positionFrom);
        items.add(positionTo, item);
        notifyItemMoved(from, to);
    }

    public interface ClickListener {
        void onDateClick(EditText dateText);

        void onTimeClick(EditText timeText);

    }
}
