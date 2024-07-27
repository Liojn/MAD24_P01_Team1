package sg.edu.np.mad.fitnessultimate.foodtracker;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.Button;
import android.content.Intent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import sg.edu.np.mad.fitnessultimate.R;
import sg.edu.np.mad.fitnessultimate.calendarPage.BaseActivity;


public class FoodTracker extends BaseActivity {
    //UI elements
    private static final int REQUEST_CODE_LOG_DETAILS = 1;
    private TextView totalCalories;
    private TextView dailyIntake;
    private TextView textToday;
    private Handler handler = new Handler();
    private Runnable runnable;
    private MaterialButtonToggleGroup mealTypeToggle;
    private RecyclerView mealsRecyclerView;
    private MealAdapter mealAdapter;
    private List<Meal> mealList = new ArrayList<>();
    private ProgressBar progressBarCarbs, progressBarProtein, progressBarFats, progressBarOthers;
    private TextView textViewCarbs, textViewProtein, textViewFats, textViewOthers;

    //This method is called when the activity is first created.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); //Enables edge to edge display
        setContentView(R.layout.food_tracker); //Sets the content view to food_tracker layout
        //Handle system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Back button Intent
        findViewById(R.id.btnBack).setOnClickListener(v -> {
            onBackPressed();
        });


        //Set up for the date to update every hour
        textToday = findViewById(R.id.textToday);
        runnable = new Runnable() {
            @Override
            public void run() {
                textToday.setText(getCurrentDate());
                handler.postDelayed(this, 3600000);
            }
        };
        handler.post(runnable);


