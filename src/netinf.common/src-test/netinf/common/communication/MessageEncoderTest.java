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

import static org.junit.Assert.assertEquals;

import java.util.Properties;

import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.IdentifierLabel;
import netinf.common.datamodel.InformationObject;
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
import netinf.common.utils.Utils;

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
   private Injector injector;
   private Properties properties;

   private MessageEncoder messageEncoder;
   private DatamodelFactory datamodelFactory;
   private Identifier testIdentifier;
   private Identifier testIdentity;
   private InformationObject testIO;

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

      IdentifierLabel testIdentityLabel = this.datamodelFactory.createIdentifierLabel();
      testIdentityLabel.setLabelName("Chuck");
      testIdentityLabel.setLabelValue("Norris");
      this.testIdentity = this.datamodelFactory.createIdentifier();
      this.testIdentity.addIdentifierLabel(testIdentityLabel);

      this.testIO = this.datamodelFactory.createInformationObject();
      this.testIO.setIdentifier(this.testIdentifier);
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
   }

   @Test
   public void testRSGetRequest() {
      RSGetRequest m = new RSGetRequest(this.testIdentifier);
      m.setDownloadBinaryObject(true);
      m.setFetchAllVersions(true);
      testEncoder(m);
   }

   @Test
   public void testRSGetResponse() {
      RSGetResponse m = new RSGetResponse();
      m.addInformationObject(this.testIO);
      testEncoder(m);
   }

   @Test
   public void testRSPutRequest() {
      RSPutRequest m = new RSPutRequest(this.testIO);
      testEncoder(m);
   }

   @Test
   public void testRSPutResponse() {
      RSPutResponse m = new RSPutResponse();
      testEncoder(m);
   }

   @Test
   public void testRSGetNameRequest() {
      RSGetNameRequest m = new RSGetNameRequest(this.testIdentifier);
      testEncoder(m);
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

   @Test
   public void testTCStartTransferRequest() {
      TCStartTransferRequest m = new TCStartTransferRequest();
      m.setSource("TestSource");
      m.setTransferServiceToUse("testings");
      testEncoder(m);
   }

   @Test
   public void testTCStartTransferResponse() {
      TCStartTransferResponse m = new TCStartTransferResponse();
      m.setSource("source");
      m.setDestination("destination");
      m.setJobId("jobID");
      testEncoder(m);
   }

   @Test
   public void testTCChangeTransferRequest() {
      TCChangeTransferRequest m = new TCChangeTransferRequest();
      m.setProceed(true);
      m.setJobId("jobID");
      testEncoder(m);
   }

   @Test
   public void testTCChangeTransferResponse() {
      TCChangeTransferResponse m = new TCChangeTransferResponse();
      m.setSource("source");
      m.setNewDestination("destination");
      m.setJobId("jobID");
      testEncoder(m);
   }

   @Test
   public void testSCGetByQueryTemplateRequest() {
      SCGetByQueryTemplateRequest m = new SCGetByQueryTemplateRequest("test", 42);
      m.addParameter("param1");
      m.addParameter("param2");
      testEncoder(m);
   }

   @Test
   public void testSCGetBySPARQLRequest() {
      SCGetBySPARQLRequest m = new SCGetBySPARQLRequest("Netinf", 42);
      testEncoder(m);
   }

   @Test
   public void testSCSearchResponse() {
      SCSearchResponse m = new SCSearchResponse();
      m.addResultIdentifier(this.testIdentifier);
      testEncoder(m);
   }

   @Test
   public void testSCGetTimeoutAndNewSearchIDRequest() {
      SCGetTimeoutAndNewSearchIDRequest m = new SCGetTimeoutAndNewSearchIDRequest(42);
      testEncoder(m);
   }

   @Test
   public void testSCGetTimeoutAndNewSearchIDResponse() {
      SCGetTimeoutAndNewSearchIDResponse m = new SCGetTimeoutAndNewSearchIDResponse(42, 21);
      testEncoder(m);
   }

   private void testEncoder(NetInfMessage message) {
      byte[] encodedMessage = this.messageEncoder.encodeMessage(message);
      NetInfMessage decodedMessage = this.messageEncoder.decodeMessage(encodedMessage);
      assertEquals(message, decodedMessage);
   }
}
