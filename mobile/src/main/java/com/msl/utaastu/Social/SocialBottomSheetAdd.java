package com.msl.utaastu.Social;

import android.app.Dialog;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.msl.utaastu.Application.MyApplication;
import com.msl.utaastu.R;

import static android.text.TextUtils.isEmpty;
import static com.msl.utaastu.Firebase.FirebaseConstants.SOCIAL_KEY;
import static com.msl.utaastu.Utils.Validator.isValidUrl;

/**
 * Created by Malek Shefat on 6/15/2017.
 */

public class SocialBottomSheetAdd extends BottomSheetDialogFragment implements Toolbar.OnMenuItemClickListener, TextWatcher {

    private FirebaseUser user = MyApplication.getUser();
    private FirebaseDatabase database = MyApplication.getDatabase();
    private DatabaseReference social;

    private View progressbar;
    private AppCompatEditText name, desc, langs, link;

    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        View root = View.inflate(getContext(), R.layout.social_bottom_sheet_add, null);
        dialog.setContentView(root);

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) root.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = params.getBehavior();

        if (behavior != null && behavior instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(mBottomSheetBehaviorCallback);
        }

        progressbar = root.findViewById(R.id.progressbar_horizontal);

        name = root.findViewById(R.id.name);
        desc = root.findViewById(R.id.desc);
        langs = root.findViewById(R.id.langs);
        link = root.findViewById(R.id.link);
        //langs.addTextChangedListener(this);

        Toolbar toolbar = root.findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.social_groups_add);
        toolbar.inflateMenu(R.menu.social_add_menu);
        toolbar.setOnMenuItemClickListener(this);

        social = database.getReference(SOCIAL_KEY);

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
            case R.id.done:
                if (isValid()) {
                    progressbar.setVisibility(View.VISIBLE);
                    social.child(name.getText().toString())
                            .setValue(new SocialGroupItem().setName(name.getText().toString())
                                    .setDesc(desc.getText().toString())
                                    .setLanguage(langs.getText().toString())
                                    .setLink(link.getText().toString())
                                    .setId(user.getUid()));
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            progressbar.setVisibility(View.GONE);
                            ((SocialBottomSheet) getTargetFragment()).update();
                            dismiss();
                        }
                    }, 250);
                }
                break;
        }
        return false;
    }

    private boolean isValid() {
        if (isEmpty(name.getText())) {
            name.setError(getString(R.string.empty_field));
            return false;
        }
        if (isEmpty(desc.getText())) {
            desc.setError(getString(R.string.empty_field));
            return false;
        }
        if (isEmpty(langs.getText())) {
            langs.setError(getString(R.string.empty_field));
            return false;
        }
        if (isEmpty(link.getText())) {
            link.setError(getString(R.string.empty_field));
            return false;
        }
        if (!isValidUrl(link.getText().toString())) {
            link.setError(getString(R.string.invalid_link));
            return false;
        }

        return true;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        langs.setText(langs.getText().toString().replace(" ", ", "));
    }
}
