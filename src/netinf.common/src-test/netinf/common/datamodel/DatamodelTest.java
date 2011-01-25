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
package netinf.common.datamodel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import netinf.common.communication.NetInfNodeConnection;
import netinf.common.communication.RemoteNodeConnection;
import netinf.common.communication.SerializeFormat;
import netinf.common.datamodel.attribute.Attribute;
import netinf.common.datamodel.attribute.DefinedAttributeIdentification;
import netinf.common.datamodel.identity.IdentityObject;
import netinf.common.datamodel.impl.DataObjectImpl;
import netinf.common.datamodel.impl.identity.EventServiceIdentityObjectImpl;
import netinf.common.datamodel.impl.identity.GroupIdentityObjectImpl;
import netinf.common.datamodel.impl.identity.NodeIdentityObjectImpl;
import netinf.common.datamodel.impl.identity.PersonIdentityObjectImpl;
import netinf.common.datamodel.impl.identity.ResolutionServiceIdentityObjectImpl;
import netinf.common.datamodel.impl.identity.SearchServiceIdentityObjectImpl;
import netinf.common.datamodel.impl.module.DatamodelImplModule;
import netinf.common.exceptions.NetInfUncheckedException;
import netinf.common.log.module.LogModule;
import netinf.common.security.impl.module.SecurityModule;
import netinf.common.utils.DatamodelUtils;
import netinf.common.utils.Utils;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;

/**
 * Tests arbitrary datamodel implementation. Might test rdf-datamodel implementation and impl-datamodel implementation.
 * 
 * @author PG Augnet 2, University of Paderborn
 * 
 */
public class DatamodelTest {

   public static final String PROPERTIES = "../configs/testing.properties";

   public static final String ATTRIBUTE_IDENTIFICATION = DefinedAttributeIdentification.DESCRIPTION.getURI();
   public static final String ATTRIBUTE_VALUE = "This is the first RDF Object";;
   public static final String ATTRIBUTE_PURPOSE = DefinedAttributePurpose.SYSTEM_ATTRIBUTE.getAttributePurpose();

   public static final String SUBATTRIBUTE_IDENTIFICATION = DefinedAttributeIdentification.PERSON_AGE.getURI();
   public static final Integer SUBATTRIBUTE_VALUE = 25;
   public static final String SUBATTRIBUTE_PURPOSE = DefinedAttributePurpose.USER_ATTRIBUTE.getAttributePurpose();

   public static final String IDENTIFIER_LABEL_1_NAME = DefinedLabelName.UNIQUE_LABEL.getLabelName();
   public static final String IDENTIFIER_LABEL_2_VALUE = DefinedVersionKind.UNVERSIONED.getStringRepresentation();
   public static final String IDENTIFIER_LABEL_2_NAME = DefinedLabelName.VERSION_KIND.getLabelName();
   public static final String IDENTIFIER_LABEL_1_VALUE = "1234321";

   // DummyDataObject
   public static final String DO_ATTRIBUTE_IDENTIFICATION = "testIdentification";
   public static final String DO_ATTRIBUTE_VALUE = "testValue";
   public static final String DO_ATTRIBUTE_PURPOSE = DefinedAttributePurpose.LOCATOR_ATTRIBUTE.getAttributePurpose();
   
   private static DatamodelFactory datamodelFactory;

   @BeforeClass
   public static void setUpTest() {
      System.out.println("DatamodelTest is executed");
      final Properties properties = Utils.loadProperties(PROPERTIES);
      Injector injector = Guice.createInjector(new LogModule(properties), new SecurityModule(), new DatamodelImplModule(),
            new AbstractModule() {

               @Override
               protected void configure() {
                  Names.bindProperties(binder(), properties);
                  bind(NetInfNodeConnection.class).annotatedWith(SecurityModule.Security.class).to(RemoteNodeConnection.class);
               }
            });
      setDatamodelFactory(injector.getInstance(DatamodelFactory.class));
   }

   @Test
   public void testIdentifierStringTranslation() {
      String string = "ni:HASH_OF_PK=Bar~UNIQUE_LABEL=Foo~name1=value1~name2=value2";
      Identifier identifier = getDatamodelFactory().createIdentifierFromString(string);

      Assert.assertEquals(identifier.getIdentifierLabels().size(), 4);
      Assert.assertEquals(string, identifier.toString());

      Identifier identifier2 = getDatamodelFactory().createIdentifierFromString(identifier.toString());
      Assert.assertEquals(identifier, identifier2);
   }

