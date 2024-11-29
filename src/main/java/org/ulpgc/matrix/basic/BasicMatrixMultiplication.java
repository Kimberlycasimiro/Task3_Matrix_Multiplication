package org.ulpgc.matrix.basic;

import org.ulpgc.matrix.MatrixMultiplication;

public class BasicMatrixMultiplication implements MatrixMultiplication {
    @Override
    public double[][] multiply(double[][] matrixA, double[][] matrixB) {
        int rows = matrixA.length;
        int cols = matrixB[0].length;
        int common = matrixB.length;

        double[][] result = new double[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                for (int k = 0; k < common; k++) {
                    result[i][j] += matrixA[i][k] * matrixB[k][j];
                }
            }
        }
        return result;
    }
}
