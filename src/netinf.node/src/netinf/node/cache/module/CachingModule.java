package netinf.node.cache.module;

import netinf.node.cache.NetInfCache;
import netinf.node.cache.impl.CacheServer;
import netinf.node.cache.impl.EhCacheServerImpl;
import netinf.node.cache.impl.NetInfCacheImpl;

import com.google.inject.PrivateModule;
import com.google.inject.Singleton;

/**
 * The module for the in-network-caching component
 * 
 * @author PG NetInf 3, University of Paderborn
 */
public class CachingModule extends PrivateModule {

   @Override
   protected void configure() {
      bind(NetInfCache.class).to(NetInfCacheImpl.class).in(Singleton.class);
      bind(CacheServer.class).to(EhCacheServerImpl.class);
      expose(NetInfCache.class);
   }
}
