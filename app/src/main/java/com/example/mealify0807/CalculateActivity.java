package com.example.mealify0807;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class CalculateActivity extends AppCompatActivity {

    private boolean isDarkModeEnabled;
    private List<DocumentSnapshot> foodList;
    private FirebaseFirestore db;
    private String userUID;
    private EditText searchEditText;
    private TextView foodListTextView;
    private StringBuilder foodNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculate);

        isDarkModeEnabled = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES;
        setAppTheme();

        db = FirebaseFirestore.getInstance();
        foodList = new ArrayList<>();
        foodNames = new StringBuilder();

        // Get the user UID if available
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        userUID = (user != null) ? user.getUid() : "";

        searchEditText = findViewById(R.id.searchEditText); // Initialize the searchEditText
        foodListTextView = findViewById(R.id.foodListTextView); // Initialize the foodListTextView
    }

    private void setAppTheme() {
        if (isDarkModeEnabled) {
            setTheme(R.style.Theme_Mealify0807_Dark);
        } else {
            setTheme(R.style.Theme_Mealify0807);
        }
    }

    public void addFood(View view) {
        String searchQuery = searchEditText.getText().toString().trim();

        if (searchQuery.isEmpty()) {
            Toast.makeText(this, "Please enter a food name to search", Toast.LENGTH_SHORT).show();
            return;
        }

        Query foodQuery = db.collection("Food")
                .whereEqualTo("Name", searchQuery);

        foodQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();
                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                        foodList.add(document);
                        Toast.makeText(CalculateActivity.this, "Food added: " + searchQuery, Toast.LENGTH_SHORT).show();
                        foodNames.append(searchQuery).append("\n");
                        searchEditText.setText("");
                        updateFoodList();
                    } else {
                        // If not found in the "Food" collection, search in "Food - userUID" collection
                        Query userFoodQuery = db.collection("Food - " + userUID)
                                .whereEqualTo("Name", searchQuery);

                        userFoodQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    QuerySnapshot querySnapshot = task.getResult();
                                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                        DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                                        foodList.add(document);
                                        Toast.makeText(CalculateActivity.this, "Food added: " + searchQuery, Toast.LENGTH_SHORT).show();
                                        foodNames.append(searchQuery).append("\n");
                                        searchEditText.setText("");
                                        updateFoodList();
                                    } else {
                                        Toast.makeText(CalculateActivity.this, "Food not found", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(CalculateActivity.this, "Error: " + task.getException(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                } else {
                    Toast.makeText(CalculateActivity.this, "Error: " + task.getException(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void calculateTotal(View view) {
        if (foodList.size() < 1) {
            Toast.makeText(this, "Please add at least one food item", Toast.LENGTH_SHORT).show();
            return;
        }

        float totalCalories = 0;
        float totalProtein = 0;
        float totalFat = 0;
        float totalCarbohydrates = 0;

        for (DocumentSnapshot document : foodList) {
            float calories = document.getLong("Calories").floatValue();
            float protein = document.getLong("Protein").floatValue();
            float fat = document.getLong("Fat").floatValue();
            float carbohydrates = document.getLong("Carbohydrates").floatValue();

            totalCalories += calories;
            totalProtein += protein;
            totalFat += fat;
            totalCarbohydrates += carbohydrates;
        }

        String foodInfo = "Total Calories: " + totalCalories + " kcal" + "\n"
                + "Total Protein: " + totalProtein + " g" + "\n"
                + "Total Fat: " + totalFat + " g" + "\n"
                + "Total Carbohydrates: " + totalCarbohydrates + " g";

        if (foodNames.length() > 0) {
            foodNames.insert(0, "Food List:\n");
            foodInfo = foodNames.toString() + "\n" + foodInfo;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(CalculateActivity.this);
        builder.setTitle("Total Nutritional Facts");
        builder.setMessage(foodInfo);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setIcon(R.drawable.food_display_icon);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void clearList(View view) {
        foodList.clear();
        foodNames.setLength(0);
        updateFoodList();
        Toast.makeText(this, "Food list cleared", Toast.LENGTH_SHORT).show();
    }

    private void updateFoodList() {
        String foodListText = foodNames.toString();
        foodListTextView.setText(foodListText);
    }
}
