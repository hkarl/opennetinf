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
package netinf.common.security.impl;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Hashtable;
import java.util.Properties;

import netinf.common.communication.NetInfNodeConnection;
import netinf.common.communication.RemoteNodeConnection;
import netinf.common.communication.SerializeFormat;
import netinf.common.communication.module.CommunicationModule;
import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.DefinedLabelName;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.IdentifierLabel;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.attribute.Attribute;
import netinf.common.datamodel.attribute.DefinedAttributeIdentification;
import netinf.common.datamodel.identity.IdentityObject;
import netinf.common.datamodel.impl.module.DatamodelImplModule;
import netinf.common.exceptions.NetInfCheckedException;
import netinf.common.exceptions.NetInfCheckedSecurityException;
import netinf.common.exceptions.NetInfUncheckedException;
import netinf.common.log.module.LogModule;
import netinf.common.security.CryptoAlgorithm;
import netinf.common.security.Cryptography;
import netinf.common.security.identity.IdentityManager;
import netinf.common.security.impl.module.SecurityModule;
import netinf.common.utils.Utils;

import org.apache.commons.lang.RandomStringUtils;
import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.name.Names;

/**
 * The Class CryptographyTest.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class CryptographyTest {

   public static final String NETINFNODE_PROPERTIES = "../configs/testing/netinfnode_testing.properties";

   private static Cryptography crypto;
   private static CryptoAlgorithm algorithm = new CryptoAlgorithmImpl();
   private static String defaultContentAlgorithmName = "DES/ECB/PKCS5Padding";
   private static String defaultKeyAlgorithmName = "RSA/ECB/PKCS1Padding";

   private static IdentityObject identityObject;
   private static PrivateKey privateKey;
   private static PublicKey publicKey;
   private static IdentityManager identityManager;
   private static Hashtable<String, PublicKey> publicKeys = new Hashtable<String, PublicKey>();

   private static Injector injector;
   private static DatamodelFactory factory;
   private static RemoteNodeConnection convenienceCommunicator;

   @BeforeClass
   public static void classSetUp() throws Exception {
      final Properties properties = Utils.loadProperties(NETINFNODE_PROPERTIES);
      injector = Guice.createInjector(new LogModule(properties), new DatamodelImplModule(), new CommunicationModule(),
            new SecurityModule(), new AbstractModule() {

               @Override
               protected void configure() {
                  bind(NetInfNodeConnection.class).annotatedWith(SecurityModule.Security.class).to(RemoteNodeConnection.class)
                        .in(Singleton.class);
                  Names.bindProperties(binder(), properties);
               }
            });
      factory = injector.getInstance(DatamodelFactory.class);

      identityObject = factory.createIdentityObject();
      Identifier id = factory.createIdentifier();
      IdentifierLabel label = factory.createIdentifierLabel();
      label.setLabelName(DefinedLabelName.UNIQUE_LABEL.getLabelName());
      label.setLabelValue("Test-Identity");
      id.addIdentifierLabel(label);
      identityObject.setIdentifier(id);

      try {
         KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
         keyPairGenerator.initialize(1024);
         KeyPair pair = keyPairGenerator.generateKeyPair();

         privateKey = pair.getPrivate();
         publicKey = pair.getPublic();
         String keyName = identityObject.getIdentifier().toString() + "?" + DefinedAttributeIdentification.PUBLIC_KEY.getURI();

         publicKeys.put(keyName, publicKey);

         identityObject.setPublicMasterKey(pair.getPublic());
      } catch (Exception e) {
         throw new NetInfUncheckedException("error creating keys");

      }
      convenienceCommunicator = EasyMock.createMock(RemoteNodeConnection.class);
      convenienceCommunicator.setHostAndPort("localhost", 5000);
      EasyMock.expectLastCall().anyTimes();
      convenienceCommunicator.setSerializeFormat(SerializeFormat.JAVA);
      EasyMock.expectLastCall().anyTimes();
      EasyMock.expect(convenienceCommunicator.getIO((Identifier) EasyMock.anyObject())).andReturn(identityObject).anyTimes();
      EasyMock.replay(convenienceCommunicator);

      identityManager = EasyMock.createMock(IdentityManager.class);
      EasyMock.expect(identityManager.getPrivateKey((String) EasyMock.anyObject())).andReturn(privateKey).anyTimes();
      EasyMock.expect(identityManager.hasPrivateKey((String) EasyMock.anyObject())).andReturn(true).anyTimes();
      EasyMock
            .expect(
                  identityManager.getPrivateKey(((String) EasyMock.anyObject()), (String) EasyMock.anyObject(),
                        (String) EasyMock.anyObject())).andReturn(privateKey).anyTimes();
      EasyMock
            .expect(
                  identityManager.hasPrivateKey(((String) EasyMock.anyObject()), (String) EasyMock.anyObject(),
                        (String) EasyMock.anyObject())).andReturn(true).anyTimes();
      EasyMock.replay(identityManager);

      crypto = new CryptographyImpl(identityManager, algorithm, factory, convenienceCommunicator);
   }

   @Test
   public void testAttributeEncryptionWithGivenKeys() throws NetInfCheckedSecurityException {
      Attribute attribute = createTestAttribute();
      Attribute encryptedAttribute = crypto.encrypt(attribute, publicKeys);
      Attribute decryptedAttribute = crypto.decrypt(encryptedAttribute);

      Assert.assertEquals(attribute.getIdentification(), decryptedAttribute.getIdentification());
      Assert.assertEquals(attribute.getValue(String.class), decryptedAttribute.getValue(String.class));
   }

   @Test
   public void testAttributeEncryptionWithoutGivenKeys() throws NetInfCheckedSecurityException {
      Attribute attribute = createTestAttribute();
      Attribute encryptedAttribute = crypto.encrypt(attribute);
      Attribute decryptedAttribute = crypto.decrypt(encryptedAttribute);

      Assert.assertEquals(attribute.getIdentification(), decryptedAttribute.getIdentification());
      Assert.assertEquals(attribute.getValue(String.class), decryptedAttribute.getValue(String.class));
   }

   @Test
   public void testInformationObjectEncryptionWithGivenKeys() throws NetInfCheckedSecurityException {
      InformationObject informationObject = createTestInformationObject();
      InformationObject encryptedInformationObject = crypto.encrypt(informationObject, publicKeys);
      InformationObject decryptedInformationObject = crypto.decrypt(encryptedInformationObject);

      Assert.assertEquals(informationObject.getIdentifier().toString(), decryptedInformationObject.getIdentifier().toString());
      Assert.assertEquals(informationObject.getSingleAttribute("Test-Attribute").getValueRaw(), decryptedInformationObject
            .getSingleAttribute("Test-Attribute").getValueRaw());
   }

   @Test
   public void testInformationObjectEncryptionWithoutGivenKeys() throws NetInfCheckedSecurityException {
      InformationObject informationObject = createTestInformationObject();
      InformationObject encryptedInformationObject = crypto.encrypt(informationObject);
      InformationObject decryptedInformationObject = crypto.decrypt(encryptedInformationObject);

      Assert.assertEquals(informationObject.getIdentifier().toString(), decryptedInformationObject.getIdentifier().toString());
      Assert.assertEquals(informationObject.getSingleAttribute("Test-Attribute").getValueRaw(), decryptedInformationObject
            .getSingleAttribute("Test-Attribute").getValueRaw());
   }

   @Test
   public void testWrongAlgorithmNames() {
      InformationObject informationObject = createTestInformationObject();
      try {
         crypto.encrypt(informationObject, "wrong algorithm", defaultKeyAlgorithmName);
         Assert.fail("Exception expected. Wrong content algotirhm given.");
      } catch (NetInfCheckedSecurityException securityException) {
         System.out.println(securityException.getMessage());
      }

      try {
         crypto.encrypt(informationObject, defaultContentAlgorithmName, "wrong algorithm");
         Assert.fail("Exception expected. Wrong key algotirhm given.");
      } catch (NetInfCheckedSecurityException securityException) {
         System.out.println(securityException.getMessage());
      }

      try {
         crypto.encrypt(informationObject, defaultKeyAlgorithmName, defaultContentAlgorithmName);
         Assert.fail("Exception expected. Wrong algotirhm given in wrong order.");
      } catch (NetInfCheckedSecurityException securityException) {
         System.out.println(securityException.getMessage());
      }

   }

   @Test
   public void testNoReaderList() {
      InformationObject informationObject = createTestInformationObject();

      Hashtable<String, PublicKey> readers1 = new Hashtable<String, PublicKey>();
      try {
         crypto.encrypt(informationObject, readers1);
         Assert.fail("Exception expected. No readers given.");
      } catch (NetInfCheckedSecurityException securityException) {
         System.out.println(securityException.getMessage());
      }
   }

   @Test
   public void testBadReaderKeyAlgorithm() {
      InformationObject informationObject = createTestInformationObject();

      Hashtable<String, PublicKey> readers = new Hashtable<String, PublicKey>();
      readers = new Hashtable<String, PublicKey>();
      try {
         KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("DSA");
         keyPairGenerator.initialize(1024);
         KeyPair pair = keyPairGenerator.generateKeyPair();

         PublicKey publicKey = pair.getPublic();
         readers.put("any name", publicKey);
      } catch (Exception e) {
         throw new NetInfUncheckedException("error creating keys");
      }
      try {
         crypto.encrypt(informationObject, readers);
         Assert.fail("Exception expected. Wrong reader name given.");
      } catch (NetInfCheckedSecurityException securityException) {
         System.out.println(securityException.getMessage());
      }
   }

   @Test
   public void testNoPrivateKey() throws NetInfCheckedSecurityException {
      Attribute attribute = createTestAttribute();
      Attribute encryptedAttribute = crypto.encrypt(attribute, publicKeys);

      IdentityManager wrongIdentityManager = EasyMock.createMock(IdentityManager.class);
      try {
         EasyMock.expect(wrongIdentityManager.getPrivateKey((String) EasyMock.anyObject())).andReturn(null).anyTimes();
         EasyMock
               .expect(
                     wrongIdentityManager.getPrivateKey((String) EasyMock.anyObject(), (String) EasyMock.anyObject(),
                           (String) EasyMock.anyObject())).andReturn(null).anyTimes();
      } catch (NetInfCheckedException e1) {
         // TODO Auto-generated catch block
         e1.printStackTrace();
      }
      EasyMock.expect(wrongIdentityManager.hasPrivateKey((String) EasyMock.anyObject())).andReturn(true).anyTimes();
      EasyMock
            .expect(
                  wrongIdentityManager.hasPrivateKey((String) EasyMock.anyObject(), (String) EasyMock.anyObject(),
                        (String) EasyMock.anyObject())).andReturn(true).anyTimes();
      EasyMock.replay(wrongIdentityManager);

      try {
         // FIXME added dummy-port! needs adjustment!
         CryptographyImpl crypto = new CryptographyImpl(wrongIdentityManager, algorithm, factory, convenienceCommunicator);

         crypto.decrypt(encryptedAttribute);
         Assert.fail("Exception expected. No private key given.");
      } catch (NetInfCheckedSecurityException securityException) {
         System.out.println(securityException.getMessage());
      } catch (Exception e) {
         Assert.fail("Wrong exception catched. " + e.getMessage());
         e.printStackTrace();
      }
   }

   @Test
   public void testBadPrivateKey() throws NetInfCheckedSecurityException {
      Attribute attribute = createTestAttribute();
      Attribute encryptedAttribute = crypto.encrypt(attribute, publicKeys);
      // String keyName = identityObject.getIdentifier().toString() + "?" + DefinedAttributeIdentification.PUBLIC_KEY.getURI();

      IdentityManager wrongIdentityManager = EasyMock.createMock(IdentityManager.class);
      EasyMock.expect(wrongIdentityManager.hasPrivateKey((String) EasyMock.anyObject())).andReturn(true).anyTimes();
      EasyMock
            .expect(
                  wrongIdentityManager.hasPrivateKey((String) EasyMock.anyObject(), (String) EasyMock.anyObject(),
                        (String) EasyMock.anyObject())).andReturn(true).anyTimes();
      try {
         KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
         keyPairGenerator.initialize(1024);
         KeyPair pair = keyPairGenerator.generateKeyPair();

         PrivateKey privateKey = pair.getPrivate();
         try {
            EasyMock.expect(wrongIdentityManager.getPrivateKey((String) EasyMock.anyObject())).andReturn(privateKey).anyTimes();
            EasyMock
                  .expect(
                        wrongIdentityManager.getPrivateKey((String) EasyMock.anyObject(), (String) EasyMock.anyObject(),
                              (String) EasyMock.anyObject())).andReturn(privateKey).anyTimes();
         } catch (NetInfCheckedException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
         }
      } catch (Exception e) {
         throw new NetInfUncheckedException("error creating keys");
      }
      EasyMock.replay(wrongIdentityManager);

      try {
         // FIXME added dummy-port! needs adjustment!
         CryptographyImpl crypto = new CryptographyImpl(wrongIdentityManager, algorithm, factory, convenienceCommunicator);

         crypto.decrypt(encryptedAttribute);
         Assert.fail("Exception expected. Wrong private key given.");
      } catch (NetInfCheckedSecurityException securityException) {
         System.out.println(securityException.getMessage());
      }
   }

   @Test
   public void testBadPrivateKeyAlgorithm() throws NetInfCheckedSecurityException {
      Attribute attribute = createTestAttribute();
      Attribute encryptedAttribute = crypto.encrypt(attribute, publicKeys);

      IdentityManager wrongIdentityManager = EasyMock.createMock(IdentityManager.class);
      EasyMock.expect(wrongIdentityManager.hasPrivateKey((String) EasyMock.anyObject())).andReturn(true).anyTimes();
      EasyMock
            .expect(
                  wrongIdentityManager.hasPrivateKey((String) EasyMock.anyObject(), (String) EasyMock.anyObject(),
                        (String) EasyMock.anyObject())).andReturn(true).anyTimes();
      try {
         KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("DSA");
         keyPairGenerator.initialize(1024);
         KeyPair pair = keyPairGenerator.generateKeyPair();

         PrivateKey privateKey = pair.getPrivate();
         try {
            EasyMock.expect(wrongIdentityManager.getPrivateKey((String) EasyMock.anyObject())).andReturn(privateKey).anyTimes();
            EasyMock
                  .expect(
                        wrongIdentityManager.getPrivateKey((String) EasyMock.anyObject(), (String) EasyMock.anyObject(),
                              (String) EasyMock.anyObject())).andReturn(privateKey).anyTimes();
         } catch (NetInfCheckedException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
         }
      } catch (Exception e) {
         throw new NetInfUncheckedException("error creating keys");
      }
      EasyMock.replay(wrongIdentityManager);

      try {
         // FIXME added dummy-port! needs adjustment!
         CryptographyImpl crypto = new CryptographyImpl(wrongIdentityManager, algorithm, factory, convenienceCommunicator);

         crypto.decrypt(encryptedAttribute);
         Assert.fail("Exception expected. Wrong private key given.");
      } catch (NetInfCheckedSecurityException securityException) {
         System.out.println(securityException.getMessage());
      }
   }

   @After
   public void tearDown() throws Exception {
   }

   private Attribute createTestAttribute() {
      String name = "Test-Attribute";
      String content = RandomStringUtils.random(500);

      Attribute attribute = factory.createAttribute(name, content);
      attribute.addSubattribute(createTestReaderList());

      return attribute;
   }

   private InformationObject createTestInformationObject() {
      String name = "Test-InformationObject";

      InformationObject io = factory.createInformationObject();
      Identifier id = factory.createIdentifier();
      IdentifierLabel label = factory.createIdentifierLabel();
      label.setLabelName(DefinedLabelName.UNIQUE_LABEL.getLabelName());
      label.setLabelValue(name);
      id.addIdentifierLabel(label);
      io.setIdentifier(id);

      io.addAttribute(createTestReaderList());
      io.addAttribute(createTestAttribute());

      return io;
   }

   private Attribute createTestReaderList() {
      Attribute readerList = factory.createAttribute();
      readerList.setIdentification(DefinedAttributeIdentification.AUTHORIZED_READERS.getURI());
      Attribute readerIO = factory.createAttribute();
      readerIO.setIdentification(DefinedAttributeIdentification.READER.getURI());
      readerIO.setValue(identityObject.getIdentifier().toString());
      readerList.addSubattribute(readerIO);

      return readerList;
   }

}
