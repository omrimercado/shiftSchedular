package com.example.shiftscheduler.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.shiftscheduler.R;
import com.google.android.material.transition.MaterialSharedAxis;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AdminDashbordFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AdminDashbordFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public AdminDashbordFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AdminDashbordFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AdminDashbordFragment newInstance(String param1, String param2) {
        AdminDashbordFragment fragment = new AdminDashbordFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setEnterTransition(new MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ true));
        setReturnTransition(new MaterialSharedAxis(MaterialSharedAxis.X, /* forward= */ false));
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_admin_dashbord, container, false);
        Button btUsers = view.findViewById(R.id.btnManageUsers);
        Button btShifts = view.findViewById(R.id.btnManageShifts);

        btUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigate(R.id.action_adminDashbordFragment2_to_usersListFragment);
            }
        });

        btShifts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigate(R.id.action_adminDashbordFragment2_to_adminShiftManageFragment);
            }
        });
        return view;

    }
}