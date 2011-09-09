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
package netinf.eventservice.framework;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import netinf.common.communication.Communicator;
import netinf.common.communication.MessageEncoder;
import netinf.common.communication.MessageEncoderProtobuf;
import netinf.common.communication.RemoteNodeConnection;
import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.DefinedAttributePurpose;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.attribute.Attribute;
import netinf.common.datamodel.attribute.DefinedAttributeIdentification;
import netinf.common.exceptions.NetInfCheckedException;
import netinf.common.exceptions.NetInfUncheckedException;
import netinf.common.messages.ESFEventMessage;
import netinf.common.messages.ESFFetchMissedEventsResponse;
import netinf.common.messages.ESFSubscriptionRequest;
import netinf.common.messages.ESFSubscriptionResponse;
import netinf.common.messages.ESFUnsubscriptionRequest;
import netinf.common.messages.ESFUnsubscriptionResponse;
import netinf.common.utils.Utils;

import org.apache.log4j.Logger;

import com.google.inject.Inject;

/**
 * Represents a client that has subscribed for events
 * 
 * @param <ES>
 *           The event service
 * @param <S>
 *           The subscriber
 * @param <E>
 *           The event type
 * @param <SC>
 *           the Subscriptions
 * @author PG Augnet 2, University of Paderborn
 */
public abstract class SubscriberNetInf<ES, S, E, SC> {

   private static final Logger LOG = Logger.getLogger(SubscriberNetInf.class);

   /**
    * key: subscriber of the particular event service (created for each SubscriptionMessage) value: SubscriptionIdentification
    * that belongs to the subscriber (key). invert to <code>mappingToSubscriber</code>
    */
   private final Hashtable<S, String> mappingToSubscriptionIdentification;

   /**
    * key: SubscriptionIdentification (of a SubscriptionMessage) value: the subscriber, that contains the subscriptions for the
    * SubscriptionMessage (of subscriptionIdentification) invert to <code>mappingToSubscriptionIdentification</code>
    */
   private final Hashtable<String, S> mappingToSubscriber;

   /**
    * key: subscriber (of the specific event Service) value: all the subscription (of the specific event service) that are send
    * via the subscriber (key).
    */
   private final Hashtable<S, List<SC>> mappingToSubscriptions;

   /**
    * This hashtable stores all the received subscriptions. It is only necessary for tracking.
    */
   private final Hashtable<String, ESFSubscriptionRequest> subscriptionRequests;

   private Identifier eventContainerID;
   private Identifier personObjectID;

   private final EventServiceNetInf<ES, S, E, SC> eventService;
   private Communicator subscriberCommunicator;
   private MessageEncoder messageEncoder;

   // The internal datamodelfactory
   private DatamodelFactory datamodelFactory;

   @Inject
   public SubscriberNetInf(EventServiceNetInf<ES, S, E, SC> eventServiceNetInf) {
      this.eventService = eventServiceNetInf;
      this.mappingToSubscriptionIdentification = new Hashtable<S, String>();
      this.mappingToSubscriber = new Hashtable<String, S>();
      this.mappingToSubscriptions = new Hashtable<S, List<SC>>();
      this.subscriptionRequests = new Hashtable<String, ESFSubscriptionRequest>();
   }

   @Inject
   public void setDatamodelFactory(DatamodelFactory datamodelFactory) {
      this.datamodelFactory = datamodelFactory;
   }

   @Inject
   public void setMessageEncoder(MessageEncoderProtobuf messageEncoder) {
      this.messageEncoder = messageEncoder;
   }

   public void processEvent(E event, S fromSubscriber) {
      LOG.trace(null);
      List<ESFEventMessage> translatedEvents = translateEvent(event);
      String subscriptionIdentification = this.mappingToSubscriptionIdentification.get(fromSubscriber);

      // set correct identification
      for (int i = 0; i < translatedEvents.size(); i++) {
         ESFEventMessage message = translatedEvents.get(i);
         message.setMatchedSubscriptionIdentification(subscriptionIdentification);
      }

      // Send event to client.
      try {
         for (int i = 0; i < translatedEvents.size(); i++) {
            if (this.subscriberCommunicator != null) {
               LOG.debug("Sending EventMessage to client: " + translatedEvents.get(i));
               this.subscriberCommunicator.send(translatedEvents.get(i));
            } else {
               LOG.debug("Client offline. Storing event message in Event Container IO: " + this.eventContainerID);
               appendEventMessageToEventContainerIO(translatedEvents.get(i));
            }
         }
      } catch (NetInfCheckedException e) {
         LOG.trace("Could not send event message to client", e);
      }
   }

