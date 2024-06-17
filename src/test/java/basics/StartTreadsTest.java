package basics;

import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StartTreadsTest {

    ExecutorService executorService = Executors.newCachedThreadPool();

    @Test
    public void runThread() {
        System.out.println(Thread.currentThread().getName());
        MyThread myThread = new MyThread();
        myThread.start();
        executorService.submit(myThread);
    }

    @Test
    public void willNotStartNewThreadIfCallRun() {
        System.out.println(Thread.currentThread().getName());
        MyThread myThread = new MyThread();
        myThread.run();
    }

    @Test
    public void runRunnable() {
        System.out.println(Thread.currentThread().getName());
        MyRunnable myRunnable = new MyRunnable();
        //myRunnable.start(); do NOT have start method
        Thread thread = new Thread(myRunnable);
        thread.start();
        executorService.submit(myRunnable);

    }

    public class MyRunnable implements Runnable {
        @Override
        public void run() {
            System.out.println(Thread.currentThread().getName());
        }
    }

    public class MyThread extends Thread {

        @Override
        public void run() {
            System.out.println(Thread.currentThread().getName());
        }
    }
}
