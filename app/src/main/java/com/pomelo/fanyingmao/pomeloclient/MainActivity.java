package com.pomelo.fanyingmao.pomeloclient;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

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


public class MainActivity extends AppCompatActivity {


    boolean flag = false;

    OnHandshakeSuccessHandler onConnectorHandshakeSuccessHandler = new OnHandshakeSuccessHandler() {
        @Override
        public void onSuccess(PomeloClient client, JSONObject jsonObject) {
            try {
                JSONObject connectorJson = new JSONObject();
                String username = "222";
                String rid = "a";
                connectorJson.put("username", username);
                connectorJson.put("rid", rid);
                client.request("connector.entryHandler.enter", connectorJson.toString(), new OnDataHandler() {

                    @Override
                    public void onData(PomeloMessage.Message message) {
                        System.out.println(message.toString());
                        flag = true;
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
                flag = true;
            } catch (PomeloException e) {
                e.printStackTrace();
                flag = true;
            }
        }
    };
    OnErrorHandler onErrorHandler = new OnErrorHandler() {
        @Override
        public void onError(Exception e) {
            //To change body of implemented methods use File | Settings | File Templates.
            e.printStackTrace();
            flag = true;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final PomeloClient client;
        try {
            client = new PomeloClient(new URI("ws://192.168.1.235:3014"));

//            List<Runnable> runs = new ArrayList<Runnable>();
//            runs.add(client);
//            PomeloClientTest.assertConcurrent("test websocket client", runs, 200);
            OnHandshakeSuccessHandler onHandshakeSuccessHandler = new OnHandshakeSuccessHandler() {
                @Override
                public void onSuccess(PomeloClient _client, JSONObject resp) {
                    try {
                        JSONObject json = new JSONObject();
                        json.put("uid", 1);
                        client.request("gate.gateHandler.queryEntry", json.toString(), new OnDataHandler() {
                            @Override
                            public void onData(PomeloMessage.Message message) {
                                try {
                                    JSONObject bodyJson = message.getBodyJson();
                                    String host = bodyJson.getString(PomeloClient.HANDSHAKE_RES_HOST_KEY);
                                    String port = bodyJson.getString(PomeloClient.HANDSHAKE_RES_PORT_KEY);
                                    client.close();
                                    PomeloClient connector = new PomeloClient(new URI("ws://" + host + ":" + port));
                                    connector.setOnHandshakeSuccessHandler(onConnectorHandshakeSuccessHandler);
                                    connector.setOnErrorHandler(onErrorHandler);
                                    connector.connect();
                                } catch (JSONException e) {
                                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                    flag = true;
                                } catch (URISyntaxException e) {
                                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                    flag = true;
                                }
                            }
                        });
                    } catch (PomeloException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        flag = true;
                    } catch (JSONException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        flag = true;
                    }
                }
            };
            client.setOnHandshakeSuccessHandler(onHandshakeSuccessHandler);
            client.setOnErrorHandler(onErrorHandler);
            client.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
