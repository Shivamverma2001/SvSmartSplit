package com.sv.svsplitsmart.ui.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.sv.svsplitsmart.databinding.FragmentSlideshowBinding;

public class ActivityFragment extends Fragment {

    private FragmentSlideshowBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ActivityViewModel activityViewModel =
                new ViewModelProvider(this).get(ActivityViewModel.class);

        binding = FragmentSlideshowBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textSlideshow;
        activityViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}