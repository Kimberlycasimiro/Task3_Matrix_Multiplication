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

public class CoresUsedChart {

    public static void main(String[] args) {
        String csvFilePath = "results/benchmark_results.csv";
        String outputChartPath = "output_charts/cores_used_chart.png";

        generateCoresUsedChart(csvFilePath, outputChartPath);
    }

    public static void generateCoresUsedChart(String csvFilePath, String outputChartPath) {
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
                    String implementation = line[2].trim().replace("org.ulpgc.benchmarks.BenchmarkRunner.", "");
                    int threadsUsed = Integer.parseInt(line[1].trim());
                    int coresUsed = Integer.parseInt(line[8].trim());

                    if (implementation.equals("parallelExecutors") || implementation.equals("parallelThreads")) {
                        String seriesKey = implementation;
                        dataset.addValue(coresUsed, seriesKey, Integer.toString(threadsUsed));
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Error processing a line from the CSV file: " + String.join(";", line));
                }
            }

            JFreeChart chart = ChartFactory.createLineChart(
                    "Cores Used vs Threads Used",
                    "Threads Used",
                    "Cores Used",
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
            legend.setFrame(new BlockBorder(Color.GRAY));
            legend.setBackgroundPaint(new Color(245, 245, 245));
            legend.setPosition(RectangleEdge.BOTTOM);

            CategoryPlot plot = chart.getCategoryPlot();
            plot.setBackgroundPaint(Color.WHITE);
            plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
            plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

            LineAndShapeRenderer renderer = new LineAndShapeRenderer();
            Color[] colors = {
                    new Color(0, 102, 204),
                    new Color(255, 153, 51)
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
            System.out.println("Cores Used chart generated at: " + outputChartPath);

        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
            throw new RuntimeException("Error processing the CSV file", e);
        }
    }
}