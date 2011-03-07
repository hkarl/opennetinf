/**
 * 
 */
package netinf.node.resolution.mdht;

import java.net.InetAddress;
import java.net.UnknownHostException;

import junit.framework.Assert;
import netinf.common.datamodel.InformationObject;
import netinf.node.resolution.InformationObjectHelper;

import org.junit.Before;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * @author razvan
 */
public class FreePastryDHTTest {
   private FreePastryDHT _myDHT;
   private InformationObjectHelper ioHelper;
   private InformationObject io;

   @Before
   public void setUp() throws Exception {
      this._myDHT = new FreePastryDHT(1, 2000);
      final Injector injector = Guice.createInjector(new MDHTResolutionTestModule());
      this.ioHelper = injector.getInstance(InformationObjectHelper.class);
      this.io = ioHelper.getUniqueIOWithDummyAttributeAndSubAttributes();
   }

   @Test
   public void testStartUp() {
      String result = _myDHT.getResponsibleNode(this.io.getIdentifier()).toString();
      String hostname = "";
      String myIp = "";
      byte[] ipAddr = new byte[] { 0, 0, 0, 0 };
      boolean isLocalIp = false;
      try {
         InetAddress addr = InetAddress.getLocalHost();

         // Get IP Address
         ipAddr = addr.getAddress();
         InetAddress addrs[] = InetAddress.getAllByName(addr.getHostName());
         // Get hostname
         hostname = addr.getHostName();
         String cmp = "";
         for (InetAddress adr : addrs) {
            if (!adr.isLoopbackAddress() && adr.isSiteLocalAddress()) {
               myIp = adr.getHostAddress();
               cmp = hostname + "/" + myIp + ":0";
               if (result.compareTo(cmp) == 0) {
                  isLocalIp = true;
                  break;
               }
            }
         }

      } catch (UnknownHostException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      // Assert.assertThat(result,equalTo("ZION/192.168.56.1:0"));
      Assert.assertTrue(isLocalIp);

   }

}