   @Test
   public void testCloneInformationObject() {
      InformationObject informationObject = createDummyInformationObject(getDatamodelFactory());
      InformationObject clonedInformationObject = (InformationObject) informationObject.clone();

      Assert.assertEquals(informationObject, clonedInformationObject);
      Assert.assertEquals(informationObject.hashCode(), clonedInformationObject.hashCode());
   }
   
   @Test
   public void testCloneIdentifierLabel(){
	   IdentifierLabel iLabel = getDatamodelFactory().createIdentifierLabel();
	   IdentifierLabel clonediLabel = (IdentifierLabel) iLabel.clone();
	   
	   Assert.assertEquals(iLabel, clonediLabel);
   }

   @Test
   public void testSerializeDeserializeInformationObject() {
      InformationObject informationObject = createDummyInformationObject(getDatamodelFactory());
      InformationObject deserializedInformationObject = getDatamodelFactory().createInformationObjectFromBytes(informationObject
            .serializeToBytes());

      boolean equalInformationObjects = DatamodelUtils
            .equalInformationObjects(informationObject, deserializedInformationObject);

      Assert.assertTrue(equalInformationObjects);
      Assert.assertEquals(informationObject.hashCode(), deserializedInformationObject.hashCode());
   }

   @Test
   public void testIdentifierFromString() {
      Identifier identifier = getDatamodelFactory()
            .createIdentifierFromString("ni:name1=value1~UNIQUE_LABEL=Foo~name2=value2~HASH_OF_PK=Bar");
      Assert.assertEquals("ni:HASH_OF_PK=Bar~UNIQUE_LABEL=Foo~name1=value1~name2=value2", identifier.toString());
   }
   
   @Test(expected=NetInfUncheckedException.class)
   public void testIdentifierFromStringWithError() {
      Identifier identifier = getDatamodelFactory().createIdentifierFromString("schnubbedibu");
      Assert.fail("Exception not thrown");
   }

   @Test
   public void testAddRemoveProperty() {
      InformationObject informationObject = getDatamodelFactory().createInformationObject();
      Attribute property = getDatamodelFactory().createAttribute(DefinedAttributeIdentification.E_MAIL_ADDRESS.getURI(),
            "something@somethingelse.de");

      // Add to information object
      informationObject.addAttribute(property);

      // Check
      Attribute propertyEqual = informationObject.getSingleAttribute(DefinedAttributeIdentification.E_MAIL_ADDRESS.getURI());
      Assert.assertEquals(property, propertyEqual);
      Assert.assertEquals(property.getInformationObject(), informationObject);

      // Remove from information object
      informationObject.removeAttribute(DefinedAttributeIdentification.E_MAIL_ADDRESS.getURI());

      // Check
      Attribute propertyUnequal = informationObject.getSingleAttribute(DefinedAttributeIdentification.E_MAIL_ADDRESS.getURI());
      Assert.assertEquals(propertyUnequal, null);
      Assert.assertEquals(property.getInformationObject(), null);
   }

   @Test
   public void testAddRemoveSubproperty() {
      InformationObject informationObject = getDatamodelFactory().createInformationObject();
      Attribute property1 = getDatamodelFactory().createAttribute(DefinedAttributeIdentification.E_MAIL_ADDRESS.getURI(),
            "something@somethingelse.de");

      Attribute property2 = getDatamodelFactory().createAttribute(DefinedAttributeIdentification.PERSON_NAME.getURI(),
            "something@somethingelse.de");

      // Add to information object, and add subproperty to property
      informationObject.addAttribute(property1);
      property1.addSubattribute(property2);

      // Check
      Attribute propertyEqual1 = informationObject.getSingleAttribute(DefinedAttributeIdentification.E_MAIL_ADDRESS.getURI());
      Assert.assertEquals(property1, propertyEqual1);
      Assert.assertEquals(property1.getInformationObject(), informationObject);

      Attribute propertyEqual2 = property1.getSingleSubattribute(DefinedAttributeIdentification.PERSON_NAME.getURI());
      Assert.assertEquals(property2, propertyEqual2);
      Assert.assertEquals(property2.getInformationObject(), informationObject);

      // Remove from information object
      informationObject.removeAttribute(DefinedAttributeIdentification.E_MAIL_ADDRESS.getURI());

      // Check
      Attribute propertyUnequal = informationObject.getSingleAttribute(DefinedAttributeIdentification.E_MAIL_ADDRESS.getURI());
      Assert.assertEquals(propertyUnequal, null);
      Assert.assertEquals(property1.getInformationObject(), null);
      Assert.assertEquals(property2.getInformationObject(), null);
   }

