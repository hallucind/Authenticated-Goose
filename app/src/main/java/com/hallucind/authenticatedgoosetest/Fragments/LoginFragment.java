package com.hallucind.authenticatedgoosetest.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.hallucind.authenticatedgoosetest.AuthActivity;
import com.hallucind.authenticatedgoosetest.DialogFragments.LoadingDialog;
import com.hallucind.authenticatedgoosetest.DialogFragments.ResetPasswordDialog;
import com.hallucind.authenticatedgoosetest.MainActivity;
import com.hallucind.authenticatedgoosetest.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class LoginFragment extends Fragment {

    private LoadingDialog loadingDialog;
    private FirebaseAuth firebaseAuth;

    private TextView emailTxt;
    private TextView passwordTxt;
    private TextView registerTxt;
    private TextView forgotPasswordTxt;
    private Button signInBtn;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View parentView = inflater.inflate(R.layout.fragment_login, container, false);

        loadingDialog = new LoadingDialog();
        firebaseAuth = FirebaseAuth.getInstance();

        emailTxt = parentView.findViewById(R.id.email_txt);
        passwordTxt = parentView.findViewById(R.id.password_txt);
        forgotPasswordTxt = parentView.findViewById(R.id.forgot_password_txt);
        registerTxt = parentView.findViewById(R.id.not_registered_txt);
        signInBtn = parentView.findViewById(R.id.login_btn);

        registerTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((AuthActivity)getActivity()).changeActivity(new RegisterFragment());
            }
        });

        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailTxt.getText().toString();
                String password = passwordTxt.getText().toString();
                signIn(email, password);
                loadingDialog.setMessage("Signing in...");
                loadingDialog.show(getActivity().getSupportFragmentManager(),"Signing In");
            }
        });

        forgotPasswordTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ResetPasswordDialog resetPasswordDialog = new ResetPasswordDialog();
                resetPasswordDialog.show(getActivity().getSupportFragmentManager(),"Change Display Name");
            }
        });

        return parentView;
    }

    private void signIn(String email, String password) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            getActivity().finish();
                            startActivity(new Intent(getActivity(), MainActivity.class));
                        } else {
                            Toast.makeText(getActivity(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                        loadingDialog.dismiss();
                    }
                });
    }
}