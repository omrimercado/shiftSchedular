package com.example.shiftscheduler.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.shiftscheduler.R;
import com.google.android.material.transition.platform.MaterialSharedAxis;

public class WorkerDashboardFragment extends Fragment {

    public WorkerDashboardFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_worker_dashboard, container, false);
        setEnterTransition(new MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ true));
        setReturnTransition(new MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ false));
        Button btnShifts = view.findViewById(R.id.btnViewShifts);
        Button btnReport = view.findViewById(R.id.btnViewReport);

        btnShifts.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_workerDashboardFragment_to_userShiftsFragment)
        );

        btnReport.setOnClickListener(v ->
               Navigation.findNavController(view).navigate(R.id.action_workerDashboardFragment_to_monthlyReportFragment)
        );

        return view;
    }
}
