package com.epam.multithreading;

import java.util.concurrent.*;

public class CompletableFutureExp {

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        ExecutorService pool = Executors.newCachedThreadPool();

        for(int i = 0; i < 10; i++) {
            Future<String> future1 = pool.submit(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    return "0";
                }
            });
            System.out.println(future1.get());

            Future<String> future2 = pool.submit(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    return "1";
                }
            });
            System.out.println(future2.get());
        }

        pool.shutdown();

        System.out.println("-------------------------------------");








    }
}
