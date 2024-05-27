package sg.edu.np.mad.fitnessultimate.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

import sg.edu.np.mad.fitnessultimate.R;

public class SplashActivity extends AppCompatActivity {
    private static final int SPLASH_DELAY = 1000; // 3 seconds delay

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Check if the user is already logged in
                FirebaseAuth fAuthLogin = FirebaseAuth.getInstance();
                if (fAuthLogin.getCurrentUser() != null) {
                    // User is already logged in, redirect to ProfilePageActivity
                    startActivity(new Intent(SplashActivity.this, ProfilePageActivity.class));
                    finish(); // Close MainActivity
                } else {
                    // User is not logged in, stay in MainActivity and show login options
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                }
            }
        }, SPLASH_DELAY);
    }
}