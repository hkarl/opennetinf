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
package netinf.eventservice.siena.dummy;

import netinf.common.datamodel.DatamodelFactory;
import netinf.common.messages.ESFSubscriptionRequest;
import netinf.database.DatabaseConnecter;
import netinf.eventservice.framework.EventServiceNetInf;
import netinf.eventservice.framework.SubscriberNetInf;
import netinf.eventservice.framework.subscription.SubscriberDatabaseController;

import com.google.inject.Inject;

/**
 * This is a simple class that pretends to be a {@link SubscriberDatabaseController}. In effect it does really nothing.
 * 
 * @author PG Augnet 2, University of Paderborn
 * 
 */
@SuppressWarnings("unchecked")
public class MockSubscriberDatabaseController extends SubscriberDatabaseController {

   @Inject
   public MockSubscriberDatabaseController(EventServiceNetInf eventServiceNetInf, DatamodelFactory datamodelFactory,
         DatabaseConnecter databaseConnecter) {
      super(eventServiceNetInf, datamodelFactory, databaseConnecter);
   }

   @Override
   public void addSubscriberNetInf(SubscriberNetInf subscriberNetInf) {
   }

   @Override
   public void addSubscriptionMessageToSubscriberNetInf(SubscriberNetInf subscriberNetInf,
         ESFSubscriptionRequest esfSubscriptionMessage) {
   }

   @Override
   public void removeSubcriberNetInf(SubscriberNetInf subscriberNetInf) {
   }

   @Override
   public void removeSubscriptionMessageFromSubscriberNetInf(SubscriberNetInf subscriberNetInf,
         String subscriptionIdentification) {
   }

}
