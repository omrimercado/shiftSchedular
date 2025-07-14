package com.example.shiftscheduler.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.example.shiftscheduler.R;
import com.example.shiftscheduler.Shift;
import com.example.shiftscheduler.ShiftAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AdminShiftManageFragment extends Fragment {

    private RecyclerView recyclerView;
    private ShiftAdapter adapter;
    private FloatingActionButton fabAddShift;
    private FirebaseFirestore db;

    public AdminShiftManageFragment() {
        // Required empty public constructor
    }

    public static AdminShiftManageFragment newInstance() {
        return new AdminShiftManageFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_shift_manage, container, false);

        recyclerView = view.findViewById(R.id.rvShifts);
        fabAddShift = view.findViewById(R.id.fabAddShift);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        db = FirebaseFirestore.getInstance();

        setupRecyclerView();

        fabAddShift.setOnClickListener(v -> {
            View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_update_shift, null);
            EditText dateInput = dialogView.findViewById(R.id.etDate);
            EditText startInput = dialogView.findViewById(R.id.etStartTime);
            EditText endInput = dialogView.findViewById(R.id.etEndTime);
            EditText requiredInput = dialogView.findViewById(R.id.etRequiredEmployees);

            dateInput.setInputType(InputType.TYPE_NULL);
            dateInput.setOnClickListener(v1 -> {
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        getContext(),
                        (view1, selectedYear, selectedMonth, selectedDay) -> {
                            String formattedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                            dateInput.setText(formattedDate);
                        },
                        year, month, day
                );
                datePickerDialog.show();
            });


            new AlertDialog.Builder(getContext())
                    .setTitle("Add New Shift")
                    .setView(dialogView)
                    .setPositiveButton("Add", (dialog, which) -> {
                        String date = dateInput.getText().toString().trim();
                        String startTime = startInput.getText().toString().trim();
                        String endTime = endInput.getText().toString().trim();
                        String requiredStr = requiredInput.getText().toString().trim();

                        if (date.isEmpty() || startTime.isEmpty() || endTime.isEmpty() || requiredStr.isEmpty()) {
                            Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        int requiredEmployees = Integer.parseInt(requiredStr);
                        Map<String, Object> shiftMap = new HashMap<>();
                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                            Date parsedDate = sdf.parse(date);
                            shiftMap.put("date", new Timestamp(parsedDate));
                        } catch (ParseException e) {
                            Toast.makeText(getContext(), "Invalid date format.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        shiftMap.put("start_time", startTime);
                        shiftMap.put("end_time", endTime);
                        shiftMap.put("requiredEmployees", requiredEmployees);
                        shiftMap.put("users", new ArrayList<String>());
                        shiftMap.put("fullShift", false);

                        FirebaseFirestore.getInstance().collection("shifts")
                                .add(shiftMap)
                                .addOnSuccessListener(documentReference ->
                                        Toast.makeText(getContext(), "Shift added successfully", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e ->
                                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        return view;
    }

    private void setupRecyclerView() {
        Query query = db.collection("shifts")
                .orderBy("date"); // Adjust ordering if needed

        FirestoreRecyclerOptions<Shift> options = new FirestoreRecyclerOptions.Builder<Shift>()
                .setQuery(query, Shift.class)
                .build();

        adapter = new ShiftAdapter(options);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
        }
    }
}
