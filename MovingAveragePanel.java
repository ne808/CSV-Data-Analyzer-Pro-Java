package analyzer;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.*;

/**
 * @author Lukasz Golinski
 */
public class MovingAveragePanel extends JPanel {
    
    private StatisticsEngine engine;
    private JTable maTable;
    private DefaultTableModel tableModel;
    private JSpinner windowSizeSpinner;
    private JSpinner emaSmoothingSpinner;
    private JComboBox<String> maTypeCombo;
    private JLabel summaryLabel;
    private JButton calculateButton;
    private DecimalFormat formatter;
    
    private double[] originalData;
    private double[] currentMovingAverage;
    
    public interface MovingAverageListener {
        void onMovingAverageCalculated(double[] originalData, double[] maData, String maType, int window);
    }
    
    private MovingAverageListener listener;
    
    public MovingAveragePanel() {
        engine = new StatisticsEngine();
        formatter = new DecimalFormat("#,##0.######");
        originalData = new double[0];
        currentMovingAverage = new double[0];
        
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(new Color(248, 249, 250));
        
        initializeComponents();
    }
    
    private void initializeComponents() {
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("Moving Average Analysis");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        titleLabel.setForeground(new Color(44, 62, 80));
        
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        controlsPanel.setOpaque(false);
        
        JLabel typeLabel = new JLabel("Type:");
        typeLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        controlsPanel.add(typeLabel);
        
        maTypeCombo = new JComboBox<>(new String[]{
            "Simple Moving Average (SMA)",
            "Exponential Moving Average (EMA)",
            "Weighted Moving Average (WMA)",
            "4-Point Moving Average"
        });
        maTypeCombo.setPreferredSize(new Dimension(200, 25));
        maTypeCombo.addActionListener(e -> updateControlVisibility());
        controlsPanel.add(maTypeCombo);
        
        JLabel windowLabel = new JLabel("Window:");
        windowLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        controlsPanel.add(windowLabel);
        
        SpinnerNumberModel windowModel = new SpinnerNumberModel(4, 2, 100, 1);
        windowSizeSpinner = new JSpinner(windowModel);
        windowSizeSpinner.setPreferredSize(new Dimension(60, 25));
        controlsPanel.add(windowSizeSpinner);
        
        JLabel smoothLabel = new JLabel("EMA Smoothing:");
        smoothLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        controlsPanel.add(smoothLabel);
        
        SpinnerNumberModel smoothModel = new SpinnerNumberModel(0.3, 0.01, 0.99, 0.05);
        emaSmoothingSpinner = new JSpinner(smoothModel);
        emaSmoothingSpinner.setPreferredSize(new Dimension(70, 25));
        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(emaSmoothingSpinner, "0.00");
        emaSmoothingSpinner.setEditor(editor);
        controlsPanel.add(emaSmoothingSpinner);
        
        calculateButton = new JButton("Calculate");
        calculateButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        calculateButton.setBackground(new Color(52, 152, 219));
        calculateButton.setForeground(Color.WHITE);
        calculateButton.setFocusPainted(false);
        calculateButton.setBorder(BorderFactory.createEmptyBorder(6, 15, 6, 15));
        calculateButton.addActionListener(e -> calculateMovingAverage());
        controlsPanel.add(calculateButton);
        
        topPanel.add(titleLabel, BorderLayout.NORTH);
        topPanel.add(controlsPanel, BorderLayout.CENTER);
        
        add(topPanel, BorderLayout.NORTH);
        
        String[] columns = {"Index", "Original Value", "Moving Average", "Difference", "% Change"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        maTable = new JTable(tableModel);
        maTable.setFont(new Font("SansSerif", Font.PLAIN, 12));
        maTable.setRowHeight(24);
        maTable.setShowGrid(true);
        maTable.setGridColor(new Color(230, 230, 230));
        
        JTableHeader header = maTable.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 12));
        header.setBackground(new Color(52, 73, 94));
        header.setForeground(Color.WHITE);
        
        maTable.setDefaultRenderer(Object.class, new MovingAverageCellRenderer());
        
        for (int i = 0; i < maTable.getColumnCount(); i++) {
            maTable.getColumnModel().getColumn(i).setPreferredWidth(100);
        }
        
        JScrollPane scrollPane = new JScrollPane(maTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        add(scrollPane, BorderLayout.CENTER);
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        
        summaryLabel = new JLabel("Load data and calculate moving averages to see results");
        summaryLabel.setFont(new Font("SansSerif", Font.ITALIC, 11));
        summaryLabel.setForeground(Color.GRAY);
        
        bottomPanel.add(summaryLabel, BorderLayout.WEST);
        
        add(bottomPanel, BorderLayout.SOUTH);
        
        updateControlVisibility();
    }
    
    private void updateControlVisibility() {
        String selected = (String) maTypeCombo.getSelectedItem();
        
        if (selected.contains("EMA")) {
            emaSmoothingSpinner.setEnabled(true);
            windowSizeSpinner.setEnabled(false);
        } else if (selected.contains("4-Point")) {
            emaSmoothingSpinner.setEnabled(false);
            windowSizeSpinner.setEnabled(false);
            windowSizeSpinner.setValue(4);
        } else {
            emaSmoothingSpinner.setEnabled(false);
            windowSizeSpinner.setEnabled(true);
        }
    }
    
    public void setData(double[] data) {
        this.originalData = data != null ? Arrays.copyOf(data, data.length) : new double[0];
        engine.loadData(originalData);
        tableModel.setRowCount(0);
        currentMovingAverage = new double[0];
        summaryLabel.setText("Data loaded (" + originalData.length + " values). Select type and calculate.");
    }
    
