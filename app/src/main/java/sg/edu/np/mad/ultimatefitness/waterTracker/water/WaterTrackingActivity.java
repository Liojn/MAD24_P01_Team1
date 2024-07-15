package sg.edu.np.mad.ultimatefitness.waterTracker.water;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import sg.edu.np.mad.ultimatefitness.R;
import sg.edu.np.mad.ultimatefitness.waterTracker.alarm.ReminderActivity;

public class WaterTrackingActivity extends AppCompatActivity {
    private float dX, dY; // Offset values for initial touch position
    private int lastAction; // Last touch action (ACTION_DOWN, ACTION_MOVE, etc.)
    private TextView tvProgress;
    private int currentWaterIntake = 0;
    private static final int SMALL_WATER_INCREMENT = 100; // Amount of water to increment per small droplet
    private static final int BIG_WATER_INCREMENT = 300; // Amount of water to increment per big droplet

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_water_tracking);

        // Initialize UI components
        tvProgress = findViewById(R.id.tvProgress);

        // Set insets listener for handling system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Navigate back to ProfilePage
        findViewById(R.id.backBtn).setOnClickListener(v -> {
            Intent intent = new Intent(WaterTrackingActivity.this, ReminderActivity.class);
            startActivity(intent);
        });

        // Info button listener to show the info dialog
        ImageButton infoButton = findViewById(R.id.infoButton);
        infoButton.setOnClickListener(v -> showInfoDialog());

        // Find all droplet FrameLayouts
        FrameLayout smallDroplet1 = findViewById(R.id.smallDroplet1);
        FrameLayout smallDroplet2 = findViewById(R.id.smallDroplet2);
        FrameLayout smallDroplet3 = findViewById(R.id.smallDroplet3);
        FrameLayout smallDroplet4 = findViewById(R.id.smallDroplet4);

        FrameLayout bigDroplet1 = findViewById(R.id.bigDroplet1);
        FrameLayout bigDroplet2 = findViewById(R.id.bigDroplet2);
        FrameLayout bigDroplet3 = findViewById(R.id.bigDroplet3);

        // Add listeners to each droplet
        smallDroplet1.setOnTouchListener(new MyTouchListener());
        smallDroplet2.setOnTouchListener(new MyTouchListener());
        smallDroplet3.setOnTouchListener(new MyTouchListener());
        smallDroplet4.setOnTouchListener(new MyTouchListener());

        bigDroplet1.setOnTouchListener(new MyTouchListener());
        bigDroplet2.setOnTouchListener(new MyTouchListener());
        bigDroplet3.setOnTouchListener(new MyTouchListener());
    }

    private void showInfoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_info, null);
        builder.setView(dialogView)
                .setPositiveButton(android.R.string.ok, (dialog, id) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();

        // Ensure the dialog window has a custom background
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    private final class MyTouchListener implements View.OnTouchListener {
        private float initialX, initialY; // Initial position of the droplet

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    // Record initial touch position
                    initialX = view.getX();
                    initialY = view.getY();
                    dX = view.getX() - event.getRawX();
                    dY = view.getY() - event.getRawY();
                    lastAction = MotionEvent.ACTION_DOWN;
                    break;

                case MotionEvent.ACTION_MOVE:
                    // Update view position based on touch movement
                    view.setX(event.getRawX() + dX);
                    view.setY(event.getRawY() + dY);
                    lastAction = MotionEvent.ACTION_MOVE;
                    break;

                case MotionEvent.ACTION_UP:
                    // Handle end of touch gesture
                    if (lastAction == MotionEvent.ACTION_DOWN) {
                        // Handle click event if needed
                        // Perform action when the droplet is clicked
                        Toast.makeText(getApplicationContext(), "Droplet clicked", Toast.LENGTH_SHORT).show();
                    } else {
                        // Check if droplet is dropped into the circular progress area
                        if (isViewOverlapping(view, findViewById(R.id.circleProgress))) {
                            // Determine the water increment based on the droplet type
                            int increment;
                            if ("big".equals(view.getTag())) {
                                increment = BIG_WATER_INCREMENT;
                            } else {
                                increment = SMALL_WATER_INCREMENT;
                            }

                            // Increment water intake
                            currentWaterIntake += increment;
                            tvProgress.setText(String.valueOf(currentWaterIntake));

                            // Optional: Update UI to show the progress visually
                            updateCircularProgress(currentWaterIntake);

                            // Make progress_circular visible
                            findViewById(R.id.progress_circular).setVisibility(View.VISIBLE);
                        }
                        // Reset the droplet to its initial position
                        view.setX(initialX);
                        view.setY(initialY);
                    }
                    lastAction = MotionEvent.ACTION_UP;
                    break;

                default:
                    return false;
            }
            return true;
        }
    }

    // Method to check if droplet overlaps with circular progress area
    private boolean isViewOverlapping(View firstView, View secondView) {
        Rect firstRect = new Rect();
        firstView.getHitRect(firstRect);

        Rect secondRect = new Rect();
        secondView.getHitRect(secondRect);

        return Rect.intersects(firstRect, secondRect);
    }

    // Method to update circular progress
    private void updateCircularProgress(int currentWaterIntake) {
        // Find your circular progress view
        RelativeLayout circleProgress = findViewById(R.id.circleProgress);

        // Calculate the percentage of water intake
        int maxWaterIntake = 2000; // Assuming maximum water intake is 2000ml
        float progressPercentage = (float) currentWaterIntake / maxWaterIntake;

        // Set the level of the progress drawable
        int level = (int) (progressPercentage * 10000); // 10000 is the maximum level

        // Set the progress drawable
        Drawable progressDrawable = ContextCompat.getDrawable(this, R.drawable.vertical_progress_drawable);
        progressDrawable.setLevel(level);

        // Set the background of circleProgress to the progress drawable
        circleProgress.setBackground(progressDrawable);

        // Optionally, update other UI components based on progress
        TextView tvProgress = findViewById(R.id.tvProgress);
        tvProgress.setText(String.valueOf(currentWaterIntake));
    }
}

