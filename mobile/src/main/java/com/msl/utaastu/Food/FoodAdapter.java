package com.msl.utaastu.Food;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.msl.utaastu.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import static android.content.Context.CLIPBOARD_SERVICE;

/**
 * Created by Malek Shefat on 7/12/2017.
 */

public class FoodAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int HEADER = 0;
    private final int ITEM = 1;

    private LayoutInflater inflater;
    private Context context;
    private ArrayList<FoodItem> data = new ArrayList<>();
    private ArrayList<String> dates = new ArrayList<>();
    private int n;

    private SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
    private SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.US);

    public FoodAdapter(Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    public void setData(ArrayList<FoodItem> data, ArrayList<String> dates, int n) {
        this.data = data;
        this.dates = dates;
        this.n = n;
        notifyItemRangeChanged(0, data.size() + dates.size());
    }

    @Override
    public int getItemViewType(int position) {
        return (position % n) == (position / n) ? HEADER : ITEM;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case HEADER:
                return new HeaderHolder(inflater.inflate(R.layout.food_recycler_header, parent, false));
            default:
                return new ItemHolder(inflater.inflate(R.layout.food_recycler_item, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderHolder) {
            HeaderHolder headerHolder = ((HeaderHolder) holder);
            int pos = position / n;
            Calendar date = Calendar.getInstance();
            try {
                date.setTime(sdf.parse(dates.get(pos)));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            headerHolder.date.setText(context.getResources()
                    .getString(R.string.food_header, dayFormat.format(date.getTime()), dates.get(pos)));
        } else {
            ItemHolder itemHolder = ((ItemHolder) holder);
            int pos = position - position / (n + 1) - 1;
            itemHolder.cal.setText(String.valueOf(data.get(pos).getCal()));
            itemHolder.meal.setText(data.get(pos).getMeal());
        }
    }

    @Override
    public int getItemCount() {
        return data.size() + dates.size();
    }

    private class HeaderHolder extends RecyclerView.ViewHolder {
        private TextView date;

        public HeaderHolder(View itemView) {
            super(itemView);
            date = (TextView) itemView.findViewById(R.id.date);
        }
    }

    private class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView meal, cal;

        public ItemHolder(View itemView) {
            super(itemView);
            meal = (TextView) itemView.findViewById(R.id.meal);
            cal = (TextView) itemView.findViewById(R.id.cal);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int pos = getAdapterPosition() - getAdapterPosition() / (n + 1) - 1;
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText(context.getString(R.string.app_name), data.get(pos).getMeal());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(context, R.string.meal_copied, Toast.LENGTH_LONG).show();
        }
    }
}
