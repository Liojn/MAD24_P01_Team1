package sg.edu.np.mad.fitnessultimate;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import sg.edu.np.mad.fitnessultimate.calendarPage.CalendarActivity;
import sg.edu.np.mad.fitnessultimate.chatbot.activity.ChatbotActivity;
import sg.edu.np.mad.fitnessultimate.foodtracker.*;
import sg.edu.np.mad.fitnessultimate.training.TrainingMenuActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //For Onclick for Training Schedule
        findViewById(R.id.nav_fitness).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TrainingMenuActivity.class);
            startActivity(intent);
        });
        //For Onclick for Chatbot
        findViewById(R.id.chat_button).setOnClickListener(v ->{
            Intent intent = new Intent(MainActivity.this, ChatbotActivity.class);
            startActivity(intent);
        });
        //For Onclick for Calendar
        findViewById(R.id.nav_calendar).setOnClickListener(v ->{
            Intent intent = new Intent(MainActivity.this, CalendarActivity.class);
            startActivity(intent);
        });
        //For Onclick for FoodTracker
        findViewById(R.id.nav_food).setOnClickListener(v ->{
            Intent intent = new Intent(MainActivity.this, FoodTracker.class);
            startActivity(intent);
        });

        //For Onclick for HomePage
        findViewById(R.id.nav_home).setOnClickListener(v ->{
            Intent intent = new Intent(MainActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

    }
}