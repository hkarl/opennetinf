package netinf.node.resolution.mdht.dht.pastry;

import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.NodeHandle;
import rice.p2p.past.messaging.LookupMessage;

/***
 * Custom LookupMessage for the NetInfPast application. Triggered when an object (IO) is sought for in the PAST layer
 * 
 * @author PG NetInf3
 */
public class NetInfLookupMessage extends LookupMessage {

   private static final long serialVersionUID = 6146689282945987009L;
   private int level;

   public NetInfLookupMessage(int uid, Id id, NodeHandle source, Id dest, int level) {
      super(uid, id, source, dest);
      this.level = level;
   }

   public int getLevel() {
      return level;
   }

}