   public void appendEventMessageToEventContainerIO(ESFEventMessage eventMessage) {
      LOG.trace(null);

      RemoteNodeConnection com = this.eventService.getRemoteNodeConnection();

      try {
         InformationObject informationObject = com.getIO(this.eventContainerID);

         if (informationObject == null) {
            informationObject = this.datamodelFactory.createInformationObject();
            informationObject.setIdentifier(this.eventContainerID);
         }

         Attribute eventMessageAttribute = buildAttributeForEventMessage(eventMessage);
         informationObject.addAttribute(eventMessageAttribute);
         com.putIO(informationObject);
      } catch (NetInfCheckedException e) {
         LOG.error("Failed to append EventMessage to IO: " + e.getMessage());
      }
   }

   private Attribute buildAttributeForEventMessage(ESFEventMessage eventMessage) {
      LOG.trace(null);

      byte[] serializedEventObject = this.messageEncoder.encodeMessage(eventMessage);
      Attribute attribute = this.datamodelFactory.createAttribute();
      attribute.setAttributePurpose(DefinedAttributePurpose.USER_ATTRIBUTE.getAttributePurpose());
      attribute.setIdentification(DefinedAttributeIdentification.EVENT.getURI());
      attribute.setValue(Utils.bytesToString(serializedEventObject));

      return attribute;
   }

   public ESFSubscriptionResponse processSubscriptionRequest(ESFSubscriptionRequest sm) {
      LOG.trace(null);

      ESFSubscriptionRequest storedSubscriptionRequest = this.subscriptionRequests.put(sm.getSubscriptionIdentification(), sm);

      ESFSubscriptionResponse subscriptionResponse = new ESFSubscriptionResponse();

      if (storedSubscriptionRequest != null) {
         LOG.error("A subscription message with the same identification was already processed. "
               + "First unsubscribe the old one");
         subscriptionResponse.setErrorMessage("A subscription message with the same identification was already processed. "
               + "First unsubscribe the old one");
      } else {
         List<SC> translatedSubscriptions;
         try {
            translatedSubscriptions = translateSubscriptionRequest(sm);

            // We have to have one subscriber for each subscription
            // By this it becomes possible to determine to subscription
            // that matched the subscriber
            S s = createSubscriberOfSpecificEventService();
            this.mappingToSubscriptions.put(s, translatedSubscriptions);
            this.mappingToSubscriptionIdentification.put(s, sm.getSubscriptionIdentification());
            this.mappingToSubscriber.put(sm.getSubscriptionIdentification(), s);

            // Subscriber to each translated subscriptions
            for (int i = 0; i < translatedSubscriptions.size(); i++) {
               SC translatedSubscription = translatedSubscriptions.get(i);
               LOG.debug("Subscribing with subscription: " + translatedSubscription);
               this.eventService.subscribe(s, translatedSubscription);
            }

            // Inform SubscriberDatabaseController
            // This guarantees the storage.
            this.eventService.getSubscriberDatabaseController().addSubscriptionMessageToSubscriberNetInf(this, sm);

            // Inform the SubscriptionExpirationController
            this.eventService.getSubscriptionExpirationController().addSubscriptionMessage(this.eventContainerID,
                  sm.getSubscriptionIdentification(), System.currentTimeMillis() + 1000 * sm.getExpires());
         } catch (NetInfCheckedException e) {
            LOG.error(e.getMessage());
            subscriptionResponse.setErrorMessage(e.getMessage());
         }
      }
      return subscriptionResponse;
   }

   public ESFUnsubscriptionResponse processUnsubscriptionRequest(ESFUnsubscriptionRequest usm) {
      processUnsubscriptionWithSubscriptionIdentification(usm.getSubscriptionIdentification());
      return new ESFUnsubscriptionResponse();
   }

