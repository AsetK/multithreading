package java_util_concurrent._1_atomics;

import org.junit.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class AtomicTest {

    private AtomicInteger atomicInteger = new AtomicInteger();
    private ExecutorService pool = Executors.newCachedThreadPool();

    @Test
    public void atomicInteger() throws ExecutionException, InterruptedException {
        Future<?> future1 = pool.submit(() -> {
            for (int i = 0; i < 100000; i++) {
                atomicInteger.incrementAndGet();
            }
        });

        Future<?> future2 = pool.submit(() -> {
            for (int i = 0; i < 100000; i++) {
                atomicInteger.incrementAndGet();
            }
        });

        future1.get();
        future2.get();

        System.out.println(atomicInteger.get());
    }
}
