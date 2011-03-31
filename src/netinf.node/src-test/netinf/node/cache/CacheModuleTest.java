package netinf.node.cache;

import java.net.InetAddress;
import java.net.UnknownHostException;

import netinf.common.datamodel.impl.module.DatamodelImplModule;
import netinf.node.cache.impl.CacheServer;
import netinf.node.cache.impl.EhCacheServerImpl;
import netinf.node.cache.impl.NetInfCacheImpl;

import com.google.inject.PrivateModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

/**
 * The module for the in-network-caching component
 * 
 * @author PG NetInf 3, University of Paderborn
 */
public class CacheModuleTest extends PrivateModule {

   @Override
   protected void configure() {
      bind(NetInfCache.class).to(NetInfCacheImpl.class).in(Singleton.class);
      expose(NetInfCache.class);

      install(new DatamodelImplModule());
   }

   @Provides
   CacheServer provideCacheServer() throws UnknownHostException {
      return new EhCacheServerImpl(InetAddress.getLocalHost());
   }

}
