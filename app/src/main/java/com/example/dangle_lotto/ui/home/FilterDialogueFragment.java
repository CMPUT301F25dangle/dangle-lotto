package com.example.dangle_lotto.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.example.dangle_lotto.R;
import com.example.dangle_lotto.databinding.DialogueFilterBinding;

import java.util.ArrayList;

/**
 * FilterDialogueFragment - A small dialogue fragment that shows categories for the user to select.
 *
 * <p>Saves categories selected to display the same selected ones when dialogue opened again</p>
 *
 * @author Aditya Soni
 * @version 1.0
 * @since 2025-11-01
 */
public class FilterDialogueFragment extends DialogFragment {

    public interface OnFilterSelectedListener {
        void onFiltersSelected(ArrayList<String> selectedFilters);
    }

    private OnFilterSelectedListener listener;

    public void setOnFilterSelectedListener(OnFilterSelectedListener listener) {
        this.listener = listener;
    }

    private ListView filterList;
    private ArrayList<String> test;
    private ArrayList<String> preselectedFilters;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        DialogueFilterBinding binding = DialogueFilterBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Example data
        test = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            test.add("Item " + i);
        }

        // attaching adapter to the listview
        filterList = binding.dialogueFilterList;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.filter_list_item,
                test
        );
        filterList.setAdapter(adapter);
        filterList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        // allows for filters to be saved when leaving dialogue and coming back
        if (preselectedFilters != null) {
            for (int i = 0; i < test.size(); i++) {
                if (preselectedFilters.contains(test.get(i))) {
                    filterList.setItemChecked(i, true);
                }
            }
        }

        // pressing confirm gets all selected items from filter
        binding.dialogueFilterConfirmButton.setOnClickListener(v -> {
            ArrayList<String> selected = new ArrayList<>();
            for (int i = 0; i < test.size(); i++) {
                if (filterList.isItemChecked(i)) {
                    selected.add(test.get(i));
                }
            }

            // external listener can be set and used
            if (listener != null) {
                listener.onFiltersSelected(selected);
            }

            dismiss();
        });

        // cancel button that just closes the dialogue fragment
        binding.dialogueFilterCancelButton.setOnClickListener(v -> {
            dismiss();
        });

        return root;
    }

    /**
     * Sets pre-selected filters
     * <p>To be called by parent fragment to save pre-selected filters</p>
     *
     * @param preselectedFilters The saved filters from previous use of this fragment
     */
    public void setPreselectedFilters(ArrayList<String> preselectedFilters) {
        this.preselectedFilters = preselectedFilters;
    }
}