   @Test
   public void testAddPropertyTwice() {
      InformationObject informationObject = getDatamodelFactory().createInformationObject();
      Attribute property = getDatamodelFactory().createAttribute(DefinedAttributeIdentification.E_MAIL_ADDRESS.getURI(),
            "something@somethingelse.de");

      informationObject.addAttribute(property);
      informationObject.addAttribute(property);

      // attribute should be overwritten
      Assert.assertEquals(1, informationObject.getAttributes().size());
   }

   @Test
   public void testSetAndGetPropertyValue() {
      InformationObject informationObject = getDatamodelFactory().createInformationObject();
      Attribute property = getDatamodelFactory().createAttribute(DefinedAttributeIdentification.E_MAIL_ADDRESS.getURI(),
            "something@somethingelse.de");

      informationObject.addAttribute(property);
      String value = property.getValue(String.class);

      Assert.assertEquals("something@somethingelse.de", value);
   }

   @Test(expected=ClassCastException.class)
   public void testSetAndGetPropertyValueWithError() {
      InformationObject informationObject = getDatamodelFactory().createInformationObject();
      Attribute property = getDatamodelFactory().createAttribute(DefinedAttributeIdentification.E_MAIL_ADDRESS.getURI(),
            "something@somethingelse.de");

      informationObject.addAttribute(property);
      
      // This method should throw an exception
      Integer value = property.getValue(Integer.class);

      // Assert.assertEquals("something@somethingelse.de", value);
   }

   @Test
   public void testSerializeDeserialize() throws IOException, ClassNotFoundException {
      InformationObject informationObject = getDatamodelFactory().createInformationObject();
      Attribute property1 = getDatamodelFactory().createAttribute(DefinedAttributeIdentification.E_MAIL_ADDRESS.getURI(),
            "something@somethingelse.de");

      Attribute property2 = getDatamodelFactory().createAttribute(DefinedAttributeIdentification.PERSON_NAME.getURI(),
            "something@somethingelse.de");

      informationObject.addAttribute(property1);
      informationObject.addAttribute(property2);

      byte[] bytes = informationObject.serializeToBytes();

      InformationObject copy = getDatamodelFactory().createInformationObjectFromBytes(bytes);

      Assert.assertEquals(informationObject, copy);
   }

   @Test
   public void testSorting() {
      InformationObject informationObject = getDatamodelFactory().createInformationObject();

      ArrayList<DefinedAttributeIdentification> someAttributes = new ArrayList<DefinedAttributeIdentification>();
      someAttributes.add(DefinedAttributeIdentification.PERSON_NAME);
      someAttributes.add(DefinedAttributeIdentification.PERSON_NAME);
      someAttributes.add(DefinedAttributeIdentification.E_MAIL_ADDRESS);
      someAttributes.add(DefinedAttributeIdentification.DESCRIPTION);

      int i = 0;
      for (DefinedAttributeIdentification definedAttributeIdentification : someAttributes) {
         Attribute attribute = getDatamodelFactory().createAttribute(definedAttributeIdentification.getURI(), "value" + i);
         informationObject.addAttribute(attribute);
         i++;
      }

      // Sort the Uris for verification
      String[] sortedUris = new String[someAttributes.size()];

      for (int j = 0; j < sortedUris.length; j++) {

         DefinedAttributeIdentification minimum = someAttributes.get(0);

         for (DefinedAttributeIdentification definedAttributeIdentification : someAttributes) {
            if (definedAttributeIdentification.getURI().compareTo(minimum.getURI()) < 0) {
               minimum = definedAttributeIdentification;
            }
         }

         sortedUris[j] = minimum.getURI();
         someAttributes.remove(minimum);
      }

      List<Attribute> attributes = informationObject.getAttributes();
      Attribute last = null;
      int current = 0;
      for (String string : sortedUris) {
         Attribute attribute = attributes.get(current);

         Assert.assertEquals(attribute.getIdentification(), string);

         // Check correct sorting according to criteria "identification + value".
         if (last != null) {
            boolean leq = (last.getIdentification() + last.getValueRaw()).compareTo(attribute.getIdentification()
                  + attribute.getValueRaw()) <= 0;

            Assert.assertTrue(leq);
         }

         last = attribute;
         current++;
      }

   }

