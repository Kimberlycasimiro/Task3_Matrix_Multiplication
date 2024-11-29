package org.ulpgc.charts;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.chart.ui.RectangleEdge;

import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class MemoryUsageChart {

    public static void main(String[] args) {
        String csvFilePath = "results/benchmark_results.csv";
        String outputChartPath = "output_charts/memory_usage_chart.png";

        generateMemoryUsageChart(csvFilePath, outputChartPath);
    }

    public static void generateMemoryUsageChart(String csvFilePath, String outputChartPath) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        try (CSVReader csvReader = new CSVReaderBuilder(
                new FileReader(csvFilePath))
                .withCSVParser(new CSVParserBuilder().withSeparator(';').build())
                .build()) {

            String[] headers = csvReader.readNext();
            if (headers == null || headers.length == 0) {
                throw new IllegalArgumentException("The CSV file is empty or does not have headers.");
            }

            String[] line;
            while ((line = csvReader.readNext()) != null) {
                try {
                    int matrixSize = Integer.parseInt(line[0].trim());
                    String implementation = line[2].trim().replace("org.ulpgc.benchmarks.BenchmarkRunner.", "");
                    int threadsUsed = Integer.parseInt(line[1].trim());
                    double memoryUsed = Double.parseDouble(line[6].replace(",", "."));

                    String seriesKey;
                    if (implementation.equals("parallelExecutors") || implementation.equals("parallelThreads")) {
                        seriesKey = implementation + " (Threads: " + threadsUsed + ")";
                    } else {
                        seriesKey = implementation;
                    }

                    dataset.addValue(memoryUsed, seriesKey, Integer.toString(matrixSize));
                } catch (NumberFormatException e) {
                    System.err.println("Error processing a line from the CSV file: " + String.join(";", line));
                }
            }

            JFreeChart chart = ChartFactory.createLineChart(
                    "Memory Usage vs Matrix Size",
                    "Matrix Size",
                    "Memory Usage (MB)",
                    dataset,
                    PlotOrientation.VERTICAL,
                    true,
                    true,
                    false
            );

            chart.setBackgroundPaint(Color.WHITE);

            Font timesNewRoman = new Font("Times New Roman", Font.PLAIN, 14);
            chart.getTitle().setFont(new Font("Times New Roman", Font.BOLD, 18));
            chart.getLegend().setItemFont(timesNewRoman);

            LegendTitle legend = chart.getLegend();
            legend.setFrame(new BlockBorder(Color.BLACK));
            legend.setBackgroundPaint(new Color(245, 245, 245));
            legend.setPosition(RectangleEdge.BOTTOM);

            CategoryPlot plot = chart.getCategoryPlot();
            plot.setBackgroundPaint(Color.WHITE);
            plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
            plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

            LineAndShapeRenderer renderer = new LineAndShapeRenderer();
            Color[] colors = {
                    new Color(0, 51, 102),
                    new Color(153, 0, 0),
                    new Color(0, 102, 0),
                    new Color(255, 204, 0),
                    new Color(102, 51, 0),
                    new Color(0, 102, 204),
                    new Color(153, 51, 102),
                    new Color(204, 102, 0),
                    new Color(0, 153, 153),
                    new Color(255, 102, 102),
                    new Color(186, 85, 211),
                    new Color(102, 153, 0),
                    new Color(153, 153, 255),
                    new Color(255, 153, 51),
                    new Color(0, 153, 51),
                    new Color(204, 0, 204)
            };

            for (int i = 0; i < dataset.getRowCount(); i++) {
                renderer.setSeriesPaint(i, colors[i % colors.length]);
            }
            plot.setRenderer(renderer);

            plot.getDomainAxis().setLabelFont(timesNewRoman);
            plot.getDomainAxis().setTickLabelFont(timesNewRoman);
            plot.getRangeAxis().setLabelFont(timesNewRoman);
            plot.getRangeAxis().setTickLabelFont(timesNewRoman);

            ChartUtils.saveChartAsPNG(new File(outputChartPath), chart, 1200, 800);
            System.out.println("Chart generated at: " + outputChartPath);

        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
            throw new RuntimeException("Error processing the CSV file", e);
        }
    }
}
