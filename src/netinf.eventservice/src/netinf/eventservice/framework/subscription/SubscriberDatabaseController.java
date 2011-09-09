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
package netinf.eventservice.framework.subscription;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.Identifier;
import netinf.common.messages.ESFSubscriptionRequest;
import netinf.database.DatabaseConnecter;
import netinf.eventservice.framework.EventServiceNetInf;
import netinf.eventservice.framework.SubscriberNetInf;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * Stores subscriber in database Reads subscriber from database
 * 
 * @author PG Augnet 2, University of Paderborn
 */

@SuppressWarnings("unchecked")
public class SubscriberDatabaseController {

   private static final Logger LOG = Logger.getLogger(SubscriberDatabaseController.class);
   private final EventServiceNetInf eventServiceNetInf;
   private final DatabaseConnecter databaseConnecter;

   // Shows, whether the setup method was already called, and successfully ran.
   private boolean inSetup;
   private final DatamodelFactory datamodelFactory;

   // Constants
   private String tableEcPoMapping;
   private String tableEcSubMapping;

   @Inject
   public SubscriberDatabaseController(EventServiceNetInf eventServiceNetInf, DatamodelFactory datamodelFactory,
         DatabaseConnecter databaseConnecter) {
      this.eventServiceNetInf = eventServiceNetInf;
      this.datamodelFactory = datamodelFactory;
      this.databaseConnecter = databaseConnecter;
   }

   @Inject
   public void setTableNames(@Named("database.table.table_ec_po_mapping") String tableEcPoMapping,
         @Named("database.table.table_ec_sub_mapping") String tableEcSubMapping) {
      this.tableEcPoMapping = tableEcPoMapping;
      this.tableEcSubMapping = tableEcSubMapping;
   }

   public boolean setup() {
      inSetup = true;

      boolean result = true;

      if (result) {
         result = setupDatabaseConnecter();
      }

      if (result) {
         result = setupData();
      }

      // Setup phase is over
      inSetup = false;

      return result;
   }

   public boolean tearDown() {
      LOG.trace(null);
      boolean result = true;

      try {
         databaseConnecter.close();
      } catch (SQLException e) {
         LOG.error("Could not tear down SubscriberDatabaseController", e);
         result = false;
      }
      return result;
   }

   private boolean setupDatabaseConnecter() {
      LOG.trace(null);

      boolean result = false;
      result = databaseConnecter.setup();

      return result;
   }

   private boolean setupData() {
      LOG.trace(null);

      boolean result = true;

      // Create connection to database
      Connection connection = databaseConnecter.getConnection();

      // Initialize subscriber
      Statement statement = null;
      String sqlQuery = null;
      try {
         sqlQuery = "SELECT * FROM " + tableEcPoMapping;
         statement = connection.prepareStatement(sqlQuery, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);

         LOG.debug("Executing sql query '" + sqlQuery + "'");
         ResultSet query = statement.executeQuery(sqlQuery);

         setupSetOfSubscriberNetInfs(query);
      } catch (SQLException e) {
         LOG.error("Could not create sql query '" + sqlQuery + "'", e);
         result = false;
      } finally {
         try {
            statement.close();
         } catch (SQLException e) {
            LOG.error("Could not close statement", e);
         }
      }

      // Initialise subscription
      try {
         sqlQuery = "SELECT * FROM " + tableEcSubMapping;
         statement = connection.prepareStatement(sqlQuery, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);

         LOG.debug("Executing sql query '" + sqlQuery + "'");
         ResultSet query = statement.executeQuery(sqlQuery);

         setupSubscriptionMessages(query);
      } catch (SQLException e) {
         LOG.error("Could not create sql query '" + sqlQuery + "'", e);
         result = false;
      } finally {
         try {
            statement.close();
         } catch (SQLException e) {
            LOG.error("Could not close statement", e);
         }
      }

      return result;
   }

   private void setupSubscriptionMessages(ResultSet query) throws SQLException {
      LOG.trace(null);

      while (query.next()) {
         String eventContainerIDString = query.getString("event_container_id");
         String expirationDate = query.getString("expiration_date");
         String subscriptionIdentification = query.getString("subscription_identification");
         String sparqlSubscription = query.getString("sparql_subscription");

         Identifier eventContainerID = datamodelFactory.createIdentifierFromString(eventContainerIDString);

         ESFSubscriptionRequest esfSubscriptionRequest = new ESFSubscriptionRequest(subscriptionIdentification,
               sparqlSubscription, Long.parseLong(expirationDate));

         SubscriberNetInf subscriber = eventServiceNetInf.getSubscriberNetInf(eventContainerID);
         subscriber.processSubscriptionRequest(esfSubscriptionRequest);
      }
   }

   private void setupSetOfSubscriberNetInfs(ResultSet query) throws SQLException {
      while (query.next()) {
         String eventContainerIDString = query.getString("event_container_id");
         String personObjectIDString = query.getString("person_object_id");

         Identifier eventContainerID = datamodelFactory.createIdentifierFromString(eventContainerIDString);

         Identifier personObjectID = datamodelFactory.createIdentifierFromString(personObjectIDString);

         eventServiceNetInf.createSubscriberNetInf(eventContainerID, personObjectID);
      }
   }

