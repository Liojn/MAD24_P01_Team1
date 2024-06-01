package sg.edu.np.mad.fitnessultimate.foodtracker;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import sg.edu.np.mad.fitnessultimate.R;
import sg.edu.np.mad.fitnessultimate.calendarPage.BaseActivity;

public class SearchResultsActivity extends BaseActivity {

    //UI elements
    private TextView resultTextBox;
    private SearchView searchView;
    private Button backBtn;
    private LinearLayout resultLayout;

    //API endpoint and key
    private String apiUrl;
    private String apiKey = "sEO/WztkNuDVZfEfyIOLrA==S4xbs1Ybg0QZ5vMd";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        //Initializing UI elements
        resultTextBox = findViewById(R.id.resultTextBox);
        searchView = findViewById(R.id.searchBar);
        backBtn = findViewById(R.id.backBtn);
        resultLayout = findViewById(R.id.resultLayout);

        //API base URL
        apiUrl = "https://api.api-ninjas.com/v1/nutrition?query=";

        //Setting listener for search view query submission
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Concatenate the query with the API URL
                String fullUrl = apiUrl + query;

                // Execute AsyncTask to perform network operation in the background
                new FetchDataTask().execute(fullUrl);

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        //Setting listener for back button click
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Navigate the user back to FoodTracker activity
                Intent intent = new Intent(SearchResultsActivity.this, FoodTracker.class);
                startActivity(intent);
            }
        });
    }

    //AsyncTask for performing network operation in the background
    private class FetchDataTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            // String to hold the JSON response
            StringBuilder response = new StringBuilder();

            try {
                // Create URL object from the provided URL string
                URL url = new URL(urls[0]);

                // Create HttpURLConnection object
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                // Set request method
                connection.setRequestMethod("GET");

                //Set API key header
                connection.setRequestProperty("x-api-key", apiKey);

                // Connect to the API
                connection.connect();

                // Check if the response code is successful (200)
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    // Create BufferedReader to read the response
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                    // Read the response line by line and append it to the StringBuilder
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    // Close the reader
                    reader.close();
                }

                // Disconnect the HttpURLConnection
                connection.disconnect();

            } catch (IOException e) {
                e.printStackTrace();
            }

            // Return the JSON response as a String
            return response.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            //Clearing the previous result
            resultTextBox.setText("");

            // Check if the result is not null and not empty
            if (result != null && !result.isEmpty()) {
                try {
                    // Parse the JSON response
                    JSONArray jsonArray = new JSONArray(result);

                    //Variables to hold the total nutrition values
                    double totalCalories = 0;
                    double totalCarbs = 0;
                    double totalProteins = 0;
                    double totalFats = 0;
                    double totalOthers = 0;

                    // Iterate through the JSON array
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);

                        // Extract data from the JSON object
                        String name = jsonObject.getString("name");
                        double calories = jsonObject.getDouble("calories");
                        double carbohydrates = jsonObject.getDouble("carbohydrates_total_g");
                        double proteins = jsonObject.getDouble("protein_g");
                        double fats = jsonObject.getDouble("fat_total_g");
                        double others = calories - (carbohydrates + proteins + fats);

                        //Adding in each data for total values
                        totalCalories += calories;
                        totalCarbs += carbohydrates;
                        totalProteins += proteins;
                        totalFats += fats;
                        totalOthers += others;
                    }

                    // Create a StringBuilder to format the data
                    StringBuilder formattedData = new StringBuilder();

                    // Append the formatted data to the StringBuilder
                    String query = searchView.getQuery().toString();

                    formattedData.append("Name: ").append(query).append("\n");
                    formattedData.append("Total calories: ").append(String.format("%.1f", totalCalories)).append(" kcal per 100 g\n");
                    formattedData.append("Carbohydrates: ").append(String.format("%.1f",totalCarbs)).append(" g\n");
                    formattedData.append("Protein: ").append(String.format("%.1f",totalProteins)).append(" g\n");
                    formattedData.append("Fats: ").append(String.format("%.1f",totalFats)).append(" g\n");
                    formattedData.append("Others: ").append(String.format("%.1f",totalOthers)).append(" kcal\n");

                    // Set the formatted data to the TextView
                    resultTextBox.setText(formattedData.toString());

                    //Make the result layout visible
                    resultLayout.setVisibility(LinearLayout.VISIBLE);

                } catch (JSONException e) {
                    e.printStackTrace();
                    resultTextBox.setText("Failed to parse data");
                }
            } else {
                //Display error message if no result is found
                resultTextBox.setText("No results found or error fetching data");
            }
        }
    }
}