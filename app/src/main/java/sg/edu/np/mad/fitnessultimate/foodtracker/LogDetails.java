package sg.edu.np.mad.fitnessultimate.foodtracker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import sg.edu.np.mad.fitnessultimate.R;
import sg.edu.np.mad.fitnessultimate.calendarPage.BaseActivity;

public class LogDetails extends BaseActivity {

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

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get user input
                int selectedGenderId = genderGroup.getCheckedRadioButtonId();
                RadioButton selectedGenderRadioButton = findViewById(selectedGenderId);
                String gender = selectedGenderRadioButton != null ? selectedGenderRadioButton.getText().toString() : "";

                String ageString = ageText.getText().toString();
                String weightString = weightText.getText().toString();
                String heightString = heightText.getText().toString();
                String activityLevel = activityText.getText().toString();
                int selectedGoalId = goalsGroup.getCheckedRadioButtonId();
                RadioButton selectedGoalRadioButton = findViewById(selectedGoalId);
                String goal = selectedGoalRadioButton != null ? selectedGoalRadioButton.getText().toString() : "";

                if (gender.isEmpty() || ageString.isEmpty() || weightString.isEmpty() || heightString.isEmpty() || activityLevel.isEmpty() || goal.isEmpty()) {
                    // Show an error message (e.g., a Toast) to the user indicating that all fields are required
                    Toast.makeText(LogDetails.this, "All fields are required", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Validate age
                int age;
                try {
                    age = Integer.parseInt(ageString);
                    if (age < 13 || age > 120) {
                        Toast.makeText(LogDetails.this, "Age must be between 13 and 120", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(LogDetails.this, "Invalid age format", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Validate weight
                double weight;
                try {
                    weight = Double.parseDouble(weightString);
                    if (weight < 30 || weight > 300) {
                        Toast.makeText(LogDetails.this, "Weight must be between 30 and 300 kg", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(LogDetails.this, "Invalid weight format", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Validate height
                double height;
                try {
                    height = Double.parseDouble(heightString);
                    if (height < 100 || height > 250) {
                        Toast.makeText(LogDetails.this, "Height must be between 100 and 250 cm", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(LogDetails.this, "Invalid height format", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Calculate BMR based on gender
                double bmr;
                if (gender.equals("Male")) {
                    bmr = 88.362 + (13.397 * weight) + (4.799 * height) - (5.677 * age);
                } else if (gender.equals("Female")) {
                    bmr = 447.593 + (9.247 * weight) + (3.098 * height) - (4.330 * age);
                } else {
                    Toast.makeText(LogDetails.this, "Invalid gender selection", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Validate activity level
                if (!activityLevel.equals("no") && !activityLevel.equals("low") && !activityLevel.equals("moderate") &&
                        !activityLevel.equals("high") && !activityLevel.equals("extreme")) {
                    Toast.makeText(LogDetails.this, "Invalid activity level", Toast.LENGTH_SHORT).show();
                    return;
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
                } else {
                    Toast.makeText(LogDetails.this, "Invalid goal selection", Toast.LENGTH_SHORT).show();
                    return;
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
    }}