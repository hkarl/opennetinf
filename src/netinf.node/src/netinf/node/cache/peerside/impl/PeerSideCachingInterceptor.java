package netinf.node.cache.peerside.impl;

import netinf.common.datamodel.InformationObject;
import netinf.node.cache.peerside.PeerSideCache;
import netinf.node.resolution.ResolutionInterceptor;

import org.apache.log4j.Logger;

import com.google.inject.Inject;

/**
 * The implementation of the peer-side caching interceptor. Used to intercept get IO calls in 
 * order to cache them locally
 * 
 * @author PG NetInf 3, University of Paderborn
 */

public class PeerSideCachingInterceptor implements ResolutionInterceptor {

   private PeerSideCache cache;
   private static final Logger LOG = Logger.getLogger(PeerSideCachingInterceptor.class);
   private final static String LOG_MESSAGE = "(PSCACHE ) Result of cache operation: "; 

   @Inject
   public PeerSideCachingInterceptor(PeerSideCache cache) {
      this.cache = cache;
   }

   @Override
   public InformationObject interceptGet(InformationObject io) {
 	  boolean cacheResult = cache.cache(io);
   	  LOG.info(LOG_MESSAGE + (true == cacheResult ? "SUCCESS" : "FAILURE"));      
      return io;
   }

}
