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

    /**
     *  Interface for the parent fragment to communicate with this fragment through
     */
    public interface OnFilterSelectedListener {
        void onFiltersSelected(ArrayList<String> selectedFilters);
    }

    /**
     * Listener for when filters are selected
     */
    private OnFilterSelectedListener listener;

    /**
     * Sets the listener for when filters are selected
     *
     * @param listener The listener to set
     */
    public void setOnFilterSelectedListener(OnFilterSelectedListener listener) {
        this.listener = listener;
    }

    private ListView filterList;
    String[] categories = {
            "Sports", "Music", "Volunteering", "Education",
            "Gaming", "Food", "Tech", "Career", "Community"
    };
    private ArrayList<String> preselectedCategories;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        DialogueFilterBinding binding = DialogueFilterBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // attaching adapter to the listview
        filterList = binding.dialogueFilterList;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.filter_list_item,
                categories
        );
        filterList.setAdapter(adapter);
        filterList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        // allows for filters to be saved when leaving dialogue and coming back
        if (preselectedCategories != null) {
            for (int i = 0; i < categories.length; i++) {
                if (preselectedCategories.contains(categories[i])) {
                    filterList.setItemChecked(i, true);
                }
            }
        }

        // pressing confirm gets all selected items from filter
        binding.dialogueFilterConfirmButton.setOnClickListener(v -> {
            ArrayList<String> selected = new ArrayList<>();
            for (int i = 0; i < categories.length; i++) {
                if (filterList.isItemChecked(i)) {
                    selected.add(categories[i]);
                }
            }

            // external listener can be set and used
            if (listener != null) {
                listener.onFiltersSelected(selected);
            }

            dismiss();
        });

        // cancel button that just closes the dialogue fragment
        binding.dialogueFilterCancelButton.setOnClickListener(v -> dismiss());

        return root;
    }

    /**
     * Sets pre-selected filters
     * <p>
     * To be called by parent fragment to save pre-selected filters
     *
     * @param preselectedCategories The saved filters from previous use of this fragment
     */
    public void setPreselectedCategories(ArrayList<String> preselectedCategories) {
        this.preselectedCategories = preselectedCategories;
    }
}

