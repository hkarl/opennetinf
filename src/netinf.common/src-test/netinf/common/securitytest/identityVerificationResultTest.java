
package netinf.common.securitytest;

import static org.junit.Assert.assertEquals;
import netinf.common.security.IdentityVerificationResult;

import org.junit.Test;



public class identityVerificationResultTest {

	@Test
	public void identityVerificationResulttest(){
		assertEquals("IDENTITY_VERIFICATION_FAILED",IdentityVerificationResult.getIdentityVerificationResult("IDENTITY_VERIFICATION_FAILED").getIdentityVerificationResult());
		assertEquals("IDENTITY_NOT_VERIFIABLE",IdentityVerificationResult.getIdentityVerificationResult("IDENTITY_NOT_VERIFIABLE").getIdentityVerificationResult());
		assertEquals("IDENTITY_VERIFICATION_SUCCEEDED",IdentityVerificationResult.getIdentityVerificationResult("IDENTITY_VERIFICATION_SUCCEEDED").getIdentityVerificationResult());
		
	}

}
	

	