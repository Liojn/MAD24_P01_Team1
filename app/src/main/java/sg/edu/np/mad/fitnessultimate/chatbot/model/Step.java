package sg.edu.np.mad.fitnessultimate.chatbot.model;


import com.google.android.gms.maps.model.LatLng;

public class Step {
    public LatLng startLocation;
    public LatLng endLocation;
    public String info;
    public TransitDetails transitDetails;

    public Step(LatLng startLocation, LatLng endLocation, String info, TransitDetails transitDetails) {
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.info = info;
        this.transitDetails = transitDetails;
    }
}
