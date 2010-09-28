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

import static netinf.common.datamodel.attribute.DefinedAttributeIdentification.SECURED_IN_OVERALL;
import static netinf.common.datamodel.attribute.DefinedAttributeIdentification.SIGNATURE;
import static netinf.common.datamodel.attribute.DefinedAttributeIdentification.SIGNATURE_IDENTIFICATION;
import static netinf.common.datamodel.attribute.DefinedAttributeIdentification.WRITER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
import netinf.common.security.Integrity;
import netinf.common.security.IntegrityResult;
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
 * The Class IntegrityTest.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class IntegrityTest {

   public static final String NETINFNODE_PROPERTIES = "../configs/netinfnode_testing.properties";

   private InformationObject io;
   private Attribute singleSignedAttribute;

   private static IdentityObject identityObject;

   private static Injector injector;
   private static DatamodelFactory dmFactory;

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
   }

   @Before
   public void setUp() throws Exception {
      this.io = dmFactory.createInformationObject();

      Identifier id = dmFactory.createIdentifier();

      IdentifierLabel identifierLabel = dmFactory.createIdentifierLabel();
      identifierLabel.setLabelName(DefinedLabelName.UNIQUE_LABEL.getLabelName());
      identifierLabel.setLabelValue("aValue");
      id.addIdentifierLabel(identifierLabel);

      this.io.setIdentifier(id);

      Attribute upb = dmFactory.createAttribute("Name", "University of Paderborn");
      upb.addSubattribute(dmFactory.createAttribute(SECURED_IN_OVERALL.getURI(), "true"));

      this.io.addAttribute(upb);

      this.io.addAttribute(dmFactory.createAttribute("z last", "on topmost level"));

      this.io.addAttribute(dmFactory.createAttribute("a first", "on topmost level"));

      upb.addSubattribute(dmFactory.createAttribute("below upb", "second level"));

      Attribute prop = dmFactory.createAttribute("unsigned Identification", "Unsigned Value");
      this.io.addAttribute(prop);

      this.singleSignedAttribute = dmFactory.createAttribute("Single Signed Identification", "Single Signed Value");
      this.io.addAttribute(this.singleSignedAttribute);

      this.convenienceCommunicator = EasyMock.createMock(RemoteNodeConnection.class);
      convenienceCommunicator.setHostAndPort("localhost", 5000);
      EasyMock.expectLastCall().anyTimes();
      convenienceCommunicator.setSerializeFormat(SerializeFormat.JAVA);
      EasyMock.expectLastCall().anyTimes();
      EasyMock.expect(this.convenienceCommunicator.getIO((Identifier) EasyMock.anyObject())).andReturn(identityObject).anyTimes();
      EasyMock.replay(this.convenienceCommunicator);

   }

   @Test
   public void testCreateSignatureProperty() {
      Integrity integrity = new IntegrityImpl(dmFactory, new SignatureAlgorithmImpl(), this.convenienceCommunicator,
            identityManager);

      Attribute writerAttribute = dmFactory.createAttribute(DefinedAttributeIdentification.WRITER.getURI(), identityObject
            .getIdentifier().toString()
            + IntegrityImpl.PATH_SEPERATOR + DefinedAttributeIdentification.PUBLIC_KEY.getURI());
      this.io.addAttribute(writerAttribute);
      assertEquals(IntegrityResult.SIGNING_SUCCEEDED, integrity.sign(this.io));

      assertNotNull(this.io.getSingleAttribute(SIGNATURE.getURI()));
      assertNotNull(this.io.getSingleAttribute(SIGNATURE.getURI()).getValue(String.class));
      assertNotNull(this.io.getSingleAttribute(SIGNATURE.getURI()).getSingleSubattribute(SIGNATURE_IDENTIFICATION.getURI()));
      assertNotNull(this.io.getSingleAttribute(SIGNATURE.getURI()).getSingleSubattribute(SIGNATURE_IDENTIFICATION.getURI())
            .getValue(String.class));
      assertNotNull(this.io.getSingleAttribute(WRITER.getURI()));
      assertNotNull(this.io.getSingleAttribute(WRITER.getURI()).getValue(String.class));
   }

   @Test
   public void testSignatureCheck() throws NetInfCheckedException {
      Integrity integrity = new IntegrityImpl(dmFactory, new SignatureAlgorithmImpl(), this.convenienceCommunicator,
            identityManager);

      Attribute writerAttribute = dmFactory.createAttribute(DefinedAttributeIdentification.WRITER.getURI(), identityObject
            .getIdentifier().toString()
            + IntegrityImpl.PATH_SEPERATOR + DefinedAttributeIdentification.PUBLIC_KEY.getURI());
      this.io.addAttribute(writerAttribute);
      assertEquals(IntegrityResult.SIGNING_SUCCEEDED, integrity.sign(this.io));

      assertTrue((integrity.isSignatureValid(this.io) == IntegrityResult.INTEGRITY_CHECK_SUCCEEDED ? true : false));
   }

   @Test
   public void testInvalidSignatureCheck() throws NetInfCheckedException {
      Integrity integrity = new IntegrityImpl(dmFactory, new SignatureAlgorithmImpl(), this.convenienceCommunicator,
            identityManager);

      Attribute writerAttribute = dmFactory.createAttribute(DefinedAttributeIdentification.WRITER.getURI(), identityObject
            .getIdentifier().toString()
            + IntegrityImpl.PATH_SEPERATOR + DefinedAttributeIdentification.PUBLIC_KEY.getURI());
      this.io.addAttribute(writerAttribute);

      assertEquals(IntegrityResult.SIGNING_SUCCEEDED, integrity.sign(this.io));

      this.io.getSingleAttribute("Name").setValue("veraendert");

      assertTrue((integrity.isSignatureValid(this.io) == IntegrityResult.INTEGRITY_CHECK_FAIL ? true : false));
   }

   @Test
   public void testValidSignatureChange() throws NetInfCheckedException {
      Integrity integrity = new IntegrityImpl(dmFactory, new SignatureAlgorithmImpl(), this.convenienceCommunicator,
            identityManager);

      Attribute writerAttribute = dmFactory.createAttribute(DefinedAttributeIdentification.WRITER.getURI(), identityObject
            .getIdentifier().toString()
            + IntegrityImpl.PATH_SEPERATOR + DefinedAttributeIdentification.PUBLIC_KEY.getURI());
      this.io.addAttribute(writerAttribute);

      assertEquals(IntegrityResult.SIGNING_SUCCEEDED, integrity.sign(this.io));

      this.io.getSingleAttribute("unsigned Identification").setValue("change on unsigned property value");

      assertTrue((integrity.isSignatureValid(this.io) == IntegrityResult.INTEGRITY_CHECK_SUCCEEDED ? true : false));
   }

   @Test
   public void testPropertySignatureCheck() {
      Integrity integrity = new IntegrityImpl(dmFactory, new SignatureAlgorithmImpl(), this.convenienceCommunicator,
            identityManager);

      Attribute writerAttribute = dmFactory.createAttribute(DefinedAttributeIdentification.WRITER.getURI(), identityObject
            .getIdentifier().toString()
            + IntegrityImpl.PATH_SEPERATOR + DefinedAttributeIdentification.PUBLIC_KEY.getURI());
      this.singleSignedAttribute.addSubattribute(writerAttribute);

      assertEquals(IntegrityResult.SIGNING_SUCCEEDED, integrity.sign(this.singleSignedAttribute));

      assertNotNull(this.singleSignedAttribute.getSingleSubattribute(SIGNATURE.getURI()));
      assertNotNull(this.singleSignedAttribute.getSingleSubattribute(SIGNATURE.getURI()).getValue(String.class));
      assertNotNull(this.singleSignedAttribute.getSingleSubattribute(WRITER.getURI()));
      assertNotNull(this.singleSignedAttribute.getSingleSubattribute(WRITER.getURI()).getValue(String.class));
   }

   @Test
   public void testPropertySignatureValid() throws NetInfCheckedException {
      Integrity integrity = new IntegrityImpl(dmFactory, new SignatureAlgorithmImpl(), this.convenienceCommunicator,
            identityManager);

      Attribute writerAttribute = dmFactory.createAttribute(DefinedAttributeIdentification.WRITER.getURI(), identityObject
            .getIdentifier().toString()
            + IntegrityImpl.PATH_SEPERATOR + DefinedAttributeIdentification.PUBLIC_KEY.getURI());
      this.singleSignedAttribute.addSubattribute(writerAttribute);

      assertEquals(IntegrityResult.SIGNING_SUCCEEDED, integrity.sign(this.singleSignedAttribute));

      assertTrue((integrity.isSignatureValid(this.singleSignedAttribute) == IntegrityResult.INTEGRITY_CHECK_SUCCEEDED ? true
            : false));
   }

   @Test
   public void testPropertySignatureInvalid() throws NetInfCheckedException {
      Integrity integrity = new IntegrityImpl(dmFactory, new SignatureAlgorithmImpl(), this.convenienceCommunicator,
            identityManager);

      Attribute writerAttribute = dmFactory.createAttribute(DefinedAttributeIdentification.WRITER.getURI(), identityObject
            .getIdentifier().toString()
            + IntegrityImpl.PATH_SEPERATOR + DefinedAttributeIdentification.PUBLIC_KEY.getURI());
      this.singleSignedAttribute.addSubattribute(writerAttribute);

      assertEquals(IntegrityResult.SIGNING_SUCCEEDED, integrity.sign(this.singleSignedAttribute));

      this.singleSignedAttribute.setValue("change on singleSigned property value");

      assertTrue((integrity.isSignatureValid(this.singleSignedAttribute) == IntegrityResult.INTEGRITY_CHECK_FAIL ? true : false));
   }

   @After
   public void tearDown() throws Exception {
   }

}
