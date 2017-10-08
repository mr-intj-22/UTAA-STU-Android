package com.msl.utaastu.Interfaces;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

/**
 * Created by Malek Shefat on 6/18/2017.
 */

public class BackPressedListener implements OnBackPressedListener {
    private final FragmentActivity activity;

    public BackPressedListener(FragmentActivity activity) {
        this.activity = activity;
    }

    @Override
    public void doBack() {
        activity.getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }
}