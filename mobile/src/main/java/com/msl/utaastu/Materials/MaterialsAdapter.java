package com.msl.utaastu.Materials;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.msl.utaastu.Application.MyApplication;
import com.msl.utaastu.R;
import com.msl.utaastu.Utils.Intents;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Malek Shefat on 7/6/2017.
 */

public class MaterialsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private LayoutInflater inflater;
    private List<MaterialItem> materialItems = new ArrayList<>();

    private ClickListener listener;

    private MaterialItem undoItem;
    private int undoPos;

    public MaterialsAdapter(Context context) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }

    public void setGroups(List<MaterialItem> groups) {
        this.materialItems = groups;
        notifyItemRangeChanged(0, groups.size());
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Item(inflater.inflate(R.layout.materials_recycler_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Item item = ((Item) holder);
        item.name.setText(materialItems.get(position).getName());
        item.desc.setText(materialItems.get(position).getDesc());
        item.link.setText(materialItems.get(position).getLink());
    }

    @Override
    public int getItemCount() {
        return materialItems.size();
    }

    private class Item extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView name, desc, link;
        private View menu_icon;

        public Item(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            name = itemView.findViewById(R.id.material_title);
            desc = itemView.findViewById(R.id.material_desc);
            link = itemView.findViewById(R.id.material_link);
            menu_icon = itemView.findViewById(R.id.menu_icon);
            menu_icon.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.menu_icon:
                    //creating a popup menu
                    PopupMenu popup = new PopupMenu(context, menu_icon);
                    //inflating menu from xml resource
                    popup.inflate(R.menu.recycler_item_menu);
                    //adding click listener
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.delete_group:
                                    //handle 'remove' click
                                    if (listener != null) {
                                        listener.onRemove(materialItems.get(getAdapterPosition()).getName());
                                        removeItem(getAdapterPosition(), menu_icon);
                                    }
                                    break;
                                case R.id.report:
                                    //handle 'report' click
                                    String email = "abdulmalek.s.a.shefat@gmail.com";
                                    String subject ="Report: UTAA-STU Material ("
                                            + materialItems.get(getAdapterPosition()).getName() + ")";
                                    context.startActivity(Intents.sendEmail(email, subject, null));
                                    break;
                                case R.id.share:
                                    //handle 'report' click
                                    String message = materialItems.get(getAdapterPosition()).getLink();
                                    String title ="Share: \'"
                                            + materialItems.get(getAdapterPosition()).getName() + "\"";
                                    context.startActivity(Intents.shareLink(title, message));
                                    break;
                            }
                            return false;
                        }
                    });
                    //displaying the popup
                    if (!materialItems.get(getAdapterPosition()).getId().equalsIgnoreCase(MyApplication.getUser().getUid())) {
                        popup.getMenu().findItem(R.id.delete_group).setVisible(false);
                    }
                    popup.show();
                    break;
                default:
                    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(materialItems.get(getAdapterPosition()).getLink())));
            }
        }
    }

    public void setListener(ClickListener listener) {
        this.listener = listener;
    }

    public interface ClickListener {
        void onRemove(String key);

        void onAdd(MaterialItem item);
    }

    public void removeItem(int pos, View v) {
        if (pos < 0)
            return;
        undoPos = pos;
        undoItem = materialItems.get(pos);
        materialItems.remove(pos);
        notifyItemRemoved(pos);
        Snackbar.make(v, R.string.item_removed, Snackbar.LENGTH_LONG)
                .setAction(R.string.undo, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        materialItems.add(undoPos, undoItem);
                        notifyItemInserted(undoPos);
                        listener.onAdd(undoItem);
                    }
                }).show();
    }
}
