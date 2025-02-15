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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddEmp extends AppCompatActivity {

    // Declare UI elements
    EditText edtImageLink, edtEmpName, edtEmpRole, edtEmpSalary;
    ImageView empImage;
    Button btnLoadImage, btnAddEmp;
    FirebaseFirestore firestore;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_emp);

        // Initialize Firebase
        firestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Check if the user is logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // If not logged in, navigate to LoginActivity
            startActivity(new Intent(AddEmp.this, Login.class));
            finish();
            return;
        }

        // Initialize UI elements
        edtImageLink = findViewById(R.id.imageLink);
        edtEmpName = findViewById(R.id.empName);
        edtEmpRole = findViewById(R.id.empRole);
        edtEmpSalary = findViewById(R.id.empSalary);
        empImage = findViewById(R.id.empImage);
        btnLoadImage = findViewById(R.id.btnLoadImage);
        btnAddEmp = findViewById(R.id.btnAddEmp);

        // Load Image from URL
        btnLoadImage.setOnClickListener(v -> {
            String imageUrl = edtImageLink.getText().toString().trim();
            if (!imageUrl.isEmpty()) {
                Glide.with(this).load(imageUrl).into(empImage);  // Use Glide to load image
            } else {
                Toast.makeText(this, "Enter a valid image link", Toast.LENGTH_SHORT).show();
            }
        });

        // Add Employee to Firestore
        btnAddEmp.setOnClickListener(v -> addEmployee());
    }

    private void addEmployee() {
        Log.d("FirestoreDebug", "addEmployee() called");

        String imageUrl = edtImageLink.getText().toString().trim();
        String empName = edtEmpName.getText().toString().trim();
        String empRole = edtEmpRole.getText().toString().trim();
        String empSalary = edtEmpSalary.getText().toString().trim();

        if (empName.isEmpty() || empRole.isEmpty() || empSalary.isEmpty() || imageUrl.isEmpty()) {
            Log.d("FirestoreDebug", "Empty fields detected");
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> employee = new HashMap<>();
        employee.put("empName", empName);
        employee.put("empRole", empRole);
        employee.put("empSalary", empSalary);
        employee.put("imageUrl", imageUrl);

        firestore.collection("Employees").add(employee)
                .addOnSuccessListener(documentReference -> {
                    Log.d("FirestoreDebug", "Employee added successfully with ID: " + documentReference.getId());
                    Toast.makeText(this, "Employee added!", Toast.LENGTH_SHORT).show();
                    clearFields();

                    // Navigate to the Details activity
                    Intent intent = new Intent(AddEmp.this, Details.class);
                    startActivity(intent);
                    finish();  // Close the AddEmp activity to prevent back navigation
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreDebug", "Failed to add employee", e);
                    Toast.makeText(this, "Failed to add employee", Toast.LENGTH_SHORT).show();
                });
    }

    // Method to clear input fields after adding employee
    private void clearFields() {
        edtImageLink.setText("");
        edtEmpName.setText("");
        edtEmpRole.setText("");
        edtEmpSalary.setText("");
    }
}
