package mymultiparty;

import java.util.ArrayList;
import java.util.HashMap;
import negotiator.issue.Issue;
import negotiator.issue.IssueDiscrete;
import negotiator.issue.IssueInteger;
import negotiator.issue.Value;
import negotiator.issue.ValueInteger;

public class Util {
    public static ArrayList<Value> getValues(Issue issue) {
        ArrayList<Value> ret = new ArrayList();
        
        switch(issue.getType()) {
                case DISCRETE :
                    IssueDiscrete issueD = (IssueDiscrete)issue;
                    for (Value v : issueD.getValues()) {
                        ret.add(v);
                    }
                    break;
                case INTEGER :
                    IssueInteger issueI = (IssueInteger)issue;
                    for (int i = issueI.getLowerBound();i <= issueI.getUpperBound();i++) {
                        ValueInteger value = new ValueInteger(i);
                        ret.add(value);
                    }
                    break;
                default : 
                    throw new RuntimeException("Issuetype " + issue.getType() + " not recognized");
            }
        
        return ret;
    }
}
