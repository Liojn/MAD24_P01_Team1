package sg.edu.np.mad.fitnessultimate.training.counter;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageView;
import android.widget.TextView;
import android.util.Size;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import sg.edu.np.mad.fitnessultimate.R;

public class PushupCounterActivity extends AppCompatActivity {
    private PoseNet posenet;
    private CameraDevice cameraDevice;
    private CameraCaptureSession captureSession;
    private ImageReader imageReader;
    private ImageView poseImageView;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private TextView pushupCountTextView;
    private int pushupCount = 0;
    private boolean isDownPosition = false;
    private static final float CONFIDENCE_THRESHOLD = 0.5f;
    private static final float PUSHUP_THRESHOLD = 0.15f;


    private enum MoveNetData {
        // Keypoints
        NOSE(0),
        LEFT_EYE(1),
        RIGHT_EYE(2),
        LEFT_EAR(3),
        RIGHT_EAR(4),
        LEFT_SHOULDER(5),
        RIGHT_SHOULDER(6),
        LEFT_ELBOW(7),
        RIGHT_ELBOW(8),
        LEFT_WRIST(9),
        RIGHT_WRIST(10),
        LEFT_HIP(11),
        RIGHT_HIP(12),
        LEFT_KNEE(13),
        RIGHT_KNEE(14),
        LEFT_ANKLE(15),
        RIGHT_ANKLE(16),

        // Bounding box
        BOUNDING_BOX_YMIN(51),
        BOUNDING_BOX_XMIN(52),
        BOUNDING_BOX_YMAX(53),
        BOUNDING_BOX_XMAX(54),
        BOUNDING_BOX_SCORE(55);

        private final int index;

        MoveNetData(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }

        public float getY(float[] data) {
            return data[index * 3];
        }

        public float getX(float[] data) {
            return data[index * 3 + 1];
        }

        public float getScore(float[] data) {
            return data[index * 3 + 2];
        }

