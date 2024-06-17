package java_util_concurrent._6_completable_future;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.NoSuchElementException;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.function.Supplier;


//https://www.youtube.com/watch?v=-MBPQ7NIL_Y
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = Editor.class)
public class CompletableFutureTests {

    @Autowired
    Editor editor;
    ExecutorService pool = Executors.newCachedThreadPool();

    @Test
    public void blockingCall() throws Exception {
        String text = editor.toUpperCase("text");
        System.out.println(text);
        System.out.println("Can't print this until method execution");
    }

    @Test
    public void executorService() throws Exception {
        Callable<String> task = () -> editor.toUpperCase("text");
        Future<String> future = pool.submit(task);

        System.out.println("Can print this until method execution");

        String text = future.get();
        System.out.println(text);

        System.out.println("Can't print this until method execution");
    }

    @Test
    public void waitForFirstOrAll() throws Exception {
        Callable<String> firstTask = () -> editor.toUpperCase("first");
        Callable<String> secondTask = () -> editor.toUpperCase("second");

        Future<String> first = pool.submit(firstTask);
        Future<String> second = pool.submit(secondTask);

        System.out.println("Can print this until method execution");

        String firstResult = first.get();
        String secondResult = second.get();
        System.out.println(firstResult);
        System.out.println(secondResult);

        System.out.println("Can't print this until TWO methods execution");
    }

    //Далее используем CompletableFuture

    /*
    Основные методы

    Создание CompletableFuture. Данные методы не обязательно продолжать методами продолжения.
     - CompletableFuture.runAsync(Runnable task): Запускает задачу асинхронно, не возвращая результат.
     - CompletableFuture.supplyAsync(Supplier<U> supplier): Запускает задачу асинхронно и возвращает результат.

    Методы продолжения (Chaining)
    - thenApply(Function<T, U> fn): Преобразует результат CompletableFuture который был получен от supplyAsync.
    - thenAccept(Consumer<T> action): Выполняет действие с результатом CompletableFuture который был получен от supplyAsync.
    - thenRun(Runnable action): Выполняет действие после завершения CompletableFuture, не используя его результат.
    - thenCompose(Function<T, CompletableFuture<U>> fn): Асинхронно продолжает выполнение с новым CompletableFuture.

    Комбинирование
    - thenCombine(CompletableFuture<U> other, BiFunction<T, U, V> fn): Комбинирует результаты двух CompletableFuture.
    - allOf(CompletableFuture<?>... cfs): Ждет завершения всех переданных CompletableFuture.
    - anyOf(CompletableFuture<?>... cfs): Завершается при завершении любого из переданных CompletableFuture.
    - either(CompletableFuture<U> other): Продолжает выполнение с первым завершившимся CompletableFuture (аналог applyToEither).

    Обработка исключений
    - handle(BiFunction<T, Throwable, U> fn): Обрабатывает результат или исключение CompletableFuture.
    - exceptionally(Function<Throwable, U> fn): Обрабатывает исключение и возвращает значение по умолчанию в случае ошибки.
    */

