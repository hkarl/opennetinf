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

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;
import netinf.common.communication.SerializeFormat;
import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.IdentifierLabel;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.attribute.Attribute;
import netinf.common.exceptions.NetInfCheckedException;
import netinf.common.messages.ESFEventMessage;
import netinf.common.messages.ESFFetchMissedEventsResponse;
import netinf.common.messages.ESFSubscriptionRequest;
import netinf.common.utils.Utils;
import netinf.eventservice.framework.PublisherNetInf;
import netinf.eventservice.framework.SubscriberNetInf;
import netinf.eventservice.siena.exceptions.SubscriptionInfiniteVariableRecursionException;
import netinf.eventservice.siena.exceptions.SubscriptionInvalidFilterException;
import netinf.eventservice.siena.exceptions.SubscriptionInvalidFilterOperatorException;
import netinf.eventservice.siena.exceptions.SubscriptionInvalidResultVariablesException;
import netinf.eventservice.siena.exceptions.SubscriptionInvalidSolutionModifierException;
import netinf.eventservice.siena.exceptions.SubscriptionInvalidTripleObjectException;
import netinf.eventservice.siena.exceptions.SubscriptionInvalidTriplePredicateException;
import netinf.eventservice.siena.exceptions.SubscriptionInvalidTripleSubjectException;
import netinf.eventservice.siena.exceptions.SubscriptionInvalidWhereClauseException;
import netinf.eventservice.siena.module.EventServiceSienaTranslationTestModule;
import netinf.eventservice.siena.translation.TranslationSiena;

import org.junit.BeforeClass;
import org.junit.Test;

