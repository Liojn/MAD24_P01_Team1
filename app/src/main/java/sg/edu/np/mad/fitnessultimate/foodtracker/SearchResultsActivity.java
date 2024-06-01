package sg.edu.np.mad.fitnessultimate.foodtracker;

import android.os.AsyncTask;
import android.os.Bundle;
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

public class SearchResultsActivity extends AppCompatActivity {
    private TextView resultTextBox;
    private SearchView searchView;
    private String apiUrl;
    private String apiKey = "sEO/WztkNuDVZfEfyIOLrA==S4xbs1Ybg0QZ5vMd";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        resultTextBox = findViewById(R.id.resultTextBox);
        searchView = findViewById(R.id.searchBar);

        apiUrl = "https://api.api-ninjas.com/v1/nutrition?query=";

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
    }

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

            resultTextBox.setText("");

            // Check if the result is not null and not empty
            if (result != null && !result.isEmpty()) {
                try {
                    // Parse the JSON response
                    JSONArray jsonArray = new JSONArray(result);

                    // Create a StringBuilder to format the data
                    StringBuilder formattedData = new StringBuilder();

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

                        // Append the formatted data to the StringBuilder
                        formattedData.append("Name: ").append(name).append("\n");
                        formattedData.append("Total calories: ").append(calories).append(" per 100 g\n");
                        formattedData.append("Carbohydrates: ").append(carbohydrates).append("\n");
                        formattedData.append("Protein: ").append(proteins).append("\n");
                        formattedData.append("Fats: ").append(fats).append("\n");
                        formattedData.append("Others: ").append(others).append("\n");


                    }

                    // Set the formatted data to the TextView
                    resultTextBox.setText(formattedData.toString());

                } catch (JSONException e) {
                    e.printStackTrace();
                    resultTextBox.setText("Failed to parse data");
                }
            } else {
                resultTextBox.setText("No results found or error fetching data");
            }
        }
    }
}