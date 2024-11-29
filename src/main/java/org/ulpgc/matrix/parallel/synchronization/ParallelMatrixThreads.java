package org.ulpgc.matrix.parallel.synchronization;

public class ParallelMatrixThreads {
    private final int numThreads;

    public ParallelMatrixThreads(int numThreads) {
        this.numThreads = numThreads;
    }

    public double[][] multiply(double[][] matrixA, double[][] matrixB) {
        int rows = matrixA.length;
        int cols = matrixB[0].length;
        int common = matrixB.length;
        double[][] result = new double[rows][cols];

        Thread[] threads = new Thread[numThreads];
        int chunkSize = rows / numThreads;

        for (int t = 0; t < numThreads; t++) {
            final int startRow = t * chunkSize;
            final int endRow = (t == numThreads - 1) ? rows : startRow + chunkSize;

            threads[t] = new Thread(() -> {
                for (int i = startRow; i < endRow; i++) {
                    for (int j = 0; j < cols; j++) {
                        double sum = 0;
                        for (int k = 0; k < common; k++) {
                            sum += matrixA[i][k] * matrixB[k][j];
                        }
                        result[i][j] = sum;
                    }
                }
            });
            threads[t].start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return result;
    }
}
