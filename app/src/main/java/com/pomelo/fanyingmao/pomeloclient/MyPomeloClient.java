package com.pomelo.fanyingmao.pomeloclient;


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

public class MyPomeloClient {
    private PomeloClient client;
    private String host;
    private Integer port;

    public MyPomeloClient(String host, Integer port) {
        this.host = host;
        this.port = port;
    }

    OnErrorHandler onErrorHandler = new OnErrorHandler() {
        @Override
        public void onError(Exception e) {
            //To change body of implemented methods use File | Settings | File Templates.
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    };

    public void enterConnect(final String token, final String clientType, final OnDataHandler onDataHandler) {
        try {
            client = new PomeloClient(new URI("ws://" + this.host + ":" + port));

            client.setOnHandshakeSuccessHandler(new OnHandshakeSuccessHandler() {
                @Override
                public void onSuccess(PomeloClient pomeloClient, JSONObject jsonObject) {
//                          client.close();
                    try {
                        JSONObject json = new JSONObject();
                        json.put("token", token);
                        json.put("timestamp", new Date().getTime());
                        client.request("gate.gateHandler.queryEntry", json.toString(), new OnDataHandler() {
                            @Override
                            public void onData(PomeloMessage.Message message) {
                                try {
                                    JSONObject bodyJson = message.getBodyJson();
                                    if (bodyJson.getInt("code") != 200) {
                                        System.out.println(bodyJson.toString());
                                        onDataHandler.onData(null);
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
                                                connectorJson.put("client", clientType);
                                                client.request("connector.entryHandler.enter", connectorJson.toString(), onDataHandler);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            } catch (PomeloException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                    client.setOnErrorHandler(onErrorHandler);
                                    client.connect();
                                } catch (JSONException e) {
                                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                    onDataHandler.onData(null);
                                } catch (URISyntaxException e) {
                                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                    onDataHandler.onData(null);
                                }
                            }
                        });
                    } catch (PomeloException e) {
                        e.printStackTrace();
                        System.out.println(e.getMessage());
                        onDataHandler.onData(null);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        System.out.println(e.getMessage());
                        onDataHandler.onData(null);
                    }
                }
            });
            client.setOnErrorHandler(onErrorHandler);
            client.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void send(String target, Integer type, String content, String from_name, OnDataHandler onDataHandler) {
        JSONObject json = new JSONObject();
        try {
            json.put("type", type);
            json.put("target", target);
            json.put("content", content);
            json.put("from_name'", from_name);
            if (client == null || !client.isConnected()) {
                System.out.println("未连接");
                return;
            }
            client.request("chat.chatHandler.send", json.toString(), onDataHandler);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (PomeloException e) {
            e.printStackTrace();
        }
    }

    public void on(String route, OnDataHandler onDataHandler) {
        client.on(route,onDataHandler);
    }

    public void close() {
        client.close();
    }
}
