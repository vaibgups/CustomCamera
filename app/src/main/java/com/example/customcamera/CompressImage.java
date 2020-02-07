package com.example.customcamera;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CompressImage {

    private Context context;

    public CompressImage(Context context) {
        this.context = context;
    }

    public String compressImage(String actualFilePath, byte[] bytes, ImageView imageView) {

//        String filePath = getRealPathFromURI(actualFilePath);
        String filePath = actualFilePath;
        Bitmap scaledBitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();

//      by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
//      you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true;

        Bitmap bmp = BitmapFactory.decodeByteArray(bytes,0, bytes.length, options);
//        Bitmap bmp2 = BitmapFactory.decodeFile(filePath, options);


        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;

//      max Height and width values of the compressed image is taken as 816x612
//        float maxHeight = 816.0f;
        float maxHeight = 1200.0f;
//        float maxWidth = 612.0f;
        float maxWidth = 1600.0f;
        float imgRatio = actualWidth / actualHeight;
        float maxRatio = maxWidth / maxHeight;

//      width and height values are set maintaining the aspect ratio of the image
        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) maxHeight;
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;
            }
        }

//      setting inSampleSize value allows to load a scaled down version of the original image
        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);

//      inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false;

//      this options allow android to claim the bitmap memory if it runs low on memory
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[16 * 1024];

        try {
//          load the bitmap from its path
//            bmp = BitmapFactory.decodeFile(filePath, options);
            bmp =  BitmapFactory.decodeByteArray(bytes, 0, bytes.length,options);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();

        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));


//      check the rotation of the image and display it properly
        ExifInterface exif;
        try {
//            InputStream targetStream = new ByteArrayInputStream(bytes);
//            exif = new ExifInterface(targetStream);

            exif = new ExifInterface(filePath);

            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 0);
            Log.d("EXIF", "Exif: " + orientation);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 3) {
                matrix.postRotate(180);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 8) {
                matrix.postRotate(270);
                Log.d("EXIF", "Exif: " + orientation);
            }
//            canvas.setMatrix(matrix);
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                    scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
                    true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileOutputStream out = null;
        String filename = getFilename(actualFilePath);
        try {
            File file = new File(actualFilePath);
            out = new FileOutputStream(file);

//          write the compressed bitmap at the destination specified by filename.

            Bitmap bitmapCopy = scaledBitmap.copy(Bitmap.Config.ARGB_8888, true);
            Canvas canvas1 = new Canvas(bitmapCopy);
            Paint paint = new Paint();
            paint.setColor(Color.WHITE);
            paint.setTextSize(30);
            String timeStamp = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss",
                    Locale.getDefault()).format(new Date());
            canvas1.drawText(timeStamp+"First", middleX/2,  middleY+middleY/2, paint);
            canvas1.drawText(timeStamp+"Second", middleX/2,  middleY+50+middleY/2, paint);
            canvas1.drawText(timeStamp+"Third", middleX/2,  middleY+100+middleY/2, paint);
            bitmapCopy.compress(Bitmap.CompressFormat.JPEG, 65, out);

            imageView.setImageBitmap(bitmapCopy);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return filename;
    }

    public String getFilename(String actualFilePath) {
        File tempFileName = new File(actualFilePath);
//        String path = tempFileName.getParent();
//        String fileName = tempFileName.getName();
        File file = new File(tempFileName.getParent());
        if (!file.exists()) {
            file.mkdirs();
        }

        String uriSting = (tempFileName.getParent() + "/Com_" + tempFileName.getName());
//        String uriSting = (path + fileName);
//        tempFileName.delete();
        return uriSting;
    }

    private String getRealPathFromURI(String contentURI) {
        Uri contentUri = Uri.parse(contentURI);
        Cursor cursor = context.getContentResolver().query(contentUri, null, null, null, null);
        if (cursor == null) {
            return contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(index);
        }
    }

    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }

        return inSampleSize;
    }
}

