package com.gao.jiefly.linedetection;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class Main2Activity extends Activity {


    Button btn;
    Button btnUp;
    Button btnDown;
    Button btnLeft;
    Button btnRight;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
       btn = (Button) findViewById(R.id.btn_bind);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(this, "hello world", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                intent.setClass(Main2Activity.this,CarControlService.class);
                bindService(intent,mServiceConnection,BIND_AUTO_CREATE);
            }
        });
        btnUp = (Button) findViewById(R.id.btnUp);
        btnUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mControlBinder.move(0,1000);
            }
        });
        btnDown = (Button) findViewById(R.id.btnDown);
        btnLeft = (Button) findViewById(R.id.btnLeft);
        btnRight = (Button) findViewById(R.id.btnRight);
    }
    private CarControlService.ControlBinder mControlBinder;
private ServiceConnection mServiceConnection = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.i("jiefly","service connected");
        mControlBinder = (CarControlService.ControlBinder) service;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
    }
};

/*    @OnClick(R.id.tv_but)
    public void onClick()
    {

}*/

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        Log.e("jiefly","unbind service");
    }
}
