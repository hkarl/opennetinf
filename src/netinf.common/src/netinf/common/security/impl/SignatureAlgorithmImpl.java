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

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;

import netinf.common.security.SignatureAlgorithm;
import netinf.common.utils.Utils;

import org.apache.commons.codec.binary.Base64;

/**
 * Implements the {@link SignatureAlgorithm} interface
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class SignatureAlgorithmImpl implements SignatureAlgorithm {

   /*
    * (non-Javadoc)
    * @see netinf.common.security.SignatureAlgorithm#hash(java.lang.String, java.lang.String)
    */
   @Override
   public String hash(String originalString, String hashFunction) throws NoSuchAlgorithmException {
      MessageDigest algorithm = MessageDigest.getInstance(hashFunction);
      algorithm.reset();
      algorithm.update(originalString.getBytes());
      byte[] messageDigest = algorithm.digest();
      return Utils.hexStringFromBytes(messageDigest);
   }

   /**
    * @see SignatureAlgorithm#sign(String, PrivateKey, String)
    */
   @Override
   public String sign(String originalString, PrivateKey sk, String hashAndSignatureFunction) throws NoSuchAlgorithmException,
   InvalidKeyException, SignatureException {
      Signature signature = Signature.getInstance(hashAndSignatureFunction);
      signature.initSign(sk);
      signature.update(originalString.getBytes());
      return Base64.encodeBase64String(signature.sign());
   }

   /**
    * @see SignatureAlgorithm#verifySignature(String, String, PublicKey, String)
    */
   @Override
   public boolean verifySignature(String originalString, String signatureString, PublicKey pk, String hashAndSignatureFunction)
   throws SignatureException, NoSuchAlgorithmException, InvalidKeyException {
      Signature signature = Signature.getInstance(hashAndSignatureFunction);
      signature.initVerify(pk);
      signature.update(originalString.getBytes());
      return signature.verify(Base64.decodeBase64(signatureString));
   }

}
