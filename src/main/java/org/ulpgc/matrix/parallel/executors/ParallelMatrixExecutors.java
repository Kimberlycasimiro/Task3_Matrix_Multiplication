package org.ulpgc.matrix.parallel.executors;

import org.ulpgc.matrix.MatrixMultiplication;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ParallelMatrixExecutors implements MatrixMultiplication {
    private final int numThreads;

    public ParallelMatrixExecutors(int numThreads) {
        this.numThreads = numThreads;
    }

    @Override
    public double[][] multiply(double[][] matrixA, double[][] matrixB) {
        int rows = matrixA.length;
        int cols = matrixB[0].length;
        int size = matrixB.length;
        double[][] result = new double[rows][cols];
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        for (int i = 0; i < rows; i++) {
            final int row = i;
            executor.submit(() -> {
                for (int j = 0; j < cols; j++) {
                    double sum = 0;
                    for (int k = 0; k < size; k++) {
                        sum += matrixA[row][k] * matrixB[k][j];
                    }
                    result[row][j] = sum;
                }
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return result;
    }
}
