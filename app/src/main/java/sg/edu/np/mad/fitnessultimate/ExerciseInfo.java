package sg.edu.np.mad.fitnessultimate;

import android.os.Parcel;
import android.os.Parcelable;

// implementation from https://stackoverflow.com/a/71545430
public class ExerciseInfo implements Parcelable {
    private String name;
    private String description;
    private String videoUrl;
    private String muscleGroup;
    private String difficulty;

    public ExerciseInfo(String name, String description, String videoUrl, String imageUrl, String equipment, String muscleGroup, String difficulty, String type) {
        this.name = name;
        this.description = description;
        this.videoUrl = videoUrl;
        this.muscleGroup = muscleGroup;
        this.difficulty = difficulty;
    }

    protected ExerciseInfo(Parcel in) {
        name = in.readString();
        description = in.readString();
        videoUrl = in.readString();
        muscleGroup = in.readString();
        difficulty = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(description);
        dest.writeString(videoUrl);
        dest.writeString(muscleGroup);
        dest.writeString(difficulty);
    }

    public static final Parcelable.Creator<ExerciseInfo> CREATOR = new Creator<ExerciseInfo>() {
        @Override
        public ExerciseInfo createFromParcel(Parcel in) {
            return new ExerciseInfo(in);
        }

        @Override
        public ExerciseInfo[] newArray(int size) {
            return new ExerciseInfo[size];
        }
    };

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public String getMuscleGroup() {
        return muscleGroup;
    };

    public String getDifficulty() {
        return difficulty;
    }
}