    private void calculateMovingAverage() {
        if (originalData.length == 0) {
            JOptionPane.showMessageDialog(this, 
                "No data loaded. Please load a CSV file first.",
                "No Data",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String maType = (String) maTypeCombo.getSelectedItem();
        int windowSize = (Integer) windowSizeSpinner.getValue();
        double smoothing = (Double) emaSmoothingSpinner.getValue();
        
        double[] maValues;
        
        if (maType.contains("EMA")) {
            maValues = engine.getExponentialMovingAverage(smoothing);
        } else if (maType.contains("WMA")) {
            maValues = engine.getWeightedMovingAverage(windowSize);
        } else if (maType.contains("4-Point")) {
            maValues = engine.getFourPointMovingAverage();
        } else {
            maValues = engine.getMovingAverage(windowSize);
        }
        
        currentMovingAverage = maValues;
        
        populateTable(maValues, maType, windowSize);
        
        if (listener != null) {
            listener.onMovingAverageCalculated(originalData, maValues, maType, windowSize);
        }
    }
    
    private void populateTable(double[] maValues, String maType, int windowSize) {
        tableModel.setRowCount(0);
        
        int offset = 0;
        if (maType.contains("SMA") || maType.contains("WMA") || maType.contains("4-Point")) {
            offset = (originalData.length - maValues.length) / 2;
            if (maType.contains("4-Point")) {
                offset = 1;
            } else {
                offset = windowSize / 2;
            }
        }
        
        double sumDiff = 0;
        double sumAbsDiff = 0;
        int compareCount = 0;
        
        for (int i = 0; i < originalData.length; i++) {
            String indexStr = String.valueOf(i + 1);
            String originalStr = formatter.format(originalData[i]);
            String maStr = "";
            String diffStr = "";
            String pctChangeStr = "";
            
            int maIndex = -1;
            
            if (maType.contains("EMA")) {
                maIndex = i;
            } else {
                maIndex = i - offset;
                if (maIndex < 0 || maIndex >= maValues.length) {
                    maIndex = -1;
                }
            }
            
            if (maIndex >= 0 && maIndex < maValues.length) {
                double maVal = maValues[maIndex];
                maStr = formatter.format(maVal);
                
                double diff = originalData[i] - maVal;
                diffStr = formatter.format(diff);
                
                if (maVal != 0) {
                    double pctChange = (diff / maVal) * 100;
                    pctChangeStr = formatter.format(pctChange) + "%";
                }
                
                sumDiff += diff;
                sumAbsDiff += Math.abs(diff);
                compareCount++;
            }
            
            tableModel.addRow(new Object[]{indexStr, originalStr, maStr, diffStr, pctChangeStr});
        }
        
        StringBuilder summary = new StringBuilder();
        summary.append(maType);
        if (!maType.contains("EMA")) {
            summary.append(" (window=").append(windowSize).append(")");
        } else {
            summary.append(" (α=").append(emaSmoothingSpinner.getValue()).append(")");
        }
        summary.append(" | MA Values: ").append(maValues.length);
        
        if (compareCount > 0) {
            double avgDiff = sumDiff / compareCount;
            double mae = sumAbsDiff / compareCount;
            summary.append(" | Avg Diff: ").append(formatter.format(avgDiff));
            summary.append(" | MAE: ").append(formatter.format(mae));
        }
        
        summaryLabel.setText(summary.toString());
    }
    
    public void setMovingAverageListener(MovingAverageListener listener) {
        this.listener = listener;
    }
    
    public double[] getCurrentMovingAverage() {
        return currentMovingAverage;
    }
    
    public void clearData() {
        originalData = new double[0];
        currentMovingAverage = new double[0];
        tableModel.setRowCount(0);
        summaryLabel.setText("Load data and calculate moving averages to see results");
    }
    
    private class MovingAverageCellRenderer extends DefaultTableCellRenderer {
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            Component c = super.getTableCellRendererComponent(table, value, 
                    isSelected, hasFocus, row, column);
            
            if (!isSelected) {
                if (row % 2 == 0) {
                    c.setBackground(Color.WHITE);
                } else {
                    c.setBackground(new Color(248, 249, 250));
                }
                
                if (column == 3 && value != null && !value.toString().isEmpty()) {
                    try {
                        String valStr = value.toString().replace(",", "");
                        double diff = Double.parseDouble(valStr);
                        if (diff > 0) {
                            c.setForeground(new Color(39, 174, 96));
                        } else if (diff < 0) {
                            c.setForeground(new Color(192, 57, 43));
                        } else {
                            c.setForeground(Color.BLACK);
                        }
                    } catch (NumberFormatException ex) {
                        c.setForeground(Color.BLACK);
                    }
                } else if (column == 4 && value != null && !value.toString().isEmpty()) {
                    try {
                        String valStr = value.toString().replace("%", "").replace(",", "");
                        double pct = Double.parseDouble(valStr);
                        if (pct > 0) {
                            c.setForeground(new Color(39, 174, 96));
                        } else if (pct < 0) {
                            c.setForeground(new Color(192, 57, 43));
                        } else {
                            c.setForeground(Color.BLACK);
                        }
                    } catch (NumberFormatException ex) {
                        c.setForeground(Color.BLACK);
                    }
                } else {
                    c.setForeground(Color.BLACK);
                }
            }
            
            setHorizontalAlignment(column == 0 ? SwingConstants.CENTER : SwingConstants.RIGHT);
            setFont(new Font("Monospaced", Font.PLAIN, 12));
            setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
            
            return c;
        }
    }
}
