package com.example.shiftscheduler;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Date;

public class ShiftUtils {
    public static double calculateShiftDuration(String startTime, String endTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        try {
            Date start = sdf.parse(startTime);
            Date end = sdf.parse(endTime);
            if (start != null && end != null) {
                long diffMillis = end.getTime() - start.getTime();
                return diffMillis / (1000.0 * 60 * 60); // in hours
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0.0;
    }
}
