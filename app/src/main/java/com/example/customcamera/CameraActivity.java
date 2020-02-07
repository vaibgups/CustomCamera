package com.example.customcamera;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CameraActivity extends AppCompatActivity implements View.OnClickListener {


    private final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private int REQUEST_CODE_PERMISSIONS = 312;

    private Button btmCaptureImage, btnReset, btmSubmitImage;
    private FrameLayout frameLayout;
    private Camera camera;
    private ShowCamera showCamera;
    private String fileSaveInPath = Environment.getExternalStorageDirectory().toString() + "/PMJAY/CustomCamera";
    private String actualPath;
    private File picture_file;
    private RelativeLayout rlCamera;
    private ImageView ivCaptureImage;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        init();
    }

    private void init() {
        frameLayout = findViewById(R.id.frameLayout);
        btmCaptureImage = findViewById(R.id.btmCaptureImage);
        btmCaptureImage.setOnClickListener(this);
        btnReset = findViewById(R.id.btnReset);
        btnReset.setOnClickListener(this);
        btmSubmitImage = findViewById(R.id.btmSubmitImage);
        btmSubmitImage.setOnClickListener(this);


        rlCamera = findViewById(R.id.rlCamera);
        ivCaptureImage = findViewById(R.id.ivCaptureImage);

        btmSubmitImage = findViewById(R.id.btmSubmitImage);
        btmSubmitImage.setOnClickListener(this);

    }


    @Override
    protected void onResume() {
        super.onResume();
       openCamera();
    }


    void openCamera(){

        if (allPermissionsGranted()) {
            camera = Camera.open();
            showCamera = new ShowCamera(this,camera);
            frameLayout.addView(showCamera);


        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                camera = Camera.open();
                showCamera = new ShowCamera(this,camera);
                frameLayout.addView(showCamera);
            }
        }
    }


    public boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btmCaptureImage:{
                captureImage();
                break;
            }
            case R.id.btnReset:{
                rlCamera.setVisibility(View.VISIBLE);
                btmCaptureImage.setVisibility(View.VISIBLE);
                btmSubmitImage.setVisibility(View.GONE);
                ivCaptureImage.setVisibility(View.GONE);
                openCamera();
                break;
            }
            case R.id.btmSubmitImage:{
                break;
            }
        }
    }

    Camera.PictureCallback mPictureCallBack = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] bytes, Camera camera) {
            picture_file = getOutputMediaFile();

            if (picture_file == null) {
                return;
            } else {
                try {
                    FileOutputStream fos = new FileOutputStream(picture_file);
//                    fos.write(bytes);
//                    fos.close();
//                    isPreviewImage = true;
                    if (picture_file.exists()) {
                        camera.stopPreview();
                        camera.release();
                        rlCamera.setVisibility(View.GONE);
                        btmCaptureImage.setVisibility(View.GONE);
                        btmSubmitImage.setVisibility(View.VISIBLE);
                        ivCaptureImage.setVisibility(View.VISIBLE);
                        fos.write(bytes);
                        fos.close();
//                        Bitmap cameraBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//
//                        ivCaptureImage.setImageBitmap(cameraBitmap);

                        new CompressImage(CameraActivity.this).compressImage(picture_file.getAbsolutePath(),bytes, ivCaptureImage);

                      /*  fos.write(bytes);
                        fos.close();


                        //start 4/2/2020
                        Bitmap cameraBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                        //Bitmap cameraBitmap = BitmapFactory.decodeFile(picture_file.getAbsolutePath());//get file path from intent when you take iamge.

                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        cameraBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);


                        ExifInterface exif = new ExifInterface(picture_file.getAbsolutePath());
                        float rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                        System.out.println(rotation);

                        float rotationInDegrees = exifToDegrees(rotation);
                        System.out.println(rotationInDegrees);

                        Matrix matrix = new Matrix();
                        matrix.postRotate(rotationInDegrees);

                        Bitmap scaledBitmap = Bitmap.createBitmap(cameraBitmap);
                        Bitmap rotatedBitmap = Bitmap.createBitmap(cameraBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);

                        FileOutputStream fos1=new FileOutputStream(picture_file.getAbsolutePath());
                        fos1.flush();
                        fos1.close();

                        //start

                        Bitmap scaledBitmap1 = Bitmap.createScaledBitmap(rotatedBitmap, rotatedBitmap.getWidth(), rotatedBitmap.getHeight(), true);
                        Bitmap bitmapCopy = scaledBitmap1.copy(Bitmap.Config.ARGB_8888, true);


//                        String address1 = address.substring(0, (address.length() / 2));
//                        String address2 = address.substring((address.length() / 2));


                        Canvas canvas = new Canvas(bitmapCopy);
                        Paint paint = new Paint();
                        paint.setColor(Color.WHITE);

                        paint.setTextSize(12);


                        int xPos = (canvas.getWidth() / 2);
                        int yPos = canvas.getHeight() / 2;

                        canvas.drawText(TimeStamp.getImagePreview(), 15, yPos + 110, paint);
                        canvas.drawText(address1, 15, yPos + 130, paint);
                        canvas.drawText(address2, 15, yPos + 150, paint);

                        ivCaptureImage.setImageBitmap(bitmapCopy);

                        // 4/2/2020 end


                        // Bitmap bitmap = ((BitmapDrawable) ivCaptureImage.getDrawable()).getBitmap();

                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmapCopy.compress(Bitmap.CompressFormat.JPEG, 30, baos);
                        byte[] imageInByte = baos.toByteArray();


                        fos = new FileOutputStream(getOutputMediaFile());
                        fos.write(imageInByte);
                        fos.close();*/

                    }
//                    camera.startPreview();
                } catch (IOException e) {

                } catch (Exception e) {

                }
            }

        }
    };

    private void captureImage() {

        if (camera != null){
            camera.takePicture(null,null,mPictureCallBack);
        }
    }



   /* private File getOutputMediaFile() {

        *//*String timeStamp = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss a",
                Locale.getDefault()).format(new Date());*//*

        String timeStamp = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss",
                Locale.getDefault()).format(new Date());

        File mediaStorageDir = new File(fileSaveInPath);

        if (!mediaStorageDir.exists()) {
            mediaStorageDir.mkdirs();
        }
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator
                + "CustomImage" + "_"
                + System.currentTimeMillis() + ".jpg");
        actualPath = mediaFile.getAbsolutePath();
        return mediaFile;
    }*/

    private File getOutputMediaFile() {
        String timeStamp = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss",
                Locale.getDefault()).format(new Date());
        File mediaStorageDir = new File(fileSaveInPath);
        if (!mediaStorageDir.exists()) {
            mediaStorageDir.mkdirs();
        }
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator
                + timeStamp + "_"
                + "Vai" + ".jpg");
        return mediaFile;
    }
}