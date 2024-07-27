// ChatbotActivity.java
package sg.edu.np.mad.fitnessultimate.chatbot.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.ArrayList;
import java.util.List;

import sg.edu.np.mad.fitnessultimate.MainActivity;
import sg.edu.np.mad.fitnessultimate.R;
import sg.edu.np.mad.fitnessultimate.calendarPage.BaseActivity;
import sg.edu.np.mad.fitnessultimate.chatbot.adapter.MessageAdapter;
import sg.edu.np.mad.fitnessultimate.chatbot.model.ResponseMessage;

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

    private boolean isFragmentActive = false;

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
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

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

            if (userMessage.contains("how to do a push up")) {
                showVideoRecommendationFragment("push up");
            } else if (userMessage.contains("how to do a crunch")) {
                showVideoRecommendationFragment("crunches");
            } else if (userMessage.contains("how to do pull ups")) {
                showVideoRecommendationFragment("pull ups");
            } else if (userMessage.contains("nearest gym") || userMessage.contains("gym near me")) {
                showGymSearchFragment();
            }
        }
    }

    private void showVideoRecommendationFragment(String exercise) {
        if (!isFragmentActive) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            VideoRecommendationChatbot fragment = VideoRecommendationChatbot.newInstance(exercise);
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.add(R.id.main, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
            isFragmentActive = true;
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
        String faqMessage = "Here are some FAQs:\n1) How does the training schedule work?\n2) What is the calendar for?\n3) How to use the food tracking?\n4) What are the benefits of exercise?\n5) How to do a push up? \n6) How to do crunches? \n7) How to do pull ups? \n8) Find the nearest gym to me.\n9) Display FAQ.";;
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
        } else if (message.contains("9") || message.contains("faq")) {
            return "Here are some FAQs:\n1) How does the training schedule work?\n2) What is the calendar for?\n3) How to use the food tracking?\n4) What are the benefits of exercise?\n5) How to do a push up? \n6) How to do crunches? \n7) How to do pull ups? \n8) Find the nearest gym to me.\n9) Display FAQ.";
        } else if (message.contains("5") || message.contains("push up")) {
            showVideoRecommendationFragment("push up");
            return "To do a push up, you can follow this video guide:";
        } else if (message.contains("crunch") || message.contains("how to do a crunch") || message.contains("6")) {
            showVideoRecommendationFragment("crunches");
            return "To do a crunch, here are some videos:";
        } else if (message.contains("pull up") || message.contains("how to do pull ups") || message.contains("7")) {
            showVideoRecommendationFragment("pull ups");
            return "To do pull ups, here are some video recommendations:";
        } else if (message.contains("8") || message.contains("nearest gym") || message.contains("gym")) {
            showGymSearchFragment();
            return "Searching for the nearest gym...\nEnter 9 for FAQ again";
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // No longer need to call findClosestGymAndGetDirections() here
                Toast.makeText(this, "Location permission granted. You can now search for gyms.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Location permission is required to find the nearest gym.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
