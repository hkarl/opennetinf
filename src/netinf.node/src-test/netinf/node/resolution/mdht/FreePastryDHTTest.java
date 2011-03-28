/**
 * 
 */
package netinf.node.resolution.mdht;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import junit.framework.Assert;
import netinf.common.datamodel.InformationObject;
import netinf.node.resolution.InformationObjectHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import rice.p2p.commonapi.Id;
import rice.p2p.past.PastContentHandle;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * @author razvan
 */
public class FreePastryDHTTest {
   private FreePastryDHT myDHT;
   private InformationObjectHelper ioHelper;
   private InformationObject io;

   @Before
   public void setUp() throws Exception {
      //this.myDHT = new FreePastryDHT(1, null, 5007, new MDHTResolutionService(3, "10.10.10.1", 1, 2001));
      final Injector injector = Guice.createInjector(new MDHTResolutionTestModule());
      this.ioHelper = injector.getInstance(InformationObjectHelper.class);
      this.io = ioHelper.getUniqueIOWithDummyAttributeAndSubAttributes();
      
      
   }

   
   public void testStartUp() {
      InetSocketAddress result = myDHT.getResponsibleNode(this.io);
      String hostname = "";
      String myIp = "";
      byte[] ipAddr;
      boolean isLocalIp = false;

      try {
         InetAddress addr = InetAddress.getLocalHost();

         // Get IP Address
         ipAddr = addr.getAddress();
         InetAddress[] addrs = InetAddress.getAllByName(addr.getHostName());
         // Get hostname
         hostname = addr.getHostName();
         String cmp = "";
   /*      for (InetAddress adr : addrs) {
            if (!adr.isLoopbackAddress() && adr.isSiteLocalAddress()) {
               myIp = adr.getHostAddress();
               cmp = hostname + "/" + myIp + ":0";
               if (result.compareTo(cmp) == 0) {
                  isLocalIp = true;
                  break;
               }
            }
         }*/

      } catch (UnknownHostException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      // Assert.assertThat(result,equalTo("ZION/192.168.56.1:0"));
      Assert.assertTrue(isLocalIp);
   }
   
   @Test
   public void putTest()
   {  
      PastContentHandle pch = myDHT.put(this.io);
      myDHT.get(pch);
      InformationObject retIO = FreePastryDHT.returned;
      Assert.assertEquals(this.io.toString(), retIO.toString());
   }

   @After
   public void CleanUp()
   {
     this.myDHT.leave();   
   }
}
