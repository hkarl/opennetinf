package netinf.node.cache.peerside;

/**
 * Interface of peer-side caching server.
 * 
 * @author PG NetInf 3, University of Paderborn
 */

public interface PeerSideCacheServer {

   /**
    * Checks if this cache contains the given BO. In EhCache each element has a key and here hash of DO is regarded as the key.
    * 
    * @param hash
    *           of BO
    * @return true if BO exists, otherwise false
    */

   boolean contains(String hash);

   /**
    * Caches a given BO in the Server
    * 
    * @param bo
    *           the bitlevel-object
    * @param hashOfBO
    *           the hash-value of the given BO
    */

   void cache(byte[] hashBytes, String hash);

   /**
    * Get the URL of the cached BO
    * 
    * @param hashOfBO
    *           hash-value of the given BO
    * @return URL to the cached BO
    */
   String getURL(String hash);
}
