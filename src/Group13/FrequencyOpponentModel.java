package Group13;

import java.util.ArrayList;

import negotiator.Bid;
import negotiator.Domain;
import negotiator.issue.Issue;
import negotiator.issue.IssueDiscrete;
import negotiator.issue.IssueInteger;
import negotiator.issue.Value;

/**
 * Models an opponent using the frequency model
 */
public class FrequencyOpponentModel {

    private double n;

    //issueModels contains (per issue) the model for that specific issue.
    protected ArrayList<IssueModel> issueModels;
    //weights contains the estimated weights
    protected ArrayList<Double> weights;
    //issueNumbers contains the issue number for each issue
    protected ArrayList<Integer> issueNumbers;

    protected Bid previousBid = null;

    /**
     * Constructs a model for a single opponent. Domain can only contain
     * Discrete or Integer issues, but all the default domains only contain
     * these types.
     */
    public FrequencyOpponentModel(Domain domain, double n) {
        this.n = n;

        ArrayList<Issue> issues = domain.getIssues();
        issueModels = new ArrayList(issues.size());
        weights = new ArrayList(issues.size());
        issueNumbers = new ArrayList(issues.size());

        for (Issue issue : issues) {
            switch(issue.getType()) {
                case DISCRETE :
                    issueModels.add(new DiscreteIssueModel((IssueDiscrete)issue));
                    break;
                case INTEGER :
                    issueModels.add(new IntegerIssueModel((IssueInteger)issue));
                    break;
                default :
                    throw new RuntimeException("Unknown Issue Type: " + issue.getType());
            }            
            
            weights.add(1.0 / issues.size());
            issueNumbers.add(issue.getNumber());
        }
    }

    public double estimateUtility(Bid b) {
        double ret = 0;

        for (int i = 0; i < issueModels.size(); i++) {
            try {
                ret += weights.get(i) * issueModels.get(i).estimateUtility(b.getValue(issueNumbers.get(i)));
            } catch (Exception ex) {
                System.err.println("Exception while estimating utility: " + ex.getMessage());
            }
        }

        return ret;
    }

    public void addBid(Bid b) throws Exception {
        for (int i = 0; i < issueModels.size(); i++) {
            Value v = b.getValue(issueNumbers.get(i));
            issueModels.get(i).addBid(v);
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
        double sum = Util.getSum(weights);

        for (int i = 0; i < weights.size(); i++) {
            weights.set(i, weights.get(i) / sum);
        }
    }

    public Bid getPreviousBid() {
        return previousBid;
    }
    
    
    @Override
    public String toString() {
        String ret = "";

        for (int i = 0; i < issueModels.size(); i++) {
            ret += "Issue " + issueNumbers.get(i) + " (weight = " + weights.get(i) + " )" + ":\n";

            ret += issueModels.get(i).toString();
        }

        return ret;
    }
}
