package netinf.common.security;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Test for IntegrityResult
 * 
 * @author PG NetInf 3
 */
public class IntegrityResultTest {

   @Test
   public void testIntegrityResult() {
      assertEquals("INTEGRITY_CHECK_FAILED", IntegrityResult.getIntegrityResult("INTEGRITY_CHECK_FAILED").getIntegrityResult());
   }
}