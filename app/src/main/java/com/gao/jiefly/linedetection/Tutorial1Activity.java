package com.gao.jiefly.linedetection;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.gao.jiefly.linedetection.Util.Util;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import static org.opencv.imgproc.Imgproc.CHAIN_APPROX_SIMPLE;
import static org.opencv.imgproc.Imgproc.MORPH_RECT;
import static org.opencv.imgproc.Imgproc.RETR_CCOMP;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;
import static org.opencv.imgproc.Imgproc.circle;
import static org.opencv.imgproc.Imgproc.dilate;
import static org.opencv.imgproc.Imgproc.erode;
import static org.opencv.imgproc.Imgproc.getStructuringElement;
import static org.opencv.imgproc.Imgproc.threshold;

/*
* 在CameraBridgeViewBase中改了视频的分辨率
*
*
* */
public class Tutorial1Activity extends Activity implements CvCameraViewListener2 {
    private static final String TAG = "OCVSample::Activity";
    boolean detechOk = false;
    private CameraBridgeViewBase mOpenCvCameraView;
    private Mat mRgba;
    private Mat mGray;
    private Mat mHsvMat;
    private Mat mMaskMat;
    private Mat mTmp;
    private Mat mLines;
    private Mat mDetechMat;
    private long currentTime;
    private Bitmap bitmap;
    private static final int BITMAP_OK = 6;
    private static final int SEND_TOAST = 5;
    private static final int UP = 0;
    private static final int DOWN = 1;
    private static final int LEFT = 2;
    private static final int RIGHT = 3;
    private static final int STOP = 4;


    private boolean isDetech = false;

