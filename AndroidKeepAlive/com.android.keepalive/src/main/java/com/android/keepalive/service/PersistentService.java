package com.android.keepalive.service;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.android.keepalive.KeepAliveActivity;
import com.android.keepalive.KeepAliveApplication;

public abstract class PersistentService extends Service {
    private ScreenChangeReceiver mReceiver;

    public IBinder onBind(Intent intent) {
        return null;
    }

    public abstract Class getServiceClass();

    public void onCreate() {
        super.onCreate();
        Log.v("PushService", "onCreate");
        mReceiver = new ScreenChangeReceiver();
        IntentFilter intentFilter = new IntentFilter("android.intent.action.SCREEN_ON");
        intentFilter.addAction("android.intent.action.SCREEN_OFF");

        this.registerReceiver(mReceiver, intentFilter);
        keepAlive();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @Override
    public void onDestroy() {
        Log.v("PersistentService", "onDestroy");
        if (mReceiver != null) {
            this.unregisterReceiver(mReceiver);
        }
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            /**被关闭时再启动服务，确保不被杀死*/
            @Override
            public void run() {
                try {
                    startService(new Intent(PersistentService.this, getServiceClass()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 100);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        flags = START_STICKY;
        return super.onStartCommand(intent, flags, startId);
    }

    private void keepAlive() {
        try {
            Notification notification = new Notification();
            notification.flags |= Notification.FLAG_NO_CLEAR;
            notification.flags |= Notification.FLAG_ONGOING_EVENT;
            startForeground(0, notification); // 设置为前台服务避免kill，Android4.3及以上需要设置id为0时通知栏才不显示该通知；
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    class ScreenChangeReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            Log.e("ScreenChangeReceiver", "onReceive");
            String action = intent.getAction();
            if (action.equals("android.intent.action.SCREEN_ON")) {// 屏幕点亮
                finishKeepLiveActivity();
            } else if (action.equals("android.intent.action.SCREEN_OFF")) {
                startKeepLiveActivity();
            }
        }
    }

    private void startKeepLiveActivity() {
        Intent keepLive = new Intent(KeepAliveApplication.getApp(), KeepAliveActivity.class);
        keepLive.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        KeepAliveApplication.getApp().startActivity(keepLive);
    }

    private void finishKeepLiveActivity() {
        if (KeepAliveActivity.mInstance != null) {
            KeepAliveActivity.mInstance.finish();
        }
    }
}
