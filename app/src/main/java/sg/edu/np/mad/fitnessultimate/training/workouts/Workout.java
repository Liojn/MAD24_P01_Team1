package sg.edu.np.mad.fitnessultimate.training.workouts;

import android.os.Parcelable;
import android.os.Parcel;
import android.util.Log;

import java.util.List;

public class Workout implements Parcelable {
    private String name;
    private String imageUri;
    private String description;
    private int breakTimeInMinutes;
    private int estimatedTimeInMinutes;
    private List<Exercise> exercises;

    public Workout(String name, String imageUri, String description, int breakTimeInMinutes, int estimatedTimeInMinutes, List<Exercise> exercises) {
        this.name = name;
        this.imageUri = imageUri;
        this.description = description;
        this.breakTimeInMinutes = breakTimeInMinutes;
        this.estimatedTimeInMinutes = estimatedTimeInMinutes;
        this.exercises = exercises;
    }

    // parcelable implementation to pass workout object between activities
    protected Workout(Parcel in) {
        name = in.readString();
        imageUri = in.readString();
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
        dest.writeString(imageUri);
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

    public String getImageUri() {
        return imageUri;
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
        private String count;
        private String description;
        private int sets;

        public Exercise(String name, String count, String description, int sets) {
            this.name = name;
            this.count = count;
            this.description = description;
            this.sets = sets;
        }

        public String getName() {
            return name;
        }

        public String getCount() {
            return count;
        }

        public int getSets() {
            return sets;
        }

        public String getDescription() {
            return description;
        }

        protected Exercise(Parcel in) {
            name = in.readString();
            count = in.readString();
            description = in.readString();
            sets = in.readInt();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(name);
            dest.writeString(count);
            dest.writeString(description);
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
