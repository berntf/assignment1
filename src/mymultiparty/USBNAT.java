package mymultiparty;

import java.util.ArrayList;
import java.util.Map.Entry;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import negotiator.AgentID;
import negotiator.Bid;
import negotiator.Deadline;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.Offer;
import negotiator.issue.Issue;
import negotiator.issue.Value;
import negotiator.parties.AbstractNegotiationParty;
import negotiator.session.TimeLineInfo;
import negotiator.utility.UtilitySpace;

//Idee: Houd een minimumutility bij, verlaag deze in het algemeen maar fluctueer een beetje omhoog en omlaag om de tegenstander meer te laten conceden
//Gebruik de geschiedenis van bids en accepts van de tegenstander om te kijken bij welke (eigen!) utilities zij zouden accepteren
//Gebruik deze informatie om een bid op te stellen dat voldoet aan onze eigen utility eis en die van de tegenstanders en onze utility maximaliseert.
//Als zon bid niet bestaat: Maximaliseer utilities tegenstander, maar blijf voldoen aan onze eis. 
public class USBNAT extends AbstractNegotiationParty {

    HashMap<Object, FrequencyOpponentModel> opponents = new HashMap<>();
    HashMap<Object, ArrayList<Bid>> accepts = new HashMap<>();
    Bid lastBid = null;
    double n = 0.1;
    ArrayList<Bid> allbids = null;

    private double absoluteMinimum = 1;
    private final double tries = 10;//Sinus periods
    private final double momentum = 0.05;
    private final double start = 0.6;
    
    private boolean even = true;

    @Override
    public void init(UtilitySpace utilSpace, Deadline dl, TimeLineInfo tl, long randomSeed, AgentID agentId) {
        super.init(utilSpace, dl, tl, randomSeed, agentId);

        absoluteMinimum = Math.max(0.2, utilitySpace.getReservationValueUndiscounted());
    }

    private double getMinUtility(double t) {
        double sin = Math.sin(tries * 2 * Math.PI * t + 1.5 * Math.PI);
        double half = (1 - absoluteMinimum) / 2 + absoluteMinimum;
        double dist = 1 - half;
        return 0.7 * (1 - (1 - absoluteMinimum) * t) + 0.3 * (half + dist * sin);
    }

    private HashMap<Object, Double> getMinUtils() {
        HashMap<Object, Double> ret = new HashMap(accepts.size());

        for (Entry<Object, ArrayList<Bid>> entry : accepts.entrySet()) {
            ArrayList<Bid> acc = entry.getValue();
            FrequencyOpponentModel model = opponents.get(entry.getKey());
            double min = 1;

            for (Bid b : acc) {
                double util = model.estimateUtility(b);
                
                if (util < min) {
                    min = util;
                }
            }
            
            ret.put(entry.getKey(), min);
        }
        
        return ret;
    }

    private boolean isAcceptable(Bid b, HashMap<Object,Double> minUtils) {
        for (Entry<Object,Double> entry : minUtils.entrySet()) {
            if (opponents.get(entry.getKey()).estimateUtility(b) < entry.getValue()) {
                return false;
            }
        }
        
        return true;
    }
    
    //Best Acceptable Bid
    private Bid generateBAB(HashMap<Object,Double> minUtils, double myMin) {
        Iterator<Bid> it = allbids.iterator();
        
        while (it.hasNext()) {
            Bid b = it.next();
            
            if (myMin > getUtility(b)) {
                return null;
            }
            
            if (isAcceptable(b, minUtils)) {
                return b;
            }
        }
        
        return null;
    }
    
    //Max Bid over Minimum
    private Bid generateMBM(double minUtility) {
        double max = 0;
        Bid bestBid = null;
        
        for (Bid b : allbids) {
            if (getUtility(b) >= minUtility) {
                double min = 1;
                
                for (FrequencyOpponentModel model : opponents.values()) {
                    double util = model.estimateUtility(b);
                    
                    if (util < min) {
                        min = util;
                    }
                }
                
                if (bestBid == null || max < min) {
                    max = min;
                    bestBid = b;
                }
            }
        }
        
        return bestBid;
    }
    
