package netinf.common.securitytest;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import netinf.common.security.Hashing;

import org.junit.Test;


public class HashingTest {

	@Test
	public void testHashing(){
		byte[] bytes = new byte[1];
		bytes[0] = 65;
		
		InputStream input = new ByteArrayInputStream(bytes);
		try {
			assertEquals("A", new String(Hashing.hashSHA1(input)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}