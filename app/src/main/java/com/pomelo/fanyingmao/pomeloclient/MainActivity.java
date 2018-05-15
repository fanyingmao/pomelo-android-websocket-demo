package com.pomelo.fanyingmao.pomeloclient;

import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.zvidia.pomelo.exception.PomeloException;
import com.zvidia.pomelo.protocol.PomeloMessage;
import com.zvidia.pomelo.websocket.OnDataHandler;
import com.zvidia.pomelo.websocket.OnErrorHandler;
import com.zvidia.pomelo.websocket.OnHandshakeSuccessHandler;
import com.zvidia.pomelo.websocket.PomeloClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private MyPomeloClient mMyPomeloClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.tv_connect).setOnClickListener(this);
        findViewById(R.id.tv_send).setOnClickListener(this);
        mMyPomeloClient = new MyPomeloClient("192.168.1.215", 3014);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_connect:
                mMyPomeloClient.enterConnect("test552", "android",new OnDataHandler() {
                    @Override
                    public void onData(PomeloMessage.Message message) {
                        if(message == null){
                            System.out.println("连接认证失败");
                            return;
                        }
                        System.out.println(message.toString());
                        JSONObject bodyJson = message.getBodyJson();
                        try {
                            if (bodyJson.getInt("code") != 200) {
                                System.out.println(bodyJson.toString());
                            } else {
                                System.out.println("连接认证成功");
                                onPush();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                });
                break;
            case R.id.tv_send:
                mMyPomeloClient.send("41", 0, "hello", "fym", new OnDataHandler() {
                    @Override
                    public void onData(PomeloMessage.Message message) {
                        JSONObject bodyJson = message.getBodyJson();
                        try {
                            if (bodyJson.getInt("code") != 200) {
                                System.out.println(bodyJson.toString());
                            } else {
                                System.out.println("发送成功");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
                break;
        }
    }

    private void onPush(){
        mMyPomeloClient.on("onChat", new OnDataHandler() {
            @Override
            public void onData(PomeloMessage.Message message) {
                JSONObject bodyJson = message.getBodyJson();
                System.out.println("onChat:" + bodyJson.toString());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


}
