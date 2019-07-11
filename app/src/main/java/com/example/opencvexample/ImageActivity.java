package com.example.opencvexample;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.Arrays;

public class ImageActivity extends AppCompatActivity {

    private static final String TAG = "ImageActivity";

    private Mat originalImage;
    private Mat simpleImage;
    private static final int SELECT_PICTURE = 1;
    private String selectedImagePath;
    private Bitmap bitMap;
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        checkPermissions();
        if (savedInstanceState != null) {
            ImageView iv = (ImageView) findViewById(R.id.ImageView);
            bitMap = savedInstanceState.getParcelable("image");
            if(bitMap != null) {
                iv.setImageBitmap(bitMap);
                simpleImage = new Mat();
                Utils.bitmapToMat(bitMap, simpleImage);
            }
        }
    }

    private void visibleMenuOptions(){
        MenuItem menuItemHistogram = (MenuItem) menu.findItem(R.id.action_Histogram);
        MenuItem menuItemAverage = (MenuItem) menu.findItem(R.id.action_resize);
        MenuItem menuItemGaussian = (MenuItem) menu.findItem(R.id.action_gaussian);
        MenuItem menuItemMedian = (MenuItem) menu.findItem(R.id.action_binary);
        MenuItem menuItemAdaptive = (MenuItem) menu.findItem(R.id.action_adaptive);
        MenuItem menuItemCanny = (MenuItem) menu.findItem(R.id.action_canny);
        MenuItem menuItemNormal = (MenuItem) menu.findItem(R.id.action_Normal);
        MenuItem menuItemMask = (MenuItem) menu.findItem(R.id.action_mask);
        if(menuItemHistogram != null) {
            menuItemHistogram.setVisible(true);
            menuItemAverage.setVisible(true);
            menuItemGaussian.setVisible(true);
            menuItemMedian.setVisible(true);
            menuItemCanny.setVisible(true);
            menuItemNormal.setVisible(true);
            menuItemAdaptive.setVisible(true);
            menuItemMask.setVisible(true);
        }
    }

    @Override
    protected void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle bundle = new Bundle();
        outState.putParcelable("image", bitMap);
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    Log.i(TAG, "OpenCV loaded successfully");
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.menu = menu;
        if(bitMap != null) {
            this.visibleMenuOptions();
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_openGallery) {
            bitMap = null;
            checkPermissions();
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent,"Choose image"), SELECT_PICTURE);
            return true;
        } else if (id == R.id.action_Normal) {
            Mat histImage = new Mat();
            simpleImage.copyTo(histImage);
            calcHist(histImage);
            displayImage(simpleImage);
            return true;
        } else if (id == R.id.action_Histogram) {
            Mat histImage = new Mat();
            simpleImage.copyTo(histImage);
            calcHist(histImage);
            displayImage(histImage);
            return true;
        } else if(id == R.id.action_resize) {
            Mat resizedImage = new Mat();
            Size size=new Size(40,40);
            Imgproc.resize(simpleImage, resizedImage, size);
            displayImage(resizedImage);
            return true;
        } else if(id == R.id.action_gaussian) {
            Mat blurredImage = new Mat();
            Size size = new Size(7,7);
            Imgproc.GaussianBlur(simpleImage, blurredImage, size, 0,0);
            displayImage(blurredImage);
            return true;
        } else if(id == R.id.action_binary) {
            Mat binaryImage = new Mat();
            Mat grayImage = new Mat();
            Imgproc.cvtColor(simpleImage, grayImage, Imgproc.COLOR_RGB2GRAY);
            Imgproc.threshold(grayImage, binaryImage,120, 255,Imgproc.THRESH_BINARY);
            displayImage(binaryImage);
            return true;
        }  else if(id == R.id.action_adaptive) {
            Mat binaryImage = new Mat();
            Mat grayImage = new Mat();
            Imgproc.cvtColor(simpleImage, grayImage, Imgproc.COLOR_RGB2GRAY);
            Imgproc.adaptiveThreshold(grayImage, binaryImage,155, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 2);
            displayImage(binaryImage);
            return true;
        } else if(id == R.id.action_mask) {
            Mat gray = new Mat();
            Imgproc.cvtColor(simpleImage, gray, Imgproc.COLOR_RGB2GRAY);
            Mat mask = new Mat(simpleImage.rows(), simpleImage.cols(), CvType.CV_8U, Scalar.all(0));
            org.opencv.core.Point point = new org.opencv.core.Point(simpleImage.rows() / 2, simpleImage.cols() / 2);
            Size size = new Size(simpleImage.rows() /3, simpleImage.cols() /3);
            Scalar scalar = new Scalar(255, 255, 255);
            Imgproc.ellipse( mask, point, size, 70, 0, 360, scalar, -1, 8, 0);
            Mat withMask = new Mat();
            simpleImage.copyTo(withMask, mask);
            displayImage(withMask);
            return true;
        } else if(id==R.id.action_canny) {
            Mat gray = new Mat();
            Imgproc.cvtColor(simpleImage, gray, Imgproc.COLOR_RGB2GRAY);
            Mat edgeImage=new Mat();
            Imgproc.Canny(gray, edgeImage, 100, 200);
            displayImage(edgeImage);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void calcHist(Mat image)
    {
        int mHistSizeNum = 25;
        MatOfInt mHistSize = new MatOfInt(mHistSizeNum);
        Mat hist = new Mat();
        float []mBuff = new float[mHistSizeNum];
        MatOfFloat histogramRanges = new MatOfFloat(0f, 256f);
        Scalar mColorsRGB[] = new Scalar[] { new Scalar(200, 0, 0, 255), new Scalar(0, 200, 0, 255), new Scalar(0, 0, 200, 255) };
        org.opencv.core.Point mP1 = new org.opencv.core.Point();
        org.opencv.core.Point mP2 = new org.opencv.core.Point();

        int thikness = (int) (image.width() / (mHistSizeNum + 10) / 3);
        if(thikness > 3) {
            thikness = 3;
        }
        MatOfInt mChannels[] = new MatOfInt[] { new MatOfInt(0), new MatOfInt(1), new MatOfInt(2) };
        Size sizeRgba = image.size();
        int offset = (int) ((sizeRgba.width - (3 * mHistSizeNum + 30) * thikness));

        int counter;
        for(counter = 0; counter < 3; counter++) {
            Imgproc.calcHist(Arrays.asList(image), mChannels[counter], new Mat(), hist, mHistSize, histogramRanges);
            Core.normalize(hist, hist, sizeRgba.height/2, 0, Core.NORM_INF);
            hist.get(0, 0, mBuff);
            int music;
            for(music = 0; music <mHistSizeNum; music++) {
                mP1.x = mP2.x = offset + (counter * (mHistSizeNum + 10) + music) * thikness;
                mP1.y = sizeRgba.height - 1;
                mP2.y = mP1.y - (int) mBuff[music];
                Imgproc.line(image, mP1, mP2, mColorsRGB[counter], thikness);
            }
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
                selectedImagePath = getPath(selectedImageUri);
                Log.i(TAG, "Imagem esolhida: " + selectedImagePath);
                loadImage(selectedImagePath);
                displayImage(simpleImage);
            }
        }
    }

    private void displayImage(Mat image)
    {
        Bitmap bitMap = Bitmap.createBitmap(image.cols(), image.rows(),Bitmap.Config.RGB_565);
        Utils.matToBitmap(image, bitMap);
        ImageView iv = (ImageView) findViewById(R.id.ImageView);
        iv.setImageBitmap(bitMap);
    }

    private String getPath(Uri uri) {
        String filePath = "";
        String fileId = DocumentsContract.getDocumentId(uri);
        // Split at colon, use second item in the array
        String id = fileId.split(":")[1];
        String[] column = {MediaStore.Images.Media.DATA};
        String selector = MediaStore.Images.Media._ID + "=?";
        Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                column, selector, new String[]{id}, null);
        int columnIndex = cursor.getColumnIndex(column[0]);
        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }
        cursor.close();
        return filePath;
    }

    private void loadImage(String path)
    {
        originalImage = Imgcodecs.imread(path);
        Mat rgbImage = new Mat();

        Imgproc.cvtColor(originalImage, rgbImage, Imgproc.COLOR_BGR2RGB);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        int width = size.x;
        int height = size.y;
        simpleImage = new Mat();

        double downSampleRatio = calculateSubSampleSize(rgbImage,width,height);

        Imgproc.resize(rgbImage, simpleImage, new Size(),downSampleRatio,downSampleRatio,Imgproc.INTER_AREA);

        try {
            ExifInterface exif = new ExifInterface(selectedImagePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            switch (orientation)
            {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    simpleImage = simpleImage.t();
                    Core.flip(simpleImage, simpleImage, 1);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    simpleImage = simpleImage.t();
                    Core.flip(simpleImage, simpleImage, 0);
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        visibleMenuOptions();
    }



    private static double calculateSubSampleSize(Mat srcImage, int reqWidth, int reqHeight) {
        final int height = srcImage.height();
        final int width = srcImage.width();
        double inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final double heightRatio = (double) reqHeight / (double) height;
            final double widthRatio = (double) reqWidth / (double) width;
            inSampleSize = heightRatio<widthRatio ? heightRatio :widthRatio;
        }
        return inSampleSize;
    }


    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    1);
        }
    }
}
