package Group13;

import negotiator.Bid;
import negotiator.Domain;

/**
 * Frequency model which ignores bids that are equal to the last bid 
 */
public class BetterFOM extends FrequencyOpponentModel {

    public BetterFOM(Domain domain, double n) {
        super(domain, n);
    }

    @Override
    public void addBid(Bid b) throws Exception {
        if (getPreviousBid() == null || !b.equals(getPreviousBid())) {
            super.addBid(b);
        }
    }
    
    
    
}
