package sg.edu.np.mad.fitnessultimate.chatbot.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
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

import java.util.ArrayList;
import java.util.List;

public class ChatbotActivity extends BaseActivity {

    EditText userInput;
    RecyclerView recyclerView;
    MessageAdapter messageAdapter;
    List<ResponseMessage> responseMessageList;
    FrameLayout layoutSend;
    ImageView sendIcon;

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
        }
    }

    private void displayFaqMessage() {
        String faqMessage = "Here are some FAQs:\n1) How does the training schedule work?\n2) What is the calendar for?\n3) How to use the food tracking?\n4) What are the benefits of exercise\n5) Display FAQ's again";
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
            return "You can choose a follow along work out inside has different type of work out sets which you can choose from! Alternatively, you can choose excerises and see how to do each individual excerise.\n\nEnter 5 to see FAQ again";
        } else if (message.contains("2") || message.contains("calendar")) {
            return "It displays workouts in a calendar format, allowing users to see their workout history at a glance and differentiates between types of workouts or intensity levels using colour-coding.\n\nEnter 5 to see FAQ again";
        } else if (message.contains("3") || message.contains("food") || message.contains("tracking")) {
            return "Users can enter the food they eat by selecting foods from the database and specifying the portion sizes.\n\nEnter 5 to see FAQ again";
        } else if (message.contains("4") || message.contains("benefits")) {
            return "Exercise has many benefits including improving cardiovascular health, strengthening muscles, and enhancing mental health.\n\nEnter 5 to see FAQ again";
        } else if(message.contains("hi") || message.contains("hello")) {
            return "Hello! I am the Fitness Ultimate's Chatbot, choose a question from the FAQ's and I shall answer!";
        } else if(message.contains("5") || message.contains("faq")) {
                return "Here are some FAQs:\n1) How does the training schedule work?\n2) What is the calendar for?\n3) How to use the food tracking?\n4) What are the benefits of exercise\n5) Display FAQ's again";
        } else {
            return "Sorry, I don't have an answer for that. Please ask another question.";
        }
    }

    boolean isLastVisible() {
        LinearLayoutManager layoutManager = ((LinearLayoutManager) recyclerView.getLayoutManager());
        int pos = layoutManager.findLastCompletelyVisibleItemPosition();
        int numItems = recyclerView.getAdapter().getItemCount();
        return (pos >= numItems);
    }
}
