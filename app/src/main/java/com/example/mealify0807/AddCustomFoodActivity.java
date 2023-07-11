package com.example.mealify0807;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddCustomFoodActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseUser user;

    private EditText nameEditText;
    private EditText caloriesEditText;
    private EditText proteinEditText;
    private EditText fatEditText;
    private EditText carbohydratesEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_custom_food);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        nameEditText = findViewById(R.id.foodNameEditText);
        caloriesEditText = findViewById(R.id.caloriesEditText);
        proteinEditText = findViewById(R.id.proteinEditText);
        fatEditText = findViewById(R.id.fatEditText);
        carbohydratesEditText = findViewById(R.id.carbohydratesEditText);

        Button addFoodButton = findViewById(R.id.addFoodButton);
        addFoodButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addCustomFood();
            }
        });
    }

    private void addCustomFood() {
        String name = nameEditText.getText().toString().trim();
        String caloriesStr = caloriesEditText.getText().toString().trim();
        String proteinStr = proteinEditText.getText().toString().trim();
        String fatStr = fatEditText.getText().toString().trim();
        String carbohydratesStr = carbohydratesEditText.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(caloriesStr) ||
                TextUtils.isEmpty(proteinStr) || TextUtils.isEmpty(fatStr) ||
                TextUtils.isEmpty(carbohydratesStr)) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        float calories = Float.parseFloat(caloriesStr);
        float protein = Float.parseFloat(proteinStr);
        float fat = Float.parseFloat(fatStr);
        float carbohydrates = Float.parseFloat(carbohydratesStr);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String collectionName = "Food - " + user.getUid();

        Map<String, Object> foodData = new HashMap<>();
        foodData.put("Name", name);
        foodData.put("Calories", calories);
        foodData.put("Protein", protein);
        foodData.put("Fat", fat);
        foodData.put("Carbohydrates", carbohydrates);

        db.collection(collectionName)
                .add(foodData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(AddCustomFoodActivity.this, "Custom food added successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AddCustomFoodActivity.this, "Failed to add custom food: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
