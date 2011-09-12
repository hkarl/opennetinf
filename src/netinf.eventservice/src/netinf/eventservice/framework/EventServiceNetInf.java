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

import java.io.IOException;
import java.util.List;

import netinf.access.TCPServer;
import netinf.common.communication.AsyncReceiveHandler;
import netinf.common.communication.RemoteNodeConnection;
import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.Identifier;
import netinf.common.exceptions.NetInfCheckedException;
import netinf.eventservice.framework.module.Publisher;
import netinf.eventservice.framework.module.Subscriber;
import netinf.eventservice.framework.subscription.SubscriberDatabaseController;
import netinf.eventservice.framework.subscription.SubscriptionExpirationController;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * The Class EventServiceNetInf.
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
public abstract class EventServiceNetInf<ES, S, E, SC> {

   private static final Logger LOG = Logger.getLogger(EventServiceNetInf.class);

   private ES eventService;

   private TCPServer subscriberServer;
   private TCPServer publisherServer;
   private SubscriptionExpirationController subscriptionExpirationController;
   private SubscriberDatabaseController subscriberDatabaseController;
   private RemoteNodeConnection remoteNodeConnection;

   private SubscriberHandler subscriberHandler;
   private PublisherHandler publisherHandler;

   private String eventContainerPrefix;
   private Identifier identifier;

   @Inject
   public EventServiceNetInf() {
   }

   @Inject
   public void setHandler(@Subscriber AsyncReceiveHandler subscriberHandler, @Publisher AsyncReceiveHandler publisherHandler) {
      if (subscriberHandler instanceof SubscriberHandler) {
         this.subscriberHandler = (SubscriberHandler) subscriberHandler;
      } else {
         LOG.error("Could not set subscriberHandler");
      }

      if (publisherHandler instanceof PublisherHandler) {
         this.publisherHandler = (PublisherHandler) publisherHandler;
      } else {
         LOG.error("Could not set subscriberHandler");
      }
   }

   @Inject
   public void setIdentity(@Named("esf.identity") String identity, DatamodelFactory datamodelFactory) {
      this.identifier = datamodelFactory.createIdentifierFromString(identity);
   }

   @Inject
   public void setEventContainerPrefix(@Named("esf.containerPrefix") String eventContainerPrefix) {
      this.eventContainerPrefix = eventContainerPrefix;
   }

   @Inject
   public void setServer(@Subscriber TCPServer subscriberServer, @Publisher TCPServer publisherServer) {
      this.subscriberServer = subscriberServer;
      this.publisherServer = publisherServer;
   }

   @Inject
   public void setSubscriptionExpirationController(SubscriptionExpirationController subscriptionExpirationController) {
      this.subscriptionExpirationController = subscriptionExpirationController;
   }

   @Inject
   public void setSubscriptionDatabaseController(SubscriberDatabaseController subscriberDatabaseController) {
      this.subscriberDatabaseController = subscriberDatabaseController;
   }

   @Inject
   public void setConvenienceCommunicator(RemoteNodeConnection convenienceCommunicator) {
      this.remoteNodeConnection = convenienceCommunicator;
   }

   public RemoteNodeConnection getRemoteNodeConnection() {
      return this.remoteNodeConnection;
   }

   protected ES getEventService() {
      return this.eventService;
   }

   protected void setEventService(ES eventService) {
      this.eventService = eventService;
   }

   public boolean setup() {
      LOG.trace(null);

      boolean result = true;

      if (result) {
         result = setupEventService();
      }

      if (result) {
         result = setupPublisherServer();
      }

      if (result) {
         result = setupSubscriberServer();
      }

      if (result) {
         result = setupSubscriptionExpirationController();
      }

      // After everything else has been initialized.
      // Load the old data from the database.

      if (result) {
         result = setupSubscriptionDatabaseController();
      }

      return result;
   }

   private boolean setupSubscriptionExpirationController() {
      LOG.trace(null);

      boolean result = this.subscriptionExpirationController.setup();

      return result;
   }

   private boolean setupSubscriptionDatabaseController() {
      LOG.trace(null);

      boolean setup = this.subscriberDatabaseController.setup();
      if (!setup) {
         LOG.error("Could not setup subscription controller");
      }

      return setup;
   }

   private boolean setupSubscriberServer() {
      LOG.trace(null);

      boolean result = true;

      try {
         LOG.debug("Starting Subscriber server");
         this.subscriberServer.start();
      } catch (NetInfCheckedException e) {
         LOG.error("Could not start subscriber server on port " + this.subscriberServer.getAddress(), e);

         result = false;

         if (this.subscriberServer != null) {
            try {
               this.subscriberServer.stop();
            } catch (IOException e2) {
               LOG.error("Could not stop subscriber server on port " + this.subscriberServer.getAddress(), e2);
            }
         }
      }

      return result;
   }

   private boolean setupPublisherServer() {
      LOG.trace(null);

      boolean result = true;

      try {
         LOG.debug("Starting Publisher server");
         this.publisherServer.start();
      } catch (NetInfCheckedException e) {
         LOG.error("Could not start publisher server on port " + this.publisherServer.getAddress(), e);

         result = false;

         if (this.publisherServer != null) {
            try {
               this.publisherServer.stop();
            } catch (IOException e2) {
               LOG.error("Could not stop publisher server on port " + this.publisherServer.getAddress(), e2);
            }
         }
      }

      return result;

   }

