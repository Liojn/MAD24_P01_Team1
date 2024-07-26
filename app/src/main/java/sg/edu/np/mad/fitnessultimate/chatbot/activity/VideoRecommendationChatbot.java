package sg.edu.np.mad.fitnessultimate.chatbot.activity;

import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import sg.edu.np.mad.fitnessultimate.R;
import sg.edu.np.mad.fitnessultimate.chatbot.adapter.VideoAdapter;

public class VideoRecommendationChatbot extends Fragment {



    private static final String ARG_EXERCISE = "exercise";

    private String exercise;
    private GestureDetector gestureDetector;

    public VideoRecommendationChatbot() {
        // Required empty public constructor
    }

    public static VideoRecommendationChatbot newInstance(String exercise) {
        VideoRecommendationChatbot fragment = new VideoRecommendationChatbot();
        Bundle args = new Bundle();
        args.putString(ARG_EXERCISE, exercise);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            exercise = getArguments().getString(ARG_EXERCISE);
        }

        gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityY, float velocityX) {
                if (velocityY > 700) {
                    getParentFragmentManager().beginTransaction().remove(VideoRecommendationChatbot.this).commit();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_video_recommendation_chatbot, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.videoRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);

        List<String> videoIds = getVideoIdsForExercise(exercise);
        VideoAdapter adapter = new VideoAdapter(videoIds, getContext());
        recyclerView.setAdapter(adapter);

        // Add SnapHelper to RecyclerView
        SnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(recyclerView);

        view.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));

        return view;
    }

    private List<String> getVideoIdsForExercise(String exercise) {
        switch (exercise) {
            case "push up":
                return Arrays.asList("IODxDxX7oi4", "zkU6Ok44_CI");
            case "crunches":
                return Arrays.asList("5ER5Of4MOPI", "MKmrqcoCZ-M");
            case "pull ups":
                return Arrays.asList("fO3dKSQayfg", "eGo4IYlbE5g");
            default:
                return Collections.emptyList();
        }
    }
}

