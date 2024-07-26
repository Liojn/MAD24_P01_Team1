package sg.edu.np.mad.ultimatefitness.chatbot.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import sg.edu.np.mad.ultimatefitness.R;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {

    private List<String> videoIds;
    private Context context;

    public VideoAdapter(List<String> videoIds, Context context) {
        this.videoIds = videoIds;
        this.context = context;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chatbot_video, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        String videoId = videoIds.get(position);
        holder.loadYouTubeVideo(videoId);
    }

    @Override
    public int getItemCount() {
        return videoIds.size();
    }

    static class VideoViewHolder extends RecyclerView.ViewHolder {
        WebView webView;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            webView = itemView.findViewById(R.id.videoWebView);
        }

        void loadYouTubeVideo(String videoId) {
            webView.setWebViewClient(new WebViewClient());
            WebSettings webSettings = webView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            String videoUrl = "https://www.youtube.com/embed/" + videoId;
            String html = "<html><body style='margin:0;padding:0;'><iframe width='100%' height='100%' src='" + videoUrl + "' frameborder='0' allow='autoplay; encrypted-media' allowfullscreen></iframe></body></html>";
            webView.loadData(html, "text/html", "utf-8");
        }
    }
}
