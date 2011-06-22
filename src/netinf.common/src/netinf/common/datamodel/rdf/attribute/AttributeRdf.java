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
package netinf.common.datamodel.rdf.attribute;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.attribute.Attribute;
import netinf.common.datamodel.rdf.DatamodelFactoryRdf;
import netinf.common.datamodel.rdf.DefinedRdfNames;
import netinf.common.datamodel.rdf.InformationObjectRdf;
import netinf.common.datamodel.rdf.NetInfObjectWrapperRdf;
import netinf.common.exceptions.NetInfCheckedException;
import netinf.common.exceptions.NetInfUncheckedException;
import netinf.common.utils.DatamodelUtils;
import netinf.common.utils.Utils;
import netinf.common.utils.ValueUtils;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/**
 * This is a {@link AttributeRdf}. One {@link AttributeRdf} can only be used within the rdf-implementation of the datamodel.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class AttributeRdf extends NetInfObjectWrapperRdf implements Attribute {

   private static final Logger LOG = Logger.getLogger(AttributeRdf.class);

   private final List<AttributeRdf> subattributes;
   private Object attributeValue;
   private String attributePurpose;
   private String attributeIdentification;

   // For navigation
   private InformationObjectRdf informationObject;
   private AttributeRdf parentAttribute;

   @Inject
   public AttributeRdf(DatamodelFactoryRdf datamodelFactoryRdf) {
      super(datamodelFactoryRdf);
      subattributes = new ArrayList<AttributeRdf>();
      setResource(null);
   }

   @Override
   public void addSubattribute(Attribute attribute) {
      AttributeRdf attributeRdf = (AttributeRdf) attribute;

      // The attribute is bound and has to be removed from its parent (informationobject or parent attribute
      if (attributeRdf.getInformationObject() != null || attributeRdf.getParentAttribute() != null) {
         if (attributeRdf.getParentAttribute() != null) {
            attributeRdf.getParentAttribute().removeSubattribute(attributeRdf);
         } else {
            attributeRdf.getInformationObject().removeAttribute(attributeRdf);
         }
      }

      attributeRdf.setParentAttribute(this);

      if (getResource() != null) {
         Resource resourceForAttribute = getResource().getModel().createResource();
         attributeRdf.bindToResource(resourceForAttribute);
      }

      subattributes.add(attributeRdf);
   }

   @Override
   public int compareTo(Attribute arg0) {
      return DatamodelUtils.compareAttributes(this, arg0);
   }

   @Override
   public String getAttributePurpose() {
      return attributePurpose;
   }

   @Override
   public String getIdentification() {
      return attributeIdentification;
   }

   @Override
   public InformationObject getInformationObject() {
      if (informationObject == null) {
         if (parentAttribute == null) {
            return null;
         } else {
            return parentAttribute.getInformationObject();
         }

      } else {
         return informationObject;
      }
   }

   @Override
   public Attribute getParentAttribute() {
      return parentAttribute;
   }

   @Override
   public Attribute getSingleSubattribute(String attributeIdentification) {
      Attribute result = null;

      if (attributeIdentification != null) {

         for (Attribute attribute : this.subattributes) {
            if (attributeIdentification.equals(attribute.getIdentification())) {
               result = attribute;
               break;
            }
         }
      }

      return result;
   }

   @Override
   public List<Attribute> getSubattribute(String attributeIdentification) {
      ArrayList<Attribute> result = new ArrayList<Attribute>();

      if (attributeIdentification != null) {

         for (Attribute attribute : this.subattributes) {
            if (attributeIdentification.equals(attribute.getIdentification())) {
               result.add(attribute);
            }
         }
      }

      Collections.sort(result);
      return result;
   }

   @Override
   public List<Attribute> getSubattributes() {
      ArrayList<Attribute> result = new ArrayList<Attribute>(this.subattributes);
      Collections.sort(result);
      return result;
   }

   @Override
   public List<Attribute> getSubattributesForPurpose(String attributePurpose) {
      List<Attribute> result = getSubattributes();
      Iterator<Attribute> iteratorAttributes = result.iterator();

      while (iteratorAttributes.hasNext()) {
         Attribute attribute = iteratorAttributes.next();

         if (!attribute.getAttributePurpose().equals(attributePurpose)) {
            iteratorAttributes.remove();
         }
      }

      return result;
   }

   @Override
   public <T> T getValue(Class<T> class1) {
      return class1.cast(attributeValue);
   }

   @Override
   public String getValueRaw() {
      if (attributeValue != null) {
         return getValueType() + DatamodelUtils.TYPE_VALUE_SEPARATOR + attributeValue.toString();
      } else {
         return null;
      }
   }

   @Override
   public String getValueType() {
      return DatamodelUtils.getValueType(attributeValue);
   }

   @Override
   public void removeSubattribute(String identification) {
      ArrayList<Attribute> toDelete = new ArrayList<Attribute>();

      if (attributeIdentification != null) {
         for (Attribute attribute : this.subattributes) {
            if (attributeIdentification.equals(attribute.getIdentification())) {
               toDelete.add(attribute);
            }
         }
      }

      for (Attribute attribute : toDelete) {
         removeSubattribute(attribute);
      }
   }

   @Override
   public void removeSubattribute(Attribute attribute) {
      if (subattributes.contains(attribute)) {
         AttributeRdf attributeRdf = (AttributeRdf) attribute;
         attributeRdf.setParentAttribute(null);
         subattributes.remove(attribute);

         if (getResource() != null) {
            // Thus, the attribute is not attached to any resource anymore
            attributeRdf.bindToResource(null);
         }
      }
   }

   @Override
   public void setAttributePurpose(String string) {
      this.attributePurpose = string;

      if (getResource() != null) {
         // Remove old attribute purpose
         getResource().removeAll(DatamodelFactoryRdf.getProperty(DefinedRdfNames.ATTRIBUTE_PURPUSE));

         // Add new attribute purpose.
         getResource().getModel().add(getResource(), DatamodelFactoryRdf.getProperty(DefinedRdfNames.ATTRIBUTE_PURPUSE),
               attributePurpose);
      }
   }

   @Override
   public void setValue(Object object) {
      this.attributeValue = object;

      if (getResource() != null) {
         // Remove old value
         getResource().removeAll(DatamodelFactoryRdf.getProperty(DefinedRdfNames.ATTRIBUTE_VALUE));

         // Add new value
         getResource().getModel().add(getResource(), DatamodelFactoryRdf.getProperty(DefinedRdfNames.ATTRIBUTE_VALUE),
               getValueRaw());

      }

   }

   public void setIdentification(String uri) {
      String oldAttributeIdentification = attributeIdentification;

      this.attributeIdentification = uri;

      if (getResource() != null) {

         // remove old identification
         Resource parentResource = getParentResource();
         getResource().getModel().remove(parentResource, DatamodelFactoryRdf.getProperty(oldAttributeIdentification),
               getResource());

         // Add new identification
         getResource().getModel().add(parentResource, DatamodelFactoryRdf.getProperty(attributeIdentification), getResource());
      }
   }

   @Override
   public byte[] serializeToBytes() {
      // The serialization to a resource differs significantly from the binding to resources

      DatamodelFactoryRdf datamodelFactory = (DatamodelFactoryRdf) getDatamodelFactory();
      Model attributeModel = datamodelFactory.createModelForAttribute();
      List<Statement> statements = attributeModel.listStatements().toList();

      if (statements.size() == 1 && statements.get(0).getPredicate().getURI().equals(DefinedRdfNames.POINTER_TO_ATTRIBUTE)) {

         Statement statement = statements.get(0);

         Resource serializeToResource = (Resource) statement.getObject();
         Resource parentResource = statement.getSubject();
         serializeToResource(serializeToResource, parentResource);
      } else {
         throw new NetInfUncheckedException("Could not serialize attribute seperately, "
               + "since the attribute model contains errors");
      }

      StringWriter stringWriter = new StringWriter();
      attributeModel.write(stringWriter, DefinedRdfNames.NETINF_RDF_STORAGE_FORMAT);
      return Utils.stringToBytes(stringWriter.toString());
   }

   @Override
   public void initFromResource(Resource resource) throws NetInfCheckedException {
      // TODO: Check validity of Resource
      // TODO: remove all the old values from this Attribute

      // We are getting bound, thus update from resource
      this.setResource(resource);

      // Set Attribute identification
      Resource parentResource = getParentResource();

      List<Statement> attributeIdentificationList = resource.getModel().listStatements(parentResource, null, resource).toList();

      if (attributeIdentificationList.size() == 1) {
         Statement attributeIdentificationStatement = attributeIdentificationList.get(0);
         attributeIdentification = attributeIdentificationStatement.getPredicate().getURI();

      } else {
         throw new NetInfCheckedException("AttributeIdentification could not be found");
      }

      // Set Attribute purpose
      List<Statement> attributePurposeList = resource.listProperties(
            DatamodelFactoryRdf.getProperty(DefinedRdfNames.ATTRIBUTE_PURPUSE)).toList();

      if (attributePurposeList.size() == 1) {
         Statement attributePurposeStatement = attributePurposeList.get(0);
         if (attributePurposeStatement.getObject().isLiteral()) {
            attributePurpose = attributePurposeStatement.getObject().as(Literal.class).getString();
         } else {
            throw new NetInfCheckedException("AttributePurpose is no literal");
         }

      } else {
         throw new NetInfCheckedException("Attribute Purpose could not be set (either more than one, or missing)");
      }

      // Set Attribute value
      List<Statement> attributeValueList = resource.listProperties(
            DatamodelFactoryRdf.getProperty(DefinedRdfNames.ATTRIBUTE_VALUE)).toList();

      if (attributeValueList.size() == 1) {
         Statement attributeValueStatement = attributeValueList.get(0);
         if (attributeValueStatement.getObject().isLiteral()) {
            String rawValue = attributeValueStatement.getObject().as(Literal.class).getString();
            attributeValue = ValueUtils.getObjectFromRaw(rawValue);
         } else {
            throw new NetInfCheckedException("AttributeValue is no literal");
         }

      } else {
         throw new NetInfCheckedException("AttributeValue could not be set (either more than one, or missing)");
      }

      // Now consider all the subattributes
      StmtIterator allStatements = resource.listProperties();
      while (allStatements.hasNext()) {
         Statement statement = allStatements.next();
         if (!statement.getPredicate().equals(DatamodelFactoryRdf.getProperty(DefinedRdfNames.ATTRIBUTE_VALUE))
               && !statement.getPredicate().equals(DatamodelFactoryRdf.getProperty(DefinedRdfNames.ATTRIBUTE_PURPUSE))) {

            // We have a subattribute
            AttributeRdf attribute = (AttributeRdf) getDatamodelFactory().createAttribute();

            // It is important to know the parent
            attribute.setParentAttribute(this);

            // Initialize from resource
            Resource tmpResource = (Resource) statement.getObject();

            try {
               attribute.initFromResource(tmpResource);
               subattributes.add(attribute);

            } catch (NetInfCheckedException e) {
               LOG.debug("Could not initialize Attribute from Resource, ignoring Resource: " + tmpResource);
            }

         }
      }
   }

   public void setParentAttribute(AttributeRdf attribute) {
      this.parentAttribute = attribute;
   }

   public void setInformationObject(InformationObjectRdf informationObjectRdf) {
      this.informationObject = informationObjectRdf;
   }

   @Override
   protected void addToResource(Resource givenResource) {
      this.setResource(givenResource);

      Model model = getResource().getModel();
      Resource parentResource = getParentResource();

      // First bind resource to parent
      model.add(parentResource, DatamodelFactoryRdf.getProperty(attributeIdentification), getResource());
      model.add(getResource(), DatamodelFactoryRdf.getProperty(DefinedRdfNames.ATTRIBUTE_VALUE), getValueRaw());
      model.add(getResource(), DatamodelFactoryRdf.getProperty(DefinedRdfNames.ATTRIBUTE_PURPUSE), attributePurpose);

      // Now all subattributes
      Iterator<AttributeRdf> subAttributeIterator = subattributes.iterator();

      while (subAttributeIterator.hasNext()) {
         AttributeRdf next = subAttributeIterator.next();
         Resource resource = model.createResource();
         next.bindToResource(resource);
      }
   }

   @Override
   protected void removeFromResource(Resource givenResource) {
      if (this.getResource() != givenResource) {
         throw new NetInfUncheckedException("Trying to unbind from a resource, which was not bound");
      }
      this.setResource(null);

      givenResource.removeAll(DatamodelFactoryRdf.getProperty(DefinedRdfNames.ATTRIBUTE_VALUE));
      givenResource.removeAll(DatamodelFactoryRdf.getProperty(DefinedRdfNames.ATTRIBUTE_PURPUSE));

      // Remove the connection to parent
      givenResource.getModel().removeAll(null, DatamodelFactoryRdf.getProperty(attributeIdentification), givenResource);

      Iterator<AttributeRdf> subAttributeIterator = subattributes.iterator();

      while (subAttributeIterator.hasNext()) {
         AttributeRdf next = subAttributeIterator.next();
         next.bindToResource(null);
      }
   }

   /*
    * This method is NOT generated (non-Javadoc)
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((attributeIdentification == null) ? 0 : attributeIdentification.hashCode());
      result = prime * result + ((attributePurpose == null) ? 0 : attributePurpose.hashCode());
      result = prime * result + ((attributeValue == null) ? 0 : attributeValue.hashCode());
      // Check hashcode of sorted list
      result = prime * result + ((subattributes == null) ? 0 : getSubattributes().hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj) {
      return DatamodelUtils.equalAttributes(this, obj);
   }

   @Override
   public String toString() {
      return DatamodelUtils.toStringAttribute(this, null);
   }

   // ***** Internal Methods *****/

   private Resource getParentResource() {
      Resource parentResource = null;

      if (parentAttribute != null) {
         parentResource = parentAttribute.getResource();

      } else if (informationObject != null) {
         parentResource = informationObject.getResource();

      }

      return parentResource;
   }

   public void serializeToResource(Resource serializeToResource, Resource parentResource) {
      Model model = serializeToResource.getModel();

      // First bind resource to parent
      model.add(parentResource, DatamodelFactoryRdf.getProperty(attributeIdentification), serializeToResource);
      model.add(serializeToResource, DatamodelFactoryRdf.getProperty(DefinedRdfNames.ATTRIBUTE_VALUE), getValueRaw());
      model.add(serializeToResource, DatamodelFactoryRdf.getProperty(DefinedRdfNames.ATTRIBUTE_PURPUSE), attributePurpose);

      // Now all subattributes
      Iterator<AttributeRdf> subAttributeIterator = subattributes.iterator();

      while (subAttributeIterator.hasNext()) {
         AttributeRdf next = subAttributeIterator.next();
         Resource serializeToResourceSubAttribute = model.createResource();
         next.serializeToResource(serializeToResourceSubAttribute, serializeToResource);
      }
   }
}