        public static float getBoundingBoxValue(float[] data, MoveNetData boundingBoxItem) {
            if (boundingBoxItem.getIndex() < BOUNDING_BOX_YMIN.getIndex()) {
                throw new IllegalArgumentException("Not a bounding box item");
            }
            return data[boundingBoxItem.getIndex()];
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pushup_counter);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        poseImageView = findViewById(R.id.poseImageView);
        surfaceView = findViewById(R.id.surfaceView);
        pushupCountTextView = findViewById(R.id.pushupCountTextView);

        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                setupCamera();
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
                // Handle surface changes if needed
            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
                // Handle surface destruction if needed
            }
        });

        try {
            posenet = new PoseNet(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            setupCamera();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 0);
        }
    }

    private void setupCamera() {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            String cameraId = null;
            for (String id : manager.getCameraIdList()) {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(id);
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_BACK) {
                    cameraId = id;
                    break;
                }
            }

            if (cameraId == null) {
                throw new RuntimeException("No back facing camera found.");
            }

            Size[] sizes = manager.getCameraCharacteristics(cameraId).get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.YUV_420_888);
            Size size = sizes[0];
            imageReader = ImageReader.newInstance(size.getWidth()*2, size.getHeight()*2, ImageFormat.YUV_420_888, 2);
            imageReader.setOnImageAvailableListener(reader -> {
                Image image = reader.acquireNextImage();
                if (image != null) {
                    ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                    Bitmap largeBitmap = Bitmap.createBitmap(size.getWidth(), size.getHeight(), Bitmap.Config.ARGB_8888);
                    Bitmap bitmap = Bitmap.createScaledBitmap(largeBitmap, size.getWidth() / 2, size.getHeight() / 2, true);
                    bitmap.copyPixelsFromBuffer(buffer);
                    detectPose(bitmap);
                    image.close();
                }
            }, null);

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            manager.openCamera(cameraId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    cameraDevice = camera;
                    startPreview();
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {
                    cameraDevice.close();
                    cameraDevice = null;
                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {
                    if (cameraDevice != null)
                        cameraDevice.close();
                    cameraDevice = null;
                }
            }, null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void startPreview() {
        try {
            Surface surface = surfaceHolder.getSurface();
            CaptureRequest.Builder requestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            requestBuilder.addTarget(surface);
            requestBuilder.addTarget(imageReader.getSurface());

            cameraDevice.createCaptureSession(Arrays.asList(surface, imageReader.getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    captureSession = session;
                    try {
                        CaptureRequest request = requestBuilder.build();
                        captureSession.setRepeatingRequest(request, new CameraCaptureSession.CaptureCallback() {
                            @Override
                            public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                                super.onCaptureCompleted(session, request, result);
                            }
                        }, null);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Toast.makeText(PushupCounterActivity.this, "Configuration change", Toast.LENGTH_SHORT).show();
                }
            }, null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void detectPose(Bitmap bitmap) {
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        float[][][] poses = posenet.detectPoses(rotatedBitmap);

        if (poses != null && poses[0].length > 0) {
            float[] person = poses[0][0];

            float noseY = MoveNetData.NOSE.getY(person);
            float leftShoulderY = MoveNetData.LEFT_SHOULDER.getY(person);
            float rightShoulderY = MoveNetData.RIGHT_SHOULDER.getY(person);
            float leftElbowY = MoveNetData.LEFT_ELBOW.getY(person);
            float rightElbowY = MoveNetData.RIGHT_ELBOW.getY(person);

            float noseScore = MoveNetData.NOSE.getScore(person);
            float leftShoulderScore = MoveNetData.LEFT_SHOULDER.getScore(person);
            float rightShoulderScore = MoveNetData.RIGHT_SHOULDER.getScore(person);
            float leftElbowScore = MoveNetData.LEFT_ELBOW.getScore(person);
            float rightElbowScore = MoveNetData.RIGHT_ELBOW.getScore(person);

            if (noseScore > CONFIDENCE_THRESHOLD && leftShoulderScore > CONFIDENCE_THRESHOLD &&
                    rightShoulderScore > CONFIDENCE_THRESHOLD && leftElbowScore > CONFIDENCE_THRESHOLD &&
                    rightElbowScore > CONFIDENCE_THRESHOLD) {

                float averageShoulderY = (leftShoulderY + rightShoulderY) / 2;
                float averageElbowY = (leftElbowY + rightElbowY) / 2;

                if (noseY > averageShoulderY + PUSHUP_THRESHOLD && averageElbowY > averageShoulderY) {
                    isDownPosition = true;
                } else if (noseY < averageShoulderY - PUSHUP_THRESHOLD && averageElbowY < averageShoulderY && isDownPosition) {
                    isDownPosition = false;
                    pushupCount++;
                    runOnUiThread(() -> pushupCountTextView.setText(String.valueOf(pushupCount)));
                }
            }

            // Render joints and labels on the bitmap
            Bitmap poseBitmap = renderPoseWithLabels(rotatedBitmap, person);
            runOnUiThread(() -> poseImageView.setImageBitmap(poseBitmap));
        }
    }

    private Bitmap renderPoseWithLabels(Bitmap bitmap, float[] pose) {
        Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBitmap);
        Paint paint = new Paint();
        paint.setTextSize(30f);

        for (MoveNetData keypoint : MoveNetData.values()) {
            if (keypoint.getIndex() > MoveNetData.RIGHT_ANKLE.getIndex()) {
                continue;  // Skip bounding box data
            }

            float x = keypoint.getX(pose) * bitmap.getWidth();
            float y = keypoint.getY(pose) * bitmap.getHeight();
            float confidence = keypoint.getScore(pose);

            if (confidence > CONFIDENCE_THRESHOLD) {
                // Draw joint
                paint.setColor(Color.RED);
                canvas.drawCircle(x, y, 10, paint);

                // Draw label
                paint.setColor(Color.WHITE);
                canvas.drawText(keypoint.name(), x + 15, y + 15, paint);
            }
        }

        return mutableBitmap;
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraDevice != null) {
            cameraDevice.close();
        }
    }
}