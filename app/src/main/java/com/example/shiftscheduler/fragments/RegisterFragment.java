package com.example.shiftscheduler.fragments;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.shiftscheduler.R;
import com.google.android.material.transition.platform.MaterialSharedAxis;
import com.google.firebase.auth.*;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.*;

public class RegisterFragment extends Fragment {

    private static final String TAG = "RegisterFragment";


    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private EditText etEmail, etName, etPassword, etDob;
    private TextView tvGoLogin;
    private Button btnRegister;



    private String selectedDate = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setEnterTransition(new MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ true));
        setReturnTransition(new MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ false));
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        etName = view.findViewById(R.id.etRegName);
        etEmail = view.findViewById(R.id.etRegEmail);
        etPassword = view.findViewById(R.id.etRegPassword);
        etDob = view.findViewById(R.id.etRegDob);
        btnRegister = view.findViewById(R.id.btnDoRegister);
        tvGoLogin = view.findViewById(R.id.tvLogin);
        etDob.setOnClickListener(v -> showDatePicker());
        btnRegister.setOnClickListener(v -> handleManualRegistration());

        tvGoLogin.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_registerFragment_to_loginFragment);
        });
        return view;
    }

    private void handleManualRegistration() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String dob = etDob.getText().toString().trim();

        if (!validateInputs(email, name, password, dob)) return;

        btnRegister.setEnabled(false);
        btnRegister.setText("נרשם...");

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String uid = authResult.getUser().getUid();
                    saveUserToFirestore(uid, email, name, dob);
                })
                .addOnFailureListener(e -> {
                    btnRegister.setEnabled(true);
                    btnRegister.setText("הירשם");
                    Toast.makeText(getContext(), "שגיאה: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }






    private void saveUserToFirestore(String uid, String email, String name, String dob) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("email", email);
        userData.put("name", name);
        userData.put("dateOfBirth", dob);
        userData.put("admin", false);
        userData.put("createdAt", System.currentTimeMillis());
        userData.put("salary", 30.0);

        db.collection("users").document(uid).set(userData)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(getContext(), "הרשמה הושלמה!", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView()).navigate(R.id.action_registerFragment_to_workerDashboardFragment);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "שגיאה בשמירת משתמש: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    mAuth.getCurrentUser().delete(); // rollback
                });
    }

    private boolean validateInputs(String email, String name, String password, String dob) {
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("אימייל לא תקין");
            return false;
        }
        if (name.isEmpty()) {
            etName.setError("נא הכנס שם");
            return false;
        }
        if (password.length() < 6) {
            etPassword.setError("סיסמה קצרה מדי");
            return false;
        }
        if (dob.isEmpty()) {
            Toast.makeText(getContext(), "בחר תאריך לידה", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void navigateToLogin() {
        Navigation.findNavController(requireView())
                .navigate(R.id.action_registerFragment_to_loginFragment);
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(getContext(), (view, year, month, day) -> {
            selectedDate = day + "/" + (month + 1) + "/" + year;
            etDob.setText(selectedDate);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
                .show();
    }
}
