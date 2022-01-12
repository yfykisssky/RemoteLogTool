package com.tools.remotelog.internel;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ThreadUtil {

    private static final Executor sExecutor= Executors.newCachedThreadPool();

    public static Executor getExecutor(){
        return sExecutor;
    }
}
