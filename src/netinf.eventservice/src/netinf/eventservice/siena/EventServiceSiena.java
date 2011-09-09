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

import java.io.IOException;

import netinf.eventservice.framework.EventServiceNetInf;
import netinf.eventservice.framework.PublisherNetInf;
import netinf.eventservice.framework.SubscriberNetInf;

import org.apache.log4j.Logger;

import siena.Filter;
import siena.HierarchicalDispatcher;
import siena.Notifiable;
import siena.Notification;
import siena.SienaException;
import siena.comm.InvalidSenderException;
import siena.comm.PacketSenderException;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

/**
 * Integration of Siena in our EventService Framework EventService (ES) = Siena Subscriber (S) = Notifiable Event (E) =
 * Notification Subscription (SC) = Filter
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class EventServiceSiena extends
EventServiceNetInf<HierarchicalDispatcher, Notifiable, Notification, Filter> {

   private static final Logger LOG = Logger.getLogger(EventServiceSiena.class);

   private String subscriptionStorage;
   private String subscriptionRefreshTime;
   private String masterBroker;

   private Provider<SubscriberSiena> subscriberProvider;
   private Provider<PublisherSiena> publisherProvider;

   @Inject
   public EventServiceSiena() {
      super();
      LOG.trace(null);
   }

   @Inject
   public void setSubscriberProvider(Provider<SubscriberSiena> subscriberProvider) {
      this.subscriberProvider = subscriberProvider;
   }

   @Inject
   public void setPublisherProvider(Provider<PublisherSiena> publisherProvider) {
      this.publisherProvider = publisherProvider;
   }

   @Inject
   public void setSienaData(@Named("siena.subscription_storage.name") String subscriptionStorage,
         @Named("siena.subscription_storage.refresh_time") String subscriptionRefreshtime,
         @Named("siena.master_broker") String masterBroker) {
      this.subscriptionStorage = subscriptionStorage;
      this.subscriptionRefreshTime = subscriptionRefreshtime;
      if (!masterBroker.equals("")) {
         this.masterBroker = masterBroker;
      }
   }

   @Override
   protected boolean setupEventService() {
      LOG.trace(null);
      boolean result = true;

      HierarchicalDispatcher eventService = new HierarchicalDispatcher();
      setEventService(eventService);

      try {
         eventService.initStore(this.subscriptionStorage);
      } catch (IOException e) {
         result = false;
         LOG.error("IOException occured", e);
      } catch (SienaException e) {
         result = false;
         LOG.error("SienaException occured", e);
      }

      int refreshTimeout = Integer.parseInt(this.subscriptionRefreshTime);
      eventService.setStoreRefreshTimeout(refreshTimeout);

      // Check whether we are running standalone, or have to connect to another siena event broker
      if (this.masterBroker != null) {
         try {
            eventService.setMaster(this.masterBroker);
         } catch (InvalidSenderException e) {
            result = false;
            LOG.error("Could not connect to the master event broker '" + this.masterBroker + "'", e);
         } catch (PacketSenderException e) {
            result = false;
            LOG.error("Could not connect to the master event broker '" + this.masterBroker + "'", e);
         } catch (IOException e) {
            result = false;
            LOG.error("Could not connect to the master event broker '" + this.masterBroker + "'", e);
         }
      }

      return result;
   }

   @Override
   protected PublisherNetInf<HierarchicalDispatcher, Notifiable, Notification, Filter>
   createPublisherNetInfForSpecificEventService() {
      LOG.trace(null);

      PublisherSiena publisherSiena = this.publisherProvider.get();
      return publisherSiena;
   }

   @Override
   protected SubscriberNetInf<HierarchicalDispatcher, Notifiable, Notification, Filter>
   createSubscriberNetInfForSpecificEventService() {
      LOG.trace(null);

      SubscriberSiena subscriberSiena = this.subscriberProvider.get();
      return subscriberSiena;
   }

   @Override
   public void publish(Notification e) {
      LOG.trace(null);

      try {
         LOG.debug("Trying to publish notification: " + e);
         getEventService().publish(e);
      } catch (SienaException e1) {
         LOG.error("Could not publish notificaiton to event service siena", e1);
      }
   }

   @Override
   public void subscribe(Notifiable s, Filter sc) {
      LOG.trace(null);

      try {
         LOG.debug("Subscribing to event service siena with filter: " + sc);

         getEventService().subscribe(sc, s);
      } catch (SienaException e) {
         LOG.error("Could not subscribe to event service siena", e);
      }
   }

   @Override
   protected boolean tearDownEventService() {
      LOG.trace(null);
      getEventService().shutdown();

      return true;
   }

   @Override
   public void unsubscribe(Notifiable s, Filter sc) {
      LOG.debug("Unsubscribing in event service siena with filter: " + sc);
      getEventService().unsubscribe(sc, s);
   }

   @Override
   protected void removePublisherNetInfOfSpecificEventService(
         PublisherNetInf<HierarchicalDispatcher, Notifiable, Notification, Filter> publisherNetInf) {
   }

   @Override
   protected void removeSubscriberNetInfOfSpecificEventService(
         SubscriberNetInf<HierarchicalDispatcher, Notifiable, Notification, Filter> subscriberNetInf) {

   }
}
