package netinf.node.resolution.mdht;

import netinf.common.datamodel.InformationObject;
import netinf.common.exceptions.NetInfCheckedException;
import netinf.node.resolution.InformationObjectHelper;
import netinf.node.resolution.ResolutionService;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class MDHTResolutionServiceTest {

   protected InformationObjectHelper ioHelper;
   protected ResolutionService resolutionService;
   protected Injector injector;

   @Before
   public void setUp() throws Exception {
      injector = Guice.createInjector(new MDHTResolutionTestModule());
      ioHelper = injector.getInstance(InformationObjectHelper.class);
      resolutionService = injector.getInstance(MDHTResolutionService.class);
   }

   @Test
   public void testPutGet() throws NetInfCheckedException {
      resolutionService.put(ioHelper.getDummyIO());
      InformationObject io = resolutionService.get(ioHelper.getDummyIO().getIdentifier());
      Assert.assertTrue(ioHelper.getDummyIO().equals(io));
   }

   @After
   public void tearDown() throws Exception {
      // ((MDHTResolutionService) resolutionService).getPastryNode().destroy();
      // Thread.sleep(100);
   }

}
