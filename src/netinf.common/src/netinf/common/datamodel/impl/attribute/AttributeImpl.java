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
package netinf.common.datamodel.impl.attribute;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.attribute.Attribute;
import netinf.common.datamodel.impl.DatamodelFactoryImpl;
import netinf.common.datamodel.impl.InformationObjectImpl;
import netinf.common.datamodel.impl.NetInfObjectWrapperImpl;
import netinf.common.utils.DatamodelUtils;

/**
 * The Class AttributeImpl.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class AttributeImpl extends NetInfObjectWrapperImpl implements Attribute, Comparable<Attribute> {

   private static final long serialVersionUID = 7641011358981786273L;
   private final LinkedList<AttributeImpl> subattributes;
   private Object attributeValue;
   private String attributePurpose;
   private String attributeIdentification;

   // For navigation
   private transient InformationObject informationObject;
   private transient Attribute parentAttribute;

   public AttributeImpl(DatamodelFactoryImpl datamodelFactory) {
      super(datamodelFactory);
      subattributes = new LinkedList<AttributeImpl>();
      attributeValue = null;
      attributePurpose = null;
   }

   @Override
   public void addSubattribute(Attribute attribute) {
      AttributeImpl attributeImpl = (AttributeImpl) attribute;

      // The attribute is bound and has to be removed from its parent (informationobject or parent attribute
      if (attributeImpl.getInformationObject() != null || attributeImpl.getParentAttribute() != null) {
         if (attributeImpl.getParentAttribute() != null) {
            attributeImpl.getParentAttribute().removeSubattribute(attributeImpl);
         } else {
            attributeImpl.getInformationObject().removeAttribute(attributeImpl);
         }
      }

      attributeImpl.setParentAttribute(this);
      int index = Collections.binarySearch(subattributes, attributeImpl);
      if (index < 0) {
         subattributes.add(-index - 1, attributeImpl);
      } else {
         subattributes.add(index, attributeImpl);
      }
      if (parentAttribute != null) {
         ((AttributeImpl) parentAttribute).resortAttributes();
      }
      if (informationObject != null) {
         ((InformationObjectImpl) informationObject).resortAttributes();
      }
   }

   @Override
   public List<Attribute> getSubattributes() {
      return new ArrayList<Attribute>(subattributes);
   }

   @Override
   public String getAttributePurpose() {
      return attributePurpose;
   }

   @Override
   public void setAttributePurpose(String attributePurpose) {
      this.attributePurpose = attributePurpose;
   }

   @Override
   public Attribute getSingleSubattribute(String attributeIdentification) {
      Attribute result = null;

      if (attributeIdentification != null) {
         for (Attribute attribute : subattributes) {
            if (attributeIdentification.equals(attribute.getIdentification())) {
               result = attribute;
               break;
            }
         }
      }

      return result;
   }

   @Override
   public List<Attribute> getSubattributesForPurpose(String attributePurpose) {
      ArrayList<Attribute> result = new ArrayList<Attribute>();

      for (Attribute attribute : subattributes) {
         if (attribute.getAttributePurpose().equals(attributePurpose)) {
            result.add(attribute);
         }
      }

      return result;
   }

   @Override
   public void removeSubattribute(String attributeIdentification) {
      Attribute toDelete = null;

      if (attributeIdentification != null) {
         for (Attribute attribute : subattributes) {
            if (attributeIdentification.equals(attribute.getIdentification())) {
               toDelete = attribute;
               break;
            }
         }
      }

      if (toDelete != null) {
         removeSubattribute(toDelete);
      }
   }

   @Override
   public void removeSubattribute(Attribute attribute) {
      AttributeImpl attributeImpl = (AttributeImpl) attribute;
      attributeImpl.setParentAttribute(null);
      subattributes.remove(attribute);
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
   public <T> T getValue(Class<T> class1) {
      return class1.cast(attributeValue);
   }

   @Override
   public void setValue(Object object) {
      attributeValue = object;
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
   public String getIdentification() {
      return attributeIdentification;
   }

   @Override
   public void setIdentification(String uri) {
      this.attributeIdentification = uri;
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
   public List<Attribute> getSubattribute(String attributeIdentification) {
      ArrayList<Attribute> result = new ArrayList<Attribute>();

      if (attributeIdentification != null) {

         for (Attribute attribute : subattributes) {
            if (attributeIdentification.equals(attribute.getIdentification())) {
               result.add(attribute);
            }
         }
      }

      return result;
   }

   /**
    * This method compares this attribute to another. It is required by the Comparable and the Attribute-Interface. It compares in
    * multiple steps 1) lexicographic order of identification 2) lexicographic order of value 3) sort by subattributes 3a)
    * attributes are equal iff their subattribute lists are equal 3b) the attribute with more subattributes comes first 3c) the
    * first difference in equal-length subattribute lists decides
    */
   @Override
   public int compareTo(Attribute arg0) {
      return DatamodelUtils.compareAttributes(this, arg0);
   }

   @Override
   public String toString() {
      return DatamodelUtils.toStringAttribute(this, null);
   }

   /**
    * This method are intended to be only called by impl-classes
    * 
    * @param informationObject
    */
   public void setInformationObject(InformationObject informationObject) {
      this.informationObject = informationObject;
   }

   /**
    * This method are intended to be only called by impl-classes
    * 
    * @param informationObject
    */
   public void setParentAttribute(Attribute parentAttribute) {
      this.parentAttribute = parentAttribute;
   }

   private void resortAttributes() {
      Collections.sort(subattributes);
      if (parentAttribute != null) {
         ((AttributeImpl) parentAttribute).resortAttributes();
      }
      if (informationObject != null) {
         ((InformationObjectImpl) informationObject).resortAttributes();
      }
   }

}
