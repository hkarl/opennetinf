package netinf.node.cache.network;

import netinf.common.datamodel.DataObject;

/**
 * Interface for an in-network-cache that can be used by a NetInf node
 * 
 * @author PG NetInf 3, University of Paderborn
 */
public interface NetworkCache {

   /**
    * Stores a DO in the cache and adds the locator to the DO
    * 
    * @param dataObject
    *           the DataObject that should be stored
    */
   void cache(DataObject dataObject);

   /**
    * Gets the BO related to the IO
    * 
    * @param dataObject
    *           the given DataObject
    * @return the corresponding BO
    */
   byte[] getBObyIO(DataObject dataObject);

   /**
    * Checks if the cache contains a specific BO
    * 
    * @param hashfOfBO
    *           the hash-value of the bitlevel-object
    * @return true if BO exists, otherwise false
    */
   boolean contains(String hashfOfBO);

   /**
    * checks if the cache is connected
    * 
    * @return true if the cache is successfully connected, otherwise false
    */
   boolean isConnected();
   
   String getAddress();

}
