package com.example.opencvexample;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import com.example.opencvexample.utils.CascadeHelper;
import com.example.opencvexample.utils.ChangeFrame;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Rect2d;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.InputStream;

public class HandsDetectActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private CameraBridgeViewBase mOpenCvCameraView;
    private CascadeClassifier cascadeClassifier;
    private int absoluteHandsSize;
    private Mat mRgba;
    private Mat mRgbaF;
    private Mat mRgbaT;
    private Display display;

    BaseLoaderCallback mLoaderCallBack = new BaseLoaderCallback(this) {

        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case BaseLoaderCallback.SUCCESS:
                    getCascade();
                    mOpenCvCameraView.enableView();
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    private void getCascade() {
        InputStream is = getResources().openRawResource(R.raw.haarcascade_hand_2);
        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
        cascadeClassifier = CascadeHelper.initializeOpenCVDependencies(is, cascadeDir,
                "haarcascade_hand_2.xml");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hands_detect);
        mOpenCvCameraView = (JavaCameraView) findViewById(R.id.java_camera_hands_detect);
        mOpenCvCameraView.setVisibility(View.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRgba.release();
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallBack);
        } else {
            mLoaderCallBack.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        absoluteHandsSize = (int) (height * 0.4);
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mRgbaF = new Mat(height, width, CvType.CV_8UC4);
        mRgbaT = new Mat(width, width, CvType.CV_8UC4);
    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        ChangeFrame.changeRotation(display, mRgba, mRgbaF, mRgbaT);
        drawRectandles(calculateRectangles());
        return mRgba;
    }

    private Rect[] calculateRectangles(){
        MatOfRect hands = new MatOfRect();
        if (cascadeClassifier != null) {
            cascadeClassifier.detectMultiScale(mRgba, hands, 2, 1, 1,
                    new Size(absoluteHandsSize, absoluteHandsSize), new Size());
        }
        return hands.toArray();
    }

    private void drawRectandles(Rect[] facesArray) {
        if (facesArray != null) {
            for (Rect rect : facesArray) {
                Rect2d rd = new Rect2d();
                rd.height = rect.height;
                rd.width = rect.width;
                rd.x = rect.x;
                rd.y = rect.y;
                Imgproc.rectangle(mRgba, rd.tl(), rd.br(), new Scalar(0, 0, 255, 255), 4, 8);
            }
        }
    }
}
