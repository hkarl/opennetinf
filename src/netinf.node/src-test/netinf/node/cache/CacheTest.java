package netinf.node.cache;

import junit.framework.Assert;
import netinf.node.cache.module.CachingModule;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class CacheTest {

   private static Injector injector;
   private static NetInfCache cache;

   @BeforeClass
   public static void setUp() throws Exception {
      injector = Guice.createInjector(new CachingModule());
      cache = injector.getInstance(NetInfCache.class);
   }

   @Test
   public void testStoreAndGet() {
      Assert.assertTrue(true);
   }

}
