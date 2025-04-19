package com.example.firebasetesting;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class AddData extends AppCompatActivity {

    EditText etName, etPrice, etDescription;
    Button btnSubmit;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_data); // Your XML file name

        // Initialize views
        etName = findViewById(R.id.name);
        etPrice = findViewById(R.id.price);
        etDescription = findViewById(R.id.description);
        btnSubmit = findViewById(R.id.btnSubmit);

        // Get Firebase DB reference
        databaseReference = FirebaseDatabase.getInstance().getReference("Shopping");

        // Submit button listener
        btnSubmit.setOnClickListener(v -> insertData());
    }

    private void insertData() {
        String name = etName.getText().toString().trim();
        String price = etPrice.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (name.isEmpty() || price.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Creating HashMap for Firebase
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("price", price);
        map.put("description", description);

        // Push data to Firebase
        databaseReference.push().setValue(map)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Data added successfully", Toast.LENGTH_SHORT).show();
                    etName.setText("");
                    etPrice.setText("");
                    etDescription.setText("");
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}
