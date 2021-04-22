package com.mygdx.minigolf.util;

import com.badlogic.gdx.Gdx;

public class ConcurrencyUtils {
    public static void waitForPostRunnable(Runnable runnable) {
        // TODO: This only checks for desktop main thread name (LWJGL). It does not check for the android thread's name.
        if (Thread.currentThread().getName().contentEquals("LWJGL Application")) {
            // Don't postpone if already in App thread. Will break if libGdx ever changes the main thread's name
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
        // TODO: This only checks for desktop main thread name (LWJGL). It does not check for the android thread's name.
        if (Thread.currentThread().getName().contentEquals("LWJGL Application")) {
            // Don't postpone if already in App thread.
            runnable.run();
        } else {
            Gdx.app.postRunnable(runnable);
        }
    }
}
