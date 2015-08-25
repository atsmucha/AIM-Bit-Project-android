package project.jdlp.com.jdlp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import project.jdlp.com.jdlp.controller.CameraPreview;

/**
 * Created by atsmucha on 15. 7. 14.
 */
public class CameraActivity extends Activity{
    private CameraPreview preview;
    private Camera camera;
    private SurfaceView surfaceView;
    private SurfaceHolder holder;
    private Activity activity;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        activity = this;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_camera);
        surfaceView = (SurfaceView)findViewById(R.id.activity_cam_surface);
//        holder = surfaceView.getHolder();
        preview = new CameraPreview(this, surfaceView);
        ((FrameLayout)findViewById(R.id.activity_cam_layout)).addView(preview);
        preview.setKeepScreenOn(true);

        setCameraActivityListener();
    }

    public void setCameraActivityListener() {
        preview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    camera.takePicture(shutterCallback, rawCallback, jpegCallback);
                } finally {
//                    finish();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        int numCams = Camera.getNumberOfCameras();
        if(numCams > 0) {
            try {
                camera = Camera.open(0);
                camera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        if(success) {
                            camera.startPreview();
                        }

                    }
                });

                preview.setCamera(camera);
            } catch (RuntimeException ex) {
                Toast.makeText(getApplicationContext(), "카메라를 찾을수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }   //onResume()

    @Override
    protected void onPause() {
        if(camera != null) {
            camera.stopPreview();
            preview.setCamera(null);
            camera.release();
            camera = null;
        }
        super.onPause();
    }

    private class SaveImageTask extends AsyncTask<byte[], Void, Void> {
        @Override
        protected Void doInBackground(byte[]... data) {
            FileOutputStream outStream = null;

            try {
                File sdCard = Environment.getExternalStorageDirectory();
                File dir = new File(sdCard.getAbsolutePath() + "/Jdlp");
                dir.mkdirs();

                String fileName = String.format("%d.jpg", System.currentTimeMillis());
                File outFile = new File(dir, fileName);

                outStream = new FileOutputStream(outFile);
                outStream.write(data[0]);
                outStream.flush();
                outStream.close();

                Log.e("cameraActivity", outFile.getAbsolutePath()+"");

                refreshGallery(outFile);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }   //SaveImageTask

    private void resetCam() {
        camera.startPreview();
        preview.setCamera(camera);
    }   //resetCam();

    private void refreshGallery(File file) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(Uri.fromFile(file));
        sendBroadcast(mediaScanIntent);
    }   //refreshGallery()

    /*
    *    camera callback
    */

    Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
        @Override
        public void onShutter() {
        }
    };
    Camera.PictureCallback rawCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
        }
    };

    Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            new SaveImageTask().execute(data);
//            resetCam();
            Intent intent = getIntent();
            intent.putExtra("data", data);
            setResult(1, intent);
            finish();
        }
    };

}
