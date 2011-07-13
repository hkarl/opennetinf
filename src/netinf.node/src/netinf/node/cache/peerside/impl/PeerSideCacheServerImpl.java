package netinf.node.cache.peerside.impl;

import java.io.File;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.event.CacheEventListener;
import netinf.node.cache.peerside.CacheMemoryStoreEvictionPolicy;
import netinf.node.cache.peerside.PeerSideCacheServer;

import org.apache.log4j.Logger;

import com.google.inject.Inject;

/**
 * Interface of peer-side caching server.
 * 
 * @author PG NetInf 3, University of Paderborn
 */

public class PeerSideCacheServerImpl implements PeerSideCacheServer, CacheEventListener {

   // not read locally private static final Logger LOG = Logger.getLogger(PeerSideCacheServerImpl.class);
   // private static String DEFAULT_CACHE_CONFIG_PATH = "configs/ehcache_config.xml";
   // private CacheManager cacheManager;

   private Cache cache;
   private static final Logger LOG = Logger.getLogger(PeerSideCacheServerImpl.class);
   private boolean lastOpSucceeded;
   private boolean isEventListenerBound;

   private CacheConfiguration cacheConfig;
   /**
    * Default constructor. Will initialize a cache with the name "peerSideCacheConfig"
    * which is stored in the temp folder of the local computer. The cache is TEMPORARY
    * cacheConfig.setEternal() can be used to change that.
    */
   @Inject
   public PeerSideCacheServerImpl() {

	  this.lastOpSucceeded = false;
	  this.isEventListenerBound = false;
	 //CacheConfiguration object needed in order to initialize the Cache object
	  CacheManager singletonCacheMgr = CacheManager.create();

      cacheConfig = new CacheConfiguration("peerSideCacheConfig", 10000);
      cacheConfig.setDiskStorePath(System.getProperty("java.io.tmpdir") + File.separator + "peerSideCache");
      cacheConfig.setMaxElementsOnDisk(10000);
      cacheConfig.setEternal(false);
      cacheConfig.setMemoryStoreEvictionPolicy("LRU");
      cacheConfig.setOverflowToDisk(true);
      //Temporary Cache object, but need it in order to add to the singletonCacheMgr
      Cache localCache = new Cache(cacheConfig);
      singletonCacheMgr.addCache(localCache);
      
      //Get the VALID reference to the cache named peerSideCacheConfig which we have just addded
      cache = singletonCacheMgr.getCache("peerSideCacheConfig");
    //Register a listener for this cache, so we could handle different events such as whether the put was successful
      //or not
      this.isEventListenerBound = cache.getCacheEventNotificationService().registerListener(this);
      LOG.info("(PSCACHE ) Initialized");
      if (true == this.isEventListenerBound) {
    	  LOG.info("(PSCACHE ) Successfully added event listener");
    	  
      } else {
    	  LOG.warn("(PSCACHE ) Failed to add event listener. Will be unable to return reliable status");
    	  
      }
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
   //@Inject
   public PeerSideCacheServerImpl(String cacheName, int maxElementsInMemory, String path, Boolean eternal,
         CacheMemoryStoreEvictionPolicy policy, int maxElementsOnDisk, Boolean overflowToDisk) {

      //CacheConfiguration object needed in order to initialize the Cache object
      cacheConfig = new CacheConfiguration(cacheName, maxElementsInMemory);

      cacheConfig.setDiskStorePath(path);
      cacheConfig.setMaxElementsOnDisk(maxElementsOnDisk);
      cacheConfig.setEternal(eternal);
      cacheConfig.setMemoryStoreEvictionPolicy(policy.toString());
      cacheConfig.setOverflowToDisk(overflowToDisk);

      cache = new Cache(cacheConfig);
      //Register a listener for this cache, so we could handle different events such as whether the put was successful
      //or not
      cache.getCacheEventNotificationService().registerListener(this);
   }

   @Override
   public boolean contains(String hash) {
      if (cache.getQuiet(hash) != null) {
         return true;
      }
      return false;
   }

   /**
    * Method returns true if the element was put in the cache, otherwise false. 
    * TODO: Only returns a relevant value if the event listener is successfully bound, otherwise just returns true. Fix
    */
   @Override
   public boolean cache(byte[] hashBytes, String hash) {
      Element element = new Element(hash, hashBytes);
      cache.put(element);
      //Check to see if last op was successful
      if (true == this.isEventListenerBound) {
    	 boolean bSucceeded = this.lastOpSucceeded;
    	 this.lastOpSucceeded = false;
    	 return bSucceeded;
      } else {
    	 return true;
      }
   }

   @Override
   public String getURL(String hash) {
      return cacheConfig.getDiskStorePath() + "/" + hash;
   }
   
   @Override
   public boolean containsBO(String hashOfBO) {
	      return cache.isKeyInCache(hashOfBO);
	   }

   @Override
   public void notifyElementRemoved(Ehcache cache, Element element)
   		throws CacheException {
   	// TODO Auto-generated method stub

   }

   @Override
   public void notifyElementPut(Ehcache cache, Element element)
   		throws CacheException {
   		LOG.info("(PSCACHE) Element with key " + element.getKey().toString() + " was added to cache with name " + cache.getName());
   		this.lastOpSucceeded = true;
   }

   @Override
   public void notifyElementUpdated(Ehcache cache, Element element)
   		throws CacheException {
	   LOG.info("(PSCACHE) Element with key " + element.getKey().toString() + " was UPDATED in cache with name " + cache.getName());

   }

   @Override
   public void notifyElementExpired(Ehcache cache, Element element) {
   	LOG.info("(PSCACHE) Element with key " + element.getKey().toString() + " EXPIRED in cache with name " + cache.getName());

   }

   @Override
   public void notifyElementEvicted(Ehcache cache, Element element) {
	   LOG.info("(PSCACHE) Element with key " + element.getKey().toString() + " was EVICTED from cache with name " + cache.getName());

   }

   @Override
   public void notifyRemoveAll(Ehcache cache) {
   	// TODO Auto-generated method stub

   }

   @Override
   public void dispose() {
   	// TODO Auto-generated method stub

   }

   public Object clone() throws CloneNotSupportedException {
   	return null;
   }
}
