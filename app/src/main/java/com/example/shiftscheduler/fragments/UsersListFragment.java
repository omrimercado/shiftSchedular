package com.example.shiftscheduler.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.shiftscheduler.R;
import com.example.shiftscheduler.User;
import com.example.shiftscheduler.UserAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link UsersListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UsersListFragment extends Fragment {
    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private FirebaseFirestore db;

    public UsersListFragment() {
        // Required empty public constructor
    }

    public static UsersListFragment newInstance() {
        return new UsersListFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_users_list, container, false);

        initializeViews(view);
        setupRecyclerView();

        return view;

    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.rvUsers);;
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        db = FirebaseFirestore.getInstance();

        // You can modify this query to add ordering or filtering
        Query query = db.collection("users")
                .whereEqualTo("admin", false)
                .orderBy("email");

        FirestoreRecyclerOptions<User> options = new FirestoreRecyclerOptions.Builder<User>()
                .setQuery(query, User.class)
                .build();

        try {
            adapter = new UserAdapter(options);
            recyclerView.setAdapter(adapter);
        } catch (Exception e) {
            Log.e("RecyclerViewInit", "Error initializing adapter", e);
        }

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clean up references to prevent memory leaks
        recyclerView = null;
        adapter = null;
        db = null;
    }
}