import com.epam.multithreading.CallableTask;
import com.epam.multithreading.Editor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.function.Supplier;


//https://www.youtube.com/watch?v=-MBPQ7NIL_Y
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = Editor.class)
public class MyTests {

    @Autowired
    Editor editor;
    ExecutorService executorService = Executors.newCachedThreadPool();

    @Test
    public void blockingCall() throws Exception {
        String text = editor.toUpperCase("text");
        System.out.println(text);
        System.out.println("Can't print this until method execution");
    }

    @Test
    public void executorService() throws Exception {
        Callable<String> task = ()-> editor.toUpperCase("text");
        Future<String> future = executorService.submit(task);

        System.out.println("Can print this until method execution");

        String text = future.get();
        System.out.println(text);

        System.out.println("Can't print this until method execution");
    }

    @Test
    public void waitForFirstOrAll() throws Exception{
        Callable<String> firstTask = ()-> editor.toUpperCase("first");
        Callable<String> secondTask = ()-> editor.toUpperCase("second");

        Future<String> first = executorService.submit(firstTask);
        Future<String> second = executorService.submit(secondTask);

        System.out.println("Can print this until method execution");

        String firstResult = first.get();
        String secondResult = second.get();
        System.out.println(firstResult);
        System.out.println(secondResult);

        System.out.println("Can't print this until TWO methods execution");
    }

    //Далее используем CompletableFuture
    @Test
    public void multipleTasks() throws Exception {
        Supplier supplier = () -> {
            try { Thread.sleep(2000); } catch (InterruptedException e) { e.printStackTrace(); }
            System.out.println(Thread.currentThread().getName());
            return "text";
        };

        Function<String, String> function = text -> {
            try { Thread.sleep(2000); } catch (InterruptedException e) { e.printStackTrace(); }
            System.out.println(Thread.currentThread().getName());
            return text.toUpperCase();
        };

        //можем ассинхронно выполнять две и более задачи которые занимают много времени
        // Можно также в метод перадать executor. Если не передавать по умолчанию использует ForkJoinPool.commonPool
        CompletableFuture<String> completableFuture = CompletableFuture.supplyAsync(supplier); //первая задача.
        CompletableFuture<String> completableFuture1 = completableFuture.thenApply(function); //вторая задача

        System.out.println("main"); //в тоже время основной поток не блокируется.

        System.out.println(completableFuture1.get()); // а потом получить результат когда будет готов
    }

    @Test
    public void thenCompose() throws Exception {
        //Если Function возвращает CompletableFuture, будет вложенность при использовании thenApply
        // т.к. он оборачивает возвращаемый объект в CompletableFuture
        CompletableFuture<CompletableFuture<String>> nested = methodThatReturnsCompletableFuture().thenApply(unHandledText->methodThatReturnsCompletableFuture());
        String text = nested.get().get();
        System.out.println(text);

        //thenCompose избавляет нас от этой вложенности
        CompletableFuture<String> notNested = methodThatReturnsCompletableFuture().thenCompose(unHandledText->methodThatReturnsCompletableFuture());
        String text2 = notNested.get();
        System.out.println(text2);
    }

    private CompletableFuture<String> methodThatReturnsCompletableFuture(){
        return CompletableFuture.supplyAsync(()->"text").thenApply(text->text);
    }

    @Test
    public void thenCombine() throws InterruptedException, ExecutionException {
        CompletableFuture<String> first = CompletableFuture.supplyAsync(()->{ try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); }
            return "first";});
        CompletableFuture<String> second = CompletableFuture.supplyAsync(()->{ try { Thread.sleep(2000); } catch (InterruptedException e) { e.printStackTrace();}
            return "second";});

        System.out.println("Main");

        //ждет когда выполнятся два future-а, и затем работает с ними.
        CompletableFuture<String> both = first.thenCombine(second, (String firstText, String secondText) -> firstText + secondText);

        System.out.println("Main");

        CompletableFuture<Void> cf = both.thenAccept(text-> System.out.println("thenAccept both: " + text));
        cf.get(); //нужен чтобы заблокировать main, иначе main выполниться раньше чем future-ы и вывод в консоль из future-а не выполнится.
    }

    @Test
    public void either() throws ExecutionException, InterruptedException {
        CompletableFuture<String> first = CompletableFuture.supplyAsync(()->{ try { Thread.sleep(2000); } catch (InterruptedException e) { e.printStackTrace(); }
            return "first";});
        CompletableFuture<String> second = CompletableFuture.supplyAsync(()->{ try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace();}
            return "second";});

        //применит к первому из выполнившихся.
        // Ко второму тоже выполнится, но ссылки на него все равно нет. Но не знаю как это проверить
        CompletableFuture<String> either = first.applyToEither(second, text -> text.toUpperCase());
        either.thenAccept(text-> System.out.println(text));
        either.get();
    }

    @Test
    public void allOf() throws InterruptedException, ExecutionException {
        CompletableFuture<String> first = CompletableFuture.supplyAsync(()->{ try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); }
            return "first";});
        CompletableFuture<String> second = CompletableFuture.supplyAsync(()->{ try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); }
            return "second";});
        CompletableFuture<String> third = CompletableFuture.supplyAsync(()->{ try { Thread.sleep(3000); } catch (InterruptedException e) { e.printStackTrace(); }
            return "third";});

        //Возвращает future который завершен, когда все внутренние завершены.
        //Это не значит что allCompleted future создается когда все внутренние завершены
        //Это значит allCompleted.get() будет ждать пока все внутренние не завершатся.
        CompletableFuture<Void> allCompleted = CompletableFuture.allOf(first, second, third);
        allCompleted.get();

