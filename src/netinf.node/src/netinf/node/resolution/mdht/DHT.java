/**
 * 
 */
package netinf.node.resolution.mdht;

import java.net.InetSocketAddress;

import netinf.common.datamodel.Identifier;

/**
 * @author PG NetInf 3
 */
public interface DHT {

   /**
    * @param id
    *           the corresponding identifier
    * @return the Id of the responsible node
    */
   public String getResponsibleNode(Identifier id);

   /**
    * 
    */
   public void joinRing(InetSocketAddress address);

   /**
    * 
    */
   public void leaveRing();
}
