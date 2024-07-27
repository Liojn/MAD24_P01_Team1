package sg.edu.np.mad.fitnessultimate.HomePage;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

import sg.edu.np.mad.fitnessultimate.R;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder> {

    private List<BannerItem> itemList;
    private Context context;

    public BannerAdapter(Context context, List<BannerItem> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_image_view, parent, false);
        return new BannerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
        BannerItem item = itemList.get(position);
        holder.imageView.setImageResource(item.getImageResourceId());
        holder.textViewTitle.setText(item.getTitle());

        holder.itemView.setOnClickListener(v -> {
            if (item.getTargetActivity() != null) {
                Intent intent = new Intent(context, item.getTargetActivity());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class BannerViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textViewTitle;

        public BannerViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
        }
    }
}
