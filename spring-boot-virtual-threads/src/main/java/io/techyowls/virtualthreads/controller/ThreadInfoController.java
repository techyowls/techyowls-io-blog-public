package io.techyowls.virtualthreads.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller to demonstrate and verify virtual thread behavior.
 */
@RestController
@RequestMapping("/api/threads")
public class ThreadInfoController {

    /**
     * Returns information about the current thread.
     * Use this to verify virtual threads are enabled.
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getThreadInfo() {
        Thread current = Thread.currentThread();

        Map<String, Object> info = new HashMap<>();
        info.put("threadName", current.getName());
        info.put("threadId", current.threadId());
        info.put("isVirtual", current.isVirtual());
        info.put("state", current.getState().toString());

        return ResponseEntity.ok(info);
    }

    /**
     * Simulates a slow endpoint to demonstrate virtual thread behavior.
     * The thread yields during Thread.sleep(), allowing other virtual threads to run.
     */
    @GetMapping("/slow")
    public ResponseEntity<Map<String, Object>> slowEndpoint(
            @RequestParam(defaultValue = "100") int delayMs) throws InterruptedException {

        Thread current = Thread.currentThread();
        long startTime = System.currentTimeMillis();

        // This is where virtual threads shine - during sleep, the carrier thread is released
        Thread.sleep(delayMs);

        long duration = System.currentTimeMillis() - startTime;

        Map<String, Object> response = new HashMap<>();
        response.put("requestedDelay", delayMs);
        response.put("actualDuration", duration);
        response.put("threadName", current.getName());
        response.put("isVirtual", current.isVirtual());
        response.put("message", "Virtual threads allow thousands of concurrent slow requests!");

        return ResponseEntity.ok(response);
    }

    /**
     * Returns count of virtual threads currently running.
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> getThreadCount() {
        long virtualCount = Thread.getAllStackTraces().keySet().stream()
            .filter(Thread::isVirtual)
            .count();

        long platformCount = Thread.getAllStackTraces().keySet().stream()
            .filter(t -> !t.isVirtual())
            .count();

        Map<String, Object> counts = new HashMap<>();
        counts.put("virtualThreads", virtualCount);
        counts.put("platformThreads", platformCount);
        counts.put("totalThreads", virtualCount + platformCount);

        return ResponseEntity.ok(counts);
    }
}
