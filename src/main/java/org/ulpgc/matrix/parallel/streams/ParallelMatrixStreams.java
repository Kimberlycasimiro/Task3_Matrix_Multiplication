package org.ulpgc.matrix.parallel.streams;

import org.ulpgc.matrix.MatrixMultiplication;

import java.util.stream.IntStream;

public class ParallelMatrixStreams implements MatrixMultiplication {
    @Override
    public double[][] multiply(double[][] matrixA, double[][] matrixB) {
        int rows = matrixA.length;
        int cols = matrixB[0].length;
        int size = matrixB.length;
        double[][] result = new double[rows][cols];

        IntStream.range(0, rows).parallel().forEach(i -> {
            for (int j = 0; j < cols; j++) {
                double sum = 0;
                for (int k = 0; k < size; k++) {
                    sum += matrixA[i][k] * matrixB[k][j];
                }
                result[i][j] = sum;
            }
        });

        return result;
    }
}
