package java_util_concurrent._5_executors;

import org.junit.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

//https://www.youtube.com/watch?v=6Oo-9Can3H8 про ExecutorService - посмотри
//https://www.youtube.com/watch?v=6Oo-9Can3H8&list=PLPn4T86dJstfydxMrepAcakR3MSZEHaa5 - весь плейлист
public class ExecutorServiceTests {

    @Test
    public void fixedThreadPool() {
        ExecutorService pool = Executors.newFixedThreadPool(3);
        for (int i = 0; i < 10; i++) {
            Future<?> submit = pool.submit(() -> System.out.println(Thread.currentThread().getName()));
        }

        assertEquals(((ThreadPoolExecutor) pool).getPoolSize(), 3);
        assertTrue(((ThreadPoolExecutor) pool).getQueue().size() != 0);
        System.out.println(((ThreadPoolExecutor) pool).getQueue().size());

        pool.shutdown();
    }

    @Test
    public void cachedThreadPool() {
        ExecutorService pool = Executors.newCachedThreadPool();
        for (int i = 0; i < 100; i++) {
            Future<?> submit = pool.submit(() -> System.out.println(Thread.currentThread().getName()));
        }
    }

    @Test
    public void singleThreadExecutor() {
        ExecutorService pool = Executors.newSingleThreadExecutor();
        for (int i = 0; i < 100; i++) {
            Future<?> submit = pool.submit(() -> System.out.println(Thread.currentThread().getName()));
        }

        pool.shutdown();
    }

    @Test
    public void scheduledThreadPool_schedule() throws InterruptedException, ExecutionException {
        // Выполняет задачу после задержки(delay) в 3 секунды
        ScheduledExecutorService pool = Executors.newScheduledThreadPool(3);

        ScheduledFuture<?> future = pool.schedule(() -> System.out.println(Thread.currentThread().getName()), 3, TimeUnit.SECONDS);
        future.get();

        pool.shutdown();
    }

    @Test
    public void scheduledThreadPool_scheduleAtFixedRate() throws InterruptedException, ExecutionException {
        // Выполняет задачу после задержки(delay) в 3 секунды, и 2 секунды периодичности между ЗАПУСКАМИ операции.
        // Выполняет бесконечное количество рах пока сами не прервем
        ScheduledExecutorService pool = Executors.newScheduledThreadPool(3);

        ScheduledFuture<?> future = pool.scheduleAtFixedRate(() -> System.out.println(Thread.currentThread().getName()), 3, 2, TimeUnit.SECONDS);
        future.get();

        pool.shutdown();
    }

    @Test
    public void scheduledThreadPool_scheduleWithFixedDelay() throws InterruptedException, ExecutionException {
        // Выполняет задачу после задержки(delay) в 3 секунды, и 2 секунды периодичности между ЗАВЕРШЕНИЕМ ПРЕДДЫДУЩЕЙ операции и ЗАПУСКОМ СЛЕДУЮЩЕЙ
        // Выполняет бесконечное количество рах пока сами не прервем
        ScheduledExecutorService pool = Executors.newScheduledThreadPool(3);

        ScheduledFuture<?> future = pool.scheduleWithFixedDelay(() -> System.out.println(Thread.currentThread().getName()), 3, 2, TimeUnit.SECONDS);
        future.get();

        pool.shutdown();
    }

    @Test
    public void scheduledThreadPool_scheduleWithFixedDelay_stop() throws InterruptedException, ExecutionException {
        // Завершает выполнение после n выполнений
        AtomicInteger atomicInteger = new AtomicInteger(0);

        ScheduledExecutorService pool = Executors.newScheduledThreadPool(3);

        ScheduledFuture<?> future = pool.scheduleWithFixedDelay(() -> {
            int executionsAmount = atomicInteger.incrementAndGet();
            System.out.println(Thread.currentThread().getName());
            if (executionsAmount >= 5) {
                pool.shutdown();
            }
        }, 3, 2, TimeUnit.SECONDS);

        future.get();
    }


}
