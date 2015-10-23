package Group13;

import java.util.HashMap;
import negotiator.issue.IssueDiscrete;
import negotiator.issue.ValueDiscrete;

public class DiscreteIssueModel implements IssueModel<ValueDiscrete> {

    private HashMap<ValueDiscrete, Integer> frequency;

    public DiscreteIssueModel(IssueDiscrete i) {
        frequency = new HashMap(i.getNumberOfValues());

        for (ValueDiscrete v : i.getValues()) {
            frequency.put(v, 0);
        }
    }

    @Override
    public void addBid(ValueDiscrete v) {
        int f = frequency.get(v);

        frequency.put(v, f + 1);
    }

    @Override
    public double estimateUtility(ValueDiscrete v) {
        Integer max = Util.getMaximum(frequency.values());

        return frequency.get(v) / (double) max;
    }

    @Override
    public String toString() {
        String ret = "";

        Integer max = Util.getMaximum(frequency.values());
        
        if (max == 0) {
            return "Not enough data\n";
        }

        for (ValueDiscrete v : frequency.keySet()) {
            ret += "   " + v.toString() + " : " + frequency.get(v) / (double)max + "\n";
        }
        
        return ret;
    }

}
