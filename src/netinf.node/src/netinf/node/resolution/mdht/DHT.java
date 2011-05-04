/**
 * 
 */
package netinf.node.resolution.mdht;

import java.net.InetSocketAddress;

import netinf.common.datamodel.InformationObject;
import rice.p2p.past.PastContentHandle;

/**
 * @author PG NetInf 3
 */
public interface DHT {

   /**
    * @param io
    *           identifier of the IO that has to be putted
    * @return the address of the responsible node
    */
   InetSocketAddress getResponsibleNode(InformationObject io);

   /**
    * 
    */
   void join(InetSocketAddress bootstrapAddress);

   /**
    * @param o
    * @return
    */
   public PastContentHandle put(InformationObject o);

   /**
    * @param contentKey
    * @return
    */
   public String get(PastContentHandle contentKey);

   /**
    * Return IO after searching for it in PAST. Only valid after get() call and only until next get() call
    * 
    * @return
    */
   public InformationObject getReturnedIOFromPast();

   /**
    * 
    */
   void leave();
}
