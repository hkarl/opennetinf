package netinf.node.cache;


/**
 * Test class for in-network-cache. Caching Server has to run a Cache server (Ehcache)
 * 
 * @author netinf
 */
public class CacheTest {

//   private static Injector injector;
//   private static NetworkCache cache;
//   private static DatamodelFactory dmFactory;
//
//   @BeforeClass
//   public static void setUp() throws Exception {
//      final Properties properties = Utils.loadProperties("../configs/testing.properties");
//      injector = Guice.createInjector(new LogModule(properties), new SecurityModule(), new DatamodelImplModule(),
//            new AbstractModule() {
//
//               @Override
//               protected void configure() {
//                  Names.bindProperties(binder(), properties);
//                  bind(NetInfNodeConnection.class).annotatedWith(SecurityModule.Security.class).to(RemoteNodeConnection.class);
//
//                  bind(NetworkCache.class).to(NetworkCache.class).in(Singleton.class);
//                  bind(BOCacheServer.class).to(RemoteEhcacheServer.class);
//               }
//            });
//      cache = injector.getInstance(NetworkCache.class);
//      dmFactory = injector.getInstance(DatamodelFactory.class);
//   }
//
//   @Test
//   public void testCache() {
////      DataObject doObj = dmFactory.createDataObject();
////      String hashOfFile = "5966dae262c580a01b239341081d595e2d47e4aa";
////      String fileURL = "http://www.netinf.org/wp-content/themes/lightword/images/header-image.png";
////
////      // url attr
////      Attribute attribute = dmFactory.createAttribute();
////      attribute.setAttributePurpose(DefinedAttributePurpose.LOCATOR_ATTRIBUTE.toString());
////      attribute.setIdentification(DefinedAttributeIdentification.HTTP_URL.getURI());
////      attribute.setValue(fileURL);
////
////      // hash attr
////      Attribute attribute2 = dmFactory.createAttribute();
////      attribute2.setIdentification(DefinedAttributeIdentification.HASH_OF_DATA.getURI());
////      attribute2.setAttributePurpose(DefinedAttributePurpose.SYSTEM_ATTRIBUTE.getAttributePurpose());
////      attribute2.setValue(hashOfFile);
////
////      // add attrs
////      doObj.addAttribute(attribute);
////      doObj.addAttribute(attribute2);
////
////      // exec cache
////      cache.cache(doObj);
////
////      List<Attribute> locators = doObj.getAttributesForPurpose(DefinedAttributePurpose.LOCATOR_ATTRIBUTE.toString());
////
////      // should be two locators now (original + cached one)
////      Assert.assertTrue(locators.size() == 2);
//   }
//
//   @Test
//   public void testIsConnected() {
//      // should be true if server is running
//      Assert.assertTrue(cache.isConnected());
//   }
//
//   @Test
//   public void testContainsFalse() {
//      boolean result = cache.contains("testABCWE");
//      Assert.assertFalse(result);
//   }

}
