package netinf.common.securitytest;

import static org.junit.Assert.assertEquals;

import netinf.common.security.IntegrityResult;

import org.junit.Test;


public class IntegrityResultTest {

	@Test
	public void testIntegrityResult(){
		assertEquals("INTEGRITY_CHECK_FAILED",IntegrityResult.getIntegrityResult("INTEGRITY_CHECK_FAILED").getIntegrityResult());
        
}
	}