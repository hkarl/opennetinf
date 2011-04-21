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

import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import netinf.common.communication.NetInfNodeConnection;
import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.attribute.Attribute;
import netinf.common.datamodel.attribute.DefinedAttributeIdentification;
import netinf.common.datamodel.identity.IdentityObject;
import netinf.common.exceptions.NetInfCheckedException;
import netinf.common.exceptions.NetInfCheckedSecurityException;
import netinf.common.security.CryptoAlgorithm;
import netinf.common.security.Cryptography;
import netinf.common.security.identity.IdentityManager;
import netinf.common.security.impl.module.SecurityModule.Security;
import netinf.common.utils.Utils;

import org.apache.log4j.Logger;

import com.google.inject.Inject;

/**
 * Implements the interface Cryptography
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class CryptographyImpl implements Cryptography {

   private static final Logger LOG = Logger.getLogger(CryptographyImpl.class);
   private final IdentityManager identityManager;
   private final String defaultContentAlgorithm = "DES/ECB/PKCS5Padding";
   private final String defaultKeyAlgorithm = "RSA/ECB/PKCS1Padding";
   private final CryptoAlgorithm cryptoAlgorithm;
   private final DatamodelFactory factory;
   private final NetInfNodeConnection nodeConnection;

   public static final String PATH_SEPERATOR = "?";

   @Inject
   public CryptographyImpl(IdentityManager identityManager, CryptoAlgorithm cryptoAlgorithm, DatamodelFactory factory,
         @Security NetInfNodeConnection nodeConnection) {
      super();
      this.identityManager = identityManager;
      this.cryptoAlgorithm = cryptoAlgorithm;
      this.factory = factory;
      this.nodeConnection = nodeConnection;
      LOG.debug("Cryptography module created.");
   }

   /*
    * (non-Javadoc)
    * @see netinf.common.security.Cryptography#decrypt(netinf.common.datamodel. InformationObject)
    */
   @Override
   public InformationObject decrypt(InformationObject informationObject) throws NetInfCheckedSecurityException {
      return decrypt(informationObject, null, null);
   }

   /*
    * (non-Javadoc)
    * @see netinf.common.security.Cryptography#decrypt(netinf.common.datamodel. InformationObject)
    */
   @Override
   public InformationObject decrypt(InformationObject informationObject, String userName, String privateKey)
         throws NetInfCheckedSecurityException {
      Attribute enryptedContentProperty = informationObject
            .getSingleAttribute(DefinedAttributeIdentification.ENCRYPTED_INFORMATION_OBJECT.getURI());
      // Check whether the InformationObject has encrypted contend representing an encrypted InformationObject
      if ((enryptedContentProperty != null)) {
         LOG.debug("Encrypted content property found.");
         // Decrypt the encrypted contend
         String unencryptedString = decryptContent(enryptedContentProperty, userName, privateKey);
         if (unencryptedString != null) {
            LOG.debug("Succesfully decrypted content.");
            byte[] unecryptedBytes = Utils.stringToBytes(unencryptedString);
            // Create InformationObject from the unencrypted string
            InformationObject unencryptedObject = this.factory.createInformationObjectFromBytes(unecryptedBytes);
            return unencryptedObject;
         } else {
            LOG.error("Failed to unencrypt content.");
            throw new NetInfCheckedSecurityException("Failed to unencrypt content.");
         }
      } else {
         LOG.error("Encrypted content property not found.");
         throw new NetInfCheckedSecurityException("Encrypted content property not found.");
      }
   }

   /*
    * (non-Javadoc)
    * @see netinf.common.security.Cryptography#decrypt(netinf.common.datamodel.property .Property)
    */
   @Override
   public Attribute decrypt(Attribute property) throws NetInfCheckedSecurityException {
      return decrypt(property, null, null);
   }

   /*
    * (non-Javadoc)
    * @see netinf.common.security.Cryptography#decrypt(netinf.common.datamodel.property .Property)
    */
   @Override
   public Attribute decrypt(Attribute property, String userName, String privateKey) throws NetInfCheckedSecurityException {
      LOG.trace("Decrypting property.");
      Attribute enryptedContentProperty = null;
      // Check whether the Attribute has or is encrypted contend representing an encrypted Attribute
      if (property.getIdentification().equals(DefinedAttributeIdentification.ENCRYPTED_CONTENT.getURI())) {
         enryptedContentProperty = property;
      } else {
         enryptedContentProperty = property.getSingleSubattribute(DefinedAttributeIdentification.ENCRYPTED_CONTENT.getURI());
      }
      if ((enryptedContentProperty != null)) {
         LOG.debug("Encrypted content property found.");
         // Decrypt the encrypted contend
         String unencryptedString = decryptContent(enryptedContentProperty, userName, privateKey);
         if (unencryptedString != null) {
            LOG.debug("Succesfully decrypted content.");
            byte[] unecryptedBytes = Utils.stringToBytes(unencryptedString);
            // Create Attribute from the unencrypted string
            Attribute unencryptedObject = this.factory.createAttributeFromBytes(unecryptedBytes);
            return unencryptedObject;
         } else {
            LOG.error("Failed to unencrypt content.");
            throw new NetInfCheckedSecurityException("Failed to unencrypt content.");
         }
      } else {
         LOG.error("Encrypted content property not found.");
         throw new NetInfCheckedSecurityException("Encrypted content property not found.");
      }
   }

   /*
    * (non-Javadoc)
    * @seenetinf.common.security.Cryptography#encrypt(netinf.common.datamodel. InformationObject, java.util.List)
    */
   @Override
   public InformationObject encrypt(InformationObject informationObject) throws NetInfCheckedSecurityException {
      return encrypt(informationObject, getReaderList(informationObject), this.defaultContentAlgorithm, this.defaultKeyAlgorithm);
   }

   /*
    * (non-Javadoc)
    * @seenetinf.common.security.Cryptography#encrypt(netinf.common.datamodel. InformationObject, java.util.List)
    */
   @Override
   public InformationObject encrypt(InformationObject informationObject, Hashtable<String, PublicKey> readerKeys)
         throws NetInfCheckedSecurityException {
      return encrypt(informationObject, readerKeys, this.defaultContentAlgorithm, this.defaultKeyAlgorithm);
   }

   /*
    * (non-Javadoc)
    * @seenetinf.common.security.Cryptography#encrypt(netinf.common.datamodel. InformationObject, java.util.List,
    * java.lang.String)
    */
   @Override
   public InformationObject encrypt(InformationObject informationObject, String contentAlgorithm, String keyAlgorithm)
         throws NetInfCheckedSecurityException {
      return encrypt(informationObject, getReaderList(informationObject), contentAlgorithm, keyAlgorithm);
   }

   /*
    * (non-Javadoc)
    * @seenetinf.common.security.Cryptography#encrypt(netinf.common.datamodel. InformationObject, java.util.List,
    * java.lang.String)
    */
   @Override
   public InformationObject encrypt(InformationObject informationObject, Hashtable<String, PublicKey> readerKeys,
         String contentAlgorithm, String keyAlgorithm) throws NetInfCheckedSecurityException {
      // Serialize content
      String stringContent = Utils.bytesToString(informationObject.serializeToBytes());
      // Encrypt content
      Attribute encryptedContent = encryptContent(stringContent, readerKeys, contentAlgorithm, keyAlgorithm);
      encryptedContent.setIdentification(DefinedAttributeIdentification.ENCRYPTED_INFORMATION_OBJECT.getURI());
      // Create InformationObject containing the encrypted content
      InformationObject encryptedIO = this.factory.createInformationObject();
      encryptedIO.setIdentifier(this.factory.copyObject(informationObject.getIdentifier()));
      encryptedIO.addAttribute(encryptedContent);
      return encryptedIO;
   }

   /*
    * (non-Javadoc)
    * @see netinf.common.security.Cryptography#encrypt(netinf.common.datamodel.property .Property, java.util.List)
    */
   @Override
   public Attribute encrypt(Attribute attribute) throws NetInfCheckedSecurityException {
      return encrypt(attribute, getReaderList(attribute), this.defaultContentAlgorithm, this.defaultKeyAlgorithm);
   }

   /*
    * (non-Javadoc)
    * @see netinf.common.security.Cryptography#encrypt(netinf.common.datamodel.property .Property, java.util.List)
    */
   @Override
   public Attribute encrypt(Attribute attribute, Hashtable<String, PublicKey> readerKeys) throws NetInfCheckedSecurityException {
      return encrypt(attribute, readerKeys, this.defaultContentAlgorithm, this.defaultKeyAlgorithm);
   }

   /*
    * (non-Javadoc)
    * @see netinf.common.security.Cryptography#encrypt(netinf.common.datamodel.property .Property, java.util.List,
    * java.lang.String)
    */
   @Override
   public Attribute encrypt(Attribute attribute, String contentAlgorithm, String keyAlgorithm)
         throws NetInfCheckedSecurityException {
      return encrypt(attribute, getReaderList(attribute), contentAlgorithm, keyAlgorithm);
   }

   /*
    * (non-Javadoc)
    * @see netinf.common.security.Cryptography#encrypt(netinf.common.datamodel.property .Property, java.util.List,
    * java.lang.String)
    */
   @Override
   public Attribute encrypt(Attribute attribute, Hashtable<String, PublicKey> readerKeys, String contentAlgorithm,
         String keyAlgorithm) throws NetInfCheckedSecurityException {
      // Serialize content
      String stringContent = Utils.bytesToString(attribute.serializeToBytes());
      // Encrypt content
      Attribute encryptedContent = encryptContent(stringContent, readerKeys, contentAlgorithm, keyAlgorithm);
      return encryptedContent;
   }

   /**
    * Encrypts the given content using the given algorithm. Returns a property containing the encypted version of the content,
    * information about the used algorithm and a key list for the given readers.
    * 
    * @param content
    *           the content to be encrypted
    * @param readers
    *           the readers that are able to decrypt the content later on
    * @param algorithm
    *           the algorithm used for encryption
    * @return the property containing the encrypted content
    */
   private Attribute encryptContent(String content, Hashtable<String, PublicKey> readerKeys, String contentAlgorithm,
         String keyAlgorithm) throws NetInfCheckedSecurityException {
      try {
         // Read requested algorithm for content encryption
         String generatorAlgorithm = contentAlgorithm;
         // Not interested in any options for the algorithm since it is now only used to generate a key
         if (generatorAlgorithm.contains("/")) {
            generatorAlgorithm = generatorAlgorithm.substring(0, generatorAlgorithm.indexOf("/"));
         }
         // Generate secret key for the given algorithm
         SecretKey secretKey = KeyGenerator.getInstance(generatorAlgorithm).generateKey();

         LOG.debug("Encrypting content.");
         // Encrypt content with given algorithm and generated key
         String encryptedContentString = this.cryptoAlgorithm.encrypt(contentAlgorithm, secretKey, content);
         LOG.debug("Encrypted content: " + encryptedContentString);

         // Create an Attribute containing the encrypted content
         Attribute encryptedContentAttribute = this.factory.createAttribute(DefinedAttributeIdentification.ENCRYPTED_CONTENT
               .getURI(), encryptedContentString);
         // Add a key list for the readers to the Attribute
         Attribute keyList = generateKeyListAttribute(secretKey, readerKeys, keyAlgorithm);
         encryptedContentAttribute.addSubattribute(keyList);

         // Create an entry stating the used algorithm for content encryption
         Attribute usedAlgorithm = this.factory.createAttribute(DefinedAttributeIdentification.ENCRYPTION_ALGORITHM.getURI(),
               contentAlgorithm);
         encryptedContentAttribute.addSubattribute(usedAlgorithm);
         return encryptedContentAttribute;
      } catch (NoSuchAlgorithmException e) {
         LOG.error("SecretKey generator algorithm not found.");
         throw new NetInfCheckedSecurityException("SecretKey generator algorithm not found. " + e.getMessage());
      }
   }

   /**
    * Generates a key list attribute containing the key required to unencrypt the content. Each entry hold the key encrypted for
    * one reader. The key will be encrypted using the readers public key.
    * 
    * @param key
    *           the key that will be encrypted
    * @param readerKeys
    *           the readers the key is encrypted for
    * @param contentAlgorithm
    *           the algorithm that was used to encrypt the content
    * @param keyAlgorithm
    *           the algorithm that will be used to encrypt the secretkey
    * @return the property containing a subattribute for each reader
    */
   private Attribute generateKeyListAttribute(SecretKey secretKey, Hashtable<String, PublicKey> readerKeys, String keyAlgorithm)
         throws NetInfCheckedSecurityException {

      // Check whether there is any reader
      if (!readerKeys.isEmpty()) {
         LOG.debug("Generating reader key list.");
         LOG.trace("SecretKet algorithm: " + keyAlgorithm);
         // Create an Attribute representing the list of keys
         Attribute keyList = this.factory.createAttribute(DefinedAttributeIdentification.ENCRYPTED_READER_KEY_LIST.getURI(),
               keyAlgorithm);

         // Create an entry in the list for each reader given
         for (String reader : readerKeys.keySet()) {
            try {
               // Encrypt the secret key for the current reader
               Attribute readerKeyAttribute = generateEncryptedReaderKeyAttribute(secretKey, reader, readerKeys.get(reader),
                     keyAlgorithm);
               // Add the entry to the list Attribute
               keyList.addSubattribute(readerKeyAttribute);
            } catch (NetInfCheckedSecurityException securityException) {
               LOG.error("Failed to generate readerKey Attribute for " + reader + ". " + securityException.getMessage());
               throw new NetInfCheckedSecurityException("Failed to generate readerKey Attribute for " + reader + ". "
                     + securityException.getMessage());
            }
         }
         return keyList;
      } else {
         throw new NetInfCheckedSecurityException("No PublicKey in reader list.");
      }
   }

   /**
    * Generates an encrypted key property containing the SecretKey required to unencrypt the content. The SecretKey will be
    * encrypted using the readers public key.
    * 
    * @param secretKey
    *           the key that will be encrypted
    * @param reader
    *           the reader the key is encrypted for
    * @param readerKey
    *           the readers public key the secretKey is encrypted with
    * @param algo
    *           the algorithm used to encrypt the key
    * @return the property containing the key
    */
   private Attribute generateEncryptedReaderKeyAttribute(SecretKey secretKey, String reader, PublicKey readerKey, String algo)
         throws NetInfCheckedSecurityException {
      // Check whether a key is given to encrypt the secret key
      if (readerKey != null) {
         LOG.debug("ReaderKey found.");
         LOG.trace("ReaderKey: " + readerKey);
         LOG.trace("Encrypting SecretKey: " + secretKey);
         LOG.trace("Encryption algorithm: " + algo);
         // Encrypt the secret key for the given reader
         String encryptedKey = this.cryptoAlgorithm.encryptSecretKey(algo, readerKey, secretKey);
         LOG.trace("Encrypted ReaderKey: " + encryptedKey);

         // Create an Attribute for the encrypted key
         Attribute readerKeyAttribute = this.factory.createAttribute(DefinedAttributeIdentification.ENCRYPTED_READER_KEY_ENTRY
               .getURI(), reader);
         readerKeyAttribute.addSubattribute(this.factory.createAttribute(DefinedAttributeIdentification.ENCRYPTED_READER_KEY
               .getURI(), encryptedKey));

         return readerKeyAttribute;
      } else {
         throw new NetInfCheckedSecurityException("No reader key given to encrypt SecretKey.");
      }
   }

   /**
    * Decrypts an encrypted content attribute. The algorithm used for encryption has to be mentioned in a subattribute. A list
    * that contains the key required for decrypting the content encrypted for the user has to be a subattribute. The content will
    * be returned as String, representing a serialized NetInfObject.
    * 
    * @param contentAttribute
    *           the content attribute to decrypt
    * @param userName
    *           username that is used to load the private keys required for decryption
    * @param userName
    *           private key that is used to load the private keys required for decryption
    * @return the string
    */
   private String decryptContent(Attribute contentAttribute, String userName, String privateKey)
         throws NetInfCheckedSecurityException {
      // Get the Attribute stating the algorithm that was used to encrypt the content
      Attribute usedAlgorithmAttribute = contentAttribute
            .getSingleSubattribute(DefinedAttributeIdentification.ENCRYPTION_ALGORITHM.getURI());
      if (usedAlgorithmAttribute != null) {
         // Get the used algorithm
         String algorithm = usedAlgorithmAttribute.getValue(String.class);
         if (algorithm != null) {
            LOG.debug("Algorithm used to encrypt content found.");
            LOG.trace("Algorithm used to encrypt content: " + algorithm);
            // Read the key list Attribute to get the secret key used to encrypt the content
            SecretKey key = readKeyListAttribute(contentAttribute, algorithm, userName, privateKey);
            LOG.debug("Used SecretKey decrypted.");
            LOG.trace("SecretKey: " + key);
            // Decrypt the content with help of the algorithm and the key
            String content = this.cryptoAlgorithm.decrypt(algorithm, key, contentAttribute.getValue(String.class));
            return content;
         } else {
            LOG.warn("No algorithm used to encrypt content found.");
            throw new NetInfCheckedSecurityException("Decryption failed. No algorithm used to encrypt content found.");
         }
      } else {
         LOG.warn("Encryption algorithm attribute not found.");
         throw new NetInfCheckedSecurityException("Decryption failed. Encryption algorithm attribute not found.");
      }
   }

   /**
    * The key list is read with help of the private user key and the algorithm. If successful the key to decrypt the content will
    * be found and returned.
    * 
    * @param contentAttribute
    *           the content attribute containing the
    * @param algorithm
    *           the algorithm used for content encryption
    * @param userName
    *           username that is used to load the private keys required to decrypt the reader key
    * @param userName
    *           private key that is used to load the private keys required to decrypt the reader key
    * @return the private key
    */
   private SecretKey readKeyListAttribute(Attribute contentAttribute, String algorithm, String userName, String privateKey)
         throws NetInfCheckedSecurityException {
      // Check whether there is a reader key list
      Attribute readerListAttribute = contentAttribute
            .getSingleSubattribute(DefinedAttributeIdentification.ENCRYPTED_READER_KEY_LIST.getURI());
      if (readerListAttribute != null) {
         // Read the algorithm used to encrypt the content
         String generatorAlgorithm = algorithm;
         // Not interested in any options for the algorithm since it is now only used to read the secret key
         if (generatorAlgorithm.contains("/")) {
            generatorAlgorithm = generatorAlgorithm.substring(0, generatorAlgorithm.indexOf("/"));
         }
         // Read the algorithm used to encrypt the secret key
         String usedAlgorithm = readerListAttribute.getValue(String.class);
         if (usedAlgorithm != null && usedAlgorithm.length() > 0) {
            LOG.trace("Algorithm used to encrypt SecretKey: " + usedAlgorithm);
            // Check for each entry in the reader key list whether it can be decrypted
            for (Attribute reader : readerListAttribute.getSubattribute(DefinedAttributeIdentification.ENCRYPTED_READER_KEY_ENTRY
                  .getURI())) {
               // Get the Identity the current key was encrypted for
               String keyName = reader.getValue(String.class);
               // Check whether the referenced private key is available
               if (this.identityManager.hasPrivateKey(keyName, userName, privateKey)) {
                  LOG.trace("Using PrivateKey: " + keyName);
                  // Get the secret key encrypted for the referenced private key
                  String encryptedKey = reader
                        .getSingleSubattribute(DefinedAttributeIdentification.ENCRYPTED_READER_KEY.getURI()).getValue(
                              String.class);
                  LOG.trace("Decrypting ReaderKey: " + encryptedKey);
                  try {
                     // Use the referenced private key to decrept the secret key
                     SecretKey secretKey = this.cryptoAlgorithm.decryptSecretKey(usedAlgorithm, generatorAlgorithm,
                           this.identityManager.getPrivateKey(keyName, userName, privateKey), encryptedKey);
                     LOG.trace("Decrypted Value: " + secretKey);
                     return secretKey;
                  } catch (NetInfCheckedSecurityException securityException) {
                     LOG.warn("Security exception, unable to decrypt secret key. " + securityException.getMessage());
                  } catch (NetInfCheckedException checkedException) {
                     LOG.warn("Checket exception, private key could not be retrieved. " + checkedException.getMessage());
                  }
               }
            }
         }
      }
      throw new NetInfCheckedSecurityException("No key encrypted for any availible identity found.");
   }

   /**
    * Returns a Hashtable containing the names and the public keys of all readers that should be able to read the given
    * InformationObject.
    * 
    * @param informationObject
    *           the InformaitonObject for which the reader list should be returned
    * @return the Hashtable
    */
   private Hashtable<String, PublicKey> getReaderList(InformationObject informationObject) {
      List<Identifier> identifierList = informationObject.getReaderIdentifiers();
      return getReaderList(identifierList);
   }

   /**
    * Returns a Hashtable containing the names and the public keys of all readers that should be able to read the given Attribute.
    * 
    * @param attribute
    * @return
    */
   private Hashtable<String, PublicKey> getReaderList(Attribute attribute) {
      // Get the subattribute containing the readers
      List<Attribute> readerListProperties = attribute
            .getSubattribute(DefinedAttributeIdentification.AUTHORIZED_READERS.getURI());

      List<Identifier> identifierList = new ArrayList<Identifier>();

      // Create the Identifiers of all readers
      for (Attribute readerList : readerListProperties) {
         List<Attribute> readerProperties = readerList.getSubattribute(DefinedAttributeIdentification.READER.getURI());
         for (Attribute reader : readerProperties) {
            try {
               // To create the Identifier only the first part of the path to the key is required
               String readerIdentifierString = reader.getValue(String.class);
               if (readerIdentifierString.indexOf("?") != -1) {
                  readerIdentifierString = readerIdentifierString.substring(0, readerIdentifierString.indexOf("?"));
               }

               // Create an identifier from the read string
               Identifier identifier = this.factory.createIdentifierFromString(readerIdentifierString);
               identifierList.add(identifier);
            } catch (Exception e) {
               LOG.warn("Unable to get reader Identifier from String " + reader + ". " + e.getMessage());
            }
         }
      }
      return getReaderList(identifierList);
   }

   /**
    * Returns a Hashtable containing the names and the public keys of all the Identities referenced by the given Identifiers.
    * 
    * @param attribute
    * @return
    */
   private Hashtable<String, PublicKey> getReaderList(List<Identifier> identifierList) {
      // Check whether a list of identifiers was given
      if (identifierList != null) {
         Hashtable<String, PublicKey> readers = new Hashtable<String, PublicKey>();
         // Receive the public key for each identity given in the list
         for (Identifier identifier : identifierList) {
            InformationObject identity;
            try {
               // Get the Identity that belongs to the Identifier
               identity = this.nodeConnection.getIO(identifier);
               // Check whether it is an Identity
               if (identity instanceof IdentityObject) {
                  // Put the name of the Identity combined with the path to the public master key as entry into the hashmap and
                  // the the value to the value of the public master key
                  readers.put(((IdentityObject) identity).getIdentifier().toString() + "?"
                        + DefinedAttributeIdentification.PUBLIC_KEY.getURI(), ((IdentityObject) identity).getPublicMasterKey());
               }
            } catch (NetInfCheckedException e) {
               LOG.warn("Unable to retrieve IdentityObject " + identifier.toString() + ". " + e.getMessage());
            }
         }
         return readers;
      }
      return null;
   }

}