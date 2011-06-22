package netinf.node.resolution.mdht.dht;

import rice.p2p.commonapi.Id;
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
   public void put(InformationObject io, int level, int maxlevels, byte[] sourceAddress);

   /**
    * Attempts to get the InformationObject by the specified NetInf Identifier.
    * 
    * @param id The NetInf Identifier of the desired InformationObject
    * @param level The level/ring at which this request should take place
    * @return The corresponding InformationObject if stored in DHT, {@code null} otherwise
    */
   public InformationObject get(Identifier id, int level);
   
   /**
    * Attempts to get the InformationObject by the specified Commonapi Identifier.
    * 
    * @param id The Id (e.g. FreePastry ID) of the desired InformationObject
    * @param level The level/ring at which this request should take place
    * @return The corresponding InformationObject if stored in DHT, {@code null} otherwise
    */
   public InformationObject get(Id id, int level);

   /**
    * Leave the current DHT.
    */
   void leave();
}