   @Test
   public void testEqualityOfAttributes() {
      InformationObject dummyIO = createDummyInformationObject(getDatamodelFactory());
      Attribute attribute = dummyIO.getSingleAttribute(ATTRIBUTE_IDENTIFICATION);
      Attribute clone = (Attribute) attribute.clone();
      boolean equalAttributes = DatamodelUtils.equalAttributes(attribute, clone);

      Assert.assertTrue(equalAttributes);

      clone.setValue("somethingasdkfasdf");

      equalAttributes = DatamodelUtils.equalAttributes(attribute, clone);
      Assert.assertFalse(equalAttributes);
   }

   @Test
   public void testEqualityOfIdentifier() {
      InformationObject io = createDummyInformationObject(getDatamodelFactory());
      Identifier first = io.getIdentifier();

      Identifier second = (Identifier) first.clone();
      boolean equalIdentifiers = DatamodelUtils.equalIdentifiers(first, second);

      Assert.assertTrue(equalIdentifiers);

      second.getIdentifierLabels().get(0).setLabelName("afjakldfj");

      equalIdentifiers = DatamodelUtils.equalIdentifiers(first, second);
      Assert.assertFalse(equalIdentifiers);
   }
   
   @Test
   public void testGetSerializeFormat(){
	   Assert.assertEquals(SerializeFormat.JAVA, getDatamodelFactory().getSerializeFormat());
   }
   
   @Test
   public void testDifferentInformationObjectTypes() {
      InformationObject object = getDatamodelFactory().createDataObject();
      NetInfObjectWrapper desObject = getDatamodelFactory().createFromBytes(object.serializeToBytes());
      Assert.assertEquals(DataObjectImpl.class.getCanonicalName(), desObject.getClass().getCanonicalName());
      
      object = getDatamodelFactory().createEventServiceIdentityObject();
      desObject = getDatamodelFactory().createFromBytes(object.serializeToBytes());
      Assert.assertEquals(EventServiceIdentityObjectImpl.class.getCanonicalName(), desObject.getClass().getCanonicalName());

      object = getDatamodelFactory().createGroupIdentityObject();
      desObject = getDatamodelFactory().createFromBytes(object.serializeToBytes());
      Assert.assertEquals(GroupIdentityObjectImpl.class.getCanonicalName(), desObject.getClass().getCanonicalName());
      
      object = getDatamodelFactory().createNodeIdentityObject();
      desObject = getDatamodelFactory().createFromBytes(object.serializeToBytes());
      Assert.assertEquals(NodeIdentityObjectImpl.class.getCanonicalName(), desObject.getClass().getCanonicalName());
      
      object = getDatamodelFactory().createPersonIdentityObject();
      desObject = getDatamodelFactory().createFromBytes(object.serializeToBytes());
      Assert.assertEquals(PersonIdentityObjectImpl.class.getCanonicalName(), desObject.getClass().getCanonicalName());
      
      object = getDatamodelFactory().createResolutionServiceIdentityObject();
      desObject = getDatamodelFactory().createFromBytes(object.serializeToBytes());
      Assert.assertEquals(ResolutionServiceIdentityObjectImpl.class.getCanonicalName(), desObject.getClass().getCanonicalName());
      
      object = getDatamodelFactory().createSearchServiceIdentityObject();
      desObject = getDatamodelFactory().createFromBytes(object.serializeToBytes());
      Assert.assertEquals(SearchServiceIdentityObjectImpl.class.getCanonicalName(), desObject.getClass().getCanonicalName());
   }

