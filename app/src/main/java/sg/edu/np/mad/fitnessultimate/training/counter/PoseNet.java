package sg.edu.np.mad.fitnessultimate.training.counter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class PoseNet {
    private static final String TAG = "PoseNet";
    private Interpreter interpreter;
    private ByteBuffer inputBuffer;
    private int[] inputShape = {1, 1, 1, 3};  // As per the log output

    public PoseNet(Context context) throws IOException {
        Interpreter.Options options = new Interpreter.Options();
        options.setNumThreads(4);
        interpreter = new Interpreter(FileUtil.loadMappedFile(context, "multipose-lightning-tflite-float16.tflite"), options);

        // Prepare input buffer
        int bufferSize = 1 * 1 * 1 * 3; // [1, 1, 1, 3]
        inputBuffer = ByteBuffer.allocateDirect(bufferSize);
        inputBuffer.order(ByteOrder.nativeOrder());
    }

    public float[][][] detectPoses(Bitmap bitmap) {
        // Prepare input: average RGB values of the entire image
        inputBuffer.rewind();
        int pixelCount = bitmap.getWidth() * bitmap.getHeight();
        int redSum = 0, greenSum = 0, blueSum = 0;
        int[] pixels = new int[pixelCount];
        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        for (int pixel : pixels) {
            redSum += Color.red(pixel);
            greenSum += Color.green(pixel);
            blueSum += Color.blue(pixel);
        }

        inputBuffer.put((byte) (redSum / pixelCount));
        inputBuffer.put((byte) (greenSum / pixelCount));
        inputBuffer.put((byte) (blueSum / pixelCount));

        // Prepare output
        float[][][] outputArray = new float[1][6][56];

        // Run inference
        interpreter.run(inputBuffer, outputArray);

        return outputArray;
    }
}