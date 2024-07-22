package sg.edu.np.mad.ultimatefitness.waterTracker.water;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class IntakeHistory {
    private String intakeAmount;
    private String increment;
    private String time;
    private int goal;

    public IntakeHistory(String intakeAmount, String increment, int goal) {
        this.intakeAmount = intakeAmount;
        this.increment = increment;
        this.time = getCurrentTime();
        this.goal = goal;
    }

    public String getIntakeAmount() {
        return intakeAmount;
    }

    public void setIntakeAmount(String intakeAmount) {
        this.intakeAmount = intakeAmount;
    }

    public String getIncrement() {
        return increment;
    }

    public void setIncrement(String increment) {
        this.increment = increment;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a, MMM dd yyyy", Locale.getDefault());
        return sdf.format(new Date());
    }

    public int getGoal() {
        return goal;
    }

    public void setGoal(int goal) {
        this.goal = goal;
    }
}

