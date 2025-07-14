package com.example.shiftscheduler.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import android.view.*;
import com.example.shiftscheduler.R;
import com.google.android.material.transition.platform.MaterialSharedAxis;
import com.google.android.material.button.MaterialButton;

public class WelcomeFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setEnterTransition(new MaterialSharedAxis(MaterialSharedAxis.X, true));
        setReturnTransition(new MaterialSharedAxis(MaterialSharedAxis.X, false));

    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_welcome, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialButton register = view.findViewById(R.id.btnRegister);
        MaterialButton login    = view.findViewById(R.id.btnLogin);

        register.setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_welcomeFragment_to_registerFragment)
        );

        login.setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_welcomeFragment_to_loginFragment)
        );
    }
}
