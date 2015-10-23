package mymultiparty;

import Group13.BetterFOM;
import java.util.LinkedList;

import negotiator.Bid;
import negotiator.Domain;

public class ThijsFreqMod extends BetterFOM{

	public ThijsFreqMod(Domain domain, double n) {
		super(domain, n);
		rejected=new LinkedList<Bid>();
	}
	
	LinkedList<Bid> rejected;
	
	public void BidRejected(Bid b){
	
		rejected.push(b);
	}
	
	public double estimateUtility(Bid b) {
	    double ret = 0;
	    for(int i=0;i<Math.min(10, rejected.size());i++){
	    	ret=-0.1;
	    }
	    for (int i = 0; i < issueModels.size(); i++) {
	        try {
	            ret += weights.get(i) * issueModels.get(i).estimateUtility(b.getValue(issueNumbers.get(i)));
	        } catch (Exception ex) {
	            System.err.println("Exception while estimating utility: " + ex.getMessage());
	        }
	    }
	
	    return ret;
	}
	
	
	

}
