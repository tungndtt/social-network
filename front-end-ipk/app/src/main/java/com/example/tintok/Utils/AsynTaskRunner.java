package com.example.tintok.Utils;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class AsynTaskRunner {
    ExecutorService executors ;
    Handler uiThread;

    public AsynTaskRunner(){
        executors = Executors.newCachedThreadPool();
        uiThread = new Handler(Looper.getMainLooper());
    }

    public<T> void executeTask(CustomCallable<T> callable){
        callable.beforeWork();
        Future f = executors.submit(() -> {
            try {
                T result = callable.call();
                uiThread.post(() -> callable.afterWorkDone(result));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

}
