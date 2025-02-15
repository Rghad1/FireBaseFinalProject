package com.example.firebasefinalproject;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;


public class Details extends AppCompatActivity {

    LinearLayout detailsContent;
    FirebaseFirestore firestore;
    EditText searchBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_details);

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();

        // Locate the included top bar first
        View topBar = findViewById(R.id.topBarInclude); // Make sure to give the <include> an ID

        // Now find the addButton within the top bar
        ImageView addButton = topBar.findViewById(R.id.addButton);
        ImageView profile = topBar.findViewById(R.id.profileIcon);

        // Initialize UI elements
        detailsContent = findViewById(R.id.detailsContent);
        searchBar = findViewById(R.id.search_bar);

        // Fetch and display menu items
        fetchMenuItems();

        // Set onClickListener for the add button
        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(Details.this, AddMenu.class);
            startActivity(intent);
        });

        profile.setOnClickListener(v -> {
            Intent intent = new Intent(Details.this, EmpDetails.class);
            startActivity(intent);
        });


        // Search Filter Listener
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterItems(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterItems(String query) {
        for (int i = 0; i < detailsContent.getChildCount(); i++) {
            View itemView = detailsContent.getChildAt(i);
            TextView itemName = itemView.findViewById(R.id.menuItemName);

            if (itemName != null) {
                String name = itemName.getText().toString().toLowerCase();
                if (name.contains(query.toLowerCase())) {
                    itemView.setVisibility(View.VISIBLE);
                } else {
                    itemView.setVisibility(View.GONE);
                }
            }
        }
    }
    private void fetchMenuItems() {
        firestore.collection("MenuItems")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.w("FirestoreDebug", "Listen failed.", error);
                        return;
                    }

                    if (value != null && !value.isEmpty()) {
                        detailsContent.removeAllViews(); // Clear previous items

                        for (QueryDocumentSnapshot doc : value) {
                            String docId = doc.getId();
                            String menuName = doc.getString("menuName");
                            String menuPrice = doc.getString("menuPrice");
                            String imageUrl = doc.getString("imageUrl");

                            if (menuName != null && menuPrice != null && imageUrl != null) {
                                addMenuItemView(docId, menuName, menuPrice, imageUrl);
                            }
                        }
                    } else {
                        Toast.makeText(Details.this, "No menu items found.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addMenuItemView(String docId, String name, String price, String imageUrl) {
        View itemView = LayoutInflater.from(this).inflate(R.layout.menu_items, detailsContent, false);

        ImageView menuItemImage = itemView.findViewById(R.id.menuItemImage);
        TextView menuItemName = itemView.findViewById(R.id.menuItemName);
        TextView menuItemPrice = itemView.findViewById(R.id.menuItemPrice);
        Button updateButton = itemView.findViewById(R.id.updateButton);
        Button deleteButton = itemView.findViewById(R.id.deleteButton);

        menuItemName.setText(name);
        menuItemPrice.setText(price);
        Glide.with(this).load(imageUrl).into(menuItemImage);

        // Store docId in button tags
        updateButton.setTag(docId);
        deleteButton.setTag(docId);

        // Set onClickListener for update button
        updateButton.setOnClickListener(v -> {
            // Navigate to an update screen and pass details
            Intent intent = new Intent(Details.this, UpdateMenuActivity.class);
            intent.putExtra("docId", docId);
            intent.putExtra("menuName", name);
            intent.putExtra("menuPrice", price);
            intent.putExtra("imageUrl", imageUrl);
            startActivity(intent);
        });

        deleteButton.setOnClickListener(v -> deleteMenuItem(docId));

        detailsContent.addView(itemView);
    }


    private void deleteMenuItem(String docId) {
        firestore.collection("MenuItems").document(docId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(Details.this, "Item deleted", Toast.LENGTH_SHORT).show();
                    fetchMenuItems();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(Details.this, "Failed to delete", Toast.LENGTH_SHORT).show()
                );
    }

}