   /**
    * Update database to always reflect same status like in the programm has
    * 
    * @param subscriberNetInf
    */
   public void addSubscriberNetInf(SubscriberNetInf subscriberNetInf) {
      LOG.trace(null);

      // The database does not have to be updated during setup phase
      if (!inSetup) {
         String eventContainerID = subscriberNetInf.getEventContainerID().toString();
         String personObjectID = subscriberNetInf.getPersonObjectID().toString();

         Connection connection = databaseConnecter.getConnection();

         PreparedStatement statement = null;
         try {
            String sqlQuery = "REPLACE INTO " + tableEcPoMapping + "(event_container_id, person_object_id) VALUES(?,?)";

            statement = connection.prepareStatement(sqlQuery);
            statement.setString(1, eventContainerID);
            statement.setString(2, personObjectID);

            LOG.debug("Executing sql query '" + statement.toString() + "'");
            statement.executeUpdate();

         } catch (SQLException e) {
            LOG.error("Could not create sql query '" + statement.toString() + "'", e);
         } finally {
            try {
               statement.close();
            } catch (SQLException e) {
               LOG.error("Could not close statement", e);
            }
         }
      }
   }

   /**
    * Update database to always reflect same status like in the programm has
    */
   public void removeSubcriberNetInf(SubscriberNetInf subscriberNetInf) {
      LOG.trace(null);

      String eventContainerID = subscriberNetInf.getEventContainerID().toString();

      // Update database
      Connection connection = databaseConnecter.getConnection();

      PreparedStatement statement = null;
      try {
         String sqlQuery = "DELETE FROM " + tableEcPoMapping + " WHERE event_container_id=?";

         statement = connection.prepareStatement(sqlQuery);
         statement.setString(1, eventContainerID);

         LOG.debug("Executing sql query '" + statement.toString() + "'");
         statement.executeUpdate();

      } catch (SQLException e) {
         LOG.error("Could not run sql-query '" + statement.toString() + "'", e);
      } finally {
         try {
            statement.close();
         } catch (SQLException e) {
            LOG.error("Could not close statement", e);
         }
      }
   }

   /**
    * Update database to always reflect same status like in the programm has
    * 
    * @param subscriberNetInf
    * @param esfSubscriptionMessage
    */
   public void addSubscriptionMessageToSubscriberNetInf(SubscriberNetInf subscriberNetInf,
         ESFSubscriptionRequest esfSubscriptionMessage) {
      LOG.trace(null);

      if (!inSetup) {
         String eventContainerIDString = subscriberNetInf.getEventContainerID().toString();

         String sparqlSubscription = esfSubscriptionMessage.getSparqlSubscription();
         String subscriptionIdentification = esfSubscriptionMessage.getSubscriptionIdentification();
         long expirationDate = esfSubscriptionMessage.getExpires();

         Connection connection = databaseConnecter.getConnection();

         PreparedStatement statement = null;
         try {
            String sqlQuery = "INSERT INTO " + tableEcSubMapping
                  + "(event_container_id, sparql_subscription, subscription_identification, expiration_date) VALUES(?,?,?,?)";

            statement = connection.prepareStatement(sqlQuery);
            statement.setString(1, eventContainerIDString);
            statement.setString(2, sparqlSubscription);
            statement.setString(3, subscriptionIdentification);
            statement.setBigDecimal(4, new BigDecimal(expirationDate));

            LOG.debug("Executing sql query '" + statement.toString() + "'");
            statement.executeUpdate();

         } catch (SQLException e) {
            LOG.error("Could not create sql query '" + statement.toString() + "'", e);
         } finally {
            try {
               statement.close();
            } catch (SQLException e) {
               LOG.error("Could not close statement", e);
            }
         }
      }
   }

   /**
    * Update database to always reflect same status like in the programm has
    * 
    * @param subscriberNetInf
    * @param subscriptionIdentification
    */
   public void removeSubscriptionMessageFromSubscriberNetInf(SubscriberNetInf subscriberNetInf,
         String subscriptionIdentification) {
      LOG.trace(null);

      String eventContainerID = subscriberNetInf.getEventContainerID().toString();

      // Update database
      Connection connection = databaseConnecter.getConnection();

      PreparedStatement statement = null;
      try {
         String sqlQuery = "DELETE FROM " + tableEcSubMapping + " WHERE event_container_id=? AND subscription_identification=?";

         statement = connection.prepareStatement(sqlQuery);
         statement.setString(1, eventContainerID);
         statement.setString(2, subscriptionIdentification);

         LOG.debug("Executing sql query '" + statement.toString() + "'");
         statement.executeUpdate();

      } catch (SQLException e) {
         LOG.error("Could not create sql query '" + statement.toString() + "'", e);
      } finally {
         try {
            statement.close();
         } catch (SQLException e) {
            LOG.error("Could not close statement", e);
         }
      }

   }
}
