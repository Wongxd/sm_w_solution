package io.wongxd.solution.camera.core;

import android.app.Activity;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class CameraUtils {

    // 相机默认宽高，相机的宽度和高度跟屏幕坐标不一样，手机屏幕的宽度和高度是反过来的。
    public static final int DEFAULT_WIDTH = 1280;
    public static final int DEFAULT_HEIGHT = 720;
    public static final int DESIRED_PREVIEW_FPS = 30;

    private int mCameraID = Camera.CameraInfo.CAMERA_FACING_FRONT;
    private Camera mCamera;
    private int mCameraPreviewFps;
    private int mOrientation = 0;


    public void openBackCamera(int expectFps) {
        openCamera(Camera.CameraInfo.CAMERA_FACING_BACK, expectFps);
    }

    /**
     * 打开相机，默认打开前置相机
     *
     * @param expectFps
     */
    public void openFrontalCamera(int expectFps) {
        try {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            int numberOfCameras = Camera.getNumberOfCameras();
            for (int i = 0; i < numberOfCameras; i++) {
                Camera.getCameraInfo(i, cameraInfo);
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    mCameraID = Camera.CameraInfo.CAMERA_FACING_FRONT;
                    mCamera = Camera.open(i);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 如果没有前置摄像头，则打开默认的后置摄像头
        if (mCamera == null) {
            mCamera = Camera.open();
            mCameraID = Camera.CameraInfo.CAMERA_FACING_BACK;
            openCamera(mCameraID, expectFps);
        }
    }

    /**
     * 根据ID打开相机
     *
     * @param cameraID
     * @param expectFps
     */
    public void openCamera(int cameraID, int expectFps) {
        if (mCamera != null) {
            throw new RuntimeException("camera already initialized!");
        }
        mCamera = Camera.open(cameraID);
        if (mCamera == null) {
            throw new RuntimeException("Unable to open camera");
        }
        mCameraID = cameraID;
        Camera.Parameters parameters = mCamera.getParameters();
        maxZoomValue = parameters.getMaxZoom();
        currentZoomValue = parameters.getZoom();

        Log.i("变焦,支持", parameters.isZoomSupported() + "");
        Log.i("变焦,最大", parameters.getMaxZoom() + "");
        Log.i("变焦,最小", parameters.getZoom() + "");

        mCameraPreviewFps = chooseFixedPreviewFps(parameters, expectFps * 1000);
        // 设置聚焦模式
        try {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            parameters.setRecordingHint(true);
            mCamera.setParameters(parameters);
        } catch (Exception e) {
            e.printStackTrace();
            mCamera.setParameters(parameters);
        }
        setPreviewSize(mCamera, CameraUtils.DEFAULT_WIDTH, CameraUtils.DEFAULT_HEIGHT);
        setPictureSize(mCamera, CameraUtils.DEFAULT_WIDTH, CameraUtils.DEFAULT_HEIGHT);
        mCamera.setDisplayOrientation(mOrientation);
    }

    /**
     * 开始预览
     *
     * @param holder
     */
    public void startPreviewDisplay(SurfaceHolder holder) {
        if (mCamera == null) {
            throw new IllegalStateException("Camera must be set when start preview");
        }
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 切换相机
     *
     * @param cameraID
     */
    public void switchCamera(int cameraID, SurfaceHolder holder) {
        if (mCameraID == cameraID) {
            return;
        }
        mCameraID = cameraID;
        // 释放原来的相机
        releaseCamera();
        // 打开相机
        openCamera(cameraID, CameraUtils.DESIRED_PREVIEW_FPS);
        // 打开预览
        startPreviewDisplay(holder);
    }

    /**
     * 释放相机
     */
    public void releaseCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    public boolean isReleaseYet() {
        return mCamera == null;
    }

    /**
     * 开始预览
     */
    public void startPreview() {
        if (mCamera != null) {
            currentZoomValue = mCamera.getParameters().getZoom();
            mCamera.startPreview();
        }
    }

    /**
     * 停止预览
     */
    public void stopPreview() {
        if (mCamera != null) {
            currentZoomValue = mCamera.getParameters().getZoom();
            mCamera.stopPreview();
        }
    }

    /**
     * 拍照
     */
    public void takePicture(Camera.ShutterCallback shutterCallback,
                            Camera.PictureCallback rawCallback,
                            Camera.PictureCallback pictureCallback) {
        if (mCamera != null) {
            mCamera.takePicture(shutterCallback, rawCallback, pictureCallback);
        }
    }

    /**
     * 设置预览大小
     *
     * @param camera
     * @param expectWidth
     * @param expectHeight
     */
    public void setPreviewSize(Camera camera, int expectWidth, int expectHeight) {
        Camera.Parameters parameters = camera.getParameters();
        Camera.Size size = calculatePerfectSize(parameters.getSupportedPreviewSizes(), expectWidth, expectHeight);
        parameters.setPreviewSize(size.width, size.height);
        camera.setParameters(parameters);
    }

    /**
     * 获取预览大小
     *
     * @return
     */
    public Camera.Size getPreviewSize() {
        if (mCamera != null) {
            return mCamera.getParameters().getPreviewSize();
        }
        return null;
    }

    /**
     * 设置拍摄的照片大小
     *
     * @param camera
     * @param expectWidth
     * @param expectHeight
     */
    public void setPictureSize(Camera camera, int expectWidth, int expectHeight) {
        Camera.Parameters parameters = camera.getParameters();
        Camera.Size size = calculatePerfectSize(parameters.getSupportedPictureSizes(),
                expectWidth, expectHeight);
        parameters.setPictureSize(size.width, size.height);
        camera.setParameters(parameters);
    }

    /**
     * 获取照片大小
     *
     * @return
     */
    public Camera.Size getPictureSize() {
        if (mCamera != null) {
            return mCamera.getParameters().getPictureSize();
        }
        return null;
    }

    /**
     * 计算最完美的Size
     *
     * @param sizes
     * @param expectWidth
     * @param expectHeight
     * @return
     */
    public Camera.Size calculatePerfectSize(List<Camera.Size> sizes, int expectWidth,
                                            int expectHeight) {
        sortList(sizes); // 根据宽度进行排序
        Camera.Size result = sizes.get(0);
        boolean widthOrHeight = false; // 判断存在宽或高相等的Size
        // 辗转计算宽高最接近的值
        for (Camera.Size size : sizes) {
            // 如果宽高相等，则直接返回
            if (size.width == expectWidth && size.height == expectHeight) {
                result = size;
                break;
            }
            // 仅仅是宽度相等，计算高度最接近的size
            if (size.width == expectWidth) {
                widthOrHeight = true;
                if (Math.abs(result.height - expectHeight)
                        > Math.abs(size.height - expectHeight)) {
                    result = size;
                }
            }
            // 高度相等，则计算宽度最接近的Size
            else if (size.height == expectHeight) {
                widthOrHeight = true;
                if (Math.abs(result.width - expectWidth)
                        > Math.abs(size.width - expectWidth)) {
                    result = size;
                }
            }
            // 如果之前的查找不存在宽或高相等的情况，则计算宽度和高度都最接近的期望值的Size
            else if (!widthOrHeight) {
                if (Math.abs(result.width - expectWidth)
                        > Math.abs(size.width - expectWidth)
                        && Math.abs(result.height - expectHeight)
                        > Math.abs(size.height - expectHeight)) {
                    result = size;
                }
            }
        }
        return result;
    }

    /**
     * 排序
     *
     * @param list
     */
    private void sortList(List<Camera.Size> list) {
        Collections.sort(list, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size pre, Camera.Size after) {
                if (pre.width > after.width) {
                    return 1;
                } else if (pre.width < after.width) {
                    return -1;
                }
                return 0;
            }
        });
    }

    /**
     * 选择合适的FPS
     *
     * @param parameters
     * @param expectedThoudandFps 期望的FPS
     * @return
     */
    public int chooseFixedPreviewFps(Camera.Parameters parameters, int expectedThoudandFps) {
        List<int[]> supportedFps = parameters.getSupportedPreviewFpsRange();
        for (int[] entry : supportedFps) {
            if (entry[0] == entry[1] && entry[0] == expectedThoudandFps) {
                parameters.setPreviewFpsRange(entry[0], entry[1]);
                return entry[0];
            }
        }
        int[] temp = new int[2];
        int guess;
        parameters.getPreviewFpsRange(temp);
        if (temp[0] == temp[1]) {
            guess = temp[0];
        } else {
            guess = temp[1] / 2;
        }
        return guess;
    }

    /**
     * 设置预览角度，setDisplayOrientation本身只能改变预览的角度
     * previewFrameCallback以及拍摄出来的照片是不会发生改变的，拍摄出来的照片角度依旧不正常的
     * 拍摄的照片需要自行处理
     * 这里Nexus5X的相机简直没法吐槽，后置摄像头倒置了，切换摄像头之后就出现问题了。
     *
     * @param activity
     */
    public int calculateCameraPreviewOrientation(Activity activity) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(mCameraID, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;
        } else {
            result = (info.orientation - degrees + 360) % 360;
        }
        mOrientation = result;
        return result;
    }

    public void setPreviewOrientation(int orientation){
        mOrientation = orientation;
    }

    /**
     * 获取当前的Camera ID
     *
     * @return
     */
    public int getCameraID() {
        return mCameraID;
    }

    /**
     * 获取当前预览的角度
     *
     * @return
     */
    public int getPreviewOrientation() {
        return mOrientation;
    }

    /**
     * 获取FPS（千秒值）
     *
     * @return
     */
    public int getCameraPreviewThousandFps() {
        return mCameraPreviewFps;
    }

    public void autoFocus() {
        if (mCamera != null) {
//            mCamera.cancelAutoFocus();
//            mCamera.getParameters().setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
//            mCamera.autoFocus(new Camera.AutoFocusCallback() {
//                @Override
//                public void onAutoFocus(boolean success, Camera camera) {
//
//                }
//            });

            Camera.Parameters parameters = mCamera.getParameters();
            try {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                mCamera.setParameters(parameters);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private boolean isSupportZoom = false;

    public boolean isSupportZoom() {
        if (mCamera.getParameters().isZoomSupported()) {
            isSupportZoom = true;
        }
        return isSupportZoom;
    }

    public int maxZoomValue = 0;
    public int currentZoomValue = 0;

    public void setZoomOut() {
        if (mCamera == null) return;

        if (isSupportZoom()) {
            try {
                Camera.Parameters params = mCamera.getParameters();
                final int MAX = params.getMaxZoom();
                currentZoomValue = params.getZoom();
                Log.i("ZOOM", "-----------------MAX:" + MAX + " params : " + currentZoomValue);
                if (currentZoomValue < MAX) {
                    currentZoomValue += 1;
                    params.setZoom(currentZoomValue);
                    mCamera.setParameters(params);
                }
                Log.i("ZOOM", "Is support Zoom " + params.isZoomSupported());
            } catch (Exception e) {
                Log.i("ZOOM", "--------exception zoom");
                e.printStackTrace();
            }
        } else {
            Log.i("ZOOM", "--------the phone not support zoom");
        }
    }

    public void setZoomIn() {
        if (mCamera == null) return;
        if (isSupportZoom()) {
            try {
                Camera.Parameters params = mCamera.getParameters();
                final int MAX = params.getMaxZoom();
                currentZoomValue = params.getZoom();
                Log.i("ZOOM", "-----------------MAX:" + MAX + " params : " + currentZoomValue);
                if (currentZoomValue >= 1) {
                    currentZoomValue -= 1;
                    params.setZoom(currentZoomValue);
                    mCamera.setParameters(params);
                }
                Log.i("ZOOM", "Is support Zoom " + params.isZoomSupported());
            } catch (Exception e) {
                Log.i("ZOOM", "--------exception zoom");
                e.printStackTrace();
            }
        } else {
            Log.i("ZOOM", "--------the phone not support zoom");
        }
    }

    public void setZoom(int zoom) {
        if (mCamera == null) return;
        if (isSupportZoom()) {
            try {
                Camera.Parameters params = mCamera.getParameters();
                final int MAX = params.getMaxZoom();
                currentZoomValue = params.getZoom();
                Log.i("ZOOM", "-----------------MAX:" + MAX + " params : " + currentZoomValue);
                if (zoom >= 0 && zoom < MAX) {
                    currentZoomValue = zoom;
                    params.setZoom(currentZoomValue);
                    mCamera.setParameters(params);
                }
                Log.i("ZOOM", "Is support Zoom " + params.isZoomSupported());
            } catch (Exception e) {
                Log.i("ZOOM", "--------exception zoom");
                e.printStackTrace();
            }
        } else {
            Log.i("ZOOM", "--------the phone not support zoom");
        }
    }

    public void resetZoom() {
        if (mCamera == null) return;
        Camera.Parameters params = mCamera.getParameters();
        final int MAX = params.getMaxZoom();
        maxZoomValue = MAX;
        currentZoomValue = 0;
    }


}