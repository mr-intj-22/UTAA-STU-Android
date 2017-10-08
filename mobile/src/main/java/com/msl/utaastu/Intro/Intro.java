package com.msl.utaastu.Intro;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.msl.utaastu.Activities.MainActivity;

import agency.tango.materialintroscreen.MaterialIntroActivity;

/**
 * Created by Malek Shefat on 6/13/2017.
 */

public class Intro extends MaterialIntroActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addSlide(CustomIntro.CustomIntro(1));
        addSlide(CustomIntro.CustomIntro(2));
        addSlide(CustomIntro.CustomIntro(3));
        addSlide(CustomIntro.CustomIntro(4));
        addSlide(CustomIntro.CustomIntro(5));
        addSlide(CustomIntro.CustomIntro(6));
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            addSlide(CustomIntro.CustomIntro(7));
        addSlide(new SignIn());

    }

    @Override
    public void onFinish() {
        super.onFinish();
        finish();
        startActivity(new Intent(this, MainActivity.class));
    }
}
