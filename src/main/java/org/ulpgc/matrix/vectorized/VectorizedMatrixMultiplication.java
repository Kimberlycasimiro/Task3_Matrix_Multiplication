package org.ulpgc.matrix.vectorized;

import org.ulpgc.matrix.MatrixMultiplication;
import jdk.incubator.vector.*;

public class VectorizedMatrixMultiplication implements MatrixMultiplication {
    private static final VectorSpecies<Double> SPECIES = DoubleVector.SPECIES_PREFERRED;

    @Override
    public double[][] multiply(double[][] matrixA, double[][] matrixB) {
        int rows = matrixA.length;
        int cols = matrixB[0].length;
        int common = matrixB.length;
        double[][] result = new double[rows][cols];

        double[][] transposedB = transpose(matrixB);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[i][j] = vectorizedDotProduct(matrixA[i], transposedB[j]);
            }
        }

        return result;
    }

    private double[][] transpose(double[][] matrix) {
        int rows = matrix.length;
        int cols = matrix[0].length;
        double[][] transposed = new double[cols][rows];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                transposed[j][i] = matrix[i][j];
            }
        }

        return transposed;
    }

    private double vectorizedDotProduct(double[] row, double[] col) {
        int length = row.length;
        double sum = 0.0;
        int i = 0;

        while (i < SPECIES.loopBound(length)) {
            DoubleVector vectorA = DoubleVector.fromArray(SPECIES, row, i);
            DoubleVector vectorB = DoubleVector.fromArray(SPECIES, col, i);
            sum += vectorA.mul(vectorB).reduceLanes(VectorOperators.ADD);
            i += SPECIES.length();
        }

        for (; i < length; i++) {
            sum += row[i] * col[i];
        }

        return sum;
    }
}
