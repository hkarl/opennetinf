/*
 * Copyright (C) 2009,2010 University of Paderborn, Computer Networks Group
 * Copyright (C) 2009,2010 Christian Dannewitz <christian.dannewitz@upb.de>
 * Copyright (C) 2009,2010 Thorsten Biermann <thorsten.biermann@upb.de>
 * 
 * Copyright (C) 2009,2010 Eduard Bauer <edebauer@mail.upb.de>
 * Copyright (C) 2009,2010 Matthias Becker <matzeb@mail.upb.de>
 * Copyright (C) 2009,2010 Frederic Beister <azamir@zitmail.uni-paderborn.de>
 * Copyright (C) 2009,2010 Nadine Dertmann <ndertmann@gmx.de>
 * Copyright (C) 2009,2010 Michael Kionka <mkionka@mail.upb.de>
 * Copyright (C) 2009,2010 Mario Mohr <mmohr@mail.upb.de>
 * Copyright (C) 2009,2010 Felix Steffen <felix.steffen@gmx.de>
 * Copyright (C) 2009,2010 Sebastian Stey <sebstey@mail.upb.de>
 * Copyright (C) 2009,2010 Steffen Weber <stwe@mail.upb.de>
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of Paderborn nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package netinf.common.security.impl;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;

import netinf.common.exceptions.NetInfCheckedSecurityException;
import netinf.common.security.CryptoAlgorithm;
import netinf.common.utils.Utils;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

/**
 * Implements the {@link CryptoAlgorithm} Interface.
 * 
 * @see CryptoAlgorithm
 * @author PG Augnet 2, University of Paderborn
 */
public class CryptoAlgorithmImpl implements CryptoAlgorithm {

   private static final Logger LOG = Logger.getLogger(CryptographyImpl.class);

   @Override
   public String decrypt(String algorithm, Key key, String encrypted) throws NetInfCheckedSecurityException {
      try {
         LOG.debug("Decrypting string.");
         LOG.trace("Used algorithm: " + algorithm);
         LOG.trace("Used key: " + key);
         LOG.trace("Used string: " + encrypted);
         Cipher cipher = Cipher.getInstance(algorithm);
         cipher.init(Cipher.DECRYPT_MODE, key);
         byte[] encryptedBytes = Base64.decodeBase64(encrypted);
         LOG.trace("Encrypted bytes: " + Utils.bytesToString(encryptedBytes));
         byte[] unencryptedBytes = cipher.doFinal(encryptedBytes);
         LOG.trace("Unencrypted bytes: " + Utils.bytesToString(unencryptedBytes));
         String unencryptedString = Utils.bytesToString(unencryptedBytes);
         LOG.trace("Unencrypted String: " + unencryptedString);
         return unencryptedString;
      } catch (NoSuchAlgorithmException e) {
         throw new NetInfCheckedSecurityException("Unknown cipher-algorithm. " + e.getMessage());
      } catch (NoSuchPaddingException e) {
         throw new NetInfCheckedSecurityException("Unknown cipher-padding. " + e.getMessage());
      } catch (InvalidKeyException e) {
         throw new NetInfCheckedSecurityException("Invalid Key. " + e.getMessage());
      } catch (IllegalBlockSizeException e) {
         throw new NetInfCheckedSecurityException("Illegal cipher-block-size. " + e.getMessage());
      } catch (BadPaddingException e) {
         throw new NetInfCheckedSecurityException("Bad cipher-padding. " + e.getMessage());
      }
   }

   @Override
   public String encrypt(String algorithm, Key key, String unencrypted) throws NetInfCheckedSecurityException {
      try {
         LOG.debug("Encrypting string.");
         LOG.trace("Used algorithm: " + algorithm);
         LOG.trace("Used key: " + key);
         LOG.trace("Used string: " + unencrypted);
         Cipher cipher = Cipher.getInstance(algorithm);
         cipher.init(Cipher.ENCRYPT_MODE, key);
         byte[] unencryptedBytes = Utils.stringToBytes(unencrypted);
         byte[] encryptedBytes = cipher.doFinal(unencryptedBytes);
         return Base64.encodeBase64String(encryptedBytes);
      } catch (NoSuchAlgorithmException e) {
         throw new NetInfCheckedSecurityException("Unknown cipher-algorithm: " + e.getMessage());
      } catch (NoSuchPaddingException e) {
         throw new NetInfCheckedSecurityException("Unknown cipher-padding: " + e.getMessage());
      } catch (InvalidKeyException e) {
         throw new NetInfCheckedSecurityException("Invalid Key. " + e.getMessage());
      } catch (IllegalBlockSizeException e) {
         throw new NetInfCheckedSecurityException("Illegal cipher-block-size: " + e.getMessage());
      } catch (BadPaddingException e) {
         throw new NetInfCheckedSecurityException("Bad cipher-padding: " + e.getMessage());
      }
   }

