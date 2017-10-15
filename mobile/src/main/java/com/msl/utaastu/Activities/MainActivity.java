package com.msl.utaastu.Activities;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.common.annotation.KeepForSdk;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.msl.utaastu.AcademicCalendar.AcademicCalendarActivity;
import com.msl.utaastu.Application.MyApplication;
import com.msl.utaastu.BusSchedule.BusFragment;
import com.msl.utaastu.Contacts.ContactsBottomSheet;
import com.msl.utaastu.CourseSchedule.ScheduleFragment;
import com.msl.utaastu.Drawer.DrawerAdapter;
import com.msl.utaastu.Drawer.DrawerItem;
import com.msl.utaastu.Drawer.SimpleItem;
import com.msl.utaastu.Drawer.SpaceItem;
import com.msl.utaastu.Exam.ExamBottomSheet;
import com.msl.utaastu.Food.FoodFragment;
import com.msl.utaastu.GPACalculator.GPACalculatorFragment;
import com.msl.utaastu.Interfaces.OnBackPressedListener;
import com.msl.utaastu.Intro.Intro;
import com.msl.utaastu.Materials.MaterialsBottomSheet;
import com.msl.utaastu.Notifications.NotificationsActivity;
import com.msl.utaastu.R;
import com.msl.utaastu.Social.SocialBottomSheet;
import com.msl.utaastu.UserData.ProfileDialog;
import com.msl.utaastu.UserData.UserData;
import com.vansuita.materialabout.views.CircleImageView;
import com.yarolegovich.slidingrootnav.SlidingRootNav;
import com.yarolegovich.slidingrootnav.SlidingRootNavBuilder;
import com.yarolegovich.slidingrootnav.callback.DragListener;

import java.util.Arrays;

import static com.msl.utaastu.Firebase.FirebaseConstants.DEPARTMENTS_NODE;
import static com.msl.utaastu.Firebase.FirebaseConstants.USERS_NODE;
import static com.msl.utaastu.Utils.ImageUtils.base64StringToBitmap;