   public boolean tearDown() {
      LOG.trace(null);

      boolean result = true;

      if (result) {
         result = tearDownEventService();
      }

      if (result) {
         result = tearDownSubscriberServer();
      }

      if (result) {
         result = tearDownSubscriber();
      }

      if (result) {
         result = tearDownPublisherServer();
      }

      if (result) {
         result = tearDownPublisher();
      }

      if (result) {
         result = tearDownSubscriptionExpirationController();
      }

      if (result) {
         result = tearDownSubscriptionDatabaseController();
      }

      return result;
   }

   private boolean tearDownSubscriptionDatabaseController() {
      boolean result = true;

      result = this.subscriberDatabaseController.tearDown();

      return result;
   }

   private boolean tearDownSubscriptionExpirationController() {
      boolean result = true;

      result = this.subscriptionExpirationController.tearDown();

      return result;
   }

   @SuppressWarnings("unchecked")
   private boolean tearDownPublisher() {
      LOG.trace(null);
      boolean result = true;

      List<PublisherNetInf> publisher = this.publisherHandler.getAllPublisherNetInf();

      for (PublisherNetInf publisherNetInf : publisher) {
         removePublisherNetInf(publisherNetInf);
      }

      return result;
   }

   private boolean tearDownSubscriber() {
      LOG.trace(null);
      boolean result = true;

      // Do NOT call remove subscriber, since this would remove them from the database
      // This is not what we want. We simply want create the state before the launch.
      result = this.subscriberHandler.tearDown();

      return result;
   }

   protected boolean tearDownSubscriberServer() {
      LOG.trace(null);

      boolean result = true;

      try {
         this.subscriberServer.stop();
      } catch (IOException e) {
         result = false;
         LOG.error("Failed to stop SubscriberServer: " + e.getMessage());
      }

      return result;
   }

   private boolean tearDownPublisherServer() {
      LOG.trace(null);

      boolean result = true;

      try {
         this.publisherServer.stop();
      } catch (IOException e) {
         result = false;
         LOG.error("Failed to stop PublisherServer: " + e.getMessage());
      }

      return result;
   }

   public PublisherNetInf<ES, S, E, SC> createPublisherNetInf() {
      // This method can be used in order for the management of the
      // different publisher

      PublisherNetInf<ES, S, E, SC> publisherNetInf = createPublisherNetInfForSpecificEventService();
      this.publisherHandler.addPublisherNetInf(publisherNetInf);
      return publisherNetInf;
   }

   public SubscriberNetInf<ES, S, E, SC> createSubscriberNetInf(Identifier eventContainerID, Identifier personObjectID) {
      LOG.trace(null);

      // This method can be used for the management of the
      // different subscriber
      LOG.debug("Creating SubscribernetInf for eventContainerID '" + eventContainerID + "' and personObjectId '" + personObjectID
            + "'");

      SubscriberNetInf<ES, S, E, SC> subscriberNetInf = createSubscriberNetInfForSpecificEventService();
      subscriberNetInf.setPersonObjectID(personObjectID);
      subscriberNetInf.setEventContainerID(eventContainerID);

      this.subscriberHandler.addSubscriberNetInf(subscriberNetInf);

      this.subscriberDatabaseController.addSubscriberNetInf(subscriberNetInf);

      return subscriberNetInf;
   }

   public void removePublisherNetInf(PublisherNetInf<ES, S, E, SC> publisherNetInf) {
      removePublisherNetInfOfSpecificEventService(publisherNetInf);
      this.publisherHandler.removePublisherNetInf(publisherNetInf);
   }

   public void removeSubscriberNetInf(SubscriberNetInf<ES, S, E, SC> subscriberNetInf) {
      // Regard the reverse direction of removing and creating!

      removeSubscriberNetInfOfSpecificEventService(subscriberNetInf);

      this.subscriberDatabaseController.removeSubcriberNetInf(subscriberNetInf);

      this.subscriberHandler.removeSubscriberNetInf(subscriberNetInf);
   }

   public SubscriberDatabaseController getSubscriberDatabaseController() {
      return this.subscriberDatabaseController;
   }

   public Identifier getIdentifer() {
      return this.identifier;
   }

   public String getEventContainerPrefix() {
      return this.eventContainerPrefix;
   }

   public SubscriptionExpirationController getSubscriptionExpirationController() {
      return this.subscriptionExpirationController;
   }

   @SuppressWarnings("unchecked")
   public SubscriberNetInf<ES, S, E, SC> getSubscriberNetInf(Identifier eventContainerId) {
      return this.subscriberHandler.getSubscriberByIdentifier(eventContainerId);
   }

   /**
    * Intended to setup the specific event service. All these methods depend on particular settings and invocations of the
    * underlying event service.
    */
   protected abstract boolean setupEventService();

   protected abstract boolean tearDownEventService();

   // These methods are intended to create the publisher and the subscriber
   // that are specific for each implemented event service
   protected abstract PublisherNetInf<ES, S, E, SC> createPublisherNetInfForSpecificEventService();

   protected abstract SubscriberNetInf<ES, S, E, SC> createSubscriberNetInfForSpecificEventService();

   public abstract void publish(E e);

   public abstract void subscribe(S s, SC sc);

   public abstract void unsubscribe(S s, SC sc);

   protected abstract void removeSubscriberNetInfOfSpecificEventService(SubscriberNetInf<ES, S, E, SC> subscriberNetInf);

   protected abstract void removePublisherNetInfOfSpecificEventService(PublisherNetInf<ES, S, E, SC> publisherNetInf);
}
