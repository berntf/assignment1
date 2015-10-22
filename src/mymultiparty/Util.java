package mymultiparty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import negotiator.issue.Issue;
import negotiator.issue.IssueDiscrete;
import negotiator.issue.IssueInteger;
import negotiator.issue.Value;
import negotiator.issue.ValueInteger;
import negotiator.session.TimeLineInfo;
import negotiator.session.Timeline;

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
    
    public static double estimatedRoundsLeft(TimeLineInfo time, int currentRound) {
        if (time.getType() == Timeline.Type.Rounds) {
            return time.getTotalTime() - currentRound;
        } else {
            return (time.getTotalTime() - time.getCurrentTime())/ (time.getCurrentTime()/currentRound);
        }        
    }
    
    public static <T extends Number> double getSum(Collection<T> c) {
        double sum = 0;
        for (T t : c) {
            sum += t.doubleValue();
        }

        return sum;
    }
    
    public static <T extends Comparable> T getMaximum(Collection<T> c) {
        T max = null;
        
        for (T t : c) {
            if (max == null || t.compareTo(max) > 0) {
                max = t;
            }
        }
        
        return max;
    }
}
