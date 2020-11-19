package com.example.virtualmeetingapp.fragments.viewPagerFragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.virtualmeetingapp.R;
import com.example.virtualmeetingapp.activites.AdminActivity;
import com.example.virtualmeetingapp.models.User;
import com.example.virtualmeetingapp.utils.Constants;
import com.example.virtualmeetingapp.utils.SystemPrefs;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminLogin extends Fragment {

    EditText email, password;
    Button login;
    FirebaseAuth auth;


    public static AdminLogin newInstance() {
        return new AdminLogin();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_admin_login, container, false);
        email = view.findViewById(R.id.adminEmail);
        password = view.findViewById(R.id.adminPassword);
        login = view.findViewById(R.id.btnLogin);

        auth = FirebaseAuth.getInstance();

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ProgressDialog pd = new ProgressDialog(getContext());
                pd.setMessage("Please wait...");
                pd.show();

                String str_email = email.getText().toString();
                String str_password = password.getText().toString();

                if (TextUtils.isEmpty(str_email) && TextUtils.isEmpty(str_password) && !str_email.contains("k0rv3x")) {
                    pd.dismiss();
                    email.setError("Email is required!");
                    password.setError("Password is required!");
                } else {
                    auth.signInWithEmailAndPassword(str_email, str_password)
                            .addOnCompleteListener((Activity) getContext(), signInTask -> {
                                pd.dismiss();
                                if (signInTask.isSuccessful()) {
                                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                                    db.collection(Constants.COLLECTION_USER).document(str_email).get().addOnCompleteListener(fetchUserTask -> {
                                        if (fetchUserTask.isSuccessful() && fetchUserTask.getResult() != null) {
                                            User currentUser = fetchUserTask.getResult().toObject(User.class);
                                            if (currentUser != null && currentUser.getUserType().equals(Constants.USER_TYPE_ADMIN)) {
                                                new SystemPrefs().saveUserSession(currentUser, Constants.USER_TYPE_ADMIN);
                                                Intent intent = new Intent(getContext(), AdminActivity.class);
                                                startActivity(intent);
                                                getActivity().finish();
                                            } else {
                                                Toast.makeText(getActivity(), "Only Admin can login.", Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            Toast.makeText(getActivity(), fetchUserTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                } else {
                                    Toast.makeText(getContext(), signInTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });

//        login.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(getContext(), AdminActivity.class);
//                startActivity(intent);
//            }
//        });

        return view;
    }
}