   @Override
   public String encryptSecretKey(String algorithmUsedToEncryptTheKey, Key key, SecretKey keyToEncrypt)
   throws NetInfCheckedSecurityException {
      try {
         LOG.debug("Encrypting SecretKey.");
         LOG.trace("Used algorithm for encryption: " + algorithmUsedToEncryptTheKey);
         LOG.trace("Used key: " + key);
         LOG.trace("Used key to be encrypted: " + keyToEncrypt);
         Cipher cipher = Cipher.getInstance(algorithmUsedToEncryptTheKey);
         cipher.init(Cipher.WRAP_MODE, key);
         return Base64.encodeBase64String(cipher.wrap(keyToEncrypt));
      } catch (NoSuchAlgorithmException e) {
         throw new NetInfCheckedSecurityException("Unknown cipher-algorithm: " + e.getMessage());
      } catch (NoSuchPaddingException e) {
         throw new NetInfCheckedSecurityException("Unknown cipher-padding: " + e.getMessage());
      } catch (InvalidKeyException e) {
         throw new NetInfCheckedSecurityException("Invalid Key. " + e.getMessage());
      } catch (IllegalBlockSizeException e) {
         throw new NetInfCheckedSecurityException("Illegal cipher-block-size: " + e.getMessage());
      }
   }

   @Override
   public SecretKey decryptSecretKey(String algorithmUsedToEncryptTheKey, String algorithmKeyIsUsedFor, Key key,
         String keyToDecrypt) throws NetInfCheckedSecurityException {
      try {
         LOG.debug("Decrypting SecretKey.");
         LOG.trace("Used algorithm for encryption: " + algorithmUsedToEncryptTheKey);
         LOG.trace("Used algorithm of encrypted key: " + algorithmKeyIsUsedFor);
         LOG.trace("Used key: " + key);
         LOG.trace("Used key to be decrypted: " + keyToDecrypt);
         Cipher cipher = Cipher.getInstance(algorithmUsedToEncryptTheKey);
         cipher.init(Cipher.UNWRAP_MODE, key);
         return (SecretKey) cipher.unwrap(Base64.decodeBase64(keyToDecrypt), algorithmKeyIsUsedFor, Cipher.SECRET_KEY);
      } catch (NoSuchAlgorithmException e) {
         throw new NetInfCheckedSecurityException("Unknown cipher-algorithm: " + e.getMessage());
      } catch (NoSuchPaddingException e) {
         throw new NetInfCheckedSecurityException("Unknown cipher-padding: " + e.getMessage());
      } catch (InvalidKeyException e) {
         throw new NetInfCheckedSecurityException("Invalid Key. " + e.getMessage());
      }
   }

   @Override
   public String encryptPrivateKey(String algorithmUsedToEncryptTheKey, Key key, PrivateKey keyToEncrypt)
   throws NetInfCheckedSecurityException {
      try {
         Cipher cipher = Cipher.getInstance(algorithmUsedToEncryptTheKey);
         cipher.init(Cipher.WRAP_MODE, key);
         return Utils.bytesToString(cipher.wrap(keyToEncrypt));
      } catch (NoSuchAlgorithmException e) {
         throw new NetInfCheckedSecurityException("Unknown cipher-algorithm: " + e.getMessage());
      } catch (NoSuchPaddingException e) {
         throw new NetInfCheckedSecurityException("Unknown cipher-padding: " + e.getMessage());
      } catch (InvalidKeyException e) {
         throw new NetInfCheckedSecurityException("Invalid Key. " + e.getMessage());
      } catch (IllegalBlockSizeException e) {
         throw new NetInfCheckedSecurityException("Illegal cipher-block-size: " + e.getMessage());
      }
   }

   @Override
   public PrivateKey decryptPrivateKey(String algorithmUsedToEncryptTheKey, String algorithmKeyIsUsedFor, Key key,
         String keyToDecrypt) throws NetInfCheckedSecurityException {
      try {
         Cipher cipher = Cipher.getInstance(algorithmUsedToEncryptTheKey);
         cipher.init(Cipher.UNWRAP_MODE, key);
         return (PrivateKey) cipher.unwrap(Utils.stringToBytes(keyToDecrypt), algorithmKeyIsUsedFor, Cipher.PRIVATE_KEY);
      } catch (NoSuchAlgorithmException e) {
         throw new NetInfCheckedSecurityException("Unknown cipher-algorithm: " + e.getMessage());
      } catch (NoSuchPaddingException e) {
         throw new NetInfCheckedSecurityException("Unknown cipher-padding: " + e.getMessage());
      } catch (InvalidKeyException e) {
         throw new NetInfCheckedSecurityException("Invalid Key. " + e.getMessage());
      }
   }

   @Override
   public SecretKey getSecretKeyFromString(String contentAlgorithmName, String password) throws NetInfCheckedSecurityException {

      try {
         DESedeKeySpec desKeySpec = new DESedeKeySpec(password.getBytes());
         SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(contentAlgorithmName);
         return keyFactory.generateSecret(desKeySpec);
      } catch (Exception e) {
         throw new NetInfCheckedSecurityException("Unable to create SecretKey. " + e.getMessage());
      }
   }
}
