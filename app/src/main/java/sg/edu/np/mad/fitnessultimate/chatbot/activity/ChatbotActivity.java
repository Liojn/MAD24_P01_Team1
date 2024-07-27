// ChatbotActivity.java
package sg.edu.np.mad.fitnessultimate.chatbot.activity;

import android.os.AsyncTask;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.net.Uri;

import android.Manifest;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import sg.edu.np.mad.fitnessultimate.MainActivity;

import sg.edu.np.mad.fitnessultimate.R;
import sg.edu.np.mad.fitnessultimate.calendarPage.BaseActivity;
import sg.edu.np.mad.fitnessultimate.chatbot.adapter.MessageAdapter;
import sg.edu.np.mad.fitnessultimate.chatbot.model.ResponseMessage;

import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.api.Places;


import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

public class ChatbotActivity extends BaseActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private FusedLocationProviderClient fusedLocationClient;
    private PlacesClient placesClient;
    EditText userInput;
    RecyclerView recyclerView;
    MessageAdapter messageAdapter;
    List<ResponseMessage> responseMessageList;
    FrameLayout layoutSend;
    ImageView sendIcon;

    private boolean isFragmentActive = false; // Flag to check if fragment is active

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chatbot);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);

            return insets;
        });

        Places.initialize(getApplicationContext(), "AIzaSyDBfpwpIuauYIj-XdbEWKisxhFSxjLazQ0");
        placesClient = Places.createClient(this);
        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Check permissions and start process
        checkLocationPermission();

        findViewById(R.id.imageBack).setOnClickListener(v -> {
            Intent MessageActivity = new Intent(ChatbotActivity.this, MainActivity.class);
            startActivity(MessageActivity);
        });

        userInput = findViewById(R.id.userInput);
        recyclerView = findViewById(R.id.conversation);
        layoutSend = findViewById(R.id.layoutSend);
        sendIcon = findViewById(R.id.sendIcon);

        responseMessageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(responseMessageList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(messageAdapter);

        displayFaqMessage();

        userInput.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_SEND) {
                sendMessage();
                return true;
            }
            return false;
        });

        layoutSend.setOnClickListener(v -> sendMessage());
    }

    private void sendMessage() {
        String userMessage = userInput.getText().toString().toLowerCase();
        if (!userMessage.trim().isEmpty()) {
            addMessageToChat(userMessage, false);
            String botResponse = getResponseForMessage(userMessage);
            addMessageToChat(botResponse, true);
            userInput.setText(""); // Clear input field

            // Check for specific queries and show fragment
            if (userMessage.contains("how to do a push up")) {
                showVideoRecommendationFragment("push up");
            } else if (userMessage.contains("how to do a crunch")) {
                showVideoRecommendationFragment("crunches");
            } else if (userMessage.contains("how to do pull ups")) {
                showVideoRecommendationFragment("pull ups");
            }
        }
    }

    private void showVideoRecommendationFragment(String exercise) {
        if (!isFragmentActive) { // Check if fragment is already active
            FragmentManager fragmentManager = getSupportFragmentManager();
            VideoRecommendationChatbot fragment = VideoRecommendationChatbot.newInstance(exercise);
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.add(R.id.main, fragment);
            transaction.addToBackStack(null); // Add this line to handle the back stack correctly
            transaction.commit();
            isFragmentActive = true; // Set flag to true
        }
    }

    public void setFragmentActive(boolean isActive) {
        this.isFragmentActive = isActive;
    }

    @Override
    public void onBackPressed() {
        if (isFragmentActive) {
            closeVideoRecommendationFragment();
        } else {
            super.onBackPressed();
        }
    }

    private void closeVideoRecommendationFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.popBackStackImmediate();
        isFragmentActive = false;
    }

    private void displayFaqMessage() {
        String faqMessage = "Here are some FAQs:\n1) How does the training schedule work?\n2) What is the calendar for?\n3) How to use the food tracking?\n4) What are the benefits of exercise?\n5) How to do a push up? \n6) How to do crunches? \n7) How to do pull ups? \n8) Display FAQs again.";
        addMessageToChat(faqMessage, true);
    }

    private void addMessageToChat(String message, boolean isUser) {
        ResponseMessage responseMessage = new ResponseMessage(message, isUser);
        responseMessageList.add(responseMessage);
        messageAdapter.notifyDataSetChanged();
        if (!isLastVisible()) {
            recyclerView.smoothScrollToPosition(messageAdapter.getItemCount() - 1);
        }
    }

    private String getResponseForMessage(String message) {
        message = message.toLowerCase();
        if (message.contains("1") || message.contains("training")) {
            return "You can choose a follow along workout with different types of workout sets!";
        } else if (message.contains("2") || message.contains("calendar")) {
            return "It displays workouts in a calendar format.";
        } else if (message.contains("3") || message.contains("food") || message.contains("tracking")) {
            return "Users can enter the food they eat by selecting foods from the database.";
        } else if (message.contains("4") || message.contains("benefits")) {
            return "Exercise has many benefits including improving cardiovascular health.";
        } else if (message.contains("hi") || message.contains("hello")) {
            return "Hello! I am the Fitness Ultimate's Chatbot. Choose a question from the FAQs!";
        } else if (message.contains("8") || message.contains("faq")) {
            return "Here are some FAQs:\n1) How does the training schedule work?\n2) What is the calendar for?\n3) How to use the food tracking?\n4) What are the benefits of exercise?\n5) How to do a push up? \n6) How to do crunches? \n7) How to do pull ups? \n8) Display FAQs again.";
        } else if (message.contains("5") || message.contains("push up")) {
            showVideoRecommendationFragment("push up");
            return "To do a push up, you can follow this video guide:";
        } else if (message.contains("crunch") || message.contains("how to do a crunch") || message.contains("6")) {
            showVideoRecommendationFragment("crunches");
            return "To do a crunch, here are some videos:";
        } else if (message.contains("pull up") || message.contains("how to do pull ups") || message.contains("7")) {
            showVideoRecommendationFragment("pull ups");
            return "To do pull ups, here are some video recommendations:";
        } else if (message.contains("nearest gym") || message.contains("gym near me")) {
            showGymSearchFragment();
            return "Searching for the nearest gym...";
        } else {
            return "Sorry, I don't have an answer for that. Please ask another question.";
        }
    }

    private boolean isLastVisible() {
        if (messageAdapter != null && messageAdapter.getItemCount() != 0) {
            LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            int pos = layoutManager.findLastCompletelyVisibleItemPosition();
            return pos >= messageAdapter.getItemCount() - 1;
        }
        return false;
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Permission already granted
            findClosestGymAndGetDirections();
        }
    }

    private void findClosestGymAndGetDirections() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request the missing permissions
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                        findNearestGym(currentLatLng);
                    } else {
                        addMessageToChat("Unable to get current location. Please try again.", true);
                    }
                });
    }

    private void findNearestGym(LatLng currentLatLng) {
        String location = currentLatLng.latitude + "," + currentLatLng.longitude;
        String radius = "5000"; // Search within 5km radius
        String query = "gym";
        String apiKey = "AIzaSyDBfpwpIuauYIj-XdbEWKisxhFSxjLazQ0";
        String url = "https://maps.googleapis.com/maps/api/place/textsearch/json?query=" + query + "&location=" + location + "&radius=" + radius + "&key=" + apiKey;

        new FetchPlacesTask().execute(url);
    }

    private class FetchPlacesTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();
                return result.toString();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    JSONObject jsonResult = new JSONObject(result);
                    JSONArray results = jsonResult.getJSONArray("results");
                    if (results.length() > 0) {
                        JSONObject gym = results.getJSONObject(0);
                        String gymName = gym.getString("name");
                        JSONObject geometry = gym.getJSONObject("geometry");
                        JSONObject location = geometry.getJSONObject("location");
                        LatLng gymLatLng = new LatLng(location.getDouble("lat"), location.getDouble("lng"));

                        addMessageToChat("Nearest gym: " + gymName + " at " + gymLatLng, true);
                        drawRoute(gymLatLng);
                    } else {
                        addMessageToChat("No gyms found nearby.", true);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    addMessageToChat("Error parsing gyms.", true);
                }
            } else {
                addMessageToChat("Error fetching gyms.", true);
            }
        }
    }

    private void drawRoute(LatLng gymLatLng) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                String url = "https://maps.googleapis.com/maps/api/directions/json?" +
                        "origin=" + currentLatLng.latitude + "," + currentLatLng.longitude +
                        "&destination=" + gymLatLng.latitude + "," + gymLatLng.longitude +
                        "&mode=transit" +
                        "&key=AIzaSyDBfpwpIuauYIj-XdbEWKisxhFSxjLazQ0";

                new FetchDirectionsTask().execute(url);
            }
        });
    }

    private class FetchDirectionsTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();
                return result.toString();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    JSONObject jsonResult = new JSONObject(result);
                    JSONArray routes = jsonResult.getJSONArray("routes");
                    if (routes.length() > 0) {
                        JSONObject route = routes.getJSONObject(0);
                        JSONArray legs = route.getJSONArray("legs");
                        JSONObject leg = legs.getJSONObject(0);
                        String duration = leg.getJSONObject("duration").getString("text");
                        String distance = leg.getJSONObject("distance").getString("text");

                        StringBuilder steps = new StringBuilder();
                        JSONArray stepsArray = leg.getJSONArray("steps");
                        for (int i = 0; i < stepsArray.length(); i++) {
                            JSONObject step = stepsArray.getJSONObject(i);
                            String instruction = step.getString("html_instructions");
                            String travelMode = step.getString("travel_mode");
                            steps.append(travelMode).append(": ").append(instruction).append("\n");
                        }

                        String directions = "Duration: " + duration + "\nDistance: " + distance + "\n\nDirections:\n" + steps.toString();
                        addMessageToChat(directions, true);
                    } else {
                        addMessageToChat("No routes found.", true);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    addMessageToChat("Error parsing directions.", true);
                }
            } else {
                addMessageToChat("Error fetching directions.", true);
            }
        }
    }

    private void showGymSearchFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        GymSearchFragment fragment = new GymSearchFragment();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.main, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                findClosestGymAndGetDirections();
            } else {
                Toast.makeText(this, "Location permission is required to find the nearest gym.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}