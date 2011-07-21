/**
 * 
 */
package netinf.node.cache;

import netinf.common.datamodel.DataObject;
import netinf.common.datamodel.DefinedAttributePurpose;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.attribute.Attribute;
import netinf.node.api.impl.LocalNodeConnection;
import netinf.node.cache.network.NetworkCache;
import netinf.node.resolution.ResolutionInterceptor;

import org.apache.log4j.Logger;

import com.google.inject.Inject;

/**
 * @author indy
 */
public class NetworkCacheInterceptor implements ResolutionInterceptor {

   private static final Logger LOG = Logger.getLogger(NetworkCacheInterceptor.class);
   private NetworkCache networkCache;
   private LocalNodeConnection connection;

   @Inject(optional = true)
   public void setCache(NetworkCache cache) {
      networkCache = cache;
      if (!networkCache.isConnected()) {
         networkCache = null;
         LOG.info("(NWInterceptor ) NWCache not connected");
      } else {
         LOG.info("(NWInterceptor ) NWCache connected");
      }
   }
   
   @Inject
   public void setNodeConnection(LocalNodeConnection conn) {
      connection = conn;
   }

   @Override
   public InformationObject interceptGet(InformationObject io) {
      LOG.info("(NWInterceptor ) handling interception...");

      if (!(io instanceof DataObject)) {
         LOG.info("(NWInterceptor ) IO is no DataObject, returning...");
         return io;
      }

      if (networkCache == null) {
         LOG.info("(NWInterceptor ) Cache is not reachable, returning...");
         return io;
      }
      
      if (this.hasNetworkCacheLocator(io)) {
         LOG.info("(NWInterceptor ) DataObject already cached by NetworkCache...");
         return io;
      }

      LOG.info("(NWInterceptor ) starting caching...");
      DataObject dO = (DataObject) io.clone();
      NetworkCacheJob job = new NetworkCacheJob(dO, networkCache, connection);
      job.start();

      return io;
   }

   private boolean hasNetworkCacheLocator(InformationObject dObj) {
      String cacheAdd = networkCache.getAddress();
      for (Attribute attr : dObj.getAttributes()) {
         if (attr.getAttributePurpose().toString() == DefinedAttributePurpose.LOCATOR_ATTRIBUTE.toString()) {
            if (attr.getValue(String.class).contains(cacheAdd)) {
               return true;
            }
         }
      }
      return false;
   }
}