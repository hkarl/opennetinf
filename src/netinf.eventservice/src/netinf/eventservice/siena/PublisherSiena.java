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
package netinf.eventservice.siena;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.attribute.Attribute;
import netinf.common.datamodel.attribute.DefinedAttributeIdentification;
import netinf.common.exceptions.NetInfUncheckedException;
import netinf.common.messages.ESFEventMessage;
import netinf.eventservice.framework.EventServiceNetInf;
import netinf.eventservice.framework.PublisherNetInf;
import netinf.eventservice.siena.translation.TranslationSiena;

import org.apache.log4j.Logger;

import siena.Filter;
import siena.HierarchicalDispatcher;
import siena.Notifiable;
import siena.Notification;

import com.google.inject.Inject;

/**
 * Processes EventMessages received from a ResolutionService. Every {@link ESFEventMessage} contains two kinds of
 * {@link InformationObject} - the old one and the new one. The translation in the case of Siena works as follows. Since Siena
 * supports only name-value pairs, we use every predicate (a attribute-uri) as a name, and every object as the value of the
 * name-value pair. In the case of of objects being resources without a value, we skip the according translation of the property.
 * Recognize that this translation is not lossless. We lose the information about the position of a predicate in the tree (we are
 * not taking the subject into account while translating). Additionally, we cannot handle {@link InformationObject} which do use
 * the same attribute-uri more than once. In this case the first object for the attribute-uri is used, and consecutive are
 * ignored. Although this are pretty large restrictions, we cannot do that in another way. Finally, in order to retrieve the
 * initial {@link InformationObject}s, we add two fields to each {@link Notification} which contain the serialized form of the old
 * and the new {@link InformationObject}.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class PublisherSiena extends PublisherNetInf<HierarchicalDispatcher, Notifiable, Notification, Filter> {
   private static final Logger LOG = Logger.getLogger(PublisherNetInf.class);

   @Inject
   @SuppressWarnings("unchecked")
   public PublisherSiena(EventServiceNetInf eventServiceNetInf) {
      super(eventServiceNetInf);
   }

   @Override
   public List<Notification> translateEventMessage(ESFEventMessage e) {
      LOG.trace(null);
      LOG.debug("Translating event:" + e);

      Notification notification = new Notification();

      InformationObject newInformationObject = e.getNewInformationObject();
      InformationObject oldInformationObject = e.getOldInformationObject();

      // First add the information that is needed to serialize back the InformationObjects.
      notification.putAttribute(TranslationSiena.FORMAT, e.getSerializeFormat().getSerializeFormat());
      if (newInformationObject != null) {
         notification.putAttribute(TranslationSiena.PREFIX_NEW, newInformationObject.serializeToBytes());
      }
      if (oldInformationObject != null) {
         notification.putAttribute(TranslationSiena.PREFIX_OLD, oldInformationObject.serializeToBytes());
      }

      // Translate old and new IOs
      if (newInformationObject != null) {
         translateInformationObject(newInformationObject, TranslationSiena.PREFIX_NEW, notification);
      }
      if (oldInformationObject != null) {
         translateInformationObject(oldInformationObject, TranslationSiena.PREFIX_OLD, notification);
      }

      createDiff(oldInformationObject, newInformationObject, TranslationSiena.PREFIX_DIFF, notification);

      ArrayList<Notification> result = new ArrayList<Notification>();
      result.add(notification);
      return result;
   }

   private void createDiff(InformationObject oldInformationObject, InformationObject newInformationObject, String prefixDiff,
         Notification notification) {
      LOG.trace(null);

      Hashtable<String, String> usedNames = new Hashtable<String, String>();

      List<Attribute> oldAttributes = null;
      if (oldInformationObject != null) {
         oldAttributes = oldInformationObject.getAttributes();
      }

      List<Attribute> newAttributes = null;
      if (newInformationObject != null) {
         newAttributes = newInformationObject.getAttributes();
      }

      createDiffAttributeList(oldAttributes, newAttributes, prefixDiff, notification, usedNames);
   }

   private void createDiffAttributeList(List<Attribute> oldAttributes, List<Attribute> newAttributes, String prefixDiff,
         Notification notification, Hashtable<String, String> usedNames) {
      LOG.trace(null);

      // First check all the new Attributes and search them in the oldAttribute list
      if (newAttributes != null) {
         for (Attribute newAttribute : newAttributes) {
            Attribute oldAttribute = findAttribute(oldAttributes, newAttribute.getIdentification());

            if (oldAttribute != null) {
               createDiffAttribute(oldAttribute, newAttribute, prefixDiff, notification, usedNames);
               oldAttributes.remove(oldAttribute);
            } else {
               // Old Attribute does not exist, this is a new Attribute that was inserted
               // Although this method call could be made easier, the method call is
               // done explicitly at this place.
               createDiffAttribute(null, newAttribute, prefixDiff, notification, usedNames);
            }
         }
      }

      // Then check all the attributes of the old list.
      if (oldAttributes != null) {
         for (Attribute oldAttribute : oldAttributes) {
            createDiffAttribute(oldAttribute, null, prefixDiff, notification, usedNames);
         }
      }
   }

   private void createDiffAttribute(Attribute oldAttribute, Attribute newAttribute, String prefixDiff, Notification notification,
         Hashtable<String, String> usedNames) {
      LOG.trace(null);

      if (oldAttribute != null && newAttribute != null) {
         // The must have the same identification

         // First compare the value, in order to determine, whether the value has changed, or not
         Object oldValue = oldAttribute.getValue(Object.class);
         Object newValue = newAttribute.getValue(Object.class);

         if ((oldValue == null && newValue == null) || (oldValue != null && newValue != null && oldValue.equals(newValue))) {
            // Unchanged
            // Does not matter, which attribute is taken, whether newAttribute or oldAttribute
            createDiffOneAttribute(oldAttribute, TranslationSiena.STATUS_UNCHANGED, prefixDiff, notification, usedNames);
         } else {
            // Changed
            // Does not matter, which attribute is taken, whether newAttribute or oldAttribute
            createDiffOneAttribute(oldAttribute, TranslationSiena.STATUS_CHANGED, prefixDiff, notification, usedNames);

            if (isNumber(oldValue) && isNumber(newValue)) {
               String status = determineDetailStatus(oldValue, newValue);

               // It does not matter, whether the new or the old attribute is given here
               // We simply need the recursive structure.

               // This method generates additional pieces of information for numbers, with a seperate prefix.
               createDiffOneAttribute(oldAttribute, status, TranslationSiena.PREFIX_DIFF_DETAILS, notification, usedNames);
            }
         }

         // Now go through all the subAttributes
         createDiffAttributeList(oldAttribute.getSubattributes(), newAttribute.getSubattributes(), prefixDiff, notification,
               usedNames);

      } else if (oldAttribute != null && newAttribute == null) {
         // Deleted
         createDiffOneAttribute(oldAttribute, TranslationSiena.STATUS_DELETED, prefixDiff, notification, usedNames);

         // Now go through all the subattributes
         createDiffAttributeList(oldAttribute.getSubattributes(), null, prefixDiff, notification, usedNames);
      } else if (oldAttribute == null && newAttribute != null) {
         // Created
         createDiffOneAttribute(newAttribute, TranslationSiena.STATUS_CREATED, prefixDiff, notification, usedNames);

         // Now go through all the subattributes
         createDiffAttributeList(null, newAttribute.getSubattributes(), prefixDiff, notification, usedNames);
      }
   }

   // This method is NOT recursive, does not look for subattributes
   private void createDiffOneAttribute(Attribute attribute, String status, String prefixDiff, Notification notification,
         Hashtable<String, String> usedNames) {
      LOG.trace(null);

      Object valueObject = attribute.getValue(Object.class);
      if (valueObject != null) {

         // Here we have to add the name for each attribute up in the hierarchy a name and the according value
         // By that it becomes possible to subscribe to a particular structure.
         String lastIdentification = "";
         boolean first = true;
         Attribute currentAttribute = attribute;

         while (currentAttribute != null) {

            String identification;
            if (first) {
               identification = currentAttribute.getIdentification();
               first = false;
            } else {
               identification = currentAttribute.getIdentification() + TranslationSiena.SEPARATOR + lastIdentification;
            }

            String sienaName = identification;

            if (currentAttribute.getParentAttribute() != null) {
               // We are somewhere in the middle. Accordingly, we add a * prefix.
               sienaName = TranslationSiena.WILDCARD + TranslationSiena.SEPARATOR + sienaName;
            }

            sienaName = prefixDiff + TranslationSiena.SEPARATOR + sienaName;

            if (usedNames.containsKey(sienaName)) {
               LOG.warn("This identification was already bound to: Name = '" + sienaName + "', Value = '"
                     + usedNames.get(sienaName) + "', ignoring...");
            } else {
               notification.putAttribute(sienaName, status);

               usedNames.put(sienaName, status);
            }

            // Advance to the next attribute in the hierarchy
            lastIdentification = identification;
            currentAttribute = currentAttribute.getParentAttribute();
         }
      }
   }

   /**
    * @param informationObject
    * @param prefix
    * @param toStoreIn
    * @return whether the translation could be done with unique names. If true => unique names. Otherwise a name was used twice.
    */
   private boolean translateInformationObject(InformationObject informationObject, String prefix, Notification toStoreIn) {
      LOG.trace(null);

      boolean result = true;

      // The first one is the used uri (concatenated with the prefix)
      // The second one states the value, to which the name was bound
      Hashtable<String, String> usedNames = new Hashtable<String, String>();

      // Add the identifier as an Attribute
      toStoreIn.putAttribute(prefix + TranslationSiena.SEPARATOR + DefinedAttributeIdentification.IDENTIFIER.getURI(),
            informationObject.getIdentifier().toString());

      // iterate recursively over all properties that are set
      for (Attribute attribute : informationObject.getAttributes()) {
         result &= translateAttribute(attribute, prefix, toStoreIn, usedNames);
      }

      return result;
   }

   private boolean translateAttribute(Attribute attribute, String prefix, Notification toStoreIn,
         Hashtable<String, String> usedNames) {
      LOG.trace(null);

      boolean result = true;

      // First add yourself, if the value is defined
      // If null, the translation is simply skipped.
      Object valueObject = attribute.getValue(Object.class);
      if (valueObject != null) {

         // Here we have to add the name for each attribute up in the hierarchy a name and the according value
         // By that it becomes possible to subscribe to a particular structure.
         String lastIdentification = "";
         boolean first = true;
         Attribute currentAttribute = attribute;

         while (currentAttribute != null) {

            String identification;
            if (first) {
               identification = currentAttribute.getIdentification();
               first = false;
            } else {
               identification = currentAttribute.getIdentification() + TranslationSiena.SEPARATOR + lastIdentification;
            }

            String sienaName = identification;

            if (currentAttribute.getParentAttribute() != null) {
               // We are somewhere in the middle. Accordingly, we add a * prefix.
               sienaName = TranslationSiena.WILDCARD + TranslationSiena.SEPARATOR + sienaName;
            }

            sienaName = prefix + TranslationSiena.SEPARATOR + sienaName;

            if (usedNames.containsKey(sienaName)) {
               LOG.warn("This identification was already bound to: Name = '" + sienaName + "', Value = '"
                     + usedNames.get(sienaName) + "', ignoring...");
            } else {
               if (valueObject instanceof String) {
                  toStoreIn.putAttribute(sienaName, (String) valueObject);
               } else if (valueObject instanceof Integer) {
                  toStoreIn.putAttribute(sienaName, ((Integer) valueObject).intValue());
               } else if (valueObject instanceof Boolean) {
                  toStoreIn.putAttribute(sienaName, ((Boolean) valueObject).booleanValue());
               } else if (valueObject instanceof Long) {
                  toStoreIn.putAttribute(sienaName, ((Long) valueObject).longValue());
               } else if (valueObject instanceof Double) {
                  toStoreIn.putAttribute(sienaName, ((Double) valueObject).doubleValue());
               } else {
                  LOG.warn("The type of Name = '" + sienaName + "', Value = '" + valueObject + "' Identification of type = '"
                        + attribute.getValueType() + "' is not supported, ignoring...");
               }

               usedNames.put(sienaName, attribute.getValueRaw());
            }

            // Advance to the next attribute in the hierarchy
            lastIdentification = identification;
            currentAttribute = currentAttribute.getParentAttribute();
         }
      }

      // Now add all the subattributes
      for (Attribute subAttribute : attribute.getSubattributes()) {
         result &= translateAttribute(subAttribute, prefix, toStoreIn, usedNames);
      }

      return result;
   }

   /**
    * Assumes that the attribute list is sorted.
    * 
    * @param attributes
    * @param identification
    * @return
    */
   private Attribute findAttribute(List<Attribute> attributes, String identification) {
      if (attributes != null) {
         // Only in case the list where to search is not null

         for (Attribute attribute : attributes) {
            int compare = attribute.getIdentification().compareTo(identification);
            if (compare < 0) {
               continue;
            } else if (compare == 0) {
               return attribute;
            } else {
               // We have run to far
               break;
            }
         }

      }

      // Specified argument not found
      return null;
   }

   private boolean isNumber(Object value) {
      boolean result = false;

      // This are the only three supported types by Siena.
      result |= value instanceof Integer;
      result |= value instanceof Double;
      result |= value instanceof Long;

      return result;
   }

   private String determineDetailStatus(Object oldValue, Object newValue) {
      double oldValueDouble = ((Number) oldValue).doubleValue();
      double newValueDouble = ((Number) newValue).doubleValue();

      if (oldValueDouble < newValueDouble) {
         return TranslationSiena.STATUS_CHANGED_LT;
      } else if (oldValueDouble > newValueDouble) {
         return TranslationSiena.STATUS_CHANGED_GT;
      } else {
         throw new NetInfUncheckedException("There are only two detail status: lt and gt. But the given values are equal");
      }
   }
}
