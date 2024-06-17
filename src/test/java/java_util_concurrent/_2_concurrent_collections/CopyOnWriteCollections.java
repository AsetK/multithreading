package java_util_concurrent._2_concurrent_collections;

import org.junit.Test;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class CopyOnWriteCollections {

    ExecutorService pool = Executors.newCachedThreadPool();

    @Test
    public void simpleArrayListConcurrentWritingIssue() throws ExecutionException, InterruptedException {
        /*
        Race Conditions
        When multiple threads attempt to add elements concurrently, several types of race conditions can occur:

        1. Lost Update: If two threads read the same value of size before either updates it, they might both write their elements to the same index, and the final size might only be incremented once.
           Thread 1 reads size as 5.
           Thread 2 reads size as 5.
           Thread 1 writes to elementData[5] and increments size to 6.
           Thread 2 writes to elementData[5] (overwriting the value written by Thread 1) and increments size to 6.

        2. Array Index Out of Bounds: If one thread resizes the array while another thread is adding an element, the latter might try to write to an invalid index.
           Thread 1 calls ensureCapacity and resizes the array.
           Thread 2 is adding an element and might get an invalid index if it gets a stale value of size.
         */
        ArrayList<Integer> array = new ArrayList<>();

        Future<?> future1 = pool.submit(() -> {
            for (int i = 0; i < 100000; i++) { //нужно вставлять много элементов чтобы Race Condition был, если мало элементов то первый поток завершиться раньше чем запуститься второй поток
                array.add(i);
            }
        });

        Future<?> future2 = pool.submit(() -> {
            for (int i = 0; i < 100000; i++) {
                array.add(i);
            }
        });

        future1.get();
        future2.get();

        System.out.println(array.size());
    }

    @Test
    public void simpleArrayListIteratorIssue() throws ExecutionException, InterruptedException {
        /* Второй поток выбросит ConcurrentModificationException, возможно нужно запустить НЕСКОЛЬКО раз.
           Итератор ArrayList является fail-fast, что означает, что он бросает ConcurrentModificationException,
           если обнаруживает, что коллекция была изменена после создания итератора, кроме как через сам итератор.
           Если бы итератор не был fail-fast, могли бы возникнуть несколько проблем:
           Неконсистентные данные: Представьте, что у вас есть список из 10 элементов, и два потока одновременно работают с этим списком.
           Один поток итерирует по списку, а другой изменяет его (добавляет или удаляет элементы). Итератор может пропустить новые элементы или
           обработать элементы, которые уже были удалены, что приведет к некорректной обработке данных.
         */

        ArrayList<Integer> array = new ArrayList<>();

        Future<?> future1 = pool.submit(() -> {
            for (int i = 0; i < 100000; i++) {
                array.add(i);
            }
        });

        Future<?> future2 = pool.submit(() -> {
            array.iterator().next();
        });

        future1.get();
        future2.get();

        System.out.println(array.size());
    }

    @Test
    public void CopyOnWriteArrayList () throws ExecutionException, InterruptedException {
        //Нужно подождать 30-60 сек
        /*
        Что дает CopyOnWriteArrayList
        1. В случае модификации создается новая копия массива, и запись происходит в эту новую копию.
        Таким образом, текущий массив остается неизменным для всех потоков, выполняющих чтение, пока не завершится операция записи.
        2. Потокобезопасность при чтении без использования блокировок. Это значит, что все потоки могут читать из списка одновременно
        без необходимости синхронизации, что значительно повышает производительность в сценариях с частым чтением и редкими записями.
        3. Копирование массива при каждой модификации гарантирует, что итераторы всегда видят неизменяемую и последовательную версию данных.
        Это устраняет проблему с изменением данных во время итерации, которая может привести к исключению ConcurrentModificationException
        */
        CopyOnWriteArrayList<Integer> array = new CopyOnWriteArrayList<>();

        Future<?> future1 = pool.submit(() -> {
            for (int i = 0; i < 100000; i++) { //нужно вставлять много элементов чтобы Race Condition был, если мало элементов то первый поток завершиться раньше чем запуститься второй поток
                array.add(i);
            }
        });

        Future<?> future2 = pool.submit(() -> {
            for (int i = 0; i < 100000; i++) {
                array.add(i);
            }
        });

        future1.get();
        future2.get();

        System.out.println(array.size());
    }
}
