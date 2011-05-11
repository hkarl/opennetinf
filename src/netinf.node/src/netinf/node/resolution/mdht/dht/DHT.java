package netinf.node.resolution.mdht.dht;

import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.InformationObject;

/**
 * Generic interface which specifies common functionalities for all used DHT systems.
 * 
 * @author PG NetInf 3
 */
public interface DHT {
   
   /**
    * Attempts to join a DHT. If boot address is the same as the node's own address then the node will initialize a new ring. 
    * 
    * @throws InterruptedException
    */
   void join() throws InterruptedException;

   /**
    * Attempts to put the given InformationObject into the DHT.
    * 
    * @param io The InformationObject to be stored
    */
   public void put(InformationObject io);

   /**
    * Attempts to get the InformationObject by the specified NetInf Identifier.
    * 
    * @param id The NetInf Identifier of the desired InformationObject
    * @return The corresponding InformationObject if stored in DHT, {@code null} otherwise
    */
   public InformationObject get(Identifier id);

   /**
    * Leave the current DHT.
    */
   void leave();
}
