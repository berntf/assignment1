package Group13;

import negotiator.issue.IssueInteger;
import negotiator.issue.ValueInteger;

public class IntegerIssueModel implements IssueModel<ValueInteger> {
    private double minFreq;
    private double maxFreq;
    private int min;
    private int max;
    private int half;
    
    public IntegerIssueModel(IssueInteger i) {
        minFreq = maxFreq = 0;
        
        min = i.getLowerBound();
        max = i.getUpperBound();
        
        half = min + (max - min)/2;
    }

    @Override
    public void addBid(ValueInteger v) {
        if (v.getValue() < half) {
            minFreq += (half - v.getValue())/(half - min);
        } else if(v.getValue() > half) {
            maxFreq += (v.getValue() - half)/(max - half);
        }
    }

    @Override
    public double estimateUtility(ValueInteger v) {
        if (minFreq == 0 && maxFreq == 0) {
            return 1;
        }
        
        double m = minFreq >= maxFreq ? minFreq : maxFreq;
        
        double div = (max - min) * m;
        
        return ((max - v.getValue()) * minFreq + (v.getValue() - min) * maxFreq)/div; 
    }

    @Override
    public String toString() {
        double m = minFreq >= maxFreq ? minFreq : maxFreq;
        
        return min + ": " + (minFreq / m) + ", " + max + ": " + (maxFreq / m); 
    }
    
}
