package analyzer;

import java.util.*;

/**
 * @author Lukasz Golinski
 */
public class StatisticsEngine {
    
    private double[] data;
    private double[] sortedData;
    private int dataSize;
    
    public StatisticsEngine() {
        this.data = new double[0];
        this.sortedData = new double[0];
        this.dataSize = 0;
    }
    
    public void loadData(double[] inputData) {
        if (inputData == null || inputData.length == 0) {
            this.data = new double[0];
            this.sortedData = new double[0];
            this.dataSize = 0;
            return;
        }
        
        this.data = Arrays.copyOf(inputData, inputData.length);
        this.sortedData = Arrays.copyOf(inputData, inputData.length);
        Arrays.sort(this.sortedData);
        this.dataSize = inputData.length;
    }
    
    public void loadData(List<Double> inputList) {
        if (inputList == null || inputList.isEmpty()) {
            this.data = new double[0];
            this.sortedData = new double[0];
            this.dataSize = 0;
            return;
        }
        
        this.data = new double[inputList.size()];
        for (int i = 0; i < inputList.size(); i++) {
            this.data[i] = inputList.get(i);
        }
        this.sortedData = Arrays.copyOf(this.data, this.data.length);
        Arrays.sort(this.sortedData);
        this.dataSize = inputList.size();
    }
    
    public int getCount() {
        return dataSize;
    }
    
    public double getSum() {
        if (dataSize == 0) return 0.0;
        double total = 0.0;
        for (double val : data) {
            total += val;
        }
        return total;
    }
    
    public double getMean() {
        if (dataSize == 0) return 0.0;
        return getSum() / dataSize;
    }
    
    public double getMinimum() {
        if (dataSize == 0) return 0.0;
        return sortedData[0];
    }
    
    public double getMaximum() {
        if (dataSize == 0) return 0.0;
        return sortedData[dataSize - 1];
    }
    
    public double getRange() {
        if (dataSize == 0) return 0.0;
        return getMaximum() - getMinimum();
    }
    
    public double getMedian() {
        if (dataSize == 0) return 0.0;
        
        int midPoint = dataSize / 2;
        if (dataSize % 2 == 0) {
            return (sortedData[midPoint - 1] + sortedData[midPoint]) / 2.0;
        } else {
            return sortedData[midPoint];
        }
    }
    
    public double getMode() {
        if (dataSize == 0) return 0.0;
        
        Map<Double, Integer> frequencyMap = new HashMap<>();
        for (double val : data) {
            frequencyMap.put(val, frequencyMap.getOrDefault(val, 0) + 1);
        }
        
        double modeValue = data[0];
        int maxFrequency = 0;
        
        for (Map.Entry<Double, Integer> entry : frequencyMap.entrySet()) {
            if (entry.getValue() > maxFrequency) {
                maxFrequency = entry.getValue();
                modeValue = entry.getKey();
            }
        }
        
        return modeValue;
    }
    
    public int getModeFrequency() {
        if (dataSize == 0) return 0;
        
        Map<Double, Integer> frequencyMap = new HashMap<>();
        for (double val : data) {
            frequencyMap.put(val, frequencyMap.getOrDefault(val, 0) + 1);
        }
        
        int maxFreq = 0;
        for (int freq : frequencyMap.values()) {
            if (freq > maxFreq) maxFreq = freq;
        }
        return maxFreq;
    }
    
    public double getGeometricMean() {
        if (dataSize == 0) return 0.0;
        
        double logSum = 0.0;
        int validCount = 0;
        
        for (double val : data) {
            if (val > 0) {
                logSum += Math.log(val);
                validCount++;
            }
        }
        
        if (validCount == 0) return 0.0;
        return Math.exp(logSum / validCount);
    }
    
    public double getHarmonicMean() {
        if (dataSize == 0) return 0.0;
        
        double reciprocalSum = 0.0;
        int validCount = 0;
        
        for (double val : data) {
            if (val != 0) {
                reciprocalSum += 1.0 / val;
                validCount++;
            }
        }
        
        if (validCount == 0 || reciprocalSum == 0) return 0.0;
        return validCount / reciprocalSum;
    }
    
    public double getVariance() {
        if (dataSize < 2) return 0.0;
        
        double mean = getMean();
        double sumSquaredDiff = 0.0;
        
        for (double val : data) {
            double diff = val - mean;
            sumSquaredDiff += diff * diff;
        }
        
        return sumSquaredDiff / (dataSize - 1);
    }
    
    public double getPopulationVariance() {
        if (dataSize == 0) return 0.0;
        
        double mean = getMean();
        double sumSquaredDiff = 0.0;
        
        for (double val : data) {
            double diff = val - mean;
            sumSquaredDiff += diff * diff;
        }
        
        return sumSquaredDiff / dataSize;
    }
    
