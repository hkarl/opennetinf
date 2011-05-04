package netinf.node.cache.network.impl;

/**
 * Interface for an cache adapter that can be used by a NetInfCache
 * 
 * @author PG NetInf 3, University of Paderborn
 */
public interface CacheServer {

   /**
    * Caches a given BO in the Server
    * 
    * @param bo
    *           the bitlevel-object
    * @param hashOfBO
    *           the hash-value of the given BO
    * @return true if the operation was successfully executed, otherwise false
    */
   boolean cacheBO(byte[] bo, String hashOfBO);

   /**
    * Gets the BO from the cache
    * 
    * @param hashOfBO
    *           the hash-value of the given BO
    * @return the BO, in case of failure null
    */
   byte[] getBO(String hashOfBO);

   /**
    * Get the URL of the cached BO
    * 
    * @param hashOfBO
    *           hash-value of the given BO
    * @return URL to the cached BO, in case of failure null
    */
   String getURL(String hashOfBO);

   /**
    * Checks if the cache contains a specific BO
    * 
    * @param hashOfBO
    *           the hash-value of the bitlevel-object
    * @return true if BO exists, otherwise false
    */
   boolean containsBO(String hashOfBO);

   /**
    * checks if the adapter is successfully connected to the cache server
    * 
    * @return true if cache is connected, otherwise false
    */
   boolean isConnected();
}
