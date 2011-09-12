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
package netinf.common.datamodel;

import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import netinf.common.communication.NetInfNodeConnection;
import netinf.common.communication.RemoteNodeConnection;
import netinf.common.communication.SerializeFormat;
import netinf.common.datamodel.attribute.Attribute;
import netinf.common.datamodel.attribute.DefinedAttributeIdentification;
import netinf.common.datamodel.creator.ValidCreator;
import netinf.common.datamodel.identity.EventServiceIdentityObject;
import netinf.common.datamodel.identity.GroupIdentityObject;
import netinf.common.datamodel.identity.IdentityObject;
import netinf.common.datamodel.identity.NodeIdentityObject;
import netinf.common.datamodel.identity.PersonIdentityObject;
import netinf.common.datamodel.identity.ResolutionServiceIdentityObject;
import netinf.common.datamodel.identity.SearchServiceIdentityObject;
import netinf.common.datamodel.identity.ServiceIdentityObject;
import netinf.common.datamodel.impl.DataObjectImpl;
import netinf.common.datamodel.impl.DatamodelFactoryImpl;
import netinf.common.datamodel.impl.identity.EventServiceIdentityObjectImpl;
import netinf.common.datamodel.impl.identity.GroupIdentityObjectImpl;
import netinf.common.datamodel.impl.identity.NodeIdentityObjectImpl;
import netinf.common.datamodel.impl.identity.PersonIdentityObjectImpl;
import netinf.common.datamodel.impl.identity.ResolutionServiceIdentityObjectImpl;
import netinf.common.datamodel.impl.identity.SearchServiceIdentityObjectImpl;
import netinf.common.datamodel.impl.identity.ServiceIdentityObjectImpl;
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
   public void testCloneIdentifierLabel() {
      IdentifierLabel iLabel = getDatamodelFactory().createIdentifierLabel();
      IdentifierLabel clonediLabel = (IdentifierLabel) iLabel.clone();

      Assert.assertEquals(iLabel, clonediLabel);
   }

   @Test
   public void testSerializeDeserializeInformationObject() {
      InformationObject informationObject = createDummyInformationObject(getDatamodelFactory());
      InformationObject deserializedInformationObject = getDatamodelFactory().createInformationObjectFromBytes(
            informationObject.serializeToBytes());

      boolean equalInformationObjects = DatamodelUtils.equalInformationObjects(informationObject, deserializedInformationObject);

      Assert.assertTrue(equalInformationObjects);
      Assert.assertEquals(informationObject.hashCode(), deserializedInformationObject.hashCode());
   }

   @Test
   public void testIdentifierFromString() {
      Identifier identifier = getDatamodelFactory().createIdentifierFromString(
            "ni:name1=value1~UNIQUE_LABEL=Foo~name2=value2~HASH_OF_PK=Bar");
      Assert.assertEquals("ni:HASH_OF_PK=Bar~UNIQUE_LABEL=Foo~name1=value1~name2=value2", identifier.toString());
   }

   @Test(expected = NetInfUncheckedException.class)
   public void testIdentifierFromStringWithError() {
      Identifier identifier = getDatamodelFactory().createIdentifierFromString("schnubbedibu");
      Assert.fail("Exception not thrown " + identifier.toString());
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

   @Test(expected = ClassCastException.class)
   public void testSetAndGetPropertyValueWithError() {
      InformationObject informationObject = getDatamodelFactory().createInformationObject();
      Attribute property = getDatamodelFactory().createAttribute(DefinedAttributeIdentification.E_MAIL_ADDRESS.getURI(),
            "something@somethingelse.de");

      informationObject.addAttribute(property);

      // This method should throw an exception
      Integer value = property.getValue(Integer.class);
      Assert.fail("error not thrown: value" + value);
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
   public void testGetSerializeFormat() {
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
   public void testGetLocatorByLocatorType() {
      DataObject obj = createDummyDataObject(getDatamodelFactory());
      List<Attribute> attrs = obj.getLocatorByLocatorType(DO_ATTRIBUTE_IDENTIFICATION);

      // array should not be empty
      Assert.assertFalse(attrs.isEmpty());

      // all returned attributes should be of that type
      for (Attribute attr : attrs) {
         Assert.assertEquals(attr.getIdentification(), DO_ATTRIBUTE_IDENTIFICATION);
      }
   }

   @Test
   public void testRemoveIdentifierLabel() {
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
   public void testSetPublicKeys() {
      IdentityObject identObj = getDatamodelFactory().createIdentityObject();
      Attribute attr = getDatamodelFactory().createAttribute(DefinedAttributeIdentification.PUBLIC_KEY.getURI(), "asdasdasd");
      identObj.setPublicKeys(attr);

      // should be the same
      Assert.assertEquals(identObj.getPublicKeys(), attr);
   }

   @Test
   public void testCreateIdentifierFromBytes() {
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
   public void testGetDefinedAttributePurpose() {
      String test = DefinedAttributePurpose.SYSTEM_ATTRIBUTE.toString();
      // what is this method good for?
      DefinedAttributePurpose purp = DefinedAttributePurpose.getDefinedAttributePurpose(test);
      // should be the same
      Assert.assertEquals(test, purp.toString());
   }

   @Test
   public void testGetVersionKind() {
      String test = DefinedVersionKind.VERSIONED.toString();
      // what is this method good for?
      DefinedVersionKind vers = DefinedVersionKind.getVersionKind(test);
      // should be the same
      Assert.assertEquals(test, vers.toString());
      // with error:
      vers = DefinedVersionKind.getVersionKind("wrongRepresenation");
      // should not be the same (UNKNOWN)
      Assert.assertNotSame(test, vers.toString());
   }

   @Test
   public void testgetDefinedAttributeIdentificationByURI() {
      String test = DefinedAttributeIdentification
            .getURIByAttributeIdentification(DefinedAttributeIdentification.AUTHORIZED_READERS);
      // what is this method good for?
      DefinedAttributeIdentification ident = DefinedAttributeIdentification.getDefinedAttributeIdentificationByURI(test);
      // should be the same
      Assert.assertEquals(test, ident.getURI());
   }

   @Test
   public void testGetSubattributesForPurpose() {
      InformationObject iObj = createDummyInformationObject(getDatamodelFactory());

      // Attributes should exist (dummy)
      if (iObj.getAttributes().size() > 0) {
         // dummy contains only one attribute
         Attribute attr = iObj.getAttributes().get(0);
         if (attr.getSubattributes().size() > 0) {
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
   public void testSetIdentificationTwice() {
      InformationObject iObj = createDummyInformationObject(getDatamodelFactory());
      String identif1 = DefinedAttributeIdentification.EVENT.getURI();
      String identif2 = DefinedAttributeIdentification.DESCRIPTION.getURI();
      List<Attribute> attrs = iObj.getAttribute(ATTRIBUTE_IDENTIFICATION);

      if (attrs.size() > 0) {
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

   @Test(expected = NetInfUncheckedException.class)
   public void testCopyOjectWithError() {
      getDatamodelFactory().copyObject(this);
      Assert.fail("Exception not thrown");
   }

   @Test
   public void testGetReaderIdentifiers() {
      // dummy
      InformationObject iObj = createDummyInformationObject(getDatamodelFactory());

      // without readers -> should be empty
      Assert.assertTrue(iObj.getReaderIdentifiers().isEmpty());

      Attribute attribute = getDatamodelFactory().createAttribute();
      Attribute subattribute = getDatamodelFactory().createAttribute();

      // reader - subattribute
      subattribute.setIdentification(DefinedAttributeIdentification.READER.getURI());
      subattribute
            .setValue("ni:HASH_OF_PK=560822d0da71369f1efe70fb96c7a9aa586b9836~HASH_OF_PK_IDENT=SHA1~VERSION_KIND=UNVERSIONED?http://rdf.netinf.org/2009/netinf-rdf/1.0/#public_key");
      subattribute.setAttributePurpose(DefinedAttributePurpose.SYSTEM_ATTRIBUTE.getAttributePurpose());

      // readers-list - attribute
      attribute.setIdentification(DefinedAttributeIdentification.AUTHORIZED_READERS.getURI());
      attribute.setValue("asd");
      attribute.setAttributePurpose(DefinedAttributePurpose.SYSTEM_ATTRIBUTE.getAttributePurpose());
      attribute.addSubattribute(subattribute);
      iObj.addAttribute(attribute);

      // with readers -> should not be empty
      Assert.assertFalse(iObj.getReaderIdentifiers().isEmpty());
   }

   @Test
   public void testDescribeIO() {
      InformationObject iObj = getDatamodelFactory().createInformationObject();
      String testEmpty = "a (general) Information Object that ";

      // empty description
      Assert.assertEquals(testEmpty, iObj.describe());

      // The identifier
      Identifier identifier = datamodelFactory.createIdentifier();
      iObj.setIdentifier(identifier);

      // still empty
      Assert.assertEquals(testEmpty, iObj.describe());

      // The label
      IdentifierLabel identLab = datamodelFactory.createIdentifierLabel();
      identLab.setLabelName(DefinedLabelName.VERSION_KIND.getLabelName());
      identifier.addIdentifierLabel(identLab);

      // label UNVERSIONED
      identLab.setLabelValue(DefinedVersionKind.UNVERSIONED.getStringRepresentation());
      // marked unversioned
      Assert.assertTrue(iObj.describe().contains("unversioned"));

      // label UNKNOWN
      identLab.setLabelValue(DefinedVersionKind.UNKNOWN.getStringRepresentation());
      // marked unknown
      Assert.assertTrue(iObj.describe().contains("versioned or not"));

      // label VERSIONED
      identLab.setLabelValue(DefinedVersionKind.VERSIONED.getStringRepresentation());
      // marked versioned
      Assert.assertTrue(iObj.describe().contains("is versioned"));

      // label Version Number
      IdentifierLabel versionNumber = getDatamodelFactory().createIdentifierLabel();
      versionNumber.setLabelName(DefinedLabelName.VERSION_NUMBER.getLabelName());
      versionNumber.setLabelValue("42");
      identifier.addIdentifierLabel(versionNumber);

      Assert.assertTrue(iObj.describe().contains("42"));

      // label Hash_of_PK
      IdentifierLabel hashOfPk = getDatamodelFactory().createIdentifierLabel();
      hashOfPk.setLabelName(DefinedLabelName.HASH_OF_PK.getLabelName());
      hashOfPk.setLabelValue("560822d0da71369f1efe70fb96c7a9aa586b9836");
      identifier.addIdentifierLabel(hashOfPk);

      Assert.assertTrue(iObj.describe().contains("56082..."));

      // label Hash_of_PK_Ident
      IdentifierLabel hashOfPkIdent = getDatamodelFactory().createIdentifierLabel();
      hashOfPkIdent.setLabelName(DefinedLabelName.HASH_OF_PK_IDENT.getLabelName());
      hashOfPkIdent.setLabelValue("SHA1");
      identifier.addIdentifierLabel(hashOfPkIdent);

      Assert.assertTrue(iObj.describe().contains("SHA1"));

      // label Unique_Label
      IdentifierLabel unique = getDatamodelFactory().createIdentifierLabel();
      unique.setLabelName(DefinedLabelName.UNIQUE_LABEL.getLabelName());
      unique.setLabelValue("un!qu314831");
      identifier.addIdentifierLabel(unique);

      Assert.assertTrue(iObj.describe().contains("un!qu314831"));

      // label UNDEFINED_LABEL
      IdentifierLabel undefined = getDatamodelFactory().createIdentifierLabel();
      undefined.setLabelName("UNDEFINED_LABEL");
      undefined.setLabelValue("undefined");
      identifier.addIdentifierLabel(undefined);

      Assert.assertTrue(iObj.describe().contains("undefined"));
   }

   @Test
   public void testIsVersioned() {
      Identifier identifier = datamodelFactory.createIdentifier();
      IdentifierLabel identLab = datamodelFactory.createIdentifierLabel();
      identLab.setLabelName(DefinedLabelName.VERSION_KIND.getLabelName());
      identifier.addIdentifierLabel(identLab);

      identLab.setLabelValue(DefinedVersionKind.VERSIONED.getStringRepresentation());
      Assert.assertTrue(identifier.isVersioned());
   }

   @Test
   public void testDescribeOtherIOs() {
      // Identifier
      Identifier identifier = datamodelFactory.createIdentifier();

      // -> PersonIdentity
      InformationObject pers = getDatamodelFactory().createPersonIdentityObject();
      pers.setIdentifier(identifier);
      Assert.assertTrue(pers.describe().contains("Person Identity Object"));

      // -> NodeIdentityObject
      NodeIdentityObject nodeId = getDatamodelFactory().createNodeIdentityObject();
      nodeId.setIdentifier(identifier);
      Assert.assertTrue(nodeId.describe().contains("Node Identity Object"));

      // -> EventServiceIdentity
      EventServiceIdentityObject event = getDatamodelFactory().createEventServiceIdentityObject();
      event.setIdentifier(identifier);
      Assert.assertTrue(event.describe().contains("Event Service Identity Object"));

      // -> GroupIdentity
      GroupIdentityObject group = getDatamodelFactory().createGroupIdentityObject();
      group.setIdentifier(identifier);
      Assert.assertTrue(group.describe().contains("Group Identity Object"));

      // -> SearchServiceIdentity
      SearchServiceIdentityObject search = getDatamodelFactory().createSearchServiceIdentityObject();
      search.setIdentifier(identifier);
      Assert.assertTrue(search.describe().contains("Search Service Identity Object"));

      // -> ResolutionServiceIdentity
      ResolutionServiceIdentityObject reso = getDatamodelFactory().createResolutionServiceIdentityObject();
      reso.setIdentifier(identifier);
      Assert.assertTrue(reso.describe().contains("Resolution Service Identity Object"));

      // -> IdentityObject
      IdentityObject identObj = getDatamodelFactory().createIdentityObject();
      identObj.setIdentifier(identifier);
      Assert.assertTrue(identObj.describe().contains("(general) Identity Object"));

      DataObject dataObj = getDatamodelFactory().createDataObject();
      dataObj.setIdentifier(identifier);
      Assert.assertTrue(dataObj.describe().contains("Data Object"));
   }

   @Test
   public void testDescribeServiceIdentityObject() {
      // Identifier
      Identifier identifier = datamodelFactory.createIdentifier();

      ServiceIdentityObject servId = new ServiceIdentityObjectImpl((DatamodelFactoryImpl) getDatamodelFactory());
      servId.setIdentifier(identifier);
      Assert.assertTrue(servId.describe().contains("Service Identity Object"));
   }

   @Test
   public void testGetAndSetOfDifferentIOs() {
      // -> PersonIdentity
      PersonIdentityObject pers = getDatamodelFactory().createPersonIdentityObject();

      // should be null here
      Assert.assertNull(pers.getName());
      Assert.assertNull(pers.getMailAddress());

      pers.setName("Chuck Norris");
      Assert.assertEquals("Chuck Norris", pers.getName());

      pers.setMailAddress("chuck@norris.de");
      Assert.assertEquals("chuck@norris.de", pers.getMailAddress());

      // -> GroupIdentity
      GroupIdentityObject group = getDatamodelFactory().createGroupIdentityObject();
      Attribute attr = getDatamodelFactory().createAttribute(DefinedAttributeIdentification.ENCRYPTED_GROUP_KEYS.getURI(),
            "test767678");

      Assert.assertNull(group.getEncryptedGroupKeys());
      group.setEncryptedGroupKeys(attr);
      Assert.assertEquals(group.getEncryptedGroupKeys(), attr);

      Attribute attr2 = getDatamodelFactory()
            .createAttribute(DefinedAttributeIdentification.MEMBERS_OF_GROUP.getURI(), "test124");

      Assert.assertNull(group.getMembers());
      group.setMembers(attr2);
      Assert.assertEquals(group.getMembers(), attr2);

      // -> SearchServiceIdentity
      SearchServiceIdentityObject search = getDatamodelFactory().createSearchServiceIdentityObject();

      Assert.assertNull(search.getName());
      search.setName("chuck");
      Assert.assertEquals(search.getName(), "chuck");

      Assert.assertNull(search.getDescription());
      search.setDescription("desc...");
      Assert.assertEquals(search.getDescription(), "desc...");

      // -> ResolutionServiceIdentity
      ResolutionServiceIdentityObject reso = getDatamodelFactory().createResolutionServiceIdentityObject();

      Assert.assertNull(reso.getName());
      reso.setName("chuck");
      Assert.assertEquals(reso.getName(), "chuck");

      Assert.assertNull(reso.getDescription());
      reso.setDescription("desc...");
      Assert.assertEquals(reso.getDescription(), "desc...");

      Assert.assertNull(reso.getDefaultPriority());
      reso.setDefaultPriority(42);
      Assert.assertEquals(reso.getDefaultPriority().toString(), "42");

      // -> IdentityObject
      IdentityObject identObj = getDatamodelFactory().createIdentityObject();

      PublicKey key = getPublicKey();
      identObj.setPublicMasterKey(key);
      Assert.assertEquals(identObj.getPublicMasterKey(), key);
   }

   @Test
   public void testAttributeToString() {
      Attribute attr = getDatamodelFactory().createAttribute();
      attr.setIdentification(DefinedAttributeIdentification.DESCRIPTION.getURI());
      attr.setAttributePurpose(DefinedAttributePurpose.SYSTEM_ATTRIBUTE.getAttributePurpose());
      attr.setValue("test");

      Attribute sub = getDatamodelFactory().createAttribute();
      sub.setIdentification(DefinedAttributeIdentification.DESCRIPTION.getURI());
      sub.setAttributePurpose(DefinedAttributePurpose.SYSTEM_ATTRIBUTE.getAttributePurpose());
      sub.setValue("subtest");

      attr.addSubattribute(sub);
      String toString = attr.toString();

      Assert.assertTrue(toString.contains("test"));
      Assert.assertTrue(toString.contains("description"));
      Assert.assertTrue(toString.contains("SYSTEM_ATTRIBUTE"));
      Assert.assertTrue(toString.contains("subtest"));
   }

   @Test
   public void testGetNullValueAttribute() {
      Attribute attr = getDatamodelFactory().createAttribute();
      Assert.assertEquals(null, attr.getValueRaw());
   }

   @Test
   public void testAddSubattributesSpecial() {
      // IO with Attribute
      InformationObject iObj = getDatamodelFactory().createInformationObject();
      Attribute attr = getDatamodelFactory().createAttribute(DefinedAttributeIdentification.DESCRIPTION.getURI(), "test");
      iObj.addAttribute(attr);

      // add it now as the subattribute of an other attribute
      Attribute otherAttr = getDatamodelFactory().createAttribute(DefinedAttributeIdentification.DESCRIPTION.getURI(), "test2");
      otherAttr.addSubattribute(attr);

      Assert.assertEquals(otherAttr, attr.getParentAttribute());
   }

   @Test
   public void testAddSubattributesSpecial2() {
      // attribute with subattribute
      Attribute attr = getDatamodelFactory().createAttribute(DefinedAttributeIdentification.DESCRIPTION.getURI(), "test2");
      Attribute otherAttr = getDatamodelFactory().createAttribute(DefinedAttributeIdentification.DESCRIPTION.getURI(), "test2");
      attr.addSubattribute(otherAttr);

      Attribute anOtherAttr = getDatamodelFactory().createAttribute(DefinedAttributeIdentification.DESCRIPTION.getURI(), "test3");
      anOtherAttr.addSubattribute(otherAttr);

      Assert.assertEquals(anOtherAttr, otherAttr.getParentAttribute());
   }

   @Test
   public void testRemoveSubAttribute() {
      Attribute attr = getDatamodelFactory().createAttribute(DefinedAttributeIdentification.DESCRIPTION.getURI(), "test2");
      Attribute otherAttr = getDatamodelFactory().createAttribute(DefinedAttributeIdentification.DESCRIPTION.getURI(), "test2");
      attr.addSubattribute(otherAttr);

      // and remove again
      attr.removeSubattribute(otherAttr.getIdentification());
   }

   @Test
   public void testIOtoString() {
      InformationObject iOb = getDatamodelFactory().createInformationObject();
      Attribute attr = getDatamodelFactory().createAttribute(DefinedAttributeIdentification.E_MAIL_ADDRESS.getURI(),
            "hans@meiser.de");
      iOb.addAttribute(attr);

      Assert.assertTrue(iOb.toString().contains("hans@meiser.de"));
   }

   @Test
   public void testcreateValidIdentifier() {
      // implies correctness ofisSyntacticallyValidIdentifier
      Identifier ifier = ValidCreator.createValidIdentifier(DefinedVersionKind.VERSIONED, getPublicKey(), "42");
      Assert.assertTrue(DatamodelUtils.isSyntacticallyValidIdentifier(ifier));
   }

   @Test
   public void testSecureAttributeInOverall() {
      InformationObject iObj = getDatamodelFactory().createInformationObject();
      Attribute attr = getDatamodelFactory().createAttribute(DefinedAttributeIdentification.E_MAIL_ADDRESS.getURI(),
            "hans@meiser.de");
      iObj.addAttribute(attr);
      ValidCreator.secureAttributeInOverall(attr, "testPathToKey");

      String desc = attr.toString();
      Assert.assertTrue(desc.contains("secured_in_overall") && desc.contains("true"));
   }

   @Test
   public void testIsAttributeNotSigned() {
      Attribute attr = getDatamodelFactory().createAttribute(DefinedAttributeIdentification.DESCRIPTION.getURI(), "test");
      Assert.assertFalse(ValidCreator.isAttributeSigned(attr));
      Assert.assertFalse(ValidCreator.isAttributeSignedIncorrectly(attr));
   }

   @Test
   public void testAddAuthorizedReader() {
      // to IO
      InformationObject iObj = getDatamodelFactory().createInformationObject();
      String string = "ni:HASH_OF_PK=Bar~UNIQUE_LABEL=Foo~name1=value1~name2=value2";
      Identifier identifier = getDatamodelFactory().createIdentifierFromString(string);

      IdentityObject ident = getDatamodelFactory().createIdentityObject();
      ident.setIdentifier(identifier);

      ValidCreator.addAuthorizedReader(iObj, ident);
      Assert.assertTrue(iObj.toString().contains(string));

      // to Attribute
      Attribute attr = getDatamodelFactory().createAttribute(DefinedAttributeIdentification.DESCRIPTION.getURI(), "test");
      ValidCreator.addAuthorizedReader(attr, ident);
      Assert.assertTrue(attr.toString().contains("test"));
   }

   @Test
   public void testaddAuthorizedWriterToIO() {
      InformationObject iObj = getDatamodelFactory().createInformationObject();
      Attribute attribute = getDatamodelFactory().createAttribute();
      attribute.setIdentification(DefinedAttributeIdentification.OWNER.getURI());
      attribute.setValue("test-writer-path");
      attribute.setAttributePurpose(DefinedAttributePurpose.SYSTEM_ATTRIBUTE.getAttributePurpose());
      iObj.addAttribute(attribute);

      String string = "ni:HASH_OF_PK=Bar~UNIQUE_LABEL=Foo~name1=123~name2=456";
      Identifier identifier = getDatamodelFactory().createIdentifierFromString(string);

      IdentityObject ident = getDatamodelFactory().createIdentityObject();
      ident.setIdentifier(identifier);
      ValidCreator.addAuthorizedWriterToIO(iObj, ident);
   }

   private PublicKey getPublicKey() {
      KeyPairGenerator keyPairGenerator;
      try {
         keyPairGenerator = KeyPairGenerator.getInstance("RSA");

         keyPairGenerator.initialize(1024);
         KeyPair pair = keyPairGenerator.generateKeyPair();

         return pair.getPublic();
      } catch (NoSuchAlgorithmException e) {
         return null;
      }
   }

   public static DataObject createDummyDataObject(DatamodelFactory datamodelFactory) {
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
