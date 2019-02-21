package com.zyc.zcontrol;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MQTTService extends Service
        implements MqttCallback {

    public final static String ACTION_MQTT_CONNECTED =
            "com.zyc.zcontrol.mqtt.ACTION_MQTT_CONNECTED";
    public final static String ACTION_MQTT_DISCONNECTED =
            "com.zyc.zcontrol.mqtt.ACTION_MQTT_DISCONNECTED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.zyc.zcontrol.mqtt.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA_TOPIC =
            "com.zyc.zcontrol.mqtt.EXTRA_DATA_TOPIC";
    public final static String EXTRA_DATA_CONTENT =
            "com.zyc.zcontrol.mqtt.EXTRA_DATA_CONTENT";


    //region 广播相关定义
    private LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
    //endregion

    MqttClient mqttClient = null;


    public MQTTService() {
    }

/*
    //region 线程函数,mqtt相关功能在此实现
    Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {
            MqttClient mqttClient = null;
            Log.d("MQTTThread", "start");
            while (flag) {


                Log.d("MQTTThread", "url:" + mqtt_uri + ",id:" + mqtt_id
                        + "user,pwd:" + mqtt_user + "," + mqtt_password);


                //消息缓存方式，内存缓存
                MemoryPersistence persistence = new MemoryPersistence();
                try {
                    if (mqttClient != null && mqttClient.isConnected()) {
                        mqttClient.disconnect();
                        mqttClient = null;
                    }

                    //region 建立客户端
                    mqttClient = new MqttClient(mqtt_uri, mqtt_id, persistence);
                    //连接的配置参数
                    MqttConnectOptions connectOptions = new MqttConnectOptions();
                    connectOptions.setCleanSession(true);  //不记忆上次会话
                    connectOptions.setUserName(mqtt_user); //用户名
                    connectOptions.setPassword(mqtt_password.toCharArray()); //密码
                    connectOptions.setConnectionTimeout(30);  //超时时间
                    connectOptions.setKeepAliveInterval(60); //心跳时间,单位秒
                    connectOptions.setAutomaticReconnect(true);//自动重连
                    //setWill方法，如果项目中需要知道客户端是否掉线可以调用该方法。设置最终端口的通知消息
                    //            connectOptions.setWill(topic, "close".getBytes(), 2, true);
                    Log.d("MQTTThread", "connecting to broker");
                    //endregion


                    mqttClient.setCallback(new MqttCallback() {
                        @Override
                        public void connectionLost(Throwable cause) {
                            Log.d("MQTTThread", "connectionLost");

                        }

                        @Override
                        public void messageArrived(String topic, MqttMessage message) throws Exception {
                            Log.d("MQTTThread", "topic:" + topic);
                            Log.d("MQTTThread", "Qos:" + message.getQos());
                            Log.d("MQTTThread", "message content:" + new String(message.getPayload()));

                            //region 广播测试
                            Intent intent = new Intent("com.zyc.zcontrol.MQTTRECEIVER");
                            intent.putExtra("string", new String(message.getPayload()));
                            localBroadcastManager.sendBroadcast(intent);
                            //endregion

                        }

                        @Override
                        public void deliveryComplete(IMqttDeliveryToken token) {
                            Log.d("MQTTThread", "deliveryComplete");
                        }
                    });
                    //连接服务器
                    mqttClient.connect(connectOptions);

                    //订阅消息
                    mqttClient.subscribe("/test/androidGet", 0);

                    while (flag) {
                        while (mqtt_send_topic != null &&
                                mqtt_send_string != null && flag) {
                            MqttMessage message = new MqttMessage(mqtt_send_string.getBytes());
                            //设定消息发送等级
                            message.setQos(qos);
                            //发布消息
                            mqttClient.publish(mqtt_send_topic, message);
                            mqtt_send_topic = null;
                            mqtt_send_string = null;
                        }
                    }


                    // System.exit (0);//关闭UI进程
                } catch (MqttException e) {
                    Log.e("MQTTThread", "reason " + e.getReasonCode());
                    Log.e("MQTTThread", "msg " + e.getMessage());
                    Log.e("MQTTThread", "loc " + e.getLocalizedMessage());
                    Log.e("MQTTThread", "cause " + e.getCause());
                    Log.e("MQTTThread", "excep " + e);
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException ee) {
                    }
                    e.printStackTrace();
                }

            }
            //断开连接
            if (mqttClient != null) {
                try {
                    mqttClient.disconnect();
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
            Log.d("MQTTThread", "end");

        }
    });*/
    //endregion

    //region Service相关配置
    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public MQTTService getService() {
            return MQTTService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    //endregion
    @Override
    public void onCreate() {
        Log.d("MQTTService", "OnCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("MQTTService", "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
        Log.d("MQTTService", "onDestroy");
    }


    void broadcastUpdate(String action) {
        final Intent intent = new Intent(action);
        localBroadcastManager.sendBroadcast(intent);
    }

    void broadcastUpdate(String action, String topic, MqttMessage message) {
        final Intent intent = new Intent(action);
        intent.putExtra(EXTRA_DATA_TOPIC, topic);
        intent.putExtra(EXTRA_DATA_CONTENT, new String(message.getPayload()));
        localBroadcastManager.sendBroadcast(intent);
    }

    //region MQTT回调
    @Override
    public void connectionLost(Throwable cause) {
        Log.d("MQTTThread", "connectionLost");
        broadcastUpdate(ACTION_MQTT_DISCONNECTED);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        Log.d("MQTTThread", "deliveryComplete");
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
//        Log.d("MQTTThread", "topic:" + topic);
//        Log.d("MQTTThread", "Qos:" + message.getQos());
//        Log.d("MQTTThread", "message content:" + new String(message.getPayload()));
        broadcastUpdate(ACTION_DATA_AVAILABLE, topic, message);
    }

    //endregion


    public void connect(String mqtt_uri, String mqtt_id,
                        String mqtt_user, String mqtt_password) {

        //消息缓存方式，内存缓存
        MemoryPersistence persistence = new MemoryPersistence();
        try {
            //region 建立客户端
            if (mqttClient == null)
                mqttClient = new MqttClient(mqtt_uri, mqtt_id, persistence);
            //连接的配置参数
            MqttConnectOptions connectOptions = new MqttConnectOptions();
            connectOptions.setCleanSession(true);  //不记忆上次会话
            if (mqtt_user != null)
                connectOptions.setUserName(mqtt_user); //用户名
            if (mqtt_password != null)
                connectOptions.setPassword(mqtt_password.toCharArray()); //密码
            connectOptions.setConnectionTimeout(30);  //超时时间
            connectOptions.setKeepAliveInterval(60); //心跳时间,单位秒
            connectOptions.setAutomaticReconnect(true);//自动重连
            //setWill方法，如果项目中需要知道客户端是否掉线可以调用该方法。设置最终端口的通知消息
            //            connectOptions.setWill(topic, "close".getBytes(), 2, true);
            Log.d("MQTTThread", "connecting to broker");
            //endregion

            mqttClient.setCallback(this);

            //连接服务器
            mqttClient.connect(connectOptions);

            //订阅消息
            mqttClient.subscribe("/test/android", 0);

            broadcastUpdate(ACTION_MQTT_CONNECTED); //连接成功
        } catch (MqttException e) {
            Log.e("MQTTService", "reason " + e.getReasonCode());
            Log.e("MQTTService", "msg " + e.getMessage());
            Log.e("MQTTService", "loc " + e.getLocalizedMessage());
            Log.e("MQTTService", "cause " + e.getCause());
            Log.e("MQTTService", "excep " + e);
            e.printStackTrace();
            broadcastUpdate(ACTION_MQTT_DISCONNECTED); //连接失败
        }
    }

    public boolean isConnected() {
        return mqttClient.isConnected();
    }

    public void disconnect() {
        try {
            mqttClient.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void reconnect() {
        try {
            mqttClient.reconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    //region MQTT发送函数
    public void Send(String topic, String str) {
        Send(topic, str, 0);
    }

    public void Send(String topic, String str, int qos) {
        //region 发送

        try {
            MqttMessage message = new MqttMessage(str.getBytes());
            message.setQos(qos);
            mqttClient.publish(topic, message);//发布消息
        } catch (MqttException e) {
            e.printStackTrace();
        }
        //endregion

    }
    //endregion
}
