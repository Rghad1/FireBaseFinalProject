package com.example.firebasefinalproject;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class UpdateMenuActivity extends AppCompatActivity {

    EditText edtImageLink, edtMenuName, edtMenuPrice;
    ImageView menuImage;
    Button btnLoadImage, btnUpdateMenu;
    FirebaseFirestore firestore;

    String docId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_menu); // Use the same layout as AddMenu

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();

        // Initialize UI elements
        edtImageLink = findViewById(R.id.imageLink);
        edtMenuName = findViewById(R.id.menuName);
        edtMenuPrice = findViewById(R.id.menuPrice);
        menuImage = findViewById(R.id.menuImage);
        btnLoadImage = findViewById(R.id.btnLoadImage);
        btnUpdateMenu = findViewById(R.id.btnAddMenu); // Update button (same as AddMenu)

        // Get passed data from Intent
        docId = getIntent().getStringExtra("docId");
        String menuName = getIntent().getStringExtra("menuName");
        String menuPrice = getIntent().getStringExtra("menuPrice");
        String imageUrl = getIntent().getStringExtra("imageUrl");

        // Set existing data
        edtMenuName.setText(menuName);
        edtMenuPrice.setText(menuPrice);
        edtImageLink.setText(imageUrl);
        Glide.with(this).load(imageUrl).into(menuImage);

        // Load Image from URL
        btnLoadImage.setOnClickListener(v -> {
            String imageUrlInput = edtImageLink.getText().toString().trim();
            if (!imageUrlInput.isEmpty()) {
                Glide.with(this).load(imageUrlInput).into(menuImage);
            } else {
                Toast.makeText(this, "Enter a valid image link", Toast.LENGTH_SHORT).show();
            }
        });

        // Update Menu Item in Firestore
        btnUpdateMenu.setOnClickListener(v -> updateMenuItem());
    }

    private void updateMenuItem() {
        String imageUrl = edtImageLink.getText().toString().trim();
        String menuName = edtMenuName.getText().toString().trim();
        String menuPrice = edtMenuPrice.getText().toString().trim();

        if (menuName.isEmpty() || menuPrice.isEmpty() || imageUrl.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updatedMenuItem = new HashMap<>();
        updatedMenuItem.put("menuName", menuName);
        updatedMenuItem.put("menuPrice", menuPrice);
        updatedMenuItem.put("imageUrl", imageUrl);

        firestore.collection("MenuItems").document(docId)
                .update(updatedMenuItem)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(UpdateMenuActivity.this, "Menu item updated!", Toast.LENGTH_SHORT).show();
                    finish(); // Close the activity after update
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(UpdateMenuActivity.this, "Failed to update item", Toast.LENGTH_SHORT).show();
                });
    }
}
