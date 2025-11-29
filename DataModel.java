package analyzer;

import java.util.*;

/**
 * @author Lukasz Golinski
 */
public class DataModel {
    
    private Map<String, List<Double>> columnData;
    private List<String> columnNames;
    private List<String[]> rawRecords;
    private String sourceFileName;
    private int totalRecords;
    private int validRecords;
    private int skippedRecords;
    
    public DataModel() {
        this.columnData = new LinkedHashMap<>();
        this.columnNames = new ArrayList<>();
        this.rawRecords = new ArrayList<>();
        this.totalRecords = 0;
        this.validRecords = 0;
        this.skippedRecords = 0;
    }
    
    public void addColumn(String name) {
        if (!columnData.containsKey(name)) {
            columnNames.add(name);
            columnData.put(name, new ArrayList<>());
        }
    }
    
    public void addValueToColumn(String columnName, Double value) {
        if (columnData.containsKey(columnName)) {
            columnData.get(columnName).add(value);
        }
    }
    
    public void addRawRecord(String[] record) {
        rawRecords.add(record);
    }
    
    public List<Double> getColumnValues(String columnName) {
        return columnData.getOrDefault(columnName, new ArrayList<>());
    }
    
    public double[] getColumnAsArray(String columnName) {
        List<Double> values = getColumnValues(columnName);
        double[] arr = new double[values.size()];
        for (int i = 0; i < values.size(); i++) {
            arr[i] = values.get(i);
        }
        return arr;
    }
    
    public List<String> getColumnNames() {
        return new ArrayList<>(columnNames);
    }
    
    public int getColumnCount() {
        return columnNames.size();
    }
    
    public int getRecordCount(String columnName) {
        return columnData.containsKey(columnName) ? columnData.get(columnName).size() : 0;
    }
    
    public List<String[]> getRawRecords() {
        return rawRecords;
    }
    
    public String getSourceFileName() {
        return sourceFileName;
    }
    
    public void setSourceFileName(String sourceFileName) {
        this.sourceFileName = sourceFileName;
    }
    
    public int getTotalRecords() {
        return totalRecords;
    }
    
    public void setTotalRecords(int totalRecords) {
        this.totalRecords = totalRecords;
    }
    
    public int getValidRecords() {
        return validRecords;
    }
    
    public void setValidRecords(int validRecords) {
        this.validRecords = validRecords;
    }
    
    public int getSkippedRecords() {
        return skippedRecords;
    }
    
    public void setSkippedRecords(int skippedRecords) {
        this.skippedRecords = skippedRecords;
    }
    
    public void clearData() {
        columnData.clear();
        columnNames.clear();
        rawRecords.clear();
        totalRecords = 0;
        validRecords = 0;
        skippedRecords = 0;
        sourceFileName = null;
    }
    
    public boolean hasData() {
        return !columnData.isEmpty() && !columnNames.isEmpty();
    }
    
    public Double getValueAt(String columnName, int index) {
        List<Double> values = columnData.get(columnName);
        if (values != null && index >= 0 && index < values.size()) {
            return values.get(index);
        }
        return null;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("DataModel[");
        sb.append("file=").append(sourceFileName);
        sb.append(", columns=").append(columnNames.size());
        sb.append(", records=").append(totalRecords);
        sb.append("]");
        return sb.toString();
    }
}
