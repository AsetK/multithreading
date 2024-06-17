package java_util_concurrent._5_executors;

import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveTask;

public class ForkJoinPoolTest {

    private static final int THRESHOLD = 10; // Порог для разделения задачи


    @Test
    public void withoutForkJoinPool() throws ExecutionException, InterruptedException {
        //не эффективно, так как на каждую рекурсию будет создавать новые потоки.
        long start = System.currentTimeMillis();
        int[] array = new int[100000];
        for (int i = 0; i < array.length; i++) {
            array[i] = i;
        }

        // Создаём пул потоков
        ExecutorService executor = Executors.newCachedThreadPool();

        try {
            // Запускаем задачу суммирования и получаем результат
            long result = parallelSum(array, 0, array.length, executor);
            System.out.println("Sum: " + result);
        } finally {
            // Завершаем пул потоков
            executor.shutdown();
        }
        long end = System.currentTimeMillis();
        long takenTime = end - start;

        System.out.println("Taken time: " + takenTime);
    }

    private long parallelSum(int[] array, int start, int end, ExecutorService executor) throws ExecutionException, InterruptedException {
        System.out.println(Thread.currentThread().getName());
        if (end - start <= THRESHOLD) {
            // Если размер подмассива меньше или равен порогу, вычисляем сумму напрямую
            long sum = 0;
            for (int i = start; i < end; i++) {
                sum += array[i];
            }
            return sum;
        } else {
            // Иначе разделяем задачу на две подзадачи
            int middle = (start + end) / 2;

            // Создаём задачи для левой и правой частей массива
            Callable<Long> leftTask = () -> parallelSum(array, start, middle, executor);
            Callable<Long> rightTask = () -> parallelSum(array, middle, end, executor);

            // Запускаем задачи и получаем результаты
            Future<Long> leftResult = executor.submit(leftTask);
            Future<Long> rightResult = executor.submit(rightTask);

            // Объединяем результаты
            return leftResult.get() + rightResult.get();
        }
    }

    @Test
    public void forkJoinPool() {
        /*
        Work-stealing алгоритм: Пул потоков использует алгоритм "воровства работы" (work-stealing),
        что позволяет эффективнее распределять задачи между потоками.
         */
        long start = System.currentTimeMillis();
        int[] array = new int[100000];
        for (int i = 0; i < array.length; i++) {
            array[i] = i;
        }

        SumTask sumTask = new SumTask(array, 0, array.length);

        Long result = new ForkJoinPool().invoke(sumTask);

        System.out.println(result);

        long end = System.currentTimeMillis();
        long takenTime = end - start;

        System.out.println("Taken time: " + takenTime);
    }


    public class SumTask extends RecursiveTask<Long> {
        private static final int THRESHOLD = 10;
        int[] array;
        int start;
        int end;

        public SumTask(int[] array, int start, int end) {
            this.array = array;
            this.start = start;
            this.end = end;
        }

        @Override
        protected Long compute() {
            System.out.println(Thread.currentThread().getName());
            if (end - start <= THRESHOLD) {
                // Directly compute the sum
                long sum = 0;
                for (int i = start; i < end; i++) {
                    sum = sum + array[i];
                }

                return sum;

            } else {
                // Split the task into two subtasks
                int middle = (start + end) / 2;

                SumTask leftTask = new SumTask(array, start, middle);
                SumTask rightTask = new SumTask(array, middle, end);

                leftTask.fork();
                rightTask.fork();

                Long result1 = leftTask.join();
                Long result2 = rightTask.join();

                long sum = result1 + result2;

                return sum;
            }
        }
    }

}
