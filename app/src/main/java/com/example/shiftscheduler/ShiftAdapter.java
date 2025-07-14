package com.example.shiftscheduler;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.*;

public class ShiftAdapter extends FirestoreRecyclerAdapter<Shift, ShiftAdapter.ShiftViewHolder> {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public ShiftAdapter(@NonNull FirestoreRecyclerOptions<Shift> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull ShiftViewHolder holder, int position, @NonNull Shift model) {
        holder.dateText.setText("Date: " + model.getDateFormatted());
        holder.timeText.setText("Time: " + model.getStartTime() + " - " + model.getEndTime());
        holder.requiredText.setText("Required: " + model.getRequiredEmployees());
        List<Map<String, Object>> users = model.getUsers();
        holder.assignedText.setText("Assigned: " + (users != null ? users.size() : 0));
        holder.fullShiftText.setText("Full: " + model.isFullShift());

        String docId = getSnapshots().getSnapshot(position).getId();

        holder.toggleFullShiftBtn.setOnClickListener(v -> {
            boolean newStatus = !model.isFullShift();
            db.collection("shifts").document(docId)
                    .update("fullShift", newStatus)
                    .addOnSuccessListener(aVoid ->
                            Toast.makeText(holder.itemView.getContext(), "Full shift updated.", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(holder.itemView.getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        holder.updateShiftBtn.setOnClickListener(v -> {
            View dialogView = LayoutInflater.from(holder.itemView.getContext()).inflate(R.layout.dialog_update_shift, null);
            EditText dateInput = dialogView.findViewById(R.id.etDate);
            EditText startInput = dialogView.findViewById(R.id.etStartTime);
            EditText endInput = dialogView.findViewById(R.id.etEndTime);
            EditText requiredInput = dialogView.findViewById(R.id.etRequiredEmployees);

            dateInput.setOnClickListener(view -> {
                Calendar calendar = Calendar.getInstance();
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        holder.itemView.getContext(),
                        (view1, year, month, dayOfMonth) -> {
                            String selectedDate = year + "-" + String.format("%02d", (month + 1)) + "-" + String.format("%02d", dayOfMonth);
                            dateInput.setText(selectedDate);
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                );
                datePickerDialog.show();
            });

            startInput.setText(model.getStartTime());
            endInput.setText(model.getEndTime());
            requiredInput.setText(String.valueOf(model.getRequiredEmployees()));

            new AlertDialog.Builder(holder.itemView.getContext())
                    .setTitle("Update Shift")
                    .setView(dialogView)
                    .setPositiveButton("Update", (dialog, which) -> {
                        String newDateStr = dateInput.getText().toString().trim();
                        String newStart = startInput.getText().toString().trim();
                        String newEnd = endInput.getText().toString().trim();
                        String requiredStr = requiredInput.getText().toString().trim();

                        if (newDateStr.isEmpty() || newStart.isEmpty() || newEnd.isEmpty() || requiredStr.isEmpty()) {
                            Toast.makeText(holder.itemView.getContext(), "Please fill in all fields.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Timestamp newDate;
                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                            newDate = new Timestamp(Objects.requireNonNull(sdf.parse(newDateStr)));
                        } catch (Exception e) {
                            Toast.makeText(holder.itemView.getContext(), "Invalid date.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        int newRequired = Integer.parseInt(requiredStr);

                        db.collection("shifts").document(docId)
                                .update(
                                        "date", newDate,
                                        "start_time", newStart,
                                        "end_time", newEnd,
                                        "requiredEmployees", newRequired
                                )
                                .addOnSuccessListener(aVoid ->
                                        Toast.makeText(holder.itemView.getContext(), "Shift updated.", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e ->
                                        Toast.makeText(holder.itemView.getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        holder.confirmArrivalBtn.setOnClickListener(v -> {
            if (users == null || users.isEmpty()) {
                Toast.makeText(holder.itemView.getContext(), "No users to confirm.", Toast.LENGTH_SHORT).show();
                return;
            }

            String[] userIds = new String[users.size()];
            boolean[] arrived = new boolean[users.size()];
            for (int i = 0; i < users.size(); i++) {
                Map<String,Object> u = users.get(i);
                userIds[i] = Objects.toString(u.get("name"),  Objects.toString(u.get("email")));
                arrived[i] = Boolean.TRUE.equals(users.get(i).get("arrived"));
            }

            View dialogView = LayoutInflater.from(holder.itemView.getContext()).inflate(R.layout.dialog_confirm_arrival, null);
            LinearLayout container = dialogView.findViewById(R.id.userArrivalContainer);
            List<CheckBox> checkBoxes = new ArrayList<>();

            for (int i = 0; i < userIds.length; i++) {
                CheckBox checkBox = new CheckBox(holder.itemView.getContext());
                checkBox.setText(userIds[i]);
                checkBox.setChecked(arrived[i]);
                container.addView(checkBox);
                checkBoxes.add(checkBox);
            }

            new AlertDialog.Builder(holder.itemView.getContext())
                    .setTitle("Confirm Arrivals")
                    .setView(dialogView)
                    .setPositiveButton("Save", (dialog, which) -> {
                        List<Map<String, Object>> updatedUsers = new ArrayList<>();
                        for (CheckBox cb : checkBoxes) {
                            Map<String, Object> userMap = new HashMap<>();
                            userMap.put("name", cb.getText().toString());
                            userMap.put("arrived", cb.isChecked());
                            updatedUsers.add(userMap);
                        }
                        db.collection("shifts").document(docId)
                                .update("users", updatedUsers)
                                .addOnSuccessListener(aVoid ->
                                        Toast.makeText(holder.itemView.getContext(), "Arrivals updated.", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e ->
                                        Toast.makeText(holder.itemView.getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    @NonNull
    @Override
    public ShiftViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.shift_list_item, parent, false);
        return new ShiftViewHolder(v);
    }

    static class ShiftViewHolder extends RecyclerView.ViewHolder {
        TextView dateText, timeText, requiredText, assignedText, fullShiftText;
        Button updateShiftBtn, toggleFullShiftBtn, confirmArrivalBtn;

        public ShiftViewHolder(@NonNull View itemView) {
            super(itemView);
            dateText = itemView.findViewById(R.id.tvDate);
            timeText = itemView.findViewById(R.id.tvTime);
            requiredText = itemView.findViewById(R.id.tvRequired);
            assignedText = itemView.findViewById(R.id.tvAssigned);
            fullShiftText = itemView.findViewById(R.id.tvFullShift);

            updateShiftBtn = itemView.findViewById(R.id.btnUpdateShift);
            toggleFullShiftBtn = itemView.findViewById(R.id.btnToggleFullShift);
            confirmArrivalBtn = itemView.findViewById(R.id.btnConfirmArrival); // Rename in layout to 'btnConfirmArrival'
        }
    }
}
