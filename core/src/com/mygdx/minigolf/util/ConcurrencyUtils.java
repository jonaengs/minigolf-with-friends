package com.mygdx.minigolf.util;

import com.badlogic.gdx.Gdx;

import java.util.Arrays;
import java.util.List;

public class ConcurrencyUtils {
    // Will break if the libGdx main thread's name ever changes
    private final static List<String> appThreadNames = Arrays.asList("LWJGL Application"); // TODO: Add android thread name

    public static void postRunnable(Runnable runnable) {
        Gdx.app.postRunnable(runnable);
    }

    public static void skipPostRunnable(Runnable runnable) {
        if (appThreadNames.contains(Thread.currentThread().getName())) {
            runnable.run();
        } else {
            Gdx.app.postRunnable(runnable);
        }
    }

    public static void skipWaitPostRunnable(Runnable runnable) {
        if (appThreadNames.contains(Thread.currentThread().getName())) {
            runnable.run();
        } else {
            waitPostRunnable(runnable);
        }
    }

    public static void waitPostRunnable(Runnable runnable) {
        final Object lock = new Object();
        synchronized (lock) {
            Gdx.app.postRunnable(() -> {
                runnable.run();
                synchronized (lock) {
                    lock.notify();
                }
            });
            try {
                lock.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