   @Test
   public void testGetLocatorByLocatorType(){
	   DataObject obj = createDummyDataObject(getDatamodelFactory());
	   List<Attribute> attrs = obj.getLocatorByLocatorType(DO_ATTRIBUTE_IDENTIFICATION);
	   
	   // array should not be empty
	   Assert.assertFalse(attrs.isEmpty());
	   
	   // all returned attributes should be of that type
	   for(Attribute attr : attrs){
		   Assert.assertEquals(attr.getIdentification(), DO_ATTRIBUTE_IDENTIFICATION);   
	   }
   }
   
   @Test
   public void testRemoveIdentifierLabel(){
	   Identifier identifier = datamodelFactory.createIdentifier();
	   
	   IdentifierLabel identifierLabel1 = datamodelFactory.createIdentifierLabel();
	   identifierLabel1.setLabelName(IDENTIFIER_LABEL_1_NAME);
	   identifierLabel1.setLabelValue(IDENTIFIER_LABEL_1_VALUE);
	   identifier.addIdentifierLabel(identifierLabel1);
	   
	   // remove (previously) added label
	   identifier.removeIdentifierLabel(IDENTIFIER_LABEL_1_NAME);
	   
	   // should be null
	   Assert.assertNull(identifier.getIdentifierLabel(IDENTIFIER_LABEL_1_NAME));
   }
   
   @Test
   public void testSetPublicKeys(){
	   IdentityObject identObj = getDatamodelFactory().createIdentityObject();
	   Attribute attr = getDatamodelFactory().createAttribute(DefinedAttributeIdentification.PUBLIC_KEY.getURI(), "asdasdasd");
	   identObj.setPublicKeys(attr);
	   
	   // should be the same
	   Assert.assertEquals(identObj.getPublicKeys(), attr);
   }
   
   @Test
   public void testCreateIdentifierFromBytes(){
	   // dummy
	   Identifier identifier = datamodelFactory.createIdentifier();
	  
	   IdentifierLabel identifierLabel1 = datamodelFactory.createIdentifierLabel();
	   identifierLabel1.setLabelName(IDENTIFIER_LABEL_1_NAME);
	   identifierLabel1.setLabelValue(IDENTIFIER_LABEL_1_VALUE);
	  
	   identifier.addIdentifierLabel(identifierLabel1);
	   
	   NetInfObjectWrapper desIdent = getDatamodelFactory().createIdentifierFromBytes(identifier.serializeToBytes());
	   
	   // should be the same
	   Assert.assertEquals(identifier, desIdent);
   }
   
   @Test
   public void testGetDefinedAttributePurpose(){
	   String test = DefinedAttributePurpose.SYSTEM_ATTRIBUTE.toString();
	   // what is this method good for?
	   DefinedAttributePurpose purp = DefinedAttributePurpose.getDefinedAttributePurpose(test);
	   // should be the same
	   Assert.assertEquals(test, purp.toString());
   }
   
   @Test
   public void testgetVersionKind(){
	   String test = DefinedVersionKind.VERSIONED.toString();
	   // what is this method good for?
	   DefinedVersionKind vers = DefinedVersionKind.getVersionKind(test);
	   // should be the same
	   Assert.assertEquals(test, vers.toString());
	   //with error:
	   vers = DefinedVersionKind.getVersionKind("wrongRepresenation");
	   // should not be the same (UNKNOWN)
	   Assert.assertNotSame(test, vers.toString());
   }
   
   @Test
   public void testgetDefinedAttributeIdentificationByURI(){
	   String test = DefinedAttributeIdentification.getURIByAttributeIdentification(DefinedAttributeIdentification.AUTHORIZED_READERS);
	   // what is this method good for?
	   DefinedAttributeIdentification ident = DefinedAttributeIdentification.getDefinedAttributeIdentificationByURI(test);
	   // should be the same
	   Assert.assertEquals(test, ident.getURI());
   }
   
   @Test
   public void testGetSubattributesForPurpose(){
	   InformationObject iObj = createDummyInformationObject(getDatamodelFactory());
	   
	   // Attributes should exist (dummy)
	   if(iObj.getAttributes().size() > 0){
		   // dummy contains only one attribute
		   Attribute attr = iObj.getAttributes().get(0);
		   if(attr.getSubattributes().size() > 0){
			   List<Attribute> subAttrs = attr.getSubattributesForPurpose(SUBATTRIBUTE_PURPOSE);
			   // should be one
			   Assert.assertEquals(subAttrs.size(), 1);
			   // and the same purpose
			   Assert.assertEquals(subAttrs.get(0).getAttributePurpose(), SUBATTRIBUTE_PURPOSE);
		   } else {
			   Assert.fail("Dummy IO should contain Subattributes");
		   }
	   } else {
		   Assert.fail("Dummy IO should contain Attributes");
	   }
   }
   
