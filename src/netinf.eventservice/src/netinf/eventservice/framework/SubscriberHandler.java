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
/**
 * 
 */
package netinf.eventservice.framework;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import netinf.common.communication.AsyncReceiveHandler;
import netinf.common.communication.Communicator;
import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.DefinedLabelName;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.IdentifierLabel;
import netinf.common.messages.ESFFetchMissedEventsRequest;
import netinf.common.messages.ESFFetchMissedEventsResponse;
import netinf.common.messages.ESFRegistrationRequest;
import netinf.common.messages.ESFRegistrationResponse;
import netinf.common.messages.ESFSubscriptionRequest;
import netinf.common.messages.ESFSubscriptionResponse;
import netinf.common.messages.ESFUnsubscriptionRequest;
import netinf.common.messages.ESFUnsubscriptionResponse;
import netinf.common.messages.NetInfMessage;
import netinf.common.security.SignatureAlgorithm;

import org.apache.log4j.Logger;

import com.google.inject.Inject;

/**
 * TODO: This class can be made more performant by the usage of hashtables.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
@SuppressWarnings("unchecked")
public class SubscriberHandler implements AsyncReceiveHandler {
   private static final Logger LOG = Logger.getLogger(SubscriberHandler.class);

   private final EventServiceNetInf eventService;
   private final ArrayList<SubscriberNetInf> subscriberStorage;
   
   private DatamodelFactory datamodelFactory;
   private SignatureAlgorithm signatureAlgorithm;

   @Inject
   public SubscriberHandler(EventServiceNetInf eventService) {
      this.eventService = eventService;
      subscriberStorage = new ArrayList<SubscriberNetInf>();
   }
   
   @Inject
   public void setSignatureAlgorithm(SignatureAlgorithm signatureAlgorithm) {
      this.signatureAlgorithm = signatureAlgorithm;
   }
   
   @Inject
   public void setDatamodelFactory(DatamodelFactory datamodelFactory) {
      this.datamodelFactory = datamodelFactory;
   }

   @Override
   public void receivedMessage(NetInfMessage message, Communicator arrivedOver) {
      LOG.trace(null);

      if (message instanceof ESFRegistrationRequest) {
         LOG.debug("Received a ESFRegistrationRequest");

         ESFRegistrationRequest request = (ESFRegistrationRequest) message;
         Identifier eventContainerID = request.getEventContainerIdentifier();
         Identifier personObjectID = request.getPersonObjectIdentifier();
         
         if (eventContainerID == null) {
            eventContainerID = buildEventContainerIdentifier(personObjectID);
         }

         // Check whether the subscriber is already connected
         SubscriberNetInf subscriberNetInf = getSubscriberByIdentifier(eventContainerID);
         if (subscriberNetInf != null) {
            LOG.debug("SubscriberNetInf for given EventContainer already exists");
            subscriberNetInf.setCommunicator(arrivedOver);
         } else {
            LOG.debug("SubscriberNetInf for given EventContainer does not exist. Creating a new one...");
            subscriberNetInf = eventService.createSubscriberNetInf(eventContainerID, personObjectID);
            addSubscriberNetInf(subscriberNetInf);
            subscriberNetInf.setCommunicator(arrivedOver);
         }

         LOG.debug("Building ESFRegistrationResponse");
         ESFRegistrationResponse response = new ESFRegistrationResponse(subscriberNetInf
               .getEventContainerID());
         arrivedOver.sendAsync(response);
      } else if (message instanceof ESFSubscriptionRequest) {
         LOG.debug("Received a ESFSubscriptionRequest");

         SubscriberNetInf subscriberNetInf = getSubscriberByCommunicator(arrivedOver);
         ESFSubscriptionResponse response = subscriberNetInf
               .processSubscriptionRequest((ESFSubscriptionRequest) message);

         arrivedOver.sendAsync(response);
      } else if (message instanceof ESFUnsubscriptionRequest) {
         LOG.debug("Received a ESFUnsubscriptionRequest");

         ESFUnsubscriptionRequest request = (ESFUnsubscriptionRequest) message;
         SubscriberNetInf subscriberNetInf = getSubscriberByCommunicator(arrivedOver);
         ESFUnsubscriptionResponse response = subscriberNetInf.processUnsubscriptionRequest(request);

         arrivedOver.sendAsync(response);
      } else if (message instanceof ESFFetchMissedEventsRequest) {
         LOG.debug("Received ESFFetchMissedEventsRequest");

         SubscriberNetInf subscriberNetInf = getSubscriberByCommunicator(arrivedOver);
         ESFFetchMissedEventsResponse response = subscriberNetInf.processFetchMissedEventsRequest();
         arrivedOver.sendAsync(response);
      }
   }

   private Identifier buildEventContainerIdentifier(Identifier personObjectId) {
      LOG.trace(null);
      
      String personObjectIdentifierHash = null;
      try {
         personObjectIdentifierHash = signatureAlgorithm.hash(personObjectId.toString(), "SHA1");
      } catch (NoSuchAlgorithmException e) {
         LOG.error(e.getMessage());
      }

      IdentifierLabel publicKeyHash = eventService.getIdentifer().getIdentifierLabel(DefinedLabelName.HASH_OF_PK.getLabelName());
      String uniqueLabel = eventService.getEventContainerPrefix() + personObjectIdentifierHash;

      IdentifierLabel identifierLabel1 = datamodelFactory.createIdentifierLabel();
      identifierLabel1.setLabelName(DefinedLabelName.HASH_OF_PK.getLabelName());
      identifierLabel1.setLabelValue(publicKeyHash.getLabelValue());

      IdentifierLabel identifierLabel2 = datamodelFactory.createIdentifierLabel();
      identifierLabel2.setLabelName(DefinedLabelName.UNIQUE_LABEL.getLabelName());
      identifierLabel2.setLabelValue(uniqueLabel);

      Identifier identifier = datamodelFactory.createIdentifier();
      identifier.addIdentifierLabel(identifierLabel1);
      identifier.addIdentifierLabel(identifierLabel2);
      
      return identifier;
   }

   public void addSubscriberNetInf(SubscriberNetInf subscriberNetInf) {
      LOG.trace(null);

      if (!subscriberStorage.contains(subscriberNetInf)) {
         subscriberStorage.add(subscriberNetInf);
      }
   }

   public SubscriberNetInf getSubscriberByCommunicator(Communicator communicator) {
      SubscriberNetInf result = null;

      for (SubscriberNetInf subscriberNetInf : subscriberStorage) {
         if (communicator.equals(subscriberNetInf.getCommunicator())) {
            result = subscriberNetInf;
            break;
         }
      }

      return result;
   }

   /**
    * Here the identifier of eventcontainer belonging to the subscriber is meant.
    * 
    * @return
    */
   public SubscriberNetInf getSubscriberByIdentifier(Identifier eventContainerID) {
      if (eventContainerID == null) {
         return null;
      }
      
      SubscriberNetInf result = null;

      for (SubscriberNetInf subscriberNetInf : subscriberStorage) {
         if (eventContainerID.equals(subscriberNetInf.getEventContainerID())) {
            result = subscriberNetInf;
            break;
         }
      }

      return result;
   }

   public void removeSubscriberNetInf(SubscriberNetInf subscriberNetInf) {
      LOG.trace(null);

      boolean remove = subscriberStorage.remove(subscriberNetInf);

      if (remove) {
         LOG.debug("Removed subscriberNetInf");
      } else {
         LOG.error("Could not remove subscriberNetInf, since not present");
      }
   }

   public List<SubscriberNetInf> getAllSubscriberNetInf() {
      // Must return a copy.
      return new ArrayList<SubscriberNetInf>(subscriberStorage);
   }

   /**
    * Remove all subscribers immediately. Does not affect the database.
    * 
    * @return
    */
   public boolean tearDown() {
      LOG.trace(null);

      boolean result = true;

      subscriberStorage.clear();

      return result;
   }

}
