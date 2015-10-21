package mymultiparty;

import negotiator.Bid;
import negotiator.Domain;

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
