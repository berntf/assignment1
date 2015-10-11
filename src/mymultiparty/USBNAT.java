package mymultiparty;

import java.util.HashMap;
import java.util.List;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.Offer;
import negotiator.parties.AbstractNegotiationParty;

public class USBNAT extends AbstractNegotiationParty {
    
    HashMap<Object,FrequencyOpponentModel> opponents = new HashMap<>();
    double n = 0.1;

    @Override
    public Action chooseAction(List<Class<? extends Action>> list) {
        try {
            return new Offer(getUtilitySpace().getMaxUtilityBid());
        } catch (Exception ex) {
            System.err.println("Exception in chooseAction: " + ex.getMessage());
            return new Accept();
        }
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
