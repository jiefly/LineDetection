package com.gao.jiefly.linedetection;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.Toast;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class Main2Activity extends AppCompatActivity {


    @Bind(R.id.tv_but)
    Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        ButterKnife.bind(this);
    }


    @OnClick(R.id.tv_but)
    public void onClick() {
        Toast.makeText(this,"hello world",Toast.LENGTH_SHORT).show();
    }
}
