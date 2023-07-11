package com.example.mealify0807;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.PopupMenu;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseUser user;
    private boolean isDarkModeEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        isDarkModeEnabled = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES;
        setAppTheme();
    }

    private void setAppTheme() {
        if (isDarkModeEnabled) {
            setTheme(R.style.Theme_Mealify0807_Dark);
        } else {
            setTheme(R.style.Theme_Mealify0807);
        }
    }

    public void showDropdownMenu(View v) {
        if (v.getId() == R.id.circularButton2) {
            Button circularButton = findViewById(R.id.circularButton2);

            PopupMenu popupMenu = new PopupMenu(MainActivity.this, circularButton);
            popupMenu.getMenuInflater().inflate(R.menu.menu_main, popupMenu.getMenu());

            if (user != null) {
                popupMenu.getMenu().findItem(R.id.menu_login).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_logout).setVisible(true);
                popupMenu.getMenu().findItem(R.id.menu_check_food).setVisible(true);
            } else {
                popupMenu.getMenu().findItem(R.id.menu_login).setVisible(true);
                popupMenu.getMenu().findItem(R.id.menu_logout).setVisible(false);
                popupMenu.getMenu().findItem(R.id.menu_check_food).setVisible(false);
            }

            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    int itemId = item.getItemId();
                    if (itemId == R.id.menu_login) {
                        launchLogin(v);
                        return true;
                    } else if (itemId == R.id.menu_logout) {
                        handleLogout();
                        return true;
                    } else if (itemId == R.id.menu_check_food) {
                        handleCheckFood();
                        return true;
                    } else if (itemId == R.id.menu_settings) {
                        handleSettings();
                        return true;
                    }
                    return false;
                }
            });

            popupMenu.show();
        }
    }

    public void launchLogin(View v) {
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
    }

    public void handleLogout() {
        auth.signOut();
        user = null;
        Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
        invalidateOptionsMenu();
    }

    public void handleCheckFood() {
        if (user != null) {
            Intent intent = new Intent(MainActivity.this, AddCustomFoodActivity.class);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Food not found", Toast.LENGTH_SHORT).show();
        }
    }

    public void handleSettings() {
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    public void searchFood(View v) {
        EditText searchEditText = findViewById(R.id.searchEditText);
        String searchQuery = searchEditText.getText().toString().trim();

        if (searchQuery.isEmpty()) {
            Toast.makeText(this, "Please enter a food name to search", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query foodQuery = db.collection("Food")
                .whereEqualTo("Name", searchQuery);

        foodQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();
                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                        displayFoodInfo(document);
                    } else {
                        String capitalizedQuery = searchQuery.substring(0, 1).toUpperCase() + searchQuery.substring(1);
                        Query foodQueryCapitalized = db.collection("Food")
                                .whereEqualTo("Name", capitalizedQuery);

                        foodQueryCapitalized.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    QuerySnapshot querySnapshot = task.getResult();
                                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                        DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                                        displayFoodInfo(document);
                                    } else {
                                        if (user != null) {
                                            Query userFoodQueryCapitalized = db.collection("Food - " + user.getUid())
                                                    .whereEqualTo("Name", capitalizedQuery);

                                            userFoodQueryCapitalized.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        QuerySnapshot querySnapshot = task.getResult();
                                                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                                            DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                                                            displayFoodInfo(document);
                                                        } else {
                                                            Toast.makeText(MainActivity.this, "Food not found", Toast.LENGTH_SHORT).show();
                                                        }
                                                    } else {
                                                        Toast.makeText(MainActivity.this, "Error: " + task.getException(), Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                        } else {
                                            Toast.makeText(MainActivity.this, "Food not found", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                } else {
                                    Toast.makeText(MainActivity.this, "Error: " + task.getException(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Error: " + task.getException(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void displayFoodInfo(DocumentSnapshot document) {
        String name = document.getString("Name");
        float calories = document.getLong("Calories").floatValue();
        float protein = document.getLong("Protein").floatValue();
        float fat = document.getLong("Fat").floatValue();
        float carbohydrates = document.getLong("Carbohydrates").floatValue();

        String foodInfo = "Calories: " + calories + " kcal" + "\n"
                + "Protein: " + protein + " g" + "\n"
                + "Fat: " + fat + " g" + "\n"
                + "Carbohydrates: " + carbohydrates + " g";

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(name + " nutritional facts" + "\n(100g)");
        builder.setMessage(foodInfo);
        builder.setPositiveButton("OK", null);
        builder.setIcon(R.drawable.food_display_icon);

        AlertDialog dialog = builder.create();
        dialog.show();
    }
    public void openCalculateActivity(View view) {
        Intent intent = new Intent(this, CalculateActivity.class);
        startActivity(intent);
    }
}
