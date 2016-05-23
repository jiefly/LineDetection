package com.gao.jiefly.linedetection;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class CarControlService extends Service {
    private static final String CAR_DISCONNE = "car disconnected!!!";
    private static final String KEY_HANDLER = "avtivity_handler";
    private static final int UP = 0;
    private static final int DOWN = 1;
    private static final int LEFT = 2;
    private static final int RIGHT = 3;
    private static final int STOP = 4;

    private static final int SEND_TOAST = 5;

    private boolean connectFlag = false;
    private boolean upKey = false;
    private boolean downKey = false;
    private boolean stopKey = false;
    private boolean leftKey = false;
    private boolean rightKey = false;
    private static final String TAG = "carControlService";
    private ControlBinder mControlBinder = new ControlBinder();
    //service 持有activity 注意内存泄漏
    private Handler activityHandler;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                   // Toast.makeText(getApplicationContext(), "Disconnected", Toast.LENGTH_SHORT);
                    Log("Disconnected");
                    /*connectionStateTV.setText("Disconnected");*/
                    connectFlag = false;
                    break;
                case 1:
                   // Toast.makeText(getApplicationContext(), "connected", Toast.LENGTH_SHORT);
//                    connectionStateTV.setText("connected");
                    Log("connected");
                    connectFlag = true;
                    break;
                case 2:
                    //Toast.makeText(getApplicationContext(), "wait connection", Toast.LENGTH_SHORT);
//                    connectionStateTV.setText("wait connection");
                    Log("wait connection");
                    break;
                default:
                    break;
            }
        }
    };

    public CarControlService() {
        this.getBaseContext();
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled())
            wifiManager.setWifiEnabled(true);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        Log.i(TAG, "IpAddress:" + intToIP(wifiInfo.getIpAddress()));
        Thread controlThread = new ControlThread();
        controlThread.start();
    }

    //int转ip地址
    public String intToIP(int i) {
        return (i & 0xff) + "." + ((i >> 8) & 0xff) + "." + ((i >> 16) & 0xff) + "." + ((i >> 24) & 0xff);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log("onBind");
        activityHandler = (Handler) intent.getExtras().get(KEY_HANDLER);
        return mControlBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "service onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "service onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "service onDestroy");
    }

    class ControlThread extends Thread {
        public void run() {
            try {
                Message message = new Message();
                message.what = 2;
                handler.sendMessage(message);
                ServerSocket serverSocket = new ServerSocket(9876);
                Socket socket = serverSocket.accept();
                Message message1 = new Message();
                message1.what = 1;
                handler.sendMessage(message1);
                OutputStream outputStream = socket.getOutputStream();
                while (true) {
                    if (upKey) {
                        upKey = false;
                        Log("up");
                        outputStream.write(0x01);
                        //outputStream.flush();
                    } else if (downKey) {
                        downKey = false;
                        Log("down");
                        outputStream.write(0x02);
                        //outputStream.flush();
                    } else if (leftKey) {
                        leftKey = false;
                        Log("left");
                        outputStream.write(0x04);
//                        outputStream.flush();
                    } else if (rightKey) {
                        rightKey = false;
                        Log("right");
                        outputStream.write(0x08);
//                        outputStream.flush();
                    } else if (stopKey) {
                        stopKey = false;
                        Log("stop");
                        outputStream.write(0x10);
                        //outputStream.flush();
                    } else if (!socket.isConnected()) {
                        System.out.println("Socket is Disconnected");
                        Message message2 = new Message();
                        message2.what = 0;
                        handler.sendMessage(message2);
                        connectFlag = false;
                        if (outputStream != null)
                            outputStream.close();
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    class ControlBinder extends Binder {
        public void move(int key, int time) {
            if (connectFlag) {
                switch (key) {
                    case UP:
                        upKey = true;
                        handler.postDelayed(stopRunnable, time);
                        break;
                    case DOWN:
                        downKey = true;
                        handler.postDelayed(stopRunnable, time);
                        break;
                    case LEFT:
                        leftKey = true;
                        handler.postDelayed(stopRunnable, time);
                        break;
                    case RIGHT:
                        rightKey = true;
                        handler.postDelayed(stopRunnable, time);
                        break;
                    case STOP:
                        stopKey = true;
                        break;
                }
            } else {
                Log(CAR_DISCONNE);
            }
        }
    }

    Runnable stopRunnable = new Runnable() {
        @Override
        public void run() {
            stopKey = true;
        }
    };
    Message toastMessage = new Message();
    private void Log(String str) {
        //Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
        //Log.e(TAG,str);
        toastMessage.what =SEND_TOAST;
        toastMessage.obj = str;
        activityHandler.sendMessage(toastMessage);
    }
}
