package org.ulpgc.benchmarks;

import com.sun.management.OperatingSystemMXBean;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.ulpgc.matrix.basic.BasicMatrixMultiplication;
import org.ulpgc.matrix.parallel.executors.ParallelMatrixExecutors;
import org.ulpgc.matrix.parallel.streams.ParallelMatrixStreams;
import org.ulpgc.matrix.parallel.synchronization.*;
import org.ulpgc.matrix.vectorized.VectorizedMatrixMultiplication;
import org.ulpgc.utils.MatrixGenerator;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class BenchmarkExecutor {

    private static Properties config;
    private static final OperatingSystemMXBean OS_BEAN =
            (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    private static final MemoryMXBean MEMORY_BEAN = ManagementFactory.getMemoryMXBean();
    private static final List<MemoryPoolMXBean> MEMORY_POOLS = ManagementFactory.getMemoryPoolMXBeans();

    public static void main(String[] args) throws Exception {
        loadConfig();

        String outputPath = config.getProperty("output.path");
        String[] matrixSizes = config.getProperty("matrix.sizes").split(",");
        String[] numThreads = config.getProperty("num.threads").split(",");

        ensureResultsDirectory(outputPath);

        Options opt = new OptionsBuilder()
                .include(".*BenchmarkRunner.*")
                .param("matrixSize", matrixSizes)
                .param("numThreads", numThreads)
                .result(outputPath.replace(".csv", ".json"))
                .resultFormat(ResultFormatType.JSON)
                .build();

        Collection<RunResult> results = new Runner(opt).run();

        exportMetrics(results, outputPath);
    }

    private static void loadConfig() throws IOException {
        config = new Properties();
        try (InputStream input = BenchmarkExecutor.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new RuntimeException("config.properties not found in resources");
            }
            config.load(input);
        }
    }

    private static void ensureResultsDirectory(String outputPath) {
        Path resultsPath = Paths.get(outputPath).getParent();
        if (resultsPath != null && !Files.exists(resultsPath)) {
            try {
                Files.createDirectories(resultsPath);
                System.out.println("Created results directory at: " + resultsPath);
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("Could not create results directory");
            }
        }
    }

    private static void exportMetrics(Collection<RunResult> results, String outputPath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
            writer.write("Matrix Size;Threads Used;Implementation;Execution Time (ms);Speedup;Efficiency;Memory Used (MB);CPU Used (%);Cores Used;Total Cores;Total Logical Cores\n");

            Map<String, Double> baselineTimes = new HashMap<>();
            for (RunResult result : results) {
                String implementation = result.getParams().getBenchmark();
                String key = result.getParams().getParam("matrixSize");
                double executionTime = result.getPrimaryResult().getScore();

                if (implementation.contains("basicMultiplication")) {
                    baselineTimes.put(key, executionTime);
                }
            }

            int totalLogicalCores = OS_BEAN.getAvailableProcessors();
            int totalPhysicalCores = 14;
            double threadsPerCore = (double) totalLogicalCores / totalPhysicalCores;

            for (RunResult result : results) {
                String fullImplementation = result.getParams().getBenchmark();
                String implementation = extractMethodName(fullImplementation);
                String matrixSize = result.getParams().getParam("matrixSize");

                String threadsUsed = result.getParams().getParamsKeys().contains("numThreads")
                        ? result.getParams().getParam("numThreads")
                        : "1";

                double memoryUsed = calculateMemoryUsageForImplementation(Integer.parseInt(matrixSize), Integer.parseInt(threadsUsed), implementation);
                double cpuUsed = calculateCpuUsageDuringMultiplication(Integer.parseInt(matrixSize), Integer.parseInt(threadsUsed), implementation);

                double executionTime = result.getPrimaryResult().getScore();
                double baselineTime = baselineTimes.getOrDefault(matrixSize, executionTime);

                double speedup = baselineTime / executionTime;
                int threads = Integer.parseInt(threadsUsed);
                double efficiency = threads == 1 ? 1.0 : speedup / threads;

                int coresUsed = (int) Math.ceil(threads / threadsPerCore);

                writer.write(String.format("%s;%s;%s;%.3f;%.3f;%.3f;%.2f;%.2f;%d;%d;%d\n",
                        matrixSize,
                        threadsUsed,
                        fullImplementation,
                        executionTime,
                        speedup,
                        efficiency,
                        memoryUsed,
                        cpuUsed,
                        coresUsed,
                        totalPhysicalCores,
                        totalLogicalCores
                ));
            }

            System.out.println("Benchmark results saved to: " + outputPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static double calculateCpuUsageDuringMultiplication(int matrixSize, int nThreads, String method) {
        double[][] a = MatrixGenerator.generateMatrix(matrixSize, matrixSize);
        double[][] b = MatrixGenerator.generateMatrix(matrixSize, matrixSize);

        long startCpuTime = OS_BEAN.getProcessCpuTime();
        long startWallTime = System.nanoTime();

        switch (method) {
            case "basicMultiplication":
                new BasicMatrixMultiplication().multiply(a, b);
                break;
            case "parallelExecutors":
                new ParallelMatrixExecutors(nThreads).multiply(a, b);
                break;
            case "parallelThreads":
                new ParallelMatrixThreads(nThreads).multiply(a, b);
                break;
            case "parallelStreams":
                new ParallelMatrixStreams().multiply(a, b);
                break;
            case "parallelAtomic":
                new ParallelMatrixAtomic().multiply(a, b);
                break;
            case "parallelSemaphore":
                new ParallelMatrixSemaphore().multiply(a, b);
                break;
            case "parallelSynchronized":
                new ParallelMatrixSynchronized().multiply(a, b);
                break;
            case "vectorizedMultiplication":
                new VectorizedMatrixMultiplication().multiply(a, b);
                break;
            default:
                throw new IllegalArgumentException("Unknown multiplication method: " + method);
        }

        long endCpuTime = OS_BEAN.getProcessCpuTime();
        long endWallTime = System.nanoTime();

        long cpuTimeUsed = endCpuTime - startCpuTime;
        long wallTimeElapsed = endWallTime - startWallTime;

        if (wallTimeElapsed <= 0) {
            return 0.0;
        }

        int totalLogicalCores = OS_BEAN.getAvailableProcessors();
        double cpuUsage = ((double) cpuTimeUsed / (wallTimeElapsed * totalLogicalCores)) * 100;

        return Math.min(cpuUsage, 100.0);
    }


    private static double calculateMemoryUsageForImplementation(int matrixSize, int nThreads, String method) {
        double[][] a = MatrixGenerator.generateMatrix(matrixSize, matrixSize);
        double[][] b = MatrixGenerator.generateMatrix(matrixSize, matrixSize);

        System.gc();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        long beforeMemory = getUsedMemory();

        switch (method) {
            case "basicMultiplication":
                new BasicMatrixMultiplication().multiply(a, b);
                break;
            case "parallelExecutors":
                new ParallelMatrixExecutors(nThreads).multiply(a, b);
                break;
            case "parallelThreads":
                new ParallelMatrixThreads(nThreads).multiply(a, b);
                break;
            case "parallelStreams":
                new ParallelMatrixStreams().multiply(a, b);
                break;
            case "parallelAtomic":
                new ParallelMatrixAtomic().multiply(a, b);
                break;
            case "parallelSemaphore":
                new ParallelMatrixSemaphore().multiply(a, b);
                break;
            case "parallelSynchronized":
                new ParallelMatrixSynchronized().multiply(a, b);
                break;
            case "vectorizedMultiplication":
                new VectorizedMatrixMultiplication().multiply(a, b);
                break;
            default:
                throw new IllegalArgumentException("Unknown multiplication method: " + method);
        }

        System.gc();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        long afterMemory = getUsedMemory();

        return Math.max(0.0, (afterMemory - beforeMemory) / (1024.0 * 1024.0));
    }

    private static long getUsedMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }


    private static String extractMethodName(String fullImplementation) {
        if (fullImplementation.contains(".")) {
            return fullImplementation.substring(fullImplementation.lastIndexOf('.') + 1);
        }
        return fullImplementation;
    }
}
