package com.example.dangle_lotto.ui.create_event;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.example.dangle_lotto.databinding.DialogueFilterBinding;

import java.util.ArrayList;

/**
 * QRDialogueFragment - A dialogue fragment that displays the QR code of an event
 * that is being made
 *
 * @author Annie Ding
 * @version 1.0
 * @since 2025-11-07
 */
public class QRDialogueFragment extends DialogFragment {
    /**
     *  Interface for the parent fragment to communicate with this fragment through
     */

    private Bitmap qr;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState){
        DialogueFilterBinding binding = DialogueFilterBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        return root;
    }
}
