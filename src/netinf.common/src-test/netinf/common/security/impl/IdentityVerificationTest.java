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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.security.NoSuchAlgorithmException;
import java.util.Properties;

import netinf.common.communication.NetInfNodeConnection;
import netinf.common.communication.RemoteNodeConnection;
import netinf.common.communication.SerializeFormat;
import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.DefinedVersionKind;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.attribute.Attribute;
import netinf.common.datamodel.attribute.DefinedAttributeIdentification;
import netinf.common.datamodel.creator.ValidCreator;
import netinf.common.datamodel.identity.IdentityObject;
import netinf.common.datamodel.impl.module.DatamodelImplModule;
import netinf.common.exceptions.NetInfCheckedException;
import netinf.common.log.module.LogModule;
import netinf.common.security.IdentityVerification;
import netinf.common.security.IdentityVerificationResult;
import netinf.common.security.Integrity;
import netinf.common.security.IntegrityResult;
import netinf.common.security.SignatureAlgorithm;
import netinf.common.security.identity.IdentityManager;
import netinf.common.security.impl.module.SecurityModule;
import netinf.common.utils.Utils;

import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.name.Names;

/**
 * The Class IdentityVerificationTest.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class IdentityVerificationTest {

   public static final String NETINFNODE_PROPERTIES = "../configs_official/netinfnode_testing.properties";

   private InformationObject io;

   private static IdentityObject identityObject;

   private static Injector injector;
   private static DatamodelFactory dmFactory;
   private static SignatureAlgorithm signatureAlgorithm;
   private static IdentityManager identityManager;

   private RemoteNodeConnection convenienceCommunicator;

   @BeforeClass
   public static void setup2() throws NetInfCheckedException {
      final Properties properties = Utils.loadProperties(NETINFNODE_PROPERTIES);
      injector = Guice.createInjector(new LogModule(properties), new DatamodelImplModule(), new SecurityModule(),
            new AbstractModule() {

               @Override
               protected void configure() {
                  bind(NetInfNodeConnection.class).annotatedWith(SecurityModule.Security.class).to(RemoteNodeConnection.class)
                        .in(Singleton.class);
                  Names.bindProperties(binder(), properties);
               }
            });
      dmFactory = injector.getInstance(DatamodelFactory.class);

      identityManager = injector.getInstance(IdentityManager.class);
      identityObject = identityManager.createNewMasterIdentity();

      System.out.println(identityObject);

   }

   @Before
   public void setUp() throws Exception {
      ValidCreator.setDatamodelFactory(dmFactory);
      ValidCreator.setSignatureAlgorithm(injector.getInstance(SignatureAlgorithm.class));
      this.io = ValidCreator.createValidInformationObject(identityObject, DefinedVersionKind.UNVERSIONED, "someValue");

      this.convenienceCommunicator = EasyMock.createMock(RemoteNodeConnection.class);
      convenienceCommunicator.setHostAndPort("localhost", 5000);
      EasyMock.expectLastCall().anyTimes();
      convenienceCommunicator.setSerializeFormat(SerializeFormat.JAVA);
      EasyMock.expectLastCall().anyTimes();
      EasyMock.expect(this.convenienceCommunicator.getIO((Identifier) EasyMock.anyObject())).andReturn(identityObject).anyTimes();
      EasyMock.replay(this.convenienceCommunicator);

   }

   @Test
   public void testIOVerifiedByOwner() throws NetInfCheckedException {
      IdentityVerification idVerify = new IdentityVerificationImpl(dmFactory, this.convenienceCommunicator, identityManager,
            signatureAlgorithm);
      assertTrue(idVerify.isIOVerifiedByOwner(this.io) == IdentityVerificationResult.IDENTITY_VERIFICATION_SUCCEEDED ? true
            : false);
   }

   @Test
   public void testIOVerifiedByOwnerWithAttack() throws NetInfCheckedException {
      IdentityVerification idVerify = new IdentityVerificationImpl(dmFactory, this.convenienceCommunicator, identityManager,
            signatureAlgorithm);

      IdentityObject newIdO = identityManager.createNewMasterIdentity();

      this.io.getSingleAttribute(DefinedAttributeIdentification.OWNER.getURI())
            .setValue(
                  newIdO.getIdentifier().toString() + IntegrityImpl.PATH_SEPERATOR
                        + DefinedAttributeIdentification.PUBLIC_KEY.getURI());

      assertTrue(idVerify.isIOVerifiedByOwner(this.io) == IdentityVerificationResult.IDENTITY_VERIFICATION_FAILED ? true : false);
   }

   @Test
   public void testIOVerifiedByOwnerForIdentityObject() throws NetInfCheckedException {
      IdentityVerification idVerify = new IdentityVerificationImpl(dmFactory, this.convenienceCommunicator, identityManager,
            signatureAlgorithm);
      assertTrue(idVerify.isIOVerifiedByOwner(identityObject) == IdentityVerificationResult.IDENTITY_VERIFICATION_SUCCEEDED ? true
            : false);
   }

   @Test
   public void testOwnerIsVerified() throws NetInfCheckedException, NoSuchAlgorithmException {
      IdentityVerification idVerify = new IdentityVerificationImpl(dmFactory, this.convenienceCommunicator, identityManager,
            new SignatureAlgorithmImpl());
      assertTrue(idVerify.isOwnerVerified(this.io) == IdentityVerificationResult.IDENTITY_VERIFICATION_SUCCEEDED ? true : false);
   }

   @Test
   public void testOwnerIsVerifiedForIdentityObject() throws NetInfCheckedException, NoSuchAlgorithmException {
      IdentityVerification idVerify = new IdentityVerificationImpl(dmFactory, this.convenienceCommunicator, identityManager,
            new SignatureAlgorithmImpl());
      assertTrue(idVerify.isOwnerVerified(identityObject) == IdentityVerificationResult.IDENTITY_VERIFICATION_SUCCEEDED ? true
            : false);
   }

   @Test
   public void testWriterIsVerified() throws NetInfCheckedException, NoSuchAlgorithmException {
      IdentityVerification idVerify = new IdentityVerificationImpl(dmFactory, this.convenienceCommunicator, identityManager,
            signatureAlgorithm);
      Attribute writerAttribute = dmFactory.createAttribute(DefinedAttributeIdentification.WRITER.getURI(), identityObject
            .getIdentifier().toString()
            + IntegrityImpl.PATH_SEPERATOR + DefinedAttributeIdentification.PUBLIC_KEY.getURI());
      this.io.addAttribute(writerAttribute);
      Attribute writersList = dmFactory.createAttribute(DefinedAttributeIdentification.AUTHORIZED_WRITERS.getURI(),
            identityObject.getIdentifier().toString() + IntegrityImpl.PATH_SEPERATOR
                  + DefinedAttributeIdentification.PUBLIC_KEY.getURI());
      this.io.addAttribute(writersList);

      assertTrue(idVerify.isWriterVerified(this.io) == IdentityVerificationResult.IDENTITY_VERIFICATION_SUCCEEDED);
   }

   @Test
   public void testOtherWriterThanOwnerIsVerified() throws NetInfCheckedException, NoSuchAlgorithmException {
      IdentityManager idManagerWriter = injector.getInstance(IdentityManager.class);
      IdentityObject idoWriter = idManagerWriter.createNewMasterIdentity();
      System.out.println(idoWriter);

      Integrity integrity = new IntegrityImpl(dmFactory, new SignatureAlgorithmImpl(), this.convenienceCommunicator,
            identityManager);
      IdentityVerification idVerify = new IdentityVerificationImpl(dmFactory, this.convenienceCommunicator, identityManager,
            signatureAlgorithm);

      Attribute writerAttribute = dmFactory.createAttribute(DefinedAttributeIdentification.WRITER.getURI(), idoWriter
            .getIdentifier().toString()
            + IntegrityImpl.PATH_SEPERATOR + DefinedAttributeIdentification.PUBLIC_KEY.getURI());
      this.io.addAttribute(writerAttribute);

      Attribute writersList = dmFactory.createAttribute(DefinedAttributeIdentification.AUTHORIZED_WRITERS.getURI(), idoWriter
            .getIdentifier().toString()
            + IntegrityImpl.PATH_SEPERATOR + DefinedAttributeIdentification.PUBLIC_KEY.getURI());
      Attribute writerOfList = dmFactory.createAttribute(DefinedAttributeIdentification.WRITER.getURI(), identityObject
            .getIdentifier().toString()
            + IntegrityImpl.PATH_SEPERATOR + DefinedAttributeIdentification.PUBLIC_KEY.getURI());
      writersList.addSubattribute(writerOfList);
      this.io.addAttribute(writersList);

      assertEquals(IntegrityResult.SIGNING_SUCCEEDED, integrity.sign(writersList));
      assertTrue(idVerify.isWriterVerified(this.io) == IdentityVerificationResult.IDENTITY_VERIFICATION_SUCCEEDED);
   }

   @Test
   public void testWriterIsVerifiedforAttribute() throws NetInfCheckedException, NoSuchAlgorithmException {
      IdentityVerification idVerify = new IdentityVerificationImpl(dmFactory, this.convenienceCommunicator, identityManager,
            signatureAlgorithm);

      Attribute writerAttribute = dmFactory.createAttribute(DefinedAttributeIdentification.WRITER.getURI(), identityObject
            .getIdentifier().toString()
            + IntegrityImpl.PATH_SEPERATOR + DefinedAttributeIdentification.PUBLIC_KEY.getURI());
      Attribute upb = dmFactory.createAttribute("Name", "University of Paderborn");
      upb.addSubattribute(writerAttribute);
      this.io.addAttribute(upb);

      Attribute writersList = dmFactory.createAttribute(DefinedAttributeIdentification.AUTHORIZED_WRITERS.getURI(),
            identityObject.getIdentifier().toString() + IntegrityImpl.PATH_SEPERATOR
                  + DefinedAttributeIdentification.PUBLIC_KEY.getURI());
      this.io.addAttribute(writersList);

      assertTrue(idVerify.isWriterVerified(upb) == IdentityVerificationResult.IDENTITY_VERIFICATION_SUCCEEDED);
   }

   @After
   public void tearDown() throws Exception {
   }

}
