package netinf.node.cache;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import netinf.common.datamodel.DataObject;
import netinf.common.datamodel.DefinedAttributePurpose;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.attribute.Attribute;
import netinf.common.utils.DatamodelUtils;
import netinf.node.api.impl.LocalNodeConnection;
import netinf.node.cache.network.NetworkCache;
import netinf.node.cache.peerside.PeerSideCache;
import netinf.node.resolution.ResolutionInterceptor;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * An Interceptor that coordinates all available caches
 * 
 * @author PG NetInf 3
 */
public class CachingInterceptor implements ResolutionInterceptor {

   private static final Logger LOG = Logger.getLogger(CachingInterceptor.class);
   private LocalNodeConnection connection;
   private List<Cache> usedCaches = new ArrayList<Cache>();
   private Hashtable<String, Thread> runningCacheJobs = new Hashtable<String, Thread>();
   private boolean useChunking;

   /**
    * Constructor
    * 
    * @param useChunking
    *           The injected chunking flag.
    */
   @Inject
   public CachingInterceptor(@Named("use_chunking") final boolean useChunking) {
      this.useChunking = useChunking;
      if (this.useChunking) {
         LOG.info("(CachingInterceptor ) Chunking is enabled");
      } else {
         LOG.info("(CachingInterceptor ) Chunking is NOT enabled");
      }
   }

   @Inject(optional = true)
   public void setNetworkCache(NetworkCache cache) {
      if (!cache.isConnected()) {
         LOG.info("(NWInterceptor ) NWCache not connected");
      } else {
         usedCaches.add(cache);
         LOG.info("(NWInterceptor ) NWCache connected");
      }
   }

   @Inject(optional = true)
   public void setPeersideCache(PeerSideCache cache) {
      if (!cache.isConnected()) {
         LOG.info("(PSInterceptor ) PSCache not connected");
      } else {
         usedCaches.add(cache);
         LOG.info("(PSInterceptor ) PSCache connected");
      }
   }

   @Inject
   public void setNodeConnection(LocalNodeConnection conn) {
      connection = conn;
   }

   @Override
   public InformationObject interceptGet(InformationObject io) {
      LOG.info("(CachingInterceptor ) GET intercepted...");

      if (!(io instanceof DataObject)) {
         LOG.info("(CachingInterceptor ) IO is no DataObject");
         return io;
      }

      List<Cache> useThisCaches = whoShouldCache(io);
      if (useThisCaches.isEmpty()) {
         LOG.info("(CachingInterceptor ) nobody should cache this DO");
         return io;
      }

      if (!isCacheable(io)) {
         LOG.info("(CachingInterceptor ) DO is not cacheable");
         return io;
      }

      // is a thread already working on that DO?
      String key = io.getIdentifier().toString();
      if (runningCacheJobs.containsKey(key)) {
         if (runningCacheJobs.get(key).isAlive()) {
            LOG.info("(CachingInterceptor ) CacheJob already started for this DO");
            return io;
         } else {
            runningCacheJobs.remove(key);
         }
      }

      LOG.info("(CacheInterceptor ) starting caching...");
      DataObject dO = (DataObject) io.clone();

      // start the caching process
      CacheJob job = new CacheJob(dO, useThisCaches, connection, useChunking);
      runningCacheJobs.put(key, job);
      job.start();

      // return the old IO, the new cached one will be reputted
      return io;
   }

   /**
    * Provides a list of caches that should be used
    * 
    * @param obj
    *           The IO
    * @return A subset of all usable caches (which make sense)
    */
   private List<Cache> whoShouldCache(InformationObject obj) {
      List<Cache> useThisCaches = new ArrayList<Cache>();
      List<Attribute> locators = obj.getAttributesForPurpose(DefinedAttributePurpose.LOCATOR_ATTRIBUTE.toString());
      for (Cache cache : usedCaches) {
         boolean addCache = true;
         for (Attribute loc : locators) {
            if (loc.getValue(String.class).contains(cache.getAddress())) {
               addCache = false;
            }
         }
         if (addCache) {
            useThisCaches.add(cache);
         }
      }

      return useThisCaches;
   }

   /**
    * Determines whether this IO is cachable (hash, locators) or not.
    * 
    * @param obj
    *           The IO that has to be checked.
    * @return true if IO is cachable, otherwise false.
    */
   private boolean isCacheable(InformationObject obj) {
      String hash = DatamodelUtils.getHash(obj);
      if (hash == null) {
         return false;
      }

      List<Attribute> locators = obj.getAttributesForPurpose(DefinedAttributePurpose.LOCATOR_ATTRIBUTE.toString());
      if (locators.isEmpty()) {
         return false;
      }

      return true;
   }
}