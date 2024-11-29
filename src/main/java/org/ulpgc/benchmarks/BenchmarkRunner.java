package org.ulpgc.benchmarks;

import org.openjdk.jmh.annotations.*;
import org.ulpgc.matrix.basic.BasicMatrixMultiplication;
import org.ulpgc.matrix.parallel.executors.ParallelMatrixExecutors;
import org.ulpgc.matrix.parallel.streams.ParallelMatrixStreams;
import org.ulpgc.matrix.parallel.synchronization.*;
import org.ulpgc.matrix.vectorized.VectorizedMatrixMultiplication;
import org.ulpgc.utils.MatrixGenerator;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(1)
@Warmup(iterations = 1, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
public class BenchmarkRunner {

    @State(Scope.Thread)
    public static class GlobalMatrixState {
        @Param({"64", "128", "512", "1024", "2048"})
        public int matrixSize;

        public double[][] matrixA;
        public double[][] matrixB;

        @Setup(Level.Trial)
        public void setup() {
            matrixA = MatrixGenerator.generateMatrix(matrixSize, matrixSize);
            matrixB = MatrixGenerator.generateMatrix(matrixSize, matrixSize);
        }
    }

    @State(Scope.Thread)
    public static class ParallelState {
        @Param({"1", "2", "4", "8", "16"})
        public int numThreads;
    }

    @Benchmark
    public double[][] basicMultiplication(GlobalMatrixState state) {
        return new BasicMatrixMultiplication().multiply(state.matrixA, state.matrixB);
    }

    @Benchmark
    public double[][] parallelExecutors(GlobalMatrixState matrixState, ParallelState parallelState) {
        return new ParallelMatrixExecutors(parallelState.numThreads).multiply(matrixState.matrixA, matrixState.matrixB);
    }

    @Benchmark
    public double[][] parallelThreads(GlobalMatrixState matrixState, ParallelState parallelState) {
        return new ParallelMatrixThreads(parallelState.numThreads).multiply(matrixState.matrixA, matrixState.matrixB);
    }

    @Benchmark
    public double[][] parallelStreams(GlobalMatrixState state) {
        return new ParallelMatrixStreams().multiply(state.matrixA, state.matrixB);
    }

    @Benchmark
    public double[][] parallelAtomic(GlobalMatrixState state) {
        return new ParallelMatrixAtomic().multiply(state.matrixA, state.matrixB);
    }

    @Benchmark
    public double[][] parallelSemaphore(GlobalMatrixState state) {
        return new ParallelMatrixSemaphore().multiply(state.matrixA, state.matrixB);
    }

    @Benchmark
    public double[][] parallelSynchronized(GlobalMatrixState state) {
        return new ParallelMatrixSynchronized().multiply(state.matrixA, state.matrixB);
    }

    @Benchmark
    public double[][] vectorizedMultiplication(GlobalMatrixState state) {
        return new VectorizedMatrixMultiplication().multiply(state.matrixA, state.matrixB);
    }
}
