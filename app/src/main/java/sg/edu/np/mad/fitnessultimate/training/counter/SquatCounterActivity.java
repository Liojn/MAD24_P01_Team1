package sg.edu.np.mad.fitnessultimate.training.counter;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
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
import android.os.Looper;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;

import sg.edu.np.mad.fitnessultimate.R;

public class SquatCounterActivity extends AppCompatActivity {
    private static final String TAG = "SquatCounterActivity";
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private static final int PREVIEW_WIDTH = 640;
    private static final int PREVIEW_HEIGHT = 480;

    private Button backButton;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private CameraDevice cameraDevice;
    private CameraCaptureSession captureSession;
    private CaptureRequest.Builder captureRequestBuilder;
    private ImageReader imageReader;
    private Handler backgroundHandler;
    private HandlerThread backgroundThread;
    private Semaphore cameraOpenCloseLock = new Semaphore(1);

    private MoveNet moveNet;
    private TextView squatCountTextView;
    private TextView countdownTextView;

    private int squatCount = 0;
    private boolean isInSquatPosition = false;
    private static final float SQUAT_THRESHOLD = 0.50f;
    private static final float CONFIDENCE_THRESHOLD = 0.3f;
    private static final long MIN_SQUAT_DURATION = 800;
    private static final int WINDOW_SIZE = 20;
    private Queue<Float> noseYPositions = new LinkedList<>();
    private float highestNoseY = Float.MAX_VALUE;
    private float lowestNoseY = 0;
    private long squatStartTime = 0;

    private boolean countdownFinished = false;
    private CountDownTimer countdownTimer;
    private boolean isProcessingImage = false;
    private Handler processingHandler;
    private static final long PROC_DELAY = 150;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_squat_counter);

        surfaceView = findViewById(R.id.surfaceView);
        squatCountTextView = findViewById(R.id.textView8);
        countdownTextView = findViewById(R.id.textView7);
        backButton = findViewById(R.id.backbutton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Toast.makeText(SquatCounterActivity.this, "Congrats! You did " + squatCount + " squats.", Toast.LENGTH_SHORT).show();
            }
        });

        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(surfaceHolderCallback);

        processingHandler = new Handler(Looper.getMainLooper());
        initMoveNet();
        startCountdown();
    }

    private void initMoveNet() {
        try {
            moveNet = new MoveNet(this);
        } catch (IOException e) {
            Log.e(TAG, "Error initializing MoveNet", e);
            Toast.makeText(this, "Failed to initialize MoveNet. Please restart the app.", Toast.LENGTH_LONG).show();
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
            // This should be the back camera
            String cameraId = manager.getCameraIdList()[1];
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
            if (isProcessingImage) return;

            Image image = null;
            try {
                image = reader.acquireLatestImage();
                if (image != null && countdownFinished) {
                    Bitmap bitmap = imageToBitmap(image);
                    if (bitmap != null) {
                        isProcessingImage = true;
                        processingHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                processImage(bitmap);
                            }
                        }, PROC_DELAY);
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
        float[][] keypoints = moveNet.estimateSinglePose(bitmap);
        detectSquat(keypoints);
        isProcessingImage = false;
    }

    private void detectSquat(float[][] keypoints) {
        float[] nose = keypoints[MoveNet.BodyPart.NOSE.ordinal()];

        if (nose[2] > CONFIDENCE_THRESHOLD) {
            float noseY = nose[1];
            updateNosePositions(noseY);

            float currentRange = highestNoseY - lowestNoseY;
            float currentPosition = noseY - lowestNoseY;
            float relativePosition = currentPosition / currentRange;

            Log.w(TAG, String.format("Nose Y: %.2f, Relative Position: %.2f", noseY, relativePosition));

            long currentTime = System.currentTimeMillis();

            if (relativePosition < SQUAT_THRESHOLD && !isInSquatPosition) {
                Log.w(TAG, "Entered squat position");
                isInSquatPosition = true;
                squatStartTime = currentTime;
            } else if (relativePosition > 1 - SQUAT_THRESHOLD && isInSquatPosition) {
                if (currentTime - squatStartTime >= MIN_SQUAT_DURATION) {
                    isInSquatPosition = false;
                    squatCount++;
                    Log.w(TAG, "Squat counted. Total: " + squatCount);
                    runOnUiThread(() -> squatCountTextView.setText("Squats: " + squatCount));
                } else {
                    Log.w(TAG, "Squat too short, not counted");
                }
            }
        } else {
            Log.e(TAG, "Nose confidence below threshold");
        }
    }

    private void updateNosePositions(float noseY) {
        noseYPositions.offer(noseY);
        if (noseYPositions.size() > WINDOW_SIZE) {
            noseYPositions.poll();
        }

        highestNoseY = Float.MAX_VALUE;
        lowestNoseY = 0;
        for (float y : noseYPositions) {
            if (y < highestNoseY) highestNoseY = y;
            if (y > lowestNoseY) lowestNoseY = y;
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
        if (moveNet != null) {
            moveNet.close();
        }
        if (countdownTimer != null) {
            countdownTimer.cancel();
        }
    }
}