    private ImageView mImageView;
    private CarControlService.ControlBinder mControlBinder;

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i("jiefly", "service connected");
            mControlBinder = (CarControlService.ControlBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    private android.os.Handler mHandler = new android.os.Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BITMAP_OK:
                    mImageView.setImageBitmap(bitmap);
                    break;
                case SEND_TOAST:
                    Toast.makeText(Tutorial1Activity.this, (String) msg.obj, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
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

    public Tutorial1Activity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.tutorial1_surface_view);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.tutorial1_activity_java_surface_view);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);
        mImageView = (ImageView) findViewById(R.id.imageView);

        Button button = (Button) findViewById(R.id.btn_change_canny);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isDetech = !isDetech;
                Log.e("jiefly", "isDetech:" + isDetech);
            }
        });
        Button btnBind = (Button) findViewById(R.id.btn_bind);
        btnBind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //绑定service，通过service来操控小车
                bindService(new Intent(Tutorial1Activity.this, CarControlService.class), mServiceConnection, BIND_AUTO_CREATE);
            }
        });


        Button btnTest = (Button) findViewById(R.id.btnTest);
        btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mControlBinder.move(UP,1000);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        //解绑service，防止内存泄漏
        unbindService(mServiceConnection);
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        /*VideoCapture videoCapture = new VideoCapture(0);
        System.out.printf("width"+Videoio.CV_CAP_PROP_FRAME_WIDTH);
        System.out.printf("height"+Videoio.CV_CAP_PROP_FRAME_HEIGHT);
        System.out.printf("============================================");
        videoCapture.set(Videoio.CV_CAP_PROP_FRAME_WIDTH, videoCapture.get(Videoio.CV_CAP_PROP_FRAME_WIDTH) / 3);
        videoCapture.set(Videoio.CV_CAP_PROP_FRAME_HEIGHT, videoCapture.get(Videoio.CV_CAP_PROP_FRAME_HEIGHT) / 3);
        System.out.printf("width"+Videoio.CV_CAP_PROP_FRAME_WIDTH);
        System.out.printf("height" + Videoio.CV_CAP_PROP_FRAME_HEIGHT);
        System.out.printf("============================================");*/
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mHsvMat = new Mat();
        mMaskMat = new Mat();
        mGray = new Mat(height, width, CvType.CV_8UC1);
        mTmp = new Mat(height, width, CvType.CV_8UC4);
        mLines = new Mat();
        currentTime = System.currentTimeMillis();

    }

    public void onCameraViewStopped() {
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();
        //return findRect(mGray,mRgba);
        Imgproc.cvtColor(mRgba, mHsvMat, Imgproc.COLOR_RGB2HSV);
        Scalar lowerThreshold = new Scalar(160, 100, 100);//红色-低 hsv
/*        75 90 90
                140 255 255*/
        Scalar upperThreshold = new Scalar(179, 255, 255);//红色-高 hsv
        Core.inRange(mHsvMat, lowerThreshold, upperThreshold, mMaskMat);
        Imgproc.dilate(mMaskMat, mTmp, new Mat());
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(mTmp, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        Log.e("jiefly", "contours size:" + contours.size());
        for (int i = 0; i < contours.size(); i++) {
            //Log.e("jiefly","宽："+contours.get(i).width()+"高"+contours.get(i).height());
            Imgproc.drawContours(mRgba, contours, i, new Scalar(255, 0, 0));
            if (contours.get(i).width() * contours.get(i).height() > 100) {

                //Imgproc.drawContours(mRgba, contours, i, new Scalar(0, 0, 255),2);
                //Log.e("jiefly", "宽：" + contours.get(i).width() + "高" + contours.get(i).height());
                Rect rect = Imgproc.boundingRect(contours.get(i));
                int x, y, w, h;
                x = rect.x;
                y = rect.y;
                w = rect.width;
                h = rect.height;
                //构造一个新的Mat用来存储挖出来的指定颜色区域，并对其进行矩形检测
                mDetechMat = new Mat(rect.size(), CvType.CV_8UC4);
                Mat m = new Mat(mRgba, rect);
                Core.copyMakeBorder(m, mDetechMat, 1, 1, 1, 1, Core.BORDER_DEFAULT);
                if (isDetech) {
                    bitmap = Bitmap.createBitmap(mDetechMat.cols(), mDetechMat.rows(), Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(mDetechMat, bitmap);
                    Util.saveBitmap("test", bitmap);
                    Log.e("jiefly", "宽：--" + mDetechMat.width() + "高：--" + mDetechMat.height() + "--");
                    mHandler.sendEmptyMessage(BITMAP_OK);
                    isDetech = false;
                }

                if (findRect(mDetechMat)) {
                    Imgproc.rectangle(mRgba, new Point(x, y), new Point(x + w, y + h), new Scalar(0, 255, 0), 2);
                }
            }
        }
        return mRgba;

       /* // return findRect(mGray,mRgba);
        if (detechOk || firstDetech) {
            Imgproc.GaussianBlur(inputFrame.rgba(), mRgba, new Size(5, 5), 0, 0);
            new mThread().start();
            detechOk = false;
            firstDetech = false;
        }
        //数组a存储检测出的直线端点坐标
        int[] a = new int[(int) mLines.total() * mLines.channels()];
        if (a.length > 4) {
            mLines.get(0, 0, a);
            for (int x = 0; x < a.length; x += 4) {
                Point start = new Point(a[x], a[x + 1]);
                Point end = new Point(a[x + 2], a[x + 3]);
                line(mRgba, start, end, new Scalar(0, 0, 255), 2);
               //Log.i("jiefly",a[x]+","+ a[x + 1]+","+a[x + 2]+","+a[x + 3]+",");
            }
        }*/
        // Imgproc.Canny(mGray, mTmp, canny_1, canny_2);
        // return mRgba;
    }

    public class mThread extends Thread {
        @Override
        public void run() {
            super.run();
            Imgproc.Canny(mGray, mTmp, canny_1, canny_2);
            //Imgproc.cvtColor(mTmp, mRgba, Imgproc.COLOR_GRAY2RGBA, 4);
            long time = System.currentTimeMillis();
            Imgproc.HoughLinesP(mTmp, mLines, 1, Math.PI / 180, 100, 50, 10);
            Log.e("jiefly", "用时：" + (System.currentTimeMillis() - time));
            detechOk = true;
        }
    }

    private int canny_1 = 100, canny_2 = 120;

    private boolean findRect(Mat srcMat) {
        Mat tmp = new Mat();
        Mat grayMat = new Mat();
        Imgproc.cvtColor(srcMat, grayMat, Imgproc.COLOR_BGR2GRAY);
        //获取自定义核
        Mat element = getStructuringElement(MORPH_RECT,
                new Size(3, 3));
        Imgproc.Canny(grayMat, tmp, 110, 110);
        threshold(grayMat, tmp, 125, 255, THRESH_BINARY);
        Mat dilate = new Mat();
        dilate(tmp, dilate, element);
        Mat erode = new Mat();
        erode(dilate, erode, element);
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(erode, contours, new Mat(), RETR_CCOMP, CHAIN_APPROX_SIMPLE);
        Log.e("jiefly", "矩形数目" + contours.size());
        if (contours.size() > 0) {
            return true;
        } else return false;
    }

    private Mat findRect(Mat grayMat, Mat srcMat) {
        Mat tmp = new Mat();
        //int g_nStructElementSize = 3; //结构元素(内核矩阵)的尺寸

        //获取自定义核
        Mat element = getStructuringElement(MORPH_RECT,
                new Size(3, 3));
        Imgproc.Canny(grayMat, tmp, 110, 110);
        threshold(grayMat, tmp, 125, 255, THRESH_BINARY);
        Mat dilate = new Mat();
        dilate(tmp, dilate, element);
        Mat erode = new Mat();
        erode(dilate, erode, element);

        List<MatOfPoint> contours = new ArrayList<>();

        Imgproc.findContours(erode, contours, new Mat(), RETR_CCOMP, CHAIN_APPROX_SIMPLE);
        int i = 0;
        for (MatOfPoint matOfPoint : contours) {
            i++;
            if (Imgproc.contourArea(matOfPoint) > 500) {
                Rect rect = Imgproc.boundingRect(matOfPoint);
                int x, y, w, h;
                x = rect.x;
                y = rect.y;
                w = rect.width;
                h = rect.height;
                Imgproc.rectangle(srcMat, new Point(x, y), new Point(x + w, y + h), new Scalar(0, 255, 0), 2);
                circle(srcMat, new Point(x + w / 2, y + h / 2), 3, new Scalar(255, 0, 0), 2);

                /*Moments moments =
                        Imgproc.moments(matOfPoint);

                double cx = moments.get_m10()/moments.get_m00();
                double cy = moments.get_m01()/moments.get_m00();
                circle(srcMat,new Point(cx,cy),3,new Scalar(255,0,0),2);*/
            }
        }
        return srcMat;
    }
}
