package org.ext.threading;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.os.AsyncTask;

public class TaskQueue<Params, Progress, Result> {

    private ExecutorService mExecutor;
    private Queue<AsyncTask<Params, Progress, Result>> mTaskQueue = new LinkedList<AsyncTask<Params, Progress, Result>>();
    private Queue<Params[]> mParamQueue = new LinkedList<Params[]>();
    private WeakReference<AsyncTask<Params, Progress, Result>> mCurrentTaskRef;

    public synchronized void pushTask(AsyncTask<Params, Progress, Result> task, Params[] params) {
        mTaskQueue.add(task);
        mParamQueue.add(params);
    }

    private synchronized AsyncTask<Params, Progress, Result> startNext() {
        AsyncTask<Params, Progress, Result> nextTask = mTaskQueue.poll();
        if (nextTask != null) {
            nextTask.execute(mParamQueue.poll());
            mCurrentTaskRef = new WeakReference<AsyncTask<Params, Progress, Result>>(nextTask);
        }
        return nextTask;
    }

    public synchronized void start() {
        if (mExecutor != null)
            return;
        mExecutor = Executors.newSingleThreadExecutor();
        mExecutor.submit(new TaskRunner<Params, Progress, Result>(this));
    }

    public synchronized void stop() {
        mTaskQueue.clear();
        mParamQueue.clear();
        mExecutor.shutdownNow();
        mExecutor = null;
        if (mCurrentTaskRef != null) {
            AsyncTask<Params, Progress, Result> currentTask = mCurrentTaskRef.get();
            if (currentTask != null) {
                currentTask.cancel(true);
            }
        }
    }

    private static class TaskRunner<Params, Progress, Result> implements Runnable {
        WeakReference<TaskQueue<Params, Progress, Result>> mTaskQueueRef;

        public TaskRunner(TaskQueue<Params, Progress, Result> taskQueue) {
            mTaskQueueRef = new WeakReference<TaskQueue<Params, Progress, Result>>(taskQueue);
        }

        public void run() {
            while (true) {
                AsyncTask<Params, Progress, Result> task = mTaskQueueRef.get().startNext();
                if (task != null) {
                    try {
                        task.get(); // Wait on the task to complete. If it is interrupted, stop
                                    // executing.
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                        return;
                    } catch (ExecutionException ex) {
                        ex.printStackTrace();
                        return;
                    }
                } else {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                        return;
                    }
                }
            }
        }
    }
}
