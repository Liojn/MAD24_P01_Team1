package sg.edu.np.mad.ultimatefitness.waterTracker.alarm;

import java.util.List;

public class AlarmClass {
    private String time;
    private List<Integer> repeatDays;
    private boolean enabled;
    private String documentId;
    private int id;

    public AlarmClass(String time, List<Integer> repeatDays, boolean enabled, String documentId) {
        this.time = time;
        this.repeatDays = repeatDays;
        this.enabled = enabled;
        this.documentId = documentId;
    }

    // getters and setters for all fields
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    public List<Integer> getRepeatDays() { return repeatDays; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }
    public int getId() { return id; }

    public void setId(int id) { this.id = id; }
}