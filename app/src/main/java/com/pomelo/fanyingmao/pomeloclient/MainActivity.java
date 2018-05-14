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


    private PomeloClient client;
    OnErrorHandler onErrorHandler = new OnErrorHandler() {
        @Override
        public void onError(Exception e) {
            //To change body of implemented methods use File | Settings | File Templates.
            e.printStackTrace();
            Looper.prepare();
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            Looper.loop();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.tv_connect).setOnClickListener(this);
        findViewById(R.id.tv_send).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_connect:
                try {
                    String ip = "192.168.1.215";
                    Integer port = 3014;
                    if (client != null && client.isConnected()) {
                        client.close();
                    }
                    client = new PomeloClient(new URI("ws://" + ip + ":" + port));
                    client.setOnHandshakeSuccessHandler(new OnHandshakeSuccessHandler() {
                        @Override
                        public void onSuccess(PomeloClient pomeloClient, JSONObject jsonObject) {
//                          client.close();
                            try {
                                JSONObject json = new JSONObject();
                                json.put("token", "test552");
                                json.put("timestamp", new Date().getTime());
                                client.request("gate.gateHandler.queryEntry", json.toString(), new OnDataHandler() {
                                    @Override
                                    public void onData(PomeloMessage.Message message) {
                                        try {
                                            JSONObject bodyJson = message.getBodyJson();
                                            if (bodyJson.getInt("code") != 200) {
                                                Looper.prepare();
                                                Toast.makeText(MainActivity.this, bodyJson.toString(), Toast.LENGTH_SHORT).show();
                                                Looper.loop();
                                                return;
                                            }
                                            String host = bodyJson.getString("host");
                                            Integer port = bodyJson.getInt("port");
                                            final String init_token = bodyJson.getString("init_token");
                                            client.close();
                                            client = new PomeloClient(new URI("ws://" + host + ":" + port));
                                            client.setOnHandshakeSuccessHandler(new OnHandshakeSuccessHandler() {
                                                @Override
                                                public void onSuccess(PomeloClient pomeloClient, JSONObject jsonObject) {
                                                    try {
                                                        JSONObject connectorJson = new JSONObject();
                                                        connectorJson.put("init_token", init_token);
                                                        connectorJson.put("client", "android");
                                                        client.request("connector.entryHandler.enter", connectorJson.toString(), new OnDataHandler() {

                                                            @Override
                                                            public void onData(PomeloMessage.Message message) {
                                                                System.out.println(message.toString());
                                                                JSONObject bodyJson = message.getBodyJson();
                                                                try {
                                                                    Looper.prepare();
                                                                    if (bodyJson.getInt("code") != 200) {
                                                                        Toast.makeText(MainActivity.this, bodyJson.toString(), Toast.LENGTH_SHORT).show();
                                                                    } else {
                                                                        Toast.makeText(MainActivity.this, "连接认证成功", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                    Looper.loop();

                                                                } catch (JSONException e) {
                                                                    e.printStackTrace();
                                                                }

                                                            }
                                                        });
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }
                                                    catch (PomeloException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            });
                                            client.setOnErrorHandler(onErrorHandler);
                                            client.connect();
                                        } catch (JSONException e) {
                                            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                        } catch (URISyntaxException e) {
                                            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                        }
                                    }
                                });
                            } catch (PomeloException e) {
                                e.printStackTrace();
                                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    client.setOnErrorHandler(onErrorHandler);
                    client.connect();
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.tv_send:
                send();
                break;
        }
    }

    private void send(){
        JSONObject json = new JSONObject();
        try {
            json.put("type", 0);
            json.put("target", "41");
            json.put("content", "hello");
            json.put("from_name'", "41");
            if (client == null || !client.isConnected()) {
                Toast.makeText(MainActivity.this, "未连接", Toast.LENGTH_SHORT).show();
                return;
            }
            client.request("chat.chatHandler.send", json.toString(), new OnDataHandler() {
                @Override
                public void onData(PomeloMessage.Message message) {
                    JSONObject bodyJson = message.getBodyJson();
                    try {
                        Looper.prepare();
                        if (bodyJson.getInt("code") != 200) {
                            Toast.makeText(MainActivity.this, bodyJson.toString(), Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(MainActivity.this, "发送成功", Toast.LENGTH_SHORT).show();
                        }
                        Looper.loop();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (PomeloException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (client != null && client.isConnected()) {
            client.close();
        }
    }
}
