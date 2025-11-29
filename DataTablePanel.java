package analyzer;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

/**
 * @author Lukasz Golinski
 */
public class DataTablePanel extends JPanel {
    
    private JTable dataTable;
    private DefaultTableModel tableModel;
    private JLabel infoLabel;
    private JComboBox<String> highlightColumnCombo;
    private JTextField searchField;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private DecimalFormat formatter;
    private int highlightColumnIndex;
    
    public DataTablePanel() {
        formatter = new DecimalFormat("#,##0.######");
        highlightColumnIndex = -1;
        
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(new Color(248, 249, 250));
        
        initializeComponents();
    }
    
    private void initializeComponents() {
        JPanel topPanel = new JPanel(new BorderLayout(10, 5));
        topPanel.setOpaque(false);
        
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        leftPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("Data Preview");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        titleLabel.setForeground(new Color(44, 62, 80));
        leftPanel.add(titleLabel);
        
        JLabel highlightLabel = new JLabel("  Highlight Column:");
        highlightLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        leftPanel.add(highlightLabel);
        
        highlightColumnCombo = new JComboBox<>();
        highlightColumnCombo.setPreferredSize(new Dimension(150, 25));
        highlightColumnCombo.addItem("None");
        highlightColumnCombo.addActionListener(e -> updateHighlight());
        leftPanel.add(highlightColumnCombo);
        
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        rightPanel.setOpaque(false);
        
        JLabel searchLabel = new JLabel("Filter:");
        searchLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        rightPanel.add(searchLabel);
        
        searchField = new JTextField(15);
        searchField.setPreferredSize(new Dimension(150, 25));
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
        });
        rightPanel.add(searchField);
        
        topPanel.add(leftPanel, BorderLayout.WEST);
        topPanel.add(rightPanel, BorderLayout.EAST);
        
        add(topPanel, BorderLayout.NORTH);
        
        tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        dataTable = new JTable(tableModel);
        dataTable.setFont(new Font("SansSerif", Font.PLAIN, 12));
        dataTable.setRowHeight(24);
        dataTable.setShowGrid(true);
        dataTable.setGridColor(new Color(230, 230, 230));
        dataTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        dataTable.setSelectionBackground(new Color(52, 152, 219, 80));
        dataTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        JTableHeader header = dataTable.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 12));
        header.setBackground(new Color(52, 73, 94));
        header.setForeground(Color.WHITE);
        header.setReorderingAllowed(true);
        
        rowSorter = new TableRowSorter<>(tableModel);
        dataTable.setRowSorter(rowSorter);
        
        dataTable.setDefaultRenderer(Object.class, new HighlightCellRenderer());
        
        JScrollPane scrollPane = new JScrollPane(dataTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        add(scrollPane, BorderLayout.CENTER);
        
        infoLabel = new JLabel("No data loaded");
        infoLabel.setFont(new Font("SansSerif", Font.ITALIC, 11));
        infoLabel.setForeground(Color.GRAY);
        infoLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        
        add(infoLabel, BorderLayout.SOUTH);
    }
    
    public void loadData(DataModel model) {
        tableModel.setRowCount(0);
        tableModel.setColumnCount(0);
        highlightColumnCombo.removeAllItems();
        highlightColumnCombo.addItem("None");
        highlightColumnIndex = -1;
        
        if (model == null || !model.hasData()) {
            infoLabel.setText("No data loaded");
            return;
        }
        
        List<String> columns = model.getColumnNames();
        
        tableModel.addColumn("Row #");
        for (String col : columns) {
            tableModel.addColumn(col);
            highlightColumnCombo.addItem(col);
        }
        
        int maxRecords = 0;
        for (String col : columns) {
            int count = model.getRecordCount(col);
            if (count > maxRecords) maxRecords = count;
        }
        
        for (int row = 0; row < maxRecords; row++) {
            Object[] rowData = new Object[columns.size() + 1];
            rowData[0] = row + 1;
            
            for (int col = 0; col < columns.size(); col++) {
                Double value = model.getValueAt(columns.get(col), row);
                if (value != null) {
                    rowData[col + 1] = formatter.format(value);
                } else {
                    rowData[col + 1] = "";
                }
            }
            
            tableModel.addRow(rowData);
        }
        
        for (int i = 0; i < dataTable.getColumnCount(); i++) {
            TableColumn column = dataTable.getColumnModel().getColumn(i);
            int preferredWidth = 80;
            
            TableCellRenderer headerRenderer = dataTable.getTableHeader().getDefaultRenderer();
            Component headerComp = headerRenderer.getTableCellRendererComponent(
                dataTable, column.getHeaderValue(), false, false, 0, i);
            preferredWidth = Math.max(preferredWidth, headerComp.getPreferredSize().width + 20);
            
            for (int row = 0; row < Math.min(50, dataTable.getRowCount()); row++) {
                TableCellRenderer cellRenderer = dataTable.getCellRenderer(row, i);
                Component cellComp = dataTable.prepareRenderer(cellRenderer, row, i);
                preferredWidth = Math.max(preferredWidth, cellComp.getPreferredSize().width + 20);
            }
            
            column.setPreferredWidth(Math.min(preferredWidth, 200));
        }
        
        StringBuilder info = new StringBuilder();
        info.append("Loaded: ").append(model.getSourceFileName());
        info.append(" | Columns: ").append(columns.size());
        info.append(" | Records: ").append(model.getTotalRecords());
        if (model.getSkippedRecords() > 0) {
            info.append(" (").append(model.getSkippedRecords()).append(" skipped)");
        }
        
        infoLabel.setText(info.toString());
    }
    
    private void updateHighlight() {
        String selected = (String) highlightColumnCombo.getSelectedItem();
        
        if (selected == null || selected.equals("None")) {
            highlightColumnIndex = -1;
        } else {
            highlightColumnIndex = tableModel.findColumn(selected);
        }
        
        dataTable.repaint();
    }
    
    private void applyFilter() {
        String text = searchField.getText();
        
        if (text.trim().isEmpty()) {
            rowSorter.setRowFilter(null);
        } else {
            try {
                rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
            } catch (java.util.regex.PatternSyntaxException ex) {
                rowSorter.setRowFilter(null);
            }
        }
    }
    
    public void clearData() {
        tableModel.setRowCount(0);
        tableModel.setColumnCount(0);
        highlightColumnCombo.removeAllItems();
        highlightColumnCombo.addItem("None");
        highlightColumnIndex = -1;
        searchField.setText("");
        infoLabel.setText("No data loaded");
    }
    
    public void setHighlightColumn(String columnName) {
        for (int i = 0; i < highlightColumnCombo.getItemCount(); i++) {
            if (highlightColumnCombo.getItemAt(i).equals(columnName)) {
                highlightColumnCombo.setSelectedIndex(i);
                break;
            }
        }
    }
    
    private class HighlightCellRenderer extends DefaultTableCellRenderer {
        
        private Color highlightColor = new Color(46, 204, 113, 60);
        private Color alternateRowColor = new Color(248, 249, 250);
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            Component c = super.getTableCellRendererComponent(table, value, 
                    isSelected, hasFocus, row, column);
            
            if (!isSelected) {
                if (column == highlightColumnIndex) {
                    c.setBackground(highlightColor);
                } else if (row % 2 == 0) {
                    c.setBackground(Color.WHITE);
                } else {
                    c.setBackground(alternateRowColor);
                }
            }
            
            if (column == 0) {
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(getFont().deriveFont(Font.BOLD));
                c.setBackground(new Color(240, 240, 240));
            } else {
                setHorizontalAlignment(SwingConstants.RIGHT);
                setFont(new Font("Monospaced", Font.PLAIN, 12));
            }
            
            setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
            
            return c;
        }
    }
}
