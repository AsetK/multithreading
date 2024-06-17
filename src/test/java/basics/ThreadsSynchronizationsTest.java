package basics;

import org.junit.Test;

public class ThreadsSynchronizationsTest {

    @Test
    public void join() throws InterruptedException {

        Thread t1 = new Thread(() -> {
            System.out.println("Tread 1");
            try {
                Thread.sleep(5000); // имитация работы
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        Thread t2 = new Thread(() -> {
            try {
                t1.join(); //Ждет первый поток
                System.out.println("Tread 2");
                Thread.sleep(5000); // имитация работы
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        t1.start();
        t2.start();

        t1.join(); //Ждет первый поток
        t2.join(); //Ждет второй поток
        System.out.println("Thread main");
    }

    @Test
    public void synchronizedBlock() throws InterruptedException {
        CounterService counterService = new CounterService();

        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 10000; i++) {
                synchronized (counterService) {
                    counterService.counter++;
                }
            }
        });

        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 10000; i++) {
                synchronized (counterService) {
                    counterService.counter++;
                }
            }
        });

        t1.start();
        t2.start();

        t1.join();
        t2.join();
        System.out.println(counterService.counter);
    }

    @Test
    public void synchronizedMethod() throws InterruptedException {
        SynchronizedCounterService synchronizedCounterService = new SynchronizedCounterService();

        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 10000; i++) {
                synchronizedCounterService.increment();
            }
        });

        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 10000; i++) {
                synchronizedCounterService.increment();
            }
        });

        t1.start();
        t2.start();

        t1.join();
        t2.join();
        System.out.println(synchronizedCounterService.getCounter());
    }

    @Test
    public void waitNotify() throws InterruptedException {
        /*
        Синхронизация по событию выполняется с помощью методов wait(), notify().
        wait() – дает потоку указание ждать. Есть перегруженный вариант с таймаутом в параметре, т.е. сам проснется когда время придет.
        notify() – будит.
        Данные методы должны вызываться в синхронизационном блоке.

        Минус данного способа: notify() может запуститься раньше wait(), тогда 2 поток не запуститься. Программа не закончиться, зависнет. Посмотри второй спосоь
        Если несколько потоков будут ждать один поток, то метод notify() разбудит случайный поток. Что бы разбудить все потоки нужно вызвать метод notifyAll().
        */

        Object sync = new Object();
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 100; i++) {
                System.out.println("Thread 1: " + i);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (i == 50) {
                    synchronized (sync) {
                        sync.notify();
                    }
                }
            }
        });

        Thread t2 = new Thread(() -> {
            synchronized (sync) {
                try {
                    sync.wait();
                    System.out.println("Tread 2");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        t1.start();
        t2.start();

        t1.join();
        t2.join();
    }

    @Test
    public void waitNotify2() throws InterruptedException {
        // условие когда будется спящий поток в самом спящем потоке.
        CounterService counterService = new CounterService();

        Object sync = new Object();
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 100; i++) {
                System.out.println("Thread 1: " + i);

                synchronized (sync) {
                    sync.notify();
                    counterService.counter++;
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        Thread t2 = new Thread(() -> {
            synchronized (sync) {
                try {
                    while (counterService.counter < 50) {
                        sync.wait();
                    }
                    System.out.println("Tread 2");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        t1.start();
        t2.start();

        t1.join();
        t2.join();
    }


    public class CounterService {
        public volatile int counter = 0;
    }

    public class SynchronizedCounterService {
        private volatile int counter = 0;

        public synchronized void increment() {
            counter++;
        }

        public int getCounter() {
            return counter;
        }

    }
}
