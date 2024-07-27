package sg.edu.np.mad.fitnessultimate.training.counter;

import android.content.Context;
import android.graphics.Bitmap;


import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.gpu.CompatibilityList;
import org.tensorflow.lite.gpu.GpuDelegate;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;

import java.io.IOException;

public class MoveNet {
    private static final String TAG = "MoveNet";
    private static final int NUM_KEYPOINTS = 17;
    private static final int INPUT_SIZE = 192;

    private Interpreter interpreter;
    private ImageProcessor imageProcessor;
    private TensorImage inputImage;
    private GpuDelegate gpuDelegate;

    public enum BodyPart {
        NOSE, LEFT_EYE, RIGHT_EYE, LEFT_EAR, RIGHT_EAR,
        LEFT_SHOULDER, RIGHT_SHOULDER, LEFT_ELBOW, RIGHT_ELBOW,
        LEFT_WRIST, RIGHT_WRIST, LEFT_HIP, RIGHT_HIP,
        LEFT_KNEE, RIGHT_KNEE, LEFT_ANKLE, RIGHT_ANKLE
    }

    // initialise singlepose-lightning.tflite model
    public MoveNet(Context context) throws IOException {
        Interpreter.Options options = new Interpreter.Options();
        CompatibilityList compatList = new CompatibilityList();

        if(compatList.isDelegateSupportedOnThisDevice()){
            gpuDelegate = new GpuDelegate();
            options.addDelegate(gpuDelegate);
        } else {
            options.setNumThreads(4);
        }

        interpreter = new Interpreter(FileUtil.loadMappedFile(context, "singlepose-lightning.tflite"), options);
        imageProcessor = new ImageProcessor.Builder()
                .add(new ResizeOp(INPUT_SIZE, INPUT_SIZE, ResizeOp.ResizeMethod.BILINEAR))
                .build();
        inputImage = new TensorImage(interpreter.getInputTensor(0).dataType());
    }

    // perform inference on the input image using hardware
    public float[][] estimateSinglePose(Bitmap bitmap) {
        inputImage.load(bitmap);
        TensorImage processedImage = imageProcessor.process(inputImage);

        float[][][][] outputBuffer = new float[1][1][NUM_KEYPOINTS][3];
        interpreter.run(processedImage.getBuffer(), outputBuffer);

        return outputBuffer[0][0];
    }

    public void close() {
        if (interpreter != null) {
            interpreter.close();
        }
        if (gpuDelegate != null) {
            gpuDelegate.close();
        }
    }

    public static class KeyPoint {
        public BodyPart bodyPart;
        public float x;
        public float y;
        public float score;

        public KeyPoint(BodyPart bodyPart, float x, float y, float score) {
            this.bodyPart = bodyPart;
            this.x = x;
            this.y = y;
            this.score = score;
        }
    }
}