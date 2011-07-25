package netinf.node.cache;

/**
 * Interface for an CachingServer
 * 
 * @author PG NetInf 3, University of Paderborn
 */
public interface BOCacheServer {

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

   /**
    * Provides the address of the cache server
    * 
    * @return the address (url)
    */
   String getAddress();

   /**
    * Provides the given scope of this CachinServer.
    * 
    * @return The scope as integer.
    */
   int getScope();
}
