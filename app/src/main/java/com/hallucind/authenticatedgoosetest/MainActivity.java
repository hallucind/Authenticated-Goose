package com.hallucind.authenticatedgoosetest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.hallucind.authenticatedgoosetest.DialogFragments.ChangeDisplayNameDialog;
import com.hallucind.authenticatedgoosetest.DialogFragments.ChangeEmailDialog;
import com.hallucind.authenticatedgoosetest.DialogFragments.ChangePasswordDialog;
import com.hallucind.authenticatedgoosetest.DialogFragments.DeleteAccountDialog;
import com.hallucind.authenticatedgoosetest.DialogFragments.LoadingDialog;

public class MainActivity extends AppCompatActivity implements FirebaseListener,
        ChangeDisplayNameDialog.ChangeDisplayNameListener, ChangeEmailDialog.ChangeEmailListener,
        ChangePasswordDialog.ChangePasswordListener, DeleteAccountDialog.DeleteAccountListener {

    private final int PICK_IMAGE = 100;

    private FirebaseUser firebaseUser;
    LoadingDialog loadingDialog;

    private ImageView imageView;
    private TextView displayNameTxt;
    private TextView useridTxt;
    private TextView emailTxt;
    private TextView verifiedTxt;
    private TextView sendVerificationTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() == null) {
            finish();
            startActivity(new Intent(this, AuthActivity.class));
            return;
        } else {
            firebaseUser = firebaseAuth.getCurrentUser();
        }

        loadingDialog = new LoadingDialog();

        imageView = findViewById(R.id.image);
        useridTxt = findViewById(R.id.uid_txt);
        displayNameTxt = findViewById(R.id.edittext);
        emailTxt = findViewById(R.id.email_txt);
        verifiedTxt = findViewById(R.id.verified_txt);
        sendVerificationTxt = findViewById(R.id.send_verification_txt);

        Uri profilePicture = firebaseUser.getPhotoUrl();
        String uid = firebaseUser.getUid();
        String displayName = firebaseUser.getDisplayName();
        final String email = firebaseUser.getEmail();
        String verifiedEmail = "No";

        if (firebaseUser.isEmailVerified()) {
            verifiedEmail = "Yes";
            sendVerificationTxt.setVisibility(View.GONE);
        } else {
            sendVerificationTxt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FirebaseAuth auth = FirebaseAuth.getInstance();

                    auth.sendPasswordResetEmail(email)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(MainActivity.this, "Email sent! Check your inbox", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            });
        }

        Glide.with(this).load(profilePicture).into(imageView);
        useridTxt.setText(uid);
        displayNameTxt.setText(displayName);
        emailTxt.setText(email);
        verifiedTxt.setText(verifiedEmail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Authenticated Goose");
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.change_profile_picture:
                openGallery();
                break;

            case R.id.change_display_name:
                ChangeDisplayNameDialog changeDisplayNameDialog = new ChangeDisplayNameDialog();
                changeDisplayNameDialog.show(getSupportFragmentManager(),"Change Display Name");
                break;

            case R.id.change_email:
                ChangeEmailDialog changeEmailDialog = new ChangeEmailDialog();
                changeEmailDialog.show(getSupportFragmentManager(),"Change Email");
                break;

            case R.id.change_password:
                ChangePasswordDialog changePasswordDialog = new ChangePasswordDialog();
                changePasswordDialog.show(getSupportFragmentManager(),"Change Password");
                break;

            case R.id.delete_account:
                DeleteAccountDialog deleteAccountDialog = new DeleteAccountDialog();
                deleteAccountDialog.show(getSupportFragmentManager(),"Delete account");
                break;

            case R.id.sign_out:
                FirebaseAuth.getInstance().signOut();
                finish();
                startActivity(new Intent(MainActivity.this, AuthActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openGallery() {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE) {
            final Uri imageUri = data.getData();
            updateUserProfilePicture(imageUri);
        }
    }

    private void updateUserProfilePicture(final Uri uri) {
        loadingDialog.setMessage("Changing profile picture...");
        loadingDialog.show(getSupportFragmentManager(),"Changing Profile Picture");

        UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                .setPhotoUri(uri)
                .build();

        firebaseUser.updateProfile(profileChangeRequest)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            onChangedProfilePicture(uri);
                        }
                        loadingDialog.dismiss();
                    }
                });
    }

    @Override
    public void onChangedProfilePicture(Uri uri) {
        Glide.with(this).load(uri).into(imageView);
    }

    @Override
    public void onChangeDisplayName(final String displayName) {
        loadingDialog.setMessage("Changing display name...");
        loadingDialog.show(getSupportFragmentManager(),"Changing Display Name...");

        UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build();

        firebaseUser.updateProfile(profileChangeRequest)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            displayNameTxt.setText(displayName);
                        }
                        loadingDialog.dismiss();
                    }
                });
    }

    @Override
    public void onChangeEmail(final String newEmail, String password) {
        loadingDialog.setMessage("Changing email...");
        loadingDialog.show(getSupportFragmentManager(),"Changing Email");

        String currentEmail = firebaseUser.getEmail();
        AuthCredential credential = EmailAuthProvider.getCredential(currentEmail, password);

        firebaseUser.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {

                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {
                        firebaseUser.updateEmail(newEmail)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            emailTxt.setText(newEmail);
                                        }
                                        loadingDialog.dismiss();
                                    }
                                });
                        } else {
                            Toast.makeText(MainActivity.this, "Authentication failed, wrong password?", Toast.LENGTH_LONG).show();
                            loadingDialog.dismiss();
                        }
                    }
                });
    }

    @Override
    public void onChangePassword(String oldPassword, final String newPassword) {
        loadingDialog.setMessage("Changing password...");
        loadingDialog.show(getSupportFragmentManager(),"Changing Password");

        String currentEmail = firebaseUser.getEmail();
        AuthCredential credential = EmailAuthProvider.getCredential(currentEmail, oldPassword);

        firebaseUser.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {

                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            firebaseUser.updatePassword(newPassword)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(MainActivity.this, "Password was changed successfully", Toast.LENGTH_LONG).show();
                                            }
                                            loadingDialog.dismiss();
                                        }
                                    });
                        } else {
                            Toast.makeText(MainActivity.this, "Authentication failed, wrong password?", Toast.LENGTH_LONG).show();
                            loadingDialog.dismiss();
                        }
                    }
                });
    }

    @Override
    public void onDeleteAccount(String password) {
        loadingDialog.setMessage("Deleting account...");
        loadingDialog.show(getSupportFragmentManager(),"Deleting Account");

        String currentEmail = firebaseUser.getEmail();
        AuthCredential credential = EmailAuthProvider.getCredential(currentEmail, password);

        firebaseUser.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {

                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            firebaseUser.delete()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                finish();
                                                startActivity(new Intent(MainActivity.this, AuthActivity.class));
                                            }
                                            loadingDialog.dismiss();
                                        }
                                    });
                        } else {
                            Toast.makeText(MainActivity.this, "Authentication failed, wrong password?", Toast.LENGTH_LONG).show();
                            loadingDialog.dismiss();
                        }
                    }
                });
    }
}
