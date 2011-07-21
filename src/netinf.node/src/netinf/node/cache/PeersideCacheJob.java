package netinf.node.cache;

import netinf.common.datamodel.DataObject;
import netinf.node.cache.peerside.PeerSideCache;

import org.apache.log4j.Logger;

import com.google.inject.Inject;

/**
 * @author PG NetInf 3
 */
public class PeersideCacheJob extends Thread {

   private static final Logger LOG = Logger.getLogger(PeersideCacheJob.class);
   private PeerSideCache peersideCache;
   private DataObject toBeCached;

   /**
    * @param dO
    * @param cacheCallback
    */
   public PeersideCacheJob(DataObject dO) {
      toBeCached = dO;
   }

   @Inject(optional = true)
   public void setCache(PeerSideCache cache) {
      peersideCache = cache;
      LOG.info("(PeerCacheJob ) Connected with PeersideCache");
      // TODO: check connection
   }

   @Override
   public void run() {
      peersideCache.cache(toBeCached);
   }
}