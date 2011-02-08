
package netinf.common.securitytest;

import static org.junit.Assert.assertEquals;

import netinf.common.security.IdentityVerification;
import netinf.common.security.IdentityVerificationResult;
import netinf.common.security.impl.IdentityVerificationTest;

import org.junit.Test;



public class identityVerificationResultTest {

	@SuppressWarnings("deprecation")
	@Test
	public void identityVerificationResulttest(){
		IdentityVerificationResult vr = IdentityVerificationResult.getIdentityVerificationResult("null");
		assertEquals(null,IdentityVerificationResult.getIdentityVerificationResult(null));
	

	assertEquals(null,IdentityVerificationTest.getIdentityVerificationResultByVerificationResultString("identityVerificationResult"));
    assertEquals("identityVerificationResult",vr.getIdentityVerificationResult());

}

	private static Object[] getIdentityVerificationResultByVerificationResult(
			String string) {
		// TODO Auto-generated method stub
		return null;
	}
}
	

	