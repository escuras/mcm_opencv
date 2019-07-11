LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

#opencv
OPENCVROOT := /home/toze/Android/OpenCV-android-sdk
OPENCV_CAMERA_MODULES := on
OPENCV_INSTALL_MODULES := on
OPENCV_LIB_TYPE := SHARED
include ${OPENCVROOT}/sdk/native/jni/OpenCV.mk

LOCAL_SRC_FILES:= com_example_opencvexample_opencvnative_OpenCvNativeClass.cpp
LOCAL_LDLIBS+= -llog
LOCAL_MODULE := MyOpenCvLibs

include $(BUILD_SHARED_LIBRARY)
