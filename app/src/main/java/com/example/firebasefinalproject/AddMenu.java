package com.example.firebasefinalproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;
import java.util.Map;

public class AddMenu extends AppCompatActivity {

    // Declare UI elements
    EditText edtImageLink, edtMenuName, edtMenuPrice;
    ImageView menuImage;
    Button btnLoadImage, btnAddMenu;
    FirebaseFirestore firestore;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_menu);

        // Initialize Firebase
        firestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Check if the user is logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // If not logged in, navigate to LoginActivity
            startActivity(new Intent(AddMenu.this, Login.class));
            finish();
            return;
        }

        // Initialize UI elements
        edtImageLink = findViewById(R.id.imageLink);
        edtMenuName = findViewById(R.id.menuName);
        edtMenuPrice = findViewById(R.id.menuPrice);
        menuImage = findViewById(R.id.menuImage);
        btnLoadImage = findViewById(R.id.btnLoadImage);
        btnAddMenu = findViewById(R.id.btnAddMenu);

        // Load Image from URL
        btnLoadImage.setOnClickListener(v -> {
            String imageUrl = edtImageLink.getText().toString().trim();
            if (!imageUrl.isEmpty()) {
                Glide.with(this).load(imageUrl).into(menuImage);  // Use Glide to load image
            } else {
                Toast.makeText(this, "Enter a valid image link", Toast.LENGTH_SHORT).show();
            }
        });

        // Add Menu Item to Firestore
        btnAddMenu.setOnClickListener(v -> addMenuItem());
    }

    private void addMenuItem() {
        Log.d("FirestoreDebug", "addMenuItem() called");

        String imageUrl = edtImageLink.getText().toString().trim();
        String menuName = edtMenuName.getText().toString().trim();
        String menuPrice = edtMenuPrice.getText().toString().trim();

        if (menuName.isEmpty() || menuPrice.isEmpty() || imageUrl.isEmpty()) {
            Log.d("FirestoreDebug", "Empty fields detected");
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> menuItem = new HashMap<>();
        menuItem.put("menuName", menuName);
        menuItem.put("menuPrice", menuPrice);
        menuItem.put("imageUrl", imageUrl);

        firestore.collection("MenuItems").add(menuItem)
                .addOnSuccessListener(documentReference -> {
                    Log.d("FirestoreDebug", "Item added successfully with ID: " + documentReference.getId());
                    Toast.makeText(this, "Menu item added!", Toast.LENGTH_SHORT).show();
                    clearFields();
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreDebug", "Failed to add item", e);
                    Toast.makeText(this, "Failed to add item", Toast.LENGTH_SHORT).show();
                });
    }


    // Method to clear input fields after adding menu item
    private void clearFields() {
        edtImageLink.setText("");
        edtMenuName.setText("");
        edtMenuPrice.setText("");
    }
}
