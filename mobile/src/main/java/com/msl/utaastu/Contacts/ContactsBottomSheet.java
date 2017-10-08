package com.msl.utaastu.Contacts;

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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.msl.utaastu.Application.MyApplication;
import com.msl.utaastu.R;
import com.msl.utaastu.Utils.Intents;

import java.util.ArrayList;
import java.util.List;

import static com.msl.utaastu.Firebase.FirebaseConstants.CONTACTS_KEY;

/**
 * Created by Malek Shefat on 6/15/2017.
 */

public class ContactsBottomSheet extends BottomSheetDialogFragment implements Toolbar.OnMenuItemClickListener, ValueEventListener {

    private final int TITLE = 0;
    private final int PHONE = 1;
    private final int FAX = 2;
    private final int EMAIL = 3;
    private final int WEBSITE = 4;

    private FirebaseDatabase database = MyApplication.getDatabase();
    private DatabaseReference contacts;
    private List<ContactItem> contactItems = new ArrayList<>();

    private ContactsAsync contactsAsync;
    private View progressbar, empty;
    private RecyclerView recyclerView;
    private ContactsAdapter adapter;

    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        View root = View.inflate(getContext(), R.layout.contacts_bottom_sheet, null);
        dialog.setContentView(root);

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) root.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = params.getBehavior();

        if (behavior != null && behavior instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(mBottomSheetBehaviorCallback);
        }

        empty = root.findViewById(R.id.imageView);
        progressbar = root.findViewById(R.id.progressbar_horizontal);

        Toolbar toolbar = root.findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.contacts);
        toolbar.inflateMenu(R.menu.contacts_menu);
        toolbar.setOnMenuItemClickListener(this);

        recyclerView = root.findViewById(R.id.recyclerView);
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(manager);
        adapter = new ContactsAdapter(getContext());

        contacts = database.getReference(CONTACTS_KEY);
        contacts.addValueEventListener(this);

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
            case R.id.report:
                String email = "abdulmalek.s.a.shefat@gmail.com";
                String subject =  "Report: UTAA-STU Contacts";
                startActivity(Intents.sendEmail(email, subject, null));
                break;
        }
        return true;
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        if (dataSnapshot.getChildrenCount() > 0) {
            contactItems.clear();
            contactsAsync = new ContactsAsync();
            contactsAsync.execute(dataSnapshot);
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
            ContactItem item = group.getValue(ContactItem.class);
            setType(item);
            contactItems.add(item);
        }
    }

    private void setType(ContactItem item) {
        if (item.getTitle() != null)
            item.setType(TITLE);
        else {
            switch (item.getLabel()) {
                case "Phone":
                    item.setType(PHONE);
                    break;
                case "Fax":
                    item.setType(FAX);
                    break;
                case "Email":
                    item.setType(EMAIL);
                    break;
                default:
                    item.setType(WEBSITE);
                    break;
            }
        }
    }

    private class ContactsAsync extends AsyncTask<DataSnapshot, Void, Void> {

        @Override
        protected Void doInBackground(DataSnapshot... params) {
            getData(params[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (contactItems.size() > 0) {
                recyclerView.setAdapter(adapter);
                adapter.setItems(contactItems);
                empty.setVisibility(View.GONE);
            } else {
                empty.setVisibility(View.VISIBLE);
            }
            progressbar.setVisibility(View.GONE);
        }
    }

    private void stopAsyncTasks() {
        if (contactsAsync != null && contactsAsync.getStatus() == AsyncTask.Status.RUNNING)
            contactsAsync.cancel(true);
        contactsAsync = null;
    }

    @Override
    public void onDestroy() {
        stopAsyncTasks();
        super.onDestroy();
    }
}
