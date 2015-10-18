package mymultiparty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import static mymultiparty.Group13.getAllBids;
import negotiator.Bid;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.Offer;
import negotiator.issue.ISSUETYPE;
import negotiator.issue.Issue;
import negotiator.issue.IssueDiscrete;
import negotiator.issue.Value;
import negotiator.issue.ValueDiscrete;
import negotiator.parties.AbstractNegotiationParty;


//Idee: Houd een minimumutility bij, verlaag deze aan het begin snel en gooi hem aan het eind weer omhoog
//Gebruik de geschiedenis van bids en accepts van de tegenstander om te kijken bij welke (eigen!) utilities zij zouden accepteren
//Gebruik deze informatie om een bid op te stellen dat voldoet aan onze eigen utility eis en die van de tegenstanders en onze utility maximaliseert.
//Als zon bid niet bestaat: Maximaliseer utilities tegenstander, maar blijf voldoen aan onze eis. 
public class USBNAT extends AbstractNegotiationParty {
    
    HashMap<Object,FrequencyOpponentModel> opponents = new HashMap<>();
    HashMap<Object,ArrayList<Bid>> accepts;
    double n = 0.1;
    ArrayList<Bid> allbids;
    
    
    private double getUtilityPerFraction(double remaining){
    	double[] points ={0,0.2,0.7,1};
    	double[] positions ={0,0.6,0.7,0.3};
    	
    	for (int i=1;i<points.length;i++){
    		if(points[i-1]<remaining&&points[i]>remaining){
    			double subpercentage=(remaining-points[i-1])/(points[i]-points[i-1]);
    			double stepsize=positions[i]-positions[i-1];
    			return positions[i-1]+(subpercentage*stepsize);
    		}
    	}
    	return 1;
    }
    
    private ArrayList<Bid> generateAllBids() throws Exception {
        ArrayList<Issue> issues = utilitySpace.getDomain().getIssues();
        
        ArrayList<Bid> ret = new ArrayList();
        
        for (HashMap<Integer,Value> values : getAllBids(issues, 0)) {
            Bid bid = new Bid(utilitySpace.getDomain(), values);
            ret.add(bid);
        }
        
        Collections.sort(ret, new Comparator<Bid>() {
            @Override
            public int compare(Bid a, Bid b) {
                if (getUtility(b) > getUtility(a)) {
                    return 1;
                } else if(getUtility(b) == getUtility(a)) {
                    return 0;
                } else {
                    return -1;
                }
            }
            
        });
        
        return ret;
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
    
    @Override
    public Action chooseAction(List<Class<? extends Action>> list) {
        try {
            
            System.out.println(getUtilityPerFraction(getTimeLine().getTime()));
            System.out.println("-+-+-");
            
            return new Offer(getUtilitySpace().getMaxUtilityBid());
        } catch (Exception ex) {
            System.err.println("Exception in chooseAction: " + ex.getMessage());
            return new Accept();
        }
    }
    boolean accaptable(double minimal,Bid offer){
    	Set<Entry <Object,FrequencyOpponentModel>> models=opponents.entrySet();
    	for(Entry e : models){
    		if(((FrequencyOpponentModel)e.getValue()).estimateUtility(offer)<minimal){
    			return false;
    		}
    	}
    	return true;
    }
    public Bid generateBid(){
        double fractionRemaining=timeline.getTime();
        double hostileUtility=getUtilityPerFraction(fractionRemaining);
        for(int i=0;i<allbids.size();i++){
        	if(accaptable(hostileUtility,allbids.get(i))){
        		return allbids.get(i);
        	}
        }
		return allbids.get(0);
    }
    @Override
    public void receiveMessage(Object sender, Action action) {
        super.receiveMessage(sender, action);
        
        if (!opponents.containsKey(sender)) {
            opponents.put(sender, new FrequencyOpponentModel(getUtilitySpace().getDomain(), n));
        }

        if (action instanceof Offer) {
            FrequencyOpponentModel OM = opponents.get(sender);
            try {
                OM.addBid(((Offer)action).getBid());
            } catch (Exception ex) {
                System.err.println("Exception in receiveMessage: " + ex.getMessage());
            }
        }
    }
    
}
