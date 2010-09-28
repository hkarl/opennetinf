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
package netinf.eventservice.siena;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;
import netinf.common.communication.SerializeFormat;
import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.attribute.Attribute;
import netinf.common.datamodel.attribute.DefinedAttributeIdentification;
import netinf.common.messages.ESFEventMessage;
import netinf.common.utils.Utils;
import netinf.eventservice.framework.PublisherNetInf;
import netinf.eventservice.framework.SubscriberNetInf;
import netinf.eventservice.siena.module.EventServiceSienaTranslationTestModule;
import netinf.eventservice.siena.translation.TranslationSiena;

import org.junit.BeforeClass;
import org.junit.Test;

import siena.AttributeValue;
import siena.Filter;
import siena.HierarchicalDispatcher;
import siena.Notifiable;
import siena.Notification;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Tests the translation of ESFEventMessage instances
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class EventTranslationTest {

   private static final String HASH_CONSTANT = "This is the hash value";
   private static final String IDENTIFIER_STRING = "ni:HASH_OF_PK=123~HASH_OF_PK_IDENT=SHA1"
      + "~VERSION_KIND=UNVERSIONED~UNIQUE_LABEL=somethingUnique";
   private static final String TESTING_PROPERTIES = "../configs/eventservicesiena_testing.properties";
   private static Injector injector;
   private static EventServiceSiena eventServiceSiena;
   private static DatamodelFactory datamodelFactory;
   private static PublisherNetInf<HierarchicalDispatcher, Notifiable, Notification, Filter> publisherNetInf;

   private static String personName = "Ede Bauer";
   private static String eMail = "edebauer@mail.de";
   private static String description = "This is my mail addresss";

   @BeforeClass
   public static void setUpTest() throws SQLException {
      Properties properties = Utils.loadProperties(TESTING_PROPERTIES);
      injector = Guice.createInjector(new EventServiceSienaTranslationTestModule(properties));
      eventServiceSiena = injector.getInstance(EventServiceSiena.class);
      datamodelFactory = injector.getInstance(DatamodelFactory.class);

      publisherNetInf = eventServiceSiena.createPublisherNetInf();
   }

   @Test
   public void testSerializationBackAndForth() {
      // Creation of testdata
      InformationObject oldInformationObject = datamodelFactory.createInformationObject();
      InformationObject newInformationObject = datamodelFactory.createInformationObject();

      Identifier identifier = datamodelFactory.createIdentifierFromString(IDENTIFIER_STRING);

      Identifier identifierOld = datamodelFactory.createIdentifierFromString(IDENTIFIER_STRING);
      oldInformationObject.setIdentifier(identifierOld);
      Identifier identifierNew = datamodelFactory.createIdentifierFromString(IDENTIFIER_STRING);
      newInformationObject.setIdentifier(identifierNew);

      Notification notification = translate(oldInformationObject, newInformationObject);

      // Check the serialization of old information object
      boolean equals = Arrays.equals(notification.getAttribute(TranslationSiena.PREFIX_OLD).byteArrayValue(),
            oldInformationObject.serializeToBytes());
      Assert.assertTrue(equals);

      equals = Arrays.equals(notification.getAttribute(TranslationSiena.PREFIX_NEW).byteArrayValue(), newInformationObject
            .serializeToBytes());
      Assert.assertTrue(equals);

      // Try the other way around, deserialize the according notification
      SubscriberNetInf<HierarchicalDispatcher, Notifiable, Notification, Filter> subscriberNetInf = eventServiceSiena
      .createSubscriberNetInf(identifier, (Identifier) identifier.clone());

      List<ESFEventMessage> eventList = subscriberNetInf.translateEvent(notification);
      ESFEventMessage eventMessage2 = eventList.get(0);

      InformationObject newInformationObject2 = eventMessage2.getNewInformationObject();
      InformationObject oldInformationObject2 = eventMessage2.getOldInformationObject();

      Assert.assertEquals(newInformationObject, newInformationObject2);
      Assert.assertEquals(oldInformationObject, oldInformationObject2);
   }

   @Test
   public void testSubproperties() {
      InformationObject informationObject = datamodelFactory.createInformationObject();

      Identifier identifier = datamodelFactory.createIdentifierFromString(IDENTIFIER_STRING);

      // IdentifierProperty
      Attribute identifierAttribute = datamodelFactory.createAttribute(DefinedAttributeIdentification.IDENTIFIER.getURI(),
            identifier.toString());

      // Subproperty
      Attribute subProperty = datamodelFactory.createAttribute(DefinedAttributeIdentification.HASH.getURI(), HASH_CONSTANT);

      // Add everything to InformationObject
      informationObject.setIdentifier(identifier);
      informationObject.addAttribute(identifierAttribute);
      identifierAttribute.addSubattribute(subProperty);

      Notification notification = translate(null, informationObject);

      // First check the Identifier property
      AttributeValue identifierAttributeSiena = notification.getAttribute(createName(TranslationSiena.PREFIX_NEW, false,
            DefinedAttributeIdentification.IDENTIFIER));

      Assert.assertNotNull(identifierAttributeSiena);
      String stringValue = identifierAttributeSiena.stringValue();
      Assert.assertEquals(identifier.toString(), stringValue);

      // Second check the Subattribute with full uri
      AttributeValue subAttributeFull = notification.getAttribute(createName(TranslationSiena.PREFIX_NEW, false,
            DefinedAttributeIdentification.IDENTIFIER, DefinedAttributeIdentification.HASH));

      Assert.assertNotNull(subAttributeFull);
      stringValue = subAttributeFull.stringValue();
      Assert.assertEquals(HASH_CONSTANT, stringValue);

      // Finally check the subattribute with relative uri
      AttributeValue subAttributeEmpty = notification.getAttribute(TranslationSiena.PREFIX_NEW + TranslationSiena.SEPARATOR
            + TranslationSiena.WILDCARD + TranslationSiena.SEPARATOR + DefinedAttributeIdentification.HASH.getURI());

      Assert.assertNotNull(subAttributeEmpty);
      stringValue = subAttributeEmpty.stringValue();
      Assert.assertEquals(HASH_CONSTANT, stringValue);
   }

   @Test
   public void testDoubleIdentification() {
      InformationObject informationObject = datamodelFactory.createInformationObject();

      Identifier identifier = datamodelFactory.createIdentifierFromString(IDENTIFIER_STRING);

      // Identifierattribute
      Attribute identifierAttribute = datamodelFactory.createAttribute(DefinedAttributeIdentification.IDENTIFIER.getURI(),
            identifier.toString());

      informationObject.setIdentifier(identifier);
      informationObject.addAttribute(identifierAttribute);
      informationObject.addAttribute(identifierAttribute);

      Notification notification = translate(informationObject, null);

      AttributeValue attributeValue = notification.getAttribute(TranslationSiena.PREFIX_OLD + TranslationSiena.SEPARATOR
            + DefinedAttributeIdentification.IDENTIFIER.getURI());

      Assert.assertNotNull(attributeValue);
      String stringValue = attributeValue.stringValue();
      Assert.assertEquals(identifier.toString(), stringValue);
   }

   @Test
   public void testDifferentTypes() {
      boolean exampleBoolean = true;
      int exampleInt = 100;
      double exampleDouble = 105.0d;
      long exampleLong = 110L;
      float exampleFloat = 110f;

      InformationObject informationObject = datamodelFactory.createInformationObject();

      Identifier identifier = datamodelFactory.createIdentifierFromString(IDENTIFIER_STRING);

      // Identifierattribute
      informationObject.setIdentifier(identifier);

      // Int attribute
      informationObject.addAttribute(datamodelFactory.createAttribute("int", exampleInt));

      // Double attribute
      informationObject.addAttribute(datamodelFactory.createAttribute("double", exampleDouble));

      // Float attribute
      informationObject.addAttribute(datamodelFactory.createAttribute("long", exampleLong));

      // Boolean attribute
      informationObject.addAttribute(datamodelFactory.createAttribute("boolean", exampleBoolean));

      // Float attribute, this has to create a warning.
      informationObject.addAttribute(datamodelFactory.createAttribute("something", exampleFloat));

      Notification notification = translate(informationObject, null);

      AttributeValue intAttribute = notification.getAttribute(TranslationSiena.PREFIX_OLD + TranslationSiena.SEPARATOR + "int");

      AttributeValue doubleAttribute = notification.getAttribute(TranslationSiena.PREFIX_OLD + TranslationSiena.SEPARATOR
            + "double");

      AttributeValue longAttribute = notification.getAttribute(TranslationSiena.PREFIX_OLD + TranslationSiena.SEPARATOR + "long");

      AttributeValue booleanAttribute = notification.getAttribute(TranslationSiena.PREFIX_OLD + TranslationSiena.SEPARATOR
            + "boolean");

      Assert.assertEquals(intAttribute.intValue(), exampleInt);
      Assert.assertEquals(doubleAttribute.doubleValue(), exampleDouble);
      Assert.assertEquals(longAttribute.longValue(), exampleLong);
      Assert.assertEquals(booleanAttribute.booleanValue(), exampleBoolean);
   }

   @Test
   public void testDiffCreatedInformationObject() {
      InformationObject newInformationObject = datamodelFactory.createInformationObject();

      Identifier identifierNew = datamodelFactory.createIdentifierFromString(IDENTIFIER_STRING);
      newInformationObject.setIdentifier(identifierNew);
      Attribute attribute = datamodelFactory.createAttribute(DefinedAttributeIdentification.PERSON_NAME.getURI(), personName);
      Attribute subAttribute = datamodelFactory.createAttribute(DefinedAttributeIdentification.E_MAIL_ADDRESS.getURI(), eMail);
      attribute.addSubattribute(subAttribute);
      newInformationObject.addAttribute(attribute);

      Notification notification = translate(null, newInformationObject);

      // Check whether all necessary Diff-Elements are present:
      String attributeName = createName(TranslationSiena.PREFIX_DIFF, false, DefinedAttributeIdentification.PERSON_NAME);
      String subAttributeName1 = createName(TranslationSiena.PREFIX_DIFF, true, DefinedAttributeIdentification.E_MAIL_ADDRESS);
      String subAttributeName2 = createName(TranslationSiena.PREFIX_DIFF, false, DefinedAttributeIdentification.PERSON_NAME,
            DefinedAttributeIdentification.E_MAIL_ADDRESS);

      String stringValue = notification.getAttribute(attributeName).stringValue();
      Assert.assertEquals(TranslationSiena.STATUS_CREATED, stringValue);

      stringValue = notification.getAttribute(subAttributeName1).stringValue();
      Assert.assertEquals(TranslationSiena.STATUS_CREATED, stringValue);

      stringValue = notification.getAttribute(subAttributeName2).stringValue();
      Assert.assertEquals(TranslationSiena.STATUS_CREATED, stringValue);
   }

   @Test
   public void testDiffDeletedInformationObject() {
      InformationObject oldInformationObject = datamodelFactory.createInformationObject();

      Identifier identifierOld = datamodelFactory.createIdentifierFromString(IDENTIFIER_STRING);
      oldInformationObject.setIdentifier(identifierOld);
      Attribute attribute = datamodelFactory.createAttribute(DefinedAttributeIdentification.PERSON_NAME.getURI(), personName);
      Attribute subAttribute = datamodelFactory.createAttribute(DefinedAttributeIdentification.E_MAIL_ADDRESS.getURI(), eMail);
      attribute.addSubattribute(subAttribute);
      oldInformationObject.addAttribute(attribute);

      Notification notification = translate(oldInformationObject, null);

      // Check whether all necessary Diff-Elements are present:
      String attributeName = createName(TranslationSiena.PREFIX_DIFF, false, DefinedAttributeIdentification.PERSON_NAME);
      String subAttributeName1 = createName(TranslationSiena.PREFIX_DIFF, true, DefinedAttributeIdentification.E_MAIL_ADDRESS);
      String subAttributeName2 = createName(TranslationSiena.PREFIX_DIFF, false, DefinedAttributeIdentification.PERSON_NAME,
            DefinedAttributeIdentification.E_MAIL_ADDRESS);

      String stringValue = notification.getAttribute(attributeName).stringValue();
      Assert.assertEquals(TranslationSiena.STATUS_DELETED, stringValue);

      stringValue = notification.getAttribute(subAttributeName1).stringValue();
      Assert.assertEquals(TranslationSiena.STATUS_DELETED, stringValue);

      stringValue = notification.getAttribute(subAttributeName2).stringValue();
      Assert.assertEquals(TranslationSiena.STATUS_DELETED, stringValue);
   }

   @Test
   public void testDiffUnchangedInformationObject() {
      InformationObject informationObject = datamodelFactory.createInformationObject();

      Identifier identifier = datamodelFactory.createIdentifierFromString(IDENTIFIER_STRING);
      informationObject.setIdentifier(identifier);
      Attribute attribute = datamodelFactory.createAttribute(DefinedAttributeIdentification.PERSON_NAME.getURI(), personName);
      Attribute subAttribute = datamodelFactory.createAttribute(DefinedAttributeIdentification.E_MAIL_ADDRESS.getURI(), eMail);
      Attribute subAttribute2 = datamodelFactory
      .createAttribute(DefinedAttributeIdentification.DESCRIPTION.getURI(), description);
      attribute.addSubattribute(subAttribute);
      attribute.addSubattribute(subAttribute2);
      informationObject.addAttribute(attribute);

      Notification notification = translate(informationObject, informationObject);

      // Check whether all necessary Diff-Elements are present:
      String attributeName = createName(TranslationSiena.PREFIX_DIFF, false, DefinedAttributeIdentification.PERSON_NAME);
      String subAttribute1Name1 = createName(TranslationSiena.PREFIX_DIFF, true, DefinedAttributeIdentification.E_MAIL_ADDRESS);
      String subAttribute1Name2 = createName(TranslationSiena.PREFIX_DIFF, false, DefinedAttributeIdentification.PERSON_NAME,
            DefinedAttributeIdentification.E_MAIL_ADDRESS);

      String subAttribute2Name1 = createName(TranslationSiena.PREFIX_DIFF, true, DefinedAttributeIdentification.DESCRIPTION);
      String subAttribute2Name2 = createName(TranslationSiena.PREFIX_DIFF, false, DefinedAttributeIdentification.PERSON_NAME,
            DefinedAttributeIdentification.DESCRIPTION);

      String stringValue = notification.getAttribute(attributeName).stringValue();
      Assert.assertEquals(TranslationSiena.STATUS_UNCHANGED, stringValue);

      // First subattribute
      stringValue = notification.getAttribute(subAttribute1Name1).stringValue();
      Assert.assertEquals(TranslationSiena.STATUS_UNCHANGED, stringValue);

      stringValue = notification.getAttribute(subAttribute1Name2).stringValue();
      Assert.assertEquals(TranslationSiena.STATUS_UNCHANGED, stringValue);

      // Second Subattribute.
      stringValue = notification.getAttribute(subAttribute2Name1).stringValue();
      Assert.assertEquals(TranslationSiena.STATUS_UNCHANGED, stringValue);

      stringValue = notification.getAttribute(subAttribute2Name2).stringValue();
      Assert.assertEquals(TranslationSiena.STATUS_UNCHANGED, stringValue);
   }

   @Test
   public void testDiffChanged2Values1Remaining() {
      // The name changes, the email address changes, but the description remains the same.
      String personNameOld = "Ede Bauer";
      String personNameNew = "Eduard B";
      String eMailOld = "edebauer@mail.de";
      String eMailNew = "eduardb@mail.de";
      String description = "This is my mail addresss";

      InformationObject oldInformationObject = datamodelFactory.createInformationObject();

      Identifier identifierOld = datamodelFactory.createIdentifierFromString(IDENTIFIER_STRING);
      oldInformationObject.setIdentifier(identifierOld);
      Attribute attributeOld = datamodelFactory.createAttribute(DefinedAttributeIdentification.PERSON_NAME.getURI(),
            personNameOld);
      Attribute subAttribute1Old = datamodelFactory.createAttribute(DefinedAttributeIdentification.E_MAIL_ADDRESS.getURI(),
            eMailOld);
      Attribute subAttribute2Old = datamodelFactory.createAttribute(DefinedAttributeIdentification.DESCRIPTION.getURI(),
            description);
      attributeOld.addSubattribute(subAttribute1Old);
      attributeOld.addSubattribute(subAttribute2Old);
      oldInformationObject.addAttribute(attributeOld);

      // New Information Object
      InformationObject newInformationObject = datamodelFactory.createInformationObject();

      Identifier identifierNew = datamodelFactory.createIdentifierFromString(IDENTIFIER_STRING);
      newInformationObject.setIdentifier(identifierNew);
      Attribute attributenew = datamodelFactory.createAttribute(DefinedAttributeIdentification.PERSON_NAME.getURI(),
            personNameNew);
      Attribute subAttribute1New = datamodelFactory.createAttribute(DefinedAttributeIdentification.E_MAIL_ADDRESS.getURI(),
            eMailNew);
      Attribute subAttribute2New = datamodelFactory.createAttribute(DefinedAttributeIdentification.DESCRIPTION.getURI(),
            description);
      attributenew.addSubattribute(subAttribute1New);
      attributenew.addSubattribute(subAttribute2New);
      newInformationObject.addAttribute(attributenew);

      Notification notification = translate(oldInformationObject, newInformationObject);

      // Check whether all necessary Diff-Elements are present:
      String attributeName = createName(TranslationSiena.PREFIX_DIFF, false, DefinedAttributeIdentification.PERSON_NAME);
      String subAttribute1Name1 = createName(TranslationSiena.PREFIX_DIFF, true, DefinedAttributeIdentification.E_MAIL_ADDRESS);
      String subAttribute1Name2 = createName(TranslationSiena.PREFIX_DIFF, false, DefinedAttributeIdentification.PERSON_NAME,
            DefinedAttributeIdentification.E_MAIL_ADDRESS);

      String subAttribute2Name1 = createName(TranslationSiena.PREFIX_DIFF, true, DefinedAttributeIdentification.DESCRIPTION);
      String subAttribute2Name2 = createName(TranslationSiena.PREFIX_DIFF, false, DefinedAttributeIdentification.PERSON_NAME,
            DefinedAttributeIdentification.DESCRIPTION);

      String stringValue = notification.getAttribute(attributeName).stringValue();
      Assert.assertEquals(TranslationSiena.STATUS_CHANGED, stringValue);

      // First subattribute
      stringValue = notification.getAttribute(subAttribute1Name1).stringValue();
      Assert.assertEquals(TranslationSiena.STATUS_CHANGED, stringValue);

      stringValue = notification.getAttribute(subAttribute1Name2).stringValue();
      Assert.assertEquals(TranslationSiena.STATUS_CHANGED, stringValue);

      // Second Subattribute.
      stringValue = notification.getAttribute(subAttribute2Name1).stringValue();
      Assert.assertEquals(TranslationSiena.STATUS_UNCHANGED, stringValue);

      stringValue = notification.getAttribute(subAttribute2Name2).stringValue();
      Assert.assertEquals(TranslationSiena.STATUS_UNCHANGED, stringValue);
   }

   @Test
   public void testDeletedAttributeWithoutSubattribute() {
      // Old InformationObject
      InformationObject oldInformationObject = datamodelFactory.createInformationObject();

      Identifier identifierOld = datamodelFactory.createIdentifierFromString(IDENTIFIER_STRING);
      oldInformationObject.setIdentifier(identifierOld);

      Attribute attribute1Old = datamodelFactory.createAttribute(DefinedAttributeIdentification.PERSON_NAME.getURI(), personName);
      Attribute subAttribute1Old = datamodelFactory
      .createAttribute(DefinedAttributeIdentification.E_MAIL_ADDRESS.getURI(), eMail);

      Attribute attribute2Old = datamodelFactory
      .createAttribute(DefinedAttributeIdentification.DESCRIPTION.getURI(), description);

      attribute1Old.addSubattribute(subAttribute1Old);
      oldInformationObject.addAttribute(attribute1Old);
      oldInformationObject.addAttribute(attribute2Old);

      // New InformationObject
      InformationObject newInformationObject = datamodelFactory.createInformationObject();

      Identifier identifierNew = datamodelFactory.createIdentifierFromString(IDENTIFIER_STRING);
      newInformationObject.setIdentifier(identifierNew);

      Attribute attribute1New = datamodelFactory.createAttribute(DefinedAttributeIdentification.PERSON_NAME.getURI(), personName);
      Attribute subAttribute1New = datamodelFactory
      .createAttribute(DefinedAttributeIdentification.E_MAIL_ADDRESS.getURI(), eMail);

      // Description is deleted
      attribute1New.addSubattribute(subAttribute1New);
      newInformationObject.addAttribute(attribute1New);

      Notification notification = translate(oldInformationObject, newInformationObject);

      // Check whether all necessary Diff-Elements are present:
      String attributeName = createName(TranslationSiena.PREFIX_DIFF, false, DefinedAttributeIdentification.PERSON_NAME);
      String subAttribute1Name1 = createName(TranslationSiena.PREFIX_DIFF, true, DefinedAttributeIdentification.E_MAIL_ADDRESS);
      String subAttribute1Name2 = createName(TranslationSiena.PREFIX_DIFF, false, DefinedAttributeIdentification.PERSON_NAME,
            DefinedAttributeIdentification.E_MAIL_ADDRESS);

      String attribute2Name = createName(TranslationSiena.PREFIX_DIFF, false, DefinedAttributeIdentification.DESCRIPTION);

      String stringValue = notification.getAttribute(attributeName).stringValue();
      Assert.assertEquals(TranslationSiena.STATUS_UNCHANGED, stringValue);

      // First subattribute
      stringValue = notification.getAttribute(subAttribute1Name1).stringValue();
      Assert.assertEquals(TranslationSiena.STATUS_UNCHANGED, stringValue);

      stringValue = notification.getAttribute(subAttribute1Name2).stringValue();
      Assert.assertEquals(TranslationSiena.STATUS_UNCHANGED, stringValue);

      // Second Attribute.
      stringValue = notification.getAttribute(attribute2Name).stringValue();
      Assert.assertEquals(TranslationSiena.STATUS_DELETED, stringValue);
   }

   @Test
   public void testDeletedAttributeWithSubattribute() {
      // Old InformationObject
      InformationObject oldInformationObject = datamodelFactory.createInformationObject();

      Identifier identifierOld = datamodelFactory.createIdentifierFromString(IDENTIFIER_STRING);
      oldInformationObject.setIdentifier(identifierOld);

      Attribute attribute1Old = datamodelFactory.createAttribute(DefinedAttributeIdentification.PERSON_NAME.getURI(), personName);
      Attribute subAttribute1Old = datamodelFactory
      .createAttribute(DefinedAttributeIdentification.E_MAIL_ADDRESS.getURI(), eMail);

      Attribute attribute2Old = datamodelFactory
      .createAttribute(DefinedAttributeIdentification.DESCRIPTION.getURI(), description);

      attribute1Old.addSubattribute(subAttribute1Old);
      oldInformationObject.addAttribute(attribute1Old);
      oldInformationObject.addAttribute(attribute2Old);

      // New InformationObject
      InformationObject newInformationObject = datamodelFactory.createInformationObject();

      Identifier identifierNew = datamodelFactory.createIdentifierFromString(IDENTIFIER_STRING);
      newInformationObject.setIdentifier(identifierNew);

      // Person Name, and subattribute e-mail Address is deleted

      Attribute attribute1New = datamodelFactory
      .createAttribute(DefinedAttributeIdentification.DESCRIPTION.getURI(), description);
      newInformationObject.addAttribute(attribute1New);

      Notification notification = translate(oldInformationObject, newInformationObject);

      // Check whether all necessary Diff-Elements are present:
      String attributeName = createName(TranslationSiena.PREFIX_DIFF, false, DefinedAttributeIdentification.PERSON_NAME);
      String subAttribute1Name1 = createName(TranslationSiena.PREFIX_DIFF, true, DefinedAttributeIdentification.E_MAIL_ADDRESS);
      String subAttribute1Name2 = createName(TranslationSiena.PREFIX_DIFF, false, DefinedAttributeIdentification.PERSON_NAME,
            DefinedAttributeIdentification.E_MAIL_ADDRESS);

      String attribute2Name = createName(TranslationSiena.PREFIX_DIFF, false, DefinedAttributeIdentification.DESCRIPTION);

      // Must be deleted
      String stringValue = notification.getAttribute(attributeName).stringValue();
      Assert.assertEquals(TranslationSiena.STATUS_DELETED, stringValue);

      // First subattribute
      stringValue = notification.getAttribute(subAttribute1Name1).stringValue();
      Assert.assertEquals(TranslationSiena.STATUS_DELETED, stringValue);

      stringValue = notification.getAttribute(subAttribute1Name2).stringValue();
      Assert.assertEquals(TranslationSiena.STATUS_DELETED, stringValue);

      // Second Attribute.
      stringValue = notification.getAttribute(attribute2Name).stringValue();
      Assert.assertEquals(TranslationSiena.STATUS_UNCHANGED, stringValue);
   }

   @Test
   public void testNewAttributeWithoutSubattribute() {
      InformationObject oldInformationObject = datamodelFactory.createInformationObject();
      InformationObject newInformationObject = datamodelFactory.createInformationObject();

      Identifier identifierOld = datamodelFactory.createIdentifierFromString(IDENTIFIER_STRING);
      oldInformationObject.setIdentifier(identifierOld);
      Identifier identifierNew = datamodelFactory.createIdentifierFromString(IDENTIFIER_STRING);
      newInformationObject.setIdentifier(identifierNew);

      Attribute attribute1Old = datamodelFactory.createAttribute(DefinedAttributeIdentification.PERSON_NAME.getURI(), personName);
      Attribute subAttribute1 = datamodelFactory.createAttribute(DefinedAttributeIdentification.E_MAIL_ADDRESS.getURI(), eMail);
      attribute1Old.addSubattribute(subAttribute1);

      Attribute attribute2 = datamodelFactory.createAttribute(DefinedAttributeIdentification.DESCRIPTION.getURI(), description);

      oldInformationObject.addAttribute(attribute1Old);
      Attribute attribute1New = (Attribute) attribute1Old.clone();
      newInformationObject.addAttribute(attribute1New);
      newInformationObject.addAttribute(attribute2);

      Notification notification = translate(oldInformationObject, newInformationObject);

      String attributeName = createName(TranslationSiena.PREFIX_DIFF, false, DefinedAttributeIdentification.PERSON_NAME);
      String subAttribute1Name1 = createName(TranslationSiena.PREFIX_DIFF, true, DefinedAttributeIdentification.E_MAIL_ADDRESS);
      String subAttribute1Name2 = createName(TranslationSiena.PREFIX_DIFF, false, DefinedAttributeIdentification.PERSON_NAME,
            DefinedAttributeIdentification.E_MAIL_ADDRESS);

      String attribute2Name = createName(TranslationSiena.PREFIX_DIFF, false, DefinedAttributeIdentification.DESCRIPTION);

      // Unchanged
      String stringValue = notification.getAttribute(attributeName).stringValue();
      Assert.assertEquals(TranslationSiena.STATUS_UNCHANGED, stringValue);

      // First subattribute, unchanged
      stringValue = notification.getAttribute(subAttribute1Name1).stringValue();
      Assert.assertEquals(TranslationSiena.STATUS_UNCHANGED, stringValue);

      stringValue = notification.getAttribute(subAttribute1Name2).stringValue();
      Assert.assertEquals(TranslationSiena.STATUS_UNCHANGED, stringValue);

      // Second Attribute.
      stringValue = notification.getAttribute(attribute2Name).stringValue();
      Assert.assertEquals(TranslationSiena.STATUS_CREATED, stringValue);
   }

   @Test
   public void testNewAttributeWithSubattribute() {
      InformationObject oldInformationObject = datamodelFactory.createInformationObject();
      InformationObject newInformationObject = datamodelFactory.createInformationObject();

      Identifier identifierOld = datamodelFactory.createIdentifierFromString(IDENTIFIER_STRING);
      oldInformationObject.setIdentifier(identifierOld);
      Identifier identifierNew = datamodelFactory.createIdentifierFromString(IDENTIFIER_STRING);
      newInformationObject.setIdentifier(identifierNew);

      Attribute attribute1 = datamodelFactory.createAttribute(DefinedAttributeIdentification.PERSON_NAME.getURI(), personName);
      Attribute subAttribute1 = datamodelFactory.createAttribute(DefinedAttributeIdentification.E_MAIL_ADDRESS.getURI(), eMail);
      attribute1.addSubattribute(subAttribute1);

      Attribute attribute2Old = datamodelFactory
      .createAttribute(DefinedAttributeIdentification.DESCRIPTION.getURI(), description);

      oldInformationObject.addAttribute(attribute2Old);
      newInformationObject.addAttribute(attribute1);
      Attribute attribute2New = (Attribute) attribute2Old.clone();
      newInformationObject.addAttribute(attribute2New);

      Notification notification = translate(oldInformationObject, newInformationObject);

      String attributeName = createName(TranslationSiena.PREFIX_DIFF, false, DefinedAttributeIdentification.PERSON_NAME);
      String subAttribute1Name1 = createName(TranslationSiena.PREFIX_DIFF, true, DefinedAttributeIdentification.E_MAIL_ADDRESS);
      String subAttribute1Name2 = createName(TranslationSiena.PREFIX_DIFF, false, DefinedAttributeIdentification.PERSON_NAME,
            DefinedAttributeIdentification.E_MAIL_ADDRESS);

      String attribute2Name = createName(TranslationSiena.PREFIX_DIFF, false, DefinedAttributeIdentification.DESCRIPTION);

      // Unchanged
      String stringValue = notification.getAttribute(attributeName).stringValue();
      Assert.assertEquals(TranslationSiena.STATUS_CREATED, stringValue);

      // First subattribute, unchanged
      stringValue = notification.getAttribute(subAttribute1Name1).stringValue();
      Assert.assertEquals(TranslationSiena.STATUS_CREATED, stringValue);

      stringValue = notification.getAttribute(subAttribute1Name2).stringValue();
      Assert.assertEquals(TranslationSiena.STATUS_CREATED, stringValue);

      // Second Attribute.
      stringValue = notification.getAttribute(attribute2Name).stringValue();
      Assert.assertEquals(TranslationSiena.STATUS_UNCHANGED, stringValue);
   }

   @Test
   public void testDiffDetailsLt() {
      String intIdent = "int";
      String longIdent = "long";

      InformationObject oldInformationObject = datamodelFactory.createInformationObject();
      InformationObject newInformationObject = datamodelFactory.createInformationObject();

      Identifier identifierOld = datamodelFactory.createIdentifierFromString(IDENTIFIER_STRING);
      oldInformationObject.setIdentifier(identifierOld);
      Identifier identifierNew = datamodelFactory.createIdentifierFromString(IDENTIFIER_STRING);
      newInformationObject.setIdentifier(identifierNew);

      Attribute attribute1 = datamodelFactory.createAttribute(intIdent, 5);
      Attribute subAttribute11 = datamodelFactory.createAttribute(longIdent, 5L);

      attribute1.addSubattribute(subAttribute11);
      oldInformationObject.addAttribute(attribute1);

      Attribute attribute2 = datamodelFactory.createAttribute(intIdent, 10);
      Attribute subAttribute21 = datamodelFactory.createAttribute(longIdent, 10L);

      attribute2.addSubattribute(subAttribute21);
      newInformationObject.addAttribute(attribute2);

      Notification notification = translate(oldInformationObject, newInformationObject);

      String attributeName = createName(TranslationSiena.PREFIX_DIFF_DETAILS, false, intIdent);
      String subAttributeNameWildcard = createName(TranslationSiena.PREFIX_DIFF_DETAILS, true, longIdent);
      String subAttributeNameFull = createName(TranslationSiena.PREFIX_DIFF_DETAILS, false, intIdent, longIdent);

      String stringValue = notification.getAttribute(attributeName).stringValue();
      Assert.assertEquals(TranslationSiena.STATUS_CHANGED_LT, stringValue);

      // First subattribute, unchanged
      stringValue = notification.getAttribute(subAttributeNameWildcard).stringValue();
      Assert.assertEquals(TranslationSiena.STATUS_CHANGED_LT, stringValue);

      stringValue = notification.getAttribute(subAttributeNameFull).stringValue();
      Assert.assertEquals(TranslationSiena.STATUS_CHANGED_LT, stringValue);
   }

   @Test
   public void testDiffDetailsGt() {
      String intIdent = "int";
      String doubleIdent = "double";

      InformationObject oldInformationObject = datamodelFactory.createInformationObject();
      InformationObject newInformationObject = datamodelFactory.createInformationObject();

      Identifier identifierOld = datamodelFactory.createIdentifierFromString(IDENTIFIER_STRING);
      oldInformationObject.setIdentifier(identifierOld);
      Identifier identifierNew = datamodelFactory.createIdentifierFromString(IDENTIFIER_STRING);
      newInformationObject.setIdentifier(identifierNew);

      Attribute attribute1 = datamodelFactory.createAttribute(intIdent, 15);
      Attribute subAttribute11 = datamodelFactory.createAttribute(doubleIdent, 55d);

      attribute1.addSubattribute(subAttribute11);
      oldInformationObject.addAttribute(attribute1);

      Attribute attribute2 = datamodelFactory.createAttribute(intIdent, 10);
      Attribute subAttribute21 = datamodelFactory.createAttribute(doubleIdent, 51d);

      attribute2.addSubattribute(subAttribute21);
      newInformationObject.addAttribute(attribute2);

      Notification notification = translate(oldInformationObject, newInformationObject);

      String attributeName = createName(TranslationSiena.PREFIX_DIFF_DETAILS, false, intIdent);
      String subAttributeNameWildcard = createName(TranslationSiena.PREFIX_DIFF_DETAILS, true, doubleIdent);
      String subAttributeNameFull = createName(TranslationSiena.PREFIX_DIFF_DETAILS, false, intIdent, doubleIdent);

      String stringValue = notification.getAttribute(attributeName).stringValue();
      Assert.assertEquals(TranslationSiena.STATUS_CHANGED_GT, stringValue);

      // First subattribute, unchanged
      stringValue = notification.getAttribute(subAttributeNameWildcard).stringValue();
      Assert.assertEquals(TranslationSiena.STATUS_CHANGED_GT, stringValue);

      stringValue = notification.getAttribute(subAttributeNameFull).stringValue();
      Assert.assertEquals(TranslationSiena.STATUS_CHANGED_GT, stringValue);
   }

   @Test
   public void testDiffDetailsDifferentDataTypes() {
      String intIdent = "int";
      String doubleIdent = "double";

      InformationObject oldInformationObject = datamodelFactory.createInformationObject();
      InformationObject newInformationObject = datamodelFactory.createInformationObject();

      Identifier identifierOld = datamodelFactory.createIdentifierFromString(IDENTIFIER_STRING);
      oldInformationObject.setIdentifier(identifierOld);
      Identifier identifierNew = datamodelFactory.createIdentifierFromString(IDENTIFIER_STRING);
      newInformationObject.setIdentifier(identifierNew);

      Attribute attribute1 = datamodelFactory.createAttribute(intIdent, 15L);
      Attribute subAttribute11 = datamodelFactory.createAttribute(doubleIdent, 55d);

      attribute1.addSubattribute(subAttribute11);
      oldInformationObject.addAttribute(attribute1);

      Attribute attribute2 = datamodelFactory.createAttribute(intIdent, 10);
      Attribute subAttribute21 = datamodelFactory.createAttribute(doubleIdent, 151515150);

      attribute2.addSubattribute(subAttribute21);
      newInformationObject.addAttribute(attribute2);

      Notification notification = translate(oldInformationObject, newInformationObject);

      String attributeName = createName(TranslationSiena.PREFIX_DIFF_DETAILS, false, intIdent);
      String subAttributeNameWildcard = createName(TranslationSiena.PREFIX_DIFF_DETAILS, true, doubleIdent);
      String subAttributeNameFull = createName(TranslationSiena.PREFIX_DIFF_DETAILS, false, intIdent, doubleIdent);

      String stringValue = notification.getAttribute(attributeName).stringValue();
      Assert.assertEquals(TranslationSiena.STATUS_CHANGED_GT, stringValue);

      // First subattribute, unchanged
      stringValue = notification.getAttribute(subAttributeNameWildcard).stringValue();
      Assert.assertEquals(TranslationSiena.STATUS_CHANGED_LT, stringValue);

      stringValue = notification.getAttribute(subAttributeNameFull).stringValue();
      Assert.assertEquals(TranslationSiena.STATUS_CHANGED_LT, stringValue);
   }

   private Notification translate(InformationObject oldInformationObject, InformationObject newInformationObject) {
      ESFEventMessage eventMessage = new ESFEventMessage();
      eventMessage.setOldInformationObject(oldInformationObject);
      eventMessage.setNewInformationObject(newInformationObject);
      eventMessage.setSerializeFormat(SerializeFormat.JAVA);

      List<Notification> notificationList = publisherNetInf.translateEventMessage(eventMessage);
      return notificationList.get(0);
   }

   private String createName(String prefix, boolean wildcard, DefinedAttributeIdentification... names) {
      String result = prefix;
      result = result + TranslationSiena.SEPARATOR;

      if (wildcard) {
         result = result + TranslationSiena.WILDCARD + TranslationSiena.SEPARATOR;
      }

      boolean first = true;

      for (DefinedAttributeIdentification definedAttributeIdentification : names) {

         if (first) {
            first = false;
         } else {
            result = result + TranslationSiena.SEPARATOR;
         }

         result = result + definedAttributeIdentification.getURI();
      }

      return result;

   }

   private String createName(String prefix, boolean wildcard, String... names) {
      String result = prefix;
      result = result + TranslationSiena.SEPARATOR;

      if (wildcard) {
         result = result + TranslationSiena.WILDCARD + TranslationSiena.SEPARATOR;
      }

      boolean first = true;

      for (String string : names) {

         if (first) {
            first = false;
         } else {
            result = result + TranslationSiena.SEPARATOR;
         }

         result = result + string;
      }

      return result;

   }
}
