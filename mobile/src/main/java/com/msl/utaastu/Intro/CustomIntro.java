package com.msl.utaastu.Intro;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.msl.utaastu.R;
import com.msl.utaastu.Utils.InternetConnection;

import agency.tango.materialintroscreen.SlideFragment;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * Created by Malek Shefat on 6/13/2017.
 */

public class CustomIntro extends SlideFragment implements View.OnClickListener {

    private final int REQUEST_CODE_ASK_PERMISSION = 280;
    int current = 0;
    String[] permissions = new String[]{READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE};
    private String error;
    private View root;
    private Button button;
    private boolean permissions_granted = false;

    public static CustomIntro CustomIntro(int i) {
        CustomIntro intro = new CustomIntro();
        Bundle args = new Bundle();
        args.putInt("slide", i);
        intro.setArguments(args);
        return intro;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        current = getArguments().getInt("slide");
        root = inflater.inflate(R.layout.intro_custom_fragment, container, false);
        ImageView imageView = root.findViewById(R.id.image);
        imageView.setImageResource(getImg());
        if (current == 6)
            imageView.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.intro_permissions_background));
        else
            imageView.setBackgroundColor(ContextCompat.getColor(getActivity(), getPrimary()));
        TextView title = root.findViewById(R.id.title);
        title.setText(getString(getTitle()));
        title.setTextColor(ContextCompat.getColor(getActivity(), getPrimary()));
        TextView desc = root.findViewById(R.id.desc);
        desc.setText(getString(getDesc()));
        desc.setTextColor(ContextCompat.getColor(getActivity(), getAccent()));
        button = root.findViewById(R.id.signIn);
        if (current == 7) {
            button.setVisibility(View.VISIBLE);
            button.setText(getString(R.string.th6_button));
            button.setTextColor(ContextCompat.getColor(getActivity(), getPrimary()));
            button.setOnClickListener(this);
        }
        return root;
    }

    @Override
    public boolean canMoveFurther() {
        if (current == 7) {
            //return false if permission isn't granted
            error = getString(R.string.please_grant_permissions);
            if (!isDDMODEEnabled())
                askDDMODE();
            return permissions_granted && isDDMODEEnabled();
        }
        error = getString(R.string.check_connection);

        return InternetConnection.isNetworkAvailable(getActivity());
    }

    @Override
    public String cantMoveFurtherErrorMessage() {
        return error;
    }

    @Override
    public int backgroundColor() {
        switch (current) {
            case 1:
                return R.color.st_bg;
            case 2:
                return R.color.sec_bg;
            case 3:
                return R.color.rd_bg;
            case 4:
                return R.color.th4_bg;
            case 5:
                return R.color.th5_bg;
            case 6:
                return R.color.th6_bg;
            case 7:
                return R.color.th7_bg;
            default:
                return android.R.color.white;
        }
    }

    @Override
    public int buttonsColor() {
        return getAccent();
    }

    private int getPrimary() {
        switch (current) {
            case 1:
                return R.color.st_title;
            case 2:
                return R.color.sec_title;
            case 3:
                return R.color.rd_title;
            case 4:
                return R.color.th4_title;
            case 5:
                return R.color.th5_title;
            case 6:
                return R.color.th6_title;
            case 7:
                return R.color.th7_title;
            default:
                return R.color.color_primary_dark;
        }
    }

    private int getAccent() {
        switch (current) {
            case 1:
                return R.color.st_buttons;
            case 2:
                return R.color.sec_buttons;
            case 3:
                return R.color.rd_buttons;
            case 4:
                return R.color.th4_buttons;
            case 5:
                return R.color.th5_buttons;
            case 6:
                return R.color.th6_buttons;
            case 7:
                return R.color.th7_buttons;
            default:
                return R.color.color_accent;
        }
    }

    private void askPermissions() {
        if (ActivityCompat.checkSelfPermission(getActivity(), READ_EXTERNAL_STORAGE) != PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(getActivity(), WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
            requestPermissions(permissions, REQUEST_CODE_ASK_PERMISSION);
        } else {
            //  all granted
            button.setText(R.string.permissions_granted);
            button.setEnabled(false);
            permissions_granted = true;
        }
    }

    private int getTitle() {
        switch (current) {
            case 1:
                return R.string.st_title;
            case 2:
                return R.string.sec_title;
            case 3:
                return R.string.rd_title;
            case 4:
                return R.string.th4_title;
            case 5:
                return R.string.th5_title;
            case 6:
                return R.string.th6_title;
            case 7:
                return R.string.th7_title;
            default:
                return R.string.app_name;
        }
    }

    private int getDesc() {
        switch (current) {
            case 1:
                return R.string.st_desc;
            case 2:
                return R.string.sec_desc;
            case 3:
                return R.string.rd_desc;
            case 4:
                return R.string.th4_desc;
            case 5:
                return R.string.th5_desc;
            case 6:
                return R.string.th6_desc;
            case 7:
                return R.string.th7_desc;
            default:
                return R.string.app_name;
        }
    }

    private int getImg() {
        switch (current) {
            case 1:
                return R.drawable.intro_features;
            case 2:
                return R.drawable.intro_schedule;
            case 3:
                return R.drawable.intro_bus;
            case 4:
                return R.drawable.intro_gpa_calc;
            case 5:
                return R.drawable.intro_food;
            case 6:
                return R.drawable.intro_groups;
            case 7:
                return R.drawable.intro_permissions;
            default:
                return R.drawable.intro_signing;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.signIn:
                askPermissions();
        }
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        if (menuVisible) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && getActivity() != null) {
                Window window = getActivity().getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setNavigationBarColor(ContextCompat.getColor(getActivity(), getPrimary()));
            }
        }
        super.setMenuVisibility(menuVisible);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSION:
                int notGranted = 0;
                for (int i : grantResults) {
                    if (i != PERMISSION_GRANTED)
                        notGranted++;
                }
                if (notGranted > 0)
                    Snackbar.make(root, R.string.please_grant_permissions, Snackbar.LENGTH_SHORT)
                            .setAction(R.string.grant_permissions, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                }
                            })
                            .show();
                else {
                    //  all granted
                    button.setText(R.string.permissions_granted);
                    button.setEnabled(false);
                    permissions_granted = true;
                    if (!isDDMODEEnabled())
                        askDDMODE();
                }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void askDDMODE() {
        Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
        startActivity(intent);
    }

    private boolean isDDMODEEnabled() {
        NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || notificationManager.isNotificationPolicyAccessGranted();
    }
}
