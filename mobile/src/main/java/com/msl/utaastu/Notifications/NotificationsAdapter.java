package com.msl.utaastu.Notifications;

import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.msl.utaastu.R;
import com.msl.utaastu.Utils.Formatter;
import com.msl.utaastu.Utils.UlTagHandler;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Malek Shefat on 7/14/2017.
 */

public class NotificationsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private LayoutInflater inflater;
    private ArrayList<NotificationItem> data = new ArrayList<>();

    private int pos = Integer.MAX_VALUE;
    private TextView bodyText;
    private CardView card;
    private boolean[] cards;
    private ClickListener listener;

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");

    public NotificationsAdapter(Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    public void setData(ArrayList<NotificationItem> data) {
        this.data = data;
        cards = new boolean[data.size()];
        notifyItemRangeChanged(0, data.size());
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new NotificationHolder(inflater.inflate(R.layout.notification_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        NotificationHolder notificationHolder = ((NotificationHolder) holder);
        notificationHolder.title.setText(data.get(position).getTitle());
        notificationHolder.body.setText(Html.fromHtml(data.get(position).getBody(), null, new UlTagHandler()));
        notificationHolder.topic.setText(data.get(position).getTopic());
        notificationHolder.menu_icon.setVisibility(data.get(position).isMine() ? View.VISIBLE : View.GONE);
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(sdf.parse(data.get(position).getDate()));
            notificationHolder.time.setText(Formatter.FormatDates(calendar));
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class NotificationHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private View menu_icon;
        private TextView title, body, topic, time;
        private CardView cardView;

        public NotificationHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            body = itemView.findViewById(R.id.body);
            topic = itemView.findViewById(R.id.topic);
            time = itemView.findViewById(R.id.time);
            cardView = itemView.findViewById(R.id.cardView);
            cardView.setOnClickListener(this);
            menu_icon = itemView.findViewById(R.id.menu_icon);
            menu_icon.setOnClickListener(this);
        }

        @Override
        public void onClick(final View v) {
            switch (v.getId()) {
                case R.id.cardView:
                    if (cards[getAdapterPosition()]) {
                        collapse((CardView) v, body, getAdapterPosition());
                    } else if (pos < cards.length) {
                        collapse(card, bodyText, pos);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                expand((CardView) v, body, getAdapterPosition());
                            }
                        }, 200);
                    } else {
                        expand((CardView) v, body, getAdapterPosition());
                    }
                    break;
                case R.id.menu_icon:
                    //creating a popup menu
                    PopupMenu popup = new PopupMenu(context, menu_icon);
                    //inflating menu from xml resource
                    popup.inflate(R.menu.recycler_item_menu);
                    popup.getMenu().findItem(R.id.report).setVisible(false);
                    //adding click listener
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.delete_group:
                                    //handle 'remove' click
                                    if (listener != null) {
                                        listener.onRemove();
                                        removeItem(getAdapterPosition());
                                    }
                                    break;
                            }
                            return false;
                        }
                    });
                    popup.show();
                    break;
            }
        }

        private void collapse(CardView cardView, TextView body, final int position) {
            card = cardView;
            bodyText = body;
            pos = Integer.MAX_VALUE;
            int dpValue = 8; // margin in dips
            float d = context.getResources().getDisplayMetrics().density;
            int margin = (int) (dpValue * d); // margin in pixels
            ValueAnimator varl = ValueAnimator.ofInt(margin, 0);
            varl.setDuration(150);
            varl.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                    layoutParams.setMargins((Integer) animation.getAnimatedValue(), (Integer) animation.getAnimatedValue()
                            , (Integer) animation.getAnimatedValue(), (Integer) animation.getAnimatedValue());
                    card.setLayoutParams(layoutParams);
                    if ((Integer) animation.getAnimatedValue() == 0) {
                        bodyText.setMaxLines(1);
                        bodyText.setEllipsize(TextUtils.TruncateAt.END);
                        cards[position] = false;
                    }
                }
            });
            varl.start();
        }

        private void expand(CardView cardView, TextView body, int position) {
            card = cardView;
            bodyText = body;
            pos = position;
            int dpValue = 8; // margin in dips
            float d = context.getResources().getDisplayMetrics().density;
            final int margin = (int) (dpValue * d); // margin in pixels
            ValueAnimator varl = ValueAnimator.ofInt(margin);
            varl.setDuration(150);
            varl.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                    layoutParams.setMargins((Integer) animation.getAnimatedValue(), (Integer) animation.getAnimatedValue()
                            / 2, (Integer) animation.getAnimatedValue(), (Integer) animation.getAnimatedValue() / 2);
                    card.setLayoutParams(layoutParams);
                    if ((Integer) animation.getAnimatedValue() == margin) {
                        bodyText.setMaxLines(Integer.MAX_VALUE);
                        bodyText.setEllipsize(null);
                        cards[getAdapterPosition()] = true;
                    }
                }
            });
            varl.start();
        }
    }

    public void setListener(ClickListener listener) {
        this.listener = listener;
    }

    public void removeItem(int pos) {
        if (pos < 0)
            return;
        data.remove(pos);
        notifyItemRemoved(pos);
    }

    public interface ClickListener {
        void onRemove();
    }
}
