package netinf.node.cache;

import java.util.Properties;

import netinf.common.datamodel.impl.module.DatamodelImplModule;
import netinf.common.security.impl.module.SecurityModule;
import netinf.node.cache.network.NetworkCache;

import com.google.inject.PrivateModule;
import com.google.inject.name.Names;

/**
 * The module for the in-network-caching component
 * 
 * @author PG NetInf 3, University of Paderborn
 */
public class CacheModuleTest extends PrivateModule {

   private final Properties properties;

   public CacheModuleTest(final Properties properties) {
      this.properties = properties;
   }

   @Override
   public void configure() {
      Names.bindProperties(binder(), properties);
      bind(BOCacheServer.class).to(NetworkCache.class);

      install(new DatamodelImplModule());
      install(new SecurityModule());
   }

}
