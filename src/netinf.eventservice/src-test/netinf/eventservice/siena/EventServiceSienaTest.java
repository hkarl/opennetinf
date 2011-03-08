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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;
import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.Identifier;
import netinf.common.exceptions.NetInfCheckedException;
import netinf.common.messages.ESFSubscriptionRequest;
import netinf.common.utils.Utils;
import netinf.database.DatabaseConnecter;
import netinf.eventservice.framework.SubscriberNetInf;
import netinf.eventservice.siena.module.EventServiceSienaTestModule;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import siena.Filter;
import siena.HierarchicalDispatcher;
import siena.Notifiable;
import siena.Notification;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Tests the Siena Event Service
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class EventServiceSienaTest {

   private static final String CONFIGS_EVENTSERVICESIENA_TESTING_PROPERTIES = "../configs/testing/eventservicesiena_testing.properties";
   private static final String EVENT_CONTAINER_ID_STRING = "ni:HASH_OF_PK=123~HASH_OF_PK_IDENT=SHA1~"
         + "VERSION_KIND=UNVERSIONED~UNIQUE_LABEL=eventContainer";
   private static final String PERSON_IDENTITY_OBJECT_ID_STRING = "ni:HASH_OF_PK=123~HASH_OF_PK_IDENT=SHA1"
         + "~VERSION_KIND=UNVERSIONED~UNIQUE_LABEL=personIdentityObject";

   private static final String SUBSCRIPTION_IDENTIFICATION = "subscriptionIdentification";
   private static final String SPARQL_REQUEST = "SELECT ?old ?new WHERE {}";;
   private static final long LONG_EXPIRES = 1000;
   private static final long SHORT_EXPIRES = 1;

   private static Injector injector;
   private static EventServiceSiena eventServiceSiena;
   private static DatabaseConnecter databaseConnecter;
   private static DatamodelFactory datamodelFactory;

   private static Identifier eventContainerID;
   private static Identifier personIdentityObjectID;

   @BeforeClass
   public static void setUpTest() throws SQLException {
      Properties properties = Utils.loadProperties(CONFIGS_EVENTSERVICESIENA_TESTING_PROPERTIES);
      injector = Guice.createInjector(new EventServiceSienaTestModule(properties));
      eventServiceSiena = injector.getInstance(EventServiceSiena.class);
      datamodelFactory = injector.getInstance(DatamodelFactory.class);

      databaseConnecter = injector.getInstance(DatabaseConnecter.class);
      databaseConnecter.setup();
      Statement statement = databaseConnecter.getConnection().createStatement();
      statement.execute("DELETE from ec_po_mapping");
      statement.execute("DELETE from ec_sub_mapping");

      eventContainerID = datamodelFactory.createIdentifierFromString(EVENT_CONTAINER_ID_STRING);

      personIdentityObjectID = datamodelFactory.createIdentifierFromString(PERSON_IDENTITY_OBJECT_ID_STRING);

      statement.close();
   }

   @AfterClass
   public static void tearDown() {
      eventServiceSiena.tearDown();
   }

   @Test
   public void testSetup() {
      boolean setup = eventServiceSiena.setup();

      Assert.assertTrue(setup);
   }

   @Test
   public void testCreateSubscriber() throws SQLException {
      SubscriberNetInf<HierarchicalDispatcher, Notifiable, Notification, Filter> subscriberNetInf = eventServiceSiena
            .createSubscriberNetInf(eventContainerID, personIdentityObjectID);

      // Check if subscriber was correctly created
      Assert.assertEquals(eventContainerID, subscriberNetInf.getEventContainerID());
      Assert.assertEquals(personIdentityObjectID, subscriberNetInf.getPersonObjectID());

      // Check state of database
      Statement statement = databaseConnecter.getConnection().createStatement();
      statement.execute("SELECT * FROM ec_po_mapping WHERE event_container_id = '" + EVENT_CONTAINER_ID_STRING + "'");
      ResultSet resultSet = statement.getResultSet();

      int runs = 0;

      while (resultSet.next()) {
         runs++;

         String eventContainerIDString = resultSet.getString("event_container_id");
         String personIdentityObjectIDString = resultSet.getString("person_object_id");

         Assert.assertEquals(eventContainerIDString, EVENT_CONTAINER_ID_STRING);
         Assert.assertEquals(personIdentityObjectIDString, PERSON_IDENTITY_OBJECT_ID_STRING);
      }

      Assert.assertEquals(1, runs);
   }

   @SuppressWarnings("unchecked")
   @Test
   public void testCreateSubscription() throws SQLException, NetInfCheckedException {
      ESFSubscriptionRequest subscriptionRequest = new ESFSubscriptionRequest(SUBSCRIPTION_IDENTIFICATION, SPARQL_REQUEST,
            LONG_EXPIRES);
      SubscriberNetInf subscriberNetInf = eventServiceSiena.getSubscriberNetInf(eventContainerID);
      subscriberNetInf.processSubscriptionRequest(subscriptionRequest);

      // Check state of database
      Statement statement = databaseConnecter.getConnection().createStatement();
      statement.execute("SELECT * FROM ec_sub_mapping WHERE event_container_id = '" + EVENT_CONTAINER_ID_STRING
            + "' AND subscription_identification = '" + SUBSCRIPTION_IDENTIFICATION + "'");
      ResultSet resultSet = statement.getResultSet();

      int runs = 0;

      while (resultSet.next()) {
         runs++;

         String eventContainerIDString = resultSet.getString("event_container_id");
         String spaqlSubscription = resultSet.getString("sparql_subscription");
         String subscriptionIdentification = resultSet.getString("subscription_identification");
         long expires = resultSet.getLong("expiration_date");

         Assert.assertEquals(eventContainerIDString, EVENT_CONTAINER_ID_STRING);
         Assert.assertEquals(spaqlSubscription, SPARQL_REQUEST);
         Assert.assertEquals(subscriptionIdentification, SUBSCRIPTION_IDENTIFICATION);
         Assert.assertEquals(expires, LONG_EXPIRES);
      }

      Assert.assertEquals(1, runs);
   }

   @Test
   public void testTearDown() {
      boolean tearDown = eventServiceSiena.tearDown();

      Assert.assertTrue(tearDown);
   }

   @SuppressWarnings("unchecked")
   @Test
   public void testCorrectSetupFromDatabase() {
      boolean setup = eventServiceSiena.setup();

      Assert.assertTrue(setup);

      SubscriberNetInf newSubscriberNetInf = eventServiceSiena.getSubscriberNetInf(eventContainerID);

      // Check whether the subscriber is correct
      Assert.assertEquals(eventContainerID, newSubscriberNetInf.getEventContainerID());
      Assert.assertEquals(personIdentityObjectID, newSubscriberNetInf.getPersonObjectID());

      // Check whether subscriptions are correct
      List<ESFSubscriptionRequest> subscriptionRequests = newSubscriberNetInf.getSubscriptionRequests();
      Assert.assertEquals(1, subscriptionRequests.size());

      ESFSubscriptionRequest newSubscriptionRequest = subscriptionRequests.get(0);
      Assert.assertEquals(LONG_EXPIRES, newSubscriptionRequest.getExpires());
      Assert.assertEquals(SPARQL_REQUEST, newSubscriptionRequest.getSparqlSubscription());
      Assert.assertEquals(SUBSCRIPTION_IDENTIFICATION, newSubscriptionRequest.getSubscriptionIdentification());
   }

   @SuppressWarnings("unchecked")
   @Test
   public void testUnsubscription() {
      SubscriberNetInf newSubscriberNetInf = eventServiceSiena.getSubscriberNetInf(eventContainerID);
      newSubscriberNetInf.processUnsubscriptionWithSubscriptionIdentification(SUBSCRIPTION_IDENTIFICATION);

      List<ESFSubscriptionRequest> subscriptionRequests = newSubscriberNetInf.getSubscriptionRequests();
      Assert.assertEquals(0, subscriptionRequests.size());

      // Since the subscriber has no subscription and no connection over the communicator, he should have been removed
      SubscriberNetInf mustBeNull = eventServiceSiena.getSubscriberNetInf(eventContainerID);
      Assert.assertEquals(null, mustBeNull);
   }

   @SuppressWarnings("unchecked")
   @Test
   public void testExpiration() throws InterruptedException, NetInfCheckedException {
      SubscriberNetInf newSubscriberNetInf = eventServiceSiena.createSubscriberNetInf(eventContainerID, personIdentityObjectID);
      ESFSubscriptionRequest newSubscriptionRequest = new ESFSubscriptionRequest(SUBSCRIPTION_IDENTIFICATION, SPARQL_REQUEST,
            SHORT_EXPIRES);
      newSubscriberNetInf.processSubscriptionRequest(newSubscriptionRequest);

      Thread.sleep(1000 * SHORT_EXPIRES + 200);

      List<ESFSubscriptionRequest> subscriptionRequests = newSubscriberNetInf.getSubscriptionRequests();
      Assert.assertEquals(0, subscriptionRequests.size());

      // Since the subscriber has no subscription and no connection over the communicator, he should have been removed
      SubscriberNetInf mustBeNull = eventServiceSiena.getSubscriberNetInf(eventContainerID);
      Assert.assertEquals(null, mustBeNull);
   }
}
