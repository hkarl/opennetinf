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
package netinf.common.datamodel.rdf;

import java.util.List;
import java.util.Properties;

import junit.framework.Assert;
import netinf.common.communication.NetInfNodeConnection;
import netinf.common.communication.RemoteNodeConnection;
import netinf.common.communication.SerializeFormat;
import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.DatamodelTest;
import netinf.common.datamodel.DefinedAttributePurpose;
import netinf.common.datamodel.DefinedLabelName;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.NetInfObjectWrapper;
import netinf.common.datamodel.attribute.Attribute;
import netinf.common.datamodel.attribute.DefinedAttributeIdentification;
import netinf.common.datamodel.impl.DatamodelFactoryImpl;
import netinf.common.datamodel.impl.InformationObjectImpl;
import netinf.common.datamodel.rdf.attribute.AttributeRdf;
import netinf.common.datamodel.rdf.identity.EventServiceIdentityObjectRdf;
import netinf.common.datamodel.rdf.identity.GroupIdentityObjectRdf;
import netinf.common.datamodel.rdf.identity.NodeIdentityObjectRdf;
import netinf.common.datamodel.rdf.identity.PersonIdentityObjectRdf;
import netinf.common.datamodel.rdf.identity.ResolutionServiceIdentityObjectRdf;
import netinf.common.datamodel.rdf.identity.SearchServiceIdentityObjectRdf;
import netinf.common.datamodel.rdf.module.DatamodelRdfModule;
import netinf.common.exceptions.NetInfCheckedException;
import netinf.common.log.module.LogModule;
import netinf.common.security.impl.module.SecurityModule;
import netinf.common.utils.DatamodelUtils;
import netinf.common.utils.Utils;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

