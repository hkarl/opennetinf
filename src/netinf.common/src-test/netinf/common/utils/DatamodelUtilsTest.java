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
package netinf.common.utils;

import java.util.Properties;

import netinf.common.communication.NetInfNodeConnection;
import netinf.common.communication.RemoteNodeConnection;
import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.DefinedAttributePurpose;
import netinf.common.datamodel.DefinedLabelName;
import netinf.common.datamodel.DefinedVersionKind;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.IdentifierLabel;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.attribute.Attribute;
import netinf.common.datamodel.attribute.DefinedAttributeIdentification;
import netinf.common.datamodel.impl.module.DatamodelImplModule;
import netinf.common.log.module.LogModule;
import netinf.common.security.impl.module.SecurityModule;

import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;

/**
 * @author PG NetInf 3
 */
public class DatamodelUtilsTest {

   public static final String PROPERTIES = "../configs/testing.properties";

   public static final String ATTRIBUTE_IDENTIFICATION = DefinedAttributeIdentification.DESCRIPTION.getURI();
   public static final String ATTRIBUTE_VALUE = "This is: the first = RDF Object";;
   public static final String ATTRIBUTE_PURPOSE = DefinedAttributePurpose.SYSTEM_ATTRIBUTE.getAttributePurpose();

   public static final String SUBATTRIBUTE_IDENTIFICATION = DefinedAttributeIdentification.PUBLIC_KEY.getURI();

   public static final String SUBATTRIBUTE_VALUE = " The value of subAttribute.";
   public static final String SUBATTRIBUTE_PURPOSE = DefinedAttributePurpose.USER_ATTRIBUTE.getAttributePurpose();

   public static final String SUBSUBATTRIBUTE_IDENTIFICATION = DefinedAttributeIdentification.OWNER.getURI();
   public static final String SUBSUBATTRIBUTE_VALUE = " The value of subSubAttribute.";
   public static final String SUBSUBATTRIBUTE_PURPOSE = DefinedAttributePurpose.LOCATOR_ATTRIBUTE.getAttributePurpose();

   public static final String IDENTIFIER_LABEL_1_NAME = DefinedLabelName.HASH_OF_PK.getLabelName();
   public static final String IDENTIFIER_LABEL_2_NAME = DefinedLabelName.HASH_OF_PK_IDENT.getLabelName();
   public static final String IDENTIFIER_LABEL_3_NAME = DefinedLabelName.VERSION_KIND.getLabelName();

   public static final String IDENTIFIER_LABEL_2_VALUE = "Paderborn";
   public static final String IDENTIFIER_LABEL_3_VALUE = DefinedVersionKind.VERSIONED.getStringRepresentation();
   public static final String IDENTIFIER_LABEL_1_VALUE = "1234321";

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

   @After
   public void tearDown() throws Exception {
   }

   @Test
   public void testCompareAttributes() {

      Attribute property1 = datamodelFactory.createAttribute(ATTRIBUTE_IDENTIFICATION, ATTRIBUTE_VALUE);
      Attribute property2 = datamodelFactory.createAttribute(ATTRIBUTE_IDENTIFICATION, ATTRIBUTE_VALUE);

      Attribute subProperty1 = datamodelFactory.createAttribute(SUBATTRIBUTE_IDENTIFICATION, SUBATTRIBUTE_VALUE);
      Attribute subProperty2 = datamodelFactory.createAttribute(SUBATTRIBUTE_IDENTIFICATION, SUBATTRIBUTE_VALUE);

      property1.setAttributePurpose(ATTRIBUTE_PURPOSE);
      property2.setAttributePurpose(ATTRIBUTE_PURPOSE);

      property1.setValue(ATTRIBUTE_VALUE);
      property2.setValue(ATTRIBUTE_VALUE);

      subProperty1.setAttributePurpose(SUBATTRIBUTE_PURPOSE);
      subProperty2.setAttributePurpose(SUBATTRIBUTE_PURPOSE);

      subProperty1.setValue(SUBATTRIBUTE_VALUE);
      subProperty2.setValue(SUBATTRIBUTE_VALUE);

      property1.addSubattribute(subProperty1);
      int subAttributeNotEqual = DatamodelUtils.compareAttributes(property1, property2);
      Assert.assertEquals(false, subAttributeNotEqual == 0);

      property2.addSubattribute(subProperty2);

      int expectedResult = DatamodelUtils.compareAttributes(property1, property2);
      Assert.assertEquals(0, expectedResult);

   }

