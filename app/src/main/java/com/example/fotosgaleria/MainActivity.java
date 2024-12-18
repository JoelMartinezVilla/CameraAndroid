package com.example.fotosgaleria;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private ImageView imageView;
    private ActivityResultLauncher<Intent> activityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar las vistas
        imageView = findViewById(R.id.imageView);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP); // Ajustar automáticamente sin distorsionar
        Button button_gallery = findViewById(R.id.button_gallery);
        Button button_camera_normal = findViewById(R.id.button_camera);
        Button button_camera_full_size = findViewById(R.id.button_full_size);

        // Configurar el lanzador para manejar resultados
        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        if (result.getData().getExtras() != null && result.getData().getExtras().get("data") != null) {
                            // Resultado de la cámara
                            Bitmap photo = (Bitmap) result.getData().getExtras().get("data");
                            imageView.setImageBitmap(photo);
                        } else if (result.getData().getData() != null) {
                            // Resultado de la galería
                            Uri selectedImageUri = result.getData().getData();
                            try {
                                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                                imageView.setImageBitmap(bitmap);
                            } catch (IOException e) {
                                Log.e("MainActivity", "Error al cargar la imagen: " + e.getMessage());
                            }
                        }
                    } else {
                        Log.d("MainActivity", "No se seleccionó ninguna imagen o se canceló la operación.");
                    }
                }
        );

        // Configurar el evento del botón de la galería
        button_gallery.setOnClickListener(v -> {
            // Crear un Intent para abrir la galería
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            activityResultLauncher.launch(intent);
        });

        // Configurar el evento del botón de la cámara normal
        button_camera_normal.setOnClickListener(v -> {
            if (checkCameraPermission()) {
                openCamera();
            } else {
                requestCameraPermission();
            }
        });

        // Configurar el evento del botón de la cámara para ver a tamaño real
        button_camera_full_size.setOnClickListener(v -> {
            if (checkCameraPermission()) {
                openCameraForFullSize();
            } else {
                requestCameraPermission();
            }
        });
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra("full_size", false);
        activityResultLauncher.launch(intent);
    }

    private void openCameraForFullSize() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra("full_size", true);
        activityResultLauncher.launch(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Log.d("MainActivity", "Permiso de cámara denegado.");
            }
        }
    }
}
