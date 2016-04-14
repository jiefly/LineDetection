package com.gao.jiefly.linedetection;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import static org.opencv.imgproc.Imgproc.line;

public class Tutorial1Activity extends Activity implements CvCameraViewListener2 {
    private static final String TAG = "OCVSample::Activity";
    boolean detechOk = false;
    boolean firstDetech = true;
    private CameraBridgeViewBase mOpenCvCameraView;
    private boolean mIsJavaCamera = true;
    private MenuItem mItemSwitchCamera = null;
    private Mat mRgba;
    private Mat mGray;
    private Mat mTmp;
    private Mat mLines;
    private long currentTime;

    private Button mButton;
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

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.tutorial1_surface_view);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.tutorial1_activity_java_surface_view);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);

        mButton = (Button) findViewById(R.id.btn_change_canny);
        mButton.setVisibility(View.INVISIBLE);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                canny_1 = (int) (1.1*canny_1);
                canny_2 = (int) (1.1*canny_2);
                Log.e("jiefly","canny_1:"+canny_1+"canny_2:"+canny_2);
            }
        });
        // mOpenCvCameraView.
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
            }
        }
       // Imgproc.Canny(mGray, mTmp, canny_1, canny_2);
        return mRgba;
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

    private int canny_1=100,canny_2=120;
}
