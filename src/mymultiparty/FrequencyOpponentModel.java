package mymultiparty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import negotiator.Bid;
import negotiator.Domain;
import negotiator.issue.Issue;
import negotiator.issue.IssueDiscrete;
import negotiator.issue.IssueInteger;
import negotiator.issue.Value;
import negotiator.issue.ValueInteger;

/**
 * Models an opponent using the frequency model
 */
public class FrequencyOpponentModel {

    private double n;

    //valueFreq contains (per issue) the frequencies of all values
    private ArrayList<HashMap<Value, Integer>> valueFreq;
    //weights contains the estimated weights
    private ArrayList<Double> weights;
    //issueNumbers contains the issue number for each issue
    private ArrayList<Integer> issueNumbers;

    private Bid previousBid = null;

    /**
     * Constructs a model for a single opponent. Domain can only contain
     * Discrete or Integer issues, but all the default domains only contain
     * these types.
     */
    public FrequencyOpponentModel(Domain domain, double n) {
        this.n = n;

        ArrayList<Issue> issues = domain.getIssues();
        valueFreq = new ArrayList(issues.size());
        weights = new ArrayList(issues.size());
        issueNumbers = new ArrayList(issues.size());

        for (Issue issue : issues) {
            ArrayList<Value> values = Util.getValues(issue);

            HashMap<Value, Integer> map = new HashMap(values.size());

            for (Value v : values) {
                map.put(v, 0);
            }

            valueFreq.add(map);
            weights.add(1.0 / issues.size());
            issueNumbers.add(issue.getNumber());
        }
    }

    public double estimateUtility(Bid b) {
        double ret = 0;

        for (int i = 0; i < valueFreq.size(); i++) {
            HashMap<Value, Integer> freq = valueFreq.get(i);
            Value v = null;
            try {
                v = b.getValue(issueNumbers.get(i));
            } catch (Exception e) {
                e.printStackTrace();
            }
            double sum = getSum(freq.values());

            ret += weights.get(i) * freq.get(v) / sum;
        }

        return ret;
    }

    private <T extends Number> double getSum(Collection<T> c) {
        double sum = 0;
        for (T t : c) {
            sum += t.doubleValue();
        }

        return sum;
    }

    public void addBid(Bid b) throws Exception {
        for (int i = 0; i < valueFreq.size(); i++) {
            HashMap<Value, Integer> freq = valueFreq.get(i);
            Value v = b.getValue(issueNumbers.get(i));

            freq.put(v, freq.get(v) + 1);
        }

        if (previousBid != null) {
            for (int i = 0; i < weights.size(); i++) {
                if (b.getValue(issueNumbers.get(i)).equals(previousBid.getValue(issueNumbers.get(i)))) {
                    weights.set(i, weights.get(i) + n);
                }
            }
            normalizeWeights();
        }

        previousBid = b;
    }

    private void normalizeWeights() {
        double sum = getSum(weights);

        for (int i = 0; i < weights.size(); i++) {
            weights.set(i, weights.get(i) / sum);
        }
    }

    @Override
    public String toString() {
        String ret = "";

        for (int i = 0; i < valueFreq.size(); i++) {
            ret += "Issue " + issueNumbers.get(i) + " (weight = " + weights.get(i) + " )" + ":\n";

            HashMap<Value, Integer> freq = valueFreq.get(i);
            double sum = getSum(freq.values());

            for (Value v : freq.keySet()) {
                ret += "   " + v.toString() + " : " + freq.get(v) / sum + "\n";
            }
        }

        return ret;
    }
}