        //Finds Search Button in the food_tracker layout
        Button searchButton = findViewById(R.id.btnSearch);
        //Sets a click listener for the Search Button
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Create an intent to start the SearchResultsActivity
                Intent intent = new Intent(FoodTracker.this, SearchResultsActivity.class);
                //Starts the next activity
                startActivity(intent);
            }
        });

        //Find the add meal button in the food_tracker layout
        Button addButton = findViewById(R.id.addMeal);
        //Sets a click listener for the Search Button
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Create an intent to start the SearchResultsActivity
                Intent intent = new Intent(FoodTracker.this, SearchResultsActivity.class);
                //Starts the next activity
                startActivity(intent);
            }
        });

        //Finds the TextViews and log details button in the layout
        totalCalories = findViewById(R.id.totalCalories);
        Button logDetails = findViewById(R.id.btnlogDetails);
        dailyIntake = findViewById(R.id.dailyCalories);

        //Set a click listener for log details button
        logDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FoodTracker.this, LogDetails.class);
                startActivityForResult(intent, REQUEST_CODE_LOG_DETAILS);
            }
        });

        mealTypeToggle = findViewById(R.id.mealTypeToggle);
        mealsRecyclerView = findViewById(R.id.mealsRecyclerView);

        setupMealTypeToggle();
        setupRecyclerView();
        fetchMealsFromDatabase();

        progressBarCarbs = findViewById(R.id.progressBarCarbs);
        progressBarProtein = findViewById(R.id.progressBarProtein);
        progressBarFats = findViewById(R.id.progressBarFats);
        progressBarOthers = findViewById(R.id.progressBarOthers);

        textViewCarbs = findViewById(R.id.carbs);
        textViewProtein = findViewById(R.id.protein);
        textViewFats = findViewById(R.id.fats);
        textViewOthers = findViewById(R.id.others);

    }

    //Method is called when an activity lunched exists, giving back requestCode, resultCode, and other additional data if it exists
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Handles the result from the LogDetails activity
        if (requestCode == REQUEST_CODE_LOG_DETAILS && resultCode == RESULT_OK) {
            double bmr = data.getDoubleExtra("bmr", 0.0); //Get bmr value from result
            storeSuggestedCalorieIntake(bmr);
            totalCalories.setText(String.valueOf(bmr) + " kcals"); //Update total calories TextView
            updateDailyIntake(); //Update daily intake TextView
        }
    }

    //Method is called when the activity is destroyed
    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);//Remove the runnable callback to prevent memory leaks
    }

    //Method to get the current date in the specified format
    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy (EEEE)", Locale.getDefault());
        return sdf.format(new Date()); //Return the formatted date
    }

    private void setupMealTypeToggle() {
        mealTypeToggle.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                String mealType = getMealTypeFromButtonId(checkedId);
                updateMealList(mealType);
            }
        });
        // Set default selection
        mealTypeToggle.check(R.id.btnBreakfast);
    }

    private String getMealTypeFromButtonId(int buttonId) {
        if (buttonId == R.id.btnBreakfast) return "Breakfast";
        if (buttonId == R.id.btnLunch) return "Lunch";
        if (buttonId == R.id.btnDinner) return "Dinner";
        return "";
    }

    private void setupRecyclerView() {
        mealAdapter = new MealAdapter(mealList);
        mealsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mealsRecyclerView.setAdapter(mealAdapter);
    }

    private void fetchMealsFromDatabase() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DatabaseReference userMealsRef = FirebaseDatabase.getInstance().getReference()
                    .child("users")
                    .child(user.getUid())
                    .child("food_item");
            userMealsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    mealList.clear();
                    for (DataSnapshot mealSnapshot : dataSnapshot.getChildren()) {
                        Meal meal = mealSnapshot.getValue(Meal.class);
                        if (meal != null) {
                            mealList.add(meal);
                        }
                    }
                    updateMealList(getMealTypeFromButtonId(mealTypeToggle.getCheckedButtonId()));
                    updateDailyIntake();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(FoodTracker.this, "Failed to load meals", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void updateMealList(String mealType) {
        List<Meal> filteredMeals = mealList.stream()
                .filter(meal -> meal.mealType.equals(mealType))
                .collect(Collectors.toList());
        mealAdapter.updateMeals(filteredMeals);
    }

    private void storeSuggestedCalorieIntake(double suggestedIntake) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                    .child("users")
                    .child(user.getUid());
            userRef.child("suggested_calorie_intake").setValue(suggestedIntake);
        }
    }

    private void updateDailyIntake() {
        double totalIntake = mealList.stream()
                .mapToDouble(Meal::getCalories)
                .sum();
        dailyIntake.setText(String.format(Locale.getDefault(), "%.1f kcals / ", totalIntake));

        // Fetch suggested intake from Firebase
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                    .child("users")
                    .child(user.getUid());
            userRef.child("suggested_calorie_intake").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Double suggestedIntake = dataSnapshot.getValue(Double.class);
                    if (suggestedIntake != null) {
                        totalCalories.setText(String.format(Locale.getDefault(), "%.1f kcals", suggestedIntake));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(FoodTracker.this, "Failed to load suggested intake", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void updateNutrientInfo() {
        double totalCarbs = 0, totalProtein = 0, totalFats = 0, totalOthers = 0;
        for (Meal meal : mealList) {
            totalCarbs += meal.getCarbs();
            totalProtein += meal.getProtein();
            totalFats += meal.getFats();
            totalOthers += meal.getOthers();
        }

        double totalNutrients = totalCarbs + totalProtein + totalFats + totalOthers;

        updateProgressBar(progressBarCarbs, textViewCarbs, totalCarbs, totalNutrients, "Carbs");
        updateProgressBar(progressBarProtein, textViewProtein, totalProtein, totalNutrients, "Protein");
        updateProgressBar(progressBarFats, textViewFats, totalFats, totalNutrients, "Fats");
        updateProgressBar(progressBarOthers, textViewOthers, totalOthers, totalNutrients, "Others");
    }

    private void updateProgressBar(ProgressBar progressBar, TextView textView, double value, double total, String label) {
        int percentage = (int) ((value / total) * 100);
        progressBar.setProgress(percentage);
        textView.setText(String.format(Locale.getDefault(), "%s: %.1fg", label, value));

        // Set color based on percentage
        int color = getColorForPercentage(percentage);
        progressBar.getProgressDrawable().setColorFilter(color, PorterDuff.Mode.SRC_IN);
    }

    private int getColorForPercentage(int percentage) {
        if (percentage < 20) return Color.RED;
        else if (percentage < 40) return Color.YELLOW;
        else if (percentage < 60) return Color.GREEN;
        else if (percentage < 80) return Color.BLUE;
        else return Color.MAGENTA;
    }
}
