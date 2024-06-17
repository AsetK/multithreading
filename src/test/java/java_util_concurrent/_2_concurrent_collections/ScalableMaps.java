package java_util_concurrent._2_concurrent_collections;

import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ScalableMaps {

    ExecutorService pool = Executors.newCachedThreadPool();

    @Test
    public void simpleHashMap() throws ExecutionException, InterruptedException {
        HashMap<String, String> map = new HashMap<>();

        Future<?> future1 = pool.submit(() -> {
            for (int i = 0; i < 10000; i++) {
                map.put(String.valueOf(i), String.valueOf(i));

            }
        });

        Future<?> future2 = pool.submit(() -> {
            for (int i = 10000; i < 20000; i++) {
                map.put(String.valueOf(i), String.valueOf(i));
            }
        });

        future1.get();
        future2.get();

        System.out.println(map.size());
    }

    @Test
    public void synchronizedHashMap() throws ExecutionException, InterruptedException {
        //При  модификации блокируется вся карта.
        HashMap<String, String> hashMap = new HashMap<>();
        Map<String, String> map = Collections.synchronizedMap(hashMap);

        Future<?> future1 = pool.submit(() -> {
            for (int i = 0; i < 10000; i++) {
                map.put(String.valueOf(i), String.valueOf(i));
            }
        });

        Future<?> future2 = pool.submit(() -> {
            for (int i = 10000; i < 20000; i++) {
                map.put(String.valueOf(i), String.valueOf(i));
            }
        });

        future1.get();
        future2.get();

        System.out.println(map.size());
    }


    @Test
    public void concurrentHashMap() throws ExecutionException, InterruptedException {
        //В отличии от Collections.synchronizedMap(), при модификации блокируется только сегмент элементов, а не вся карта.
        // Это позволяет другим потокам работать над другими сегментами.
        ConcurrentHashMap<String, String> map = new ConcurrentHashMap();

        Future<?> future1 = pool.submit(() -> {
            for (int i = 0; i < 10000; i++) {
                map.put(String.valueOf(i), String.valueOf(i));
            }
        });

        Future<?> future2 = pool.submit(() -> {
            for (int i = 10000; i < 20000; i++) {
                map.put(String.valueOf(i), String.valueOf(i));
            }
        });

        future1.get();
        future2.get();

        System.out.println(map.size());
    }
}
