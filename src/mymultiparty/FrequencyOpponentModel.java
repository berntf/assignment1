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
    public ArrayList<HashMap<Value,Double>> values;
    public ArrayList<Double> weights;
    
    
    public FrequencyOpponentModel(Domain domain) {
        ArrayList<Issue> issues = domain.getIssues();
        values = new ArrayList(issues.size());
        weights = new ArrayList(issues.size());
        
        for (Issue issue : issues) {
            HashMap<Value,Double> map;
            int numValues;
            switch(issue.getType()) {
                case DISCRETE :
                    IssueDiscrete issueD = (IssueDiscrete)issue;
                    numValues = issueD.getNumberOfValues();
                    map = new HashMap(numValues);
                    for (Value v : issueD.getValues()) {
                        map.put(v, 1.0/numValues);
                    }
                    break;
                case INTEGER :
                    IssueInteger issueI = (IssueInteger)issue;
                    numValues = issueI.getUpperBound()-issueI.getLowerBound() + 1;
                    map = new HashMap(numValues);
                    for (int i = issueI.getLowerBound();i <= issueI.getUpperBound();i++) {
                        ValueInteger value = new ValueInteger(i);
                        map.put(value, 1.0/numValues);
                    }
                    break;
                default : 
                    throw new RuntimeException("Issuetype " + issue.getType() + " is not recognized by FrequencyOpponontModel");
            }
            
            values.add(map);
            weights.add(1.0/issues.size());
        }
    }
}
