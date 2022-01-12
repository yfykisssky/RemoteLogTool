package com.tools.remotelog.remotelogcat;

import org.json.JSONObject;

public interface IMontitor extends Runnable{

    int getInterval();

    void start();

    void stop();

    interface OnNotifyObserver{
        void onChange(JSONObject jsonObject);
    }
}
