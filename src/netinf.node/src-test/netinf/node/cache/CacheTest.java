package netinf.node.cache;

import java.util.List;
import java.util.Properties;

import junit.framework.Assert;
import netinf.common.communication.NetInfNodeConnection;
import netinf.common.communication.RemoteNodeConnection;
import netinf.common.datamodel.DataObject;
import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.DefinedAttributePurpose;
import netinf.common.datamodel.attribute.Attribute;
import netinf.common.datamodel.attribute.DefinedAttributeIdentification;
import netinf.common.datamodel.impl.module.DatamodelImplModule;
import netinf.common.log.module.LogModule;
import netinf.common.security.impl.module.SecurityModule;
import netinf.common.utils.Utils;
import netinf.node.cache.network.NetworkCache;
import netinf.node.cache.network.impl.CacheServer;
import netinf.node.cache.network.impl.EhCacheServerImpl;
import netinf.node.cache.network.impl.NetInfCacheImpl;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.name.Names;

/**
 * Test class for in-network-cache. Caching Server has to run a Cache server (Ehcache)
 * 
 * @author netinf
 */
public class CacheTest {

   private static Injector injector;
   private static NetworkCache cache;
   private static DatamodelFactory dmFactory;

   @BeforeClass
   public static void setUp() throws Exception {
      final Properties properties = Utils.loadProperties("../configs/testing.properties");
      injector = Guice.createInjector(new LogModule(properties), new SecurityModule(), new DatamodelImplModule(),
            new AbstractModule() {

               @Override
               protected void configure() {
                  Names.bindProperties(binder(), properties);
                  bind(NetInfNodeConnection.class).annotatedWith(SecurityModule.Security.class).to(RemoteNodeConnection.class);

                  bind(NetworkCache.class).to(NetInfCacheImpl.class).in(Singleton.class);
                  bind(CacheServer.class).to(EhCacheServerImpl.class);
               }
            });
      cache = injector.getInstance(NetworkCache.class);
      dmFactory = injector.getInstance(DatamodelFactory.class);
   }

   @Test
   public void testCache() {
      DataObject doObj = dmFactory.createDataObject();
      String hashOfFile = "5966dae262c580a01b239341081d595e2d47e4aa";
      String fileURL = "http://www.netinf.org/wp-content/themes/lightword/images/header-image.png";

      // url attr
      Attribute attribute = dmFactory.createAttribute();
      attribute.setAttributePurpose(DefinedAttributePurpose.LOCATOR_ATTRIBUTE.toString());
      attribute.setIdentification(DefinedAttributeIdentification.HTTP_URL.getURI());
      attribute.setValue(fileURL);

      // hash attr
      Attribute attribute2 = dmFactory.createAttribute();
      attribute2.setIdentification(DefinedAttributeIdentification.HASH_OF_DATA.getURI());
      attribute2.setAttributePurpose(DefinedAttributePurpose.SYSTEM_ATTRIBUTE.getAttributePurpose());
      attribute2.setValue(hashOfFile);

      // add attrs
      doObj.addAttribute(attribute);
      doObj.addAttribute(attribute2);

      // exec cache
      cache.cache(doObj);

      List<Attribute> locators = doObj.getAttributesForPurpose(DefinedAttributePurpose.LOCATOR_ATTRIBUTE.toString());

      // should be two locators now (original + cached one)
      Assert.assertTrue(locators.size() == 2);
   }

   @Test
   public void testIsConnected() {
      // should be true if server is running
      Assert.assertTrue(cache.isConnected());
   }

   @Test
   public void testContainsFalse() {
      boolean result = cache.contains("testABCWE");
      Assert.assertFalse(result);
   }

}
