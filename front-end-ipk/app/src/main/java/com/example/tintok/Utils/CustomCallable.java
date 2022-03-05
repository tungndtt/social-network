package com.example.tintok.Utils;

import java.util.concurrent.Callable;

public interface CustomCallable<T> extends Callable<T> {
    void afterWorkDone(T result);
    void beforeWork();
}
