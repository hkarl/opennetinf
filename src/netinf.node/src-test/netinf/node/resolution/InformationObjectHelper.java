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
package netinf.node.resolution;

import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.DefinedAttributePurpose;
import netinf.common.datamodel.DefinedLabelName;
import netinf.common.datamodel.DefinedVersionKind;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.IdentifierLabel;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.attribute.Attribute;
import netinf.common.datamodel.rdf.DefinedRdfNames;

import com.google.inject.Inject;

/**
 * A class to provide some InformationObjects for Unit Tests
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class InformationObjectHelper {

   private static int counter = 0;
   protected DatamodelFactory datamodelFactory;

   @Inject
   public InformationObjectHelper(DatamodelFactory datamodelFactory) {
      this.datamodelFactory = datamodelFactory;
   }

   public InformationObject getDummyIO() {
      InformationObject io = this.datamodelFactory.createInformationObject();
      Identifier identifier = this.datamodelFactory.createIdentifier();
      IdentifierLabel label = this.datamodelFactory.createIdentifierLabel();
      label.setLabelName("Name");
      label.setLabelValue("Value");
      identifier.addIdentifierLabel(label);
      label = this.datamodelFactory.createIdentifierLabel();
      label.setLabelName(DefinedLabelName.VERSION_KIND.getLabelName());
      label.setLabelValue(DefinedVersionKind.UNVERSIONED.getStringRepresentation());
      identifier.addIdentifierLabel(label);
      io.setIdentifier(identifier);
      return io;
   }

   public InformationObject createUniqueIO() {
      InformationObject io = this.datamodelFactory.createInformationObject();
      Identifier identifier = this.datamodelFactory.createIdentifier();
      for (int i = 0; i < 3; i++) {
         IdentifierLabel label = this.datamodelFactory.createIdentifierLabel();
         label.setLabelName(getRandomString());
         label.setLabelValue(getRandomString());
         identifier.addIdentifierLabel(label);
      }
      IdentifierLabel label = this.datamodelFactory.createIdentifierLabel();
      label.setLabelName(DefinedLabelName.VERSION_KIND.getLabelName());
      label.setLabelValue(DefinedVersionKind.UNVERSIONED.getStringRepresentation());
      identifier.addIdentifierLabel(label);
      io.setIdentifier(identifier);
      return io;
   }

   public InformationObject createUniqueVersionedIO() {
      InformationObject io = this.datamodelFactory.createInformationObject();
      Identifier identifier = this.datamodelFactory.createIdentifier();
      for (int i = 0; i < 3; i++) {
         IdentifierLabel label = this.datamodelFactory.createIdentifierLabel();
         label.setLabelName(getRandomString());
         label.setLabelValue(getRandomString());
         identifier.addIdentifierLabel(label);
      }
      IdentifierLabel label = this.datamodelFactory.createIdentifierLabel();
      label.setLabelName(DefinedLabelName.VERSION_KIND.getLabelName());
      label.setLabelValue(DefinedVersionKind.VERSIONED.getStringRepresentation());
      identifier.addIdentifierLabel(label);
      label = this.datamodelFactory.createIdentifierLabel();
      label.setLabelName(DefinedLabelName.VERSION_NUMBER.getLabelName());
      label.setLabelValue(String.valueOf(counter++));
      identifier.addIdentifierLabel(label);
      io.setIdentifier(identifier);
      return io;
   }

   public InformationObject createDummyIOWithUniqueVersion() {
      InformationObject io = this.datamodelFactory.createInformationObject();
      Identifier identifier = this.datamodelFactory.createIdentifier();
      IdentifierLabel label = this.datamodelFactory.createIdentifierLabel();
      label.setLabelName("Name");
      label.setLabelValue("Value");
      identifier.addIdentifierLabel(label);
      label = this.datamodelFactory.createIdentifierLabel();
      label.setLabelName(DefinedLabelName.VERSION_KIND.getLabelName());
      label.setLabelValue(DefinedVersionKind.VERSIONED.getStringRepresentation());
      identifier.addIdentifierLabel(label);
      label = this.datamodelFactory.createIdentifierLabel();
      label.setLabelName(DefinedLabelName.VERSION_NUMBER.getLabelName());
      label.setLabelValue(String.valueOf(counter++));
      identifier.addIdentifierLabel(label);
      io.setIdentifier(identifier);
      return io;
   }

   public InformationObject getUniqueIOWithDummyAttributeAndSubAttributes() {
      InformationObject io = createUniqueIO();
      Attribute mainAttribute = this.datamodelFactory.createAttribute();
      mainAttribute.setIdentification(DefinedRdfNames.ioProperty(io, "mainTestAttribute").getURI());
      mainAttribute.setValue("MAINATTR_VALUE");
      mainAttribute.setAttributePurpose(DefinedAttributePurpose.USER_ATTRIBUTE.name());
      Attribute subAttribute1 = this.datamodelFactory.createAttribute();
      subAttribute1.setIdentification(DefinedRdfNames.ioProperty(io, "subTestAttribute").getURI());
      subAttribute1.setValue("SUBATTR1_VALUE");
      subAttribute1.setAttributePurpose(DefinedAttributePurpose.USER_ATTRIBUTE.name());
      Attribute subAttribute2 = this.datamodelFactory.createAttribute();
      subAttribute2.setIdentification(DefinedRdfNames.ioProperty(io, "subTestAttribute").getURI());
      subAttribute2.setValue("SUBATTR2_VALUE");
      subAttribute2.setAttributePurpose(DefinedAttributePurpose.USER_ATTRIBUTE.name());
      Attribute subSubAttribute = this.datamodelFactory.createAttribute();
      subSubAttribute.setIdentification(DefinedRdfNames.ioProperty(io, "subSubTestAttribute").getURI());
      subSubAttribute.setValue("SUBSUBATTR_VALUE");
      subSubAttribute.setAttributePurpose(DefinedAttributePurpose.USER_ATTRIBUTE.name());
      subAttribute1.addSubattribute(subSubAttribute);
      mainAttribute.addSubattribute(subAttribute1);
      mainAttribute.addSubattribute(subAttribute2);
      io.addAttribute(mainAttribute);
      return io;
   }

   private String getRandomString() {
      return "test" + counter++;
   }

}
