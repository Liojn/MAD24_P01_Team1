package sg.edu.np.mad.ultimatefitness.loginSignup;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import sg.edu.np.mad.ultimatefitness.R;
import sg.edu.np.mad.ultimatefitness.R.id;
import sg.edu.np.mad.ultimatefitness.calendarPage.BaseActivity;

public class LoginOrSignUpOption extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_or_sign_up_option);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button btnLogin = findViewById(id.button_login);
        Button btnSignUp = findViewById(id.button_signUp);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the MainActivity
                Intent intent = new Intent(LoginOrSignUpOption.this, LoginPageActivity.class);
                startActivity(intent);
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the SignUpPageActivity
                Intent intent = new Intent(LoginOrSignUpOption.this, SignUpPageActivity.class);
                startActivity(intent);
            }
        });
    }
}