   @Test
   public void testCompareIdentifierLabels() {

   }

   @Test
   public void testEqualAttributes() {

   }

   @Test
   public void testEqualIdentifiers() {

   }

   @Test
   public void testEqualIdentifierLabels() {

   }

   @Test
   public void testEqualInformationObjects() {

   }

   @Test
   public void testGetValueType() {

   }

   @Test
   public void testIsSyntacticallyValidIdentifier() {

      Identifier identifier = null;
      Boolean expectedResultNull = DatamodelUtils.isSyntacticallyValidIdentifier(identifier);
      Assert.assertEquals(false, expectedResultNull);

      identifier = getDatamodelFactory().createIdentifierFromString(
            "ni:name1=value1~UNIQUE_LABEL=Foo~name2=value2~HASH_OF_PK=Bar");

      IdentifierLabel identifierPKLabel = this.datamodelFactory.createIdentifierLabel();
      IdentifierLabel identifierIDENTLabel = this.datamodelFactory.createIdentifierLabel();
      IdentifierLabel identifierVERSIONLabel = this.datamodelFactory.createIdentifierLabel();

      identifierPKLabel.setLabelName(IDENTIFIER_LABEL_1_NAME);
      identifierPKLabel.setLabelValue(IDENTIFIER_LABEL_1_VALUE);

      identifierIDENTLabel.setLabelName(IDENTIFIER_LABEL_2_NAME);
      identifierIDENTLabel.setLabelValue(IDENTIFIER_LABEL_2_VALUE);

      identifierVERSIONLabel.setLabelName(IDENTIFIER_LABEL_3_NAME);
      identifierVERSIONLabel.setLabelValue(IDENTIFIER_LABEL_3_VALUE);

      Boolean expectedResultLabel = DatamodelUtils.isSyntacticallyValidIdentifier(identifier);
      Assert.assertEquals(false, expectedResultLabel);

      identifier.addIdentifierLabel(identifierPKLabel);
      identifier.addIdentifierLabel(identifierIDENTLabel);

      Boolean expectedResultNeedLabel = DatamodelUtils.isSyntacticallyValidIdentifier(identifier);
      Assert.assertEquals(false, expectedResultNeedLabel);

      identifier.addIdentifierLabel(identifierVERSIONLabel);

      Boolean expectedResult = DatamodelUtils.isSyntacticallyValidIdentifier(identifier);
      Assert.assertEquals(true, expectedResult);
   }

