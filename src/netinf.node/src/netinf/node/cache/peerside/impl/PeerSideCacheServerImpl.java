package netinf.node.cache.peerside.impl;

import java.io.File;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.Status;
import net.sf.ehcache.config.CacheConfiguration;
import netinf.node.cache.peerside.PeerSideCacheServer;

import org.apache.log4j.Logger;

import com.google.inject.Inject;

/**
 * Interface of peer-side caching server.
 * 
 * @author PG NetInf 3, University of Paderborn
 */

public class PeerSideCacheServerImpl implements PeerSideCacheServer {

   // not read locally private static final Logger LOG = Logger.getLogger(PeerSideCacheServerImpl.class);
   // private static String DEFAULT_CACHE_CONFIG_PATH = "configs/ehcache_config.xml";
   // private CacheManager cacheManager;

   private Cache cache;
   private static final Logger LOG = Logger.getLogger(PeerSideCacheServerImpl.class);
   private boolean lastOpSucceeded;

   private CacheConfiguration cacheConfig;

   /**
    * Default constructor. Will initialize a cache with the name "peerSideCacheConfig" which is stored in the temp folder of the
    * local computer. The cache is TEMPORARY cacheConfig.setEternal() can be used to change that.
    */
   @Inject
   public PeerSideCacheServerImpl() {

      lastOpSucceeded = false;
      // CacheConfiguration object needed in order to initialize the Cache object
      CacheManager singletonCacheMgr = CacheManager.create();

      cacheConfig = new CacheConfiguration("peerSideCacheConfig", 10000);
      cacheConfig.setDiskStorePath(System.getProperty("java.io.tmpdir") + File.separator + "peerSideCache");
      cacheConfig.setMaxElementsOnDisk(10000);
      cacheConfig.setEternal(true);
      cacheConfig.setMemoryStoreEvictionPolicy("LRU");
      cacheConfig.setOverflowToDisk(true);
      // Temporary Cache object, but need it in order to add to the singletonCacheMgr
      Cache localCache = new Cache(cacheConfig);
      singletonCacheMgr.addCache(localCache);

      // Get the VALID reference to the cache named peerSideCacheConfig which we have just addded
      cache = singletonCacheMgr.getCache("peerSideCacheConfig");
   }

//   /**
//    * @param cacheName
//    * @param maxElementsInMemory
//    * @param path
//    * @param eternal
//    * @param policy
//    * @param maxElementsOnDisk
//    * @param overflowToDisk
//    */
//   // @Inject
//   public PeerSideCacheServerImpl(String cacheName, int maxElementsInMemory, String path, Boolean eternal,
//         CacheMemoryStoreEvictionPolicy policy, int maxElementsOnDisk, Boolean overflowToDisk) {
//
//      // CacheConfiguration object needed in order to initialize the Cache object
//      cacheConfig = new CacheConfiguration(cacheName, maxElementsInMemory);
//
//      cacheConfig.setDiskStorePath(path);
//      cacheConfig.setMaxElementsOnDisk(maxElementsOnDisk);
//      cacheConfig.setEternal(eternal);
//      cacheConfig.setMemoryStoreEvictionPolicy(policy.toString());
//      cacheConfig.setOverflowToDisk(overflowToDisk);
//
//      cache = new Cache(cacheConfig);
//   }

   @Override
   public boolean contains(String hash) {
      return cache.isKeyInCache(hash);
   }

   /**
    * Method returns true if the element was put in the cache, otherwise false. TODO: Only returns a relevant value if the event
    * listener is successfully bound, otherwise just returns true. Fix
    */
   @Override
   public boolean cache(byte[] hashBytes, String hash) {
      Element element = new Element(hash, hashBytes);
      try {
         cache.put(element);
         return true;
      } catch (Exception ex) {
         LOG.warn("Put not succeeded");
         return false;
      }
   }

   @Override
   public String getURL(String hash) {
      return cacheConfig.getDiskStorePath() + "/" + hash;
   }

   @Override
   public boolean isConnected() {
      if (cache.getStatus().equals(Status.STATUS_ALIVE)) {
         return true;
      }
      return false;
   }
}
