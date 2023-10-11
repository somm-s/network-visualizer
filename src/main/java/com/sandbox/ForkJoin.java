package com.sandbox;

import java.util.concurrent.RecursiveTask;
import java.util.concurrent.ForkJoinPool;

public class ForkJoin extends RecursiveTask<Long> {
    private static final int THRESHOLD = 10;
    private long[] array;
    private int start;
    private int end;

    public ForkJoin(long[] array, int start, int end) {
        this.array = array;
        this.start = start;
        this.end = end;
    }

    @Override
    protected Long compute() {
        if (end - start <= THRESHOLD) {
            // If the task is small enough, compute the result directly
            long sum = 0;
            for (int i = start; i < end; i++) {
                sum += array[i];
            }
            return sum;
        } else {
            // Split the task into smaller subtasks
            int mid = (start + end) / 2;
            ForkJoin leftTask = new ForkJoin(array, start, mid);
            ForkJoin rightTask = new ForkJoin(array, mid, end);

            // Fork the subtasks
            leftTask.fork();
            rightTask.fork();

            // Join the results of the subtasks
            return leftTask.join() + rightTask.join();
        }
    }

    public static void main(String[] args) {
        // Create a ForkJoinPool
        ForkJoinPool forkJoinPool = ForkJoinPool.commonPool();

        // Create an array
        long[] array = new long[100];

        // Populate the array
        for (int i = 0; i < 100; i++) {
            array[i] = i + 1;
        }

        // Create a ForkJoinTask for the main computation
        ForkJoin task = new ForkJoin(array, 0, array.length);

        // Execute the task and get the result
        long result = forkJoinPool.invoke(task);

        System.out.println("Sum: " + result);

        // Shutdown the ForkJoinPool
        forkJoinPool.shutdown();
    }
}
