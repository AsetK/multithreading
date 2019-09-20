import com.epam.multithreading.Editor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.*;

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

    @Test
    public void completed() throws Exception {
//        CompletableFuture<String> completableFuture = CompletableFuture.completedFuture("text");
//        String text = completableFuture.get();

        CompletableFuture.supplyAsync(()->"text");
        Callable<String> firstTask = ()-> editor.toUpperCase("first");

        System.out.println("main");
    }
}