    @Test
    public void multipleTasks() throws Exception {

        Supplier supplier = () -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName());
            return "text";
        };

        Function<String, String> function = text -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName());
            return text.toUpperCase();
        };

        //можем ассинхронно выполнять две и более задачи которые занимают много времени
        //completableFuture и completableFuture1 выполнятся последовательно друг другу, но паралельно main потоку.
        // completableFuture1 получит входной параметр, который completableFuture вернет (return "text";)
        // Можно также в метод перадать executor. Если не передавать по умолчанию использует ForkJoinPool.commonPool
        CompletableFuture<String> completableFuture = CompletableFuture.supplyAsync(supplier); //первая задача.
        CompletableFuture<String> completableFuture1 = completableFuture.thenApply(function); //вторая задача

        System.out.println("main"); //в тоже время основной поток не блокируется.

        System.out.println(completableFuture1.get()); // а потом получить результат когда будет готов
    }

    @Test
    public void multipleTasksWithRunnable() throws ExecutionException, InterruptedException {
        CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(() -> System.out.println("text"));

        completableFuture.get();
    }

    @Test
    public void thenCompose() throws Exception {
        //Если Function возвращает CompletableFuture, будет вложенность при использовании thenApply
        // т.к. он оборачивает возвращаемый объект в CompletableFuture
        CompletableFuture<CompletableFuture<String>> nested = methodThatReturnsCompletableFuture().thenApply(unHandledText -> methodThatReturnsCompletableFuture());
        String text = nested.get().get();
        System.out.println(text);

        //thenCompose избавляет нас от этой вложенности
        CompletableFuture<String> notNested = methodThatReturnsCompletableFuture().thenCompose(unHandledText -> methodThatReturnsCompletableFuture());
        String text2 = notNested.get();
        System.out.println(text2);
    }

    private CompletableFuture<String> methodThatReturnsCompletableFuture() {
        return CompletableFuture.supplyAsync(() -> "text");
    }

    @Test
    public void thenCombine() throws InterruptedException, ExecutionException {
        CompletableFuture<String> first = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "first";
        });
        CompletableFuture<String> second = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "second";
        });

        System.out.println("Main");

        //ждет когда выполнятся два future-а, и затем работает с ними.
        CompletableFuture<String> both = first.thenCombine(second, (String firstText, String secondText) -> firstText + secondText);

        System.out.println("Main");

        CompletableFuture<Void> cf = both.thenAccept(text -> System.out.println("thenAccept both: " + text));
        cf.get(); //нужен чтобы заблокировать main, иначе main выполниться раньше чем future-ы и вывод в консоль из future-а не выполнится.
    }

    @Test
    public void either() throws ExecutionException, InterruptedException {
        CompletableFuture<String> first = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "first";
        });
        CompletableFuture<String> second = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "second";
        });

        //применит к первому из выполнившихся.
        // Ко второму тоже выполнится, но ссылки на него все равно нет. Но не знаю как это проверить
        CompletableFuture<String> either = first.applyToEither(second, text -> text.toUpperCase());
        String result = either.get();
        System.out.println(result);
    }

    @Test
    public void allOf() throws InterruptedException, ExecutionException {
        CompletableFuture<String> first = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "first";
        });
        CompletableFuture<String> second = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "second";
        });
        CompletableFuture<String> third = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "third";
        });

        CompletableFuture<Void> forth = CompletableFuture.runAsync(() -> System.out.println("forth sout")); // Необязательно все должны быть CompletableFuture котороые от supplyAsync

        //Возвращает future который завершен, когда все внутренние завершены.
        //Это не значит что allCompleted future создается когда все внутренние завершены
        //Это значит allCompleted.get() будет ждать пока все внутренние не завершатся.
        CompletableFuture<Void> allCompleted = CompletableFuture.allOf(first, second, third);
        allCompleted.get();


        CompletableFuture<Void> cf = allCompleted.thenRun(() -> { //можно и так
            try {
                System.out.println(first.get());
                System.out.println(second.get());
                System.out.println(third.get());
                System.out.println(forth.get());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    public void anyOf() throws ExecutionException, InterruptedException {
        CompletableFuture<String> first = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "first";
        });
        CompletableFuture<String> second = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "second";
        });
        CompletableFuture<String> third = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "third";
        });

        CompletableFuture<Void> forth = CompletableFuture.runAsync(() -> System.out.println("forth")); // Необязательно все должны быть CompletableFuture котороые от supplyAsync

        CompletableFuture<Object> firstCompleted = CompletableFuture.anyOf(first, second, third, forth); //первого завершившегося.

        firstCompleted.thenAccept(System.out::println);

        Object o = firstCompleted.get();
    }

    @Test
    public void exception() throws ExecutionException, InterruptedException {
        CompletableFuture<String> cfe = this.throwsException(true);

        CompletableFuture<String> cf = cfe.thenApply(text -> { //never get called. because there is no result(text), there is an exception.
            System.out.println(text);
            return text;
        });

        System.out.println(cfe.get()); //never get called. throws exception too
    }

    private CompletableFuture<String> throwsException(boolean b) throws IllegalArgumentException {
        CompletableFuture<String> cf = CompletableFuture.supplyAsync(() -> {
            if (b) {
                throw new IllegalArgumentException();
            } else {
                return "first";
            }
        });

        return cf;
    }

    @Test
    public void handleException() throws Exception {
        CompletableFuture<String> cfe = this.throwsException(true);

        CompletableFuture<String> cf = cfe
                .handle((result, throwable) -> { //либо exception, либо result
                    if (throwable != null) {
                        return "Handled: " + throwable; //if exception
                    } else {
                        return result.toUpperCase(); //if no exception
                    }
                });

        System.out.println(cf.get());
    }

    @Test
    public void exceptionally() throws Exception {
        CompletableFuture<String> cfe = this.throwsException(true);

        CompletableFuture cf = cfe.exceptionally(throwable -> {
            return "Handled: " + throwable;
        }); //только exception, в отличии от handle который либо exception, либо result.
        System.out.println(cf.get());
    }

    @Test
    public void exceptionInOneOfStages() throws Exception {
        CompletableFuture<String> cfe = this.throwsException(false);
        CompletableFuture<String> cfeee = cfe
                .thenApply(r -> {
                    throw new NoSuchElementException();
                }) //вернет первый Exception, потому что дальше передавать уже нечего.
                .thenApply(r -> {
                    throw new RuntimeException();
                })
                .thenApply(r -> "text2");

        CompletableFuture cf = cfeee.exceptionally(throwable -> {
            return "Handled: " + throwable;
        }); //только exception, в отличии от handle который либо exception, либо result.
        System.out.println(cf.get());

        //при anyOf - если первый(самый быстрый) выполнится удачно, не важно что будет с остальными, вернется результат первого.
//                  - если первый(самый быстрый) выполнится НЕудачно, не важно что будет с остальными, вернется Exception
        //при allOf - если хотя бы один бросит Exception, то и результат будет Exception
    }
}
