/**
 * 
 */
package netinf.node.resolution.mdht;

import java.util.ArrayList;

import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.Message;
import rice.p2p.commonapi.NodeHandle;
import rice.p2p.past.PastContent;

/**
 * @author NetInf3 at the University of Paderborn
 * @since 2011
 * Note: Only CommonAPI functionality is used here. For more information on CommonAPI see the paper:
 * 
 * Towards a Common API for Structured Peer-to-Peer Overlays, 
 * by F. Dabek, B. Zhao, P. Druschel, J. Kubiatowicz, and I. Stoica, 
 * second International Workshop on Peer-to-Peer Systems, Berkeley, CA, February 2003
 */
public class NetInfDHTMessage implements Message {

   /**
    * Automatically generated field for Serializable object NetInfDHTMessage
    */
   private static final long serialVersionUID = 6203375763932442376L;
   //Source of message
   private Id from;
   //Destination of message
   private Id to;
   
   private ArrayList<NodeHandle> passport;
   
   private DummyPastContent content;
   
   /**
    * 
    * @param from Source of the message
    * @param to Optional: Where the message should be routed to. 
    * @param content The actual IO
    */
   
   public NetInfDHTMessage(Id from, Id to, PastContent content)
   {
      this.from = from;
      this.to = to;
      if(content instanceof DummyPastContent)
      {
	 this.content = (DummyPastContent)content;
      }
      else
	 this.content = null;
      this.passport = new ArrayList<NodeHandle>();
   }
   
   /**
    * Method stores the NodeHandle of each node as it passes through. This way the path of the message is known
    * @param hop Current NodeHandle
    */
   public void addHop(NodeHandle hop)
   {
      passport.add(hop);
   }
   
   /* (non-Javadoc)
    * @see rice.p2p.commonapi.Message#getPriority()
    */
   @Override
   public int getPriority() {
      return Message.LOW_PRIORITY;
   }


   /* (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {
      StringBuilder path = new StringBuilder("Message Path: ");
      
      for (int i = 0; i < this.passport.size(); i++) 
      {
	 path.append(this.passport.get(i) + ",");
      }

      return "DHT-SUBSYS: NetInfRouteMessage from nodeId" + from + " to " + to + path; 
   }

   /**
    * @return the content
    */
   public DummyPastContent getContent() {
      return content;
   }

   /**
    * @return the from
    */
   public Id getFrom() {
      return from;
   }

   /**
    * @return the to
    */
   public Id getTo() {
      return to;
   }

}
