/*
 * Copyright (C) 2009-2011 University of Paderborn, Computer Networks Group
 * (Full list of owners see http://www.netinf.org/about-2/license)
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * 
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *       this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of Paderborn nor the names of its contributors may be used to endorse
 *       or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDERS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
/**
 * 
 */
package netinf.common.security.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

import netinf.common.security.SignatureAlgorithm;
import netinf.common.utils.Utils;

import org.junit.Test;

/**
 * The Class SignatureAlgorithmTest.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class SignatureAlgorithmTest {

   private final SignatureAlgorithm signatureAlgorithm = new SignatureAlgorithmImpl();

   @Test
   public void testSHA1hashing() {
      try {
         String originalString = "TestString";
         String hash = signatureAlgorithm.hash(originalString, "SHA1");
         assertEquals("d598b03bee8866ae03b54cb6912efdfef107fd6d", hash);
      } catch (NoSuchAlgorithmException e) {
         assertTrue(false);
      }
   }

   @Test
   public void testSHA1withDSAVerification() {
      KeyPair keyPair = getDSAKeyPair(1024);

      String originalString = "TestString";

      try {
         String signature = signatureAlgorithm.sign(originalString, keyPair.getPrivate(), "SHA1withDSA");
         assertTrue(signatureAlgorithm.verifySignature(originalString, signature, keyPair.getPublic(), "SHA1withDSA"));
      } catch (Exception e) {
         e.printStackTrace();
         assertTrue(false);
      }
   }

   @Test
   public void testSHA1withDSAVerificationModified() {
      KeyPair keyPair = getDSAKeyPair(1024);

      String originalString = "TestString";

      try {
         String signature = signatureAlgorithm.sign(originalString, keyPair.getPrivate(), "SHA1withDSA");
         originalString = "TestString_modified";
         assertFalse(signatureAlgorithm.verifySignature(originalString, signature, keyPair.getPublic(), "SHA1withDSA"));
      } catch (Exception e) {
         e.printStackTrace();
         assertTrue(false);
      }
   }

   private static KeyPair getDSAKeyPair(int keysize) {
      try {
         KeyPairGenerator k = KeyPairGenerator.getInstance("DSA");
         k.initialize(keysize);
         return k.generateKeyPair();

      } catch (NoSuchAlgorithmException e) {
         e.printStackTrace();
      } catch (Exception e) {
         e.printStackTrace();
      }
      return null;
   }

   @Test
   public void testStringByteConversion() {
      String original = "TESTSTRING";
      byte[] bytes = Utils.stringToBytes(original);
      Utils.bytesToString(bytes);
      assertEquals(original, Utils.bytesToString(Utils.stringToBytes(original)));
   }
}
