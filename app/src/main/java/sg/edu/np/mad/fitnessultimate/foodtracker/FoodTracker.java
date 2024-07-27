package sg.edu.np.mad.fitnessultimate.foodtracker;

import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.widget.Button;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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
    }

    //Method is called when an activity lunched exists, giving back requestCode, resultCode, and other additional data if it exists
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Handles the result from the LogDetails activity
        if(requestCode == REQUEST_CODE_LOG_DETAILS && resultCode == RESULT_OK) {
                double bmr = data.getDoubleExtra("bmr", 0.0); //Get bmr value from esult
                totalCalories.setText(String.valueOf(bmr) + " kcals"); //Update total calories TextView
                dailyIntake.setText("0.0 kcals/ "); //Update daily intake TextView
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


}
