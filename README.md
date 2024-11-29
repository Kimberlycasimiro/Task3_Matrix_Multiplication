# **Parallel and Vectorized Matrix Multiplication**

**Course:** Big Data (BD)  
**Degree:** Data Science and Engineering (GCID)  
**University:** University of Las Palmas de Gran Canaria (ULPGC)  
**Academic Year:** 2024 / 2025   
**Author:** Casimiro Torres, Kimberly 

---

## Execution Environment

The project was developed, configured, and executed in the following environment:

- **IDE Used:** IntelliJ IDEA
- **Operating System:** Windows 11 (Version 10.0, Architecture amd64)
- **CPU:** GenuineIntel 13th Gen Intel(R) Core(TM) i7-13700H
    - **Logical Processors:** 20
    - **Physical Processors:** 14
- **RAM:** 16 GB
- **System Manufacturer and Model:**
    - **Manufacturer:** HP
    - **Model:** Victus by HP Gaming Laptop 16-r0xxx
---

## Project Description

This project develops and analyzes various matrix multiplication methods, ranging from a basic algorithm to more advanced parallel and vectorized approaches. The aim is to explore how these techniques improve execution time, efficiency, and resource utilization when working with large matrices.

---

## Project Structure

The project is organized in a structured and modular manner to facilitate the implementation, analysis, and comparison of different matrix multiplication approaches. Below is a detailed description of each folder and class included in the project:

### `org.ulpgc.benchmarks`

This folder contains classes related to the execution and measurement of performance for different matrix multiplication approaches.

- **`BenchmarkExecutor`**  
  Processes results generated by the benchmark to calculate metrics such as memory usage, CPU usage, speedup, efficiency, and the number of physical and logical cores used. Manages result storage and formatting to facilitate analysis.

- **`BenchmarkRunner`**  
  Executes performance tests using JMH (Java Microbenchmark Harness) and generates detailed results such as average time per operation (Score), margin of error (Error), measurement mode (Mode), number of iterations (Cnt), and units used (Units).

---

### `org.ulpgc.charts`

Contains classes responsible for generating graphs to visualize benchmark results.

- **`CoresUsedChart`**  
  Generates a graph showing the number of cores used by each approach.

- **`CPUUsageChart`**  
  Visualizes the percentage of CPU usage during algorithm execution.

- **`EfficiencyChart`**  
  Displays the efficiency of parallel execution as a function of the number of cores used.

- **`ExecutionTimeChart`**  
  Represents the execution time of algorithms based on matrix size.

- **`MemoryUsageChart`**  
  Illustrates memory usage for each approach, helping to identify potential bottlenecks.

- **`SpeedupChart`**  
  Compares the performance of parallel and vectorized approaches against the basic algorithm.

### `org.ulpgc.matrix`

This package contains matrix multiplication algorithm implementations organized into subfolders according to their approach.

#### **`basic/`**

- **`BasicMatrixMultiplication`**  
  Implements the basic matrix multiplication algorithm using nested loops. It serves as a reference to measure improvements of advanced approaches.

#### **`parallel/`**

Contains various parallelization strategies to distribute work among multiple threads.

- **`executors/`**
    - **`ParallelMatrixExecutors`**  
      Implements parallelization using Java's executor framework. Calculations are divided into tasks processed in a thread pool.

- **`streams/`**
    - **`ParallelMatrixStreams`**  
      Utilizes parallel streams to divide and process matrix rows concurrently.

- **`synchronization/`**  
  Includes implementations ensuring consistency and synchronization in concurrent operations:

    - **`ParallelMatrixAtomic`**  
      Uses atomic variables for safe operations on shared matrices.

    - **`ParallelMatrixSemaphore`**  
      Employs semaphores to control access to critical sections of shared data.

    - **`ParallelMatrixSynchronized`**  
      Utilizes synchronized blocks to prevent race conditions on shared data.

    - **`ParallelMatrixThreads`**  
      Manually divides work among threads. Each thread processes a subset of matrix rows.

#### **`vectorized/`**

- **`VectorizedMatrixMultiplication`**  
  Implements matrix multiplication using SIMD (Single Instruction Multiple Data) instructions with the `jdk.incubator.vector` API. Focuses on processing multiple data elements simultaneously in vector registers.

### **`MatrixMultiplication`**

- **Base Interface:**  
  Defines the `multiply(double[][] matrixA, double[][] matrixB)` method to be implemented by all classes for matrix multiplication. Ensures consistency in the interface across implementations.

---

### `utils/`

Provides common utilities required for project execution.

- **`MatrixGenerator`**  
  Generates random matrices of different sizes based on configured parameters. This class ensures tests are performed consistently by generating controlled and reproducible input data for benchmarks.

---

### `resources/`

Contains the project's configuration file.

- **`config.properties`**  
  Defines key parameters such as matrix sizes (`matrix.sizes`), the number of threads for parallel implementations (`num.threads`), and the output path for results (`output.path`).

---

### `results/`

Stores benchmark results in CSV and JSON formats to facilitate data analysis and visualization.

---

### `output_charts/`

Contains graphs generated from benchmarks, such as CPU usage, memory usage, execution time, efficiency, cores used, and speedup.

---

### `target/`

Includes compiled project files, such as the executable JAR file and other artifacts generated by Maven.

---

### Dependencies

- **`JMH` (Java Microbenchmarking Harness):**  
  Used for executing and measuring implementation performance benchmarks.

