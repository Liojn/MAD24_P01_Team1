package sg.edu.np.mad.fitnessultimate.training.counter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.MappedByteBuffer;

public class PoseNet {
    private Interpreter interpreter;
    private TensorImage inputImageBuffer;
    private TensorBuffer outputBuffer;
    private static final int NUM_KEYPOINTS = 17;

    public PoseNet(Context context) throws IOException {
        MappedByteBuffer tfliteModel = FileUtil.loadMappedFile(context, "multipose-lightning-tflite-float16.tflite");
        interpreter = new Interpreter(tfliteModel);
    }

    public float[][][] detectPoses(Bitmap bitmap) {
        Bitmap resizedBitmap = resizeAndPadBitmap(bitmap, 256);

        inputImageBuffer = TensorImage.fromBitmap(resizedBitmap);
        outputBuffer = TensorBuffer.createFixedSize(new int[]{1, 6, 56}, org.tensorflow.lite.DataType.FLOAT32);
        interpreter.run(inputImageBuffer.getBuffer(), outputBuffer.getBuffer().rewind());

        return convertOutputToArray(outputBuffer);
    }

    private Bitmap resizeAndPadBitmap(Bitmap bitmap, int targetSize) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float scale = Math.min((float) targetSize / height, (float) targetSize / width);

        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);

        int targetWidth = (int) Math.ceil(scaledBitmap.getWidth() / 32.0) * 32;
        int targetHeight = (int) Math.ceil(scaledBitmap.getHeight() / 32.0) * 32;

        Bitmap paddedBitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(paddedBitmap);
        canvas.drawBitmap(scaledBitmap, 0, 0, null);

        return paddedBitmap;
    }

    private float[][][] convertOutputToArray(TensorBuffer outputBuffer) {
        float[] output = outputBuffer.getFloatArray();
        float[][][] poses = new float[1][6][56];

        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 56; j++) {
                poses[0][i][j] = output[i * 56 + j];
            }
        }

        return poses;
    }
}