    public double getStandardDeviation() {
        return Math.sqrt(getVariance());
    }
    
    public double getPopulationStdDev() {
        return Math.sqrt(getPopulationVariance());
    }
    
    public double getCoefficientOfVariation() {
        double mean = getMean();
        if (mean == 0) return 0.0;
        return (getStandardDeviation() / Math.abs(mean)) * 100.0;
    }
    
    public double getMeanAbsoluteDeviation() {
        if (dataSize == 0) return 0.0;
        
        double mean = getMean();
        double sumAbsDev = 0.0;
        
        for (double val : data) {
            sumAbsDev += Math.abs(val - mean);
        }
        
        return sumAbsDev / dataSize;
    }
    
    public double getQuartile1() {
        return getPercentile(25);
    }
    
    public double getQuartile2() {
        return getMedian();
    }
    
    public double getQuartile3() {
        return getPercentile(75);
    }
    
    public double getInterquartileRange() {
        return getQuartile3() - getQuartile1();
    }
    
    public double getPercentile(double percentile) {
        if (dataSize == 0) return 0.0;
        if (percentile < 0 || percentile > 100) return 0.0;
        
        double position = (percentile / 100.0) * (dataSize - 1);
        int lowerIndex = (int) Math.floor(position);
        int upperIndex = (int) Math.ceil(position);
        
        if (lowerIndex == upperIndex) {
            return sortedData[lowerIndex];
        }
        
        double fraction = position - lowerIndex;
        return sortedData[lowerIndex] + fraction * (sortedData[upperIndex] - sortedData[lowerIndex]);
    }
    
    public double getSkewness() {
        if (dataSize < 3) return 0.0;
        
        double mean = getMean();
        double stdDev = getStandardDeviation();
        
        if (stdDev == 0) return 0.0;
        
        double sumCubedDiff = 0.0;
        for (double val : data) {
            double normalized = (val - mean) / stdDev;
            sumCubedDiff += Math.pow(normalized, 3);
        }
        
        double n = dataSize;
        double factor = n / ((n - 1) * (n - 2));
        
        return factor * sumCubedDiff;
    }
    
    public double getKurtosis() {
        if (dataSize < 4) return 0.0;
        
        double mean = getMean();
        double stdDev = getStandardDeviation();
        
        if (stdDev == 0) return 0.0;
        
        double sumFourthPower = 0.0;
        for (double val : data) {
            double normalized = (val - mean) / stdDev;
            sumFourthPower += Math.pow(normalized, 4);
        }
        
        double n = dataSize;
        double factor1 = (n * (n + 1)) / ((n - 1) * (n - 2) * (n - 3));
        double factor2 = (3 * Math.pow(n - 1, 2)) / ((n - 2) * (n - 3));
        
        return (factor1 * sumFourthPower) - factor2;
    }
    
    public String getSkewnessInterpretation() {
        double skew = getSkewness();
        if (skew < -1) return "Highly left-skewed";
        if (skew < -0.5) return "Moderately left-skewed";
        if (skew < 0.5) return "Approximately symmetric";
        if (skew < 1) return "Moderately right-skewed";
        return "Highly right-skewed";
    }
    
    public String getKurtosisInterpretation() {
        double kurt = getKurtosis();
        if (kurt < -1) return "Platykurtic (very flat)";
        if (kurt < 0) return "Platykurtic (flat)";
        if (kurt < 1) return "Mesokurtic (normal)";
        if (kurt < 3) return "Leptokurtic (peaked)";
        return "Leptokurtic (very peaked)";
    }
    
    public double[] getMovingAverage(int windowSize) {
        if (dataSize == 0 || windowSize <= 0 || windowSize > dataSize) {
            return new double[0];
        }
        
        int resultSize = dataSize - windowSize + 1;
        double[] movingAvg = new double[resultSize];
        
        double windowSum = 0.0;
        for (int i = 0; i < windowSize; i++) {
            windowSum += data[i];
        }
        movingAvg[0] = windowSum / windowSize;
        
        for (int i = 1; i < resultSize; i++) {
            windowSum = windowSum - data[i - 1] + data[i + windowSize - 1];
            movingAvg[i] = windowSum / windowSize;
        }
        
        return movingAvg;
    }
    
    public double[] getFourPointMovingAverage() {
        return getMovingAverage(4);
    }
    
    public double[] getExponentialMovingAverage(double smoothingFactor) {
        if (dataSize == 0) return new double[0];
        if (smoothingFactor < 0 || smoothingFactor > 1) {
            smoothingFactor = 0.3;
        }
        
        double[] ema = new double[dataSize];
        ema[0] = data[0];
        
        for (int i = 1; i < dataSize; i++) {
            ema[i] = smoothingFactor * data[i] + (1 - smoothingFactor) * ema[i - 1];
        }
        
        return ema;
    }
    
