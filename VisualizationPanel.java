package analyzer;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.text.DecimalFormat;
import java.util.*;

/**
 * @author Lukasz Golinski
 */
public class VisualizationPanel extends JPanel {
    
    public enum ChartType {
        LINE_CHART,
        BAR_CHART,
        HISTOGRAM,
        SCATTER_PLOT,
        MOVING_AVERAGE_OVERLAY
    }
    
    private double[] primaryData;
    private double[] secondaryData;
    private String chartTitle;
    private String xAxisLabel;
    private String yAxisLabel;
    private ChartType currentType;
    private Color primaryColor;
    private Color secondaryColor;
    private Color gridColor;
    private Color axisColor;
    
    private static final int PADDING_LEFT = 70;
    private static final int PADDING_RIGHT = 30;
    private static final int PADDING_TOP = 50;
    private static final int PADDING_BOTTOM = 50;
    private static final int TICK_COUNT = 8;
    
    private DecimalFormat formatter;
    
    public VisualizationPanel() {
        this.primaryData = new double[0];
        this.secondaryData = new double[0];
        this.chartTitle = "Data Visualization";
        this.xAxisLabel = "Index";
        this.yAxisLabel = "Value";
        this.currentType = ChartType.LINE_CHART;
        this.primaryColor = new Color(41, 128, 185);
        this.secondaryColor = new Color(231, 76, 60);
        this.gridColor = new Color(220, 220, 220);
        this.axisColor = new Color(80, 80, 80);
        this.formatter = new DecimalFormat("#,##0.##");
        
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(600, 400));
    }
    
    public void setData(double[] data) {
        this.primaryData = data != null ? Arrays.copyOf(data, data.length) : new double[0];
        repaint();
    }
    
    public void setSecondaryData(double[] data) {
        this.secondaryData = data != null ? Arrays.copyOf(data, data.length) : new double[0];
        repaint();
    }
    
    public void setChartType(ChartType type) {
        this.currentType = type;
        repaint();
    }
    
    public void setChartTitle(String title) {
        this.chartTitle = title;
        repaint();
    }
    
    public void setAxisLabels(String xLabel, String yLabel) {
        this.xAxisLabel = xLabel;
        this.yAxisLabel = yLabel;
        repaint();
    }
    
    public void setPrimaryColor(Color color) {
        this.primaryColor = color;
        repaint();
    }
    
    public void setSecondaryColor(Color color) {
        this.secondaryColor = color;
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        
        int width = getWidth();
        int height = getHeight();
        
        int chartWidth = width - PADDING_LEFT - PADDING_RIGHT;
        int chartHeight = height - PADDING_TOP - PADDING_BOTTOM;
        
        if (primaryData.length == 0) {
            drawNoDataMessage(g2d, width, height);
            return;
        }
        
        drawTitle(g2d, width);
        drawGrid(g2d, chartWidth, chartHeight);
        drawAxes(g2d, chartWidth, chartHeight);
        
        switch (currentType) {
            case LINE_CHART:
            case MOVING_AVERAGE_OVERLAY:
                drawLineChart(g2d, chartWidth, chartHeight);
                break;
            case BAR_CHART:
                drawBarChart(g2d, chartWidth, chartHeight);
                break;
            case HISTOGRAM:
                drawHistogram(g2d, chartWidth, chartHeight);
                break;
            case SCATTER_PLOT:
                drawScatterPlot(g2d, chartWidth, chartHeight);
                break;
        }
        
        if (secondaryData.length > 0 && currentType == ChartType.MOVING_AVERAGE_OVERLAY) {
            drawSecondaryLine(g2d, chartWidth, chartHeight);
            drawLegend(g2d, width);
        }
    }
    
    private void drawNoDataMessage(Graphics2D g2d, int width, int height) {
        g2d.setColor(Color.GRAY);
        g2d.setFont(new Font("SansSerif", Font.ITALIC, 16));
        String message = "No data to display. Load a CSV file to begin analysis.";
        FontMetrics fm = g2d.getFontMetrics();
        int msgWidth = fm.stringWidth(message);
        g2d.drawString(message, (width - msgWidth) / 2, height / 2);
    }
    
    private void drawTitle(Graphics2D g2d, int width) {
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 16));
        FontMetrics fm = g2d.getFontMetrics();
        int titleWidth = fm.stringWidth(chartTitle);
        g2d.drawString(chartTitle, (width - titleWidth) / 2, 30);
    }
    
    private void drawGrid(Graphics2D g2d, int chartWidth, int chartHeight) {
        g2d.setColor(gridColor);
        g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 
                                       10, new float[]{3, 3}, 0));
        
        for (int i = 0; i <= TICK_COUNT; i++) {
            int y = PADDING_TOP + (i * chartHeight / TICK_COUNT);
            g2d.drawLine(PADDING_LEFT, y, PADDING_LEFT + chartWidth, y);
        }
        
        for (int i = 0; i <= TICK_COUNT; i++) {
            int x = PADDING_LEFT + (i * chartWidth / TICK_COUNT);
            g2d.drawLine(x, PADDING_TOP, x, PADDING_TOP + chartHeight);
        }
        
        g2d.setStroke(new BasicStroke(1));
    }
    
    private void drawAxes(Graphics2D g2d, int chartWidth, int chartHeight) {
        g2d.setColor(axisColor);
        g2d.setStroke(new BasicStroke(2));
        
        g2d.drawLine(PADDING_LEFT, PADDING_TOP + chartHeight, 
                     PADDING_LEFT + chartWidth, PADDING_TOP + chartHeight);
        g2d.drawLine(PADDING_LEFT, PADDING_TOP, 
                     PADDING_LEFT, PADDING_TOP + chartHeight);
        
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 11));
        
        double minVal = getMinValue(primaryData);
        double maxVal = getMaxValue(primaryData);
        
        if (minVal == maxVal) {
            minVal -= 1;
            maxVal += 1;
        }
        
        double padding = (maxVal - minVal) * 0.1;
        minVal -= padding;
        maxVal += padding;
        
        for (int i = 0; i <= TICK_COUNT; i++) {
            double value = maxVal - (i * (maxVal - minVal) / TICK_COUNT);
            int y = PADDING_TOP + (i * chartHeight / TICK_COUNT);
            
            g2d.setColor(axisColor);
            g2d.drawLine(PADDING_LEFT - 5, y, PADDING_LEFT, y);
            
            String label = formatter.format(value);
            FontMetrics fm = g2d.getFontMetrics();
            int labelWidth = fm.stringWidth(label);
            g2d.drawString(label, PADDING_LEFT - labelWidth - 8, y + 4);
        }
        
        for (int i = 0; i <= TICK_COUNT; i++) {
            int x = PADDING_LEFT + (i * chartWidth / TICK_COUNT);
            int index = (int) ((double) i / TICK_COUNT * (primaryData.length - 1));
            
            g2d.setColor(axisColor);
            g2d.drawLine(x, PADDING_TOP + chartHeight, x, PADDING_TOP + chartHeight + 5);
            
            String label = String.valueOf(index);
            FontMetrics fm = g2d.getFontMetrics();
            int labelWidth = fm.stringWidth(label);
            g2d.drawString(label, x - labelWidth / 2, PADDING_TOP + chartHeight + 18);
        }
        
        g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
        FontMetrics fm = g2d.getFontMetrics();
        
        int xLabelWidth = fm.stringWidth(xAxisLabel);
        g2d.drawString(xAxisLabel, 
                       PADDING_LEFT + (chartWidth - xLabelWidth) / 2, 
                       getHeight() - 10);
        
        AffineTransform original = g2d.getTransform();
        g2d.rotate(-Math.PI / 2);
        int yLabelWidth = fm.stringWidth(yAxisLabel);
        g2d.drawString(yAxisLabel, 
                       -(PADDING_TOP + chartHeight / 2 + yLabelWidth / 2), 
                       15);
        g2d.setTransform(original);
    }
    
    private void drawLineChart(Graphics2D g2d, int chartWidth, int chartHeight) {
        if (primaryData.length < 2) return;
        
        double minVal = getMinValue(primaryData);
        double maxVal = getMaxValue(primaryData);
        
        if (minVal == maxVal) {
            minVal -= 1;
            maxVal += 1;
        }
        
        double padding = (maxVal - minVal) * 0.1;
        minVal -= padding;
        maxVal += padding;
        
        g2d.setColor(primaryColor);
        g2d.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        
        Path2D.Double path = new Path2D.Double();
        boolean first = true;
        
        for (int i = 0; i < primaryData.length; i++) {
            double xRatio = (double) i / (primaryData.length - 1);
            double yRatio = (primaryData[i] - minVal) / (maxVal - minVal);
            
            int x = PADDING_LEFT + (int) (xRatio * chartWidth);
            int y = PADDING_TOP + chartHeight - (int) (yRatio * chartHeight);
            
            if (first) {
                path.moveTo(x, y);
                first = false;
            } else {
                path.lineTo(x, y);
            }
        }
        
        g2d.draw(path);
        
        g2d.setStroke(new BasicStroke(1));
        for (int i = 0; i < primaryData.length; i++) {
            double xRatio = (double) i / (primaryData.length - 1);
            double yRatio = (primaryData[i] - minVal) / (maxVal - minVal);
            
            int x = PADDING_LEFT + (int) (xRatio * chartWidth);
            int y = PADDING_TOP + chartHeight - (int) (yRatio * chartHeight);
            
            g2d.setColor(Color.WHITE);
            g2d.fillOval(x - 4, y - 4, 8, 8);
            g2d.setColor(primaryColor);
            g2d.drawOval(x - 4, y - 4, 8, 8);
        }
    }
    
    private void drawSecondaryLine(Graphics2D g2d, int chartWidth, int chartHeight) {
        if (secondaryData.length < 2) return;
        
        double minVal = Math.min(getMinValue(primaryData), getMinValue(secondaryData));
        double maxVal = Math.max(getMaxValue(primaryData), getMaxValue(secondaryData));
        
        if (minVal == maxVal) {
            minVal -= 1;
            maxVal += 1;
        }
        
        double padding = (maxVal - minVal) * 0.1;
        minVal -= padding;
        maxVal += padding;
        
        g2d.setColor(secondaryColor);
        g2d.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        
        Path2D.Double path = new Path2D.Double();
        boolean first = true;
        
        int offset = (primaryData.length - secondaryData.length) / 2;
        
        for (int i = 0; i < secondaryData.length; i++) {
            double xRatio = (double) (i + offset) / (primaryData.length - 1);
            double yRatio = (secondaryData[i] - minVal) / (maxVal - minVal);
            
            int x = PADDING_LEFT + (int) (xRatio * chartWidth);
            int y = PADDING_TOP + chartHeight - (int) (yRatio * chartHeight);
            
            if (first) {
                path.moveTo(x, y);
                first = false;
            } else {
                path.lineTo(x, y);
            }
        }
        
        g2d.draw(path);
    }
    
    private void drawBarChart(Graphics2D g2d, int chartWidth, int chartHeight) {
        if (primaryData.length == 0) return;
        
        double minVal = Math.min(0, getMinValue(primaryData));
        double maxVal = getMaxValue(primaryData);
        
        if (minVal == maxVal) {
            maxVal += 1;
        }
        
        double padding = (maxVal - minVal) * 0.1;
        maxVal += padding;
        
        int barWidth = Math.max(2, (chartWidth - 20) / primaryData.length - 2);
        int spacing = (chartWidth - (barWidth * primaryData.length)) / (primaryData.length + 1);
        
        double zeroY = PADDING_TOP + chartHeight - (int) ((0 - minVal) / (maxVal - minVal) * chartHeight);
        
        for (int i = 0; i < primaryData.length; i++) {
            double yRatio = (primaryData[i] - minVal) / (maxVal - minVal);
            
            int x = PADDING_LEFT + spacing + i * (barWidth + spacing);
            int barHeight = (int) (yRatio * chartHeight);
            int y = PADDING_TOP + chartHeight - barHeight;
            
            GradientPaint gradient = new GradientPaint(
                x, y, primaryColor.brighter(),
                x + barWidth, y + barHeight, primaryColor.darker()
            );
            g2d.setPaint(gradient);
            g2d.fillRect(x, y, barWidth, barHeight);
            
            g2d.setColor(primaryColor.darker());
            g2d.drawRect(x, y, barWidth, barHeight);
        }
    }
    
    private void drawHistogram(Graphics2D g2d, int chartWidth, int chartHeight) {
        if (primaryData.length == 0) return;
        
        int binCount = Math.min(20, (int) Math.sqrt(primaryData.length));
        binCount = Math.max(5, binCount);
        
        double minVal = getMinValue(primaryData);
        double maxVal = getMaxValue(primaryData);
        
        if (minVal == maxVal) {
            minVal -= 1;
            maxVal += 1;
        }
        
        double binWidth = (maxVal - minVal) / binCount;
        int[] frequencies = new int[binCount];
        
        for (double val : primaryData) {
            int binIndex = (int) ((val - minVal) / binWidth);
            if (binIndex >= binCount) binIndex = binCount - 1;
            if (binIndex < 0) binIndex = 0;
            frequencies[binIndex]++;
        }
        
        int maxFreq = 0;
        for (int freq : frequencies) {
            if (freq > maxFreq) maxFreq = freq;
        }
        
        int barWidth = chartWidth / binCount;
        
        for (int i = 0; i < binCount; i++) {
            double heightRatio = (double) frequencies[i] / maxFreq;
            int barHeight = (int) (heightRatio * chartHeight);
            
            int x = PADDING_LEFT + i * barWidth;
            int y = PADDING_TOP + chartHeight - barHeight;
            
            GradientPaint gradient = new GradientPaint(
                x, y, new Color(52, 152, 219),
                x + barWidth, y + barHeight, new Color(41, 128, 185)
            );
            g2d.setPaint(gradient);
            g2d.fillRect(x, y, barWidth - 1, barHeight);
            
            g2d.setColor(new Color(30, 100, 150));
            g2d.drawRect(x, y, barWidth - 1, barHeight);
        }
    }
    
    private void drawScatterPlot(Graphics2D g2d, int chartWidth, int chartHeight) {
        if (primaryData.length == 0) return;
        
        double minVal = getMinValue(primaryData);
        double maxVal = getMaxValue(primaryData);
        
        if (minVal == maxVal) {
            minVal -= 1;
            maxVal += 1;
        }
        
        double padding = (maxVal - minVal) * 0.1;
        minVal -= padding;
        maxVal += padding;
        
        for (int i = 0; i < primaryData.length; i++) {
            double xRatio = (double) i / (primaryData.length - 1);
            double yRatio = (primaryData[i] - minVal) / (maxVal - minVal);
            
            int x = PADDING_LEFT + (int) (xRatio * chartWidth);
            int y = PADDING_TOP + chartHeight - (int) (yRatio * chartHeight);
            
            g2d.setColor(new Color(primaryColor.getRed(), primaryColor.getGreen(), 
                                   primaryColor.getBlue(), 180));
            g2d.fillOval(x - 5, y - 5, 10, 10);
            
            g2d.setColor(primaryColor.darker());
            g2d.drawOval(x - 5, y - 5, 10, 10);
        }
    }
    
    private void drawLegend(Graphics2D g2d, int width) {
        int legendX = width - 180;
        int legendY = 15;
        
        g2d.setColor(new Color(255, 255, 255, 230));
        g2d.fillRoundRect(legendX - 10, legendY - 5, 170, 50, 10, 10);
        g2d.setColor(Color.GRAY);
        g2d.drawRoundRect(legendX - 10, legendY - 5, 170, 50, 10, 10);
        
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 11));
        
        g2d.setColor(primaryColor);
        g2d.fillRect(legendX, legendY + 5, 20, 10);
        g2d.setColor(Color.BLACK);
        g2d.drawString("Original Data", legendX + 28, legendY + 14);
        
        g2d.setColor(secondaryColor);
        g2d.fillRect(legendX, legendY + 25, 20, 10);
        g2d.setColor(Color.BLACK);
        g2d.drawString("Moving Average", legendX + 28, legendY + 34);
    }
    
    private double getMinValue(double[] data) {
        if (data.length == 0) return 0;
        double min = data[0];
        for (double val : data) {
            if (val < min) min = val;
        }
        return min;
    }
    
    private double getMaxValue(double[] data) {
        if (data.length == 0) return 0;
        double max = data[0];
        for (double val : data) {
            if (val > max) max = val;
        }
        return max;
    }
    
    public void clearChart() {
        primaryData = new double[0];
        secondaryData = new double[0];
        chartTitle = "Data Visualization";
        repaint();
    }
}
