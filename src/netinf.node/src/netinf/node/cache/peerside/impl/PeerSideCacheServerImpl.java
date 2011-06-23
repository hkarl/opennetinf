package netinf.node.cache.peerside.impl;

import java.io.File;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import netinf.node.cache.peerside.CacheMemoryStoreEvictionPolicy;
import netinf.node.cache.peerside.PeerSideCacheServer;

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

   private CacheConfiguration cacheConfig;

   /**
    * Default constructor.
    */
   @Inject
   public PeerSideCacheServerImpl() {

      // this.cacheManager = new CacheManager(DEFAULT_CACHE_CONFIG_PATH);

      cacheConfig = new CacheConfiguration("peerSideCacheConfig", 10000);
      cacheConfig.setDiskStorePath(System.getProperty("java.io.tmpdir") + File.separator + "peerSideCache");
      cacheConfig.setMaxElementsOnDisk(10000);
      cacheConfig.setEternal(false);
      cacheConfig.setMemoryStoreEvictionPolicy("LRU");
      cacheConfig.setOverflowToDisk(true);

      cache = new Cache(cacheConfig);
   }

   /**
    * @param cacheName
    * @param maxElementsInMemory
    * @param path
    * @param eternal
    * @param policy
    * @param maxElementsOnDisk
    * @param overflowToDisk
    */
   @Inject
   public PeerSideCacheServerImpl(String cacheName, int maxElementsInMemory, String path, Boolean eternal,
         CacheMemoryStoreEvictionPolicy policy, int maxElementsOnDisk, Boolean overflowToDisk) {

      // this.cacheManager = new CacheManager(configPath);

      cacheConfig = new CacheConfiguration(cacheName, maxElementsInMemory);

      cacheConfig.setDiskStorePath(path);
      cacheConfig.setMaxElementsOnDisk(maxElementsOnDisk);
      cacheConfig.setEternal(eternal);
      cacheConfig.setMemoryStoreEvictionPolicy(policy.toString());
      cacheConfig.setOverflowToDisk(overflowToDisk);

      cache = new Cache(cacheConfig);
   }

   @Override
   public boolean contains(String hash) {
      if (cache.getQuiet(hash) != null) {
         return true;
      }
      return false;
   }

   @Override
   public void cache(byte[] hashBytes, String hash) {
      Element element = new Element(hash, hashBytes);
      cache.put(element);
   }

   @Override
   public String getURL(String hash) {
      return cacheConfig.getDiskStorePath() + "/" + hash;
   }

}