- **`JDK 21`:**  
  Used to compile and execute the project, including support for the `jdk.incubator.vector` module in the vectorized implementation.

- **`Maven`:**  
  Used to compile the project, manage dependencies, and generate the executable file.

- **`JFreeChart`:**  
  Generates graphs to visualize benchmark results.

- **`OpenCSV`:**  
  Used for reading and writing results in CSV format.

---

### **Instructions to Execute the Project**

1. **Clone the repository**  
   Download the project source code to your local machine.
   ```bash
   git clone https://github.com/Kimberlycasimiro/Task3_Matrix_Multiplication.git
   ```
2. **Compile the project using Maven**
   Ensure all dependencies are installed, and the project compiles correctly.
   ```bash
   mvn clean install
   ```
3. **Configure the environment for benchmarks**

   Configure the execution environment as follows:

   - **Select the main class:**  
     Ensure `org.ulpgc.benchmarks.BenchmarkExecutor` is selected as the main class.

   - **Enable the vectorization module:**  
     Add `--add-modules jdk.incubator.vector` in VM options to enable support for vectorized operations.


4. **Execution:**  
   Once the environment is configured, press `Run` in your development environment to execute benchmarks. This will generate:
   - Result files in CSV and JSON formats. 
   - Key metrics such as execution time, memory usage, CPU usage, speedup, efficiency, physical and logical cores used.
---

## Results

The results of this project provide a comprehensive analysis of the performance, efficiency, and resource utilization of various matrix multiplication techniques, from the basic algorithm to advanced parallel and vectorized implementations. The findings are summarized below, highlighting key observations derived from metrics such as execution time, speedup, efficiency, CPU and memory usage, and core utilization.

### Execution Time

Execution time serves as a primary metric to evaluate how each technique handles increasing computational loads. The basic algorithm, serving as the baseline, exhibited the highest execution times, particularly for large matrices such as 2048x2048, reaching execution times of over 270,000 milliseconds. This sequential approach demonstrated its inability to scale efficiently, reinforcing the necessity for optimized techniques.

Parallel implementations showed significant reductions in execution time, with parallelExecutors and parallelThreads effectively distributing computations across multiple threads. Configurations with 8 threads achieved an optimal balance, significantly reducing execution times while maintaining manageable synchronization overhead. The vectorized implementation outperformed all other techniques, leveraging SIMD instructions to process data simultaneously at the hardware level, achieving the shortest execution times even for the largest matrices.

### Speedup

Speedup measures the improvement in execution time compared to the basic algorithm. The vectorized implementation achieved the highest speedup across all matrix sizes, reaching values far exceeding those of parallel implementations. Among parallel techniques, parallelAtomic and parallelStreams also demonstrated competitive speedup for large matrices, albeit with higher resource consumption.

Configurations with a moderate number of threads, such as parallelExecutors with 8 threads, offered a well-rounded balance, achieving substantial speedup without incurring excessive overhead. Techniques with 16 threads, while achieving higher speedups, experienced diminishing returns due to synchronization costs.

### Efficiency

Efficiency, defined as the ratio between speedup and the number of threads, highlighted the importance of balancing computational load with resource allocation. Configurations such as parallelExecutors with 8 threads and parallelThreads with 4 threads maintained high efficiency, showcasing effective use of threads with minimal overhead. Conversely, implementations with 16 threads exhibited lower efficiency due to increased synchronization and task management costs.

### CPU and Memory Usage

Resource usage varied significantly across implementations:

- ParallelAtomic and parallelStreams demonstrated high CPU usage, reaching nearly 100% for large matrices. While this intensive resource utilization maximized performance, it may limit their applicability in systems with constrained hardware. 

- The vectorized implementation stood out for maintaining low CPU and memory usage while achieving the highest performance. This highlights its efficiency in leveraging modern hardware capabilities. 

- Memory usage increased with matrix size across all implementations, with parallelSynchronized and parallelThreads with 16 threads consuming the most memory for 2048x2048 matrices. While these techniques excel at parallelism, their higher memory requirements make them less suitable for resource-limited environments.

### Core Utilization
The analysis of core utilization revealed that parallel implementations scaled well with the number of threads, effectively leveraging up to 12 cores in a system with 14 physical and 20 logical cores. However, increasing the number of threads beyond 8 did not always result in proportional gains in performance. Configurations with fewer threads, such as parallelExecutors with 8 threads, achieved optimal core utilization without incurring excessive overhead.

## Comparative Observations
The results emphasize that no single technique is universally optimal. The choice of implementation depends on specific use cases:

- ParallelExecutors with 8 threads offers a balanced solution for systems requiring efficient resource usage and moderate computational demands. 

- The vectorized implementation excels in high-performance scenarios, achieving superior speedup and low resource consumption, making it ideal for large-scale matrix operations.

## Conclusion
This project demonstrates the transformative potential of parallel and vectorized computing in matrix multiplication. The analysis highlights how each technique—whether parallel or vectorized—brings unique strengths and limitations that must be carefully considered when selecting the optimal approach for a given system or application.

Parallel implementations with a moderate number of threads provide robust performance for systems with multicore processors, while the vectorized implementation stands as the most efficient solution, maximizing speedup and resource utilization. These findings provide valuable insights into the capabilities and trade-offs of modern hardware optimization techniques, offering a foundation for future research and practical applications in high-performance computing.