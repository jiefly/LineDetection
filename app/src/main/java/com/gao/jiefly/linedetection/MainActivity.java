package com.gao.jiefly.linedetection;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import static org.opencv.imgproc.Imgproc.line;

/**
 * Created by jiefly on 2016/4/6.
 * Fighting_jiiiiie
 */
public class MainActivity extends Activity {
    private Mat rgbMat, grgbMat, contours, lines;
    private String TAG = "jiefly";
    private boolean isFirstResume = false;

    private Bitmap srcBitmap, grayBitmap, lineBitmap;

    private ImageView iv, iv1, iv2;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    iv.setImageBitmap(srcBitmap);
                    break;
                case 2:
                    iv1.setImageBitmap(grayBitmap);
                    break;
                case 3:
                    iv2.setImageBitmap(lineBitmap);
                    break;
            }
        }
    };
    private BaseLoaderCallback mBaseLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            super.onManagerConnected(status);
            switch (status) {
                case BaseLoaderCallback.INIT_FAILED:
                    Log.i(TAG, "初始化失败");
                    break;
                case BaseLoaderCallback.SUCCESS:
                    Log.i(TAG, "初始化成功");
                    rgbMat = new Mat();
                    grgbMat = new Mat();
                    contours = new Mat();

                    Utils.bitmapToMat(srcBitmap, rgbMat);

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            //转换为灰度图，便于边缘提取
                            Imgproc.cvtColor(rgbMat, grgbMat, Imgproc.COLOR_BGR2GRAY, 4);
                            //边缘提取
                            Imgproc.Canny(grgbMat, contours, 50, 150, 3, false);
                            Imgproc.threshold(contours, contours, 128, 255, Imgproc.THRESH_BINARY);
                            //将灰度矩阵转换为Bitmap
                            Utils.matToBitmap(contours, grayBitmap);
                            //在界面上显示灰度图
                            handler.sendEmptyMessage(2);
                            //存储识别出的直线
                            lines = new Mat();
                            int threshold = 90;
                            //最小边缘长度
                            int minLineSize = 50;
                            //线中的最大间隔
                            int lineGap = 20;
                            //识别图片耗时
                            double startTime = System.currentTimeMillis();
                            Imgproc.HoughLinesP(contours, lines, 1, Math.PI / 180, threshold, minLineSize, lineGap);
                            System.out.println("用时：" + (System.currentTimeMillis() - startTime));

                            int[] a = new int[(int) lines.total() * lines.channels()];
                            //数组a存储检测出的直线端点坐标
                            lines.get(0, 0, a);
                            int y = 0;
                            for (int x = 0; x < a.length; x += 4) {
                                Point start = new Point(a[x], a[x + 1]);
                                Point end = new Point(a[x + 2], a[x + 3]);
                                line(rgbMat, start, end, new Scalar(0, 0, 255), 3);
                                Utils.matToBitmap(rgbMat, lineBitmap);
                                // System.out.format("x1=%d,y1=%d,x2=%d,y2=%d\n",x1,y1,x2,y2);
                                y++;
                            }
                            handler.sendEmptyMessage(3);
                           // System.out.println("width:" + lines.width() + "height:" + lines.height());
                            System.out.println("总共有" + y + "条线");
                        }
                    }).start();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.canny);
        //原始图像
        iv = (ImageView) findViewById(R.id.canny_iv);
        //灰度图像
        iv1 = (ImageView) findViewById(R.id.canny_iv1);
        //原始图像上画出检测到的直线
        iv2 = (ImageView) findViewById(R.id.canny_iv2);


        srcBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.luffy);
        grayBitmap = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), Bitmap.Config.RGB_565);
        lineBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.luffy);

        iv.setImageBitmap(srcBitmap);
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isFirstResume) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, getApplicationContext(), mBaseLoaderCallback);
        }
        isFirstResume = true;
        Log.i(TAG, "onResume sucess load OpenCV...");

    }
}
