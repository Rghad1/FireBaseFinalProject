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

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class EmpDetails extends AppCompatActivity {

    private LinearLayout empDetailsContainer;
    private EditText searchBar;
    private ImageView empAdd, addButton;
    private FirebaseFirestore db;
    private List<QueryDocumentSnapshot> employeeList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emp_details);

        // Initialize views from XML
        empDetailsContainer = findViewById(R.id.empdetails);
        searchBar = findViewById(R.id.search_bar);
        empAdd = findViewById(R.id.empadd);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Locate the included top bar first
        View topBar = findViewById(R.id.topBarInclude); // Make sure to give the <include> an ID

        // Now find the addButton within the top bar
         addButton = topBar.findViewById(R.id.addButton);


        // Fetch employee data from Firestore
        fetchEmployees();

        // Add listener to search bar for filtering
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Filter employees based on search input
                filterEmployees(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not needed
            }
        });

        // Floating Add Button click listener
        empAdd.setOnClickListener(v -> {
            // Redirect to Add Employee activity
            Intent intent = new Intent(EmpDetails.this, AddEmp.class);
            startActivity(intent);
        });

        addButton.setOnClickListener(v -> {
            Log.d("EmpDetails", "Add button clicked");
            Intent intent = new Intent(EmpDetails.this, AddMenu.class);
            startActivity(intent);
        });

    }

    private void fetchEmployees() {
        empDetailsContainer.removeAllViews(); // Clear container before fetching
        db.collection("Employees")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null) {
                            employeeList.clear(); // Clear previous data
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                employeeList.add(document);
                            }
                            // Display all employees initially
                            displayEmployees(employeeList);
                        }
                    } else {
                        Toast.makeText(EmpDetails.this, "Error fetching employees: " +
                                task.getException(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void displayEmployees(List<QueryDocumentSnapshot> list) {
        empDetailsContainer.removeAllViews(); // Clear existing views

        for (QueryDocumentSnapshot document : list) {
            // Get employee data
            final String empId = document.getId();
            final String name = document.getString("empName") != null ? document.getString("empName") : "No Name";
            final String role = document.getString("empRole") != null ? document.getString("empRole") : "No Role";
            final String salary = (document.get("empSalary") != null) ? document.get("empSalary").toString() : "No Salary";
            final String imageUrl = document.getString("imageUrl") != null ? document.getString("imageUrl") : "";

            // Inflate employee item layout
            View employeeItem = LayoutInflater.from(EmpDetails.this)
                    .inflate(R.layout.emp_items, empDetailsContainer, false);

            // Get references to views
            TextView empName = employeeItem.findViewById(R.id.empName);
            TextView empRole = employeeItem.findViewById(R.id.empRole);
            TextView empSalary = employeeItem.findViewById(R.id.empSalary);
            ImageView empImage = employeeItem.findViewById(R.id.empImage);
            Button btnUpdate = employeeItem.findViewById(R.id.btnUpdate);
            Button btnDelete = employeeItem.findViewById(R.id.btnDelete);

            // Set employee details
            empName.setText(name);
            empRole.setText(role);
            empSalary.setText(salary);

            // Load image using Glide
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(EmpDetails.this)
                        .load(imageUrl)
                        .into(empImage);
            }

            // Update button logic
            btnUpdate.setOnClickListener(v -> {
                Intent intent = new Intent(EmpDetails.this, UpdateEmpActivity.class);
                intent.putExtra("empId", empId);
                intent.putExtra("empName", name);
                intent.putExtra("empRole", role);
                intent.putExtra("empSalary", salary);
                intent.putExtra("imageUrl", imageUrl);
                startActivity(intent);
            });

            // Delete button logic
            btnDelete.setOnClickListener(v -> {
                db.collection("Employees").document(empId)
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(EmpDetails.this, "Employee deleted", Toast.LENGTH_SHORT).show();
                            empDetailsContainer.removeView(employeeItem);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(EmpDetails.this, "Error deleting employee", Toast.LENGTH_SHORT).show();
                        });
            });

            // Add employee item to container
            empDetailsContainer.addView(employeeItem);
        }
    }

    private void filterEmployees(String query) {
        List<QueryDocumentSnapshot> filteredList = new ArrayList<>();
        for (QueryDocumentSnapshot document : employeeList) {
            String name = document.getString("empName");
            if (name != null && name.toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(document);
            }
        }
        displayEmployees(filteredList);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh employee list when returning from another activity
        fetchEmployees();
    }
}
