package com.epam.multithreading;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ThreadPoolExp {
    public static void main(String[] args) throws ExecutionException, InterruptedException {

        ExecutorService pool = Executors.newCachedThreadPool();
        List<Future<Integer>> futureList = new ArrayList<>();

        for(int i = 0; i < 10; i++)
            futureList.add(pool.submit(new CallableTask()));

        for(Future<Integer> future : futureList) {
            System.out.println("Main");
            System.out.println(future.get()); //получает данные если готовы, приостанавливает текущий(main) поток если данные еще не готовы
        }

        pool.shutdown();



    }
}
