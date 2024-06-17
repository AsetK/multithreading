package java_util_concurrent._6_completable_future;

import org.springframework.stereotype.Service;

@Service
public class Editor {

    public String toUpperCase(String text) throws InterruptedException {
        Thread.sleep(2000);
        return text.toUpperCase();
    }
}
