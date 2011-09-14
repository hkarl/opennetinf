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
import java.util.List;

import netinf.common.communication.SerializeFormat;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.impl.DatamodelFactoryImpl;
import netinf.common.datamodel.rdf.DatamodelFactoryRdf;
import netinf.common.exceptions.NetInfCheckedException;
import netinf.common.messages.ESFEventMessage;
import netinf.common.messages.ESFSubscriptionRequest;
import netinf.eventservice.framework.EventServiceNetInf;
import netinf.eventservice.framework.SubscriberNetInf;
import netinf.eventservice.siena.translation.SubscriptionTranslatorSiena;
import netinf.eventservice.siena.translation.TranslationSiena;

import org.apache.log4j.Logger;

import siena.AttributeValue;
import siena.Filter;
import siena.HierarchicalDispatcher;
import siena.Notifiable;
import siena.Notification;
import siena.SienaException;

import com.google.inject.Inject;

/**
 * Represents a client that has subscribed for events
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class SubscriberSiena extends SubscriberNetInf<HierarchicalDispatcher, Notifiable, Notification, Filter> {
   private static final Logger LOG = Logger.getLogger(SubscriberSiena.class);

   private DatamodelFactoryRdf datamodelFactoryRdf;
   private DatamodelFactoryImpl datamodelFactoryImpl;

   @Inject
   @SuppressWarnings("unchecked")
   public SubscriberSiena(EventServiceNetInf e) {
      super(e);
   }

   @Inject
   public void setDatamodelFactories(DatamodelFactoryRdf datamodelFactoryRdf, DatamodelFactoryImpl datamodelFactoryImpl) {
      this.datamodelFactoryRdf = datamodelFactoryRdf;
      this.datamodelFactoryImpl = datamodelFactoryImpl;
   }

   @Override
   public Notifiable createSubscriberOfSpecificEventService() {
      LOG.trace(null);
      Notifiable notifiable = new Notifiable() {

         @Override
         public void notify(Notification[] s) throws SienaException {
            for (int i = 0; i < s.length; i++) {
               processEvent(s[i], this);
            }
         }

         @Override
         public void notify(Notification n) throws SienaException {
            LOG.trace(null);
            processEvent(n, this);
         }
      };
      return notifiable;
   }

   @Override
   public void removeSubscriberOfSpecificEventService(Notifiable subscriber) {
      // Not needed for the event service siena
   }

   @Override
   public List<ESFEventMessage> translateEvent(Notification notification) {
      LOG.trace(null);
      LOG.debug("Translating Siena Notification: " + notification);

      ESFEventMessage eventMessage = new ESFEventMessage();

      SerializeFormat format = SerializeFormat.getSerializeFormat(notification.getAttribute(TranslationSiena.FORMAT)
            .stringValue());

      AttributeValue attrNewInformationObject = notification.getAttribute(TranslationSiena.PREFIX_NEW);
      if (attrNewInformationObject != null) {
         InformationObject newInformationObject = null;

         if (format == SerializeFormat.JAVA) {
            newInformationObject = this.datamodelFactoryImpl.createInformationObjectFromBytes(attrNewInformationObject
                  .byteArrayValue());

         } else if (format == SerializeFormat.RDF) {
            newInformationObject = this.datamodelFactoryRdf.createInformationObjectFromBytes(attrNewInformationObject
                  .byteArrayValue());
         }

         eventMessage.setNewInformationObject(newInformationObject);
      }

      AttributeValue attrOldInformationObject = notification.getAttribute(TranslationSiena.PREFIX_OLD);
      if (attrOldInformationObject != null) {
         InformationObject oldInformationObject = null;

         if (format == SerializeFormat.JAVA) {
            oldInformationObject = this.datamodelFactoryImpl.createInformationObjectFromBytes(attrOldInformationObject
                  .byteArrayValue());

         } else if (format == SerializeFormat.RDF) {
            oldInformationObject = this.datamodelFactoryRdf.createInformationObjectFromBytes(attrOldInformationObject
                  .byteArrayValue());
         }

         eventMessage.setOldInformationObject(oldInformationObject);
      }

      ArrayList<ESFEventMessage> eventMessages = new ArrayList<ESFEventMessage>();
      eventMessages.add(eventMessage);

      LOG.debug("Result of translation is: " + eventMessages);
      return eventMessages;
   }

   @Override
   public List<Filter> translateSubscriptionRequest(ESFSubscriptionRequest subscriptionRequest) throws NetInfCheckedException {
      LOG.trace(null);

      String query = subscriptionRequest.getSparqlSubscription();
      SubscriptionTranslatorSiena translator = new SubscriptionTranslatorSiena(query);
      return translator.buildSienaFilters();
   }
}
