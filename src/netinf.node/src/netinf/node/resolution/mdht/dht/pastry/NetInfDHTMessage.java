package netinf.node.resolution.mdht.dht.pastry;

import java.io.IOException;
import java.io.Serializable;

import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.Message;
import rice.p2p.commonapi.NodeHandle;
import rice.p2p.commonapi.rawserialization.OutputBuffer;
import rice.p2p.util.rawserialization.JavaSerializer;

/**
 * @author PG NetInf3
 */
public class NetInfDHTMessage implements Message, Serializable {

   private static final long serialVersionUID = 6466970856585627245L;

   // Pastry ID of the sender node
   private Id from;

   // Pastry ID of the target node
   private Id to;

   public NetInfDHTMessage(NodeHandle from, Id to) {
      this.from = from.getId();
      this.to = to;
   }

   /*
    * (non-Javadoc)
    * @see rice.p2p.commonapi.Message#getPriority()
    */
   @Override
   public int getPriority() {
      return Message.DEFAULT_PRIORITY;
   }

   public void serialize(OutputBuffer buf) throws IOException {
      JavaSerializer.serialize(this, buf);
   }

   @Override
   public String toString() {
      return "NetInfDHTMessage from " + this.from + " destined for " + this.to;
   }
}
