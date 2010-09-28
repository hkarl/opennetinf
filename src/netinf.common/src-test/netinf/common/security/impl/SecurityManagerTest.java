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
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.util.Properties;

import netinf.common.communication.NetInfNodeConnection;
import netinf.common.communication.RemoteNodeConnection;
import netinf.common.communication.SerializeFormat;
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
import netinf.common.log.module.LogModule;
import netinf.common.security.Cryptography;
import netinf.common.security.IdentityVerification;
import netinf.common.security.Integrity;
import netinf.common.security.SecurityManager;
import netinf.common.security.identity.IdentityManager;
import netinf.common.security.identity.impl.IdentityManagerImpl;
import netinf.common.security.impl.module.SecurityModule;
import netinf.common.utils.Utils;

import org.apache.commons.lang.RandomStringUtils;
import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.name.Names;

/**
 * The Class SecurityManagerTest.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class SecurityManagerTest {

   public static final String NETINFNODE_PROPERTIES = "../configs_official/netinfnode_testing.properties";

   private static IdentityManager identityManager;
   private static RemoteNodeConnection convenienceCommunicator;
   private static Cryptography cryptography;
   private static Integrity integrity;
   private static IdentityVerification identityVerification;
   private static SecurityManager securityManager;

   private static String defaultContentAlgorithmName = "DES/ECB/PKCS5Padding";
   private static String defaultKeyAlgorithmName = "RSA/ECB/PKCS1Padding";

   private static IdentityObject identity;

   private static Injector injector;
   private static DatamodelFactory factory;

   @BeforeClass
   public static void classSetUp() {
      final Properties properties = Utils.loadProperties(NETINFNODE_PROPERTIES);
      injector = Guice.createInjector(new LogModule(properties), new DatamodelImplModule(), new SecurityModule(),
            new AbstractModule() {

               @Override
               protected void configure() {
                  Names.bindProperties(binder(), properties);
                  bind(NetInfNodeConnection.class).annotatedWith(SecurityModule.Security.class).to(RemoteNodeConnection.class)
                        .in(Singleton.class);
               }
            });
      factory = injector.getInstance(DatamodelFactory.class);

      identityManager = new IdentityManagerImpl(factory, new SignatureAlgorithmImpl() {

         @Override
         public boolean verifySignature(String originalString, String signature, PublicKey pk, String hashAndSignatureFunction)
               throws SignatureException, NoSuchAlgorithmException, InvalidKeyException {
            // TODO Auto-generated method stub
            return false;
         }

         @Override
         public String sign(String originalString, PrivateKey sk, String hashAndSignatureFunction)
               throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
            // TODO Auto-generated method stub
            return null;
         }

         @Override
         public String hash(String originalString, String hashFunction) throws NoSuchAlgorithmException {
            // TODO Auto-generated method stub
            return null;
         }
      }, new CryptoAlgorithmImpl());

      // identityManager = new IdentityManagerImpl(factory, new SignatureAlgorithmImpl(), new CryptoAlgorithmImpl());

      try {
         identity = identityManager.createNewMasterIdentity();
      } catch (NetInfCheckedException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }

      convenienceCommunicator = EasyMock.createMock(RemoteNodeConnection.class);
      try {
         EasyMock.expect(convenienceCommunicator.getIO((Identifier) EasyMock.anyObject())).andReturn(identity).anyTimes();
         convenienceCommunicator.setHostAndPort("localhost", 5000);
         EasyMock.expectLastCall().anyTimes();
         convenienceCommunicator.setSerializeFormat(SerializeFormat.JAVA);
         EasyMock.expectLastCall().anyTimes();
      } catch (NetInfCheckedException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      EasyMock.replay(convenienceCommunicator);

      cryptography = new CryptographyImpl(identityManager, new CryptoAlgorithmImpl(), factory, convenienceCommunicator);

      integrity = new IntegrityImpl(factory, new SignatureAlgorithmImpl(), convenienceCommunicator, identityManager);

      identityVerification = new IdentityVerificationImpl(factory, convenienceCommunicator, identityManager,
            new SignatureAlgorithmImpl());

      securityManager = new SecurityManagerImpl(factory, cryptography, integrity, identityVerification);
   }

   @Before
   public void setUp() throws Exception {
   }

   @Test
   public void testIncommingIoTrustedReceiver() throws NetInfCheckedException, NoSuchAlgorithmException {
      InformationObject informationObject = createTestInformationObject();
      Assert.assertNotSame(informationObject, securityManager.checkIncommingInformationObject(informationObject, true));
   }

   @Test
   public void testIncommingIoUntrustedReceiver() throws NetInfCheckedException, NoSuchAlgorithmException {
      InformationObject informationObject = createTestInformationObject();
      Assert.assertEquals(informationObject, securityManager.checkIncommingInformationObject(informationObject, false));
   }

   @Test
   public void testOutgoingIoTrustedSender() throws NetInfCheckedException, NoSuchAlgorithmException {
      InformationObject informationObject = createTestInformationObject();
      Assert.assertNotSame(informationObject, securityManager.checkOutgoingInformationObject(informationObject, true));
   }

   @Test
   public void testOutgoingIoUntrustedSender() throws NetInfCheckedException, NoSuchAlgorithmException {
      InformationObject informationObject = createTestInformationObject();
      Assert.assertEquals(informationObject, securityManager.checkOutgoingInformationObject(informationObject, false));
   }

   @Test
   public void testTrustedSenderTrustedReceiver() throws NetInfCheckedException, NoSuchAlgorithmException {
      InformationObject informationObject = createTestInformationObject();
      InformationObject outgoing = securityManager.checkOutgoingInformationObject(informationObject, true);
      Assert.assertNotSame(informationObject, outgoing);
      InformationObject incomming = securityManager.checkIncommingInformationObject(outgoing, true);
      Assert.assertNotSame(informationObject, incomming);
      // InformationObject outgoing2 = securityManager.checkOutgoingInformationObject(incomming, true);
      // Assert.assertNotSame(outgoing, outgoing2);
      // InformationObject incomming2 = securityManager.checkIncommingInformationObject(outgoing2, true);
      // Assert.assertNotSame(incomming, incomming2);
   }

   @After
   public void tearDown() throws Exception {
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

      Attribute attribute;
      attribute = createTestOwner();
      io.addAttribute(attribute);
      attribute = createTestWriter();
      io.addAttribute(attribute);
      attribute = createTestReaderList();
      io.addAttribute(attribute);
      attribute = createTestAttribute();
      attribute.addSubattribute(createTestWriter());
      attribute.addSubattribute(createTestReaderList());
      io.addAttribute(attribute);
      attribute = createTestAttribute();
      attribute.addSubattribute(createTestWriter());
      io.addAttribute(attribute);
      attribute = createTestAttribute();
      attribute.addSubattribute(createTestReaderList());
      io.addAttribute(attribute);
      attribute = createTestAttribute();
      io.addAttribute(attribute);

      return io;
   }

   private Attribute createTestAttribute() {
      String name = "Test-Attribute";
      String content = RandomStringUtils.random(500);

      Attribute attribute = factory.createAttribute(name, content);
      attribute.addSubattribute(createTestReaderList());

      return attribute;
   }

   private Attribute createTestPublicKey() {
      Attribute signatureCommand = factory.createAttribute();
      signatureCommand.setIdentification(DefinedAttributeIdentification.PUBLIC_KEY.getURI());
      signatureCommand.setValue(identity.getIdentifier().toString() + "?" + DefinedAttributeIdentification.PUBLIC_KEY.getURI());

      return signatureCommand;
   }

   private Attribute createTestOwner() {
      Attribute signatureCommand = factory.createAttribute();
      signatureCommand.setIdentification(DefinedAttributeIdentification.OWNER.getURI());
      signatureCommand.setValue(identity.getIdentifier().toString() + "?" + DefinedAttributeIdentification.PUBLIC_KEY.getURI());

      return signatureCommand;
   }

   private Attribute createTestWriter() {
      Attribute signatureCommand = factory.createAttribute();
      signatureCommand.setIdentification(DefinedAttributeIdentification.WRITER.getURI());
      signatureCommand.setValue(identity.getIdentifier().toString() + "?" + DefinedAttributeIdentification.PUBLIC_KEY.getURI());

      return signatureCommand;
   }

   private Attribute createTestReaderList() {

      Attribute readerList = factory.createAttribute();
      readerList.setIdentification(DefinedAttributeIdentification.AUTHORIZED_READERS.getURI());
      readerList.setValue("");
      Attribute readerIO = factory.createAttribute();
      readerIO.setIdentification(DefinedAttributeIdentification.READER.getURI());
      readerIO.setValue(identity.getIdentifier().toString() + "?" + DefinedAttributeIdentification.PUBLIC_KEY.getURI());
      // readerList.setValue(identity.getIdentifier().toString() + "?" + DefinedAttributeIdentification.PUBLIC_KEY.getURI());
      readerList.addSubattribute(readerIO);
      return readerList;
   }
}
