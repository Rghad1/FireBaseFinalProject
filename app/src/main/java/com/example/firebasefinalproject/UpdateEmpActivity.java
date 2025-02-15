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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class UpdateEmpActivity extends AppCompatActivity {

    EditText edtImageLink, edtEmpName, edtEmpRole, edtEmpSalary;
    ImageView empImage;
    Button btnLoadImage, btnUpdateEmployee;
    FirebaseFirestore firestore;

    String docId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_emp); // Reusing the AddEmp layout

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();

        // Initialize UI elements
        edtImageLink = findViewById(R.id.imageLink);
        edtEmpName = findViewById(R.id.empName);
        edtEmpRole = findViewById(R.id.empRole);
        edtEmpSalary = findViewById(R.id.empSalary);
        empImage = findViewById(R.id.empImage);
        btnLoadImage = findViewById(R.id.btnLoadImage);
        btnUpdateEmployee = findViewById(R.id.btnAddEmp); // Reusing Add button for Update

        // Get passed data from Intent
        docId = getIntent().getStringExtra("empId"); // Corrected to empId
        String empName = getIntent().getStringExtra("empName");
        String empRole = getIntent().getStringExtra("empRole");
        String empSalary = getIntent().getStringExtra("empSalary");
        String imageUrl = getIntent().getStringExtra("imageUrl");

        // Set existing data in EditTexts
        edtEmpName.setText(empName);
        edtEmpRole.setText(empRole);
        edtEmpSalary.setText(empSalary);
        edtImageLink.setText(imageUrl);

        // Load existing image using Glide
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this).load(imageUrl).into(empImage);
        }

        // Load Image from URL Button Click
        btnLoadImage.setOnClickListener(v -> {
            String imageUrlInput = edtImageLink.getText().toString().trim();
            if (!imageUrlInput.isEmpty()) {
                Glide.with(this).load(imageUrlInput).into(empImage);
            } else {
                Toast.makeText(this, "Enter a valid image link", Toast.LENGTH_SHORT).show();
            }
        });

        // Update Employee in Firestore
        btnUpdateEmployee.setText("Update Employee"); // Change button text for clarity
        btnUpdateEmployee.setOnClickListener(v -> updateEmployee());
    }

    private void updateEmployee() {
        String imageUrl = edtImageLink.getText().toString().trim();
        String empName = edtEmpName.getText().toString().trim();
        String empRole = edtEmpRole.getText().toString().trim();
        String empSalary = edtEmpSalary.getText().toString().trim();

        if (empName.isEmpty() || empRole.isEmpty() || empSalary.isEmpty() || imageUrl.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updatedEmployee = new HashMap<>();
        updatedEmployee.put("empName", empName);
        updatedEmployee.put("empRole", empRole);
        updatedEmployee.put("empSalary", empSalary);
        updatedEmployee.put("imageUrl", imageUrl);

        firestore.collection("Employees").document(docId)
                .update(updatedEmployee)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(UpdateEmpActivity.this, "Employee updated!", Toast.LENGTH_SHORT).show();

                    // Navigate back to the Details activity
                    Intent intent = new Intent(UpdateEmpActivity.this, EmpDetails.class);
                    startActivity(intent);
                    finish(); // Close UpdateEmpActivity
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(UpdateEmpActivity.this, "Failed to update employee", Toast.LENGTH_SHORT).show();
                });
    }

}