    public double[] getWeightedMovingAverage(int windowSize) {
        if (dataSize == 0 || windowSize <= 0 || windowSize > dataSize) {
            return new double[0];
        }
        
        int resultSize = dataSize - windowSize + 1;
        double[] wma = new double[resultSize];
        
        double weightSum = (windowSize * (windowSize + 1)) / 2.0;
        
        for (int i = 0; i < resultSize; i++) {
            double weightedSum = 0.0;
            for (int j = 0; j < windowSize; j++) {
                int weight = j + 1;
                weightedSum += data[i + j] * weight;
            }
            wma[i] = weightedSum / weightSum;
        }
        
        return wma;
    }
    
    public double getStandardError() {
        if (dataSize == 0) return 0.0;
        return getStandardDeviation() / Math.sqrt(dataSize);
    }
    
    public double[] getZScores() {
        if (dataSize == 0) return new double[0];
        
        double mean = getMean();
        double stdDev = getStandardDeviation();
        
        if (stdDev == 0) {
            double[] zeros = new double[dataSize];
            Arrays.fill(zeros, 0.0);
            return zeros;
        }
        
        double[] zScores = new double[dataSize];
        for (int i = 0; i < dataSize; i++) {
            zScores[i] = (data[i] - mean) / stdDev;
        }
        
        return zScores;
    }
    
    public int countOutliers(double threshold) {
        double[] zScores = getZScores();
        int outlierCount = 0;
        
        for (double z : zScores) {
            if (Math.abs(z) > threshold) {
                outlierCount++;
            }
        }
        
        return outlierCount;
    }
    
    public int countOutliersIQR() {
        if (dataSize == 0) return 0;
        
        double q1 = getQuartile1();
        double q3 = getQuartile3();
        double iqr = q3 - q1;
        
        double lowerBound = q1 - 1.5 * iqr;
        double upperBound = q3 + 1.5 * iqr;
        
        int outlierCount = 0;
        for (double val : data) {
            if (val < lowerBound || val > upperBound) {
                outlierCount++;
            }
        }
        
        return outlierCount;
    }
    
    public double getRootMeanSquare() {
        if (dataSize == 0) return 0.0;
        
        double sumSquares = 0.0;
        for (double val : data) {
            sumSquares += val * val;
        }
        
        return Math.sqrt(sumSquares / dataSize);
    }
    
    public double getSumOfSquares() {
        double sum = 0.0;
        for (double val : data) {
            sum += val * val;
        }
        return sum;
    }
    
    public double getSumOfDeviations() {
        double mean = getMean();
        double sum = 0.0;
        for (double val : data) {
            sum += Math.abs(val - mean);
        }
        return sum;
    }
    
    public double[] getData() {
        return Arrays.copyOf(data, dataSize);
    }
    
    public double[] getSortedData() {
        return Arrays.copyOf(sortedData, dataSize);
    }
    
    public boolean hasData() {
        return dataSize > 0;
    }
    
    public Map<String, Double> getFullAnalysis() {
        Map<String, Double> results = new LinkedHashMap<>();
        
        results.put("Count", (double) getCount());
        results.put("Sum", getSum());
        results.put("Minimum", getMinimum());
        results.put("Maximum", getMaximum());
        results.put("Range", getRange());
        results.put("Mean", getMean());
        results.put("Median", getMedian());
        results.put("Mode", getMode());
        results.put("Mode Frequency", (double) getModeFrequency());
        results.put("Geometric Mean", getGeometricMean());
        results.put("Harmonic Mean", getHarmonicMean());
        results.put("Variance (Sample)", getVariance());
        results.put("Variance (Population)", getPopulationVariance());
        results.put("Std Dev (Sample)", getStandardDeviation());
        results.put("Std Dev (Population)", getPopulationStdDev());
        results.put("Standard Error", getStandardError());
        results.put("Coeff of Variation %", getCoefficientOfVariation());
        results.put("Mean Abs Deviation", getMeanAbsoluteDeviation());
        results.put("Quartile 1 (25%)", getQuartile1());
        results.put("Quartile 2 (50%)", getQuartile2());
        results.put("Quartile 3 (75%)", getQuartile3());
        results.put("Interquartile Range", getInterquartileRange());
        results.put("10th Percentile", getPercentile(10));
        results.put("90th Percentile", getPercentile(90));
        results.put("Skewness", getSkewness());
        results.put("Kurtosis", getKurtosis());
        results.put("Root Mean Square", getRootMeanSquare());
        results.put("Sum of Squares", getSumOfSquares());
        results.put("Outliers (IQR method)", (double) countOutliersIQR());
        results.put("Outliers (Z > 2)", (double) countOutliers(2.0));
        
        return results;
    }
}
