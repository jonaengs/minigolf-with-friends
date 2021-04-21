package com.mygdx.minigolf.util;

import com.badlogic.gdx.Gdx;

public class ConcurrencyUtils {
    public static void waitForPostRunnable(Runnable runnable) {
        // Don't postpone if already in App thread.
        // TODO: The if-case may cause concurrentModificationError for some reason. Consider removing it.
        if (Thread.currentThread().getName().contentEquals("LWJGL Application")) {
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

    public static void postRunnable(Runnable runnable) {
        // Don't postpone if already in App thread.
        if (Thread.currentThread().getName().contentEquals("LWJGL Application")) {
            runnable.run();
        } else {
            Gdx.app.postRunnable(runnable);
        }
    }
}
