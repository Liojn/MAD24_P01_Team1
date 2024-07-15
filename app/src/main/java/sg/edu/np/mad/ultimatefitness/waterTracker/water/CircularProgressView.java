package sg.edu.np.mad.ultimatefitness.waterTracker.water;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import sg.edu.np.mad.ultimatefitness.R;

public class CircularProgressView extends View {
    private Paint backgroundPaint;
    private Paint progressPaint;
    private int maxProgress = 2000;
    private int currentProgress = 0;

    public CircularProgressView(Context context) {
        super(context);
        init(context);
    }

    public CircularProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CircularProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        backgroundPaint = new Paint();
        backgroundPaint.setColor(ContextCompat.getColor(context, R.color.white)); // Initial white background
        backgroundPaint.setStyle(Paint.Style.FILL);

        progressPaint = new Paint();
        progressPaint.setColor(ContextCompat.getColor(context, R.color.blue)); // Blue progress color
        progressPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        // Draw the background
        canvas.drawCircle(width / 2, height / 2, Math.min(width, height) / 2, backgroundPaint);

        // Calculate the sweep angle based on the current progress
        float sweepAngle = (float) currentProgress / maxProgress * 360;

        // Draw the progress
        if (sweepAngle > 0) {
            canvas.drawArc(0, 0, width, height, -90, sweepAngle, true, progressPaint);
        }
    }

    public void setProgress(int progress) {
        this.currentProgress = progress;
        invalidate();
    }
}
