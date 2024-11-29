package org.ulpgc.matrix.parallel.synchronization;

import org.ulpgc.matrix.MatrixMultiplication;

import java.util.concurrent.atomic.DoubleAdder;

public class ParallelMatrixAtomic implements MatrixMultiplication {
    @Override
    public double[][] multiply(double[][] matrixA, double[][] matrixB) {
        int rows = matrixA.length;
        int cols = matrixB[0].length;
        int common = matrixB.length;

        DoubleAdder[][] result = new DoubleAdder[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[i][j] = new DoubleAdder();
            }
        }

        Thread[] threads = new Thread[rows];
        for (int i = 0; i < rows; i++) {
            final int row = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < cols; j++) {
                    for (int k = 0; k < common; k++) {
                        result[row][j].add(matrixA[row][k] * matrixB[k][j]);
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

        double[][] finalResult = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                finalResult[i][j] = result[i][j].sum();
            }
        }

        return finalResult;
    }
}
