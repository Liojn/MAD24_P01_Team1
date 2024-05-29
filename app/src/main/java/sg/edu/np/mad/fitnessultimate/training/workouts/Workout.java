package sg.edu.np.mad.fitnessultimate.training.workouts;

import android.os.Parcelable;
import android.os.Parcel;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import sg.edu.np.mad.fitnessultimate.training.exercises.ExerciseInfo;
import sg.edu.np.mad.fitnessultimate.training.helpers.GlobalExerciseData;

public class Workout implements Parcelable {
    private String name;
    private String description;
    private int breakTimeInMinutes;
    private int estimatedTimeInMinutes;
    private List<Exercise> exercises;

    public Workout(String name, String description, int breakTimeInMinutes, int estimatedTimeInMinutes, List<Exercise> exercises) {
        this.name = name;
        this.description = description;
        this.breakTimeInMinutes = breakTimeInMinutes;
        this.estimatedTimeInMinutes = estimatedTimeInMinutes;
        this.exercises = exercises;
    }

    protected Workout(Parcel in) {
        name = in.readString();
        description = in.readString();
        breakTimeInMinutes = in.readInt();
        estimatedTimeInMinutes = in.readInt();
        exercises = in.createTypedArrayList(Exercise.CREATOR);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(description);
        dest.writeInt(breakTimeInMinutes);
        dest.writeInt(estimatedTimeInMinutes);
        dest.writeTypedList(exercises);
    }

    public static final Parcelable.Creator<Workout> CREATOR = new Creator<Workout>() {
        @Override
        public Workout createFromParcel(Parcel in) {
            return new Workout(in);
        }

        @Override
        public Workout[] newArray(int size) {
            return new Workout[size];
        }
    };

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getBreakTimeInMinutes() {
        return breakTimeInMinutes;
    }

    public int getEstimatedTimeInMinutes() {
        return estimatedTimeInMinutes;
    }

    public List<Exercise> getExercises() {
        return exercises;
    }

    public void setExercises(List<Exercise> exercises) {
        Log.i("Workout", String.format("%s: set %d exercises", name, exercises.size()));
        this.exercises = exercises;
    }

    // inner class to store exercise info relative to workout
    public static class Exercise implements Parcelable {
        private String name;
        private int count;
        private int sets;

        public Exercise(String name, int count, int sets) {
            this.name = name;
            this.count = count;
            this.sets = sets;
        }

        public String getName() {
            return name;
        }

        public int getCount() {
            return count;
        }

        public int getSets() {
            return sets;
        }


        protected Exercise(Parcel in) {
            name = in.readString();
            count = in.readInt();
            sets = in.readInt();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(name);
            dest.writeInt(count);
            dest.writeInt(sets);
        }

        public static final Parcelable.Creator<Exercise> CREATOR = new Creator<Exercise>() {
            @Override
            public Exercise createFromParcel(Parcel in) {
                return new Exercise(in);
            }

            @Override
            public Exercise[] newArray(int size) {
                return new Exercise[size];
            }
        };
    }
}