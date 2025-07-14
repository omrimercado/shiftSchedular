package com.example.shiftscheduler;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {

    private final List<Shift> shifts;
    private final String currentUserId;

    public ReportAdapter(List<Shift> shifts, String currentUserId) {
        this.shifts = shifts;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_report, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        Shift shift = shifts.get(position);
        holder.tvDate.setText("Date: " + shift.getDateFormatted());
        holder.tvTime.setText("Time: " + shift.getStartTime() + " - " + shift.getEndTime());

        int duration = getShiftDuration(shift.getStartTime(), shift.getEndTime());
        holder.tvDuration.setText("Duration: " + duration + " hours");
    }

    private int getShiftDuration(String start, String end) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date startDate = sdf.parse(start);
            Date endDate = sdf.parse(end);
            long diff = endDate.getTime() - startDate.getTime();
            return (int) (diff / (1000 * 60 * 60));
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public int getItemCount() {
        return shifts.size();
    }

    static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvTime, tvDuration;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvReportDate);
            tvTime = itemView.findViewById(R.id.tvReportTime);
            tvDuration = itemView.findViewById(R.id.tvReportDuration);
        }
    }
}
