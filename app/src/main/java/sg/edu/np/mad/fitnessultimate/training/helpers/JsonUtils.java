package sg.edu.np.mad.fitnessultimate.training.helpers;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;

import sg.edu.np.mad.fitnessultimate.training.exercises.ExerciseInfo;
import sg.edu.np.mad.fitnessultimate.training.workouts.Workout;

public class JsonUtils {
    public static List<ExerciseInfo> loadExercises(Context context) {
        String currentClass = JsonUtils.class.getSimpleName();
        String fileName = "exercises.json";
        String json = null;

        // reads contents of json file into a string
        try {
            AssetManager assetManager = context.getAssets();
            InputStream is = assetManager.open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            int bytesRead = is.read(buffer);
            is.close();

            if (bytesRead == -1) {
                System.err.printf("[%s] failed to read %s file", currentClass, fileName);
                return null;
            }

            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            System.err.printf("[%s] ioexception when reading %s: %s", currentClass, fileName, ex.getMessage());
            return null;
        }

        // use Gson to convert json string to list of ExerciseInfo objects
        Gson gson = new Gson();
        Type exerciseListType = new TypeToken<List<ExerciseInfo>>() {}.getType();
        return gson.fromJson(json, exerciseListType);
    }

    public static List<Workout> loadWorkouts(Context context) {
        String currentClass = JsonUtils.class.getSimpleName();
        String fileName = "workouts.json";
        String json = null;

        try {
            AssetManager assetManager = context.getAssets();
            InputStream is = assetManager.open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            int bytesRead = is.read(buffer);
            is.close();

            if (bytesRead == -1) {
                System.err.printf("[%s] failed to read %s file", currentClass, fileName);
                return null;
            }

            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            System.err.printf("[%s] ioexception when reading %s: %s", currentClass, fileName, ex.getMessage());
            return null;
        }

        Gson gson = new Gson();
        Type workoutListType = new TypeToken<List<Workout>>() {}.getType();
        List<Workout> workouts = gson.fromJson(json, workoutListType);

        // secondary parse to get list of exercises for each workout
        JsonArray workoutsJsonArray = JsonParser.parseString(json).getAsJsonArray();
        Type exerciseListType = new TypeToken<List<Workout.Exercise>>() {}.getType();
        for (JsonElement workoutElement : workoutsJsonArray) {
            JsonObject workoutObject = workoutElement.getAsJsonObject();

            // access the exercises value in the workout json
            JsonArray exercisesJson = workoutObject.getAsJsonArray("exercises");

            // same Gson parsing for Exercise class
            List<Workout.Exercise> exercises = gson.fromJson(exercisesJson, exerciseListType);

            Log.i("JsonUtils", "Workout: " + workoutObject.get("name").getAsString() + " Exercises: " + exercises.size());

            // set the exercises for the workout by looping through array of workouts.
            // if the workout name matches the current workout, set the exercises to the current read exercises
            for (Workout workout : workouts) {
                Log.i("JsonUtils", String.format("workout: %s", workout.getName()));
                if (workout.getName().equals(workoutObject.get("name").getAsString())) {
                    Log.i("JsonUtils", "exercises set");
                    workout.setExercises(exercises);
                    break;
                }
            }
        }

        return workouts;
    }
}