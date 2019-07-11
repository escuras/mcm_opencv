package com.example.opencvexample.utils;

import android.content.Context;
import android.view.Display;
import android.view.WindowManager;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class ChangeFrame {

    private static int getOrientation(Display display ) {
        int rotation = display.getRotation();
        switch (rotation) {
            case 0:
            case 2:
                return 1;
            case 3:
                return -1;
            default:
                return 0;
        }
    }

    public static void changeRotation(Display display, Mat mRgba, Mat mRgbaF, Mat mRgbaT) {
        Core.transpose(mRgba, mRgbaT);
        Imgproc.resize(mRgbaT, mRgbaF, mRgbaF.size(), 0, 0, 0);
        if (getOrientation(display) != 0) {
            Core.flip(mRgbaF, mRgba, getOrientation(display));
        }
    }

}
