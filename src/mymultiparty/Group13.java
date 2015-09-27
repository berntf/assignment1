package mymultiparty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import negotiator.Bid;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.Offer;
import negotiator.issue.ISSUETYPE;
import negotiator.issue.Issue;
import negotiator.issue.IssueDiscrete;
import negotiator.issue.IssueInteger;
import negotiator.issue.IssueReal;
import negotiator.issue.Value;
import negotiator.issue.ValueInteger;
import negotiator.issue.ValueReal;
import negotiator.parties.AbstractNegotiationParty;

/**
 * This is your negotiation party.
 */
public class Group13 extends AbstractNegotiationParty {

    private double minUtility = 0.8;
    private double lastBid = 0;
    private ArrayList<Offer> oldOffers = new ArrayList<Offer>();
    Bid OurLast;
    public void init() {
        minUtility = Math.max(minUtility, utilitySpace.getReservationValueUndiscounted());
    }

    /**
     * Each round this method gets called and ask you to accept or offer. The
     * first party in the first round is a bit different, it can only propose an
     * offer.
     *
     * @param validActions Either a list containing both accept and offer or
     * only offer.
     * @return The chosen action.
     */
    @Override
    public Action chooseAction(List<Class<? extends Action>> validActions) {
        try {
            if (validActions.contains(Accept.class) && shouldAccept(lastBid)) {
                return new Accept();
            } else {
                return new Offer(generateBid());
            }
        } catch (Exception ex) {
            System.err.println("Exception in chooseAction: " + ex.getMessage());
            return new Accept();
        }
    }

    /**
     * concedes the least painfull point
     * @param ourLast	offer we currently have
     * @param currentOther offer the opponent current has
     * @return a compromise
     */
    public Bid generateLeastPainfullCompromise(Bid ourLast, Offer currentOther){
    	Bid best=currentOther.getBid();
    	int index=0;
    	Iterator<Entry<Integer, Value>> it = currentOther.getBid().getValues().entrySet().iterator();
    	HashMap<Integer,Value> ours = ourLast.getValues();
    	while(it.hasNext()){
    		Entry<Integer,Value> issue=it.next();
    		if(!issue.getValue().equals(ours.get(issue.getKey()))){
    			Bid compromise = new Bid(ourLast);
    			compromise.setValue(issue.getKey(), issue.getValue());
    			if(getUtility(compromise)>getUtility(best)){
    				best=compromise;
    			}
    		}
    	}
		return best;
    	
    	
    	
    }
    /**
     * All offers proposed by the other parties will be received as a message.
     * You can use this information to your advantage, for example to predict
     * their utility.
     *
     * @param sender The party that did the action.
     * @param action The action that party did.
     **/
    @Override
    public void receiveMessage(Object sender, Action action) {
        super.receiveMessage(sender, action);

        if (action instanceof Offer) {
        	oldOffers.add((Offer)action);
            lastBid = getUtility(((Offer) action).getBid());
        }
    }

    private boolean shouldAccept(double utility) {
        return utility >= minUtility;
    }

    private Bid generateBid() throws Exception {
    	if(oldOffers.size()==0)
    		return OurLast=utilitySpace.getMaxUtilityBid();
    	return OurLast=generateLeastPainfullCompromise(OurLast,oldOffers.get(oldOffers.size()-1));
    }

    @Override
    public String getDescription() {
        return "Negotiator Group 13";
    }

}
