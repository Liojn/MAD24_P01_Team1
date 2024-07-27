package sg.edu.np.mad.fitnessultimate.foodtracker;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import sg.edu.np.mad.fitnessultimate.R;
import sg.edu.np.mad.fitnessultimate.calendarPage.BaseActivity;

public class SearchResultsActivity extends BaseActivity {

    private TextView resultTextBox, selectedDateText;
    private SearchView searchView;
    private Button backBtn, addButton, selectDateButton;
    private LinearLayout resultLayout;
    private PopupWindow popupWindow;
    private RadioGroup radioGroupMealType;
    private Button btnConfirmAdd;
    private String selectedFoodName;
    private double selectedFoodCalories, selectedCarbs, selectedProteins, selectedFats, selectedOthers;
    private FirebaseAuth mAuth;
    private FirebaseFirestore fStore;
    private DatabaseReference databaseReference;

    private String apiUrl = "https://api.calorieninjas.com/v1/nutrition?query=";
    private String apiKey = "sEO/WztkNuDVZfEfyIOLrA==SrkmlTyHRD33WiTi";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        initializeViews();
        setupListeners();
        setupFirebase();
    }

    private void initializeViews() {
        resultTextBox = findViewById(R.id.resultTextBox);
        searchView = findViewById(R.id.searchBar);
        backBtn = findViewById(R.id.backBtn);
        addButton = findViewById(R.id.addButton);
        resultLayout = findViewById(R.id.resultLayout);
    }

    private void setupListeners() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!query.trim().isEmpty()) {
                    fetchNutritionData(query);
                } else {
                    Toast.makeText(SearchResultsActivity.this, "Please enter a food item", Toast.LENGTH_SHORT).show();
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        backBtn.setOnClickListener(v -> onBackPressed());

    }

    private void setupFirebase() {
        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
    }

    private void fetchNutritionData(String query) {
        new Thread(() -> {
            try {
                String encodedQuery = URLEncoder.encode(query, "UTF-8");
                URL url = new URL(apiUrl + encodedQuery);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("X-Api-Key", apiKey);

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                String result = response.toString();
                runOnUiThread(() -> processApiResult(result));

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(SearchResultsActivity.this, "Error fetching data", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }


    private void processApiResult(String result) {
        resultTextBox.setText("");
        resultLayout.removeAllViews();

        if (result != null && !result.isEmpty()) {
            try {
                JSONObject jsonResponse = new JSONObject(result);
                JSONArray jsonArray = jsonResponse.getJSONArray("items");

                double totalCalories = 0;
                double totalCarbs = 0;
                double totalProteins = 0;
                double totalFats = 0;
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String name = jsonObject.getString("name");
                    double calories = jsonObject.getDouble("calories");
                    double carbohydrates = jsonObject.getDouble("carbohydrates_total_g");
                    double proteins = jsonObject.getDouble("protein_g");
                    double fats = jsonObject.getDouble("fat_total_g");

                    totalCalories += calories;
                    totalCarbs += carbohydrates;
                    totalProteins += proteins;
                    totalFats += fats;
                    double others = totalCalories - (totalCarbs + totalProteins + totalFats);

                    addFoodItemToLayout(name, totalCalories, totalCarbs, totalProteins, totalFats, others);
                }

                resultLayout.setVisibility(View.VISIBLE);

            } catch (JSONException e) {
                e.printStackTrace();
                resultTextBox.setText("Failed to parse data");
            }
        } else {
            resultTextBox.setText("No results found or error fetching data");
        }
    }

    private void addFoodItemToLayout(String name, double calories, double carbs, double proteins, double fats, double others) {
        CardView cardView = new CardView(this);
        CardView.LayoutParams cardParams = new CardView.LayoutParams(
                CardView.LayoutParams.MATCH_PARENT,
                CardView.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(16, 16, 16, 16);
        cardView.setLayoutParams(cardParams);
        cardView.setCardElevation(8);
        cardView.setRadius(16);

        LinearLayout itemLayout = new LinearLayout(this);
        itemLayout.setOrientation(LinearLayout.VERTICAL);
        itemLayout.setPadding(16, 16, 16, 16);

        TextView foodTextView = new TextView(this);
        String summary = String.format(
            "╔══════════════════════════════════╗\n" +
            "║ %s\n" +
            "╠══════════════════════════════════╣\n" +
            "║ Total Calories: %.1f kcal\n" +
            "╠══════════════════════════════════╣\n" +
            "║ Nutrients Breakdown:\n" +
            "║   • Carbohydrates: %.1f g\n" +
            "║   • Protein:       %.1f g\n" +
            "║   • Fats:          %.1f g\n" +
            "║   • Others:        %.1f g\n" +
            "╚══════════════════════════════════╝",
            name.toUpperCase(), calories, carbs, proteins, fats, others);

        foodTextView.setTypeface(Typeface.DEFAULT_BOLD);
        foodTextView.setTextSize(14);
        foodTextView.setText(summary);

        addButton = new Button(this);
        addButton.setText("Add");
        //addButton.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        addButton.setTextColor(Color.WHITE);
        addButton.setOnClickListener(v -> showPopup(v, name, calories, carbs, proteins, fats, others));

        itemLayout.addView(foodTextView);
        itemLayout.addView(addButton);

        cardView.addView(itemLayout);
        resultLayout.addView(cardView);
    }
    private void showPopup(View view, String foodName, double foodCalories, double carbs, double proteins, double fats, double others) {
        selectedFoodName = foodName;
        selectedFoodCalories = foodCalories;
        selectedCarbs = carbs;
        selectedProteins = proteins;
        selectedFats = fats;
        selectedOthers = others;

        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_meal_type_selection, null);
        int width = ViewGroup.LayoutParams.WRAP_CONTENT;
        int height = ViewGroup.LayoutParams.WRAP_CONTENT;
        boolean focusable = true;
        popupWindow = new PopupWindow(popupView, width, height, focusable);

        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        popupView.setElevation(20);

        radioGroupMealType = popupView.findViewById(R.id.radioGroupMealType);
        selectDateButton = popupView.findViewById(R.id.selectDateButton);
        selectedDateText = popupView.findViewById(R.id.selectedDateText);
        btnConfirmAdd = popupView.findViewById(R.id.btnConfirmAdd);

        TextView titleTextView = popupView.findViewById(R.id.popupTitle);
        titleTextView.setText("Add " + foodName);
        final String[] selectedDate = {""};
        selectDateButton.setOnClickListener(v -> showDatePicker(selectedDate));

        btnConfirmAdd.setOnClickListener(v -> {
            if (validateInput(selectedDate[0])) {
                addMealToFirestore(selectedDate[0]);
            }
        });

        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
    }

    private void showDatePicker(String[] selectedDate) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    selectedDate[0] = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year1;
                    selectedDateText.setText("Selected date: " + selectedDate[0]);
                }, year, month, day);
        datePickerDialog.show();
    }

    private boolean validateInput(String date) {
        if (radioGroupMealType.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Please select a meal type", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (date.isEmpty()) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void addMealToFirestore(String date) {
        int selectedId = radioGroupMealType.getCheckedRadioButtonId();
        RadioButton selectedRadioButton = popupWindow.getContentView().findViewById(selectedId);
        String selectedMealType = selectedRadioButton.getText().toString();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            Map<String, Object> meal = new HashMap<>();
            meal.put("foodName", selectedFoodName);
            meal.put("calories", selectedFoodCalories);
            meal.put("carbs", selectedCarbs);
            meal.put("proteins", selectedProteins);
            meal.put("fats", selectedFats);
            meal.put("others", selectedOthers);
            meal.put("mealType", selectedMealType);
            meal.put("date", date);

            fStore.collection("users").document(user.getUid())
                    .collection("meals").add(meal)
                    .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(SearchResultsActivity.this, "Meal added successfully", Toast.LENGTH_SHORT).show();
                                popupWindow.dismiss();
                            } else {
                                Toast.makeText(SearchResultsActivity.this, "Failed to add meal: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }
}