import siena.AttributeConstraint;
import siena.Filter;
import siena.HierarchicalDispatcher;
import siena.Notifiable;
import siena.Notification;
import siena.Op;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Tests the translation of ESFSubscriptionRequest instances
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class SubscriptionTranslationTest {

   private static final String PERSON_IDENTITY_OBJECT_ID_STRING = "HASH_OF_PK=123~HASH_OF_PK_IDENT=SHA1"
         + "~VERSION_KIND=UNVERSIONED~UNIQUE_LABEL=personIdentityObject";
   private static final String SUBSCRIPTION_ID = "TestIdentification";
   private static final String TESTING_PROPERTIES = "../configs/testing/eventservicesiena_testing.properties";
   private static Injector injector;
   private static EventServiceSiena eventServiceSiena;
   private static DatamodelFactory datamodelFactory;
   private static Identifier personIdentityObjectID;
   private static SubscriberNetInf<HierarchicalDispatcher, Notifiable, Notification, Filter> subscriberNetInf;

   @BeforeClass
   public static void setUpTest() throws SQLException {
      Properties properties = Utils.loadProperties(TESTING_PROPERTIES);
      injector = Guice.createInjector(new EventServiceSienaTranslationTestModule(properties));
      eventServiceSiena = injector.getInstance(EventServiceSiena.class);
      datamodelFactory = injector.getInstance(DatamodelFactory.class);

      personIdentityObjectID = datamodelFactory.createIdentifierFromString(PERSON_IDENTITY_OBJECT_ID_STRING);

      subscriberNetInf = eventServiceSiena.createSubscriberNetInf(personIdentityObjectID, null);
   }

   @Test
   @SuppressWarnings("unchecked")
   public void testSubscriptionTranslation() throws NetInfCheckedException {
      final String nameAttributeURI = "http://www.netinf.org/#name";
      final String nameAttributeValue = "Chuck Norris";
      final String ageAttributeURI = "http://www.netinf.org/#age";
      final int ageAttributeValue1 = 40;
      final int ageAttributeValue2 = 50;

      String query = "SELECT ?old ?new WHERE {" + "?old <" + nameAttributeURI + "> \"" + nameAttributeValue + "\"." + "?old <"
            + nameAttributeURI + "> ?name1." + "?new <" + nameAttributeURI + "> ?name2." + "?old <" + ageAttributeURI
            + "> ?age1." + "?new <" + ageAttributeURI + "> ?age2." + "FILTER (?age1 > " + ageAttributeValue1 + ")."
            + "FILTER (?age1 < " + ageAttributeValue2 + ")." + "FILTER (?age1 > ?age2)." + "FILTER (bound(?name2))."
            + "FILTER (!bound(?name1)).}";

      ESFSubscriptionRequest subscriptionRequest = new ESFSubscriptionRequest(null, query, 0);
      List<Filter> sienaFilters = subscriberNetInf.translateSubscriptionRequest(subscriptionRequest);

      // Check constraints
      String constraint1Name = TranslationSiena.PREFIX_OLD + TranslationSiena.SEPARATOR + nameAttributeURI;
      String constraint2Name = TranslationSiena.PREFIX_OLD + TranslationSiena.SEPARATOR + ageAttributeURI;
      String constraint3Name = TranslationSiena.PREFIX_DIFF + TranslationSiena.SEPARATOR + ageAttributeURI;
      String constraint4Name = TranslationSiena.PREFIX_DIFF + TranslationSiena.SEPARATOR + nameAttributeURI;

      Assert.assertEquals(1, sienaFilters.size());
      Filter sienaFilter = sienaFilters.get(0);

      Assert.assertTrue(sienaFilter.containsConstraint(constraint1Name));
      Assert.assertTrue(sienaFilter.containsConstraint(constraint2Name));
      Assert.assertTrue(sienaFilter.containsConstraint(constraint3Name));

      Iterator<AttributeConstraint> it;
      AttributeConstraint constraint;

      // Name
      it = sienaFilter.constraintsIterator(constraint1Name);
      constraint = it.next();
      Assert.assertEquals(Op.EQ, constraint.op);
      Assert.assertEquals(nameAttributeValue, constraint.value.stringValue());

      // Age
      it = sienaFilter.constraintsIterator(constraint2Name);
      constraint = it.next();
      Assert.assertEquals(Op.GT, constraint.op);
      Assert.assertEquals(ageAttributeValue1, constraint.value.intValue());

      constraint = it.next();
      Assert.assertEquals(Op.LT, constraint.op);
      Assert.assertEquals(ageAttributeValue2, constraint.value.intValue());

      // Age Diff
      it = sienaFilter.constraintsIterator(constraint3Name);
      constraint = it.next();
      Assert.assertEquals(Op.EQ, constraint.op);
      Assert.assertEquals(TranslationSiena.STATUS_CHANGED_GT, constraint.value.stringValue());

      // Name Diff
      it = sienaFilter.constraintsIterator(constraint4Name);
      constraint = it.next();
      Assert.assertEquals(Op.EQ, constraint.op);
      Assert.assertEquals(TranslationSiena.STATUS_CREATED, constraint.value.stringValue());
   }

   @Test(expected = SubscriptionInfiniteVariableRecursionException.class)
   public void testInfiniteVariableRecursion1() throws NetInfCheckedException {
      String query = "SELECT ?old ?new WHERE {?old <http://netinf.org/#age> ?age."
            + "?age <http://netinf.org/#age> ?age. FILTER(?age = 20)}";

      subscriberNetInf.translateSubscriptionRequest(new ESFSubscriptionRequest(null, query, 0));
   }

   @Test(expected = SubscriptionInfiniteVariableRecursionException.class)
   public void testInfiniteVariableRecursion2() throws NetInfCheckedException {
      String query = "SELECT ?old ?new WHERE {?old <http://netinf.org/#age> ?age1."
            + "?age1 <http://netinf.org/#age> ?age2. ?age2 <http://netinf.org/#age> ?age1. FILTER(?age1 = 20)}";

      subscriberNetInf.translateSubscriptionRequest(new ESFSubscriptionRequest(null, query, 0));
   }

   @Test(expected = SubscriptionInvalidFilterException.class)
   public void testInvalidFilter() throws NetInfCheckedException {
      String query = "SELECT ?old ?new WHERE {FILTER(20 = 20)}";

      subscriberNetInf.translateSubscriptionRequest(new ESFSubscriptionRequest(null, query, 0));
   }

   @Test(expected = SubscriptionInvalidFilterException.class)
   public void testInvalidBoundFilterBothOld() throws NetInfCheckedException {
      String query = "SELECT ?old ?new WHERE {?old <http://netinf.org/#age> ?age1. "
            + "?old <http://netinf.org/#age> ?age2. FILTER(?age1 = ?age2)}";

      subscriberNetInf.translateSubscriptionRequest(new ESFSubscriptionRequest(null, query, 0));
   }

   @Test(expected = SubscriptionInvalidFilterException.class)
   public void testInvalidBoundFilterBothNew() throws NetInfCheckedException {
      String query = "SELECT ?old ?new WHERE {?new <http://netinf.org/#age> ?age1. "
            + "?new <http://netinf.org/#age> ?age2. FILTER(?age1 = ?age2)}";

      subscriberNetInf.translateSubscriptionRequest(new ESFSubscriptionRequest(null, query, 0));
   }

   @Test(expected = SubscriptionInvalidFilterException.class)
   public void testInvalidBoundFilterDifferentAttribute() throws NetInfCheckedException {
      String query = "SELECT ?old ?new WHERE {?old <http://netinf.org/#age> ?age. "
            + "?new <http://netinf.org/#name> ?name. FILTER(?age = ?name)}";

      subscriberNetInf.translateSubscriptionRequest(new ESFSubscriptionRequest(null, query, 0));
   }

   @Test(expected = SubscriptionInvalidFilterException.class)
   public void testInvalidBoundFilterBothVariablesBound() throws NetInfCheckedException {
      String query = "SELECT ?old ?new WHERE {?old <http://netinf.org/#age> ?age1. "
            + "?new <http://netinf.org/#age> ?age2. FILTER(bound(?age1)). FILTER(bound(?age2))}";

      subscriberNetInf.translateSubscriptionRequest(new ESFSubscriptionRequest(null, query, 0));
   }

   @Test(expected = SubscriptionInvalidFilterException.class)
   public void testInvalidBoundFilterBothVariablesUnbound() throws NetInfCheckedException {
      String query = "SELECT ?old ?new WHERE {?old <http://netinf.org/#age> ?age1. "
            + "?new <http://netinf.org/#age> ?age2. FILTER(!bound(?age1)). FILTER(!bound(?age2))}";

      subscriberNetInf.translateSubscriptionRequest(new ESFSubscriptionRequest(null, query, 0));
   }

   @Test(expected = SubscriptionInvalidFilterOperatorException.class)
   public void testInvalidFilterOperator() throws NetInfCheckedException {
      String query = "SELECT ?old ?new WHERE {?old <http://netinf.org/#age> ?age1. "
            + "?new <http://netinf.org/#age> ?age2. FILTER(?age1 <= ?age2)}";

      subscriberNetInf.translateSubscriptionRequest(new ESFSubscriptionRequest(null, query, 0));
   }

   @Test(expected = SubscriptionInvalidResultVariablesException.class)
   public void testInvalidResultVariables1() throws NetInfCheckedException {
      String query = "SELECT ?old ?foo WHERE {}";

      subscriberNetInf.translateSubscriptionRequest(new ESFSubscriptionRequest(null, query, 0));
   }

   @Test(expected = SubscriptionInvalidResultVariablesException.class)
   public void testInvalidResultVariables2() throws NetInfCheckedException {
      String query = "SELECT ?old ?new ?foo WHERE {}";

      subscriberNetInf.translateSubscriptionRequest(new ESFSubscriptionRequest(null, query, 0));
   }

   @Test(expected = SubscriptionInvalidTripleObjectException.class)
   public void testInvalidInvalidTripleObject() throws NetInfCheckedException {
      String query = "SELECT ?old ?new WHERE {?old <http://netinf.org/#name> <http://netinf.org/#age>}";

      subscriberNetInf.translateSubscriptionRequest(new ESFSubscriptionRequest(null, query, 0));
   }

   @Test(expected = SubscriptionInvalidTriplePredicateException.class)
   public void testInvalidInvalidTriplePredicate() throws NetInfCheckedException {
      String query = "SELECT ?old ?new WHERE {?old ?age 40}";

      subscriberNetInf.translateSubscriptionRequest(new ESFSubscriptionRequest(null, query, 0));
   }

   @Test(expected = SubscriptionInvalidTripleSubjectException.class)
   public void testInvalidInvalidTripleSubject() throws NetInfCheckedException {
      String query = "SELECT ?old ?new WHERE {10 <http://netinf.org/#age> 40}";

      subscriberNetInf.translateSubscriptionRequest(new ESFSubscriptionRequest(null, query, 0));
   }

   @Test(expected = SubscriptionInvalidTripleSubjectException.class)
   public void testInvalidInvalidTripleSubjectVariable() throws NetInfCheckedException {
      String query = "SELECT ?old ?new WHERE {?foo <http://netinf.org/#age> 40}";

      subscriberNetInf.translateSubscriptionRequest(new ESFSubscriptionRequest(null, query, 0));
   }

   @Test(expected = SubscriptionInvalidSolutionModifierException.class)
   public void testInvalidInvalidSolutionModifier() throws NetInfCheckedException {
      String query = "SELECT ?old ?new WHERE {} ORDER BY ?foo";

      subscriberNetInf.translateSubscriptionRequest(new ESFSubscriptionRequest(null, query, 0));
   }

   @Test(expected = SubscriptionInvalidWhereClauseException.class)
   public void testInvalidWhereClause() throws NetInfCheckedException {
      String query = "SELECT ?old ?new WHERE { OPTIONAL {} }";

      subscriberNetInf.translateSubscriptionRequest(new ESFSubscriptionRequest(null, query, 0));
   }

   @Test
   public void testAttributeMatching() throws NetInfCheckedException {
      final String attributeURI = "http://www.netinf.org/#name";
      final String attributeValue = "Chuck Norris";

      boolean setup = eventServiceSiena.setup();
      Assert.assertTrue(setup);

      SubscriberNetInf<HierarchicalDispatcher, Notifiable, Notification, Filter> subscriberNetInf = eventServiceSiena
            .createSubscriberNetInf(personIdentityObjectID, null);
      PublisherNetInf<HierarchicalDispatcher, Notifiable, Notification, Filter> publisherNetInf = eventServiceSiena
            .createPublisherNetInf();
      Assert.assertNotNull(subscriberNetInf);
      Assert.assertNotNull(publisherNetInf);

      // Create EventMessage
      IdentifierLabel identifierLabel = datamodelFactory.createIdentifierLabel();
      identifierLabel.setLabelName("foo");
      identifierLabel.setLabelValue("bar");

      Identifier identifier = datamodelFactory.createIdentifier();
      identifier.addIdentifierLabel(identifierLabel);

      InformationObject informationObject = datamodelFactory.createInformationObject();
      informationObject.setIdentifier(identifier);
      informationObject.addAttribute(datamodelFactory.createAttribute(attributeURI, attributeValue));

      ESFEventMessage eventMessage = new ESFEventMessage();
      eventMessage.setNewInformationObject(informationObject);
      eventMessage.setSerializeFormat(SerializeFormat.JAVA);

      // Create Subscription
      String query = "SELECT ?old ?new WHERE {?new <" + attributeURI + "> \"" + attributeValue + "\". }";
      ESFSubscriptionRequest subscriptionRequest = new ESFSubscriptionRequest(SUBSCRIPTION_ID, query, 60);
      subscriberNetInf.processSubscriptionRequest(subscriptionRequest);
      publisherNetInf.processEventMessage(eventMessage);

      // Test
      InformationObject retrievedInformationObject = fetchNewInformationObjectFromEventContainer(subscriberNetInf);
      Attribute retrievedAttribute = retrievedInformationObject.getSingleAttribute(attributeURI);
      Assert.assertEquals(attributeValue, retrievedAttribute.getValue(String.class));
   }

   @Test
   public void testSubattributeMatching() throws NetInfCheckedException {
      final String attributeURI = "http://www.netinf.org/#author";
      final String subattributeURI = "http://www.netinf.org/#name";
      final String subattributeValue = "Donald Knuth";

      boolean setup = eventServiceSiena.setup();
      Assert.assertTrue(setup);

      SubscriberNetInf<HierarchicalDispatcher, Notifiable, Notification, Filter> subscriberNetInf = eventServiceSiena
            .createSubscriberNetInf(personIdentityObjectID, null);
      PublisherNetInf<HierarchicalDispatcher, Notifiable, Notification, Filter> publisherNetInf = eventServiceSiena
            .createPublisherNetInf();
      Assert.assertNotNull(subscriberNetInf);
      Assert.assertNotNull(publisherNetInf);

      // Create EventMessage
      IdentifierLabel identifierLabel = datamodelFactory.createIdentifierLabel();
      identifierLabel.setLabelName("foo");
      identifierLabel.setLabelValue("bar");

      Identifier identifier = datamodelFactory.createIdentifier();
      identifier.addIdentifierLabel(identifierLabel);

      Attribute attribute = datamodelFactory.createAttribute();
      attribute.setIdentification(attributeURI);
      attribute.addSubattribute(datamodelFactory.createAttribute(subattributeURI, subattributeValue));

      InformationObject informationObject = datamodelFactory.createInformationObject();
      informationObject.setIdentifier(identifier);
      informationObject.addAttribute(attribute);

      ESFEventMessage eventMessage = new ESFEventMessage();
      eventMessage.setNewInformationObject(informationObject);
      eventMessage.setSerializeFormat(SerializeFormat.JAVA);

      // Create Subscription
      String query = "SELECT ?old ?new WHERE {?new <" + attributeURI + "> ?var. " + "?var <" + subattributeURI + "> \""
            + subattributeValue + "\". }";
      ESFSubscriptionRequest subscriptionRequest = new ESFSubscriptionRequest(SUBSCRIPTION_ID, query, 60);
      subscriberNetInf.processSubscriptionRequest(subscriptionRequest);
      publisherNetInf.processEventMessage(eventMessage);

      // Test
      InformationObject retrievedInformationObject = fetchNewInformationObjectFromEventContainer(subscriberNetInf);
      Attribute retrievedAttribute = retrievedInformationObject.getSingleAttribute(attributeURI);

      List<Attribute> retrievedSubproperties = retrievedAttribute.getSubattributes();
      Assert.assertNotNull(retrievedSubproperties);
      Assert.assertEquals(1, retrievedSubproperties.size());

      Attribute retrievedSubattribute = retrievedSubproperties.get(0);
      Assert.assertEquals(subattributeValue, retrievedSubattribute.getValue(String.class));
   }

   private InformationObject fetchNewInformationObjectFromEventContainer(
         SubscriberNetInf<HierarchicalDispatcher, Notifiable, Notification, Filter> subscriberNetInf) {
      ESFFetchMissedEventsResponse fetchMissedEventsResponse = subscriberNetInf.processFetchMissedEventsRequest();
      List<ESFEventMessage> eventMessages = fetchMissedEventsResponse.getEventMessages();
      Assert.assertNotNull(eventMessages);
      Assert.assertEquals(1, eventMessages.size());

      ESFEventMessage eventObject = eventMessages.get(0);
      Assert.assertNotNull(eventObject.getNewInformationObject());

      InformationObject retrievedInformationObject = eventObject.getNewInformationObject();
      Assert.assertNotNull(retrievedInformationObject);
      return retrievedInformationObject;
   }
}
