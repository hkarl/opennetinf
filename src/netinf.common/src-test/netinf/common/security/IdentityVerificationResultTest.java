package netinf.common.security;

import static org.junit.Assert.assertEquals;
import netinf.common.security.impl.IdentityVerificationTest;

import org.junit.Test;

/**
 * Test for IdentityVerificationResult
 * 
 * @author PG NetInf 3
 */
public class IdentityVerificationResultTest {

   @SuppressWarnings("deprecation")
   @Test
   public void identityVerificationResulttest() {
      // ? IdentityVerificationResult vr = IdentityVerificationResult.getIdentityVerificationResult("null");
      assertEquals(null, IdentityVerificationResult.getIdentityVerificationResult(null));

      assertEquals(null,
            IdentityVerificationTest.getIdentityVerificationResultByVerificationResultString("identityVerificationResult"));
      // assertEquals("identityVerificationResult", vr.getIdentityVerificationResult());

   }

}
