package com.gao.jiefly.linedetection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by jiefly on 2016/4/9.
 * Fighting_jiiiiie
 */
public class Util {
    public static LinkedList<List<Integer>> getRet(int[] point){
        HashMap<String,Double> theta = new HashMap<>();

        boolean flag = false;

        List<Integer> line = new ArrayList<>();

        HashMap<Integer,List<Integer>> lines = new HashMap<>();

        //计算出所有直线的角度
        for (int i = 0 ;i<point.length;i+=4){
            theta.put(String.valueOf(i / 4), Math.toDegrees(Math.atan((point[i + 3] - point[i + 1]) / (point[i + 1] - point[i]))));
        }
        //对这些角度进行排序
        sortHashmap(theta);
        LinkedList<String[]> parallel = new LinkedList<>();
        for (int i=0;i<keyList.size();i++){
            if ((valueList.get(i+1)-valueList.get(i))<5){

                parallel.add(i,new String[]{keyList.get(i),keyList.get(i+1)});

            }
        }

        return null;
    }
    public static void sortHashmap(HashMap<String,Double> map){
        keyList= new LinkedList<String>();
        keyList.addAll(map.keySet());
        valueList = new LinkedList<Double>();
        valueList.addAll(map.values());
        for(int i=0; i<valueList.size(); i++)
            for(int j=i+1; j<valueList.size(); j++) {
                if(valueList.get(j)>valueList.get(i)) {
                    valueList.set(j, valueList.get(i));
                    valueList.set(i, valueList.get(j));
                    //同样调整对应的key值
                    keyList.set(j, keyList.get(i));
                    keyList.set(i, keyList.get(j));
                }
            }
    }
    public static List<String> keyList;
    public static List<Double> valueList;
}
