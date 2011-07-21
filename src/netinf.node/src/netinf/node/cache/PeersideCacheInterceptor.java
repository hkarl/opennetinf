package netinf.node.cache;

import netinf.common.datamodel.DataObject;
import netinf.common.datamodel.InformationObject;
import netinf.node.api.impl.LocalNodeConnection;
import netinf.node.cache.peerside.PeerSideCache;
import netinf.node.resolution.ResolutionInterceptor;

import org.apache.log4j.Logger;

import com.google.inject.Inject;

/**
 * @author PG NetInf 3
 */
public class PeersideCacheInterceptor implements ResolutionInterceptor {

   private static final Logger LOG = Logger.getLogger(PeersideCacheInterceptor.class);
   private PeerSideCache peersideCache;
   private LocalNodeConnection connection;

   @Inject(optional = true)
   public void setCache(PeerSideCache cache) {
      peersideCache = cache;
      if (!peersideCache.isConnected()) {
         peersideCache = null;
         LOG.info("(PSInterceptor ) PSCache not connected");
      } else {
         LOG.info("(PSInterceptor ) PSCache connected");
      }
   }

   @Inject
   public void setNodeConnection(LocalNodeConnection conn) {
      connection = conn;
   }

   @Override
   public InformationObject interceptGet(InformationObject io) {
      LOG.info("(PSInterceptor ) handling interception...");

      if (!(io instanceof DataObject)) {
         LOG.info("(PSInterceptor ) IO is no DataObject, returning...");
         return io;
      }

      if (peersideCache == null) {
         LOG.info("(PSInterceptor ) Cache is not reachable, returning...");
         return io;
      }

      LOG.info("(PSInterceptor ) starting caching...");
      DataObject dO = (DataObject) io.clone();
      PeersideCacheJob job = new PeersideCacheJob(dO, peersideCache, connection);
      job.start();

      return io;
   }

}