   @Test
   public void testIsSyntacticallyValidAttribute() {

      Attribute property = null;
      Attribute subProperty = null;
      InformationObject testIO = this.datamodelFactory.createInformationObject();

      Boolean attributeNull = DatamodelUtils.isSyntacticallyValidAttribute(subProperty, property, testIO);
      Assert.assertEquals(false, attributeNull);

      property = datamodelFactory.createAttribute(SUBSUBATTRIBUTE_IDENTIFICATION, ATTRIBUTE_VALUE);
      subProperty = datamodelFactory.createAttribute(SUBSUBATTRIBUTE_IDENTIFICATION, SUBATTRIBUTE_VALUE);

      Boolean purposeNull = DatamodelUtils.isSyntacticallyValidAttribute(subProperty, property, testIO);
      Assert.assertEquals(false, purposeNull);

      property.setAttributePurpose(ATTRIBUTE_PURPOSE);
      subProperty.setAttributePurpose(SUBATTRIBUTE_PURPOSE);

      Boolean valueNull = DatamodelUtils.isSyntacticallyValidAttribute(subProperty, property, testIO);
      Assert.assertEquals(false, valueNull);

      property.setValue(ATTRIBUTE_VALUE);
      subProperty.setValue(SUBATTRIBUTE_VALUE);

      Boolean idenNull = DatamodelUtils.isSyntacticallyValidAttribute(subProperty, property, testIO);
      Assert.assertEquals(false, idenNull);

      property.setIdentification(ATTRIBUTE_IDENTIFICATION);
      subProperty.setIdentification(SUBATTRIBUTE_IDENTIFICATION);

      Boolean parentNull = DatamodelUtils.isSyntacticallyValidAttribute(subProperty, property, testIO);
      Assert.assertEquals(false, parentNull);

      property.addSubattribute(subProperty);
      testIO = null;

      Boolean right = DatamodelUtils.isSyntacticallyValidAttribute(subProperty, property, testIO);
      Assert.assertEquals(true, right);

      Attribute subSubProperty = datamodelFactory.createAttribute(SUBSUBATTRIBUTE_IDENTIFICATION, SUBSUBATTRIBUTE_VALUE);

      subProperty.addSubattribute(subSubProperty);
      subSubProperty = null;

      Boolean subSubPropertyNull = DatamodelUtils.isSyntacticallyValidAttribute(subProperty, property, testIO);
      Assert.assertEquals(false, subSubPropertyNull);

      subSubProperty = datamodelFactory.createAttribute(DefinedAttributeIdentification.DESCRIPTION.getURI(), SUBATTRIBUTE_VALUE);

      Boolean subSubPropertyValid = DatamodelUtils.isSyntacticallyValidAttribute(subProperty, property, testIO);
      Assert.assertEquals(false, subSubPropertyValid);

      subSubProperty.setIdentification(SUBSUBATTRIBUTE_IDENTIFICATION);
      subSubProperty.setValue(SUBSUBATTRIBUTE_VALUE);
      subSubProperty.setAttributePurpose(SUBSUBATTRIBUTE_PURPOSE);

      testIO = this.datamodelFactory.createInformationObject();
      testIO.addAttribute(subProperty);
      testIO.addAttribute(property);

      property = datamodelFactory.createAttribute(SUBSUBATTRIBUTE_IDENTIFICATION, ATTRIBUTE_VALUE);
      subProperty = datamodelFactory.createAttribute(SUBSUBATTRIBUTE_IDENTIFICATION, SUBATTRIBUTE_VALUE);
      subSubProperty = datamodelFactory.createAttribute(SUBSUBATTRIBUTE_IDENTIFICATION, SUBSUBATTRIBUTE_VALUE);

      subSubProperty.setValue(SUBSUBATTRIBUTE_VALUE);
      subSubProperty.setAttributePurpose(SUBSUBATTRIBUTE_PURPOSE);
      subSubProperty.setIdentification(SUBSUBATTRIBUTE_IDENTIFICATION);

      subProperty.addSubattribute(subSubProperty);
      property.addSubattribute(subProperty);
      testIO.addAttribute(subSubProperty);

      Boolean expectedResult = DatamodelUtils.isSyntacticallyValidAttribute(subProperty, property, testIO);
      Assert.assertEquals(false, expectedResult);

   }

   @Test
   public void testGetIdentifierLabels() {

   }

   @Test
   public void testIsIdentifierVersioned() {

      Identifier identifier = getDatamodelFactory().createIdentifierFromString(
            "ni:name1=value1~UNIQUE_LABEL=Foo~name2=value2~HASH_OF_PK=Bar");
      IdentifierLabel identifierVERSIONLabel = this.datamodelFactory.createIdentifierLabel();

      identifierVERSIONLabel.setLabelName(IDENTIFIER_LABEL_3_NAME);
      identifierVERSIONLabel.setLabelValue(IDENTIFIER_LABEL_3_VALUE);

      Boolean versionLabelNull = DatamodelUtils.isIdentifierVersioned(identifier);
      Assert.assertEquals(false, versionLabelNull);

      identifier.addIdentifierLabel(identifierVERSIONLabel);

      Boolean expectedResult = DatamodelUtils.isIdentifierVersioned(identifier);
      Assert.assertEquals(true, expectedResult);

   }

