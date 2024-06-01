package sg.edu.np.mad.fitnessultimate.calendar;

public class TimeTracker {
    private long startTime;
    private long totalTimeSpent;

    public void startTracking() {
        startTime = System.currentTimeMillis();
    }

    public void stopTracking() {
        long endTime = System.currentTimeMillis();
        totalTimeSpent += (endTime - startTime);
    }

    public long getTotalTimeSpent() {
        return totalTimeSpent;
    }
}