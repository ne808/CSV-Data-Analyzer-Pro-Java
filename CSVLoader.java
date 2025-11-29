package analyzer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/**
 * @author Lukasz Golinski
 */
public class CSVLoader {
    
    private char delimiter;
    private boolean hasHeader;
    private String lastError;
    
    public CSVLoader() {
        this.delimiter = ',';
        this.hasHeader = true;
        this.lastError = null;
    }
    
    public CSVLoader(char delimiter, boolean hasHeader) {
        this.delimiter = delimiter;
        this.hasHeader = hasHeader;
        this.lastError = null;
    }
    
    public DataModel loadFile(File file) {
        DataModel model = new DataModel();
        model.setSourceFileName(file.getName());
        
        try {
            List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
            
            if (lines.isEmpty()) {
                lastError = "File is empty";
                return model;
            }
            
            delimiter = detectDelimiter(lines.get(0));
            
            int startLine = 0;
            String[] headers;
            
            if (hasHeader && !lines.isEmpty()) {
                headers = parseLine(lines.get(0));
                startLine = 1;
                
                for (int i = 0; i < headers.length; i++) {
                    String columnName = headers[i].trim();
                    if (columnName.isEmpty()) {
                        columnName = "Column_" + (i + 1);
                    }
                    model.addColumn(columnName);
                }
            } else {
                String[] firstLineParts = parseLine(lines.get(0));
                for (int i = 0; i < firstLineParts.length; i++) {
                    model.addColumn("Column_" + (i + 1));
                }
            }
            
            int totalRecords = 0;
            int validRecords = 0;
            int skippedRecords = 0;
            
            List<String> columnNames = model.getColumnNames();
            
            for (int lineIndex = startLine; lineIndex < lines.size(); lineIndex++) {
                String line = lines.get(lineIndex).trim();
                
                if (line.isEmpty()) {
                    continue;
                }
                
                totalRecords++;
                String[] values = parseLine(line);
                
                boolean hasValidValue = false;
                
                for (int col = 0; col < columnNames.size() && col < values.length; col++) {
                    String columnName = columnNames.get(col);
                    String rawValue = values[col].trim();
                    
                    Double numericValue = parseNumericValue(rawValue);
                    
                    if (numericValue != null) {
                        model.addValueToColumn(columnName, numericValue);
                        hasValidValue = true;
                    }
                }
                
                model.addRawRecord(values);
                
                if (hasValidValue) {
                    validRecords++;
                } else {
                    skippedRecords++;
                }
            }
            
            model.setTotalRecords(totalRecords);
            model.setValidRecords(validRecords);
            model.setSkippedRecords(skippedRecords);
            
        } catch (IOException ex) {
            lastError = "Error reading file: " + ex.getMessage();
        } catch (Exception ex) {
            lastError = "Error parsing file: " + ex.getMessage();
        }
        
        return model;
    }
    
    private char detectDelimiter(String line) {
        int commaCount = countOccurrences(line, ',');
        int semicolonCount = countOccurrences(line, ';');
        int tabCount = countOccurrences(line, '\t');
        int pipeCount = countOccurrences(line, '|');
        
        if (tabCount >= commaCount && tabCount >= semicolonCount && tabCount >= pipeCount) {
            return '\t';
        } else if (semicolonCount >= commaCount && semicolonCount >= pipeCount) {
            return ';';
        } else if (pipeCount >= commaCount) {
            return '|';
        }
        
        return ',';
    }
    
    private int countOccurrences(String str, char ch) {
        int count = 0;
        boolean inQuotes = false;
        
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ch && !inQuotes) {
                count++;
            }
        }
        
        return count;
    }
    
    private String[] parseLine(String line) {
        List<String> tokens = new ArrayList<>();
        StringBuilder currentToken = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    currentToken.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == delimiter && !inQuotes) {
                tokens.add(currentToken.toString());
                currentToken = new StringBuilder();
            } else {
                currentToken.append(c);
            }
        }
        
        tokens.add(currentToken.toString());
        
        return tokens.toArray(new String[0]);
    }
    
    private Double parseNumericValue(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        
        String cleanValue = value.trim();
        
        cleanValue = cleanValue.replaceAll("[$€£¥%]", "");
        cleanValue = cleanValue.replaceAll("[,\\s](?=\\d{3})", "");
        cleanValue = cleanValue.replace("(", "-").replace(")", "");
        
        cleanValue = cleanValue.trim();
        
        if (cleanValue.isEmpty() || 
            cleanValue.equalsIgnoreCase("na") || 
            cleanValue.equalsIgnoreCase("n/a") ||
            cleanValue.equalsIgnoreCase("null") ||
            cleanValue.equals("-") ||
            cleanValue.equals(".")) {
            return null;
        }
        
        try {
            return Double.parseDouble(cleanValue);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
    
    public void setDelimiter(char delimiter) {
        this.delimiter = delimiter;
    }
    
    public char getDelimiter() {
        return delimiter;
    }
    
    public void setHasHeader(boolean hasHeader) {
        this.hasHeader = hasHeader;
    }
    
    public boolean isHasHeader() {
        return hasHeader;
    }
    
    public String getLastError() {
        return lastError;
    }
    
    public boolean hasError() {
        return lastError != null;
    }
    
    public void clearError() {
        lastError = null;
    }
}
