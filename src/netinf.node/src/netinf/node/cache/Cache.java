package netinf.node.cache;

import netinf.common.datamodel.DataObject;

/**
 * General Cache Interface
 * 
 * @author PG NetInf 3
 */
public interface Cache {
   /**
    * Stores a DO in the cache and adds the locator to the DO
    * 
    * @param dataObject
    *           the DataObject that should be stored
    * @param downloadedTmpFile
    *           The path to the temp file (used for caching).
    */
   boolean cache(DataObject dataObject, String downloadedTmpFile);

   /**
    * Checks if the cache contains a specific BO
    * 
    * @param hashfOfBO
    *           the hash-value of the BO
    * @return true if BO exists, otherwise false
    */
   boolean contains(String hashfOfBO);

   /**
    * checks if the cache is connected
    * 
    * @return true if the cache is successfully connected, otherwise false
    */
   boolean isConnected();

   /**
    * Provides the base address of the cache
    * 
    * @return The address as a string.
    */
   String getAddress();

   /**
    * Provides the name of the cache.
    * 
    * @return Name as string
    */
   String getName();
}
