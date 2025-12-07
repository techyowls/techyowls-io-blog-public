package io.techyowls.virtualthreads;

import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Demonstrates carrier thread pinning and how to fix it.
 *
 * Run with: java -Djdk.tracePinnedThreads=full PinningExample
 *
 * Pinning happens when:
 * - synchronized blocks with blocking I/O
 * - Native code / JNI
 *
 * Pinning is BAD because it blocks the carrier thread.
 */
public class PinningExample {

    private final Object syncLock = new Object();
    private final ReentrantLock reentrantLock = new ReentrantLock();

    public static void main(String[] args) throws Exception {
        PinningExample example = new PinningExample();

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            // BAD: This causes pinning
            System.out.println("Running with synchronized (causes pinning)...");
            for (int i = 0; i < 10; i++) {
                executor.submit(example::badSynchronizedMethod);
            }
            Thread.sleep(2000);

            // GOOD: This does not cause pinning
            System.out.println("\nRunning with ReentrantLock (no pinning)...");
            for (int i = 0; i < 10; i++) {
                executor.submit(example::goodReentrantLockMethod);
            }
            Thread.sleep(2000);
        }
    }

    /**
     * BAD: synchronized + blocking I/O pins the carrier thread.
     */
    void badSynchronizedMethod() {
        synchronized (syncLock) {
            try {
                // This PINS the carrier thread!
                Thread.sleep(100);  // Simulating blocking I/O
                System.out.println("synchronized: " + Thread.currentThread());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * GOOD: ReentrantLock allows carrier to be released during blocking.
     */
    void goodReentrantLockMethod() {
        reentrantLock.lock();
        try {
            Thread.sleep(100);  // Carrier can be reused!
            System.out.println("ReentrantLock: " + Thread.currentThread());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            reentrantLock.unlock();
        }
    }
}
