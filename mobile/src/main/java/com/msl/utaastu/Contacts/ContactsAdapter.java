package com.msl.utaastu.Contacts;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.msl.utaastu.R;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.CLIPBOARD_SERVICE;

/**
 * Created by Malek Shefat on 7/17/2017.
 */

class ContactsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int TITLE = 0;
    private final int PHONE = 1;
    private final int FAX = 2;
    private final int EMAIL = 3;
    private final int WEBSITE = 4;

    private Context c;
    private LayoutInflater inflater;

    private List<ContactItem> items = new ArrayList<>();

    public ContactsAdapter(Context c) {
        this.c = c;
        this.inflater = LayoutInflater.from(c);
    }

    public void setItems(List<ContactItem> items) {
        this.items = items;
        notifyItemRangeChanged(0, items.size());
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getType();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TITLE:
                return new TitleHolder(inflater.inflate(R.layout.contacts_recycler_header, parent, false));
            default:
                return new ItemHolder(inflater.inflate(R.layout.contacts_recycler_item, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof TitleHolder) {
            ((TitleHolder) holder).title.setText(items.get(position).getTitle());
        } else {
            ((ItemHolder) holder).content.setText(items.get(position).getContent());
            ((ItemHolder) holder).label.setText(items.get(position).getLabel());
            ((ItemHolder) holder).setIcon(items.get(position).getType());
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

    private class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        private ImageView icon;
        private EditText label;
        private TextView content;

        public ItemHolder(View itemView) {
            super(itemView);
            icon = (ImageView) itemView.findViewById(R.id.icon);
            label = (EditText) itemView.findViewById(R.id.label);
            content = (TextView) itemView.findViewById(R.id.content);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        private void setIcon(int type) {
            int res;
            switch (type) {
                case PHONE:
                    res = R.drawable.ic_phone;
                    break;
                case FAX:
                    res = R.drawable.ic_fax;
                    break;
                case EMAIL:
                    res = R.drawable.ic_email;
                    break;
                case WEBSITE:
                    res = R.drawable.ic_web;
                    break;
                default:
                    res = R.drawable.ic_utaa_contacts;
                    break;
            }
            icon.setImageResource(res);
        }

        @Override
        public void onClick(View v) {
            String email = items.get(getAdapterPosition()).getContent();
            switch (items.get(getAdapterPosition()).getType()) {
                case PHONE:
                    Intent phoneIntent = new Intent(Intent.ACTION_DIAL, Uri.fromParts(
                            "tel", email, null));
                    c.startActivity(phoneIntent);
                    break;
                case EMAIL:
                    c.startActivity(com.msl.utaastu.Utils.Intents.sendEmail(email, null, null));
                    break;
                case WEBSITE:
                    Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(email));
                    c.startActivity(webIntent);
                    break;
                default:
                    break;
            }
        }

        @Override
        public boolean onLongClick(View v) {
            ClipboardManager clipboard = (ClipboardManager) c.getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText(c.getString(R.string.app_name), items.get(getAdapterPosition()).getContent());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(c, R.string.content_copied, Toast.LENGTH_LONG).show();
            return true;
        }
    }
}
