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
package netinf.eventservice.siena;

import java.sql.SQLException;
import java.util.Properties;

import junit.framework.Assert;
import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.IdentifierLabel;
import netinf.common.datamodel.InformationObject;
import netinf.common.messages.ESFEventMessage;
import netinf.common.messages.ESFFetchMissedEventsResponse;
import netinf.common.utils.Utils;
import netinf.eventservice.framework.SubscriberNetInf;
import netinf.eventservice.siena.module.EventServiceSienaTestModule;

import org.junit.BeforeClass;
import org.junit.Test;

import siena.Filter;
import siena.HierarchicalDispatcher;
import siena.Notifiable;
import siena.Notification;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Tests storage of EventObjects in EventContainer IOs
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class EventContainerTest {

   private static final String CONFIGS_EVENTSERVICESIENA_TESTING_PROPERTIES = "../configs_official/eventservicesiena_testing.properties";
   private static final String EVENT_CONTAINER_ID_STRING = "ni:HASH_OF_PK=123~HASH_OF_PK_IDENT=SHA1"
      + "~VERSION_KIND=UNVERSIONED~UNIQUE_LABEL=eventContainer";
   private static final String PERSON_IDENTITY_OBJECT_ID_STRING = "ni:HASH_OF_PK=123~HASH_OF_PK_IDENT=SHA1"
      + "~VERSION_KIND=UNVERSIONED~UNIQUE_LABEL=personIdentityObject";

   private static final String SUBSCRIPTION_IDENTIFICATION = "subscriptionIdentification";

   private static Injector injector;
   private static EventServiceSiena eventServiceSiena;
   private static DatamodelFactory datamodelFactory;

   private static Identifier eventContainerID;
   private static Identifier personIdentityObjectID;

   private static SubscriberNetInf<HierarchicalDispatcher, Notifiable, Notification, Filter> subscriberNetInf;

   @BeforeClass
   public static void setUpTest() throws SQLException {
      Properties properties = Utils.loadProperties(CONFIGS_EVENTSERVICESIENA_TESTING_PROPERTIES);
      injector = Guice.createInjector(new EventServiceSienaTestModule(properties));
      eventServiceSiena = injector.getInstance(EventServiceSiena.class);
      datamodelFactory = injector.getInstance(DatamodelFactory.class);

      eventContainerID = datamodelFactory.createIdentifierFromString(EVENT_CONTAINER_ID_STRING);

      personIdentityObjectID = datamodelFactory.createIdentifierFromString(PERSON_IDENTITY_OBJECT_ID_STRING);
   }

   @Test
   public void testSetup() {
      boolean setup = eventServiceSiena.setup();
      Assert.assertTrue(setup);

      subscriberNetInf = eventServiceSiena.createSubscriberNetInf(eventContainerID, personIdentityObjectID);
   }

   @Test
   public void testFetchFromMissingEventContainer() {
      ESFFetchMissedEventsResponse response = subscriberNetInf.processFetchMissedEventsRequest();
      Assert.assertEquals(0, response.getEventMessages().size());
   }

   @Test
   public void testAppendToAndFetchFromEventContainer() throws SQLException {
      Identifier identifier = createTestIdentifier();

      // Create an EventMessage
      InformationObject oldIO = datamodelFactory.createInformationObject();
      oldIO.setIdentifier(identifier);
      oldIO.addAttribute(datamodelFactory.createAttribute("testID", "oldValue"));

      InformationObject newIO = datamodelFactory.createInformationObject();
      newIO.setIdentifier(identifier);
      newIO.addAttribute(datamodelFactory.createAttribute("testID", "newValue"));

      ESFEventMessage eventMessage = new ESFEventMessage();
      eventMessage.setMatchedSubscriptionIdentification(SUBSCRIPTION_IDENTIFICATION);
      eventMessage.setOldInformationObject(oldIO);
      eventMessage.setNewInformationObject(newIO);

      // Populate EventContainer IO and check its contents
      subscriberNetInf.appendEventMessageToEventContainerIO(eventMessage);
      ESFFetchMissedEventsResponse response = subscriberNetInf.processFetchMissedEventsRequest();

      Assert.assertEquals(1, response.getEventMessages().size());
      ESFEventMessage message = response.getEventMessages().get(0);
      Assert.assertEquals(SUBSCRIPTION_IDENTIFICATION, message.getMatchedSubscriptionIdentification());
      Assert.assertEquals("oldValue", message.getOldInformationObject().getSingleAttribute("testID").getValue(String.class));
      Assert.assertEquals("newValue", message.getNewInformationObject().getSingleAttribute("testID").getValue(String.class));

      // All EventObjects should have been removed
      response = subscriberNetInf.processFetchMissedEventsRequest();
      Assert.assertEquals(0, response.getEventMessages().size());
   }

   private static Identifier createTestIdentifier() {
      IdentifierLabel identifierLabel = datamodelFactory.createIdentifierLabel();
      identifierLabel.setLabelName("testName");
      identifierLabel.setLabelValue("testValue");

      Identifier identifier = datamodelFactory.createIdentifier();
      identifier.addIdentifierLabel(identifierLabel);

      return identifier;
   }

   @Test
   public void testTearDown() {
      boolean tearDown = eventServiceSiena.tearDown();

      Assert.assertTrue(tearDown);
   }
}
