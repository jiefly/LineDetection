package com.gao.jiefly.linedetection.View;

import android.app.Activity;
import android.graphics.Camera;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.gao.jiefly.linedetection.R;
import com.gao.jiefly.linedetection.model.OnDetechListener;

/**
 * Created by jiefly on 2016/4/13.
 * Fighting_jiiiiie
 */
public class Start extends Activity implements OnDetechListener, SurfaceHolder.Callback {
    private Camera mCamera;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
    }

    @Override
    public void onSuccess() {

    }

    @Override
    public void onError() {

    }
    // InitSurfaceView
    private void initSurfaceView()
    {
        mSurfaceView = (SurfaceView) this.findViewById(R.id.surface_view);
        mSurfaceHolder = mSurfaceView.getHolder(); // 绑定SurfaceView，取得SurfaceHolder对象
        mSurfaceHolder.addCallback(Start.this); // SurfaceHolder加入回调接口
        // mSurfaceHolder.setFixedSize(176, 144); // 预览大小設置
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);// 設置顯示器類型，setType必须设置
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
