package com.example.dangle_lotto.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.dangle_lotto.R;
import com.example.dangle_lotto.databinding.DialogQrScannerBinding;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

public class QRScannerDialogFragment extends DialogFragment {
    private DialogQrScannerBinding binding;
    private DecoratedBarcodeView barcodeView;

    public interface QRScanListener {
        void onScanned(String qr);
    }

    private QRScanListener listener;

    public void setListener(QRScanListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        binding = DialogQrScannerBinding.inflate(inflater, container, false);

        barcodeView = binding.barcodeScannerView;
        barcodeView.decodeContinuous(callback);
        barcodeView.getStatusView().setVisibility(View.GONE);

//        binding.btnCancel.setOnClickListener(v -> dismiss());

        return binding.getRoot();
    }

    private final BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (result.getText() != null) {

                // pause scanning so we don't read twice
                barcodeView.pause();

                if (listener != null)
                    listener.onScanned(result.getText());

                dismiss();
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        barcodeView.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        barcodeView.pause();
    }
}

