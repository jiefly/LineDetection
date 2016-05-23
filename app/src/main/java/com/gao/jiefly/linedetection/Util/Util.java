package com.gao.jiefly.linedetection.Util;

import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.opencv.imgproc.Imgproc.line;

/**
 * Created by jiefly on 2016/4/9.
 * Fighting_jiiiiie
 */
public class Util {
    public static Double[] thetas = new Double[]{};
    public static List<String> keyList;
    public static List<Double> valueList;


    /** 保存方法 */
    public static void saveBitmap(String picName,Bitmap bitmap) {
        Log.e("jiefly", "保存图片");
        File f = new File("/sdcard/", picName+".jpg");
        if (f.exists()) {
            f.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
            Log.i("jiefly", "已经保存");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static LinkedList<List<Integer>> getRet(int[] point) {
        HashMap<String, Double> theta = new HashMap<>();


        boolean flag = false;

        List<Integer> line = new ArrayList<>();

        HashMap<Integer, List<Integer>> lines = new HashMap<>();

        //计算出所有直线的角度
        for (int i = 0; i < point.length; i += 4) {
            theta.put(String.valueOf(i / 4), Math.toDegrees(Math.atan((point[i + 3] - point[i + 1]) / (point[i + 1] - point[i]))));
            thetas[i / 4] = Math.toDegrees(Math.atan((point[i + 3] - point[i + 1]) / (point[i + 1] - point[i])));
        }
        //对这些角度进行排序
        sortHashmap(theta);
        LinkedList<String[]> parallel = new LinkedList<>();
        for (int i = 0; i < keyList.size(); i++) {
            if ((valueList.get(i + 1) - valueList.get(i)) < 5) {

                parallel.add(i, new String[]{keyList.get(i), keyList.get(i + 1)});

            }
        }

        return null;
    }

    public static void sortHashmap(HashMap<String, Double> map) {
        keyList = new LinkedList<String>();
        keyList.addAll(map.keySet());
        valueList = new LinkedList<Double>();
        valueList.addAll(map.values());
        for (int i = 0; i < valueList.size(); i++)
            for (int j = i + 1; j < valueList.size(); j++) {
                if (valueList.get(j) > valueList.get(i)) {
                    valueList.set(j, valueList.get(i));
                    valueList.set(i, valueList.get(j));
                    //同样调整对应的key值
                    keyList.set(j, keyList.get(i));
                    keyList.set(i, keyList.get(j));
                }
            }
    }

    public static int[] getVertical(HashMap<String, Double> theat) {
        Double[] line = new Double[]{};

        for (int i = 0; i < theat.size(); i++) {
            line[i] = theat.get(i + "");
        }

        for (int i = 0; i < theat.size(); i++) {
            int target = (int) (theat.get(i) - 90);
            if (theat.containsValue(target)) {
                //return new int[]{i,theat.}
            }
        }

        return null;
    }

    public static int[] twoSumFurtherOptimize(int[] nums, int target) {
        int numsLength = nums.length;
        int[] resultArr = new int[]{};
        Map<Integer, Integer> map = new HashMap<>();

        for (int i = 0; i < numsLength; i++) {
            int search = nums[i] - target;
            if (map.containsKey(search) && map.get(search) != i) {
                return new int[]{map.get(search), i};
            }
            map.put(nums[i], i);  // 向HashSet插入值
        }
        return resultArr;
    }

    public static Bitmap drawLines(Bitmap srcBitmap, Mat lines) {
        //Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), Bitmap.Config.RGB_565);
        //原始图像转换后的Mat
        Mat srcMat = new Mat();
        Utils.bitmapToMat(srcBitmap, srcMat);
        //用于存储Lines中的线段信息，每四个元素存储一条直线的信息（起始点坐标）
        int[] line = new int[(int) lines.total() * lines.channels()];
        //通过get方法获取lines中的信息
        lines.get(0, 0, line);
        //线段条数计数器
        int lineCount = 0;
        for (int x = 0; x < line.length; x += 4) {
            Point start = new Point(line[x], line[x + 1]);
            Point end = new Point(line[x + 2], line[x + 3]);
            line(srcMat, start, end, new Scalar(0, 0, 255), 3);
            // System.out.format("x1=%d,y1=%d,x2=%d,y2=%d\n",x1,y1,x2,y2);
            lineCount++;
        }

        Utils.matToBitmap(srcMat, srcBitmap);
        System.out.printf("共有" + lineCount + "条直线");
        return srcBitmap;
    }
}
