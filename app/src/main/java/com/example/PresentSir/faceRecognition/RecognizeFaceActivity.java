package com.raina.PresentSir.faceRecognition;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
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
import com.raina.PresentSir.databinding.ActivityRecognizeFaceBinding;
import com.raina.PresentSir.studentAttendance.AttendanceActivity;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class RecognizeFaceActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST = 1888;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    private static final String MyPREFERENCES = "MyPrefs";
    private final String TAG = "Present Sir";
    FaceDetector detector;
    int[] intValues;
    int inputSize = 112;  //Input size for model
    boolean isModelQuantized = false;
    float IMAGE_MEAN = 128.0f;
    float IMAGE_STD = 128.0f;
    int OUTPUT_SIZE = 192; //Output size of model
    float[][] embeddings;
    Interpreter tfLite;
    float distance = 1.0f;
    String subject;
    String modelFile = "mobile_face_net.tflite"; //model name
    private ActivityRecognizeFaceBinding binding;
    private HashMap<String, SimilarityClassifier.Recognition> registered = new HashMap<>(); //saved Faces
    private FirebaseAuth mAuth;

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

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRecognizeFaceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        registered = readFromSP();
        SharedPreferences sharedPref = getSharedPreferences("Distance", Context.MODE_PRIVATE);
        distance = sharedPref.getFloat("distance", 1.00f);
        subject = getIntent().getStringExtra("Subject");
        mAuth = FirebaseAuth.getInstance();

        setSupportActionBar(binding.toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Mark Attendance");


        // Camera Permission
        binding.capture.setOnClickListener(v -> {
            binding.text.setVisibility(View.INVISIBLE);
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
            } else {
                binding.image.setImageDrawable(null);
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        });

        binding.check.setOnClickListener(view -> {
            binding.text.setVisibility(View.VISIBLE);
            checkRollNumber(binding.text.getText().toString(), Objects.requireNonNull(mAuth.getCurrentUser()).getEmail());
        });

        //Load model
        try {
            tfLite = new Interpreter(loadModelFile(RecognizeFaceActivity.this, modelFile));
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Initialize Face Detector
        FaceDetectorOptions highAccuracyOpts =
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                        .build();
        detector = FaceDetection.getClient(highAccuracyOpts);
    }

    // Camera Permission OnResult
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_SHORT).show();
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            } else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_SHORT).show();
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

    //Load Faces from Shared Preferences.Json String to Recognition object
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

    // OnActivityResult after capturing image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_CANCELED) {
            if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                int rotationInDegrees = 0;

                InputImage imPhoto = InputImage.fromBitmap(photo, 0);
                detector.process(imPhoto).addOnSuccessListener(faces -> {

                    if (faces.size() != 0) {
                        Face face = faces.get(0);

                        //Code to recreate bitmap from source and show bitmap to canvas
                        Bitmap frame_bmp1 = rotateBitmap(photo, rotationInDegrees, false);
                        RectF boundingBox = new RectF(face.getBoundingBox());
                        Bitmap cropped_face = getCropBitmapByCPU(frame_bmp1, boundingBox);
                        cropped_face = rotateBitmap(cropped_face, 0, true);
                        Bitmap scaled = getResizedBitmap(cropped_face, 112, 112);

                        recognizeImage(scaled);
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        String text;
                        if (registered.isEmpty())
                            text = "Add Face";
                        else
                            text = "No Face Detected!";

                        binding.text.setText(text);
                    }
                }).addOnFailureListener(e -> Toast.makeText(RecognizeFaceActivity.this, "Failed to add", Toast.LENGTH_SHORT).show());
                binding.image.setImageBitmap(photo);
                binding.check.setVisibility(View.VISIBLE);
                binding.infoText.setVisibility(View.VISIBLE);
            }
        } else
            binding.image.setImageResource(R.drawable.man);
    }

    // Sends input to model and recognizes image
    public void recognizeImage(final Bitmap bitmap) {

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

        float distance_local;

        //Compare new face with saved Faces.
        if (registered.size() > 0) {

            final List<Pair<String, Float>> nearest = findNearest(embeddings[0]);//Find 2 closest matching face

            if (nearest.get(0) != null) {

                final String name = nearest.get(0).first; //get name and distance of closest matching face
                distance_local = nearest.get(0).second;

                String text;
                if (distance_local < distance) //If distance between Closest found face is more than 1.000 ,then output UNKNOWN face.
                    text = name;
                else
                    text = "Unknown";
                binding.text.setText(text);
            }
        } else {
            String text = "Add Face";
            binding.text.setText(text);
        }
    }

    //Compare Faces by distance between face embeddings
    private List<Pair<String, Float>> findNearest(float[] emb) {
        List<Pair<String, Float>> neighbour_list = new ArrayList<>();
        Pair<String, Float> ret = null; //to get closest match
        Pair<String, Float> prev_ret = null; //to get second closest match
        for (Map.Entry<String, SimilarityClassifier.Recognition> entry : registered.entrySet()) {

            final String name = entry.getKey();
            final float[] knownEmb = ((float[][]) entry.getValue().getExtra())[0];

            float distance = 0;
            for (int i = 0; i < emb.length; i++) {
                float diff = emb[i] - knownEmb[i];
                distance += diff * diff;
            }
            distance = (float) Math.sqrt(distance);
            if (ret == null || distance < ret.second) {
                prev_ret = ret;
                ret = new Pair<>(name, distance);
            }
        }
        if (prev_ret == null) prev_ret = ret;
        neighbour_list.add(ret);
        neighbour_list.add(prev_ret);

        return neighbour_list;

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

    // Function to check if student's face matches his roll number
    void checkRollNumber(String rollNum, String email) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Firebase call to check student using his roll number
        db.collection("Student").whereEqualTo("rollNumber", rollNum).whereEqualTo("email", email).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().isEmpty()) {
                    String text = "Face did not Match, Try again !";
                    binding.text.setText(text);
                }
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String text = null;
                    if (document.exists()) {
                        Log.d("Document: ", document.getId() + " " + document.getData());
                        text = "Face Matched !";
                        // Mark student's attendance
                        markAttendance(subject, document.getId());
                        Snackbar.make(binding.getRoot(), "Attendance marked Successfully", Snackbar.LENGTH_SHORT).show();
                        new Handler().postDelayed(() -> {
                            Intent intent = new Intent(RecognizeFaceActivity.this, AttendanceActivity.class);
                            finish();
                            startActivity(intent);
                        }, 1000);

                    } else if (binding.text.toString().isEmpty())
                        text = "Face did not Match, Try again !";
                    binding.text.setText(text);
                }
            } else
                Toast.makeText(RecognizeFaceActivity.this, binding.text.getText().toString(), Toast.LENGTH_LONG).show();
        });

    }

    // Mark student's attendance in firebase
    void markAttendance(String subject, String id) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        SimpleDateFormat curFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String date = curFormatter.format(new Date());

        // Get/Create document with today's date and mark attendance for given subject
        db.collection("Student").document(id).collection("Attendance").document(date).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().exists()) {
                    DocumentReference documentReference = db.collection("Student").document(id).collection("Attendance").document(date);
                    documentReference.update(subject, FieldValue.increment(1));
                } else {
                    Attendance attendance;

                    if (Objects.equals(subject, "subject1"))
                        attendance = new Attendance(1, 0, 0, 0, 0, 0);
                    else if (Objects.equals(subject, "subject2"))
                        attendance = new Attendance(0, 1, 0, 0, 0, 0);
                    else if (Objects.equals(subject, "subject3"))
                        attendance = new Attendance(0, 0, 1, 0, 0, 0);
                    else if (Objects.equals(subject, "subject4"))
                        attendance = new Attendance(0, 0, 0, 1, 0, 0);
                    else if (Objects.equals(subject, "subject5"))
                        attendance = new Attendance(0, 0, 0, 0, 1, 0);
                    else
                        attendance = new Attendance(0, 0, 0, 0, 0, 1);

                    db.collection("Student").document(id).collection("Attendance").document(date).set(attendance).addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful())
                            Log.d(TAG, "Document created successfully");
                    });
                }

            } else
                Toast.makeText(RecognizeFaceActivity.this, "Some error occurred", Toast.LENGTH_SHORT).show();
        });
    }

    // this event will enable the back
    // function to the button on press
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}