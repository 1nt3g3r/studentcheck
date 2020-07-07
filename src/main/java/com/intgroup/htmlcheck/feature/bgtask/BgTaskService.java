package com.intgroup.htmlcheck.feature.bgtask;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

@Service
public class BgTaskService {
    public static final int THREAD_COUNT = 5;

    private ThreadPoolExecutor parallelThreadPoolExecutor;
    private ThreadPoolExecutor orderedThreadExecutor;

    private ConcurrentLinkedDeque<BgTask> nonOrderedTaskQueue;

    private ConcurrentLinkedDeque<BgTask> orderedTasksQueue;

    @PostConstruct
    public void init() {
        parallelThreadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(THREAD_COUNT);
        orderedThreadExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);

        nonOrderedTaskQueue = new ConcurrentLinkedDeque<>();
        orderedTasksQueue = new ConcurrentLinkedDeque<>();
    }

    public void submitTask(String description, int tryCount, Runnable runnable) {
        submitTask(description, tryCount, runnable, false);
    }

    public void submitTask(String description, int tryCount, Runnable runnable, boolean ordered) {
        if (runnable == null) {
            return;
        }

        if (ordered) {
            orderedTasksQueue.add(new BgTask(description, tryCount, runnable, ordered));
        } else {
            nonOrderedTaskQueue.add(new BgTask(description, tryCount, runnable, ordered));
        }

    }

    @Scheduled(fixedDelay = 1000)
    public void update() {
        updateNonOrderedTasks();

        updateOrderedTasks();
    }

    private void updateNonOrderedTasks() {
        //No tasks to execute
        if (nonOrderedTaskQueue.size() == 0) {
            return;
        }

        //Too much tasks executing right now
        if (parallelThreadPoolExecutor.getQueue().size() >= THREAD_COUNT * 2) {
            return;
        }

        //OK, post tasks
        int taskCount = Math.min(nonOrderedTaskQueue.size(), THREAD_COUNT);
        for (int i = 0; i < taskCount; i++) {
            BgTask task = nonOrderedTaskQueue.poll();
            parallelThreadPoolExecutor.submit(task);
        }
    }

    private void updateOrderedTasks() {
        //No tasks to execute
        if (orderedTasksQueue.size() <= 0) {
            return;
        }

        //Too much tasks
        if (orderedThreadExecutor.getQueue().size() >= 20) {
            return;
        }

        int taskCount = Math.min(orderedTasksQueue.size(), 20);
        for (int i = 0; i < taskCount; i++) {
            BgTask task = orderedTasksQueue.poll();
            orderedThreadExecutor.submit(task);
        }
    }

    public int getTaskCount(String... searchParts) {
        int result = 0;

        List<BgTask> tasks = new ArrayList<>(nonOrderedTaskQueue);
        tasks.addAll(orderedTasksQueue);

        for (BgTask task : tasks) {
            String taskDescription = task.getDescription();

            for (String searchPart : searchParts) {
                if (taskDescription.contains(searchPart)) {
                    result++;
                    break;
                }
            }
        }

        return result;
    }

    public List<BgTask> getTasks() {
        List<BgTask> result = new ArrayList<>(nonOrderedTaskQueue);
        result.addAll(orderedTasksQueue);
        return result;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private class BgTask implements Runnable {
        private String description;
        private int tryCount;
        private Runnable runnable;
        private boolean orderedTask;

        @Override
        public void run() {
            boolean finished = false;

            tryCount--;

            if (runnable == null) {
                finished = true;
            } else {
                try {
                    runnable.run();
                    finished = true;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            if (!finished && tryCount > 0) {
                if (orderedTask) {
                    orderedTasksQueue.add(this);
                } else {
                    nonOrderedTaskQueue.add(this);
                }
            }
        }
    }
}
