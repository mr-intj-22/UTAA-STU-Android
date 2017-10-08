package com.msl.utaastu.Materials;

import android.app.Dialog;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.msl.utaastu.Application.MyApplication;
import com.msl.utaastu.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.msl.utaastu.Firebase.FirebaseConstants.MATERIALS_KEY;

/**
 * Created by Malek Shefat on 6/15/2017.
 */

public class MaterialsBottomSheet extends BottomSheetDialogFragment implements Toolbar.OnMenuItemClickListener, ValueEventListener, MaterialsAdapter.ClickListener, View.OnClickListener {

    private FirebaseUser user = MyApplication.getUser();
    private FirebaseDatabase database = MyApplication.getDatabase();
    private DatabaseReference materials;
    private List<MaterialItem> groups = new ArrayList<>();
    private List<String> material_names = new ArrayList<>();

    private SocialAsync socialAsync;
    private View progressbar, empty;
    private Button add_group;
    private RecyclerView recyclerView;
    private MaterialsAdapter adapter;

    @Override
    public void setupDialog(Dialog dialog, int style) {
        View root = View.inflate(getContext(), R.layout.materials_bottom_sheet, null);
        dialog.setContentView(root);

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) root.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = params.getBehavior();

        if (behavior != null && behavior instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(mBottomSheetBehaviorCallback);
        }

        empty = root.findViewById(R.id.emptyView);
        add_group = root.findViewById(R.id.add_group);
        add_group.setOnClickListener(this);
        progressbar = root.findViewById(R.id.progressbar_horizontal);
        progressbar.setVisibility(View.VISIBLE);

        Toolbar toolbar = root.findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.studying_materials);
        toolbar.inflateMenu(R.menu.social_menu);
        toolbar.setOnMenuItemClickListener(this);

        recyclerView = root.findViewById(R.id.recyclerView);
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(manager);
        adapter = new MaterialsAdapter(getContext());
        adapter.setListener(this);

        materials = database.getReference(MATERIALS_KEY);
        materials.addListenerForSingleValueEvent(this);

    }

    private BottomSheetBehavior.BottomSheetCallback mBottomSheetBehaviorCallback = new BottomSheetBehavior.BottomSheetCallback() {

        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismiss();
            }
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
        }
    };

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add:
                add_group.callOnClick();
                break;
        }
        return true;
    }

    private void sortList() {
        Collections.sort(groups, new Comparator<MaterialItem>() {

            @Override
            public int compare(MaterialItem groupItem, MaterialItem groupItem2) {

                boolean b1 = groupItem.getId().equals(user.getUid());
                boolean b2 = groupItem2.getId().equals(user.getUid());

                if (b1 == b2) {
                    return 0;
                }
                // either b1 is true or b2
                // if true goes after false switch the -1 and 1
                return (b1 ? -1 : 1);
            }
        });
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        if (dataSnapshot.getChildrenCount() > 0 && dataSnapshot.exists()) {
            groups.clear();
            socialAsync = new SocialAsync();
            socialAsync.execute(dataSnapshot);
        } else {
            empty.setVisibility(View.VISIBLE);
            progressbar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        empty.setVisibility(View.VISIBLE);
        progressbar.setVisibility(View.GONE);
    }

    private void getData(DataSnapshot dataSnapshot) {
        for (DataSnapshot group : dataSnapshot.getChildren()) {    // get groups one by one...
            MaterialItem item = group.getValue(MaterialItem.class);
            groups.add(item);
            material_names.add(group.getKey());
        }
    }

    @Override
    public void onRemove(String key) {
        if (material_names.contains(key))
            material_names.remove(key);
        materials.child(key).removeValue();
        if (material_names.isEmpty())
            empty.setVisibility(View.VISIBLE);
    }

    @Override
    public void onAdd(MaterialItem item) {
        materials.child(item.getName()).setValue(item);
        empty.setVisibility(View.GONE);
        if (!material_names.contains(item.getName()))
            material_names.remove(item.getName());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_group:
                MaterialsBottomSheetAdd socialBottomSheet = new MaterialsBottomSheetAdd();
                socialBottomSheet.setTargetFragment(this, 0);
                socialBottomSheet.show(getActivity().getSupportFragmentManager(), "materials-add");
                break;
        }
    }

    public List<String> getMaterials_names() {
        return material_names;
    }

    private class SocialAsync extends AsyncTask<DataSnapshot, Void, Void> {

        @Override
        protected Void doInBackground(DataSnapshot... params) {
            getData(params[0]);
            sortList();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (groups.size() > 0) {
                recyclerView.setAdapter(adapter);
                adapter.setGroups(groups);
            } else {
                empty.setVisibility(View.VISIBLE);
            }
            progressbar.setVisibility(View.GONE);
        }
    }

    private void stopAsyncTasks() {
        if (socialAsync != null && socialAsync.getStatus() == AsyncTask.Status.RUNNING)
            socialAsync.cancel(true);
        socialAsync = null;
    }

    @Override
    public void onDestroy() {
        stopAsyncTasks();
        super.onDestroy();
    }

    public void update() {
        materials.addListenerForSingleValueEvent(this);
            empty.setVisibility(View.GONE);
    }
}
