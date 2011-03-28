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
    *           identifier of the IO that has to be putted
    * @return the address of the responsible node
    */
   InetSocketAddress getResponsibleNode(Identifier id);

   /**
    * 
    */
   void joinRing(InetSocketAddress bootstrapAddress);

   /**
    * 
    */
   void leaveRing();
}
