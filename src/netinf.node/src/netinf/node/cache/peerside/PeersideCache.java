package netinf.node.cache.peerside;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.Status;
import netinf.node.access.AccessServer;
import netinf.node.cache.BOCacheServer;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * @author PG NetInf 3, University of Paderborn
 */
public class PeersideCache implements BOCacheServer {

   // TODO: cache is still not persistent
   private static final Logger LOG = Logger.getLogger(PeersideCache.class);

   private Cache cache;
   private CacheManager manager;
   private final String ehcacheConfigPath = "../configs/PeersideEhcacheConfig.xml";

   private AccessServer accessServer;
   private String host;
   private int port;

   private int mdhtScope;

   /**
    * Constructor
    */
   @Inject
   public PeersideCache(@Named("peerside.mdht.scope") final int scope, @Named("peerside.access.host") String host,
         @Named("peerside.access.port") int port) {

      // create manager with default config and init cache
      manager = CacheManager.create(ehcacheConfigPath);
      cache = manager.getCache("PeersideCache");

      // register shutdown listener
      Runtime.getRuntime().addShutdownHook(new Thread() {
         public void run() {
            LOG.info("(Ehcache ) shutdown!");
            manager.shutdown();
         }
      });

      mdhtScope = scope;

      this.host = host;
      this.port = port;
      accessServer = new PeersideAccessServer(cache, port);
      accessServer.start();
   }

   @Override
   public boolean containsBO(String hash) {
      boolean flag = cache.isKeyInCache(hash);
      LOG.info("(PeersideCache ) BO already in cache: " + flag);
      return cache.isKeyInCache(hash);
   }

   @Override
   public boolean cacheBO(byte[] bo, String hashOfBO) {
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
      return "http://" + host + ":" + port;
   }

   @Override
   public int getScope() {
      return this.mdhtScope;
   }

}
