#include <com_example_opencvexample_opencvnative_OpenCvNativeClass.h>

  JNIEXPORT jint JNICALL Java_com_example_opencvexample_opencvnative_OpenCvNativeClass_convertGray
  (JNIEnv *, jclass, jlong addrRgba, jlong addrGray) {
    Mat& mRgba = *(Mat*) addrRgba;
    Mat& mGray = *(Mat*) addrGray;

    int conv;
    jint retVal;
    conv = toGray(mRgba, mGray);
    retVal = (jint) conv;
    return retVal;
  }

  int toGray(Mat img, Mat& gray){
    cvtColor(img, gray, COLOR_RGBA2GRAY);
    if(gray.rows == img.rows && gray.cols == img.cols) {
        return 1;
    }
    return 0;
  }
