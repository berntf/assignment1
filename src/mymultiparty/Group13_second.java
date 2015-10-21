package mymultiparty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import negotiator.AgentID;
import negotiator.Bid;
import negotiator.Deadline;

import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.Offer;
import negotiator.issue.ISSUETYPE;
import negotiator.issue.Issue;
import negotiator.issue.IssueDiscrete;
import negotiator.issue.Value;
import negotiator.issue.ValueDiscrete;
import negotiator.parties.AbstractNegotiationParty;
import negotiator.session.TimeLineInfo;
import negotiator.utility.UtilitySpace;

/**
 * This is your negotiation party.
 */
public class Group13_second extends AbstractNegotiationParty {

    private double minUtility = 0.8;
    private double lastBid = 0;
    
    private ArrayList<Bid> allowedBids = null;
    private Random rng = new Random();

    @Override
    public void init(UtilitySpace utilSpace, Deadline dl, TimeLineInfo tl, long randomSeed, AgentID agentId) {
        super.init(utilSpace, dl, tl, randomSeed, agentId);
        
        minUtility = Math.max(minUtility, utilitySpace.getReservationValueUndiscounted());
    }
    
    public void initBids() throws Exception {        
        ArrayList<Issue> issues = utilitySpace.getDomain().getIssues();
        
        allowedBids = new ArrayList();
        
        for (HashMap<Integer,Value> values : getAllBids(issues, 0)) {
            Bid bid = new Bid(utilitySpace.getDomain(), values);
            if (getUtility(bid) >= minUtility) {
                allowedBids.add(bid);
            }
        }
         
    }
    
    public static ArrayList<HashMap<Integer,Value>> getAllBids(ArrayList<Issue> issues, int from) throws Exception {        
        Issue issue = issues.get(from);
        
        if (issue.getType() != ISSUETYPE.DISCRETE) {
            throw new Exception("Issuetype " + issue.getType() + " not supported");
        }
        IssueDiscrete issueD = (IssueDiscrete)issue;
        
        
        ArrayList<HashMap<Integer,Value>> bids;
        
        if (from == issues.size()-1) {
            bids = new ArrayList();
            bids.add(new HashMap());
        } else {
            bids = getAllBids(issues, from+1);
        }
        
        ArrayList<HashMap<Integer,Value>> ret = new ArrayList();
        
        for (ValueDiscrete v : issueD.getValues()) {
            for (HashMap<Integer,Value> bid : bids) {
                HashMap<Integer,Value> newBid = new HashMap(bid);
                newBid.put(issueD.getNumber(), v);
                ret.add(newBid);
            }
        }
        
        return ret;
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
     * All offers proposed by the other parties will be received as a message.
     * You can use this information to your advantage, for example to predict
     * their utility.
     *
     * @param sender The party that did the action.
     * @param action The action that party did.
     */
    @Override
    public void receiveMessage(Object sender, Action action) {
        super.receiveMessage(sender, action);

        if (action instanceof Offer) {
            lastBid = getUtility(((Offer) action).getBid());
        }
    }

    private boolean shouldAccept(double utility) {
        return utility >= minUtility;
    }

    private Bid generateBid() throws Exception {
        if (allowedBids == null) initBids();
        return allowedBids.get(rng.nextInt(allowedBids.size()));
    }

    @Override
    public String getDescription() {
        return "Negotiator Group 13 second";
    }

}
