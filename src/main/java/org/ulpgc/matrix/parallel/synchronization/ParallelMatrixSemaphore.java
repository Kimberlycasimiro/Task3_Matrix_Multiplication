package org.ulpgc.matrix.parallel.synchronization;

import org.ulpgc.matrix.MatrixMultiplication;
import java.util.concurrent.Semaphore;

public class ParallelMatrixSemaphore implements MatrixMultiplication {
    @Override
    public double[][] multiply(double[][] matrixA, double[][] matrixB) {
        int rows = matrixA.length;
        int cols = matrixB[0].length;
        int common = matrixB.length;
        double[][] result = new double[rows][cols];
        Semaphore semaphore = new Semaphore(1);

        Thread[] threads = new Thread[rows];
        for (int i = 0; i < rows; i++) {
            final int row = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < cols; j++) {
                    try {
                        semaphore.acquire();
                        for (int k = 0; k < common; k++) {
                            result[row][j] += matrixA[row][k] * matrixB[k][j];
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        semaphore.release();
                    }
                }
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        return result;
    }
}
