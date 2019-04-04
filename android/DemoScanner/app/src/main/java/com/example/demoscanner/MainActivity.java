package com.example.demoscanner;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.hardware.camera2.CameraAccessException;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.otaliastudios.cameraview.Audio;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.Frame;
import com.otaliastudios.cameraview.FrameProcessor;
import com.otaliastudios.cameraview.Gesture;
import com.otaliastudios.cameraview.GestureAction;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

public class MainActivity extends AppCompatActivity {

    final FirebaseVisionBarcodeDetectorOptions options = new  FirebaseVisionBarcodeDetectorOptions.Builder() .setBarcodeFormats(FirebaseVisionBarcode.FORMAT_ALL_FORMATS).build();
    FirebaseVisionBarcodeDetector detector = FirebaseVision.getInstance().getVisionBarcodeDetector(options);
    volatile boolean isRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final CameraView cv = findViewById(R.id.cameraview);

        cv.addFrameProcessor(new FrameProcessor() {
            @Override
            public void process(@NonNull final Frame frame) {
                if (!isRunning) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            scanImage(frame.getData(), frame.getRotation(), frame.getSize().getWidth(), frame.getSize().getHeight());
                        }
                    });
                }
            }
        });
        cv.setAudio(Audio.OFF);
        cv.mapGesture(Gesture.TAP, GestureAction.FOCUS_WITH_MARKER);
        cv.setLifecycleOwner(this);
    }

    private int getRotationCompensation(int rotation) {
        switch (rotation) {
            case 0 : return  FirebaseVisionImageMetadata.ROTATION_0;
            case 90 : return  FirebaseVisionImageMetadata.ROTATION_90;
            case 180 : return  FirebaseVisionImageMetadata.ROTATION_180;
            case 270 : return  FirebaseVisionImageMetadata.ROTATION_270;
            default: return FirebaseVisionImageMetadata.ROTATION_0;
        }
    }

    void scanImage(byte[] ba, int rotation, int width, int height ) {
        final FirebaseVisionImageMetadata metadata = new FirebaseVisionImageMetadata.Builder()
                .setWidth(width)
                .setHeight(height)
                .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
                .setRotation(getRotationCompensation(rotation))
                .build();
        detector.detectInImage(FirebaseVisionImage.fromByteArray(ba, metadata)).addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionBarcode>>() {
            @Override
            public void onSuccess(List<FirebaseVisionBarcode> firebaseVisionBarcodes) {
                isRunning = false;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Scanned", Toast.LENGTH_LONG).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                isRunning = false;
            }
        });
    }

}
