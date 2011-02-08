package netinf.common.securitytest;

import static org.junit.Assert.assertArrayEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import netinf.common.security.Hashing;

import org.junit.Test;


public class HashingTest {

	@Test
	public void testHashing(){
		String s = "Hello";
		InputStream input = new ByteArrayInputStream(s.getBytes());
		byte[] result = {1,2,3,4};
		try {
			assertArrayEquals(result, Hashing.hashSHA1(input));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}