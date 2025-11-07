package com.example.dangle_lotto.ui.create_event;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;

import com.example.dangle_lotto.databinding.DialogueQrcodeBinding;

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
        @NonNull DialogueQrcodeBinding binding = DialogueQrcodeBinding.inflate(inflater);
        ConstraintLayout root = new ConstraintLayout(requireContext());
        ImageView qrDisplay = new ImageView(requireContext());
        qrDisplay.setImageBitmap(qr);
        root.addView(qrDisplay);
        return root;
    }

    /**
     * Sets the qr code bitmap
     * @param bitmap The bitmap generated
     */
    public void setQr(Bitmap bitmap){
        this.qr = bitmap;
    }
}