   @Test
   public void testSetIdentificationTwice(){
	   InformationObject iObj = createDummyInformationObject(getDatamodelFactory());
	   String identif1 = DefinedAttributeIdentification.EVENT.getURI();
	   String identif2 = DefinedAttributeIdentification.DESCRIPTION.getURI();
	   List<Attribute> attrs = iObj.getAttribute(ATTRIBUTE_IDENTIFICATION);
	   
	   if(attrs.size() > 0){
		   Attribute attr = attrs.get(0);
		   attr.setIdentification(identif1);
		   Assert.assertEquals(attr.getIdentification(), identif1);
		   // now set again
		   attr.setIdentification(identif2);
		   Assert.assertEquals(attr.getIdentification(), identif2);
	   } else {
		   Assert.fail("IO should contain Attributes");
	   }
   }
   
   @Test(expected=NetInfUncheckedException.class)
   public void testCopyOjectWithError(){
	   getDatamodelFactory().copyObject(this);
	   Assert.fail("Exception not thrown");
   }
   
   public static DataObject createDummyDataObject(DatamodelFactory datamodelFactory){
	   DataObject object = getDatamodelFactory().createDataObject();
	   Attribute attribute1 = datamodelFactory.createAttribute();
	   Attribute attribute2 = datamodelFactory.createAttribute();
	  
	   attribute1.setIdentification(DO_ATTRIBUTE_IDENTIFICATION);
	   attribute1.setValue(DO_ATTRIBUTE_VALUE);
	   attribute1.setAttributePurpose(DO_ATTRIBUTE_PURPOSE);
	   object.addAttribute(attribute1);
	   
	   attribute2.setIdentification(ATTRIBUTE_IDENTIFICATION);
	   attribute2.setValue(ATTRIBUTE_VALUE);
	   attribute2.setAttributePurpose(ATTRIBUTE_PURPOSE);
	   object.addAttribute(attribute2);
	      
	   return object;
   }
   
   public static InformationObject createDummyInformationObject(DatamodelFactory datamodelFactory) {
      InformationObject informationObject = datamodelFactory.createInformationObject();
      Attribute attribute = datamodelFactory.createAttribute();
      Attribute subattribute = datamodelFactory.createAttribute();

      attribute.setIdentification(ATTRIBUTE_IDENTIFICATION);
      attribute.setValue(ATTRIBUTE_VALUE);
      attribute.setAttributePurpose(ATTRIBUTE_PURPOSE);

      subattribute.setIdentification(SUBATTRIBUTE_IDENTIFICATION);
      subattribute.setValue(SUBATTRIBUTE_VALUE);
      subattribute.setAttributePurpose(SUBATTRIBUTE_PURPOSE);

      attribute.addSubattribute(subattribute);
      informationObject.addAttribute(attribute);

      // The identifier
      Identifier identifier = datamodelFactory.createIdentifier();

      IdentifierLabel identifierLabel1 = datamodelFactory.createIdentifierLabel();
      identifierLabel1.setLabelName(IDENTIFIER_LABEL_1_NAME);
      identifierLabel1.setLabelValue(IDENTIFIER_LABEL_1_VALUE);

      IdentifierLabel identifierLabel2 = datamodelFactory.createIdentifierLabel();
      identifierLabel2.setLabelName(IDENTIFIER_LABEL_2_NAME);
      identifierLabel2.setLabelValue(IDENTIFIER_LABEL_2_VALUE);

      identifier.addIdentifierLabel(identifierLabel1);
      identifier.addIdentifierLabel(identifierLabel2);

      informationObject.setIdentifier(identifier);

      // String string = Utils.bytesToString(informationObject.serializeToBytes());
      // System.out.println(string);
      // System.out.println(informationObject);

      return informationObject;
   }

   public static void setDatamodelFactory(DatamodelFactory datamodelFactory) {
      DatamodelTest.datamodelFactory = datamodelFactory;
   }

   public static DatamodelFactory getDatamodelFactory() {
      return datamodelFactory;
   }
}
