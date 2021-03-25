package com.google.kouheiszk.qrcodeimagescanner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void pickGalleryImage(View view) {
        ImagePicker.Companion.with(this)
                .galleryOnly()
                .maxResultSize(1080, 1080)
                .start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            String filePath = ImagePicker.Companion.getFilePath(data);

            InputImage image;
            try {
                image = InputImage.fromFilePath(getApplicationContext(), getUri(filePath));
            } catch (IOException e) {
                Log.e("QRCodeImageScanner", e.getMessage());
                return;
            }

            BarcodeScannerOptions options = new BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build();
            BarcodeScanner scanner = BarcodeScanning.getClient(options);

            scanner.process(image)
                    .addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                        @Override
                        public void onSuccess(List<Barcode> barcodes) {
                            if (!barcodes.isEmpty()) {
                                for (Barcode barcode : barcodes) {
                                    int valueType = barcode.getValueType();
                                    if (valueType == Barcode.TYPE_URL) {
                                        String url = barcode.getUrl().getUrl();
                                        Log.d("QRCodeImageScanner", url);
                                        return;
                                    }
                                }
                            }

                            Log.e("QRCodeImageScanner", "URL is not included in the scanned QR code.");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("QRCodeImageScanner", e.getMessage());
                        }
                    });
        }
    }

    private static Uri getUri(String uri) {
        Uri parsed = Uri.parse(uri);

        if (parsed.getScheme() == null || parsed.getScheme().isEmpty()) {
            return Uri.fromFile(new File(uri));
        }

        return parsed;
    }
}
