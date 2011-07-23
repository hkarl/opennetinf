package netinf.node.cache.peerside.impl;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.Status;
import netinf.node.cache.peerside.PeerSideCacheServer;

import org.apache.log4j.Logger;

/**
 * @author PG NetInf 3, University of Paderborn
 */
public class PeerSideCacheServerImpl implements PeerSideCacheServer {

   // TODO: cache is still not persistent
   // TODO: cache is not reachable from outside -> REST
   
   private Cache cache;
   private CacheManager manager;
   private static final Logger LOG = Logger.getLogger(PeerSideCacheServerImpl.class);
   private final String ehcacheConfigPath = "../configs/PeersideEhcacheConfig.xml";
   
  /**
   * Constructor
   */
   public PeerSideCacheServerImpl() {
      // create manager with default config and init cache
      manager = CacheManager.create(ehcacheConfigPath);
      cache = manager.getCache("PeersideCache");
   }

   @Override
   public boolean contains(String hash) {
      boolean flag = cache.isKeyInCache(hash);
      System.out.println("PEERSIDE contains: " + flag);
      return cache.isKeyInCache(hash);
   }

   @Override
   public boolean cache(byte[] bo, String hashOfBO) {
      Element element = new Element(hashOfBO, bo);
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
      return this.getAddress() + "/" + hash;
   }

   @Override
   public boolean isConnected() {
      if (cache.getStatus().equals(Status.STATUS_ALIVE)) {
         return true;
      }
      return false;
   }

   @Override
   public String getAddress() {
      return manager.getDiskStorePath();
   }
}
