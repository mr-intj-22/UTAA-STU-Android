package com.msl.utaastu.Intro;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatEditText;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.msl.utaastu.Application.MyApplication;
import com.msl.utaastu.R;
import com.msl.utaastu.UserData.ProfileDialog;

import agency.tango.materialintroscreen.MaterialIntroActivity;
import agency.tango.materialintroscreen.SlideFragment;

import static android.support.design.widget.Snackbar.LENGTH_LONG;
import static android.support.design.widget.Snackbar.LENGTH_SHORT;
import static com.msl.utaastu.Firebase.FirebaseConstants.DEPARTMENTS_NODE;

/**
 * Created by Malek Shefat on 6/20/2017.
 */

public class SignIn extends SlideFragment implements View.OnClickListener {

    private FirebaseAuth auth;
    private FirebaseDatabase database;

    private Button sign_in_button, sign_up_button;
    private AppCompatEditText email, pass;
    private ProgressDialog progressDialog;

    private String password = "";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.intro_sign_in, container, false);

        auth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(getActivity());
        email = root.findViewById(R.id.email);
        pass = root.findViewById(R.id.pass);
        TextView forgot_pass = root.findViewById(R.id.forgot_password);
        forgot_pass.setOnClickListener(this);
        sign_in_button = root.findViewById(R.id.signIn);
        sign_in_button.setOnClickListener(this);
        sign_up_button = root.findViewById(R.id.signUp);
        sign_up_button.setOnClickListener(this);

        return root;
    }

    private void setButtonsEnabled(boolean b) {
        sign_in_button.setEnabled(b);
        sign_up_button.setEnabled(b);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.forgot_password:
                resetPassword();
                break;
            case R.id.signIn:
                if (TextUtils.isEmpty(email.getText()) || TextUtils.isEmpty(pass.getText())) {
                    Snackbar.make(email, R.string.fill_in, LENGTH_SHORT).show();
                } else {
                    signIn();
                }
                break;
            case R.id.signUp:
                if (TextUtils.isEmpty(email.getText()) || TextUtils.isEmpty(pass.getText())) {
                    Snackbar.make(email, R.string.fill_in, LENGTH_SHORT).show();
                } else {
                    password = pass.getText().toString();
                    reenterPassword();
                }
                break;
        }
    }

    private void register() {
        setButtonsEnabled(false);
        progressDialog.setMessage(getString(R.string.wait));
        progressDialog.show();
        auth.createUserWithEmailAndPassword(email.getText().toString(), pass.getText().toString()).
                addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Snackbar.make(email, getString(R.string.registration_complete), LENGTH_SHORT).show();
                            database = FirebaseDatabase.getInstance();
                            sign_in_button.setEnabled(false);
                            getDepartments();
                        } else {
                            setButtonsEnabled(true);
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthWeakPasswordException e) {
                                pass.setError(getString(R.string.password_weak));
                                pass.requestFocus();
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                email.setError(getString(R.string.invalid_email));
                                email.requestFocus();
                            } catch (FirebaseAuthUserCollisionException e) {
                                email.setError(getString(R.string.already_registered));
                                email.requestFocus();
                                Snackbar.make(email, R.string.already_registered, LENGTH_SHORT)
                                        .setAction(R.string.sign_in, new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                signIn();
                                            }
                                        }).show();
                            } catch (Exception e) {
                                Snackbar.make(email, R.string.error_registering, LENGTH_LONG).show();
                            }
                        }
                        progressDialog.hide();
                    }
                });
    }

    private void signIn() {
        setButtonsEnabled(false);
        progressDialog.setMessage(getString(R.string.wait));
        progressDialog.show();
        auth.signInWithEmailAndPassword(email.getText().toString(), pass.getText().toString()).
                addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Snackbar.make(email, getString(R.string.sign_in_complete), LENGTH_SHORT).show();
                            database = FirebaseDatabase.getInstance();
                            sign_in_button.setEnabled(false);
                            FirebaseMessaging.getInstance().subscribeToTopic("general-notifications");
                            FirebaseMessaging.getInstance().subscribeToTopic(auth.getCurrentUser().getUid());
                            getDepartments();
                        } else {
                            setButtonsEnabled(true);
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthInvalidCredentialsException e) {   //wrong email or pass
                                Snackbar.make(email, R.string.wrong_info, LENGTH_SHORT)
                                        .setAction(R.string.reset_pass, new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                resetPassword();
                                            }
                                        }).show();
                            } catch (FirebaseAuthInvalidUserException e) {  //user not registered
                                Snackbar.make(email, R.string.not_registered, LENGTH_SHORT)
                                        .setAction(R.string.register, new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                register();
                                            }
                                        }).show();
                            } catch (FirebaseTooManyRequestsException e) {  //too many requests
                                Snackbar.make(email, R.string.request_blocked, LENGTH_SHORT).show();
                            } catch (Exception e) {
                                Snackbar.make(email, R.string.error_sign_in, LENGTH_LONG).show();
                            }
                        }
                        progressDialog.hide();
                    }
                });
    }

    private void resetPassword() {
        setButtonsEnabled(false);
        progressDialog.setMessage(getString(R.string.wait));
        progressDialog.show();
        FirebaseAuth.getInstance().sendPasswordResetEmail(email.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Snackbar.make(email, R.string.reset_email_sent, LENGTH_SHORT).show();
                            setButtonsEnabled(true);
                        } else {
                            Snackbar.make(email, R.string.reset_email_error, LENGTH_SHORT)
                                    .setAction(R.string.try_again, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            resetPassword();
                                        }
                                    })
                                    .show();
                        }
                    }
                });
        progressDialog.hide();
    }

    private void reenterPassword() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_password, null);
        dialogBuilder.setView(dialogView);

        final AppCompatEditText editText = dialogView.findViewById(R.id.pass_conform);

        dialogBuilder.setTitle(R.string.password_title);
        dialogBuilder.setPositiveButton(R.string.sign_up, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //do something with edt.getText().toString();
                if (editText.getText().toString().equals(password)) {
                    register();
                } else {
                    Toast.makeText(getActivity(), R.string.password_no_match, Toast.LENGTH_LONG).show();
                }
            }
        });
        dialogBuilder.setNegativeButton(R.string.discard_changes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //pass
            }
        });
        dialogBuilder.create().show();

    }

    @Override
    public boolean canMoveFurther() {
        return auth.getCurrentUser() != null && MyApplication.getWritableDepartmentsDatabase().getAllStrings().size() > 0;
    }

    @Override
    public String cantMoveFurtherErrorMessage() {
        return getString(R.string.not_signed);
    }

    @Override
    public int backgroundColor() {
        return R.color.signIn_bg;
    }

    @Override
    public int buttonsColor() {
        return R.color.signIn_buttons;
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        if (menuVisible) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && getActivity() != null) {
                Window window = getActivity().getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setNavigationBarColor(ContextCompat.getColor(getActivity(), R.color.signIn_title));
            }
        }
        super.setMenuVisibility(menuVisible);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void showProfileDialog() {
        ProfileDialog profileDialog = new ProfileDialog();
        profileDialog.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        profileDialog.show(getActivity().getSupportFragmentManager(), "profile");
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
                if (auth.getCurrentUser() != null && (auth.getCurrentUser().getDisplayName() == null)
                        || TextUtils.isEmpty(auth.getCurrentUser().getDisplayName())
                        || auth.getCurrentUser().getPhotoUrl() == null
                        || TextUtils.isEmpty(auth.getCurrentUser().getPhotoUrl().toString())) {
                    showProfileDialog();
                } else {
                    ((MaterialIntroActivity) getActivity()).onFinish();
                }
                if (progressDialog.isShowing())
                    progressDialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
}
