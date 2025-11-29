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
public class ResultsPanel extends JPanel {
    
    private JTable resultsTable;
    private DefaultTableModel tableModel;
    private JLabel statusLabel;
    private JTextArea interpretationArea;
    private DecimalFormat primaryFormatter;
    private DecimalFormat scientificFormatter;
    
    public ResultsPanel() {
        primaryFormatter = new DecimalFormat("#,##0.######");
        scientificFormatter = new DecimalFormat("0.####E0");
        
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(new Color(248, 249, 250));
        
        initializeComponents();
    }
    
    private void initializeComponents() {
        JPanel headerPanel = new JPanel(new BorderLayout(5, 5));
        headerPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("Statistical Analysis Results");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        titleLabel.setForeground(new Color(44, 62, 80));
        
        statusLabel = new JLabel("No data loaded");
        statusLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
        statusLabel.setForeground(Color.GRAY);
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(statusLabel, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);
        
        String[] columnNames = {"Category", "Statistic", "Value"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        resultsTable = new JTable(tableModel);
        resultsTable.setFont(new Font("SansSerif", Font.PLAIN, 12));
        resultsTable.setRowHeight(26);
        resultsTable.setShowGrid(true);
        resultsTable.setGridColor(new Color(220, 220, 220));
        resultsTable.setSelectionBackground(new Color(52, 152, 219, 100));
        
        resultsTable.getColumnModel().getColumn(0).setPreferredWidth(130);
        resultsTable.getColumnModel().getColumn(1).setPreferredWidth(180);
        resultsTable.getColumnModel().getColumn(2).setPreferredWidth(140);
        
        JTableHeader header = resultsTable.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 12));
        header.setBackground(new Color(52, 73, 94));
        header.setForeground(Color.WHITE);
        header.setReorderingAllowed(false);
        
        resultsTable.setDefaultRenderer(Object.class, new CategoryCellRenderer());
        
