package com.epam.multithreading;

public class ThreadExp {
    public static void main(String[] args) throws InterruptedException {

        new Thread(()->{
            try {
                Thread.currentThread().sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Thread");
        }).start();


        System.out.println("Main");

    }
}
