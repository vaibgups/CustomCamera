package com.example.customcamera;

import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ShowCamera extends SurfaceView implements SurfaceHolder.Callback {

    private Context context;
    private Camera camera;
    private SurfaceHolder holder;

    public ShowCamera(Context context, Camera camera) {
        super(context);
        this.context = context;
        this.camera = camera;
        holder = getHolder();
        holder.addCallback(this);

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Camera.Parameters params = camera.getParameters();
        List<Camera.Size> sizes = params.getSupportedPictureSizes();
        Collections.sort(sizes, new Comparator<Camera.Size>() {

            public int compare(final Camera.Size a, final Camera.Size b) {
                return a.width * a.height - b.width * b.height;
            }
        });




        int tempWidth = 600;
        Camera.Size mSize = sizes.get(0);
//        Camera.Size  nmSize = sizes.get(sizes.size()-1);

        if (params.getSupportedFocusModes().contains(
                Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        }

        for (int i = 0; i < sizes.size(); i++) {
            Log.i("Camera Size ", "Width " + sizes.get(i).width + " Height " + sizes.get(i).height);
           /* if (sizes.get(i).height <= tempWidth) {
                mSize = sizes.get(i);
                break;
            } else {
                mSize = sizes.get(0);
            }*/

        }

        if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE){
            params.set("orientation","portrait");
            camera.setDisplayOrientation(90);
            params.setRotation(90);
        }else {

            params.set("orientation","landscape");
            camera.setDisplayOrientation(0);
            params.setRotation(0);
        }

        params.setPictureSize(mSize.width, mSize.height);
//        params.setPictureSize(600,800);
        camera.setParameters(params);
        try{

            camera.setPreviewDisplay(holder);
            camera.startPreview();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

        camera.stopPreview();
        camera.release();

    }
}