/**
 * RDF specific tests
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class RdfDatamodelTest extends DatamodelTest {

   @BeforeClass
   public static void setUpTest() {
      System.out.println("DatamodelRdfTest is executed");
      final Properties properties = Utils.loadProperties(PROPERTIES);
      Injector injector = Guice.createInjector(new LogModule(properties), new SecurityModule(), new DatamodelRdfModule(),
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
   public void testCreateAttribute() {
      InformationObject informationObject = createDummyInformationObject(getDatamodelFactory());
      Attribute attribute = informationObject.getSingleAttribute(ATTRIBUTE_IDENTIFICATION);
      Attribute subattribute = attribute.getSingleSubattribute(SUBATTRIBUTE_IDENTIFICATION);

      Resource ioResource = ((InformationObjectRdf) informationObject).getResource();
      Resource attributeResource = ((AttributeRdf) attribute).getResource();
      Resource subattributeResource = ((AttributeRdf) subattribute).getResource();

      // Check whether pointer was set correctly
      List<Statement> pointerToIOList = ioResource.getModel()
            .listStatements(null, DatamodelFactoryRdf.getProperty(DefinedRdfNames.POINTER_TO_IO), ioResource).toList();
      Assert.assertEquals(1, pointerToIOList.size());

      // Check whether attribute was bound correctly
      List<Statement> attributeList = ioResource.getModel()
            .listStatements(ioResource, DatamodelFactoryRdf.getProperty(ATTRIBUTE_IDENTIFICATION), attributeResource).toList();

      Assert.assertEquals(1, attributeList.size());

      // Check attributePurpose
      List<Statement> attributePurposeList = attributeResource.listProperties(
            DatamodelFactoryRdf.getProperty(DefinedRdfNames.ATTRIBUTE_PURPUSE)).toList();

      Assert.assertEquals(1, attributePurposeList.size());
      Assert.assertEquals(true, attributePurposeList.get(0).getObject().isLiteral());
      Assert.assertEquals(ATTRIBUTE_PURPOSE, ((Literal) attributePurposeList.get(0).getObject()).getString());

      // Check attributeValue
      List<Statement> attributeValueList = attributeResource.listProperties(
            DatamodelFactoryRdf.getProperty(DefinedRdfNames.ATTRIBUTE_VALUE)).toList();
      Assert.assertEquals(1, attributeValueList.size());
      Assert.assertEquals(true, attributeValueList.get(0).getObject().isLiteral());
      Assert.assertEquals(attribute.getValueRaw(), ((Literal) attributeValueList.get(0).getObject()).getString());

      // Check whether subattribute was bould correctly
      List<Statement> subattributeList = attributeResource
            .getModel()
            .listStatements(attributeResource, DatamodelFactoryRdf.getProperty(SUBATTRIBUTE_IDENTIFICATION), subattributeResource)
            .toList();

      Assert.assertEquals(1, subattributeList.size());

      // Check subattributePurpose
      List<Statement> subattributePurposeList = subattributeResource.listProperties(
            DatamodelFactoryRdf.getProperty(DefinedRdfNames.ATTRIBUTE_PURPUSE)).toList();

      Assert.assertEquals(1, subattributePurposeList.size());
      Assert.assertEquals(true, subattributePurposeList.get(0).getObject().isLiteral());
      Assert.assertEquals(SUBATTRIBUTE_PURPOSE, ((Literal) subattributePurposeList.get(0).getObject()).getString());

      // Check subattributeValue
      List<Statement> subattributeValueList = subattributeResource.listProperties(
            DatamodelFactoryRdf.getProperty(DefinedRdfNames.ATTRIBUTE_VALUE)).toList();
      Assert.assertEquals(1, subattributeValueList.size());
      Assert.assertEquals(true, subattributeValueList.get(0).getObject().isLiteral());
      Assert.assertEquals(subattribute.getValueRaw(), ((Literal) subattributeValueList.get(0).getObject()).getString());
   }

   @Test
   public void testAddAndRemoveAttribute() {
      InformationObjectRdf informationObject = (InformationObjectRdf) createDummyInformationObject(getDatamodelFactory());
      AttributeRdf attribute = (AttributeRdf) informationObject.getSingleAttribute(ATTRIBUTE_IDENTIFICATION);
      AttributeRdf subattribute = (AttributeRdf) attribute.getSingleSubattribute(SUBATTRIBUTE_IDENTIFICATION);

      // So that we can thereafter access these pieces of information.
      Resource ioResource = informationObject.getResource();
      Resource attributeResource = attribute.getResource();
      Resource subattributeResource = subattribute.getResource();

      informationObject.removeAttribute(attribute);

      // The attribute must have exactly the same values like before
      Assert.assertEquals(null, attribute.getResource());
      Assert.assertEquals(ATTRIBUTE_PURPOSE, attribute.getAttributePurpose());
      Assert.assertEquals(ATTRIBUTE_VALUE, attribute.getValue(String.class));
      Assert.assertEquals(ATTRIBUTE_IDENTIFICATION, attribute.getIdentification());

      // Subattribute similarly
      Assert.assertEquals(null, subattribute.getResource());
      Assert.assertEquals(SUBATTRIBUTE_PURPOSE, subattribute.getAttributePurpose());
      Assert.assertEquals(SUBATTRIBUTE_VALUE, subattribute.getValue(Integer.class));
      Assert.assertEquals(SUBATTRIBUTE_IDENTIFICATION, subattribute.getIdentification());

      // Check whether resource is completely empty
      List<Statement> attributeResourceProperties = attributeResource.listProperties().toList();
      Assert.assertEquals(0, attributeResourceProperties.size());

      List<Statement> subattributeResourceProperties = subattributeResource.listProperties().toList();
      Assert.assertEquals(0, subattributeResourceProperties.size());

      List<Statement> ioResourceProperties = ioResource.listProperties().toList();
      Assert.assertEquals(0, ioResourceProperties.size());

      // The whole model should have only two single statement (for transported IO)
      Model model = ioResource.getModel();
      List<Statement> allStatements = model.listStatements().toList();
      Assert.assertEquals(2, allStatements.size());
   }

   @Test
   public void testCreationFromModel() throws NetInfCheckedException {
      InformationObject tmpInformationObject = createDummyInformationObject(getDatamodelFactory());

      byte[] serializeToBytes = tmpInformationObject.serializeToBytes();

      InformationObject informationObject = getDatamodelFactory().createInformationObjectFromBytes(serializeToBytes);
      Attribute attribute = informationObject.getSingleAttribute(ATTRIBUTE_IDENTIFICATION);
      Attribute subattribute = attribute.getSingleSubattribute(SUBATTRIBUTE_IDENTIFICATION);

      Resource ioResource = ((InformationObjectRdf) informationObject).getResource();
      Resource attributeResource = ((AttributeRdf) attribute).getResource();
      Resource subattributeResource = ((AttributeRdf) subattribute).getResource();

      // Check whether pointer was set correctly
      List<Statement> pointerToIOList = ioResource.getModel()
            .listStatements(null, DatamodelFactoryRdf.getProperty(DefinedRdfNames.POINTER_TO_IO), ioResource).toList();
      Assert.assertEquals(1, pointerToIOList.size());

      // Check whether attribute was bound correctly
      List<Statement> attributeList = ioResource.getModel()
            .listStatements(ioResource, DatamodelFactoryRdf.getProperty(ATTRIBUTE_IDENTIFICATION), attributeResource).toList();

      Assert.assertEquals(1, attributeList.size());

      // Check attributePurpose
      List<Statement> attributePurposeList = attributeResource.listProperties(
            DatamodelFactoryRdf.getProperty(DefinedRdfNames.ATTRIBUTE_PURPUSE)).toList();

      Assert.assertEquals(1, attributePurposeList.size());
      Assert.assertEquals(true, attributePurposeList.get(0).getObject().isLiteral());
      Assert.assertEquals(ATTRIBUTE_PURPOSE, ((Literal) attributePurposeList.get(0).getObject()).getString());

      // Check attributeValue
      List<Statement> attributeValueList = attributeResource.listProperties(
            DatamodelFactoryRdf.getProperty(DefinedRdfNames.ATTRIBUTE_VALUE)).toList();
      Assert.assertEquals(1, attributeValueList.size());
      Assert.assertEquals(true, attributeValueList.get(0).getObject().isLiteral());
      Assert.assertEquals(attribute.getValueRaw(), ((Literal) attributeValueList.get(0).getObject()).getString());

      // Check whether subattribute was bould correctly
      List<Statement> subattributeList = attributeResource
            .getModel()
            .listStatements(attributeResource, DatamodelFactoryRdf.getProperty(SUBATTRIBUTE_IDENTIFICATION), subattributeResource)
            .toList();

      Assert.assertEquals(1, subattributeList.size());

      // Check subattributePurpose
      List<Statement> subattributePurposeList = subattributeResource.listProperties(
            DatamodelFactoryRdf.getProperty(DefinedRdfNames.ATTRIBUTE_PURPUSE)).toList();

      Assert.assertEquals(1, subattributePurposeList.size());
      Assert.assertEquals(true, subattributePurposeList.get(0).getObject().isLiteral());
      Assert.assertEquals(SUBATTRIBUTE_PURPOSE, ((Literal) subattributePurposeList.get(0).getObject()).getString());

      // Check subattributeValue
      List<Statement> subattributeValueList = subattributeResource.listProperties(
            DatamodelFactoryRdf.getProperty(DefinedRdfNames.ATTRIBUTE_VALUE)).toList();
      Assert.assertEquals(1, subattributeValueList.size());
      Assert.assertEquals(true, subattributeValueList.get(0).getObject().isLiteral());
      Assert.assertEquals(subattribute.getValueRaw(), ((Literal) subattributeValueList.get(0).getObject()).getString());

      // Test identifier
      IdentifierRdf identifier = (IdentifierRdf) informationObject.getIdentifier();
      Assert.assertSame(informationObject, identifier.getInformationObject());

      IdentifierLabelRdf identifierLabel1 = (IdentifierLabelRdf) identifier.getIdentifierLabel(IDENTIFIER_LABEL_1_NAME);
      Assert.assertSame(identifier, identifierLabel1.getIdentifier());
      Assert.assertEquals(IDENTIFIER_LABEL_1_VALUE, identifierLabel1.getLabelValue());
      Assert.assertEquals(IDENTIFIER_LABEL_1_NAME, identifierLabel1.getLabelName());

      IdentifierLabelRdf identifierLabel2 = (IdentifierLabelRdf) identifier.getIdentifierLabel(IDENTIFIER_LABEL_2_NAME);
      Assert.assertSame(identifier, identifierLabel2.getIdentifier());
      Assert.assertEquals(IDENTIFIER_LABEL_2_VALUE, identifierLabel2.getLabelValue());
      Assert.assertEquals(IDENTIFIER_LABEL_2_NAME, identifierLabel2.getLabelName());
   }

   @Test
   public void testWiringOfIdentifier() {
      InformationObjectRdf informationObject = (InformationObjectRdf) createDummyInformationObject(getDatamodelFactory());

      IdentifierRdf identifier = (IdentifierRdf) informationObject.getIdentifier();
      Assert.assertSame(informationObject, identifier.getInformationObject());

      IdentifierLabelRdf identifierLabel1 = (IdentifierLabelRdf) identifier.getIdentifierLabel(IDENTIFIER_LABEL_1_NAME);
      Assert.assertSame(identifier, identifierLabel1.getIdentifier());
      Assert.assertEquals(IDENTIFIER_LABEL_1_VALUE, identifierLabel1.getLabelValue());
      Assert.assertEquals(IDENTIFIER_LABEL_1_NAME, identifierLabel1.getLabelName());

      IdentifierLabelRdf identifierLabel2 = (IdentifierLabelRdf) identifier.getIdentifierLabel(IDENTIFIER_LABEL_2_NAME);
      Assert.assertSame(identifier, identifierLabel2.getIdentifier());
      Assert.assertEquals(IDENTIFIER_LABEL_2_VALUE, identifierLabel2.getLabelValue());
      Assert.assertEquals(IDENTIFIER_LABEL_2_NAME, identifierLabel2.getLabelName());
   }

   @Test
   public void testRemovingIdentifier() {
      InformationObjectRdf informationObject = (InformationObjectRdf) createDummyInformationObject(getDatamodelFactory());
      IdentifierRdf oldIdentifier = (IdentifierRdf) informationObject.getIdentifier();
      informationObject.setIdentifier(null);

      Resource resource = informationObject.getResource();

      Assert.assertNull(resource.getURI());
      Assert.assertNull(oldIdentifier.getInformationObject());
      Assert.assertNull(informationObject.getIdentifier());

      // Look whether serialization still works (correctly defined URIs)
      informationObject.serializeToBytes();
   }

   @Test
   public void testChangingIdentifierLabel() {
      String newLabelValue = "xxx";

      InformationObjectRdf informationObject = (InformationObjectRdf) createDummyInformationObject(getDatamodelFactory());
      IdentifierRdf identifier = (IdentifierRdf) informationObject.getIdentifier();

      IdentifierLabelRdf identifierLabel1 = (IdentifierLabelRdf) identifier.getIdentifierLabel(IDENTIFIER_LABEL_1_NAME);
      IdentifierLabelRdf identifierLabel2 = (IdentifierLabelRdf) identifier.getIdentifierLabel(IDENTIFIER_LABEL_2_NAME);

      identifierLabel1.setLabelValue(newLabelValue);
      Assert.assertEquals(informationObject.getResource().getURI(), DatamodelUtils.toStringIdentifier(identifier));

      identifierLabel1.setLabelName(DefinedLabelName.HASH_OF_PK.getLabelName());
      Assert.assertEquals(informationObject.getResource().getURI(), DatamodelUtils.toStringIdentifier(identifier));

      identifier.removeIdentifierLabel(identifierLabel2);
      Assert.assertEquals(informationObject.getResource().getURI(), DatamodelUtils.toStringIdentifier(identifier));
      Assert.assertEquals(identifier.getIdentifierLabel(IDENTIFIER_LABEL_2_NAME), null);
      Assert.assertEquals(identifierLabel2.getIdentifier(), null);

      // Remove the identifier, and go on with changing
      informationObject.setIdentifier(null);
      identifierLabel1.setLabelValue(IDENTIFIER_LABEL_1_VALUE);
      identifierLabel1.setLabelName(IDENTIFIER_LABEL_1_NAME);
      Assert.assertEquals(informationObject.getResource().getURI(), null);

      // Look whether serialization still works (correctly defined URIs)
      informationObject.serializeToBytes();
   }

   @Test
   public void testMovingIdentifierLabel() {
      InformationObjectRdf informationObject1 = (InformationObjectRdf) createDummyInformationObject(getDatamodelFactory());
      InformationObjectRdf informationObject2 = (InformationObjectRdf) createDummyInformationObject(getDatamodelFactory());

      IdentifierRdf identifier1 = (IdentifierRdf) informationObject1.getIdentifier();
      IdentifierRdf identifier2 = (IdentifierRdf) informationObject2.getIdentifier();

      IdentifierLabelRdf identifierLabel11 = (IdentifierLabelRdf) identifier1.getIdentifierLabel(IDENTIFIER_LABEL_1_NAME);
      IdentifierLabelRdf identifierLabel21 = (IdentifierLabelRdf) identifier2.getIdentifierLabel(IDENTIFIER_LABEL_1_NAME);

      identifier1.removeIdentifierLabel(IDENTIFIER_LABEL_1_NAME);
      Assert.assertEquals(null, identifierLabel11.getIdentifier());

      identifier1.addIdentifierLabel(identifierLabel21);
      Assert.assertEquals(1, identifier2.getIdentifierLabels().size());
      Assert.assertNull(identifier2.getIdentifierLabel(IDENTIFIER_LABEL_1_NAME));
      Assert.assertSame(identifierLabel21.getIdentifier(), identifier1);
      Assert.assertSame(identifierLabel21, identifier1.getIdentifierLabel(IDENTIFIER_LABEL_1_NAME));

      // Look whether serialization still works (correctly defined URIs)
      informationObject1.serializeToBytes();
      // Look whether serialization still works (correctly defined URIs)
      informationObject2.serializeToBytes();
   }

   @Test
   public void testMovingIdentifier() {
      String newLabelValue = "xxx";

      InformationObjectRdf informationObject1 = (InformationObjectRdf) createDummyInformationObject(getDatamodelFactory());
      InformationObjectRdf informationObject2 = (InformationObjectRdf) createDummyInformationObject(getDatamodelFactory());

      // Change the first identifier so that we can distinguish the string from the second one.
      IdentifierRdf identifier1 = (IdentifierRdf) informationObject1.getIdentifier();
      identifier1.getIdentifierLabel(IDENTIFIER_LABEL_1_NAME).setLabelValue(newLabelValue);

      IdentifierRdf identifier2 = (IdentifierRdf) informationObject2.getIdentifier();
      String identifier2String = DatamodelUtils.toStringIdentifier(identifier2);

      informationObject2.setIdentifier(identifier1);

      // Now, IO1 is not allowed to have an identifier
      Assert.assertEquals(null, informationObject1.getIdentifier());
      Assert.assertEquals(null, informationObject1.getResource().getURI());

      // IO2 must have i1
      Assert.assertSame(identifier1, informationObject2.getIdentifier());
      Assert.assertEquals(DatamodelUtils.toStringIdentifier(identifier1), informationObject2.getResource().getURI());
      Assert.assertEquals(null, identifier2.getInformationObject());

      // Finally check, whether the identifier2 is still valid
      Assert.assertEquals(identifier2String, DatamodelUtils.toStringIdentifier(identifier2));
   }

   @Test
   @Override
   public void testDifferentInformationObjectTypes() {
      InformationObject object = getDatamodelFactory().createDataObject();
      NetInfObjectWrapper desObject = getDatamodelFactory().createFromBytes(object.serializeToBytes());
      Assert.assertEquals(DataObjectRdf.class.getCanonicalName(), desObject.getClass().getCanonicalName());

      object = getDatamodelFactory().createEventServiceIdentityObject();
      desObject = getDatamodelFactory().createFromBytes(object.serializeToBytes());
      Assert.assertEquals(EventServiceIdentityObjectRdf.class.getCanonicalName(), desObject.getClass().getCanonicalName());

      object = getDatamodelFactory().createGroupIdentityObject();
      desObject = getDatamodelFactory().createFromBytes(object.serializeToBytes());
      Assert.assertEquals(GroupIdentityObjectRdf.class.getCanonicalName(), desObject.getClass().getCanonicalName());

      object = getDatamodelFactory().createNodeIdentityObject();
      desObject = getDatamodelFactory().createFromBytes(object.serializeToBytes());
      Assert.assertEquals(NodeIdentityObjectRdf.class.getCanonicalName(), desObject.getClass().getCanonicalName());

      object = getDatamodelFactory().createPersonIdentityObject();
      desObject = getDatamodelFactory().createFromBytes(object.serializeToBytes());
      Assert.assertEquals(PersonIdentityObjectRdf.class.getCanonicalName(), desObject.getClass().getCanonicalName());

      object = getDatamodelFactory().createSearchServiceIdentityObject();
      desObject = getDatamodelFactory().createFromBytes(object.serializeToBytes());
      Assert.assertEquals(SearchServiceIdentityObjectRdf.class.getCanonicalName(), desObject.getClass().getCanonicalName());

      object = getDatamodelFactory().createResolutionServiceIdentityObject();
      desObject = getDatamodelFactory().createFromBytes(object.serializeToBytes());
      Assert.assertEquals(ResolutionServiceIdentityObjectRdf.class.getCanonicalName(), desObject.getClass().getCanonicalName());
   }

   @Test
   public void testAttributeSerialization() {
      InformationObject informationObject = createDummyInformationObject(getDatamodelFactory());
      Attribute attribute = informationObject.getSingleAttribute(ATTRIBUTE_IDENTIFICATION);

      byte[] serializeToBytes = attribute.serializeToBytes();
      NetInfObjectWrapper attributeFromBytes = getDatamodelFactory().createFromBytes(serializeToBytes);

      Assert.assertEquals(attribute, attributeFromBytes);
   }

   @Test
   public void testIdentifierSerialization() {
      InformationObject informationObject = createDummyInformationObject(getDatamodelFactory());
      Identifier identifier = informationObject.getIdentifier();

      byte[] serializeToBytes = identifier.serializeToBytes();
      NetInfObjectWrapper identifierFromBytes = getDatamodelFactory().createFromBytes(serializeToBytes);

      Assert.assertEquals(identifier, identifierFromBytes);
   }

   @Test
   public void testCopyToImplAndToRdfBack() {
      InformationObjectRdf rdfIO1 = (InformationObjectRdf) createDummyInformationObject(getDatamodelFactory());
      DatamodelFactoryImpl datamodelFactoryImpl = new DatamodelFactoryImpl();
      InformationObjectImpl implIO1 = (InformationObjectImpl) datamodelFactoryImpl.copyObject((InformationObject) rdfIO1);

      Assert.assertTrue(DatamodelUtils.equalInformationObjects(rdfIO1, implIO1));

      InformationObject rdfIO2 = getDatamodelFactory().copyObject((InformationObject) implIO1);

      Assert.assertTrue(DatamodelUtils.equalInformationObjects(rdfIO1, rdfIO2));
   }

   @Test
   @Override
   public void testGetSerializeFormat() {
      Assert.assertEquals(SerializeFormat.RDF, getDatamodelFactory().getSerializeFormat());
   }

   @Test
   public void testGetWriterPaths() {
      // dummy
      InformationObject iObj = createDummyInformationObject(getDatamodelFactory());

      // without writers -> should be empty
      Assert.assertTrue(iObj.getWriterPaths().isEmpty());

      Attribute attribute = getDatamodelFactory().createAttribute();
      Attribute subattribute = getDatamodelFactory().createAttribute();

      // writer
      subattribute.setIdentification(DefinedAttributeIdentification.WRITER.getURI());
      subattribute.setValue("test-writer-path");
      subattribute.setAttributePurpose(DefinedAttributePurpose.SYSTEM_ATTRIBUTE.getAttributePurpose());

      // writers-list
      attribute.setIdentification(DefinedAttributeIdentification.AUTHORIZED_WRITERS.getURI());
      attribute.setValue("asd");
      attribute.setAttributePurpose(DefinedAttributePurpose.SYSTEM_ATTRIBUTE.getAttributePurpose());
      attribute.addSubattribute(subattribute);
      iObj.addAttribute(attribute);

      // owner
      attribute = getDatamodelFactory().createAttribute();
      attribute.setIdentification(DefinedAttributeIdentification.OWNER.getURI());
      attribute.setValue("test-writer-path");
      attribute.setAttributePurpose(DefinedAttributePurpose.SYSTEM_ATTRIBUTE.getAttributePurpose());

      iObj.addAttribute(attribute);

      // with writers -> should not be empty
      Assert.assertFalse(iObj.getWriterPaths().isEmpty());
   }

   @Test
   @Override
   public void testDescribeServiceIdentityObject() {
      // not implemented in RDF
      Assert.assertTrue(true);
   }

   @Test
   public void testFactoryGetProperty() {
      // implies correctness of getProperty(uri)
      Assert.assertEquals(DatamodelFactoryRdf.getProperty(DefinedAttributeIdentification.DESCRIPTION),
            DatamodelFactoryRdf.getProperty(DefinedAttributeIdentification.DESCRIPTION.getURI()));
   }
}
