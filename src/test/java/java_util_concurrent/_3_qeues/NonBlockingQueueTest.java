package java_util_concurrent._3_qeues;

import org.junit.Test;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class NonBlockingQueueTest {

    ExecutorService pool = Executors.newCachedThreadPool();

    @Test
    public void test() throws ExecutionException, InterruptedException {
        ConcurrentLinkedQueue<String> nonBlockingQueue = new ConcurrentLinkedQueue<>();

        Future<?> future = pool.submit(() -> {
            while (true) {
                nonBlockingQueue.add("Value");
                System.out.println("Add ");
            }
        });

        Future<?> future2 = pool.submit(() -> {
            while (true) {
                nonBlockingQueue.poll();
                System.out.println("Poll ");
            }
        });

        future.get();
        future2.get();
    }
}
