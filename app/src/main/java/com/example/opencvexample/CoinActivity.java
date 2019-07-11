package com.example.opencvexample;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.example.opencvexample.utils.ChangeFrame;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import java.util.ArrayList;
import java.util.List;

public class CoinActivity extends AppCompatActivity implements CvCameraViewListener2 {

    private static final String TAG = "ColorDetectActivity";

    private Mat mRgba;
    private Mat mRgbaF;
    private Mat mRgbaT;
    private Display display;
    private Menu menu;
    private CameraBridgeViewBase mOpenCvCameraView;
    private int nCircles = 0;
    private Switch switchGaussian;
    private Switch switchBlur;
    private boolean bGaussian = false;
    private boolean bBlur = false;
    private double cannyHighThreshold = 195;
    private double accumlatorThreshold = 35;
    private int minRadius = 10;
    private int maxRadius = 400;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coin);
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.coin_detection_activity_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        display = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        switchGaussian = (Switch)  findViewById(R.id.switch_Gaussian);
        switchBlur = (Switch)  findViewById(R.id.switch_blur);
        addEvents();
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
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

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mRgbaF = new Mat(height, width, CvType.CV_8UC4);
        mRgbaT = new Mat(width, width, CvType.CV_8UC4);
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        ChangeFrame.changeRotation(display, mRgba, mRgbaF, mRgbaT);
        if(mRgba == null) {
            return null;
        }
        Mat grayImage = new Mat();
        Imgproc.cvtColor(mRgba, grayImage, Imgproc.COLOR_RGB2GRAY);
        if(bBlur) {
            Imgproc.medianBlur(grayImage, grayImage, 5);
        }
        if (bGaussian) {
            Size size = new Size(5,5);
            Imgproc.GaussianBlur(grayImage, grayImage, size, 0,0);
        }
        double minDistance = grayImage.rows() / 16;
        Mat circles = new Mat();
        Imgproc.HoughCircles(grayImage, circles, Imgproc.CV_HOUGH_GRADIENT, 1, minDistance, cannyHighThreshold, accumlatorThreshold, minRadius, maxRadius);
        drawCircles(circles);
        Imgproc.putText(mRgba, "There are " + nCircles + " coins.",
                new Point(10, 350),
                Imgproc.FONT_HERSHEY_DUPLEX, 2.0, new Scalar(0, 0, 255));
        return mRgba;
    }

    private void drawCircles(Mat circles){
        List<Point> points = new ArrayList<>();
        for (int i = 0; i < circles.cols(); i++)
        {
            double[] circle = circles.get(0, i);
            double centerX = circle[0],
                    centerY = circle[1],
                    radius = circle[2];
            org.opencv.core.Point center = new org.opencv.core.Point(centerX, centerY);
            if(isInsideOthers(points, centerX, centerY, radius)){
                continue;
            }
            points.add(center);
            Imgproc.circle(mRgba, center, (int) radius, new Scalar(0,0,255),5);
        }
        nCircles = points.size();
    }

    private boolean isInsideOthers(List<Point> points, double centerX, double centerY, double radius){
        Rect rect = new Rect();
        rect.height = (int)radius * 2;
        rect.width = rect.height;
        rect.x = (int) (centerX - radius);
        rect.y = (int) (centerY - radius);
        for(org.opencv.core.Point point : points) {
            if(point.inside(rect)) {
                return true;
            }
        }
        return false;
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

    @Override
    public void onCameraViewStopped() {
    }

    @Override
    public void onPause(){
        super.onPause();
        mRgba.release();
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRgba.release();
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }
    }

    private void addEvents(){
        switchGaussian.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    bGaussian = true;
                } else {
                    bGaussian = false;
                }
            }
        });
        switchBlur.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    bBlur = true;
                } else {
                    bBlur = false;
                }
            }
        });
    }
}
