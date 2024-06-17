package java_util_concurrent._3_qeues;

import org.junit.Test;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class BlockingQueueTest {

    ExecutorService pool = Executors.newCachedThreadPool();


    @Test
    public void put() throws ExecutionException, InterruptedException {
        ArrayBlockingQueue blockingQueue = new ArrayBlockingQueue<String>(10);

        Future<Object> submit1 = pool.submit(() -> {
            while (true) {
                blockingQueue.put("Value"); //thread waits(blocked) if there is no space in queue
                System.out.println("Put");
            }
        });

        Future<Object> submit = pool.submit(() -> {
            while (true) {
                String take = (String) blockingQueue.take();
                System.out.println("Take");
                Thread.sleep(1000);
            }
        });

        submit1.get();
        submit.get();
    }

    @Test
    public void add() throws ExecutionException, InterruptedException {
        ArrayBlockingQueue blockingQueue = new ArrayBlockingQueue<String>(10);

        Future<Object> submit1 = pool.submit(() -> {
            while (true) {
                blockingQueue.add("Value"); //throws exception if there is no space in queue
                System.out.println("Put");
            }
        });

        Future<Object> submit = pool.submit(() -> {
            while (true) {
                String take = (String) blockingQueue.take();
                System.out.println("Take");
                Thread.sleep(1000);
            }
        });

        submit1.get();
        submit.get();
    }

    @Test
    public void offer() throws ExecutionException, InterruptedException {
        ArrayBlockingQueue blockingQueue = new ArrayBlockingQueue<String>(10);

        Future<Object> submit1 = pool.submit(() -> {
            while (true) {
                boolean b = blockingQueue.offer("Value"); //returns true if successfully added element, and returns false if no space in queue
                System.out.println("Put: " + b);
                Thread.sleep(500);
            }
        });

        Future<Object> submit = pool.submit(() -> {
            while (true) {
                String take = (String) blockingQueue.take();
                System.out.println("Take");
                Thread.sleep(1000);
            }
        });

        submit1.get();
        submit.get();
    }

    @Test
    public void offerWithTimeOut() throws ExecutionException, InterruptedException {
        ArrayBlockingQueue blockingQueue = new ArrayBlockingQueue<String>(10);

        Future<Object> submit1 = pool.submit(() -> {
            while (true) {
                boolean b = blockingQueue.offer("Value", 300, TimeUnit.MILLISECONDS);//waits timeout time if no space queue, then if still no space returns false
                System.out.println("Put: " + b);
                Thread.sleep(500);
            }
        });

        Future<Object> submit = pool.submit(() -> {
            while (true) {
                String take = (String) blockingQueue.take();
                System.out.println("Take");
                Thread.sleep(1000);
            }
        });

        submit1.get();
        submit.get();
    }

    @Test
    public void take() throws ExecutionException, InterruptedException {
        ArrayBlockingQueue blockingQueue = new ArrayBlockingQueue<String>(10);

        Future<Object> submit1 = pool.submit(() -> {
            while (true) {
                blockingQueue.put("Value"); //thread waits(blocked) if there is no space in queue
                System.out.println("Put");
                Thread.sleep(1000);
            }
        });

        Future<Object> submit = pool.submit(() -> {
            while (true) {
                String take = (String) blockingQueue.take(); //thread waits(blocked) if there is no object in queue
                System.out.println("Take");

            }
        });

        submit1.get();
        submit.get();
    }

    @Test
    public void poll() throws ExecutionException, InterruptedException {
        ArrayBlockingQueue blockingQueue = new ArrayBlockingQueue<String>(10);

        Future<Object> submit1 = pool.submit(() -> {
            while (true) {
                blockingQueue.put("Value"); //thread waits(blocked) if there is no space in queue
                System.out.println("Put");
                Thread.sleep(1000);
            }
        });

        Future<Object> submit = pool.submit(() -> {
            while (true) {
                String poll = (String) blockingQueue.poll(); //return null if not element in queue
                System.out.println(poll);
                Thread.sleep(500);

            }
        });

        submit1.get();
        submit.get();
    }

    @Test
    public void pollWithTimeout() throws ExecutionException, InterruptedException {
        ArrayBlockingQueue blockingQueue = new ArrayBlockingQueue<String>(10);

        Future<Object> submit1 = pool.submit(() -> {
            while (true) {
                blockingQueue.put("Value"); //thread waits(blocked) if there is no space in queue
                System.out.println("Put");
                Thread.sleep(1000);
            }
        });

        Future<Object> submit = pool.submit(() -> {
            while (true) {
                String poll = (String) blockingQueue.poll(500, TimeUnit.MILLISECONDS); //waits timeout time if no element in queue, then returns null if still no element in queue
                System.out.println(poll);
                Thread.sleep(500);
            }
        });

        submit1.get();
        submit.get();
    }

    @Test
    public void peek() throws ExecutionException, InterruptedException {
        ArrayBlockingQueue blockingQueue = new ArrayBlockingQueue<String>(10);

        Future<Object> submit1 = pool.submit(() -> {
            while (true) {
                blockingQueue.put("Value"); //thread waits(blocked) if there is no space in queue
                System.out.println("Put");
                Thread.sleep(1000);
            }
        });

        Future<Object> submit = pool.submit(() -> {
            while (true) {
                String poll = (String) blockingQueue.peek(); //takes first element in the queue without removing it, if not element returns null
                System.out.println(poll);
                Thread.sleep(500);
            }
        });

        submit1.get();
        submit.get();
    }

    @Test
    public void element() throws ExecutionException, InterruptedException {
        ArrayBlockingQueue blockingQueue = new ArrayBlockingQueue<String>(10);

        Future<Object> submit1 = pool.submit(() -> {
            while (true) {
                blockingQueue.put("Value"); //thread waits(blocked) if there is no space in queue
                System.out.println("Put");
                Thread.sleep(1000);
            }
        });

        Future<Object> submit = pool.submit(() -> {
            while (true) {
                String poll = (String) blockingQueue.element(); //takes first element in the queue without removing it, if not element throws Exception
                System.out.println(poll);
                Thread.sleep(500);
            }
        });

        submit1.get();
        submit.get();
    }
}
