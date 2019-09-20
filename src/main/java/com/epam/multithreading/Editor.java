package com.epam.multithreading;

import org.springframework.stereotype.Service;

@Service
public class Editor {

    public String toUpperCase(String text) throws InterruptedException {
        Thread.sleep(2000);
        return text.toUpperCase();
    }
}
