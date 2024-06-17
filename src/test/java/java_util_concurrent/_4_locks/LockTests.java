package java_util_concurrent._4_locks;

import org.junit.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class LockTests {

    ExecutorService pool = Executors.newCachedThreadPool();

    private int counter = 0;

    @Test
    public void reentrantLock() throws ExecutionException, InterruptedException {
        // Позволяет синхронизировать код так же как и synchronized.
        // Но есть приимущество - мы може сделать lock() в одном методе, и unlock() в другом вложенном методе.
        ReentrantLock reentrantLock = new ReentrantLock();
        Future<?> future1 = pool.submit(() -> {
            for (int i = 0; i < 100000; i++) {
                reentrantLock.lock();
                counter++;
                reentrantLock.unlock();
            }
        });

        Future<?> future2 = pool.submit(() -> {
            for (int i = 0; i < 100000; i++) {
                reentrantLock.lock();
                counter++;
                reentrantLock.unlock();
            }
        });

        future1.get();
        future2.get();

        System.out.println(counter);
    }

    @Test
    public void reentrantLockAndCondition() throws ExecutionException, InterruptedException {
        // Данный способ, в отличии wait-notify позволяет по условию разбудить необходимый поток.
        ReentrantLock reentrantLock = new ReentrantLock();
        Condition condition1 = reentrantLock.newCondition();
        Condition condition2 = reentrantLock.newCondition();
        Future<?> future1 = pool.submit(() -> {
            for (int i = 0; i < 10; i++) {
                System.out.println("Running A");
                reentrantLock.lock();

                if (i == 5) {
                    condition1.signal();
                }
                if (i == 7) {
                    condition2.signal();
                }
                reentrantLock.unlock();

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {

                }
            }
        });

        Future<?> future2 = pool.submit(() -> {
            reentrantLock.lock();
            try {
                condition1.await();
            } catch (InterruptedException e) {

            }
            reentrantLock.unlock();
            System.out.println("Running B");

        });

        Future<?> future3 = pool.submit(() -> {
            reentrantLock.lock();
            try {
                condition2.await();
            } catch (InterruptedException e) {

            }
            reentrantLock.unlock();
            System.out.println("Running C");

        });

        future1.get();
        future2.get();
        future3.get();
    }
}
