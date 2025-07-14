package com.example.shiftscheduler.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shiftscheduler.R;
import com.example.shiftscheduler.Shift;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class UserShiftsFragment extends Fragment {

    private RecyclerView rvUserShifts;
    private FirebaseFirestore db;
    private String myEmail;
    private ShiftAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_shifts, container, false);

        rvUserShifts = view.findViewById(R.id.rvUserShifts);
        rvUserShifts.setLayoutManager(new LinearLayoutManager(getContext()));

        FirebaseUser me = FirebaseAuth.getInstance().getCurrentUser();
        myEmail = (me != null ? me.getEmail() : "");




        db = FirebaseFirestore.getInstance();
        setupAdapter();
        return view;
    }

    private void setupAdapter() {
        // 🔥 ONLY upcoming: date >= now
        Query q = db.collection("shifts")
                .whereGreaterThanOrEqualTo("date", Timestamp.now())
                .orderBy("date", Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<Shift> opts =
                new FirestoreRecyclerOptions.Builder<Shift>()
                        .setQuery(q, Shift.class)
                        .build();

        adapter = new ShiftAdapter(opts);
        rvUserShifts.setAdapter(adapter);
    }

    @Override public void onStart() {
        super.onStart();
        if (adapter != null){
            adapter.startListening();
            rvUserShifts.post(() -> adapter.notifyDataSetChanged());
            }
    }
    @Override public void onStop() {
        super.onStop();
        if (adapter != null) adapter.stopListening();
    }

    private class ShiftAdapter
            extends FirestoreRecyclerAdapter<Shift, ShiftAdapter.VH> {

        ShiftAdapter(@NonNull FirestoreRecyclerOptions<Shift> opts) {
            super(opts);
        }

        @Override
        protected void onBindViewHolder(@NonNull VH holder,
                                        int position,
                                        @NonNull Shift shift) {
            // 1) bind date/time
            String dateText = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(shift.getDate().toDate());
            holder.tvShiftDate.setText(dateText);
            holder.tvShiftTime.setText(
                    shift.getStartTime() + " – " + shift.getEndTime()
            );

            // 2) see if *I* am assigned
            boolean assigned = false;
            Map<String,Object> myEntry = null;
            List<Map<String,Object>> users = shift.getUsers();
            if (users != null) {
                for (Map<String,Object> u : users) {
                    if (myEmail.equals(u.get("email"))) {
                        assigned = true;
                        myEntry = u;
                        break;
                    }
                }
            }

            // 3) swap button text & logic
            holder.btnAssignUnassign.setText(
                    assigned ? "Unassign" : "Assign"
            );

            final boolean userAssigned = assigned;
            final Map<String,Object> entryToRemove = myEntry;

            holder.btnAssignUnassign.setOnClickListener(v -> {
                String docId = getSnapshots().getSnapshot(position).getId();
                DocumentReference ref = db.collection("shifts").document(docId);

                if (!userAssigned) {
                    // ASSIGN
                    Map<String,Object> entry = new HashMap<>();
                    entry.put("email",   myEmail);
                    entry.put("arrived", false);



                    ref.update("users", FieldValue.arrayUnion(entry))
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), "Assigned!", Toast.LENGTH_SHORT).show();
                                addToCalendar(v.getContext(), shift);
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                            );
                } else {
                    // UNASSIGN
                    ref.update(
                            "users",     FieldValue.arrayRemove(entryToRemove),
                            "fullShift", false
                    ).addOnSuccessListener(aVoid ->
                            Toast.makeText(getContext(), "Unassigned!", Toast.LENGTH_SHORT).show()
                    ).addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
                }
            });
        }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent,
                                     int viewType) {
            View item = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.user_item_shift, parent, false);
            return new VH(item);
        }

        class VH extends RecyclerView.ViewHolder {
            TextView tvShiftDate, tvShiftTime;
            Button   btnAssignUnassign;

            VH(View itemView) {
                super(itemView);
                tvShiftDate       = itemView.findViewById(R.id.tvShiftDate);
                tvShiftTime       = itemView.findViewById(R.id.tvShiftTime);
                btnAssignUnassign = itemView.findViewById(R.id.btnAssignUnassign);
            }
        }

        private void addToCalendar(Context ctx, Shift shift) {
            try {
                // parse date & times
                Date day = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        .parse(shift.getDateFormatted());
                SimpleDateFormat tf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                Date st = tf.parse(shift.getStartTime());
                Date et = tf.parse(shift.getEndTime());

                Calendar start = Calendar.getInstance();
                start.setTime(day);
                Calendar t1 = Calendar.getInstance(); t1.setTime(st);
                start.set(Calendar.HOUR_OF_DAY, t1.get(Calendar.HOUR_OF_DAY));
                start.set(Calendar.MINUTE,     t1.get(Calendar.MINUTE));

                Calendar end = (Calendar) start.clone();
                Calendar t2 = Calendar.getInstance(); t2.setTime(et);
                end.set(Calendar.HOUR_OF_DAY, t2.get(Calendar.HOUR_OF_DAY));
                end.set(Calendar.MINUTE,     t2.get(Calendar.MINUTE));

                Intent intent = new Intent(Intent.ACTION_INSERT)
                        .setData(CalendarContract.Events.CONTENT_URI)
                        .putExtra(CalendarContract.Events.TITLE,       "Work Shift")
                        .putExtra(CalendarContract.Events.DESCRIPTION, "Scheduled shift")
                        .putExtra(CalendarContract.Events.AVAILABILITY,
                                CalendarContract.Events.AVAILABILITY_BUSY)
                        .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                                start.getTimeInMillis())
                        .putExtra(CalendarContract.EXTRA_EVENT_END_TIME,
                                end.getTimeInMillis());

                ctx.startActivity(intent);

            } catch (Exception ex) {
                Toast.makeText(ctx, "Could not add to calendar", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
