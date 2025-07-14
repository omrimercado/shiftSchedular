package com.example.shiftscheduler;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.PropertyName;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Shift {
    private List<Map<String, Object>> users;
    private Timestamp date;

    @PropertyName("start_time")
    private String startTime;
    @PropertyName("end_time")
    private String endTime;
    private int requiredEmployees;
    private boolean fullShift;

    public Shift() {}

    public Shift(List<Map<String, Object>> users, Timestamp date, String startTime, String endTime, int requiredEmployees, boolean fullShift) {
        this.users = users;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.requiredEmployees = requiredEmployees;
        this.fullShift = fullShift;
    }

    public List<Map<String, Object>> getUsers() {
        return users;
    }

    public void setUsers(List<Map<String, Object>> users) {
        this.users = users;
    }
    public Timestamp getDate() { return date; }

    public void setDate(Timestamp date) { this.date = date; }

    @PropertyName("end_time")
    public String getEndTime() { return endTime; }

    @PropertyName("end_time")
    public void setEndTime(String endTime) { this.endTime = endTime; }

    @PropertyName("start_time")
    public String getStartTime() { return startTime; }

    @PropertyName("start_time")
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public int getRequiredEmployees() { return requiredEmployees; }
    public void setRequiredEmployees(int requiredEmployees) { this.requiredEmployees = requiredEmployees; }

    public boolean isFullShift() { return fullShift; }
    public void setFullShift(boolean fullShift) { this.fullShift = fullShift; }
    public String getDateFormatted() {
        if (date != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            return sdf.format(date.toDate());
        }
        return "";
    }
}
