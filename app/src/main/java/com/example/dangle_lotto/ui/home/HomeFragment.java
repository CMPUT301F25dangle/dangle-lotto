package com.example.dangle_lotto.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dangle_lotto.Event;
import com.example.dangle_lotto.R;
import com.example.dangle_lotto.databinding.FragmentHomeBinding;

import java.util.ArrayList;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textHome;
        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        RecyclerView recyclerView = binding.homeEventRecyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        ArrayList<Event> events = new ArrayList<>();
        for (int i = 1; i < 4; i++) {
            events.add(new Event(String.format("Example %d", i), R.drawable.event_card_test_image));
        }
        EventCardAdapter adapter = new EventCardAdapter(events);
        recyclerView.setAdapter(adapter);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}