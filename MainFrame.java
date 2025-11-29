package analyzer;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

/**
 * @author Lukasz Golinski
 */
public class MainFrame extends JFrame {
    
    private DataModel currentModel;
    private StatisticsEngine statsEngine;
    private CSVLoader csvLoader;
    
    private JComboBox<String> columnSelector;
    private JLabel fileNameLabel;
    private JLabel statusLabel;
    
    private VisualizationPanel visualPanel;
    private ResultsPanel resultsPanel;
    private DataTablePanel dataTablePanel;
    private MovingAveragePanel movingAvgPanel;
    
    private JTabbedPane mainTabbedPane;
    
    private JComboBox<String> chartTypeCombo;
    
    private static final Color HEADER_BG = new Color(44, 62, 80);
    private static final Color ACCENT_COLOR = new Color(52, 152, 219);
    private static final Color SUCCESS_COLOR = new Color(46, 204, 113);
    
    public MainFrame() {
        super("Data Analyzer Pro - Numerical CSV Analysis Tool");
        
        currentModel = new DataModel();
        statsEngine = new StatisticsEngine();
        csvLoader = new CSVLoader();
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 800);
        setMinimumSize(new Dimension(1000, 600));
        setLocationRelativeTo(null);
        
        initializeUI();
        setupMenuBar();
        setupKeyBindings();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout(0, 0));
        
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        mainTabbedPane = new JTabbedPane();
        mainTabbedPane.setFont(new Font("SansSerif", Font.BOLD, 12));
        mainTabbedPane.setBackground(Color.WHITE);
        
        JSplitPane analysisPane = createAnalysisTab();
        mainTabbedPane.addTab("Analysis", analysisPane);
        
        dataTablePanel = new DataTablePanel();
        mainTabbedPane.addTab("Data View", dataTablePanel);
        
        movingAvgPanel = new MovingAveragePanel();
        movingAvgPanel.setMovingAverageListener((original, ma, type, window) -> {
            visualPanel.setData(original);
            visualPanel.setSecondaryData(ma);
            visualPanel.setChartType(VisualizationPanel.ChartType.MOVING_AVERAGE_OVERLAY);
            visualPanel.setChartTitle(type + " Overlay");
        });
        mainTabbedPane.addTab("Moving Averages", movingAvgPanel);
        
        JPanel helpPanel = createHelpPanel();
        mainTabbedPane.addTab("Help", helpPanel);
        
        add(mainTabbedPane, BorderLayout.CENTER);
        
        JPanel statusBar = createStatusBar();
        add(statusBar, BorderLayout.SOUTH);
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(15, 10));
        headerPanel.setBackground(HEADER_BG);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));
        
        JPanel leftSection = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftSection.setOpaque(false);
        
        JButton loadButton = createStyledButton("Load CSV", ACCENT_COLOR);
        loadButton.addActionListener(e -> loadCSVFile());
        leftSection.add(loadButton);
        
        fileNameLabel = new JLabel("No file loaded");
        fileNameLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
        fileNameLabel.setForeground(Color.LIGHT_GRAY);
        leftSection.add(fileNameLabel);
        
        JPanel centerSection = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        centerSection.setOpaque(false);
        
        JLabel columnLabel = new JLabel("Analyze Column:");
        columnLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        columnLabel.setForeground(Color.WHITE);
        centerSection.add(columnLabel);
        
        columnSelector = new JComboBox<>();
        columnSelector.setPreferredSize(new Dimension(180, 28));
        columnSelector.setFont(new Font("SansSerif", Font.PLAIN, 12));
        columnSelector.addActionListener(e -> analyzeSelectedColumn());
        centerSection.add(columnSelector);
        
        JLabel chartLabel = new JLabel("Chart:");
        chartLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        chartLabel.setForeground(Color.WHITE);
        centerSection.add(chartLabel);
        
        chartTypeCombo = new JComboBox<>(new String[]{
            "Line Chart", "Bar Chart", "Histogram", "Scatter Plot"
        });
        chartTypeCombo.setPreferredSize(new Dimension(120, 28));
        chartTypeCombo.addActionListener(e -> updateChartType());
        centerSection.add(chartTypeCombo);
        
        JPanel rightSection = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightSection.setOpaque(false);
        
        JButton exportButton = createStyledButton("Export Results", SUCCESS_COLOR);
        exportButton.addActionListener(e -> exportResults());
        rightSection.add(exportButton);
        
        JButton clearButton = createStyledButton("Clear", new Color(231, 76, 60));
        clearButton.addActionListener(e -> clearAllData());
        rightSection.add(clearButton);
        
        headerPanel.add(leftSection, BorderLayout.WEST);
        headerPanel.add(centerSection, BorderLayout.CENTER);
        headerPanel.add(rightSection, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.brighter());
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });
        
        return button;
    }
    
    private JSplitPane createAnalysisTab() {
        visualPanel = new VisualizationPanel();
        JPanel chartContainer = new JPanel(new BorderLayout());
        chartContainer.setBackground(Color.WHITE);
        chartContainer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(10, 10, 10, 5),
            BorderFactory.createLineBorder(new Color(200, 200, 200))
        ));
        chartContainer.add(visualPanel, BorderLayout.CENTER);
        
        resultsPanel = new ResultsPanel();
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, 
                                              chartContainer, resultsPanel);
        splitPane.setDividerLocation(600);
        splitPane.setResizeWeight(0.5);
        splitPane.setDividerSize(8);
        splitPane.setContinuousLayout(true);
        
        return splitPane;
    }
    
    private JPanel createHelpPanel() {
        JPanel helpPanel = new JPanel(new BorderLayout(20, 20));
        helpPanel.setBackground(Color.WHITE);
        helpPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        
        JLabel titleLabel = new JLabel("Data Analyzer Pro - User Guide");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setForeground(HEADER_BG);
        
        JTextPane helpText = new JTextPane();
        helpText.setContentType("text/html");
        helpText.setEditable(false);
        helpText.setBackground(Color.WHITE);
        
        String helpContent = 
            "<html><body style='font-family: SansSerif; font-size: 12px; line-height: 1.6;'>" +
            "<h2 style='color: #2c3e50;'>Getting Started</h2>" +
            "<p>Welcome to Data Analyzer Pro, a comprehensive tool for analyzing numerical CSV data.</p>" +
            
            "<h3 style='color: #3498db;'>Loading Data</h3>" +
            "<ul>" +
            "<li>Click <b>Load CSV</b> button or press <b>Ctrl+O</b> to open a file</li>" +
            "<li>Supports CSV, TSV, and TXT files with numerical data</li>" +
            "<li>Automatic delimiter detection (comma, semicolon, tab, pipe)</li>" +
            "<li>Non-numeric values are automatically filtered</li>" +
            "</ul>" +
            
            "<h3 style='color: #3498db;'>Statistical Analysis</h3>" +
            "<p>Select a column from the dropdown to see comprehensive statistics:</p>" +
            "<ul>" +
            "<li><b>Basic Stats:</b> Count, Sum, Min, Max, Range</li>" +
            "<li><b>Central Tendency:</b> Mean, Median, Mode, Geometric Mean, Harmonic Mean</li>" +
            "<li><b>Dispersion:</b> Variance, Standard Deviation, Coefficient of Variation</li>" +
            "<li><b>Quartiles:</b> Q1, Q2, Q3, IQR, Percentiles</li>" +
            "<li><b>Distribution Shape:</b> Skewness, Kurtosis with interpretation</li>" +
            "</ul>" +
            
            "<h3 style='color: #3498db;'>Visualization</h3>" +
            "<p>Multiple chart types available:</p>" +
            "<ul>" +
            "<li><b>Line Chart:</b> Shows data trends over index</li>" +
            "<li><b>Bar Chart:</b> Compares individual values</li>" +
            "<li><b>Histogram:</b> Shows frequency distribution</li>" +
            "<li><b>Scatter Plot:</b> Displays data point distribution</li>" +
            "</ul>" +
            
            "<h3 style='color: #3498db;'>Moving Averages</h3>" +
            "<p>The Moving Averages tab provides:</p>" +
            "<ul>" +
            "<li><b>Simple Moving Average (SMA):</b> Configurable window size</li>" +
            "<li><b>Exponential Moving Average (EMA):</b> Adjustable smoothing factor</li>" +
            "<li><b>Weighted Moving Average (WMA):</b> Linear weighted calculation</li>" +
            "<li><b>4-Point Moving Average:</b> Fixed window for trend analysis</li>" +
            "</ul>" +
            
            "<h3 style='color: #3498db;'>Keyboard Shortcuts</h3>" +
            "<ul>" +
            "<li><b>Ctrl+O:</b> Open file</li>" +
            "<li><b>Ctrl+E:</b> Export results</li>" +
            "<li><b>Ctrl+N:</b> Clear all data</li>" +
            "<li><b>F1:</b> Show this help</li>" +
            "</ul>" +
            
            "<h3 style='color: #3498db;'>Export Options</h3>" +
            "<p>Export your analysis results to a text file for reporting and further use.</p>" +
            
            "<p style='margin-top: 30px; color: #7f8c8d;'><i>Author: Lukasz Golinski</i></p>" +
            
            "</body></html>";
        
        helpText.setText(helpContent);
        helpText.setCaretPosition(0);
        
        JScrollPane scrollPane = new JScrollPane(helpText);
        scrollPane.setBorder(null);
        
        helpPanel.add(titleLabel, BorderLayout.NORTH);
        helpPanel.add(scrollPane, BorderLayout.CENTER);
        
        return helpPanel;
    }
    
    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(new Color(236, 240, 241));
        statusBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        
        statusLabel = new JLabel("Ready - Load a CSV file to begin analysis");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        statusLabel.setForeground(new Color(100, 100, 100));
        
        JLabel versionLabel = new JLabel("Data Analyzer Pro v1.0.0 | Author: Lukasz Golinski");
        versionLabel.setFont(new Font("SansSerif", Font.ITALIC, 10));
        versionLabel.setForeground(new Color(150, 150, 150));
        
        statusBar.add(statusLabel, BorderLayout.WEST);
        statusBar.add(versionLabel, BorderLayout.EAST);
        
        return statusBar;
    }
    
    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(Color.WHITE);
        
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        
        JMenuItem openItem = new JMenuItem("Open CSV...");
        openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        openItem.addActionListener(e -> loadCSVFile());
        fileMenu.add(openItem);
        
        JMenuItem exportItem = new JMenuItem("Export Results...");
        exportItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK));
        exportItem.addActionListener(e -> exportResults());
        fileMenu.add(exportItem);
        
        fileMenu.addSeparator();
        
        JMenuItem clearItem = new JMenuItem("Clear All");
        clearItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
        clearItem.addActionListener(e -> clearAllData());
        fileMenu.add(clearItem);
        
        fileMenu.addSeparator();
        
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);
        
        JMenu viewMenu = new JMenu("View");
        viewMenu.setMnemonic(KeyEvent.VK_V);
        
        JMenuItem analysisTabItem = new JMenuItem("Analysis Tab");
        analysisTabItem.addActionListener(e -> mainTabbedPane.setSelectedIndex(0));
        viewMenu.add(analysisTabItem);
        
        JMenuItem dataTabItem = new JMenuItem("Data View Tab");
        dataTabItem.addActionListener(e -> mainTabbedPane.setSelectedIndex(1));
        viewMenu.add(dataTabItem);
        
        JMenuItem maTabItem = new JMenuItem("Moving Averages Tab");
        maTabItem.addActionListener(e -> mainTabbedPane.setSelectedIndex(2));
        viewMenu.add(maTabItem);
        
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic(KeyEvent.VK_H);
        
        JMenuItem helpItem = new JMenuItem("User Guide");
        helpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
        helpItem.addActionListener(e -> mainTabbedPane.setSelectedIndex(3));
        helpMenu.add(helpItem);
        
        helpMenu.addSeparator();
        
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> showAboutDialog());
        helpMenu.add(aboutItem);
        
        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
        menuBar.add(helpMenu);
        
        setJMenuBar(menuBar);
    }
    
    private void setupKeyBindings() {
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK), "open");
        getRootPane().getActionMap().put("open", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadCSVFile();
            }
        });
    }
    
    private void loadCSVFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select CSV File");
        
        FileNameExtensionFilter csvFilter = new FileNameExtensionFilter(
            "CSV Files (*.csv, *.txt, *.tsv)", "csv", "txt", "tsv");
        fileChooser.addChoosableFileFilter(csvFilter);
        fileChooser.setFileFilter(csvFilter);
        
        int result = fileChooser.showOpenDialog(this);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            
            statusLabel.setText("Loading: " + selectedFile.getName() + "...");
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            
            SwingWorker<DataModel, Void> worker = new SwingWorker<>() {
                @Override
                protected DataModel doInBackground() {
                    return csvLoader.loadFile(selectedFile);
                }
                
                @Override
                protected void done() {
                    try {
                        currentModel = get();
                        processLoadedData();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(MainFrame.this,
                            "Error loading file: " + ex.getMessage(),
                            "Load Error",
                            JOptionPane.ERROR_MESSAGE);
                        statusLabel.setText("Error loading file");
                    } finally {
                        setCursor(Cursor.getDefaultCursor());
                    }
                }
            };
            
            worker.execute();
        }
    }
    
    private void processLoadedData() {
        if (!currentModel.hasData()) {
            JOptionPane.showMessageDialog(this,
                "No numerical data found in the file.\nPlease ensure the file contains numeric values.",
                "No Data",
                JOptionPane.WARNING_MESSAGE);
            statusLabel.setText("No data found in file");
            return;
        }
        
        fileNameLabel.setText(currentModel.getSourceFileName());
        
        columnSelector.removeAllItems();
        for (String col : currentModel.getColumnNames()) {
            columnSelector.addItem(col);
        }
        
        dataTablePanel.loadData(currentModel);
        
        if (columnSelector.getItemCount() > 0) {
            columnSelector.setSelectedIndex(0);
        }
        
        statusLabel.setText("Loaded: " + currentModel.getSourceFileName() + 
                           " | " + currentModel.getColumnCount() + " columns | " +
                           currentModel.getTotalRecords() + " records");
    }
    
    private void analyzeSelectedColumn() {
        String selectedColumn = (String) columnSelector.getSelectedItem();
        
        if (selectedColumn == null || !currentModel.hasData()) {
            return;
        }
        
        double[] columnData = currentModel.getColumnAsArray(selectedColumn);
        
        if (columnData.length == 0) {
            statusLabel.setText("No numeric data in column: " + selectedColumn);
            return;
        }
        
        statsEngine.loadData(columnData);
        
        resultsPanel.displayResults(statsEngine, selectedColumn);
        
        visualPanel.setData(columnData);
        visualPanel.setChartTitle(selectedColumn + " - Data Visualization");
        visualPanel.setAxisLabels("Index", "Value");
        
        movingAvgPanel.setData(columnData);
        
        dataTablePanel.setHighlightColumn(selectedColumn);
        
        statusLabel.setText("Analyzing: " + selectedColumn + " (" + columnData.length + " values)");
    }
    
    private void updateChartType() {
        String selected = (String) chartTypeCombo.getSelectedItem();
        
        if (selected == null) return;
        
        switch (selected) {
            case "Line Chart":
                visualPanel.setChartType(VisualizationPanel.ChartType.LINE_CHART);
                break;
            case "Bar Chart":
                visualPanel.setChartType(VisualizationPanel.ChartType.BAR_CHART);
                break;
            case "Histogram":
                visualPanel.setChartType(VisualizationPanel.ChartType.HISTOGRAM);
                break;
            case "Scatter Plot":
                visualPanel.setChartType(VisualizationPanel.ChartType.SCATTER_PLOT);
                break;
        }
    }
    
    private void exportResults() {
        if (!statsEngine.hasData()) {
            JOptionPane.showMessageDialog(this,
                "No analysis results to export.\nPlease load data and select a column first.",
                "No Results",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Analysis Results");
        fileChooser.setSelectedFile(new File("analysis_results.txt"));
        
        int result = fileChooser.showSaveDialog(this);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            File outputFile = fileChooser.getSelectedFile();
            
            try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
                writer.println("========================================");
                writer.println("    DATA ANALYZER PRO - ANALYSIS REPORT");
                writer.println("========================================");
                writer.println();
                writer.println("Author: Lukasz Golinski");
                writer.println();
                writer.println("Source File: " + currentModel.getSourceFileName());
                writer.println("Column Analyzed: " + columnSelector.getSelectedItem());
                writer.println("Generated: " + new java.util.Date());
                writer.println();
                writer.println("----------------------------------------");
                writer.println("         STATISTICAL SUMMARY");
                writer.println("----------------------------------------");
                writer.println();
                
                DecimalFormat df = new DecimalFormat("#,##0.######");
                Map<String, Double> results = statsEngine.getFullAnalysis();
                
                for (Map.Entry<String, Double> entry : results.entrySet()) {
                    String formatted = String.format("%-25s : %s", 
                                                     entry.getKey(), 
                                                     df.format(entry.getValue()));
                    writer.println(formatted);
                }
                
                writer.println();
                writer.println("----------------------------------------");
                writer.println("         INTERPRETATION");
                writer.println("----------------------------------------");
                writer.println();
                writer.println("Skewness: " + statsEngine.getSkewnessInterpretation());
                writer.println("Kurtosis: " + statsEngine.getKurtosisInterpretation());
                
                writer.println();
                writer.println("----------------------------------------");
                writer.println("         MOVING AVERAGES");
                writer.println("----------------------------------------");
                writer.println();
                
                double[] ma4 = statsEngine.getFourPointMovingAverage();
                writer.println("4-Point Moving Average values: " + ma4.length);
                if (ma4.length > 0) {
                    writer.print("First 10: ");
                    for (int i = 0; i < Math.min(10, ma4.length); i++) {
                        writer.print(df.format(ma4[i]));
                        if (i < Math.min(9, ma4.length - 1)) writer.print(", ");
                    }
                    writer.println();
                }
                
                writer.println();
                writer.println("========================================");
                writer.println("            END OF REPORT");
                writer.println("========================================");
                
                JOptionPane.showMessageDialog(this,
                    "Results exported successfully to:\n" + outputFile.getAbsolutePath(),
                    "Export Complete",
                    JOptionPane.INFORMATION_MESSAGE);
                
                statusLabel.setText("Results exported to: " + outputFile.getName());
                
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                    "Error exporting results: " + ex.getMessage(),
                    "Export Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void clearAllData() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to clear all data?",
            "Confirm Clear",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            currentModel.clearData();
            statsEngine = new StatisticsEngine();
            
            columnSelector.removeAllItems();
            fileNameLabel.setText("No file loaded");
            
            visualPanel.clearChart();
            resultsPanel.clearResults();
            dataTablePanel.clearData();
            movingAvgPanel.clearData();
            
            statusLabel.setText("All data cleared - Ready to load new file");
        }
    }
    
    private void showAboutDialog() {
        String message = 
            "Data Analyzer Pro\n" +
            "Version 1.0.0\n\n" +
            "Author: Lukasz Golinski\n\n" +
            "A comprehensive numerical data analysis tool\n" +
            "with interactive visualization and statistics.\n\n" +
            "Features:\n" +
            "  Complete statistical analysis\n" +
            "  Multiple chart types\n" +
            "  Moving average calculations\n" +
            "  Data export capabilities";
        
        JOptionPane.showMessageDialog(this,
            message,
            "About Data Analyzer Pro",
            JOptionPane.INFORMATION_MESSAGE);
    }
}