   public void processUnsubscriptionWithSubscriptionIdentification(String subscriptionIdentification) {
      LOG.trace(null);

      // Check if such a message exists.
      ESFSubscriptionRequest storedSubscriptionRequest = this.subscriptionRequests.get(subscriptionIdentification);
      if (storedSubscriptionRequest == null) {
         String string = "Could not unsubscribe subscription with subscriptionIdentification = '" + subscriptionIdentification
         + "' since the according subscription does not exists";
         LOG.error(string);
         throw new NetInfUncheckedException(string);
      }
      this.subscriptionRequests.remove(subscriptionIdentification);

      List<SC> subscriptions = translateUnsubscriptionIdentification(subscriptionIdentification);
      S subscriber = this.mappingToSubscriber.get(subscriptionIdentification);

      for (int i = 0; i < subscriptions.size(); i++) {
         LOG.debug("Unsubscribing of subscription: " + subscriptions.get(i));
         this.eventService.unsubscribe(subscriber, subscriptions.get(i));
      }

      // Inform SubscriberController
      this.eventService.getSubscriberDatabaseController().removeSubscriptionMessageFromSubscriberNetInf(this,
            subscriptionIdentification);

      // Inform the SubscriptionExpirationController
      this.eventService.getSubscriptionExpirationController().removeSubscriptionMessage(this.eventContainerID, subscriptionIdentification);

      // Finally tear down the whole subscriber
      // One subscriber (of the according event service) is created
      // for each SubscriptionMessage.
      removeSubscriberOfSpecificEventService(subscriber);

      // Remove this subscription from the mapping table.
      this.mappingToSubscriptions.remove(subscriber);
      this.mappingToSubscriptionIdentification.remove(subscriber);
      this.mappingToSubscriber.remove(subscriptionIdentification);

      // Finally check, whether the SubscriberNetInf can be removed.
      checkAndRemoveSubscriberNetInf();
   }

   /**
    * In case this subscriberNetInf can be removed, it is removed from the <code>EventServiceFramework</code>. It can be removed,
    * if: 1) The connection is closed/null 2) The <code>SubscriberNetInf</code> does not have a single Subscription outstanding.
    */
   public void checkAndRemoveSubscriberNetInf() {
      LOG.trace(null);

      boolean result = true;

      result &= this.subscriberCommunicator == null;
      result &= this.mappingToSubscriber.size() == 0;
      result &= this.mappingToSubscriptionIdentification.size() == 0;
      result &= this.mappingToSubscriptions.size() == 0;

      if (result) {
         LOG.debug("Removing SubscriberNetInf with eventContainerId '" + this.eventContainerID + "'");
         this.eventService.removeSubscriberNetInf(this);
      }
   }

   public List<SC> translateUnsubscriptionIdentification(String subscriptionIdentification) {
      LOG.trace(null);

      S subscriber = this.mappingToSubscriber.get(subscriptionIdentification);
      List<SC> subscriptions = this.mappingToSubscriptions.get(subscriber);

      return subscriptions;
   }

   public ESFFetchMissedEventsResponse processFetchMissedEventsRequest() {
      LOG.trace(null);

      RemoteNodeConnection com = this.eventService.getRemoteNodeConnection();
      ESFFetchMissedEventsResponse response = new ESFFetchMissedEventsResponse();

      try {
         InformationObject informationObject = com.getIO(this.eventContainerID);

         if (informationObject != null) {
            for (Attribute attribute : informationObject.getAttributes()) {
               if (DefinedAttributeIdentification.EVENT.getURI().equals(attribute.getIdentification())) {
                  byte[] bytes = Utils.stringToBytes(attribute.getValue(String.class));
                  ESFEventMessage eventMessage = (ESFEventMessage) this.messageEncoder.decodeMessage(bytes);
                  response.addEventMessage(eventMessage);

                  informationObject.removeAttribute(attribute);
               }
            }

            com.putIO(informationObject);
         }

      } catch (NetInfCheckedException e) {
         LOG.info("EventContainerIO does not exist: " + e.getMessage());
      }

      return response;
   }

   public Identifier getEventContainerID() {
      return this.eventContainerID;
   }

   public void setEventContainerID(Identifier eventContainerID) {
      this.eventContainerID = eventContainerID;
   }

   public Identifier getPersonObjectID() {
      return this.personObjectID;
   }

   public void setPersonObjectID(Identifier personObjectID) {
      this.personObjectID = personObjectID;
   }

   public void setCommunicator(Communicator sc) {
      this.subscriberCommunicator = sc;

      if (this.subscriberCommunicator == null) {

         // In case we have lost the connection to the client
         // we might completely remove this subscriberNetInf
         checkAndRemoveSubscriberNetInf();
      }
   }

   public Communicator getCommunicator() {
      return this.subscriberCommunicator;
   }

   public List<ESFSubscriptionRequest> getSubscriptionRequests() {
      return new ArrayList<ESFSubscriptionRequest>(this.subscriptionRequests.values());
   }

   public abstract List<ESFEventMessage> translateEvent(E event);

   public abstract List<SC> translateSubscriptionRequest(ESFSubscriptionRequest subscriptionRequest)
   throws NetInfCheckedException;

   /**
    * The <code>SubscriberNetInf</code> can use several objects of the type <code>S</code> in order to achieve its functionality.
    * It is intended that for each object of the type <code>SubscriptionMessage</code> a internal subscriber is created.
    * 
    * @param subscriber
    */
   public abstract S createSubscriberOfSpecificEventService();

   public abstract void removeSubscriberOfSpecificEventService(S subscriber);
}
