package io.wongxd.solution.camera.core;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = CameraSurfaceView.class.getSimpleName();

    private SurfaceHolder mSurfaceHolder;

    private CameraUtils mCameraUtils;

    private boolean openBack = true;

    public CameraSurfaceView(Context context, CameraUtils cameraUtils,boolean openBack) {
        super(context);
        mCameraUtils = cameraUtils;
        this.openBack = openBack;
        init();
    }

    private void init() {
        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (openBack)
            mCameraUtils.openBackCamera(CameraUtils.DESIRED_PREVIEW_FPS);
        else
            mCameraUtils.openFrontalCamera(CameraUtils.DESIRED_PREVIEW_FPS);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mCameraUtils.startPreviewDisplay(holder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mCameraUtils.releaseCamera();
    }
}