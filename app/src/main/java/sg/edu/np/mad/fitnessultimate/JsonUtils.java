package sg.edu.np.mad.fitnessultimate;

import android.content.Context;
import android.content.res.AssetManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class JsonUtils {
    public static List<ExerciseInfo> loadExercises(Context context) {
        String currentClass = JsonUtils.class.getSimpleName();
        String fileName = "exercises.json";
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
        Type exerciseListType = new TypeToken<List<ExerciseInfo>>() {}.getType();
        return gson.fromJson(json, exerciseListType);
    }
}

