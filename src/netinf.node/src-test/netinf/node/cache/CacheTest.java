package netinf.node.cache;

import java.util.List;

import junit.framework.Assert;
import netinf.common.datamodel.DataObject;
import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.DefinedAttributePurpose;
import netinf.common.datamodel.attribute.Attribute;
import netinf.common.datamodel.attribute.DefinedAttributeIdentification;
import netinf.common.datamodel.impl.DatamodelFactoryImpl;
import netinf.node.cache.module.CachingModule;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Test class for in-network-cache. Caching Server has to run a Cache server (Ehcache)
 * 
 * @author netinf
 */
public class CacheTest {

   private static Injector injector;
   private static NetInfCache cache;
   private static DatamodelFactory dmFactory;

   @BeforeClass
   public static void setUp() throws Exception {
      injector = Guice.createInjector(new CachingModule());
      cache = injector.getInstance(NetInfCache.class);
      dmFactory = injector.getInstance(DatamodelFactoryImpl.class);
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

   @Test
   public void testContainsTrue() {
      ;
   }

}