   @Test
   public void testIsSyntacticallyValidIO() {

      /*
       * String APPLICATION_PROPERTIES = "../configs/createIOs.properties"; Injector injector; RemoteNodeConnection
       * remoteNodeConnection; DatamodelFactory datamodelFactory; IdentityManager identityManager; SecuredApplicationModule
       * createIOsModule = new SecuredApplicationModule(APPLICATION_PROPERTIES); injector = Guice.createInjector(createIOsModule);
       * final Properties properties = Utils.loadProperties(APPLICATION_PROPERTIES); remoteNodeConnection = (RemoteNodeConnection)
       * injector.getInstance(NetInfNodeConnection.class); remoteNodeConnection.setSerializeFormat(SerializeFormat.RDF);
       * datamodelFactory = injector.getInstance(DatamodelFactory.class); identityManager =
       * injector.getInstance(IdentityManager.class); identityManager.setFilePath("../configs/Identities/privateKeyFile.pkf"); //
       * IdentityObject identity2 = ValidCreator.createValidIdentityObject(Utils.stringToPublicKey(properties //
       * .getProperty("publicKeyIdentity2"))); //remoteNodeConnection.putIO(identity2); PublicKey pk = Utils.stringToPublicKey(
       * properties.getProperty("publicKeyIdentity2")); String pkString = Utils.objectToString(pk); String
       * SUBATTRIBUTE_IDENTIFICATION = pkString;
       */
      InformationObject testIO = null;

      Boolean ioNull = DatamodelUtils.isSyntacticallyValidIO(testIO);
      Assert.assertEquals(false, ioNull);

      testIO = this.datamodelFactory.createInformationObject();

      Identifier identifier = getDatamodelFactory().createIdentifierFromString(
            "ni:name1=value1~UNIQUE_LABEL=Foo~name2=value2~HASH_OF_PK=Bar");
      IdentifierLabel identifierPKLabel = this.datamodelFactory.createIdentifierLabel();
      IdentifierLabel identifierIDENTLabel = this.datamodelFactory.createIdentifierLabel();
      IdentifierLabel identifierVERSIONLabel = this.datamodelFactory.createIdentifierLabel();

      identifierPKLabel.setLabelName(IDENTIFIER_LABEL_1_NAME);
      identifierPKLabel.setLabelValue(IDENTIFIER_LABEL_1_VALUE);

      identifierIDENTLabel.setLabelName(IDENTIFIER_LABEL_2_NAME);
      identifierIDENTLabel.setLabelValue(IDENTIFIER_LABEL_2_VALUE);

      identifierVERSIONLabel.setLabelName(IDENTIFIER_LABEL_3_NAME);
      identifierVERSIONLabel.setLabelValue(IDENTIFIER_LABEL_3_VALUE);

      Attribute property = datamodelFactory.createAttribute(ATTRIBUTE_IDENTIFICATION, ATTRIBUTE_VALUE);
      Attribute subProperty = datamodelFactory.createAttribute(SUBATTRIBUTE_IDENTIFICATION, SUBATTRIBUTE_VALUE);

      property.setAttributePurpose(ATTRIBUTE_PURPOSE);
      subProperty.setAttributePurpose(SUBATTRIBUTE_PURPOSE);

      property.setValue(ATTRIBUTE_VALUE);
      subProperty.setValue(SUBATTRIBUTE_VALUE);

      property.setIdentification(DefinedAttributeIdentification.OWNER.getURI());
      // subProperty.setAttributePurpose("www.google.de");
      subProperty.setIdentification(DefinedAttributeIdentification.PUBLIC_KEY.getURI());

      property.addSubattribute(subProperty);

      identifier.addIdentifierLabel(identifierPKLabel);
      identifier.addIdentifierLabel(identifierIDENTLabel);
      identifier.addIdentifierLabel(identifierVERSIONLabel);

      Boolean idenNull = DatamodelUtils.isSyntacticallyValidIO(testIO);
      Assert.assertEquals(false, idenNull);

      testIO.setIdentifier(identifier);

      testIO.addAttribute(subProperty);
      testIO.addAttribute(property);

      Boolean attributeNull = DatamodelUtils.isSyntacticallyValidIO(testIO);
      Assert.assertEquals(false, attributeNull);

      property.setAttributePurpose(ATTRIBUTE_PURPOSE);
      subProperty.setAttributePurpose(SUBATTRIBUTE_PURPOSE);

      property.setValue(ATTRIBUTE_VALUE);
      subProperty.setValue(SUBATTRIBUTE_VALUE);

      Boolean expectedResult = DatamodelUtils.isSyntacticallyValidIO(testIO);
      Assert.assertEquals(false, expectedResult);
   }

   @Test
   public void testIsValidIdentifierString() {

   }

   @Test
   public void testIdentifierFromIdentity() {
      String id = "1234567";
      DatamodelUtils dataModel = new DatamodelUtils();
      dataModel.identifierFromIdentity(id);
   }

   @Test
   public void testToStringInformationObject() {

   }

   @Test
   public void testToStringAttribute() {

   }

   @Test
   public void testToStringIdentifier() {

   }

   public static void setDatamodelFactory(DatamodelFactory datamodelFactory) {
      DatamodelUtilsTest.datamodelFactory = datamodelFactory;
   }

   public static DatamodelFactory getDatamodelFactory() {
      return datamodelFactory;
   }
}
