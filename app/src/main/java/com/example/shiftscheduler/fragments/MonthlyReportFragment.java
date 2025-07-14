package com.example.shiftscheduler.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shiftscheduler.R;
import com.example.shiftscheduler.Shift;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Displays a list of this month's confirmed (arrived) shifts
 * and calculates total hours & salary based on the user's stored hourly rate.
 */
public class MonthlyReportFragment extends Fragment {

    private TextView tvTotalHours, tvTotalSalary;
    private RecyclerView rvReport;

    private FirebaseFirestore db;
    private String currentUserEmail;
    private double userSalaryPerHour;
    private int totalMinutes;
    private final List<ReportEntry> reportEntries = new ArrayList<>();
    private ReportAdapter adapter;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_monthly_report, container, false);

        tvTotalHours   = view.findViewById(R.id.tvTotalHours);
        tvTotalSalary  = view.findViewById(R.id.tvTotalSalary);
        rvReport       = view.findViewById(R.id.rvReport);

        rvReport.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ReportAdapter(reportEntries);
        rvReport.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "User not signed in", Toast.LENGTH_SHORT).show();
            return view;
        }
        currentUserEmail = user.getEmail();

        fetchUserSalary();

        return view;
    }

    /** Step 1: Load the current user's hourly salary from Firestore by email */
    private void fetchUserSalary() {
        db.collection("users")
                .whereEqualTo("email", currentUserEmail)
                .limit(1)
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        DocumentSnapshot doc = query.getDocuments().get(0);
                        Double salary = doc.getDouble("salary");
                        userSalaryPerHour = (salary != null) ? salary : 0;
                        loadShiftsForThisMonth();
                    } else {
                        Toast.makeText(getContext(), "User record not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to load salary: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    /** Step 2: Query this month's shifts and build the report entries */
    private void loadShiftsForThisMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        Timestamp monthStart = new Timestamp(cal.getTime());
        cal.add(Calendar.MONTH, 1);
        Timestamp nextMonthStart = new Timestamp(cal.getTime());

        db.collection("shifts")
                .whereGreaterThanOrEqualTo("date", monthStart)
                .whereLessThan("date", nextMonthStart)
                .get()
                .addOnSuccessListener(this::processShifts)
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to load shifts: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void processShifts(QuerySnapshot snaps) {
        reportEntries.clear();
        totalMinutes = 0;

        for (DocumentSnapshot doc : snaps.getDocuments()) {
            Shift shift = doc.toObject(Shift.class);
            if (shift == null || shift.getUsers() == null) continue;

            for (Map<String, Object> userMap : shift.getUsers()) {
                String email = (String) userMap.get("email");
                Boolean arrived = (Boolean) userMap.get("arrived");

                if (currentUserEmail.equals(email) && Boolean.TRUE.equals(arrived)) {
                    int durationMins = getShiftDurationInMinutes(shift.getStartTime(), shift.getEndTime());
                    totalMinutes += durationMins;
                    double shiftSalary = (durationMins / 60.0) * userSalaryPerHour;
                    reportEntries.add(new ReportEntry(
                            shift.getDate().toDate(),
                            shift.getStartTime(),
                            shift.getEndTime(),
                            durationMins,
                            shiftSalary
                    ));
                    break;
                }
            }
        }

        int totalHours = totalMinutes / 60;
        double totalSalary = totalHours * userSalaryPerHour;
        tvTotalHours .setText("Total Hours: " + totalHours);
        tvTotalSalary.setText("Total Salary: ₪" + String.format(Locale.getDefault(), "%.2f", totalSalary));

        adapter.notifyDataSetChanged();
    }

    /** Utility to compute duration in minutes from "HH:mm" strings */
    private int getShiftDurationInMinutes(String start, String end) {
        try {
            SimpleDateFormat fmt = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date d1 = fmt.parse(start);
            Date d2 = fmt.parse(end);
            long diff = d2.getTime() - d1.getTime();
            return (int)(diff / (1000 * 60));
        } catch (Exception e) {
            return 0;
        }
    }

    /** Data model for a single report row */
    private static class ReportEntry {
        final Date date;
        final String startTime, endTime;
        final int durationMinutes;
        final double salary;

        ReportEntry(Date date, String startTime, String endTime, int durationMinutes, double salary) {
            this.date = date;
            this.startTime = startTime;
            this.endTime = endTime;
            this.durationMinutes = durationMinutes;
            this.salary = salary;
        }
    }

    /** RecyclerView.Adapter to display each confirmed shift */
    private class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {
        private final List<ReportEntry> items;

        ReportAdapter(List<ReportEntry> items) {
            this.items = items;
        }

        @NonNull @Override
        public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_report, parent, false);
            return new ReportViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
            ReportEntry entry = items.get(position);
            holder.tvDate.setText("Date: " + new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(entry.date));
            holder.tvTime.setText("Time: " + entry.startTime + " - " + entry.endTime);
            holder.tvDuration .setText("Duration: " + (entry.durationMinutes / 60) + "h "
                    + (entry.durationMinutes % 60) + "m");
            holder.tvSalary   .setText("Salary: ₪"
                    + String.format(Locale.getDefault(), "%.2f", entry.salary));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ReportViewHolder extends RecyclerView.ViewHolder {
            final TextView tvDate, tvTime, tvDuration, tvSalary;

            ReportViewHolder(@NonNull View itemView) {
                super(itemView);
                tvDate     = itemView.findViewById(R.id.tvReportDate);
                tvTime     = itemView.findViewById(R.id.tvReportTime);
                tvDuration = itemView.findViewById(R.id.tvReportDuration);
                tvSalary   = itemView.findViewById(R.id.tvReportSalary);
            }
        }
    }
}
