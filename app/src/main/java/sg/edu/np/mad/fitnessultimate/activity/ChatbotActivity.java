package sg.edu.np.mad.fitnessultimate.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import sg.edu.np.mad.fitnessultimate.MainActivity;
import sg.edu.np.mad.fitnessultimate.R;
import sg.edu.np.mad.fitnessultimate.adapter.MessageAdapter;
import sg.edu.np.mad.fitnessultimate.model.ResponseMessage;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

public class ChatbotActivity extends AppCompatActivity {

    EditText userInput;
    RecyclerView recyclerView;
    MessageAdapter messageAdapter;
    List<ResponseMessage> responseMessageList;

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


        findViewById(R.id.imageBack).setOnClickListener(v ->{
            Intent MessageActivity = new Intent(ChatbotActivity.this, MainActivity.class);
            startActivity(MessageActivity);
        });

        userInput = findViewById(R.id.userInput);
        recyclerView = findViewById(R.id.conversation);
        responseMessageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(responseMessageList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));
        recyclerView.setAdapter(messageAdapter);


        displayFaqMessage();

        userInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_SEND) {
                    String userMessage = userInput.getText().toString().toLowerCase();
                    addMessageToChat(userMessage, false);
                    String botResponse = getResponseForMessage(userMessage);
                    addMessageToChat(botResponse, true);
                    userInput.setText("");  // Clear input field
                }
                return false;
            }
        });
    }

    private void displayFaqMessage() {
        String faqMessage = "Here are some FAQs:\n1) How to do a push up\n2) What is a squat\n3) How to do a plank\n4) What are the benefits of exercise\n";
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
        if (message.contains("1") || message.contains("push up")) {
            return "To do a push up, start in a plank position, lower your body until your chest nearly touches the floor, and then push back up.";
        } else if (message.contains("2") || message.contains("squat")) {
            return "A squat is a strength exercise in which you lower your hips from a standing position and then stand back up.";
        } else if (message.contains("3") || message.contains("plank")) {
            return "To do a plank, hold your body in a straight line in a push-up position, supporting your weight on your forearms and toes.";
        } else if (message.contains("4") || message.contains("benefits of exercise")) {
            return "Exercise has many benefits including improving cardiovascular health, strengthening muscles, and enhancing mental health.";
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
