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

    HashMap<Object, FrequencyOpponentModel> opponents = new HashMap<>();
    HashMap<Object, ArrayList<Bid>> accepts = new HashMap<>();
    Bid lastBid = null;
    double n = 0.1;
    ArrayList<Bid> allbids = null;
    
    private double absoluteMinimum = 1;
    private double tries = 20;//Sinus periods
    
    public void init() {
        absoluteMinimum = Math.max(0.2, utilitySpace.getReservationValueUndiscounted());
    }

    private double getUtilityPerFraction(double remaining) {
        double[] points = {0, 0.2, 0.7, 1};
        double[] positions = {0, 0.6, 0.7, 0.3};

        for (int i = 1; i < points.length; i++) {
            if (points[i - 1] < remaining && points[i] > remaining) {
                double subpercentage = (remaining - points[i - 1]) / (points[i] - points[i - 1]);
                double stepsize = positions[i] - positions[i - 1];
                return positions[i - 1] + (subpercentage * stepsize);
            }
        }
        return 1;
    }
    
    private double getMinUtility(double t) {
        double sin = Math.sin(tries*2*Math.PI * t + Math.PI);
        double half = (1 - absoluteMinimum)/2 + absoluteMinimum;
        double dist = 1 - half;
        return 0.8*(1 - (1-absoluteMinimum)*t)+0.2*(half + dist*sin);
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

            System.out.println(getMinUtility(getTimeLine().getTime()));
            System.out.println("-+-+-");
            lastBid = generateBid();
            return new Offer(lastBid);
        } catch (Exception ex) {
            System.err.println("Exception in chooseAction: " + ex.getMessage());
            ex.printStackTrace();
            return new Accept();
        }
    }

    boolean accaptable(Bid offer) {
        Set<Entry<Object, FrequencyOpponentModel>> models = opponents.entrySet();
        for (Entry e : models) {
            ArrayList<Bid> offers = accepts.get(e.getKey());
            if (!offers.isEmpty()) {

                double hostileFriendlyness = getUtility(offers.get(offers.size() - 1));
                double estimatedUtil = ((FrequencyOpponentModel) e.getValue()).estimateUtility(offer);
                if (estimatedUtil < hostileFriendlyness) {
                    return false;

                }
            }
        }
        return true;
    }

    boolean accaptable(double minimal, Bid offer) {
        Set<Entry<Object, FrequencyOpponentModel>> models = opponents.entrySet();
        for (Entry e : models) {
            if (((FrequencyOpponentModel) e.getValue()).estimateUtility(offer) < minimal) {
                return false;
            }
        }
        return true;
    }

    public Bid generateBid() {
        if (allbids == null) {
            allbids = generateAllBids();
        }
        double fractionRemaining = timeline.getTime();
        double hostileUtility = getUtilityPerFraction(fractionRemaining) + Math.random() * 0.05;
        for (int i = 0; i < allbids.size(); i++) {
            if (accaptable(allbids.get(i))) {
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