        JScrollPane scrollPane = new JScrollPane(resultsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        add(scrollPane, BorderLayout.CENTER);
        
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        bottomPanel.setOpaque(false);
        
        JLabel interpretLabel = new JLabel("Analysis Interpretation:");
        interpretLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        interpretLabel.setForeground(new Color(44, 62, 80));
        
        interpretationArea = new JTextArea(4, 30);
        interpretationArea.setEditable(false);
        interpretationArea.setFont(new Font("SansSerif", Font.PLAIN, 11));
        interpretationArea.setLineWrap(true);
        interpretationArea.setWrapStyleWord(true);
        interpretationArea.setBackground(new Color(255, 255, 230));
        interpretationArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 180)),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        interpretationArea.setText("Load a CSV file and select a column to see analysis interpretation.");
        
        bottomPanel.add(interpretLabel, BorderLayout.NORTH);
        bottomPanel.add(new JScrollPane(interpretationArea), BorderLayout.CENTER);
        
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    public void displayResults(StatisticsEngine engine, String columnName) {
        tableModel.setRowCount(0);
        
        if (!engine.hasData()) {
            statusLabel.setText("No data available");
            interpretationArea.setText("No data to analyze.");
            return;
        }
        
        Map<String, Double> results = engine.getFullAnalysis();
        
        addCategoryRow("Basic Stats", "Count", formatValue(results.get("Count")));
        addCategoryRow("Basic Stats", "Sum", formatValue(results.get("Sum")));
        addCategoryRow("Basic Stats", "Minimum", formatValue(results.get("Minimum")));
        addCategoryRow("Basic Stats", "Maximum", formatValue(results.get("Maximum")));
        addCategoryRow("Basic Stats", "Range", formatValue(results.get("Range")));
        
        addCategoryRow("Central Tendency", "Arithmetic Mean", formatValue(results.get("Mean")));
        addCategoryRow("Central Tendency", "Median", formatValue(results.get("Median")));
        addCategoryRow("Central Tendency", "Mode", formatValue(results.get("Mode")));
        addCategoryRow("Central Tendency", "Mode Frequency", formatValue(results.get("Mode Frequency")));
        addCategoryRow("Central Tendency", "Geometric Mean", formatValue(results.get("Geometric Mean")));
        addCategoryRow("Central Tendency", "Harmonic Mean", formatValue(results.get("Harmonic Mean")));
        
        addCategoryRow("Dispersion", "Variance (Sample)", formatValue(results.get("Variance (Sample)")));
        addCategoryRow("Dispersion", "Variance (Population)", formatValue(results.get("Variance (Population)")));
        addCategoryRow("Dispersion", "Std Dev (Sample)", formatValue(results.get("Std Dev (Sample)")));
        addCategoryRow("Dispersion", "Std Dev (Population)", formatValue(results.get("Std Dev (Population)")));
        addCategoryRow("Dispersion", "Standard Error", formatValue(results.get("Standard Error")));
        addCategoryRow("Dispersion", "Coeff of Variation %", formatValue(results.get("Coeff of Variation %")));
        addCategoryRow("Dispersion", "Mean Abs Deviation", formatValue(results.get("Mean Abs Deviation")));
        
        addCategoryRow("Quartiles", "Q1 (25th Percentile)", formatValue(results.get("Quartile 1 (25%)")));
        addCategoryRow("Quartiles", "Q2 (50th Percentile)", formatValue(results.get("Quartile 2 (50%)")));
        addCategoryRow("Quartiles", "Q3 (75th Percentile)", formatValue(results.get("Quartile 3 (75%)")));
        addCategoryRow("Quartiles", "Interquartile Range", formatValue(results.get("Interquartile Range")));
        addCategoryRow("Quartiles", "10th Percentile", formatValue(results.get("10th Percentile")));
        addCategoryRow("Quartiles", "90th Percentile", formatValue(results.get("90th Percentile")));
        
        addCategoryRow("Distribution Shape", "Skewness", formatValue(results.get("Skewness")));
        addCategoryRow("Distribution Shape", "Kurtosis", formatValue(results.get("Kurtosis")));
        
        addCategoryRow("Additional", "Root Mean Square", formatValue(results.get("Root Mean Square")));
        addCategoryRow("Additional", "Sum of Squares", formatValue(results.get("Sum of Squares")));
        addCategoryRow("Additional", "Outliers (IQR)", formatValue(results.get("Outliers (IQR method)")));
        addCategoryRow("Additional", "Outliers (Z > 2)", formatValue(results.get("Outliers (Z > 2)")));
        
        statusLabel.setText("Analyzing: " + columnName + " (" + engine.getCount() + " values)");
        
        updateInterpretation(engine, results, columnName);
    }
    
    private void addCategoryRow(String category, String statistic, String value) {
        tableModel.addRow(new Object[]{category, statistic, value});
    }
    
    private String formatValue(Double value) {
        if (value == null) return "N/A";
        
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return "N/A";
        }
        
        double absValue = Math.abs(value);
        
        if (absValue == 0) {
            return "0";
        } else if (absValue >= 1000000 || absValue < 0.0001) {
            return scientificFormatter.format(value);
        } else {
            return primaryFormatter.format(value);
        }
    }
    
    private void updateInterpretation(StatisticsEngine engine, Map<String, Double> results, String columnName) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("Column '").append(columnName).append("' Analysis:\n\n");
        
        double mean = results.get("Mean");
        double median = results.get("Median");
        double stdDev = results.get("Std Dev (Sample)");
        
        sb.append("• Central Value: The data centers around ");
        sb.append(primaryFormatter.format(mean));
        sb.append(" (mean) with a median of ");
        sb.append(primaryFormatter.format(median)).append(".\n");
        
        double meanMedianDiff = Math.abs(mean - median);
        if (mean != 0 && meanMedianDiff / Math.abs(mean) > 0.1) {
            sb.append("• Note: Mean and median differ significantly, suggesting potential outliers or skewness.\n");
        }
        
        sb.append("• Spread: Standard deviation is ");
        sb.append(primaryFormatter.format(stdDev));
        double cv = results.get("Coeff of Variation %");
        if (cv > 0) {
            sb.append(" (CV: ").append(primaryFormatter.format(cv)).append("%)");
        }
        sb.append(".\n");
        
        sb.append("• Distribution: ").append(engine.getSkewnessInterpretation());
        sb.append(", ").append(engine.getKurtosisInterpretation()).append(".\n");
        
        int outliers = results.get("Outliers (IQR method)").intValue();
        if (outliers > 0) {
            sb.append("• Outliers: ").append(outliers).append(" potential outlier(s) detected using IQR method.\n");
        }
        
        interpretationArea.setText(sb.toString());
        interpretationArea.setCaretPosition(0);
    }
    
    public void clearResults() {
        tableModel.setRowCount(0);
        statusLabel.setText("No data loaded");
        interpretationArea.setText("Load a CSV file and select a column to see analysis interpretation.");
    }
    
    private class CategoryCellRenderer extends DefaultTableCellRenderer {
        
        private Map<String, Color> categoryColors;
        
        public CategoryCellRenderer() {
            categoryColors = new HashMap<>();
            categoryColors.put("Basic Stats", new Color(46, 204, 113, 40));
            categoryColors.put("Central Tendency", new Color(52, 152, 219, 40));
            categoryColors.put("Dispersion", new Color(155, 89, 182, 40));
            categoryColors.put("Quartiles", new Color(241, 196, 15, 40));
            categoryColors.put("Distribution Shape", new Color(231, 76, 60, 40));
            categoryColors.put("Additional", new Color(149, 165, 166, 40));
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            Component c = super.getTableCellRendererComponent(table, value, 
                    isSelected, hasFocus, row, column);
            
            if (!isSelected) {
                String category = (String) table.getValueAt(row, 0);
                Color bgColor = categoryColors.getOrDefault(category, Color.WHITE);
                c.setBackground(bgColor);
            }
            
            if (column == 0) {
                setFont(getFont().deriveFont(Font.BOLD));
            } else if (column == 2) {
                setHorizontalAlignment(SwingConstants.RIGHT);
                setFont(new Font("Monospaced", Font.PLAIN, 12));
            } else {
                setHorizontalAlignment(SwingConstants.LEFT);
                setFont(getFont().deriveFont(Font.PLAIN));
            }
            
            setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
            
            return c;
        }
    }
}
