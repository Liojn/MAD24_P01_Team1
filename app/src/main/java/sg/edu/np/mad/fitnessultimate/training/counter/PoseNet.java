package sg.edu.np.mad.fitnessultimate.training.counter;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.gpu.CompatibilityList;
import org.tensorflow.lite.gpu.GpuDelegate;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class PoseNet {
    private static final String TAG = "PoseNet";
    private static final int NUM_KEYPOINTS = 17;
    private static final int INPUT_SIZE = 192;
    private Interpreter interpreter;
    private ByteBuffer inputBuffer;
    private GpuDelegate gpuDelegate;

    public enum BodyPart {
        NOSE, LEFT_EYE, RIGHT_EYE, LEFT_EAR, RIGHT_EAR,
        LEFT_SHOULDER, RIGHT_SHOULDER, LEFT_ELBOW, RIGHT_ELBOW,
        LEFT_WRIST, RIGHT_WRIST, LEFT_HIP, RIGHT_HIP,
        LEFT_KNEE, RIGHT_KNEE, LEFT_ANKLE, RIGHT_ANKLE
    }

    public PoseNet(Context context) throws IOException {
        Interpreter.Options options = new Interpreter.Options();
        CompatibilityList compatList = new CompatibilityList();

        if(compatList.isDelegateSupportedOnThisDevice()){
            gpuDelegate = new GpuDelegate();
            options.addDelegate(gpuDelegate);
        } else {
            options.setNumThreads(4);
        }

        interpreter = new Interpreter(FileUtil.loadMappedFile(context, "singlepose-lightning.tflite"), options);
        inputBuffer = ByteBuffer.allocateDirect(1 * INPUT_SIZE * INPUT_SIZE * 3 * 4);
        inputBuffer.order(ByteOrder.nativeOrder());
    }

    public Person estimateSinglePose(Bitmap bitmap) {
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true);
        ByteBuffer inputBuffer = convertBitmapToByteBuffer(resizedBitmap);

        float[][][][] outputBuffer = new float[1][1][17][3];
        interpreter.run(inputBuffer, outputBuffer);

        return decodeSinglePose(outputBuffer[0][0], bitmap.getWidth(), bitmap.getHeight());
    }

    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        inputBuffer.rewind();
        int[] intValues = new int[INPUT_SIZE * INPUT_SIZE];
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        for (int i = 0; i < INPUT_SIZE; ++i) {
            for (int j = 0; j < INPUT_SIZE; ++j) {
                int pixelValue = intValues[i * INPUT_SIZE + j];
                inputBuffer.putFloat((pixelValue >> 16) & 0xFF);
                inputBuffer.putFloat((pixelValue >> 8) & 0xFF);
                inputBuffer.putFloat(pixelValue & 0xFF);
            }
        }
        return inputBuffer;
    }

    private Person decodeSinglePose(float[][] keypoints, int originalWidth, int originalHeight) {
        Person person = new Person();
        for (int i = 0; i < NUM_KEYPOINTS; i++) {
            float y = keypoints[i][0];
            float x = keypoints[i][1];
            float score = keypoints[i][2];

            // Log the raw output
            Log.d(TAG, String.format("Keypoint %d: x=%.2f, y=%.2f, score=%.2f", i, x, y, score));

            // Handle NaN values and convert from [0,1] to pixel coordinates
            if (Float.isNaN(x) || Float.isNaN(y) || Float.isNaN(score)) {
                x = 0f;
                y = 0f;
                score = 0f;
            } else {
                x = x * originalWidth;
                y = y * originalHeight;
            }

            person.keyPoints[i] = new KeyPoint(new Position(x, y), score);
        }
        person.score = calculatePersonScore(person.keyPoints);
        return person;
    }


    private float calculatePersonScore(KeyPoint[] keyPoints) {
        float totalScore = 0;
        for (KeyPoint keyPoint : keyPoints) {
            totalScore += keyPoint.score;
        }
        return totalScore / NUM_KEYPOINTS;
    }

    public void close() {
        if (interpreter != null) {
            interpreter.close();
        }
        if (gpuDelegate != null) {
            gpuDelegate.close();
        }
    }

    public static class Person {
        public KeyPoint[] keyPoints = new KeyPoint[NUM_KEYPOINTS];
        public float score;
    }

    public static class KeyPoint {
        public Position position;
        public float score;

        public KeyPoint(Position position, float score) {
            this.position = position;
            this.score = score;
        }
    }

    public static class Position {
        public float x;
        public float y;

        public Position(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }
}