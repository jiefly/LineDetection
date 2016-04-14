package com.gao.jiefly.linedetection.model;

import android.graphics.Bitmap;

import com.gao.jiefly.linedetection.Util.Util;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

/**
 * Created by jiefly on 2016/4/13.
 * Fighting_jiiiiie
 */
public class ImageModelImpl implements ImageModel {
    private Bitmap srcBitmap;

    private Mat srcMat = new Mat();
    private Mat grayMat = new Mat();
    private Mat edgeMat = new Mat();
    private Mat linesMat = new Mat();
    private Mat drewMat = new Mat();

    public ImageModelImpl(Bitmap srcBitmap) {
        this.srcBitmap = srcBitmap;
    }


    @Override
    public Bitmap getDetechImage(final OnDetechListener mOnDetechListener) {

        return Util.drawLines(srcBitmap, getDetechLine(mOnDetechListener));
    }

    @Override
    public Mat getDetechLine(OnDetechListener mOnDetechListener) {
        //对图片预处理（转换为灰度图，提取边缘）
        getEdgeImage();
        int threshold = 200;
        //最小边缘长度
        int minLineSize = 50;
        //线中的最大间隔
        int lineGap = 10;
        //识别图片耗时
        double startTime = System.currentTimeMillis();
        Imgproc.HoughLinesP(edgeMat, linesMat, 1, Math.PI / 180, threshold, minLineSize, lineGap);
        System.out.println("用时：" + (System.currentTimeMillis() - startTime));
        mOnDetechListener.onSuccess();
        return linesMat;
    }

    @Override
    public Bitmap getGrayImage() {
        //创建和是srcBitmap一样大小的grayBitmap
        Bitmap grayBitmap = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), Bitmap.Config.RGB_565);
        //将srcBitmap转换成Mat，便于后续处理
        Utils.bitmapToMat(srcBitmap, srcMat);
        //将srcMat灰度化，便于之后的便于提取
        Imgproc.cvtColor(srcMat, grayMat, Imgproc.COLOR_BGR2GRAY, 4);

        Utils.matToBitmap(grayMat, grayBitmap);

        return grayBitmap;
    }

    @Override
    public Bitmap getEdgeImage() {
        //创建和是srcBitmap一样大小的edgeBitmap
        Bitmap edgeBitmap = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), Bitmap.Config.RGB_565);

        //边缘提取
        Imgproc.Canny(grayMat, edgeMat, 50, 150, 3, false);
        Imgproc.threshold(edgeMat, edgeMat, 128, 255, Imgproc.THRESH_BINARY);

        //将灰度矩阵转换为Bitmap
        Utils.matToBitmap(edgeMat, edgeBitmap);

        return edgeBitmap;
    }
}
