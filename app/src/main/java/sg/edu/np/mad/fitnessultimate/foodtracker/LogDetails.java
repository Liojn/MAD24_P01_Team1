package sg.edu.np.mad.fitnessultimate.foodtracker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import sg.edu.np.mad.fitnessultimate.R;

public class LogDetails extends AppCompatActivity {

    private double calculatedBMR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_log_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Initializing views
        RadioGroup genderGroup = findViewById(R.id.genderRadio);
        EditText ageText = findViewById(R.id.ageText);
        EditText weightText = findViewById(R.id.weightText);
        EditText heightText = findViewById(R.id.heightText);
        EditText activityText = findViewById(R.id.activityText);
        RadioGroup goalsGroup = findViewById(R.id.radioOption);
        Button saveButton = findViewById(R.id.saveButton);
        Button cancelButton = findViewById(R.id.cancelButton);

        // Set a click listener for the cancel button
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Set a click listener for the save button
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get user input
                int selectedGenderId = genderGroup.getCheckedRadioButtonId();
                RadioButton selectedGenderRadioButton = findViewById(selectedGenderId);
                String gender = selectedGenderRadioButton.getText().toString();
                int age = Integer.parseInt(ageText.getText().toString());
                double weight = Double.parseDouble(weightText.getText().toString());
                double height = Double.parseDouble(heightText.getText().toString());
                String activityLevel = activityText.getText().toString();
                int selectedGoalId = goalsGroup.getCheckedRadioButtonId();
                RadioButton selectedGoalRadioButton = findViewById(selectedGoalId);
                String goal = selectedGoalRadioButton.getText().toString();

                // Calculate BMR based on gender
                double bmr;
                if (gender.equals("Male")) {
                    bmr = 88.362 + (13.397 * weight) + (4.799 * height) - (5.677 * age);
                } else {
                    bmr = 447.593 + (9.247 * weight) + (3.098 * height) - (4.330 * age);
                }

                // Adjust BMR based on activity level
                switch (activityLevel) {
                    case "no":
                        bmr *= 1.2;
                        break;
                    case "low":
                        bmr *= 1.375;
                        break;
                    case "moderate":
                        bmr *= 1.55;
                        break;
                    case "high":
                        bmr *= 1.725;
                        break;
                    case "extreme":
                        bmr *= 1.9;
                        break;

                    default:
                        bmr *= 1.2; // default
                        break;
                }

                // Adjust BMR based on goal
                if (goal.equals("Lose weight")) {
                    bmr -= 500; // Calorie adjustments for weight loss
                } else if (goal.equals("Gain weight")) {
                    bmr += 500; // Calorie adjustments for weight gain
                }
                // Formatting bmr to 1 decimal place and parsing back string to a double
                String formattedBMR = String.format("%.1f", bmr);
                calculatedBMR = Double.parseDouble(formattedBMR);
                returnCalculatedBMR(); //Calling the activity to return bmr
            }

            //Prepares the result intent with the bmr and finishes the activity
            private void returnCalculatedBMR() {
                Intent resultIntent = new Intent(); //Intent to hold the reuslt data
                resultIntent.putExtra("bmr", calculatedBMR);
                setResult(Activity.RESULT_OK, resultIntent); //Set the result of activity to RESULT_OK and attach the intent
                finish(); //Finish the activity and return to the previous one
            }
        });
    }
}