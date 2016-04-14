package com.gao.jiefly.linedetection.model;

import android.graphics.Bitmap;

import org.opencv.core.Mat;

/**
 * Created by jiefly on 2016/4/13.
 * Fighting_jiiiiie
 */
public interface ImageModel  {
    Bitmap getDetechImage(OnDetechListener mOnDetechListener);
    Mat getDetechLine(OnDetechListener mOnDetechListener);
    Bitmap getGrayImage();
    Bitmap getEdgeImage();
}
