package netinf.node.cache;

import netinf.common.datamodel.DataObject;
import netinf.common.exceptions.NetInfCheckedException;
import netinf.node.api.impl.LocalNodeConnection;
import netinf.node.cache.peerside.PeerSideCache;

import org.apache.log4j.Logger;

/**
 * @author PG NetInf 3
 */
public class PeersideCacheJob extends Thread {

   private static final Logger LOG = Logger.getLogger(PeersideCacheJob.class);
   private PeerSideCache peersideCache;
   private DataObject toBeCached;
   private LocalNodeConnection connection;

   /**
    * @param dO
    * @param cacheCallback
    */
   public PeersideCacheJob(DataObject dO, PeerSideCache cache, LocalNodeConnection conn) {
      peersideCache = cache;
      toBeCached = dO;
      connection = conn;
   }

   @Override
   public void run() {
      if (peersideCache != null) {
         LOG.info("(PSCacheJob ) CachingJob started...");
         peersideCache.cache(toBeCached);
         LOG.info("(PSCacheJob ) CachingJob finished...");

         // call RS
         try {
            LOG.info("(PSCacheJob ) Putting back to RS (+ new locators)...");
            connection.putIO(toBeCached);
         } catch (NetInfCheckedException e) {
            LOG.warn("(PSCacheJob ) Error during putting back... " + e.getMessage());
         }
      } else {
         LOG.warn("(PSCacheJob ) CachingJob could not be started, PeersideCache not reachable");
      }
   }
}