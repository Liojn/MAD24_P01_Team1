package sg.edu.np.mad.fitnessultimate.training.counter;

import android.content.Context;
import android.graphics.Bitmap;

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
        inputImageBuffer = TensorImage.fromBitmap(bitmap);
        outputBuffer = TensorBuffer.createFixedSize(new int[]{1, 1, NUM_KEYPOINTS, 3}, org.tensorflow.lite.DataType.FLOAT32);
        interpreter.run(inputImageBuffer.getBuffer(), outputBuffer.getBuffer().rewind());
        float[] output = outputBuffer.getFloatArray();
        float[][][] poses = new float[1][NUM_KEYPOINTS][3];

        for (int i = 0; i < NUM_KEYPOINTS; i++) {
            poses[0][i][0] = output[i * 3];
            poses[0][i][1] = output[i * 3 + 1];
            poses[0][i][2] = output[i * 3 + 2];
        }

        return poses;
    }
}