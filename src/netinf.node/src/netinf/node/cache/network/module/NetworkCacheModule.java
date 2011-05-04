package netinf.node.cache.network.module;

import netinf.node.cache.network.NetworkCache;
import netinf.node.cache.network.impl.CacheServer;
import netinf.node.cache.network.impl.EhCacheServerImpl;
import netinf.node.cache.network.impl.NetInfCacheImpl;

import com.google.inject.PrivateModule;
import com.google.inject.Singleton;

/**
 * The module for the in-network-caching component
 * 
 * @author PG NetInf 3, University of Paderborn
 */
public class NetworkCacheModule extends PrivateModule {

   @Override
   protected void configure() {
      bind(NetworkCache.class).to(NetInfCacheImpl.class).in(Singleton.class);
      bind(CacheServer.class).to(EhCacheServerImpl.class);
      expose(NetworkCache.class);
   }
}
