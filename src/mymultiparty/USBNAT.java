package mymultiparty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import negotiator.Bid;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.Offer;
import negotiator.parties.AbstractNegotiationParty;


//Idee: Houd een minimumutility bij, verlaag deze aan het begin snel en gooi hem aan het eind weer omhoog
//Gebruik de geschiedenis van bids en accepts van de tegenstander om te kijken bij welke (eigen!) utilities zij zouden accepteren
//Gebruik deze informatie om een bid op te stellen dat voldoet aan onze eigen utility eis en die van de tegenstanders en onze utility maximaliseert.
//Als zon bid niet bestaat: Maximaliseer utilities tegenstander, maar blijf voldoen aan onze eis. 
public class USBNAT extends AbstractNegotiationParty {
    
    HashMap<Object,FrequencyOpponentModel> opponents = new HashMap<>();
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
    @Override
    public Action chooseAction(List<Class<? extends Action>> list) {
        try {
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
            System.out.println(OM);
        }
    }
    
}