    private Bid generateBidJ(boolean forced) {
        if (allbids == null) {
            allbids = generateAllBids();
        }
        
        double time = getTimeLine().getTime();
        even = !even;
        
        if (time < start || (even && !forced)) {
            return allbids.get(0);
        }

        HashMap<Object,Double> minUtils = getMinUtils();
        double min = 1;
        
        for (Double util : minUtils.values()) {
            if (util < min) {
                min = util;
            }
        }
        
        double minUtility = Math.max(min-momentum, getMinUtility((time - start)/(1 - start)));
        
        Bid b = generateBAB(minUtils, minUtility);
        
        if (b != null) {
            return b;
        } else {
            return generateMBM(minUtility);
        }
    }

    private ArrayList<Bid> generateAllBids() {
        ArrayList<Issue> issues = utilitySpace.getDomain().getIssues();

        ArrayList<Bid> ret = new ArrayList();

        for (HashMap<Integer, Value> values : getAllBids(issues, 0)) {
            try {
                Bid bid = new Bid(utilitySpace.getDomain(), values);
                ret.add(bid);
            } catch (Exception ex) {
                System.err.println("Could not create bid");
                System.err.println(ex.getMessage());
            }
        }

        Collections.sort(ret, new Comparator<Bid>() {
            @Override
            public int compare(Bid a, Bid b) {
                if (getUtility(b) > getUtility(a)) {
                    return 1;
                } else if (getUtility(b) == getUtility(a)) {
                    return 0;
                } else {
                    return -1;
                }
            }

        });

        return ret;
    }

    public static ArrayList<HashMap<Integer, Value>> getAllBids(ArrayList<Issue> issues, int from) {
        Issue issue = issues.get(from);

        ArrayList<HashMap<Integer, Value>> bids;

        if (from == issues.size() - 1) {
            bids = new ArrayList();
            bids.add(new HashMap());
        } else {
            bids = getAllBids(issues, from + 1);
        }

        ArrayList<Value> values = Util.getValues(issue);

        ArrayList<HashMap<Integer, Value>> ret = new ArrayList();

        for (Value v : values) {
            for (HashMap<Integer, Value> bid : bids) {
                HashMap<Integer, Value> newBid = new HashMap(bid);
                newBid.put(issue.getNumber(), v);
                ret.add(newBid);
            }
        }

        return ret;
    }

    @Override
    public Action chooseAction(List<Class<? extends Action>> list) {
        try {            
            if (getUtility(generateBidJ(true)) > getUtility(lastBid)) {
                lastBid = generateBidJ(false);
                return new Offer(lastBid);
            } else {
                return new Accept();
            }
        } catch (Exception ex) {
            System.err.println("Exception in chooseAction: " + ex.getMessage());
            ex.printStackTrace();
            return new Accept();
        }
    }

    @Override
    public void receiveMessage(Object sender, Action action) {
        super.receiveMessage(sender, action);
        
        if ("Protocol".equals(sender)) {
            return;
        }

        if (!opponents.containsKey(sender)) {
            opponents.put(sender, new BetterFOM(getUtilitySpace().getDomain(), n));
            accepts.put(sender, new ArrayList<Bid>());
        }

        if (action instanceof Offer) {
            lastBid = ((Offer) action).getBid();
            FrequencyOpponentModel OM = opponents.get(sender);
            try {
                OM.addBid(lastBid);
            } catch (Exception ex) {
                System.err.println("Exception in receiveMessage: " + ex.getMessage());
            }

            accepts.get(sender).add(lastBid);
        } else if (action instanceof Accept) {
            accepts.get(sender).add(lastBid);
        }
    }

}
