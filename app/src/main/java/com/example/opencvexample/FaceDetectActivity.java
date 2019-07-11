package com.example.opencvexample;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.example.opencvexample.utils.CascadeHelper;
import com.example.opencvexample.utils.ChangeFrame;

import org.opencv.android.*;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;


import java.io.File;
import java.io.InputStream;

public class FaceDetectActivity extends Activity
        implements CvCameraViewListener2 {

    private static final String TAG = "FaceDetectActivity";

    private CameraBridgeViewBase mOpenCvCameraView;
    private CascadeClassifier cascadeClassifier;
    private int absoluteFaceSize;
    private Mat rgbaAux;
    private Mat mRgba;
    private Mat mRgbaF;
    private Mat mRgbaT;
    private static final int ORIENTATION_0 = 0;
    private static final int ORIENTATION_90 = 1;
    private static final int ORIENTATION_180 = 2;
    private static final int ORIENTATION_270 = 3;
    private int frameCounter = 0;
    private Display display;


    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    getCascade();
                    mOpenCvCameraView.enableView();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }

    };

    private void getCascade() {
        InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
        cascadeClassifier = CascadeHelper.initializeOpenCVDependencies(is, cascadeDir,
                "lbpcascade_frontalface.xml");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        JavaCameraView javaCameraView = new JavaCameraView(this, 0);
        javaCameraView.setLayoutDirection(JavaCamera2View.LAYOUT_DIRECTION_RTL);
        mOpenCvCameraView = javaCameraView;
        setContentView(mOpenCvCameraView);
        mOpenCvCameraView.setCvCameraViewListener(this);
        display = ((WindowManager)
                getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        absoluteFaceSize = (int) (height * 0.15);
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mRgbaF = new Mat(height, width, CvType.CV_8UC4);
        mRgbaT = new Mat(width, width, CvType.CV_8UC4);
    }

    @Override
    public void onCameraViewStopped() {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRgba.release();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        ChangeFrame.changeRotation(display, mRgba, mRgbaF, mRgbaT);
        Rect[] facesArray = calculateRectangles();
        drawRectandles(facesArray);
        rgbaAux = mRgba;
        return mRgba;
    }

    private Rect[] calculateRectangles(){
        MatOfRect faces = new MatOfRect();
        if (cascadeClassifier != null) {
            cascadeClassifier.detectMultiScale(mRgba, faces, 1.3, 2, 2,
                    new Size(absoluteFaceSize, absoluteFaceSize), new Size());
        }
        return faces.toArray();
    }

    private void drawRectandles(Rect[] facesArray) {
        if (facesArray != null) {
            for (Rect rect : facesArray) {
                Rect2d rd = new Rect2d();
                rd.height = rect.height;
                rd.width = rect.width;
                rd.x = rect.x;
                rd.y = rect.y;
                Imgproc.rectangle(mRgba, rd.tl(), rd.br(), new Scalar(0, 0, 0, 255), 4, 8);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }
}
