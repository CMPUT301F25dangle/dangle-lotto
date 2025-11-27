package com.example.dangle_lotto.ui.create_event;

import android.content.DialogInterface;
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
 * @author Annie Ding, Mahd Afzal
 * @version 2.0
 * @since 2025-11-23
 */
public class QRDialogueFragment extends DialogFragment {

    private Bitmap qr;
    private DialogueQrcodeBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        binding = DialogueQrcodeBinding.inflate(inflater, container, false);

        // Set bitmap in the ImageView from your XML
        binding.createEventBannerQRDisplay.setImageBitmap(qr);

        // Done button closes dialog
        binding.btnDone.setOnClickListener(v -> dismiss());

        return binding.getRoot();
    }

    public void setQr(Bitmap bitmap){
        this.qr = bitmap;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);

        // Make sure parent exists
        if (getParentFragment() instanceof CreateEventFragment) {
            ((CreateEventFragment) getParentFragment()).goBack();
        }
    }
}

