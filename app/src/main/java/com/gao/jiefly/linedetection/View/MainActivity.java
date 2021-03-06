package com.gao.jiefly.linedetection.View;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;

import com.gao.jiefly.linedetection.R;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.FileNotFoundException;

import static org.opencv.imgproc.Imgproc.line;

/**
 * Created by jiefly on 2016/4/6.
 * Fighting_jiiiiie
 */
public class MainActivity extends Activity implements View.OnClickListener {
    private static final int PICTURE_CHOOSE = 1;
    private static final int PICTURE_TAKE = 2;
    private Mat rgbMat, grgbMat, contours, lines;
    private Mat gaussRgbMat, guassGrgbMat, guassLines, guassContours;
    private String TAG = "jiefly";
    private boolean isFirstResume = false;
    private Bitmap srcBitmap, grayBitmap, lineBitmap, guassSrcBitmap, guassGrayBitmap, guassLineBitmap;
    private ImageView iv, iv1, iv2, ivGuass, ivGuass1, ivGuass2;
    private Button btnChooseImage, btnOpenCam;
    //private Uri imageUri;
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
                case 4:
                    ivGuass.setImageBitmap(guassSrcBitmap);
                    break;
                case 5:
                    ivGuass1.setImageBitmap(guassGrayBitmap);
                    break;
                case 6:
                    ivGuass2.setImageBitmap(guassLineBitmap);
                    break;
            }
        }
    };
    private Thread detechLineThread = new Thread(new Runnable() {
        @Override
        public void run() {
            Utils.bitmapToMat(srcBitmap, rgbMat);
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
            int threshold = 200;
            //最小边缘长度
            int minLineSize = 50;
            //线中的最大间隔
            int lineGap = 10;
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
    });
    BaseLoaderCallback mBaseLoaderCallback = new BaseLoaderCallback(this) {
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
                    detechLineThread.start();
                    gaussRgbMat = new Mat();
                    guassLines = new Mat();
                    guassGrgbMat = new Mat();
                    guassContours = new Mat();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.canny);  //软件activity的布局
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title); //titlebar为自己标题栏的布局
        //原始图像
        iv = (ImageView) findViewById(R.id.canny_iv);
        ivGuass = (ImageView) findViewById(R.id.canny_iv_guass);
        //灰度图像
        iv1 = (ImageView) findViewById(R.id.canny_iv1);
        ivGuass1 = (ImageView) findViewById(R.id.canny_iv1_guass);
        //原始图像上画出检测到的直线
        iv2 = (ImageView) findViewById(R.id.canny_iv2);
        ivGuass2 = (ImageView) findViewById(R.id.canny_iv2_guass);

        btnChooseImage = (Button) findViewById(R.id.btnChooseImage);
        btnOpenCam = (Button) findViewById(R.id.btnOpenCam);

        btnChooseImage.setOnClickListener(this);
        btnOpenCam.setOnClickListener(this);


        srcBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.luffy);
        grayBitmap = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), Bitmap.Config.RGB_565);
        lineBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.luffy);

        iv.setImageBitmap(srcBitmap);
    }

    @Override
    protected void onStart() {
        super.onStart();
     /*   gaussRgbMat = new Mat();
        guassLines = new Mat();
        guassGrgbMat = new Mat();
        guassContours = new Mat();*/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PICTURE_TAKE:
                if (resultCode == RESULT_OK)
                    //detechLine(data);
                    break;

            case PICTURE_CHOOSE:
                if (resultCode == RESULT_OK)
                    detechLine(data);
                break;
        }
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

    private void detechLine(Intent data) {
        try {
            srcBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(data.getData()));
            lineBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(data.getData()));
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }
        grayBitmap = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), Bitmap.Config.RGB_565);

        guassGrayBitmap = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), Bitmap.Config.RGB_565);
        guassLineBitmap = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), Bitmap.Config.RGB_565);
        guassSrcBitmap = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), Bitmap.Config.RGB_565);

        handler.sendEmptyMessage(3);
        handler.sendEmptyMessage(1);
        new Thread(new Thread(new Runnable() {
            @Override
            public void run() {
                rgbMat.empty();
                grgbMat.empty();
                contours.empty();
                gaussRgbMat.empty();
                guassContours.empty();
                guassGrgbMat.empty();

                Utils.bitmapToMat(srcBitmap, rgbMat);
                long time;
                time = System.currentTimeMillis();
                Imgproc.GaussianBlur(rgbMat, gaussRgbMat, new Size(5, 5), 0, 0);
                System.out.println("高斯滤波所用时间：" + (System.currentTimeMillis() - time));
                /*gaussRgbMat = rgbMat;
                Mat yuv = new Mat();
                // Core.split(rgbMat,gaussRgbMat);
                Imgproc.cvtColor(gaussRgbMat, yuv, Imgproc.COLOR_RGB2YCrCb);

                Core.extractChannel(yuv, gaussRgbMat, 0);*/


                Utils.matToBitmap(gaussRgbMat,guassSrcBitmap);
                handler.sendEmptyMessage(4);

                //转换为灰度图，便于边缘提取
                Imgproc.cvtColor(rgbMat, grgbMat, Imgproc.COLOR_BGR2GRAY, 4);
                Imgproc.cvtColor(gaussRgbMat, guassGrgbMat, Imgproc.COLOR_BGR2GRAY, 4);
                //边缘提取
                Imgproc.Canny(grgbMat, contours, 50, 150, 3, false);
                Imgproc.threshold(contours, contours, 128, 255, Imgproc.THRESH_BINARY);

                Imgproc.Canny(guassGrgbMat, guassContours, 50, 150, 3, false);
                Imgproc.threshold(guassContours, guassContours, 128, 255, Imgproc.THRESH_BINARY);
                //将灰度矩阵转换为Bitmap
                Utils.matToBitmap(contours, grayBitmap);
                Utils.matToBitmap(guassContours, guassGrayBitmap);
                //在界面上显示灰度图
                handler.sendEmptyMessage(2);
                handler.sendEmptyMessage(5);
                //存储识别出的直线
                lines = new Mat();
                int threshold = 200;
                //最小边缘长度
                int minLineSize = 50;
                //线中的最大间隔
                int lineGap = 10;
                //识别图片耗时
                double startTime = System.currentTimeMillis();
                Imgproc.HoughLinesP(contours, lines, 1, Math.PI / 180, threshold, minLineSize, lineGap);
                System.out.println("用时：" + (System.currentTimeMillis() - startTime));

                startTime = System.currentTimeMillis();
                Imgproc.HoughLinesP(guassContours, guassLines, 1, Math.PI / 180, threshold, minLineSize, lineGap);
                System.out.println("高斯滤波后用时：" + (System.currentTimeMillis() - startTime));

                int[] a = new int[(int) lines.total() * lines.channels()];

                int[] b = new int[(int) guassLines.total() * guassLines.channels()];
                //数组a存储检测出的直线端点坐标
                if (a.length > 4) {
                    lines.get(0, 0, a);
                    for (int i:a)
                        System.out.print(i+",");
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
                if (b.length > 4) {
                    guassLines.get(0, 0, b);
                    int y = 0;
                    for (int x = 0; x < b.length; x += 4) {
                        Point start = new Point(b[x], b[x + 1]);
                        Point end = new Point(b[x + 2], b[x + 3]);
                        line(gaussRgbMat, start, end, new Scalar(0, 0, 255), 3);
                        Utils.matToBitmap(gaussRgbMat, guassLineBitmap);
                        // System.out.format("x1=%d,y1=%d,x2=%d,y2=%d\n",x1,y1,x2,y2);
                        y++;
                    }
                    handler.sendEmptyMessage(6);
                    // System.out.println("width:" + lines.width() + "height:" + lines.height());
                    System.out.println("总共有" + y + "条线");
                }
            }
        })).start();
    }

    @Override
    public void onClick(View v) {
        /*File outputImage = new File(Environment.getExternalStorageDirectory(), "myImage.jpg");
        try {
            if (outputImage.exists()) {
                outputImage.delete();
            }
            outputImage.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        switch (v.getId()) {
            case R.id.btnChooseImage:
                //  imageUri = Uri.fromFile(outputImage);
                Intent intent1 = new Intent("android.intent.action.GET_CONTENT");
                intent1.setType("image/*");
                intent1.putExtra("crop", true);
                intent1.putExtra("scale", true);
                // intent1.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent1, PICTURE_CHOOSE);
                break;
            case R.id.btnOpenCam:
                // imageUri = Uri.fromFile(outputImage);
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                // intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent, PICTURE_TAKE);
                break;
        }
    }
}

