/**
 * 
 */
package netinf.node.resolution.mdht;

import netinf.common.datamodel.Identifier;

/**
 * @author PG NetInf 3
 */
public interface DHT {

   /**
    * @param the
    *           corresponding identifier
    * @return the Id of the responsible node
    */
   public String getResponsibleNode(Identifier id);

   /**
    * 
    */
   public void joinRing();

   /**
    * 
    */
   public void leaveRing();
}
