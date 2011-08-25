package netinf.common.transfer;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Test class for netinf.common.transfer
 */
public class TransferJobTest {

   @Test
   public void testTransfer() {
      TransferJob transferJob = new TransferJob("123", "456", "789");
      assertEquals("123", transferJob.getJobId());
      assertEquals("456", transferJob.getSource());
      assertEquals("789", transferJob.getDestination());

      transferJob.setDestination("destination");
      assertEquals("destination", transferJob.getDestination());

      transferJob.setSource("source");
      assertEquals("source", transferJob.getSource());

      assertEquals("TransferJobKind: TransferJob\nJobId: 123\nSource: source\nDestination: destination", transferJob.toString());
   }

}
