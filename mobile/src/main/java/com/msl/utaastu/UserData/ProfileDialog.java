package com.msl.utaastu.UserData;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatSpinner;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.msl.utaastu.Activities.MainActivity;
import com.msl.utaastu.Application.MyApplication;
import com.msl.utaastu.R;
import com.vansuita.materialabout.views.CircleImageView;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

import agency.tango.materialintroscreen.MaterialIntroActivity;

import static android.app.Activity.RESULT_OK;
import static com.msl.utaastu.Firebase.FirebaseConstants.USERS_NODE;
import static com.msl.utaastu.Utils.ImageUtils.base64StringToBitmap;
import static com.msl.utaastu.Utils.ImageUtils.bitmapToBase64String;

/**
 * Created by Malek Shefat on 6/15/2017.
 */

public class ProfileDialog extends DialogFragment implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private static final int PIC_CROP = 146;
    private final int RESULT_LOAD_IMG = 147;

    //Firebase
    private FirebaseUser user = MyApplication.getUser();
    private FirebaseDatabase database = MyApplication.getDatabase();
    private DatabaseReference databaseReference = database.getReference();

    private AppCompatEditText username, email, birthday, phone;
    private CircleImageView imageView;
    private String image = "";
    private View progress;
    private String department = MyApplication.readDepartment();
    private boolean started = false;
    List<String> data;

    private UserData userData;
    private AppCompatSpinner spinner;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogTheme);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.dialog_profile, container, false);

        data = MyApplication.getWritableDepartmentsDatabase().getAllStrings();
        data.add(0, getString(R.string.selectDepartment));

        progress = root.findViewById(R.id.progressbar_horizontal);
        View updateFab = root.findViewById(R.id.updateFab);
        View close = root.findViewById(R.id.dismiss);
        spinner = root.findViewById(R.id.spinner);
        spinner.setAdapter(getAdapter());
        spinner.setOnItemSelectedListener(this);
        int selection = data.indexOf(department);
        spinner.setSelection(selection);
        updateFab.setOnClickListener(this);
        close.setOnClickListener(this);
        username = root.findViewById(R.id.username);
        username.setText(user.getDisplayName() != null ? user.getDisplayName() : "");
        email = root.findViewById(R.id.email);
        email.setText(user.getEmail());
        birthday = root.findViewById(R.id.birthday);
        phone = root.findViewById(R.id.phone);
        imageView = root.findViewById(R.id.circleImageView);
        imageView.setOnClickListener(this);
        getUserData();

        return root;
    }

    private void pickImage() {
        Intent intent = new Intent();
        // call android default gallery
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        // ******** code for crop image
        intent.putExtra("crop", true);
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 196);
        intent.putExtra("outputY", 196);
        intent.putExtra("return-data", true);
        try {
            startActivityForResult(intent, RESULT_LOAD_IMG);

        } catch (ActivityNotFoundException e) {
            if (getActivity() != null)
                Toast.makeText(getActivity(), R.string.no_image_picked, Toast.LENGTH_SHORT).show();
        }
    }

    private void cropImage(Uri imageUri) {
        Intent cropIntent = new Intent(Intent.ACTION_EDIT);
        // indicate image type and Uri
        cropIntent.setDataAndType(imageUri, "image/*");
        // set crop properties here
        cropIntent.putExtra("crop", true);
        // indicate aspect of desired crop
        cropIntent.putExtra("aspectX", 1);
        cropIntent.putExtra("aspectY", 1);
        // indicate output X and Y
        cropIntent.putExtra("outputX", 196);
        cropIntent.putExtra("outputY", 196);
        // retrieve data on return
        cropIntent.putExtra("return-data", true);
        // start the activity - we handle returning in onActivityResult
        try {
            startActivityForResult(cropIntent, PIC_CROP);
        } catch (ActivityNotFoundException e) {
            // Do nothing for now
            if (imageUri != null) {
                InputStream imageStream = null;
                try {
                    imageStream = getActivity().getContentResolver().openInputStream(imageUri);
                } catch (FileNotFoundException error) {
                    error.printStackTrace();
                }
                Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                // Do something with the bitmap
                image = bitmapToBase64String(selectedImage);
                MyApplication.setProfileImage(selectedImage);

                imageView.setImageBitmap(base64StringToBitmap(image));
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) return;
        Bundle extras = data.getExtras();
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case RESULT_LOAD_IMG:

                    if (extras != null) {
                        Bitmap photo = extras.getParcelable("data");
                        if (photo == null) {
                            cropImage(data.getData());
                            return;
                        }
                        // Do something with the bitmap
                        image = bitmapToBase64String(photo);
                        MyApplication.setProfileImage(photo);

                        imageView.setImageBitmap(base64StringToBitmap(image));
                    } else {
                        InputStream imageStream = null;
                        try {
                            imageStream = getActivity().getContentResolver().openInputStream(data.getData());
                        } catch (FileNotFoundException error) {
                            error.printStackTrace();
                            Log.d("UTAA-3", "file error: " + error.getMessage());
                        }
                        Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                        // Do something with the bitmap
                        image = bitmapToBase64String(selectedImage);
                        MyApplication.setProfileImage(selectedImage);

                        imageView.setImageBitmap(base64StringToBitmap(image));
                    }

                    break;
                case PIC_CROP:
                    if (extras != null) {
                        Bitmap photo = extras.getParcelable("data");
                        if (photo == null) {
                            if (getActivity() != null)
                                Toast.makeText(getActivity(), R.string.not_supported, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        // Do something with the bitmap
                        image = bitmapToBase64String(photo);
                        MyApplication.setProfileImage(photo);

                        imageView.setImageBitmap(base64StringToBitmap(image));
                    }

                    break;
            }
        } else {
            if (getActivity() != null)
                Toast.makeText(getActivity(), R.string.no_image_picked, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStart() {
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            if (!started) {
                dialog.getWindow().setWindowAnimations(
                        R.style.DialogAnimation);
                started = !started;
            }
        }
        super.onStart();
    }

    private void updateProfile() {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(username.getText().toString())
                .setPhotoUri(Uri.parse(image))
                .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {


                        if (getActivity() instanceof MainActivity)
                            ((MainActivity) getActivity()).setProfileData(username.getText().toString());
                        updateUserData(task.isSuccessful());
                        if (getActivity() != null)
                            Toast.makeText(getActivity(), R.string.user_data_uploaded, Toast.LENGTH_LONG).show();
                        progress.setVisibility(View.GONE);
                    }
                });
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (getActivity() instanceof MaterialIntroActivity)
            ((MaterialIntroActivity) getActivity()).onFinish();
        super.onDismiss(dialog);
    }

    private ArrayAdapter<String> getAdapter() {
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item,
                data);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        return adapter;
    }

    private void updateUserData(boolean isSuccessful) {
        userData.setBirthday(birthday.getText().toString());
        if (isSuccessful) {
            userData.setName(username.getText().toString());
        }
        userData.setPhone(phone.getText().toString());
        userData.setPhoto(image);
        DatabaseReference users_ref = databaseReference.child(USERS_NODE).child(user.getUid());
        users_ref.setValue(userData);
    }

    private void getUserData() {
        DatabaseReference child = databaseReference.child(USERS_NODE).child(user.getUid());
        child.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userData = dataSnapshot.getValue(UserData.class);
                if (userData != null) {
                    birthday.setText(userData.getBirthday());
                    phone.setText(userData.getPhone());
                    department = userData.getDepartment();
                    MyApplication.storeDepartment(department);
                    int selection = data.indexOf(department);
                    spinner.setSelection(selection);
                    image = userData.getPhoto();
                }
                if (MyApplication.getProfileImage() != null) {
                    imageView.setImageBitmap(MyApplication.getProfileImage());
                } else if (image.equals("")) {
                    imageView.setImageResource(R.mipmap.profile_picture);
                } else {
                    try {
                        imageView.setImageBitmap(base64StringToBitmap(image));
                    } catch (Exception e) {
                        imageView.setImageResource(R.mipmap.profile_picture);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                imageView.setImageResource(R.mipmap.profile_picture);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.updateFab:
                if (validInputs()) {
                    progress.setVisibility(View.VISIBLE);
                    updateProfile();
                    dismiss();
                } else if (getActivity() != null) {
                    Toast.makeText(getActivity(), R.string.all_field_required, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.circleImageView:
                pickImage();
                break;
            case R.id.dismiss:
                dismiss();
                break;
        }
    }

    private boolean validInputs() {
        if (TextUtils.isEmpty(username.getText())) {
            username.setError(getString(R.string.fill_in));
            return false;
        } else if (TextUtils.isEmpty(birthday.getText())) {
            birthday.setError(getString(R.string.fill_in));
            return false;

        } else if (TextUtils.isEmpty(phone.getText())) {
            phone.setError(getString(R.string.fill_in));
            return false;
        }
        return true;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        department = data.get(position);
        userData.setDepartment(department);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

}
