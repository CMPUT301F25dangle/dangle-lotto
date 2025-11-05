package com.example.dangle_lotto.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;

import com.example.dangle_lotto.FirebaseManager;
import com.example.dangle_lotto.R;
import com.example.dangle_lotto.databinding.FragmentHomeBinding;
import com.example.dangle_lotto.Event;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private EventsAdapter adapter;

    // --- Minimal adapter for a two-line row (expects row_event.xml) ---
    static class EventsAdapter extends BaseAdapter {
        private final List<Event> data = new ArrayList<>();

        void submit(List<Event> items) {
            data.clear();
            if (items != null) data.addAll(items);
            notifyDataSetChanged();
        }



        @Override public int getCount() { return data.size(); }
        @Override public Event getItem(int position) { return data.get(position); }
        @Override public long getItemId(int position) { return position; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = (convertView != null)
                    ? convertView
                    : LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.row_event, parent, false);

            TextView tvTitle = v.findViewById(R.id.tv_title);

            Event e = getItem(position);
            tvTitle.setText(getItem(position).getName());
            return v;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        HomeViewModel homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // --- ListView setup ---
        ListView list = binding.listEvents;
        EventsAdapter adapter = new EventsAdapter();
        list.setAdapter(adapter);

        // Keep the original "observe text from ViewModel" behavior:
        // We’ll display it as a non-clickable header above the list.
        TextView header = new TextView(requireContext());
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        header.setPadding(pad, pad, pad, pad);
        header.setTextSize(16f);
        list.addHeaderView(header, null, false);
        homeViewModel.getText().observe(getViewLifecycleOwner(), header::setText);

        List<Event> mock = new ArrayList<>();
        mock.add(new Event(
                UUID.randomUUID().toString(),
                "Snacks, games, and networking.",
                20,
                "CMPUT Club Mixer",
                com.google.firebase.Timestamp.now(),
                "Student Union Building",
                "org_123",
                "photo_001"
        ));
        mock.add(new Event(
                UUID.randomUUID().toString(),
                "Lightning talks + Q&A.",
                50,
                "Tech Talk Night",
                com.google.firebase.Timestamp.now(),
                "CSC Atrium",
                "org_456",
                "photo_002"
        ));
        adapter.submit(mock);


        // Inside onCreateView(...) after list.setAdapter(adapter)
        binding.listEvents.setOnItemLongClickListener((parent, view, position, id) -> {
            // Navigate WITHOUT passing arguments
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_home_to_eventDetail);
            return true;
        });
        return root;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}