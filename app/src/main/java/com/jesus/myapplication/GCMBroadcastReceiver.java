package com.jesus.myapplication;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.jesus.myapplication.servicios.GCMIntentService;

/**
 * Created by JesúsHumberto on 19/05/2015.
 */
public class GCMBroadcastReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        ComponentName comp = new ComponentName(context.getPackageName(), GCMIntentService.class.getName());
        startWakefulService(context,(intent.setComponent(comp)));
        setResultCode(MainActivity.RESULT_OK);
    }
}
