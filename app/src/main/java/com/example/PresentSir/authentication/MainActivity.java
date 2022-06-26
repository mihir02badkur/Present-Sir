package com.raina.PresentSir.authentication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.YuvImage;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Size;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.raina.PresentSir.R;
import com.raina.PresentSir.databinding.ActivityMainBinding;
import com.raina.PresentSir.faceRecognition.SimilarityClassifier;
import com.raina.PresentSir.studentAttendance.AttendanceActivity;

import org.tensorflow.lite.Interpreter;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.ReadOnlyBufferException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private static final int MY_CAMERA_REQUEST_CODE = 100;
    private static final String MyPREFERENCES = "MyPrefs";
    public static float[][] embeddings;
    FaceDetector detector;
    Interpreter tfLite;
    CameraSelector cameraSelector;
    float distance = 1.0f;
    boolean start = true, flipX = false;
    Context context = MainActivity.this;
    int cam_face = CameraSelector.LENS_FACING_BACK; //Default Back Camera
    int[] intValues;
    int inputSize = 112;  //Input size for model
    boolean isModelQuantized = false;
    float IMAGE_MEAN = 128.0f;
    float IMAGE_STD = 128.0f;
    int OUTPUT_SIZE = 192; //Output size of model
    ProcessCameraProvider cameraProvider;
    String modelFile = "mobile_face_net.tflite"; //model name
    private ActivityMainBinding binding;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private UserViewModel userviewModel;
    private FirebaseAuth mAuth;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private HashMap<String, SimilarityClassifier.Recognition> registered = new HashMap<>(); //saved Faces

    // Image Processing - crop Bitmap
    private static Bitmap getCropBitmapByCPU(Bitmap source, RectF cropRectF) {
        Bitmap resultBitmap = Bitmap.createBitmap((int) cropRectF.width(),
                (int) cropRectF.height(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(resultBitmap);

        // draw background
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
        paint.setColor(Color.WHITE);
        canvas.drawRect(
                new RectF(0, 0, cropRectF.width(), cropRectF.height()),
                paint);

        Matrix matrix = new Matrix();
        matrix.postTranslate(-cropRectF.left, -cropRectF.top);

        canvas.drawBitmap(source, matrix, paint);

        if (source != null && !source.isRecycled()) {
            source.recycle();
        }

        return resultBitmap;
    }

    // Image Processing - rotate Bitmap
    private static Bitmap rotateBitmap(
            Bitmap bitmap, int rotationDegrees, boolean flipX) {
        Matrix matrix = new Matrix();

        // Rotate the image back to straight.
        matrix.postRotate(rotationDegrees);

        // Mirror the image along the X or Y axis.
        matrix.postScale(flipX ? -1.0f : 1.0f, 1.0f);
        Bitmap rotatedBitmap =
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        // Recycle the old bitmap if it has changed.
        if (rotatedBitmap != bitmap) {
            bitmap.recycle();
        }
        return rotatedBitmap;
    }

    //If conversion not done ,the toBitmap conversion does not work on some devices.
    private static byte[] YUV_420_888toNV21(Image image) {

        int width = image.getWidth();
        int height = image.getHeight();
        int ySize = width * height;
        int uvSize = width * height / 4;

        byte[] nv21 = new byte[ySize + uvSize * 2];

        ByteBuffer yBuffer = image.getPlanes()[0].getBuffer(); // Y
        ByteBuffer uBuffer = image.getPlanes()[1].getBuffer(); // U
        ByteBuffer vBuffer = image.getPlanes()[2].getBuffer(); // V

        int rowStride = image.getPlanes()[0].getRowStride();
        assert (image.getPlanes()[0].getPixelStride() == 1);

        int pos = 0;

        if (rowStride == width) { // likely
            yBuffer.get(nv21, 0, ySize);
            pos += ySize;
        } else {
            long yBufferPos = -rowStride; // not an actual position
            for (; pos < ySize; pos += width) {
                yBufferPos += rowStride;
                yBuffer.position((int) yBufferPos);
                yBuffer.get(nv21, pos, width);
            }
        }

        rowStride = image.getPlanes()[2].getRowStride();
        int pixelStride = image.getPlanes()[2].getPixelStride();

        assert (rowStride == image.getPlanes()[1].getRowStride());
        assert (pixelStride == image.getPlanes()[1].getPixelStride());

        if (pixelStride == 2 && rowStride == width && uBuffer.get(0) == vBuffer.get(1)) {
            // maybe V an U planes overlap as per NV21, which means vBuffer[1] is alias of uBuffer[0]
            byte savePixel = vBuffer.get(1);
            try {
                vBuffer.put(1, (byte) ~savePixel);
                if (uBuffer.get(0) == (byte) ~savePixel) {
                    vBuffer.put(1, savePixel);
                    vBuffer.position(0);
                    uBuffer.position(0);
                    vBuffer.get(nv21, ySize, 1);
                    uBuffer.get(nv21, ySize + 1, uBuffer.remaining());

                    return nv21; // shortcut
                }
            } catch (ReadOnlyBufferException ex) {
                // unfortunately, we cannot check if vBuffer and uBuffer overlap
            }

            // unfortunately, the check failed. We must save U and V pixel by pixel
            vBuffer.put(1, savePixel);
        }

        // other optimizations could check if (pixelStride == 1) or (pixelStride == 2),
        // but performance gain would be less significant

        for (int row = 0; row < height / 2; row++) {
            for (int col = 0; col < width / 2; col++) {
                int vuPos = col * pixelStride + row * rowStride;
                nv21[pos++] = vBuffer.get(vuPos);
                nv21[pos++] = uBuffer.get(vuPos);
            }
        }

        return nv21;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Add Face");

        registered = readFromSP(); //Load saved faces from memory

        binding.addFace.setVisibility(View.INVISIBLE);

        SharedPreferences sharedPref = getSharedPreferences("Distance", Context.MODE_PRIVATE);
        distance = sharedPref.getFloat("distance", 1.00f);

        binding.facePreview.setVisibility(View.INVISIBLE);
        userviewModel = new ViewModelProvider(MainActivity.this).get(UserViewModel.class);
        mAuth = FirebaseAuth.getInstance();

        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        assert result.getData() != null;
                        activityResult(result.getData().getData());
                    }
                });

        //Camera Permission
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
        }

        //On-screen switch to toggle between Cameras.
        binding.cameraSwitch.setOnClickListener(v -> {
            if (cam_face == CameraSelector.LENS_FACING_BACK) {
                cam_face = CameraSelector.LENS_FACING_FRONT;
                flipX = true;
            } else {
                cam_face = CameraSelector.LENS_FACING_BACK;
                flipX = false;
            }
            cameraProvider.unbindAll();
            cameraBind();
        });

        binding.addFace.setOnClickListener((v -> addFace()));

        binding.Upload.setOnClickListener(view -> {
            binding.defaultImg.setVisibility(View.INVISIBLE);
            loadPhoto();
        });


        binding.recognize.setOnClickListener(v -> {
            binding.defaultImg.setVisibility(View.INVISIBLE);
            if(binding.facePreview.getDrawable()!=null)
            {
                binding.facePreview.setVisibility(View.VISIBLE);
                binding.addFace.setVisibility(View.VISIBLE);
                binding.addInfo.setVisibility(View.VISIBLE);
            }
            else
                Snackbar.make(binding.getRoot(), "No Face detected, Try again!", Snackbar.LENGTH_SHORT).show();
        });

        //Load model
        try {
            tfLite = new Interpreter(loadModelFile(MainActivity.this, modelFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Initialize Face Detector
        FaceDetectorOptions highAccuracyOpts =
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                        .build();
        detector = FaceDetection.getClient(highAccuracyOpts);

        cameraBind();


    }

    // Takes student name and roll number and create student document in firebase
    private void addFace() {
        {

            start = false;
            AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogTheme);

            // Set up the input
            LinearLayout lila = new LinearLayout(this);
            lila.setOrientation(LinearLayout.VERTICAL);
            final EditText input = new EditText(this);
            final EditText rollNo = new EditText(this);
            input.setTextColor(getResources().getColor(R.color.white));
            rollNo.setTextColor(getResources().getColor(R.color.white));
            ColorStateList colorStateList = ColorStateList.valueOf(getResources().getColor(R.color.white));
            input.setBackgroundTintList(colorStateList);
            rollNo.setBackgroundTintList(colorStateList);

            input.setInputType(InputType.TYPE_CLASS_TEXT);
            rollNo.setInputType(InputType.TYPE_CLASS_NUMBER);
            input.setHint("Name");
            rollNo.setHint("Roll No");
            input.setHintTextColor(getResources().getColor(R.color.lightBg));
            rollNo.setHintTextColor(getResources().getColor(R.color.lightBg));

            lila.addView(input);
            lila.addView(rollNo);
            builder.setView(lila);

            builder.setTitle("Add Face");

            // Set up the buttons
            builder.setPositiveButton("ADD", (dialog, which) -> {
                String rollNum = rollNo.getText().toString();
                String name = input.getText().toString();

                if (rollNum.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please enter a Roll Number", Toast.LENGTH_SHORT).show();
                    rollNo.setError("Please enter a Roll Number");
                    rollNo.requestFocus();
                }
                if (name.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please enter a name", Toast.LENGTH_SHORT).show();
                    input.setError("Please enter a name");
                    input.requestFocus();
                } else {
                    FirebaseFirestore db = FirebaseFirestore.getInstance();

                    db.collection("Student").whereEqualTo("rollNumber", rollNum).get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (task.getResult().isEmpty())
                                createStudent(rollNum, name);

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if (document.exists()) {
                                    Toast.makeText(MainActivity.this, "Roll number already exists", Toast.LENGTH_SHORT).show();
                                    rollNo.setError("Roll number already exists");
                                    rollNo.requestFocus();
                                } else
                                    createStudent(rollNum, name);
                            }
                        } else
                            Toast.makeText(MainActivity.this, "Some error Occurred", Toast.LENGTH_SHORT).show();
                    });
                }
            });
            builder.setNegativeButton("Cancel", (dialog, which) -> {
                start = true;
                dialog.cancel();
            });

            builder.show();
        }
    }

    // Camera Permission OnResult
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    // Function to load model file
    private MappedByteBuffer loadModelFile(Activity activity, String MODEL_FILE) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(MODEL_FILE);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    //Bind camera and preview view
    private void cameraBind() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this in Future.
                // This should never be reached.
            }
        }, ContextCompat.getMainExecutor(this));
    }

    // Bind preview and processes image
    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder()
                .build();

        cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(cam_face)
                .build();

        preview.setSurfaceProvider(binding.preview.getSurfaceProvider());
        ImageAnalysis imageAnalysis =
                new ImageAnalysis.Builder()
                        .setTargetResolution(new Size(640, 480))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST) //Latest frame is shown
                        .build();

        Executor executor = Executors.newSingleThreadExecutor();
        imageAnalysis.setAnalyzer(executor, imageProxy -> {
            try {
                Thread.sleep(0);  //Camera preview refreshed every 10 millis(adjust as required)
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            InputImage image = null;


            @SuppressLint("UnsafeOptInUsageError")
            // Camera Feed-->Analyzer-->ImageProxy-->mediaImage-->InputImage(needed for ML kit face detection)

            Image mediaImage = imageProxy.getImage();

            if (mediaImage != null) {
                image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
            }


            //Process acquired image to detect faces
            assert image != null;
            detector.process(image)
                    .addOnSuccessListener(
                            faces -> {

                                if (faces.size() != 0) {
                                    Face face = faces.get(0); //Get first face from detected faces

                                    //mediaImage to Bitmap
                                    Bitmap frame_bmp = toBitmap(mediaImage);

                                    int rot = imageProxy.getImageInfo().getRotationDegrees();

                                    //Adjust orientation of Face
                                    Bitmap frame_bmp1 = rotateBitmap(frame_bmp, rot, false);


                                    //Get bounding box of face
                                    RectF boundingBox = new RectF(face.getBoundingBox());

                                    //Crop out bounding box from whole Bitmap(image)
                                    Bitmap cropped_face = getCropBitmapByCPU(frame_bmp1, boundingBox);

                                    if (flipX)
                                        cropped_face = rotateBitmap(cropped_face, 0, true);
                                    //Scale the acquired Face to 112*112 which is required input for model
                                    Bitmap scaled = getResizedBitmap(cropped_face, 112, 112);

                                    if (start)
                                        recognizeImage(scaled); //Send scaled bitmap to create face embeddings.

                                }

                            })
                    .addOnFailureListener(
                            e -> {
                                // Task failed with an exception
                                // ...
                            })
                    .addOnCompleteListener(task -> {

                        imageProxy.close(); //Acquire next frame for analysis
                    });


        });

        cameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis, preview);

    }

    //  Run model for the input image
    public void recognizeImage(final Bitmap bitmap) {

        // set Face to Preview
        binding.facePreview.setImageBitmap(bitmap);

        //Create ByteBuffer to store normalized image

        ByteBuffer imgData = ByteBuffer.allocateDirect(inputSize * inputSize * 3 * 4);

        imgData.order(ByteOrder.nativeOrder());

        intValues = new int[inputSize * inputSize];

        //get pixel values from Bitmap to normalize
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        imgData.rewind();

        for (int i = 0; i < inputSize; ++i) {
            for (int j = 0; j < inputSize; ++j) {
                int pixelValue = intValues[i * inputSize + j];
                if (isModelQuantized) {
                    // Quantized model
                    imgData.put((byte) ((pixelValue >> 16) & 0xFF));
                    imgData.put((byte) ((pixelValue >> 8) & 0xFF));
                    imgData.put((byte) (pixelValue & 0xFF));
                } else { // Float model
                    imgData.putFloat((((pixelValue >> 16) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                    imgData.putFloat((((pixelValue >> 8) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                    imgData.putFloat(((pixelValue & 0xFF) - IMAGE_MEAN) / IMAGE_STD);

                }
            }
        }
        //imgData is input to our model
        Object[] inputArray = {imgData};

        Map<Integer, Object> outputMap = new HashMap<>();


        embeddings = new float[1][OUTPUT_SIZE]; //output of model will be stored in this variable

        outputMap.put(0, embeddings);

        tfLite.runForMultipleInputsOutputs(inputArray, outputMap); //Run model

    }

    // Image Processing - resize Bitmap
    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }

    // Image Processing - convert Image to Bitmap
    private Bitmap toBitmap(Image image) {

        byte[] nv21 = YUV_420_888toNV21(image);

        YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, image.getWidth(), image.getHeight(), null);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, yuvImage.getWidth(), yuvImage.getHeight()), 75, out);

        byte[] imageBytes = out.toByteArray();

        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }

    //Save Faces to Shared Preferences, Conversion of Recognition objects to json string
    private void insertToSP(HashMap<String, SimilarityClassifier.Recognition> jsonMap) {

        jsonMap.putAll(readFromSP());
        String jsonString = new Gson().toJson(jsonMap);
        SharedPreferences sharedPreferences = getSharedPreferences(MyPREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("map", jsonString);
        editor.apply();
    }

    //Load Faces from Shared Preferences, Json String to Recognition object
    private HashMap<String, SimilarityClassifier.Recognition> readFromSP() {
        SharedPreferences sharedPreferences = getSharedPreferences(MyPREFERENCES, MODE_PRIVATE);
        String defValue = new Gson().toJson(new HashMap<String, SimilarityClassifier.Recognition>());
        String json = sharedPreferences.getString("map", defValue);
        TypeToken<HashMap<String, SimilarityClassifier.Recognition>> token = new TypeToken<HashMap<String, SimilarityClassifier.Recognition>>() {
        };
        HashMap<String, SimilarityClassifier.Recognition> retrievedMap = new Gson().fromJson(json, token.getType());

        //During type conversion and save/load procedure,format changes(eg float converted to double).
        //So embeddings need to be extracted from it in required format(eg.double to float).
        for (Map.Entry<String, SimilarityClassifier.Recognition> entry : retrievedMap.entrySet()) {
            float[][] output = new float[1][OUTPUT_SIZE];
            ArrayList arrayList = (ArrayList) entry.getValue().getExtra();
            if (arrayList != null) {
                arrayList = (ArrayList) arrayList.get(0);
                for (int counter = 0; counter < arrayList.size(); counter++) {
                    output[0][counter] = ((Double) arrayList.get(counter)).floatValue();
                }
            }
            entry.getValue().setExtra(output);


        }
        return retrievedMap;
    }

    //Load Photo from phone storage
    private void loadPhoto() {
        start = false;
        Intent pickIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        activityResultLauncher.launch(pickIntent);
    }

    // OnActivityResult after uploading image
    void activityResult(Uri selectedImageUri) {
        try {
            InputImage imPhoto = InputImage.fromBitmap(getBitmapFromUri(selectedImageUri), 0);
            detector.process(imPhoto).addOnSuccessListener(faces -> {

                if (faces.size() != 0) {
                    binding.addFace.setVisibility(View.VISIBLE);
                    binding.addInfo.setVisibility(View.VISIBLE);
                    binding.facePreview.setVisibility(View.VISIBLE);
                    Face face = faces.get(0);

                    Bitmap frame_bmp = null;
                    try {
                        frame_bmp = getBitmapFromUri(selectedImageUri);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Bitmap frame_bmp1 = rotateBitmap(frame_bmp, 0, flipX);


                    RectF boundingBox = new RectF(face.getBoundingBox());


                    Bitmap cropped_face = getCropBitmapByCPU(frame_bmp1, boundingBox);

                    Bitmap scaled = getResizedBitmap(cropped_face, 112, 112);

                    recognizeImage(scaled);
                    addFace();
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).addOnFailureListener(e -> {
                start = true;
                Toast.makeText(context, "Failed to add", Toast.LENGTH_SHORT).show();
            });
            binding.facePreview.setImageBitmap(getBitmapFromUri(selectedImageUri));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Get Bitmap from uri
    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }

    // Create new student document in firebase, store student's info in SharedPreference and send intent to attendance activity
    void createStudent(String rollNo, String name) {
        SimilarityClassifier.Recognition result = new SimilarityClassifier.Recognition(
                "0", "", -1f);
        result.setExtra(embeddings);

        registered.put(rollNo, result);

        String jsonString = new Gson().toJson(registered);
        userviewModel.createNewStudent(mAuth.getUid(), Objects.requireNonNull(mAuth.getCurrentUser()).getEmail(), rollNo, name, jsonString);
        insertToSP(registered);
        SharedPreferences sharedPreferences = getSharedPreferences(MyPREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("name", name);
        editor.apply();
        start = true;

        Intent intent = new Intent(MainActivity.this, AttendanceActivity.class);
        finish();
        startActivity(intent);
    }

}

