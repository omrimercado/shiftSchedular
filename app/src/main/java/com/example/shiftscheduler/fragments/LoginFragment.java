package com.example.shiftscheduler.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.shiftscheduler.R;
import com.google.android.gms.auth.api.signin.*;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.transition.MaterialSharedAxis;
import com.google.firebase.auth.*;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginFragment extends Fragment {
    private static final String TAG = "LoginFragment";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setEnterTransition(new MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ true));
        setReturnTransition(new MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ false));
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

    }



    private void checkUserRoleAndNavigate(String uid) {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Boolean isAdmin = documentSnapshot.getBoolean("admin");
                    if (Boolean.TRUE.equals(isAdmin)) {
                        Navigation.findNavController(requireView()).navigate(R.id.action_loginFragment_to_adminDashbordFragment2);
                    } else {
                        Navigation.findNavController(requireView()).navigate(R.id.action_loginFragment_to_workerDashboardFragment); ///need to change
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error accessing database", Toast.LENGTH_SHORT).show());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        EditText etEmail = view.findViewById(R.id.etLoginEmail);
        EditText etPassword = view.findViewById(R.id.etLoginPassword);
        Button btnLogin = view.findViewById(R.id.btnDoLogin);
        TextView tvGoToRegister = view.findViewById(R.id.tvGoToRegister);

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(getContext(), "Please fill in email and password", Toast.LENGTH_SHORT).show();
                return;
            }
            btnLogin.setEnabled(false);
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        btnLogin.setEnabled(true);
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                checkUserRoleAndNavigate(user.getUid());
                            }
                        } else {
                            Toast.makeText(getContext(), "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        tvGoToRegister.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_loginFragment_to_registerFragment);
        });

        return view;
    }
}
