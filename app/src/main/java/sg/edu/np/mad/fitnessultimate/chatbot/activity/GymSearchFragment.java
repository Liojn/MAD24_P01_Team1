// GymSearchFragment.java
package sg.edu.np.mad.fitnessultimate.chatbot.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import sg.edu.np.mad.fitnessultimate.R;

public class GymSearchFragment extends Fragment implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private PlacesClient placesClient;
    private LatLng currentLatLng;

    private TextView durationTextView;
    private TextView distanceTextView;
    private TextView stepsTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gym_search, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        placesClient = Places.createClient(requireContext());

        Button searchButton = view.findViewById(R.id.searchButton);
        searchButton.setOnClickListener(v -> searchNearestGym());

        durationTextView = view.findViewById(R.id.durationTextView);
        distanceTextView = view.findViewById(R.id.distanceTextView);
        stepsTextView = view.findViewById(R.id.stepsTextView);

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        enableMyLocation();
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            getCurrentLocation();
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(requireActivity(), location -> {
                    if (location != null) {
                        currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
                    }
                });
    }

    private void searchNearestGym() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
            if (location != null) {
                currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                String locationStr = currentLatLng.latitude + "," + currentLatLng.longitude;
                String query = "gym";
                String radius = "5000"; // 5km radius
                String apiKey = "AIzaSyDBfpwpIuauYIj-XdbEWKisxhFSxjLazQ0";
                String url = "https://maps.googleapis.com/maps/api/place/textsearch/json?query=" + query + "&location=" + locationStr + "&radius=" + radius + "&key=" + apiKey;

                new FetchPlacesTask().execute(url);
            }
        });
    }

    private class FetchPlacesTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();
                return result.toString();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    JSONObject jsonResult = new JSONObject(result);
                    JSONArray results = jsonResult.getJSONArray("results");
                    if (results.length() > 0) {
                        JSONObject gym = results.getJSONObject(0);
                        String gymName = gym.getString("name");
                        JSONObject geometry = gym.getJSONObject("geometry");
                        JSONObject location = geometry.getJSONObject("location");
                        LatLng gymLatLng = new LatLng(location.getDouble("lat"), location.getDouble("lng"));

                        mMap.addMarker(new MarkerOptions().position(gymLatLng).title(gymName));
                        showDirections(gymLatLng);
                    } else {
                        // Handle no gyms found
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    // Handle parsing error
                }
            } else {
                // Handle error in fetching gyms
            }
        }
    }

    private void showDirections(LatLng destination) {
        String url = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=" + currentLatLng.latitude + "," + currentLatLng.longitude +
                "&destination=" + destination.latitude + "," + destination.longitude +
                "&mode=driving" +
                "&key=AIzaSyDBfpwpIuauYIj-XdbEWKisxhFSxjLazQ0";

        new FetchDirectionsTask().execute(url);
    }

    private class FetchDirectionsTask extends AsyncTask<String, Void, PolylineOptions> {
        private String duration;
        private String distance;
        private StringBuilder steps;

        @Override
        protected PolylineOptions doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();

                return parseDirections(result.toString());
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(PolylineOptions polylineOptions) {
            if (polylineOptions != null) {
                mMap.addPolyline(polylineOptions);
                durationTextView.setText("Duration: " + duration);
                distanceTextView.setText("Distance: " + distance);
                stepsTextView.setText("Steps: " + steps.toString());
            } else {
                // Handle error in fetching directions
            }
        }

        private PolylineOptions parseDirections(String jsonData) {
            try {
                JSONObject jsonObject = new JSONObject(jsonData);
                JSONArray routes = jsonObject.getJSONArray("routes");
                if (routes.length() > 0) {
                    JSONObject route = routes.getJSONObject(0);
                    JSONArray legs = route.getJSONArray("legs");
                    JSONObject leg = legs.getJSONObject(0);
                    duration = leg.getJSONObject("duration").getString("text");
                    distance = leg.getJSONObject("distance").getString("text");

                    steps = new StringBuilder();
                    JSONArray stepsArray = leg.getJSONArray("steps");
                    PolylineOptions polylineOptions = new PolylineOptions();
                    for (int i = 0; i < stepsArray.length(); i++) {
                        JSONObject step = stepsArray.getJSONObject(i);
                        JSONObject startLocation = step.getJSONObject("start_location");
                        polylineOptions.add(new LatLng(startLocation.getDouble("lat"), startLocation.getDouble("lng")));
                        JSONObject endLocation = step.getJSONObject("end_location");
                        polylineOptions.add(new LatLng(endLocation.getDouble("lat"), endLocation.getDouble("lng")));

                        String instruction = step.getString("html_instructions");
                        String travelMode = step.getString("travel_mode");
                        steps.append(travelMode).append(": ").append(instruction).append("\n");
                    }
                    return polylineOptions;
                } else {
                    // Handle no routes found
                }
            } catch (JSONException e) {
                e.printStackTrace();
                // Handle parsing error
            }
            return null;
        }
    }
}
