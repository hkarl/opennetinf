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
package netinf.common.security.identity.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.attribute.DefinedAttributeIdentification;
import netinf.common.datamodel.creator.ValidCreator;
import netinf.common.datamodel.identity.IdentityObject;
import netinf.common.exceptions.NetInfCheckedException;
import netinf.common.security.CryptoAlgorithm;
import netinf.common.security.SignatureAlgorithm;
import netinf.common.security.identity.IdentityManager;
import netinf.common.security.impl.IntegrityImpl;
import netinf.common.utils.Utils;

import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;

import com.google.inject.Inject;

/**
 * The Class IdentityManagerImpl. {@link IdentityManagerImpl} implements the {@link IdentityManager} interface. It uses local
 * files to store the Private Keys persistently. If not used as Singleton, pay high attention to use different paths for the file
 * used to store Private Keys in ( {@link IdentityManager#setFilePath(String)})
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class IdentityManagerImpl implements netinf.common.security.identity.IdentityManager {

   private static final Logger LOG = Logger.getLogger(IntegrityImpl.class);

   /**
    * Maps Identity (as a String, e.g. IdO-Identifier/PUBLIC_MASTER_KEY) to according PrivateKey
    */
   private Map<String, PrivateKey> privateKeys;
   /**
    * In case of multiple Identity Stores being managed, this Map maps the UserSession to the related Identity->Private Key Map
    * "privateKeys"
    */
   private final Map<UserSession, Map<String, PrivateKey>> sessions = new HashMap<UserSession, Map<String, PrivateKey>>();

   /**
    * Default Password (symmetric) used to encrypt/decrypt key store file
    */
   private String defaultPassword = "passwordpasswordpassword";
   /**
    * Default path to key store file
    */
   private String defaultFilepath = "../configs/Identities/privateKeyFile.pkf";
   /**
    * Default algorithm used to encrypt Private Keys in key store file
    */
   private final String defaultKeyAlgorithmName = "DESede";

   private final CryptoAlgorithm cryptoAlgorithm;

   @Inject
   public IdentityManagerImpl(DatamodelFactory datamodelFactory, SignatureAlgorithm signatureAlgorithm,
         CryptoAlgorithm cryptoAlgorithm) {
      this.cryptoAlgorithm = cryptoAlgorithm;
      this.privateKeys = new HashMap<String, PrivateKey>();
   }

   /**
    * @see IdentityManager#createNewMasterIdentity()
    */
   @Override
   public IdentityObject createNewMasterIdentity() throws NetInfCheckedException {
      // as soon as a new Master Identity is created, all Identites known to this IdentityManager will be stored to file. Thus,
      // ensure that identities are loaded from file before
      if (this.privateKeys.size() == 0) {
         try {
            loadIdentities();
         } catch (NetInfCheckedException e) {
            LOG.warn("Unable to load key file. " + e.getMessage());
         }
      }

      KeyPairGenerator k;
      try {
         k = KeyPairGenerator.getInstance("RSA");

      } catch (Exception e) {
         LOG.warn(e.getMessage());
         return null;
      }

      k.initialize(1024);
      KeyPair pair = k.generateKeyPair();

      // A new Master Identity implies a new Identity Object. Create it
      IdentityObject newIdentity = ValidCreator.createValidIdentityObject(pair.getPublic());

      // Derive the "Identity-Path"
      String pathToKey = newIdentity.getIdentifier().toString() + IntegrityImpl.PATH_SEPERATOR
      + DefinedAttributeIdentification.PUBLIC_KEY.getURI();

      this.privateKeys.put(pathToKey, pair.getPrivate());
      
      LOG.info("Private Key: "+Utils.objectToString(pair.getPrivate()));
      LOG.info("Public Key: "+Utils.objectToString(pair.getPublic()));

      // save private keys to file
      writePrivateKeysToFile(this.defaultFilepath, this.defaultKeyAlgorithmName, this.defaultPassword);

      return newIdentity;
   }

   /**
    * @see IdentityManager#createNewSubKey(IdentityObject, String)
    */
   @Override
   public String createNewSubKey(IdentityObject identityObject, String path) {
      throw new NotImplementedException();
   }

   /**
    * @see IdentityManager#getLocalIdentity()
    */
   @Override
   public String getLocalIdentity() throws NetInfCheckedException {
      if (this.privateKeys.size() == 0) {
         try {
            loadIdentities();
         } catch (NetInfCheckedException e) {
            LOG.warn("Unable to load key file. " + e.getMessage());
         }
      }

      // TODO More sophisticated choice of local identity could be implemented
      if (this.privateKeys.size() > 0) {
         return this.privateKeys.keySet().iterator().next();
      }

      throw new NetInfCheckedException("No local identity present.");
   }

   /**
    * @see IdentityManager#getPrivateKey(String)
    */
   @Override
   public PrivateKey getPrivateKey(String pathToPublicKey) throws NetInfCheckedException {
      return getPrivateKey(pathToPublicKey, null, null);
   }

   /**
    * @see IdentityManager#getPrivateKey(String, String, String)
    */
   @Override
   public PrivateKey getPrivateKey(String pathToPublicKey, String userName, String privateKey) throws NetInfCheckedException {
      if (userName != null && privateKey != null) {
         UserSession session = new UserSession(userName, privateKey);
         if (!this.sessions.containsKey(session)) {
            this.sessions.put(session, getPrivateKeysFromFile(userName + ".pkf", privateKey));
         }
         Map<String, PrivateKey> map = this.sessions.get(session);

         PrivateKey privKey = map.get(pathToPublicKey);
         if (privKey == null) {
            throw new NetInfCheckedException("Private Key not found.");
         }

         return privKey;

      } else {
         if (this.privateKeys.size() == 0) {
            try {
               loadIdentities();
            } catch (NetInfCheckedException e) {
               LOG.warn("Unable to load key file. " + e.getMessage());
            }
         }

         PrivateKey privKey = this.privateKeys.get(pathToPublicKey);
         if (privKey == null) {
            throw new NetInfCheckedException("Private Key not found.");
         }

         return privKey;
      }
   }

   /**
    * Writes all the Private Keys this IdentityManager knows to a file encrypted by a Secret Key (symmetric)
    * 
    * @param filepath
    *           Path to file where Private Keys shall be stored
    * @param algorithmUsedToEncryptTheKey
    *           Algorithm used to encrypt the Private Key
    * @param password
    *           Password used to encrypt the Private Keys
    * @throws NetInfCheckedException
    */
   private void writePrivateKeysToFile(String filepath, String algorithmUsedToEncryptTheKey, String password)
   throws NetInfCheckedException {
      BufferedWriter bw;
      try {
         bw = new BufferedWriter(new FileWriter(filepath));
      } catch (FileNotFoundException e) {
         LOG.error(e.getMessage());
         throw new NetInfCheckedException(e.getMessage());
      } catch (IOException e) {
         LOG.error(e.getMessage());
         throw new NetInfCheckedException(e.getMessage());
      }

      Iterator<Entry<String, PrivateKey>> iterator = this.privateKeys.entrySet().iterator();
      while (iterator.hasNext()) {
         java.util.Map.Entry<String, PrivateKey> entry = iterator.next();

         try {
            // append alias
            bw.write(entry.getKey());
            bw.newLine();
            // append algorithm used to encrypt the key
            bw.write(algorithmUsedToEncryptTheKey);
            bw.newLine();
            // append algorithm key is used for
            bw.write(entry.getValue().getAlgorithm());
            bw.newLine();
            // append encrypted private key

            String encryptedPrivateKey = this.cryptoAlgorithm.encryptPrivateKey(algorithmUsedToEncryptTheKey,
                  this.cryptoAlgorithm.getSecretKeyFromString(algorithmUsedToEncryptTheKey, password), entry.getValue());
            char[] encryptedPrivateKeyChars = encryptedPrivateKey.toCharArray();

            for (int i = 0; i < encryptedPrivateKeyChars.length; i++) {
               bw.write("" + ((int) encryptedPrivateKeyChars[i]));
               bw.newLine();
            }

            bw.write("END OF KEY");
            bw.newLine();

         } catch (IOException e) {
            LOG.warn("PrivateKey couldn't be saved to file. " + e.getMessage());
         } finally {
            try {
               bw.flush();
            } catch (IOException e) {
               LOG.warn("Error writing to file. " + e.getMessage());
               throw new NetInfCheckedException("Error writing to file.");
            }
         }
      }
      try {
         bw.close();
      } catch (IOException e) {
         LOG.warn("Unable to close file. " + e.getMessage());
         throw new NetInfCheckedException("Unable to close file.");
      }
   }

   /**
    * Retrieves the Private Keys from a local file
    * 
    * @param filepath
    *           Path of file the Private Keys are stored in
    * @param password
    *           Secret Key (symmetric) used to encrypt the Private Keys
    * @return Map of String (Path to Identity, e.g. IdO-Identifier/PublicMasterKey) and according Private Key
    * @throws NetInfCheckedException
    */
   private Map<String, PrivateKey> getPrivateKeysFromFile(String filepath, String password) throws NetInfCheckedException {
      BufferedReader br;
      try {
         br = new BufferedReader(new FileReader(filepath));
      } catch (FileNotFoundException e) {
         LOG.error(e.getMessage());
         throw new NetInfCheckedException(e.getMessage());
      }

      try {
         Map<String, PrivateKey> privateKeys = new HashMap<String, PrivateKey>();
         String alias;
         String algorithmUsedToEncryptTheKey;
         String algorithmKeyIsUsedFor;
         String encryptedPrivateKey;

         while (true) {
            alias = br.readLine();
            algorithmUsedToEncryptTheKey = br.readLine();
            algorithmKeyIsUsedFor = br.readLine();
            /*
             * encryptedPrivateKey = br.readLine(); encryptedPrivateKey =
             * encryptedPrivateKey.replaceAll(REPLACEMENT_CARRIAGE_RETURN, "\r").replaceAll( REPLACEMENT_NEW_LINE, "\n");
             */
            encryptedPrivateKey = new String();
            String line = br.readLine();
            while (!line.equals("END OF KEY")) {
               encryptedPrivateKey += ((char) Integer.parseInt(line));
               line = br.readLine();
            }

            PrivateKey privKey = this.cryptoAlgorithm.decryptPrivateKey(algorithmUsedToEncryptTheKey, algorithmKeyIsUsedFor,
                  this.cryptoAlgorithm.getSecretKeyFromString(algorithmUsedToEncryptTheKey, password), encryptedPrivateKey);

            privateKeys.put(alias, privKey);

            return privateKeys;
         }
      } catch (Exception e) {
         LOG.error("Unable to read keys from file. " + e.getMessage());
         throw new NetInfCheckedException("Unable to read keys from file. " + e.getMessage());
      }

   }

   /**
    * @see IdentityManager#hasPrivateKey(String)
    */
   @Override
   public boolean hasPrivateKey(String pathToPublicKey) {
      return hasPrivateKey(pathToPublicKey, null, null);
   }

   /**
    * @see IdentityManager#hasPrivateKey(String, String, String)
    */
   @Override
   public boolean hasPrivateKey(String pathToPublicKey, String userName, String privateKey) {
      if (userName != null && privateKey != null) {
         UserSession session = new UserSession(userName, privateKey);
         if (!this.sessions.containsKey(session)) {
            try {
               this.sessions.put(session, getPrivateKeysFromFile(userName + ".pkf", privateKey));
            } catch (NetInfCheckedException e) {
               LOG.warn("Unable to load key file. " + e.getMessage());
               return false;
            }
         }
         Map<String, PrivateKey> map = this.sessions.get(session);

         return map.containsKey(pathToPublicKey);

      } else {
         if (this.privateKeys.size() == 0) {
            try {
               loadIdentities();
            } catch (NetInfCheckedException e) {
               LOG.warn("Unable to load key file. " + e.getMessage());
            }
         }

         return this.privateKeys.containsKey(pathToPublicKey);
      }
   }

   /**
    * @see IdentityManager#loadIdentities()
    */
   @Override
   public void loadIdentities() throws NetInfCheckedException {
      this.privateKeys = getPrivateKeysFromFile(this.defaultFilepath, this.defaultPassword);
   }

   /**
    * @see IdentityManager#setFilePath(String)
    */
   @Override
   public void setFilePath(String path) {
      this.defaultFilepath = path;
   }

   /**
    * @see IdentityManager#setPassword(String)
    */
   @Override
   public void setPassword(String password) {
      this.defaultPassword = password;
   }

   /**
    * @see IdentityManager#storeIdentities()
    */
   @Override
   public void storeIdentities() throws NetInfCheckedException {
      writePrivateKeysToFile(this.defaultFilepath, this.defaultKeyAlgorithmName, this.defaultPassword);
   }
}

/**
 * To manage several files containing Private Keys, the {@link UserSession} stores user names and their password to decrypt their
 * Private Key file
 */
class UserSession {
   private final String userName;
   private final String privateKey;

   public UserSession(String userName, String privateKey) {
      this.userName = userName;
      this.privateKey = privateKey;
   }

   public String getUserName() {
      return this.userName;
   }

   public String getPrivateKey() {
      return this.privateKey;
   }

   @Override
   public int hashCode() {
      if (this.userName == null) {
         return 0;
      }

      return this.userName.hashCode();
   }

   @Override
   public boolean equals(Object o) {
      if (o instanceof UserSession) {
         UserSession session = (UserSession) o;
         return session.userName.equals(this.userName) && session.privateKey.equals(this.privateKey);
      }
      return false;
   }

}
