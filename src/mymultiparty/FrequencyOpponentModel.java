package mymultiparty;

import java.util.ArrayList;
import java.util.HashMap;
import negotiator.Bid;
import negotiator.Domain;
import negotiator.boaframework.OpponentModel;
import negotiator.issue.Issue;
import negotiator.issue.IssueDiscrete;
import negotiator.issue.IssueInteger;
import negotiator.issue.Value;
import negotiator.issue.ValueInteger;

public class FrequencyOpponentModel{
    private int n;
    
    private ArrayList<HashMap<Value,Integer>> valueFreq;
    private ArrayList<Double> weights;
    
    private Bid previousBid = null;
    
    
    public FrequencyOpponentModel(Domain domain, int n) {
        this.n = n;
        
        ArrayList<Issue> issues = domain.getIssues();
        valueFreq = new ArrayList(issues.size());
        weights = new ArrayList(issues.size());
        
        for (Issue issue : issues) {
            HashMap<Value,Integer> map;
            int numValues;
            switch(issue.getType()) {
                case DISCRETE :
                    IssueDiscrete issueD = (IssueDiscrete)issue;
                    numValues = issueD.getNumberOfValues();
                    map = new HashMap(numValues);
                    for (Value v : issueD.getValues()) {
                        map.put(v, 0);
                    }
                    break;
                case INTEGER :
                    IssueInteger issueI = (IssueInteger)issue;
                    numValues = issueI.getUpperBound()-issueI.getLowerBound() + 1;
                    map = new HashMap(numValues);
                    for (int i = issueI.getLowerBound();i <= issueI.getUpperBound();i++) {
                        ValueInteger value = new ValueInteger(i);
                        map.put(value, 0);
                    }
                    break;
                default : 
                    throw new RuntimeException("Issuetype " + issue.getType() + " is not recognized by FrequencyOpponontModel");
            }
            
            valueFreq.add(issue.getNumber(), map);
            weights.add(issue.getNumber(), 1.0/issues.size());
        }
    }
    
    public void addBid(Bid b) throws Exception {
        for (int i = 0; i < valueFreq.size();i++) {
            HashMap<Value,Integer> freq = valueFreq.get(i);
            Value v = b.getValue(i);
            
            freq.put(v, freq.get(v) + 1);
        }
        
        if (previousBid != null) {
            for (int i = 0; i < weights.size(); i++) {
                if (b.getValue(i).equals(previousBid.getValue(i))) {
                    weights.add(i, weights.get(i) + n);
                }
            }
            normalizeWeights();
        }
    }
    
    private void normalizeWeights() {
        
    }
}
