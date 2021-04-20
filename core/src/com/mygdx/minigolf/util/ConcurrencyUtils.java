package com.mygdx.minigolf.util;

import com.badlogic.gdx.Gdx;

public class ConcurrencyUtils {
    public static void waitForPostRunnable(Runnable runnable) {
        if (Thread.currentThread().getName().contentEquals("LWJGL Application")) {
            // Don't postpone if already in App thread.
            runnable.run();
        } else {
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
}