//        allCompleted.thenAccept(aVoid -> {   //можно и так
//            try {
//                System.out.println(first.get());
//                System.out.println(second.get());
//                System.out.println(third.get());
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        });

        CompletableFuture<Void> cf = allCompleted.thenRun(()->{ //можно и так
            try {
                System.out.println(first.get());
                System.out.println(second.get());
                System.out.println(third.get());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    public void anyOf() throws ExecutionException, InterruptedException {
        CompletableFuture<String> first = CompletableFuture.supplyAsync(()->{ try { Thread.sleep(2000); } catch (InterruptedException e) { e.printStackTrace(); }
            return "first";});
        CompletableFuture<String> second = CompletableFuture.supplyAsync(()->{ try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); }
            return "second";});
        CompletableFuture<String> third = CompletableFuture.supplyAsync(()->{ try { Thread.sleep(2000); } catch (InterruptedException e) { e.printStackTrace(); }
            return "third";});

        CompletableFuture<Object> firstCompleted = CompletableFuture.anyOf(first, second, third); //первого завершившегося.

        firstCompleted.thenAccept(object-> System.out.println(object));

        Object o = firstCompleted.get();
    }

    @Test
    public void exception() throws ExecutionException, InterruptedException {
        CompletableFuture<String> cfe = this.throwsException(true);

        CompletableFuture<String> cf = cfe.thenApply(text->{ //never get called. because there is no result(text), there is an exception.
            System.out.println(text);
            return text;});

        System.out.println(cfe.get()); //never get called. throws exception too
    }

    private CompletableFuture<String> throwsException(boolean b) throws IllegalArgumentException{
        CompletableFuture<String> cf = CompletableFuture.supplyAsync(() -> {
            if(b){
                throw new IllegalArgumentException();
            }else {
                return "first";
            }
        });

        return cf;
    }

    @Test
    public void handleException()throws Exception{
        CompletableFuture<String> cfe = this.throwsException(true);

        CompletableFuture<String> cf = cfe
                .handle((result, throwable) -> { //либо exception, либо result
                    if(throwable != null){
                        return "Handled: " + throwable; //if exception
                    } else {
                        return result.toUpperCase(); //if no exception
                    }});

        System.out.println(cf.get());
    }

    @Test
    public void exceptionally() throws Exception{
        CompletableFuture<String> cfe = this.throwsException(true);

        CompletableFuture cf = cfe.exceptionally(throwable -> {return "Handled: " + throwable;}); //только exception, в отличии от handle который либо exception, либо result.
        System.out.println(cf.get());
    }

    @Test
    public void exceptionInOneOfStages() throws Exception{
        CompletableFuture<String> cfe = this.throwsException(false);
        CompletableFuture<String> cfeee = cfe
                .thenApply(r -> {throw new NoSuchElementException();}) //вернет первый Exception, потому что дальше передавать уже нечего.
                .thenApply(r -> {throw new RuntimeException();})
                .thenApply(r -> "text2");

        CompletableFuture cf = cfeee.exceptionally(throwable -> {return "Handled: " + throwable;}); //только exception, в отличии от handle который либо exception, либо result.
        System.out.println(cf.get());

        //при anyOf - если первый(самый быстрый) выполнится удачно, не важно что будет с остальными, вернется результат первого.
//                  - если первый(самый быстрый) выполнится НЕудачно, не важно что будет с остальными, вернется Exception
        //при allOf - если хотя бы один бросит Exception, то и результат будет Exception
    }
}