public class MainActivity extends AppCompatActivity implements DrawerAdapter.OnItemSelectedListener,
        BottomNavigationView.OnNavigationItemSelectedListener,
        BottomNavigationView.OnNavigationItemReselectedListener, View.OnClickListener, DragListener {

    /*Shortcuts intent*/
    private final String BUS_TIMES = "android.intent.action.SERVICE_TIMES";
    private final String FOOD_TABLE = "android.intent.action.FOOD_TABLE";
    private final String GPA_CALC = "android.intent.action.GPA_CALCULATIONS";

    /*Drawer starts*/
    private final int SOCIAL_GROUPS = 0;
    private final int STUDYING_MATERIALS = 1;
    private final int EXAM_TABLE = 2;
    private final int ACADEMIC_CALENDAR = 3;
    private final int UTAA_CONTACTS = 4;
    private final int NOTIFICATIONS = 6;
    private final int ABOUT = 7;
    private final int SETTINGS = 8;

    private String[] screenTitles;
    private int[] screenIcons;

    private CircleImageView profileImage;
    private TextView user_name;

    /*Drawer Ends*/

    private FirebaseUser user = MyApplication.getUser();
    private FirebaseDatabase database = MyApplication.getDatabase();
    private DatabaseReference databaseReference = database.getReference();

    private SlidingRootNav navMenu;
    private BottomNavigationView bottomNavigationView;
    private Toolbar toolbar;

    private OnBackPressedListener onBackPressedListener;

    private UserData userData;
    private String image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startIntro();

        MobileAds.initialize(this, getString(R.string.ads_app_id));

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        bottomNavigationView.setOnNavigationItemReselectedListener(this);
        String action = getIntent().getAction();
        if (action != null) {
            switch (action) {
                case BUS_TIMES:
                    bottomNavigationView.setSelectedItemId(R.id.service_times);
                    showFragment(new BusFragment(), false, "Bus");
                    break;
                case FOOD_TABLE:
                    bottomNavigationView.setSelectedItemId(R.id.food_table);
                    showFragment(new FoodFragment(), false, "Food");
                    break;
                case GPA_CALC:
                    bottomNavigationView.setSelectedItemId(R.id.gpa_calc);
                    showFragment(new GPACalculatorFragment(), false, "Gpa");
                    break;
                default:
                    bottomNavigationView.setSelectedItemId(R.id.course_table);
                    showFragment(new ScheduleFragment(), true, "Schedule");
            }
        } else {
            bottomNavigationView.setSelectedItemId(R.id.course_table);
            showFragment(new ScheduleFragment(), true, "Schedule");
        }
        setTitle(R.string.course_table);

        setNavMenu(savedInstanceState);
    }

    public void setOnBackPressedListener(OnBackPressedListener onBackPressedListener) {
        this.onBackPressedListener = onBackPressedListener;
    }

    public void setNavMenu(Bundle savedInstanceState) {
        navMenu = new SlidingRootNavBuilder(this)
                .withToolbarMenuToggle(toolbar)
                .withMenuOpened(false)
                .withSavedState(savedInstanceState)
                .withMenuLayout(R.layout.navigation_menu_main)
                .addDragListener(this)
                .inject();

        screenIcons = loadScreenIcons();
        screenTitles = loadScreenTitles();

        DrawerAdapter adapter = new DrawerAdapter(Arrays.asList(
                createItemFor(SOCIAL_GROUPS),
                createItemFor(STUDYING_MATERIALS),
                createItemFor(EXAM_TABLE),
                createItemFor(ACADEMIC_CALENDAR),
                createItemFor(UTAA_CONTACTS),
                new SpaceItem(24),
                createItemFor(NOTIFICATIONS),
                createItemFor(ABOUT),
                createItemFor(SETTINGS),
                new SpaceItem(24)));
        adapter.setListener(this);

        RecyclerView list = findViewById(R.id.list);
        list.setNestedScrollingEnabled(false);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);

        profileImage = findViewById(R.id.profile_image);
        profileImage.setOnClickListener(this);
        user_name = findViewById(R.id.user_name);
        if (user != null) {
            getUserData();
        }
    }

    private void getUserData() {
        DatabaseReference child = databaseReference.child(USERS_NODE).child(user.getUid());
        child.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                userData = dataSnapshot.getValue(UserData.class);

                if (userData != null) {
                    image = userData.getPhoto();
                    user_name.setText(userData.getName());
                }
                if (user_name.getText().equals("")) {
                    user_name.setText(TextUtils.isEmpty(user.getDisplayName()) || user.getDisplayName() == null ? user.getEmail() : user.getDisplayName());
                }
                try {
                    profileImage.setImageBitmap(base64StringToBitmap(image));
                } catch (Exception e) {
                    profileImage.setImageResource(R.mipmap.profile_picture);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                profileImage.setImageResource(R.mipmap.profile_picture);
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (!navMenu.isMenuHidden())
            navMenu.closeMenu(true);
        else if (onBackPressedListener != null)
            onBackPressedListener.doBack();
        else
            super.onBackPressed();
    }

    @Override
    public void onItemSelected(int position) {
        navMenu.closeMenu();
        switch (position) {
            case EXAM_TABLE:
                String department = MyApplication.readDepartment();
                if (department.equalsIgnoreCase(getString(R.string.selectDepartment))) {
                    Toast.makeText(this, R.string.choose_department, Toast.LENGTH_LONG).show();
                    ProfileDialog profileDialog = new ProfileDialog();
                    profileDialog.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
                    profileDialog.show(this.getSupportFragmentManager(), "profile");
                } else {
                    ExamBottomSheet examBottomSheet = new ExamBottomSheet();
                    examBottomSheet.show(getSupportFragmentManager(), "exams");
                }
                break;
            case SOCIAL_GROUPS:
                SocialBottomSheet socialBottomSheet = new SocialBottomSheet();
                socialBottomSheet.show(getSupportFragmentManager(), "social");
                break;
            case STUDYING_MATERIALS:
                MaterialsBottomSheet materialsBottomSheet = new MaterialsBottomSheet();
                materialsBottomSheet.show(getSupportFragmentManager(), "materials");
                break;
            case UTAA_CONTACTS:
                ContactsBottomSheet contactsBottomSheet = new ContactsBottomSheet();
                contactsBottomSheet.show(getSupportFragmentManager(), "contacts");
                break;
            case ABOUT:
                startActivity(new Intent(this, AboutActivity.class));
                break;
            case SETTINGS:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case NOTIFICATIONS:
                startActivity(new Intent(this, NotificationsActivity.class));
                break;
            case ACADEMIC_CALENDAR:
                startActivity(new Intent(this, AcademicCalendarActivity.class));
                break;
        }
    }

    private void showFragment(Fragment fragment, boolean first, String tag) {
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction();
        if (!first)
            transaction.setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_down);
        getSupportFragmentManager().executePendingTransactions();
        transaction.replace(R.id.container, fragment, tag)
                .commit();
    }

    private DrawerItem createItemFor(int position) {
        return new SimpleItem(screenIcons[position], screenTitles[position])
                .withIconTint(color(R.color.color_primary))
                .withTextTint(color(R.color.color_primary_dark))
                .withSelectedIconTint(color(R.color.color_primary))
                .withSelectedTextTint(color(R.color.color_primary_dark));
    }

    private String[] loadScreenTitles() {
        return getResources().getStringArray(R.array.ld_activityScreenTitles);
    }

    private int[] loadScreenIcons() {
        TypedArray ta = getResources().obtainTypedArray(R.array.ld_activityScreenIcons);
        int[] icons = new int[ta.length()];
        for (int i = 0; i < ta.length(); i++) {
            icons[i] = ta.getResourceId(i, 0);
        }
        ta.recycle();
        return icons;
    }

    @ColorInt
    private int color(@ColorRes int res) {
        return ContextCompat.getColor(this, res);
    }

    private void startIntro() {
        if (user == null) {
            finish();
            startActivity(new Intent(this, Intro.class));
        } else {
            getDepartments();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestIgnoreButteryOptimization();
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (onBackPressedListener != null) {
            onBackPressedListener.doBack();
            return false;
        }
        switch (item.getItemId()) {
            case R.id.course_table:
                showFragment(new ScheduleFragment(), false, "Schedule");
                animateTitleChange(R.string.course_table);
                break;
            case R.id.service_times:
                showFragment(new BusFragment(), false, "Bus");
                animateTitleChange(R.string.service_times);
                break;
            case R.id.food_table:
                showFragment(new FoodFragment(), false, "Food");
                animateTitleChange(R.string.food_table);
                break;
            case R.id.gpa_calc:
                showFragment(new GPACalculatorFragment(), false, "Gpa");
                animateTitleChange(R.string.gpa_calc);
                break;
        }
        return true;
    }

    @Override
    public void onNavigationItemReselected(@NonNull MenuItem item) {

    }

    public void setProfileData(String name) {
        user_name.setText(!TextUtils.isEmpty(name) ? name : user.getEmail());
        if (MyApplication.getProfileImage() != null)
            profileImage.setImageBitmap(MyApplication.getProfileImage());
        else
            profileImage.setImageResource(R.mipmap.profile_picture);
    }

    public Toolbar getToolbar() {
        return toolbar;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.profile_image:
                ProfileDialog profileDialog = new ProfileDialog();
                profileDialog.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
                profileDialog.show(getSupportFragmentManager(), "profile");
                break;
        }
    }

    public void setButtonBarItemsEnabled(boolean enabled) {
        for (int i = 0; i < bottomNavigationView.getMenu().size(); i++)
            bottomNavigationView.getMenu().getItem(i).setEnabled(enabled);
    }

    private void animateTitleChange(final int newTitle) {
        final View view = getToolbarTitle();

        if (view instanceof TextView) {
            AlphaAnimation fadeOut = new AlphaAnimation(1f, 0f);
            fadeOut.setDuration(250);
            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    setTitle(newTitle);

                    AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
                    fadeIn.setDuration(250);
                    view.startAnimation(fadeIn);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            view.startAnimation(fadeOut);
        }
    }

    private View getToolbarTitle() {
        int childCount = toolbar.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = toolbar.getChildAt(i);
            if (child instanceof TextView) {
                return child;
            }
        }

        return new View(this);
    }

    @Override
    public void onDrag(float progress) {
        (findViewById(R.id.overlay)).setAlpha(progress);
        (findViewById(R.id.overlay)).setVisibility(progress == 0 ? View.GONE : View.VISIBLE);
    }

    private void getDepartments() {
        database.getReference(DEPARTMENTS_NODE).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String departments = dataSnapshot.getValue(String.class);
                String[] departments_list = departments.split(", ");
                if (departments_list.length > 0)
                    MyApplication.getWritableDepartmentsDatabase().deleteAllData();
                for (String department : departments_list) {
                    MyApplication.getWritableDepartmentsDatabase().addString(department);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestIgnoreButteryOptimization() {
        String packageName = getPackageName();
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).setData(Uri.parse("package:" + packageName));
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException ignored) {
            }
        }
    }
}