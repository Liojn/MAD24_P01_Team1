package sg.edu.np.mad.fitnessultimate.training.counter;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import sg.edu.np.mad.fitnessultimate.R;

public class PushupCounterActivity extends AppCompatActivity {
    private static final String TAG = "PushupCounterActivity";
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private static final int PREVIEW_WIDTH = 640;
    private static final int PREVIEW_HEIGHT = 480;

    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private CameraDevice cameraDevice;
    private CameraCaptureSession captureSession;
    private CaptureRequest.Builder captureRequestBuilder;
    private ImageReader imageReader;
    private Handler backgroundHandler;
    private HandlerThread backgroundThread;
    private Semaphore cameraOpenCloseLock = new Semaphore(1);

    private PoseNet poseNet;
    private TextView pushupCountTextView;
    private TextView countdownTextView;

    private int pushupCount = 0;
    private boolean isDownPosition = false;
    private static final float PUSHUP_THRESHOLD = 0.15f;
    private static final float CONFIDENCE_THRESHOLD = 0.15f;

    private boolean countdownFinished = false;
    private CountDownTimer countdownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posenet_workout);

        surfaceView = findViewById(R.id.surfaceView);
        pushupCountTextView = findViewById(R.id.textView8);
        countdownTextView = findViewById(R.id.textView7);

        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(surfaceHolderCallback);

        initPoseNet();
        startCountdown();
    }

    private void initPoseNet() {
        try {
            poseNet = new PoseNet(this);
        } catch (IOException e) {
            Log.e(TAG, "Error initializing PoseNet", e);
            Toast.makeText(this, "Failed to initialize PoseNet. Please restart the app.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void startCountdown() {
        countdownTimer = new CountDownTimer(6000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                countdownTextView.setText("Start in \n" + millisUntilFinished / 1000);
            }

            @Override
            public void onFinish() {
                countdownTextView.setVisibility(View.INVISIBLE);
                countdownFinished = true;
            }
        }.start();
    }

    private void openCamera() {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            String cameraId = manager.getCameraIdList()[0];
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                return;
            }
            manager.openCamera(cameraId, stateCallback, backgroundHandler);
        } catch (CameraAccessException e) {
            Log.e(TAG, "Failed to open camera", e);
            Toast.makeText(this, "Failed to open camera. Please restart the app.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraOpenCloseLock.release();
            cameraDevice = camera;
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            cameraOpenCloseLock.release();
            camera.close();
            cameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            cameraOpenCloseLock.release();
            camera.close();
            cameraDevice = null;
            finish();
        }
    };

    private void createCameraPreviewSession() {
        try {
            Surface surface = surfaceHolder.getSurface();

            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);

            imageReader = ImageReader.newInstance(PREVIEW_WIDTH, PREVIEW_HEIGHT, ImageFormat.YUV_420_888, 2);
            imageReader.setOnImageAvailableListener(imageAvailableListener, backgroundHandler);
            captureRequestBuilder.addTarget(imageReader.getSurface());

            cameraDevice.createCaptureSession(Arrays.asList(surface, imageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            if (cameraDevice == null) {
                                return;
                            }
                            captureSession = session;
                            try {
                                captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                                captureSession.setRepeatingRequest(captureRequestBuilder.build(), null, backgroundHandler);
                            } catch (CameraAccessException e) {
                                Log.e(TAG, "Failed to start camera preview", e);
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                            Log.e(TAG, "Failed to configure camera session");
                        }
                    }, null);
        } catch (CameraAccessException e) {
            Log.e(TAG, "Failed to create camera preview session", e);
        }
    }

    private final ImageReader.OnImageAvailableListener imageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Image image = null;
            try {
                image = reader.acquireLatestImage();
                if (image != null && countdownFinished) {
                    Bitmap bitmap = imageToBitmap(image);
                    if (bitmap != null) {
                        processImage(bitmap);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error processing image", e);
            } finally {
                if (image != null) {
                    image.close();
                }
            }
        }
    };

    private Bitmap imageToBitmap(Image image) {
        ByteBuffer yBuffer = image.getPlanes()[0].getBuffer();
        ByteBuffer uBuffer = image.getPlanes()[1].getBuffer();
        ByteBuffer vBuffer = image.getPlanes()[2].getBuffer();

        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();

        byte[] nv21 = new byte[ySize + uSize + vSize];

        yBuffer.get(nv21, 0, ySize);
        vBuffer.get(nv21, ySize, vSize);
        uBuffer.get(nv21, ySize + vSize, uSize);

        YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, image.getWidth(), image.getHeight(), null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, yuvImage.getWidth(), yuvImage.getHeight()), 75, out);

        byte[] imageBytes = out.toByteArray();
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }

    private void processImage(Bitmap bitmap) {
        PoseNet.Person person = poseNet.estimateSinglePose(bitmap);
        if (person != null) {
            Log.d(TAG, "Raw model output:");
            for (int i = 0; i < person.keyPoints.length; i++) {
                PoseNet.KeyPoint kp = person.keyPoints[i];
                Log.d(TAG, String.format("Keypoint %d: x=%.2f, y=%.2f, score=%.2f",
                        i, kp.position.x, kp.position.y, kp.score));
            }
            detectPushup(person);
            drawPose(person, bitmap);
        } else {
            Log.e(TAG, "Pose estimation failed: person is null");
        }
    }

    private void detectPushup(PoseNet.Person person) {
        PoseNet.KeyPoint nose = person.keyPoints[PoseNet.BodyPart.NOSE.ordinal()];
        PoseNet.KeyPoint leftShoulder = person.keyPoints[PoseNet.BodyPart.LEFT_SHOULDER.ordinal()];
        PoseNet.KeyPoint rightShoulder = person.keyPoints[PoseNet.BodyPart.RIGHT_SHOULDER.ordinal()];
        PoseNet.KeyPoint leftElbow = person.keyPoints[PoseNet.BodyPart.LEFT_ELBOW.ordinal()];
        PoseNet.KeyPoint rightElbow = person.keyPoints[PoseNet.BodyPart.RIGHT_ELBOW.ordinal()];

        // Log keypoint positions and scores
        Log.d(TAG, String.format("Nose: (%.2f, %.2f), score: %.2f", nose.position.x, nose.position.y, nose.score));
        Log.d(TAG, String.format("Left Shoulder: (%.2f, %.2f), score: %.2f", leftShoulder.position.x, leftShoulder.position.y, leftShoulder.score));
        Log.d(TAG, String.format("Right Shoulder: (%.2f, %.2f), score: %.2f", rightShoulder.position.x, rightShoulder.position.y, rightShoulder.score));
        Log.d(TAG, String.format("Left Elbow: (%.2f, %.2f), score: %.2f", leftElbow.position.x, leftElbow.position.y, leftElbow.score));
        Log.d(TAG, String.format("Right Elbow: (%.2f, %.2f), score: %.2f", rightElbow.position.x, rightElbow.position.y, rightElbow.score));

        // Use the shoulder with higher confidence
        PoseNet.KeyPoint bestShoulder = leftShoulder.score > rightShoulder.score ? leftShoulder : rightShoulder;
        PoseNet.KeyPoint bestElbow = leftElbow.score > rightElbow.score ? leftElbow : rightElbow;

        if (nose.score > CONFIDENCE_THRESHOLD && bestShoulder.score > CONFIDENCE_THRESHOLD && bestElbow.score > CONFIDENCE_THRESHOLD) {
            float shoulderY = bestShoulder.position.y;
            float elbowY = bestElbow.position.y;

            Log.d(TAG, String.format("Best Shoulder Y: %.2f, Best Elbow Y: %.2f", shoulderY, elbowY));

            if (nose.position.y > shoulderY + PUSHUP_THRESHOLD && elbowY > shoulderY) {
                if (!isDownPosition) {
                    Log.d(TAG, "Down position detected");
                    isDownPosition = true;
                }
            } else if (nose.position.y < shoulderY - PUSHUP_THRESHOLD && elbowY < shoulderY && isDownPosition) {
                isDownPosition = false;
                pushupCount++;
                Log.d(TAG, "Pushup counted. Total: " + pushupCount);
                runOnUiThread(() -> pushupCountTextView.setText("Pushups: " + pushupCount));
            }
        } else {
            Log.d(TAG, "Keypoint confidence below threshold");
        }
    }


    private void drawPose(PoseNet.Person person, Bitmap bitmap) {
        Canvas canvas = surfaceHolder.lockCanvas();
        if (canvas != null) {
            canvas.drawBitmap(bitmap, new Matrix(), null);
            Paint paint = new Paint();
            paint.setColor(Color.RED);
            paint.setStrokeWidth(8f);

            for (PoseNet.KeyPoint keyPoint : person.keyPoints) {
                if (keyPoint.score > CONFIDENCE_THRESHOLD) {
                    canvas.drawCircle(keyPoint.position.x, keyPoint.position.y, 10f, paint);
                }
            }

            surfaceHolder.unlockCanvasAndPost(canvas);
        } else {
            Log.e(TAG, "Failed to lock canvas: canvas is null");
        }
    }

    private final SurfaceHolder.Callback surfaceHolderCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            openCamera();
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
        }
    };

    private void startBackgroundThread() {
        backgroundThread = new HandlerThread("CameraBackground");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        backgroundThread.quitSafely();
        try {
            backgroundThread.join();
            backgroundThread = null;
            backgroundHandler = null;
        } catch (InterruptedException e) {
            Log.e(TAG, "Error stopping background thread", e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startBackgroundThread();
        if (surfaceHolder.getSurface().isValid()) {
            openCamera();
        }
    }

    @Override
    protected void onPause() {
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

    private void closeCamera() {
        try {
            cameraOpenCloseLock.acquire();
            if (captureSession != null) {
                captureSession.close();
                captureSession = null;
            }
            if (cameraDevice != null) {
                cameraDevice.close();
                cameraDevice = null;
            }
            if (imageReader != null) {
                imageReader.close();
                imageReader = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            cameraOpenCloseLock.release();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission is required.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (poseNet != null) {
            poseNet.close();
        }
        if (countdownTimer != null) {
            countdownTimer.cancel();
        }
    }
}