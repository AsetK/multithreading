package com.epam.multithreading;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class CallableTask implements Callable {

    static AtomicInteger i = new AtomicInteger(0);
    static AtomicInteger sleepTime = new AtomicInteger(500);


    @Override
    public Object call() throws Exception {
        Thread.currentThread().sleep(sleepTime.getAndAdd(500));
        return i.incrementAndGet();
    }
}
