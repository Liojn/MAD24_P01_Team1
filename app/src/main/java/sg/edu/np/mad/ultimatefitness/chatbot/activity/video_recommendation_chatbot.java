package sg.edu.np.mad.ultimatefitness.chatbot.activity;

import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import sg.edu.np.mad.ultimatefitness.R;

public class video_recommendation_chatbot extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;
    private GestureDetector gestureDetector;
    private View.OnTouchListener gestureListener;

    public video_recommendation_chatbot() {
        // Required empty public constructor
    }

    public static video_recommendation_chatbot newInstance(String param1, String param2) {
        video_recommendation_chatbot fragment = new video_recommendation_chatbot();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e2.getY() - e1.getY() > 100 && Math.abs(velocityY) > 1000) {
                    getParentFragmentManager().beginTransaction().remove(video_recommendation_chatbot.this).commit();
                    return true;
                }
                return false;
            }
        });

        gestureListener = (v, event) -> gestureDetector.onTouchEvent(event);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_video_recommendation_chatbot, container, false);

        TextView dragDownToClose = view.findViewById(R.id.dragDownToClose);

        // Load YouTube video
        WebView webView = view.findViewById(R.id.videoWebView);
        loadYouTubeVideo(webView, "IODxDxX7oi4");

        // Set touch listener to detect gestures
        view.setOnTouchListener(gestureListener);

        // Set touch listener specifically for the dragDownToClose TextView
        dragDownToClose.setOnTouchListener(gestureListener);

        return view;
    }

    private void loadYouTubeVideo(WebView webView, String videoId) {
        webView.setWebViewClient(new WebViewClient());
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        String videoUrl = "https://www.youtube.com/embed/" + videoId;
        String html = "<html><body style='margin:0;padding:0;'><iframe width='100%' height='100%' src='" + videoUrl + "' frameborder='0' allow='autoplay; encrypted-media' allowfullscreen></iframe></body></html>";
        webView.loadData(html, "text/html", "utf-8");
    }
}
