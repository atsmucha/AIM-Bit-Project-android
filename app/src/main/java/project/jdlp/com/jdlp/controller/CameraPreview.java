package project.jdlp.com.jdlp.controller;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import java.io.IOException;
import java.util.List;

/**
 * Created by atsmucha on 15. 7. 14.
 */
public class CameraPreview extends ViewGroup implements SurfaceHolder.Callback {
    SurfaceView surface;
    SurfaceHolder holder;
    Camera.Size previewSize;
    List<Camera.Size> supportedPreviewSize;
    Camera camera;

    public CameraPreview(Context context, SurfaceView sv) {
        super(context);
        surface = sv;
        holder = surface.getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
        if(this.camera != null) {
            supportedPreviewSize = this.camera.getParameters().getSupportedPreviewSizes();
            requestLayout();
            Camera.Parameters params = this.camera.getParameters();
            List<String> focusModes = params.getSupportedFocusModes();
            if(focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                this.camera.setParameters(params);
            }
        }
    }   //setCamera

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(width, height);

        if(supportedPreviewSize != null) {
            previewSize = getOptimalPreviewSize(supportedPreviewSize, width, height);
        }
    }   //onMeasure

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if(changed && getChildCount() > 0) {
            final View child = getChildAt(0);

            final int width = r - l;
            final int height = b - t;

            int previewWidth = width;
            int previewHeight = height;
            if(previewSize != null) {
                previewWidth = previewSize.width;
                previewHeight = previewSize.height;
            }

            if(width * previewHeight > height * previewWidth) {
                final int scaledChildWidth = previewWidth * height / previewHeight;
                child.layout((width - scaledChildWidth) / 2, 0, (width + scaledChildWidth) / 2, height);
            } else {
                final int scaledChildHeight = previewHeight * width / previewWidth;
                child.layout(0, (height - scaledChildHeight) / 2, width, (height + scaledChildHeight) / 2);
            }
        }
    }   //onLayout

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double)w / h;
        if(sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        for(Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if(Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if(Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }   //getOptimalPreviewSize

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if(this.camera != null) {
            try {
                this.camera.setPreviewDisplay(holder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }   //surfaceCreated()

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if(this.camera != null) {
            Camera.Parameters parameters = this.camera.getParameters();
            parameters.setPreviewSize(previewSize.width, previewSize.height);
            requestLayout();

            this.camera.setParameters(parameters);
            this.camera.startPreview();
        }
    }   //surfaceChanged()

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if(this.camera != null) {
            this.camera.stopPreview();
        }
    }   //surfaceDestroyed()

}
