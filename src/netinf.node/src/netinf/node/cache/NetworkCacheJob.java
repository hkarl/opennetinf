package netinf.node.cache;

import netinf.common.datamodel.DataObject;
import netinf.common.exceptions.NetInfCheckedException;
import netinf.node.api.impl.LocalNodeConnection;
import netinf.node.cache.network.NetworkCache;

import org.apache.log4j.Logger;

/**
 * @author PG NetInf 3
 */
public class NetworkCacheJob extends Thread {

   private static final Logger LOG = Logger.getLogger(NetworkCacheJob.class);
   private NetworkCache networkCache;
   private DataObject toBeCached;
   private LocalNodeConnection connection;
   
   /**
    * 
    * @param dO
    * @param cacheCallback
    */
   public NetworkCacheJob(DataObject dO, NetworkCache cache, LocalNodeConnection conn) {
      networkCache = cache;
      toBeCached = dO;
      connection = conn;
   }
   
   @Override
   public void run() {
      if (networkCache != null) {
         LOG.info("(NWCacheJob ) CachingJob started...");
         networkCache.cache(toBeCached);
         LOG.info("(NWCacheJob ) CachingJob finished...");
         
         // call RS
         try {
            LOG.info("(NWCacheJob ) Putting back to RS (+ new locators)...");
            connection.putIO(toBeCached);
         } catch (NetInfCheckedException e) {
            LOG.warn("(NWCacheJob ) Error during putting back... " + e.getMessage());
         }
      } else {
         LOG.warn("(NWCacheJob ) CachingJob could not be started, NetworkCache not reachable");
      }
   }
}