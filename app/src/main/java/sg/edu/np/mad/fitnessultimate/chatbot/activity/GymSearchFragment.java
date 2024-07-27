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

import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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
import java.util.ArrayList;
import java.util.List;

import sg.edu.np.mad.fitnessultimate.R;
import sg.edu.np.mad.fitnessultimate.chatbot.adapter.InfoAdapter;
import sg.edu.np.mad.fitnessultimate.chatbot.model.Step;
import sg.edu.np.mad.fitnessultimate.chatbot.model.TransitDetails;

public class GymSearchFragment extends Fragment implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private PlacesClient placesClient;
    private LatLng currentLatLng;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gym_search, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        placesClient = Places.createClient(requireContext());

        Button searchButton = view.findViewById(R.id.searchButton);
        searchButton.setOnClickListener(v -> searchNearestGym());

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        enableMyLocation();
        mMap.setInfoWindowAdapter(new InfoAdapter(requireContext()));
        setupMarkerClickListener();
    }

    private void setupMarkerClickListener() {
        mMap.setOnMarkerClickListener(marker -> {
            marker.showInfoWindow();
            if (currentLatLng != null) {
                LatLng destination = marker.getPosition();
                showDirections(currentLatLng, destination);
            }
            return true;
        });
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
                String radius = "1km"; // 2km radius
                String apiKey = "AIzaSyDBfpwpIuauYIj-XdbEWKisxhFSxjLazQ0";
                String url = "https://maps.googleapis.com/maps/api/place/textsearch/json?query=" + query + "&location=" + locationStr + "&radius=" + radius + "&key=" + apiKey;

                new FetchPlacesTask().execute(url);
            }
        });
    }

    private void showDirections(LatLng origin, LatLng destination) {
        String url = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=" + origin.latitude + "," + origin.longitude +
                "&destination=" + destination.latitude + "," + destination.longitude +
                "&mode=transit" +
                "&key=AIzaSyDBfpwpIuauYIj-XdbEWKisxhFSxjLazQ0";

        new FetchDirectionsTask().execute(url);
    }
    private class FetchDirectionsTask extends AsyncTask<String, Void, List<Step>> {
        @Override
        protected List<Step> doInBackground(String... urls) {
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
        protected void onPostExecute(List<Step> steps) {
            if (steps != null) {
                PolylineOptions polylineOptions = new PolylineOptions();
                for (Step step : steps) {
                    LatLng startLocation = step.startLocation;
                    LatLng endLocation = step.endLocation;
                    polylineOptions.add(startLocation, endLocation);

                    // Remove HTML tags from step info
                    String cleanInfo = step.info.replaceAll("<[^>]*>", "");

                    // Add transit information if available
                    if (step.transitDetails != null) {
                        cleanInfo = "Take " + step.transitDetails.line + " - " + cleanInfo;
                    }

                    mMap.addMarker(new MarkerOptions()
                            .position(startLocation)
                            .title(cleanInfo)
                            .snippet("Click for more info"));
                }
                mMap.addPolyline(polylineOptions);
            } else {
                // Handle error in fetching directions
            }
        }


        private List<Step> parseDirections(String jsonData) {
            List<Step> steps = new ArrayList<>();
            try {
                JSONObject jsonObject = new JSONObject(jsonData);
                JSONArray routes = jsonObject.getJSONArray("routes");
                if (routes.length() > 0) {
                    JSONObject route = routes.getJSONObject(0);
                    JSONArray legs = route.getJSONArray("legs");
                    JSONObject leg = legs.getJSONObject(0);
                    JSONArray stepsArray = leg.getJSONArray("steps");

                    for (int i = 0; i < stepsArray.length(); i++) {
                        JSONObject step = stepsArray.getJSONObject(i);
                        JSONObject startLocation = step.getJSONObject("start_location");
                        JSONObject endLocation = step.getJSONObject("end_location");
                        String instruction = step.getString("html_instructions");
                        String travelMode = step.getString("travel_mode");

                        LatLng startLatLng = new LatLng(startLocation.getDouble("lat"), startLocation.getDouble("lng"));
                        LatLng endLatLng = new LatLng(endLocation.getDouble("lat"), endLocation.getDouble("lng"));

                        TransitDetails transitDetails = null;
                        if ("TRANSIT".equals(travelMode)) {
                            JSONObject transit = step.getJSONObject("transit_details");
                            String line = transit.getJSONObject("line").getString("short_name");
                            transitDetails = new TransitDetails(line);
                        }
                        steps.add(new Step(startLatLng, endLatLng, instruction, transitDetails));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return steps;
        }
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
                        showDirections(currentLatLng, gymLatLng);
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
}

