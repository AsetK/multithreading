package java_util_concurrent._7_synchronizers;

import org.junit.Test;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Exchanger;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

public class Synchronizers {

    ExecutorService pool = Executors.newCachedThreadPool();
    private Semaphore semaphore = new Semaphore(3);
    private CountDownLatch countDownLatch = new CountDownLatch(3);
    private CyclicBarrier cyclicBarrier = new CyclicBarrier(3);
    private Exchanger<String> exchanger = new Exchanger();

    @Test
    public void semaphore() throws InterruptedException {
        // Нужен когда нужно ограничть доступ к общему ресурсу
        for (int i = 0; i < 10; i++) {
            Future<?> submit = pool.submit(() -> {
                System.out.println("Entered into the Thread");

                try {
                    semaphore.acquire();
                    Thread.sleep(2000); //imitating work
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Doing work");
                semaphore.release();

                System.out.println("Exiting from the Thread");
            });
        }

        Thread.sleep(10000);
    }

    @Test
    public void countDownLatch() throws InterruptedException, ExecutionException {
        // предоставляет возможность любому количеству потоков в блоке кода ожидать до тех пор, пока не завершится определенное количество операций,
        // выполняющихся в других потоках, перед тем как они будут «отпущены», чтобы продолжить свою деятельность.
        // countDownLatch - одноразовый, если барьер сломался 1 раз, то он уже не будет "копить" потоки, а будет сразу пропускать.

        for (int i = 0; i < 3; i++) {
            Future<?> future1 = pool.submit(() -> {
                System.out.println("Entered into the Thread");

                try {
                    countDownLatch.countDown(); // я готов
                    countDownLatch.await();     // жду пока все не будут готовы
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Doing work");
                semaphore.release();

                System.out.println("Exiting from the Thread");
            });

            Thread.sleep(2000); // имитация ожидания следубщих потоков
        }


    }

    @Test
    public void cyclicBarrier() throws ExecutionException, InterruptedException {
        // Работает как CountDownLatch, но после обрыва можно использовать барьер циклично.
        for (int i = 0; i < 9; i++) {
            Future<?> future1 = pool.submit(() -> {
                System.out.println("Entered into the Thread");

                try {
                    cyclicBarrier.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }
                System.out.println("Doing work");
                semaphore.release();

                System.out.println("Exiting from the Thread");
            });

            Thread.sleep(2000); // имитация ожидания следубщих потоков
        }

    }

    @Test
    public void exchanger() throws ExecutionException, InterruptedException {
        /*
        позволяет двум потокам обмениваться данными.
        Каждый поток вызывает метод exchange(), чтобы передать данные другому потоку и получить данные от него.
        Этот метод блокирует выполнение до тех пор, пока другой поток также не вызовет exchange().
        После этого оба потока обменяются данными и продолжат выполнение.
        */

        Future<?> future1 = pool.submit(() -> {
            try {
                Thread.sleep(5000);
                String result = exchanger.exchange("From A");
                System.out.println("Result in A: " + result);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        Future<?> future2 = pool.submit(() -> {
            try {
                Thread.sleep(2000);
                String result = exchanger.exchange("From B");
                System.out.println("Result in B: " + result);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        future1.get();
        future2.get();
    }
}
