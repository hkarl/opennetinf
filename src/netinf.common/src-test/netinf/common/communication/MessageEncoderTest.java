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
package netinf.common.communication;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.IdentifierLabel;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.identity.ResolutionServiceIdentityObject;
import netinf.common.datamodel.impl.DatamodelFactoryImpl;
import netinf.common.datamodel.impl.identity.ResolutionServiceIdentityObjectImpl;
import netinf.common.messages.ESFFetchMissedEventsRequest;
import netinf.common.messages.ESFRegistrationRequest;
import netinf.common.messages.ESFRegistrationResponse;
import netinf.common.messages.ESFSubscriptionRequest;
import netinf.common.messages.ESFSubscriptionResponse;
import netinf.common.messages.ESFUnsubscriptionRequest;
import netinf.common.messages.ESFUnsubscriptionResponse;
import netinf.common.messages.NetInfMessage;
import netinf.common.messages.RSGetNameRequest;
import netinf.common.messages.RSGetNameResponse;
import netinf.common.messages.RSGetPriorityRequest;
import netinf.common.messages.RSGetPriorityResponse;
import netinf.common.messages.RSGetRequest;
import netinf.common.messages.RSGetResponse;
import netinf.common.messages.RSGetServicesRequest;
import netinf.common.messages.RSGetServicesResponse;
import netinf.common.messages.RSPutRequest;
import netinf.common.messages.RSPutResponse;
import netinf.common.messages.SCGetByQueryTemplateRequest;
import netinf.common.messages.SCGetBySPARQLRequest;
import netinf.common.messages.SCGetTimeoutAndNewSearchIDRequest;
import netinf.common.messages.SCGetTimeoutAndNewSearchIDResponse;
import netinf.common.messages.SCSearchResponse;
import netinf.common.messages.TCChangeTransferRequest;
import netinf.common.messages.TCChangeTransferResponse;
import netinf.common.messages.TCGetServicesRequest;
import netinf.common.messages.TCGetServicesResponse;
import netinf.common.messages.TCStartTransferRequest;
import netinf.common.messages.TCStartTransferResponse;
import netinf.common.utils.DatamodelUtils;
import netinf.common.utils.Utils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Tests MessageEncoderProtobuf and MessageEncoderXML
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public abstract class MessageEncoderTest {
   private static final String PROPERTIES_PATH = "../configs/testing.properties";
   private final String RS_IDO_DESC = "Test Resolution Services IdO";
   private Injector injector;
   private Properties properties;

   private MessageEncoder messageEncoder;
   private DatamodelFactory datamodelFactory;
   private DatamodelFactoryImpl datamodelFactoryImpl;
   private Identifier testIdentifier;
   private Identifier testIdentifier2;
   private Identifier testIdentity;
   private InformationObject testIO;
   private InformationObject oldTestIO; // Only for the ESFEventMessage test
   private ResolutionServiceIdentityObjectImpl rsIDO;

   @Before
   public void setup() {
      this.properties = Utils.loadProperties(PROPERTIES_PATH);
      this.injector = Guice.createInjector(new CommonTestModule(this.properties));

      this.messageEncoder = this.injector.getInstance(MessageEncoderProtobuf.class);
      this.datamodelFactory = this.injector.getInstance(DatamodelFactory.class);

      IdentifierLabel testIdentifierLabel = this.datamodelFactory.createIdentifierLabel();
      testIdentifierLabel.setLabelName("Uni");
      testIdentifierLabel.setLabelValue("Paderborn");
      this.testIdentifier = this.datamodelFactory.createIdentifier();
      this.testIdentifier.addIdentifierLabel(testIdentifierLabel);

      IdentifierLabel testIdentifierLabel2 = this.datamodelFactory.createIdentifierLabel();
      testIdentifierLabel2.setLabelName("Universitaet");
      testIdentifierLabel2.setLabelValue("Moscow");
      this.testIdentifier2 = this.datamodelFactory.createIdentifier();
      this.testIdentifier2.addIdentifierLabel(testIdentifierLabel2);

      IdentifierLabel testIdentityLabel = this.datamodelFactory.createIdentifierLabel();
      testIdentityLabel.setLabelName("Chuck");
      testIdentityLabel.setLabelValue("Norris");
      this.testIdentity = this.datamodelFactory.createIdentifier();
      this.testIdentity.addIdentifierLabel(testIdentityLabel);

      this.testIO = this.datamodelFactory.createInformationObject();
      this.testIO.setIdentifier(this.testIdentifier);

      this.oldTestIO = this.datamodelFactory.createInformationObject();
      this.oldTestIO.setIdentifier(testIdentifier2);

      this.datamodelFactoryImpl = this.injector.getInstance(DatamodelFactoryImpl.class);
      this.rsIDO = new ResolutionServiceIdentityObjectImpl(this.datamodelFactoryImpl);
      this.rsIDO.setDescription(this.RS_IDO_DESC);
   }

   protected Injector getInjector() {
      return this.injector;
   }

   protected void setMessageEncoder(MessageEncoder messageEncoder) {
      this.messageEncoder = messageEncoder;
   }

   @Test
   public void testNetInfMessage() {
      NetInfMessage m = new RSGetResponse();
      m.setSerializeFormat(SerializeFormat.RDF);
      m.setErrorMessage("Fehler in Layer 8");
      m.setUserName("Chuck Norris");
      m.setPrivateKey("123");
      // Added in NetInf 3
      Assert.assertThat(m, is(notNullValue()));
   }

   @Test
   public void testRSGetRequest() {
      RSGetRequest m = new RSGetRequest(this.testIdentifier);
      m.setDownloadBinaryObject(true);
      m.setFetchAllVersions(true);
      testEncoder(m);
   }

   @Test
   public void testRSGetRequestConstructor() {
      List<ResolutionServiceIdentityObject> resServiceToUse = null;
      RSGetRequest m = new RSGetRequest(this.testIdentifier, resServiceToUse);
      assertEquals(m.getIdentifier(), this.testIdentifier);
      assertEquals(m.getResolutionServicesToUse(), resServiceToUse);
   }

   @Test
   public void testRSGetRequestSetIdentifier() {
      RSGetRequest m = new RSGetRequest(this.testIdentifier);
      m.setIdentifier(testIdentifier2);
      assertEquals(m.getIdentifier(), testIdentifier2);
   }

   @Test
   public void testRSGetRequestEqualsAndHash() {
      RSGetRequest r1 = new RSGetRequest(this.testIdentifier);
      r1.setPrivateKey("123");
      r1.setUserName("Chuck Norris");

      RSGetRequest r2 = new RSGetRequest(this.testIdentifier2);
      r2.setPrivateKey("234");
      r2.setUserName("Chuck Norris");
      RSGetRequest r3 = new RSGetRequest(this.testIdentifier2);
      r3.setPrivateKey("234");
      r3.setUserName("Chuck Norris");
      assertIsNotEqual(r1, r2);
      assertIsEqual(r2, r3);
   }

   @Test
   public void testRSGetRequestToString() {
      RSGetRequest r1 = new RSGetRequest(this.testIdentifier);
      r1.setPrivateKey("123");
      r1.setUserName("Chuck Norris");

      String toCompare = "MessageKind: RSGetRequest\nSerializationFormat: JAVA\nErrorMessage: null\nIdentifier: "
            + this.testIdentifier.toString() + "\nFetchAllVersions: false";
      String origin = r1.toString();
      assertThat(origin, org.hamcrest.CoreMatchers.equalTo(toCompare));
   }

   @Test
   public void testRsGetRequestDescribe() {
      RSGetRequest r1 = new RSGetRequest(this.testIdentifier);
      r1.setPrivateKey("123");
      r1.setUserName("Chuck Norris");
      String toCompare = "a request for any IO that " + this.testIdentifier.describe();
      String origin = r1.describe();
      assertThat(origin, org.hamcrest.CoreMatchers.equalTo(toCompare));
   }

   @Test
   public void testRSGetResponse() {
      RSGetResponse m = new RSGetResponse();
      m.addInformationObject(this.testIO);
      testEncoder(m);
   }

   /**
    * Testing for two equal objects and two different objects of type SCGetByQueryTemplateRequest Testing <code>equals()</code>
    * for different classes just uses the parent class function
    */
   @Test
   public void testRSGetResponseHashAndEquals() {
      RSGetResponse r1 = new RSGetResponse();
      r1.addInformationObject(this.testIO);
      r1.setPrivateKey("123");
      r1.setUserName("Chuck");

      RSGetResponse r2 = new RSGetResponse();
      r2.addInformationObject(this.oldTestIO);
      r2.setPrivateKey("234");
      r2.setUserName("Chuck");

      RSGetResponse r3 = new RSGetResponse();
      r3.addInformationObject(this.oldTestIO);
      r3.setPrivateKey("234");
      r3.setUserName("Chuck");
      assertIsNotEqual(r1, r2);
      assertIsEqual(r2, r3);
   }

   /**
    * RSGetResponse This test will serialize the message in string format and check if it matches the template. WARNING: If
    * template changes, also change this test
    */
   @Test
   public void testRSGetResponseJavaFormat() {
      RSGetResponse r1 = new RSGetResponse();
      r1.addInformationObject(this.testIO);

      String toCompare = "MessageKind: RSGetResponse\nSerializationFormat: JAVA\nErrorMessage: null" + "\nInformationObject: \n"
            + DatamodelUtils.toStringInformationObject(this.testIO, DatamodelUtils.INDENT);
      String reference = r1.toString();
      assertThat(reference, org.hamcrest.CoreMatchers.equalTo(toCompare));
   }

   @Test
   public void testRSGetResponseDescribe() {
      RSGetResponse r1 = new RSGetResponse();
      r1.addInformationObject(this.testIO);
      r1.setPrivateKey("123");
      r1.setUserName("Chuck Norris");
      String toCompare = "a reply to a request that has 1 Information Object";
      String origin = r1.describe();

      assertThat(origin, org.hamcrest.CoreMatchers.equalTo(toCompare));

      r1.addInformationObject(this.oldTestIO);
      toCompare = "a reply to a request that has 2 Information Objects";
      origin = r1.describe();

      assertThat(origin, org.hamcrest.CoreMatchers.equalTo(toCompare));
   }

   @Test
   public void testRSPutRequest() {
      RSPutRequest m = new RSPutRequest(this.testIO);
      testEncoder(m);
   }

   /**
    * Testing for two equal objects and two different objects of type RSPutRequest Testing <code>equals()</code> for different
    * classes just uses the parent class function
    */
   @Test
   public void testRSPutRequestHashAndEquals() {
      RSPutRequest r1 = new RSPutRequest(this.testIO);
      r1.setPrivateKey("123");
      r1.setUserName("Chuck");

      RSPutRequest r2 = new RSPutRequest(this.oldTestIO);
      r2.setPrivateKey("234");
      r2.setUserName("Chuck");

      RSPutRequest r3 = new RSPutRequest(this.oldTestIO);
      r3.setPrivateKey("234");
      r3.setUserName("Chuck");
      assertIsNotEqual(r1, r2);
      assertIsEqual(r2, r3);
   }

   @Test
   public void testRSPutRequestToStringJavaFormat() {
      RSPutRequest r1 = new RSPutRequest(this.testIO);
      ArrayList<ResolutionServiceIdentityObject> al = new ArrayList<ResolutionServiceIdentityObject>();
      al.add(this.rsIDO);
      r1.setResolutionServicesToUse(al);

      String toCompare = "MessageKind: RSPutRequest\nSerializationFormat: JAVA\nErrorMessage: null" + "\nInformationObject: \n"
            + DatamodelUtils.toStringInformationObject(this.testIO, DatamodelUtils.INDENT)
            + "\nResolutionServiceIdentityObject: \n"
            + DatamodelUtils.toStringInformationObject(this.rsIDO, DatamodelUtils.INDENT);
      String reference = r1.toString();
      assertThat(reference, org.hamcrest.CoreMatchers.equalTo(toCompare));
   }

   @Test
   public void testRsPutRequestDescribe() {
      RSPutRequest r1 = new RSPutRequest(this.testIO);
      r1.setPrivateKey("123");
      r1.setUserName("Chuck Norris");
      ArrayList<ResolutionServiceIdentityObject> al = new ArrayList<ResolutionServiceIdentityObject>();
      al.add(this.rsIDO);
      String toCompare = "a request for storing " + this.testIO.describe();
      String origin = r1.describe();
      assertThat(origin, org.hamcrest.CoreMatchers.equalTo(toCompare));
   }

   @Test
   public void testRSPutResponse() {
      RSPutResponse m = new RSPutResponse();
      testEncoder(m);
   }

   @Test
   public void testRSPutResponseDescribe() {
      RSPutResponse r1 = new RSPutResponse();

      String toCompare = "a reply for a storing request";
      String reference = r1.describe();
      assertThat(reference, org.hamcrest.CoreMatchers.equalTo(toCompare));
      toCompare = toCompare + "; Error: failed";
      r1.setErrorMessage("failed");
      reference = r1.describe();
      assertThat(reference, org.hamcrest.CoreMatchers.equalTo(toCompare));
   }

   @Test
   public void testRSGetNameRequest() {
      RSGetNameRequest m = new RSGetNameRequest(this.testIdentifier);
      testEncoder(m);
   }

   /**
    * Testing for two equal objects and two different objects of type RSGetNameRequest Testing <code>equals()</code> for different
    * classes just uses the parent class function
    */
   @Test
   public void testRSGetNameRequestHashAndEquals() {
      RSGetNameRequest r1 = new RSGetNameRequest(this.testIdentifier);
      r1.setPrivateKey("123");
      r1.setUserName("Chuck");

      RSGetNameRequest r2 = new RSGetNameRequest(this.testIdentifier2);
      r2.setPrivateKey("234");
      r2.setUserName("Chuck");

      RSGetNameRequest r3 = new RSGetNameRequest(this.testIdentifier2);
      r3.setPrivateKey("234");
      r3.setUserName("Chuck");
      assertIsNotEqual(r1, r2);
      assertIsEqual(r2, r3);
   }

   @Test
   public void testRSGetNameRequestToStringJavaFormat() {
      RSGetNameRequest r1 = new RSGetNameRequest(this.testIdentifier);
      String toCompare = "MessageKind: RSGetNameRequest\nSerializationFormat: JAVA\nErrorMessage: null"
            + "\nResolutionServiceIdentifier: " + this.testIdentifier.toString();
      String reference = r1.toString();
      assertThat(reference, org.hamcrest.CoreMatchers.equalTo(toCompare));
   }

   @Test
   public void testRSGetNameResponse() {
      RSGetNameResponse m = new RSGetNameResponse("NetInf");
      testEncoder(m);
   }

   @Test
   public void testRSGetPriorityRequest() {
      RSGetPriorityRequest m = new RSGetPriorityRequest(this.testIdentifier);
      testEncoder(m);
   }

   /**
    * Testing for two equal objects and two different objects of type RSGetPriorityRequest Testing <code>equals()</code> for
    * different classes just uses the parent class function
    */
   @Test
   public void testRSGetPriorityRequestHashAndEquals() {
      RSGetPriorityRequest r1 = new RSGetPriorityRequest(this.testIdentifier);
      r1.setPrivateKey("123");
      r1.setUserName("Chuck");

      RSGetPriorityRequest r2 = new RSGetPriorityRequest(this.testIdentifier2);
      r2.setPrivateKey("234");
      r2.setUserName("Chuck");

      RSGetPriorityRequest r3 = new RSGetPriorityRequest(this.testIdentifier2);
      r3.setPrivateKey("234");
      r3.setUserName("Chuck");
      assertIsNotEqual(r1, r2);
      assertIsEqual(r2, r3);
   }

   @Test
   public void testRSGetPriorityRequestToStringJavaFormat() {
      RSGetPriorityRequest r1 = new RSGetPriorityRequest(this.testIdentifier);
      String toCompare = "MessageKind: RSGetPriorityRequest\nSerializationFormat: JAVA\nErrorMessage: null"
            + "\nResolutionServiceIdentifier: " + this.testIdentifier.toString();
      String reference = r1.toString();
      assertThat(reference, org.hamcrest.CoreMatchers.equalTo(toCompare));
   }

   @Test
   public void testRSGetPriorityResponse() {
      RSGetPriorityResponse m = new RSGetPriorityResponse(42);
      testEncoder(m);
   }

   @Test
   public void testRSGetServicesRequest() {
      RSGetServicesRequest m = new RSGetServicesRequest();
      testEncoder(m);
   }

   @Test
   public void testRSGetServicesResponse() {
      RSGetServicesResponse rsGetServicesResponse = new RSGetServicesResponse();
      rsGetServicesResponse.addResolutionService(this.testIdentifier);
      testEncoder(rsGetServicesResponse);
   }

   /**
    * Testing for two equal objects and two different objects of type SCGetByQueryTemplateRequest Testing <code>equals()</code>
    * for different classes just uses the parent class function
    */
   @Test
   public void testRSGetServicesResponseHashAndEquals() {
      RSGetServicesResponse r1 = new RSGetServicesResponse();
      r1.addResolutionService(this.testIdentifier);
      r1.setPrivateKey("123");
      r1.setUserName("Chuck");

      RSGetServicesResponse r2 = new RSGetServicesResponse();
      r2.addResolutionService(this.testIdentifier2);
      r2.setPrivateKey("234");
      r2.setUserName("Chuck");

      RSGetServicesResponse r3 = new RSGetServicesResponse();
      r3.addResolutionService(this.testIdentifier2);
      r3.setPrivateKey("234");
      r3.setUserName("Chuck");
      assertIsNotEqual(r1, r2);
      assertIsEqual(r2, r3);
   }

   /**
    * RSGetServicesResponse This test will serialize the message in string format and check if it matches the template. WARNING:
    * If template changes, also change this test
    */
   @Test
   public void testRSGetServicesResponseJavaFormat() {
      RSGetServicesResponse r1 = new RSGetServicesResponse();
      r1.addResolutionService(this.testIdentifier);

      String toCompare = "MessageKind: RSGetServicesResponse\nSerializationFormat: JAVA\nErrorMessage: null" + "\nIdentifier: "
            + this.testIdentifier.toString();
      String reference = r1.toString();
      assertThat(reference, org.hamcrest.CoreMatchers.equalTo(toCompare));
   }

   @Test
   public void testESFRegistrationRequest() {
      ESFRegistrationRequest m = new ESFRegistrationRequest(this.testIdentity);
      m.setEventContainerIdentifier(this.testIdentifier);
      testEncoder(m);
   }

   @Test
   public void testESFRegistrationResponse() {
      ESFRegistrationResponse m = new ESFRegistrationResponse(this.testIdentifier);
      testEncoder(m);
   }

   @Test
   public void testESFSubscriptionRequest() {
      ESFSubscriptionRequest m = new ESFSubscriptionRequest("MyID", "My SparQL Query", 1000);
      testEncoder(m);
   }

   @Test
   public void testESFSubscriptionResponse() {
      ESFSubscriptionResponse m = new ESFSubscriptionResponse();
      testEncoder(m);
   }

   @Test
   public void testESFUnsubscriptionRequest() {
      ESFUnsubscriptionRequest m = new ESFUnsubscriptionRequest("MyID");
      testEncoder(m);
   }

   @Test
   public void testESFUnsubscriptionResponse() {
      ESFUnsubscriptionResponse m = new ESFUnsubscriptionResponse();
      testEncoder(m);
   }

   @Test
   public void testESFFetchMissedEventsRequest() {
      ESFFetchMissedEventsRequest m = new ESFFetchMissedEventsRequest();
      m.setPrivateKey("123");
      m.setUserName("Chuck Norris");
      m.setSerializeFormat(SerializeFormat.RDF);
      m.setErrorMessage("Success");
      testEncoder(m);
   }

   @Test
   public void testMessageDescription() {
      ESFFetchMissedEventsRequest m = new ESFFetchMissedEventsRequest();
      m.setPrivateKey("123");
      m.setUserName("Chuck Norris");
      m.setSerializeFormat(SerializeFormat.RDF);

      assertEquals(m.describe(), "a message called " + ESFFetchMissedEventsRequest.class.getSimpleName());
   }

   @Test
   public void testMessageDescriptionWithError() {
      ESFFetchMissedEventsRequest m = new ESFFetchMissedEventsRequest();
      m.setPrivateKey("123");
      m.setUserName("Chuck Norris");
      m.setSerializeFormat(SerializeFormat.RDF);
      m.setErrorMessage("TERR");
      assertEquals(m.describe(), "a message called " + ESFFetchMissedEventsRequest.class.getSimpleName() + "; Error: " + "TERR");
   }

   @Test
   public void testMessageHashCode() {
      ESFFetchMissedEventsRequest m = new ESFFetchMissedEventsRequest();
      m.setPrivateKey("123");
      m.setUserName("Chuck Norris");
      m.setSerializeFormat(SerializeFormat.RDF);
      m.setErrorMessage("TERR");
      int run1 = m.hashCode();
      int run2 = m.hashCode();
      ESFFetchMissedEventsRequest m2 = new ESFFetchMissedEventsRequest();
      m2.setPrivateKey("123");
      m2.setUserName("Chuck Norris");
      m2.setSerializeFormat(SerializeFormat.RDF);
      m2.setErrorMessage("TERR");

      assertEquals(run1, run2);
      if (m.equals(m2)) {
         assertEquals(m.hashCode(), m2.hashCode());
      }
   }

   @Test
   public void testTCGetServicesRequest() {
      TCGetServicesRequest m = new TCGetServicesRequest();
      testEncoder(m);
   }

   @Test
   public void testTCGetServicesResponse() {
      TCGetServicesResponse m = new TCGetServicesResponse();
      m.addTransferService(this.testIdentifier.toString());
      testEncoder(m);
   }

   /**
    * Testing for two equal objects and two different objects of type TCChangeTransferRequest Testing <code>equals()</code> for
    * different classes just uses the parent class function
    */
   @Test
   public void testTCGetServicesResponseHashAndEquals() {
      TCGetServicesResponse r1 = new TCGetServicesResponse();
      r1.addTransferService(this.testIdentifier.toString());
      r1.setPrivateKey("123");
      r1.setUserName("Chuck");

      TCGetServicesResponse r2 = new TCGetServicesResponse();
      r2.addTransferService(this.testIdentifier2.toString());
      r2.setPrivateKey("234");
      r2.setUserName("Chuck");
      TCGetServicesResponse r3 = new TCGetServicesResponse();

      r3.addTransferService(this.testIdentifier2.toString());
      r3.setPrivateKey("234");
      r3.setUserName("Chuck");
      assertIsNotEqual(r1, r2);
      assertIsEqual(r2, r3);
   }

   /**
    * This test will serialize the message in string format and check if it matches the template. WARNING: If template changes,
    * also change this test
    */
   @Test
   public void testTCGetServicesResponseToStringJavaFormat() {
      TCGetServicesResponse r1 = new TCGetServicesResponse();
      r1.addTransferService(this.testIdentifier.toString());

      String toCompare = "MessageKind: TCGetServicesResponse\nSerializationFormat: JAVA\nErrorMessage: null"
            + "\nTransferServices: " + this.testIdentifier.toString();
      String reference = r1.toString();
      assertThat(reference, org.hamcrest.CoreMatchers.equalTo(toCompare));
   }

   @Test
   public void testTCStartTransferRequest() {
      TCStartTransferRequest m = new TCStartTransferRequest();
      m.setSource("TestSource");
      m.setTransferServiceToUse("testings");
      testEncoder(m);
   }

   /**
    * testTCStartTransferRequestToStringJavaFormat() This test will serialize the message in string format and check if it matches
    * the template. WARNING: If template changes, also change this test
    */
   @Test
   public void testTCStartTransferRequestToStringJavaFormat() {
      TCStartTransferRequest r1 = new TCStartTransferRequest();
      r1.setSource("TestSource");
      r1.setTransferServiceToUse("testing");

      String toCompare = "MessageKind: TCStartTransferRequest\nSerializationFormat: JAVA\nErrorMessage: null"
            + "\nSource: TestSource" + "\nDestination: null" + "\nTransferServiceToUse: testing";
      String reference = r1.toString();
      assertThat(reference, org.hamcrest.CoreMatchers.equalTo(toCompare));
   }

   /**
    * Testing for two equal objects and two different objects of type TCStartTransferRequest Testing <code>equals()</code> for
    * different classes just uses the parent class function
    */
   @Test
   public void testTCStartTransferRequestHashAndEquals() {
      TCStartTransferRequest r1 = new TCStartTransferRequest();
      r1.setSource("TestSource");
      r1.setPrivateKey("123");
      r1.setUserName("Chuck");
      r1.setTransferServiceToUse("testing");

      TCStartTransferRequest r2 = new TCStartTransferRequest();
      r2.setSource("TestSource2");
      r2.setTransferServiceToUse("testing");
      r2.setPrivateKey("234");
      r2.setUserName("Chuck");
      TCStartTransferRequest r3 = new TCStartTransferRequest();
      r3.setTransferServiceToUse("testing");
      r3.setSource("TestSource2");
      r3.setPrivateKey("234");
      r3.setUserName("Chuck");
      assertIsNotEqual(r1, r2);
      assertIsEqual(r2, r3);
   }

   @Test
   public void testTCStartTransferResponse() {
      TCStartTransferResponse m = new TCStartTransferResponse();
      m.setSource("source");
      m.setDestination("destination");
      m.setJobId("jobID");
      testEncoder(m);
   }

   /**
    * Testing for two equal objects and two different objects of type TCStartTransferResponse Testing <code>equals()</code> for
    * different classes just uses the parent class function
    */
   @Test
   public void testTCStartTransferResponseHashAndEquals() {
      TCStartTransferResponse r1 = new TCStartTransferResponse();
      r1.setSource("TestSource");
      r1.setPrivateKey("123");
      r1.setUserName("Chuck");
      r1.setDestination("destination");
      r1.setJobId("jobID");

      TCStartTransferResponse r2 = new TCStartTransferResponse();
      r2.setSource("TestSource2");
      r2.setDestination("destination");
      r2.setJobId("jobID2");
      r2.setPrivateKey("234");
      r2.setUserName("Chuck");
      TCStartTransferResponse r3 = new TCStartTransferResponse();
      r3.setDestination("destination");
      r3.setSource("TestSource2");
      r3.setJobId("jobID2");
      r3.setPrivateKey("234");
      r3.setUserName("Chuck");
      assertIsNotEqual(r1, r2);
      assertIsEqual(r2, r3);
   }

   /**
    * This test will serialize the message in string format and check if it matches the template. WARNING: If template changes,
    * also change this test
    */
   @Test
   public void testTCStartTransferResponseToStringJavaFormat() {
      TCStartTransferResponse r1 = new TCStartTransferResponse();
      r1.setSource("source");
      r1.setDestination("destination");
      r1.setJobId("jobID");

      String toCompare = "MessageKind: TCStartTransferResponse\nSerializationFormat: JAVA\nErrorMessage: null"
            + "\nSource: source" + "\nDestination: destination" + "\nJobId: jobID";
      String reference = r1.toString();
      assertThat(reference, org.hamcrest.CoreMatchers.equalTo(toCompare));
   }

   @Test
   public void testTCChangeTransferRequest() {
      TCChangeTransferRequest m = new TCChangeTransferRequest();
      m.setProceed(true);
      m.setJobId("jobID");
      testEncoder(m);
   }

   /**
    * Testing for two equal objects and two different objects of type TCChangeTransferRequest Testing <code>equals()</code> for
    * different classes just uses the parent class function
    */
   @Test
   public void testTCChangeTransferRequestHashAndEquals() {
      TCChangeTransferRequest r1 = new TCChangeTransferRequest();
      r1.setProceed(true);
      r1.setPrivateKey("123");
      r1.setUserName("Chuck");
      r1.setJobId("jobID1");
      TCChangeTransferRequest r2 = new TCChangeTransferRequest();
      r2.setProceed(false);
      r2.setJobId("jobID2");
      r2.setPrivateKey("234");
      r2.setUserName("Chuck");
      TCChangeTransferRequest r3 = new TCChangeTransferRequest();
      // r3.setProceed(true);
      r3.setJobId("jobID2");
      r3.setPrivateKey("234");
      r3.setUserName("Chuck");
      assertIsNotEqual(r1, r2);
      assertIsEqual(r2, r3);
   }

   /**
    * This tests the PARENT class NetInfMessage and NOT the child equals method
    */
   @Test
   public void testEqualsBetweenDifferentObjects() {
      TCChangeTransferRequest r3 = new TCChangeTransferRequest();
      r3.setProceed(true);
      r3.setJobId("jobID2");
      r3.setPrivateKey("234");
      r3.setUserName("Chuck");
      RSGetRequest r4 = new RSGetRequest(testIdentifier);
      r4.setPrivateKey("234");
      r4.setUserName("Chuck");
      assertIsNotEqual(r3, r4);
   }

   /**
    * This test will serialize the message in string format and check if it matches the template. WARNING: If template changes,
    * also change this test
    */
   @Test
   public void testTCChangeTransferResponseToStringJavaFormat() {
      TCChangeTransferResponse r1 = new TCChangeTransferResponse();
      r1.setSource("source");
      r1.setNewDestination("destination");
      r1.setJobId("jobID");

      String toCompare = "MessageKind: TCChangeTransferResponse\nSerializationFormat: JAVA\nErrorMessage: null"
            + "\nSource: source" + "\nNew Destination: destination" + "\nJobId: jobID";
      String reference = r1.toString();
      assertThat(reference, org.hamcrest.CoreMatchers.equalTo(toCompare));
   }

   /**
    * This test will serialize the message in string format and check if it matches the template. WARNING: If template changes,
    * also change this test
    */
   @Test
   public void testTCChangeTransferRequestToStringJavaFormat() {
      TCChangeTransferRequest r1 = new TCChangeTransferRequest();
      r1.setProceed(true);
      r1.setJobId("jobID1");

      String toCompare = "MessageKind: TCChangeTransferRequest\nSerializationFormat: JAVA\nErrorMessage: null\nJobId: jobID1"
            + "\nNew Destination: null" + "\nProceed: true" + "\nTransferServiceToUse: null";
      String reference = r1.toString();
      assertThat(reference, org.hamcrest.CoreMatchers.equalTo(toCompare));
   }

   @Test
   public void testTCChangeTransferResponse() {
      TCChangeTransferResponse m = new TCChangeTransferResponse();
      m.setSource("source");
      m.setNewDestination("destination");
      m.setJobId("jobID");
      testEncoder(m);
   }

   /**
    * Testing for two equal objects and two different objects of type TCChangeTransferResponse Testing <code>equals()</code> for
    * different classes just uses the parent class function
    */
   @Test
   public void testTCChangeTransferResponseHashAndEquals() {
      TCChangeTransferResponse r1 = new TCChangeTransferResponse();
      r1.setSource("source");
      r1.setPrivateKey("123");
      r1.setUserName("Chuck");
      r1.setJobId("jobID1");
      TCChangeTransferResponse r2 = new TCChangeTransferResponse();
      r2.setSource("another source");
      r2.setJobId("jobID2");
      r2.setPrivateKey("234");
      r2.setUserName("Chuck");
      TCChangeTransferResponse r3 = new TCChangeTransferResponse();
      r3.setSource("another source");
      r3.setJobId("jobID2");
      r3.setPrivateKey("234");
      r3.setUserName("Chuck");
      assertIsNotEqual(r1, r2);
      assertIsEqual(r2, r3);
   }

   @Test
   public void testSCGetByQueryTemplateRequest() {
      SCGetByQueryTemplateRequest m = new SCGetByQueryTemplateRequest("test", 42);
      m.addParameter("param1");
      m.addParameter("param2");
      testEncoder(m);
   }

   @Test
   public void testSCGetByQueryTemplateRequestJavaFormat() {
      SCGetByQueryTemplateRequest r1 = new SCGetByQueryTemplateRequest("Netinf", 42);
      r1.addParameter("param1");

      String toCompare = "MessageKind: SCGetByQueryTemplateRequest\nSerializationFormat: JAVA\nErrorMessage: null"
            + "\nType: Netinf" + "\nParameter: param1" + "\nSearchID: 42";
      String reference = r1.toString();
      assertThat(reference, org.hamcrest.CoreMatchers.equalTo(toCompare));
   }

   /**
    * Testing for two equal objects and two different objects of type SCGetByQueryTemplateRequest Testing <code>equals()</code>
    * for different classes just uses the parent class function
    */
   @Test
   public void testSCGetByQueryTemplateRequestHashAndEquals() {
      SCGetByQueryTemplateRequest r1 = new SCGetByQueryTemplateRequest("Netinf", 42);
      r1.setPrivateKey("123");
      r1.setUserName("Chuck");

      SCGetByQueryTemplateRequest r2 = new SCGetByQueryTemplateRequest("Netinf", 43);
      r2.setPrivateKey("234");
      r2.setUserName("Chuck");

      SCGetByQueryTemplateRequest r3 = new SCGetByQueryTemplateRequest("Netinf", 43);
      r3.setPrivateKey("234");
      r3.setUserName("Chuck");
      assertIsNotEqual(r1, r2);
      assertIsEqual(r2, r3);
   }

   @Test
   public void testSCGetBySPARQLRequest() {
      SCGetBySPARQLRequest m = new SCGetBySPARQLRequest("Netinf", 42);
      testEncoder(m);
   }

   @Test
   public void testSCGetBySPARQLRequestJavaFormat() {
      SCGetBySPARQLRequest r1 = new SCGetBySPARQLRequest("Netinf", 42);

      String toCompare = "MessageKind: SCGetBySPARQLRequest\nSerializationFormat: JAVA\nErrorMessage: null" + "\nRequest: Netinf"
            + "\nSearchID: 42";
      String reference = r1.toString();
      assertThat(reference, org.hamcrest.CoreMatchers.equalTo(toCompare));
   }

   /**
    * Testing for two equal objects and two different objects of type SCGetBySPARQLRequest Testing <code>equals()</code> for
    * different classes just uses the parent class function
    */
   @Test
   public void testSCGetBySPARQLRequestHashAndEquals() {
      SCGetBySPARQLRequest r1 = new SCGetBySPARQLRequest("Netinf", 41);
      r1.setSearchID(42);
      r1.setPrivateKey("123");
      r1.setUserName("Chuck");

      SCGetBySPARQLRequest r2 = new SCGetBySPARQLRequest("Netinf", 43);
      r2.setPrivateKey("234");
      r2.setUserName("Chuck");

      SCGetBySPARQLRequest r3 = new SCGetBySPARQLRequest("Netinf", 43);
      r3.setPrivateKey("234");
      r3.setUserName("Chuck");
      assertIsNotEqual(r1, r2);
      assertIsEqual(r2, r3);
   }

   @Test
   public void testSCSearchResponse() {
      SCSearchResponse m = new SCSearchResponse();
      m.addResultIdentifier(this.testIdentifier);
      testEncoder(m);
   }

   /**
    * Testing for two equal objects and two different objects of type TCChangeTransferResponse Testing <code>equals()</code> for
    * different classes just uses the parent class function
    */
   @Test
   public void testSCSearchResponseHashAndEquals() {
      SCSearchResponse r1 = new SCSearchResponse();
      r1.addResultIdentifier(this.testIdentifier);
      r1.setPrivateKey("123");
      r1.setUserName("Chuck");

      SCSearchResponse r2 = new SCSearchResponse();
      r2.addResultIdentifier(this.testIdentifier2);
      r2.setPrivateKey("234");
      r2.setUserName("Chuck");

      SCSearchResponse r3 = new SCSearchResponse();
      r3.addResultIdentifier(this.testIdentifier2);

      r3.setPrivateKey("234");
      r3.setUserName("Chuck");
      assertIsNotEqual(r1, r2);
      assertIsEqual(r2, r3);
   }

   /**
    * This test will serialize the message in string format and check if it matches the template. WARNING: If template changes,
    * also change this test
    */
   @Test
   public void testSCSearchResponseToStringJavaFormat() {
      SCSearchResponse r1 = new SCSearchResponse();
      r1.addResultIdentifier(this.testIdentifier);

      String toCompare = "MessageKind: SCSearchResponse\nSerializationFormat: JAVA\nErrorMessage: null" + "\nIdentifier: "
            + this.testIdentifier.toString();
      String reference = r1.toString();
      assertThat(reference, org.hamcrest.CoreMatchers.equalTo(toCompare));
   }

   @Test
   public void testSCGetTimeoutAndNewSearchIDRequest() {
      SCGetTimeoutAndNewSearchIDRequest m = new SCGetTimeoutAndNewSearchIDRequest(42);
      testEncoder(m);
   }

   /**
    * This test will serialize the message in string format and check if it matches the template. WARNING: If template changes,
    * also change this test
    */
   @Test
   public void testSCGetTimeoutAndNewSearchIDRequestToStringJavaFormat() {
      SCGetTimeoutAndNewSearchIDRequest r1 = new SCGetTimeoutAndNewSearchIDRequest(42);
      String toCompare = "MessageKind: SCGetTimeoutAndNewSearchIDRequest\nSerializationFormat: JAVA\nErrorMessage: null"
            + "\nDesiredTimeout: 42";
      String reference = r1.toString();
      assertThat(reference, org.hamcrest.CoreMatchers.equalTo(toCompare));
   }

   /**
    * This test will serialize the message in string format and check if it matches the template. WARNING: If template changes,
    * also change this test
    */
   @Test
   public void testSCGetTimeoutAndNewSearchIDResponseToStringJavaFormat() {
      SCGetTimeoutAndNewSearchIDResponse r1 = new SCGetTimeoutAndNewSearchIDResponse(42, 110);
      String toCompare = "MessageKind: SCGetTimeoutAndNewSearchIDResponse\nSerializationFormat: JAVA\nErrorMessage: null"
            + "\nUsedTimeout: 42" + "\nSearchID: 110";
      String reference = r1.toString();
      assertThat(reference, org.hamcrest.CoreMatchers.equalTo(toCompare));
   }

   @Test
   public void testSCGetTimeoutAndNewSearchIDResponse() {
      SCGetTimeoutAndNewSearchIDResponse m = new SCGetTimeoutAndNewSearchIDResponse(42, 21);
      testEncoder(m);
   }

   @Test
   public void testGetUniqueEncoderID() {
      /*
       * See if encoder ID is always returned correctly. XML Encoder has id 2 Protobuf Encoder has id 1
       */
      if (messageEncoder.getClass() == MessageEncoderProtobuf.class) {
         assertEquals(messageEncoder.getUniqueEncoderId(), 1);
      } else {
         if (messageEncoder.getClass() == MessageEncoderXML.class) {
            assertEquals(messageEncoder.getUniqueEncoderId(), 2);
         } else {
            // If none of the classes here match, then the MessageEncoder is unknown and the test fails
            fail();
         }
      }

   }

   private void testEncoder(NetInfMessage message) {
      // TODO: Comparing a string created by the encoder with the same string decoded seems to not make sense. Use a set of fixed
      // values
      byte[] encodedMessage = this.messageEncoder.encodeMessage(message);
      NetInfMessage decodedMessage = this.messageEncoder.decodeMessage(encodedMessage);
      assertEquals(message, decodedMessage);
   }

   private void assertAllEqual(Object[] objects) {
      /**
       * The point of checking each pair is to make sure that equals is transitive per the contract of
       * {@link Object#equals(java.lang.Object)}.
       */
      for (int i = 0; i < objects.length; i++) {
         Assert.assertFalse(objects[i].equals(null));
         for (int j = 0; j < objects.length; j++) {
            assertIsEqual(objects[i], objects[j]);
         }
      }
   }

   private void assertIsEqual(Object one, Object two) {
      Assert.assertTrue(one.equals(two));
      Assert.assertTrue(two.equals(one));
      Assert.assertEquals(one.hashCode(), two.hashCode());
   }

   private void assertIsNotEqual(Object one, Object two) {
      assertReflexiveAndNull(one);
      assertReflexiveAndNull(two);
      Assert.assertFalse(one.equals(two));
      Assert.assertFalse(two.equals(one));
   }

   private void assertReflexiveAndNull(Object object) {
      Assert.assertTrue(object.equals(object));
      Assert.assertFalse(object.equals(null));
   }
}
