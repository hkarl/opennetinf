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
import static org.junit.Assert.fail;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

import java.util.Properties;

import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.IdentifierLabel;
import netinf.common.datamodel.InformationObject;
import netinf.common.messages.*;
import netinf.common.utils.Utils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.internal.runners.statements.Fail;

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
   private Identifier testIdentifier2;
   private Identifier testIdentity;
   private InformationObject testIO;
   private InformationObject oldTestIO; //Only for the ESFEventMessage test

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
      testIdentifierLabel2.setLabelName("Universität");
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
      //Added in NetInf 3
      Assert.assertThat(m,is(notNullValue()));
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
   public void testESFFetchMissedEventsRequest()
   {
	   ESFFetchMissedEventsRequest m = new ESFFetchMissedEventsRequest();
	   m.setPrivateKey("123");
	   m.setUserName("Chuck Norris");
	   m.setSerializeFormat(SerializeFormat.RDF);
	   m.setErrorMessage("Success");
	   testEncoder(m);
   }
   
   @Test
   public void testESFFetchMissedEventsResponse()
   {
	   ESFFetchMissedEventsResponse m = new ESFFetchMissedEventsResponse();
	   ESFEventMessage eventm = new ESFEventMessage();
	   eventm.setNewInformationObject(testIO);
	   eventm.setOldInformationObject(oldTestIO);
	   eventm.setUserName("Chuck Norris");
	   eventm.setMatchedSubscriptionIdentification("sid:12345678");
	   eventm.setSerializeFormat(SerializeFormat.RDF);
	   m.addEventMessage(eventm);
	   m.setPrivateKey("123");
	   m.setUserName("Chuck Norris");
	   m.setSerializeFormat(SerializeFormat.RDF);
	   m.setErrorMessage("Success");
	   testEncoder(m);
   }
   
   @Test
   public void testMessageDescription()
   {
	   ESFFetchMissedEventsRequest m = new ESFFetchMissedEventsRequest();
	   m.setPrivateKey("123");
	   m.setUserName("Chuck Norris");
	   m.setSerializeFormat(SerializeFormat.RDF);
	   
	   assertEquals(m.describe(), "a message called "+ESFFetchMissedEventsRequest.class.getSimpleName());
   }

   @Test
   public void testMessageDescriptionWithError()
   {
	   ESFFetchMissedEventsRequest m = new ESFFetchMissedEventsRequest();
	   m.setPrivateKey("123");
	   m.setUserName("Chuck Norris");
	   m.setSerializeFormat(SerializeFormat.RDF);
	   m.setErrorMessage("TERR");
	   assertEquals(m.describe(), "a message called "+ESFFetchMissedEventsRequest.class.getSimpleName()+"; Error: "+"TERR");
   }
   
   @Test
   public void testMessageHashCode()
   {
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
	   
	   assertEquals(run1,run2);
	   if(m.equals(m2))
			   {
		   		assertEquals(m.hashCode(),m2.hashCode());
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
   @Test
   public void testGetUniqueEncoderID(){
	   /*See if encoder ID is always returned correctly.
	    * XML Encoder has id 2
	    * Protobuf Encoder has id 1 
	    */
	   if(messageEncoder.getClass() == MessageEncoderProtobuf.class)
		   assertEquals(messageEncoder.getUniqueEncoderId(),1);
	   else{
		   if(messageEncoder.getClass() == MessageEncoderXML.class){
			   assertEquals(messageEncoder.getUniqueEncoderId(),2);
		   }
		   else
		   {
			   //If none of the classes here match, then the MessageEncoder is unknown and the test fails
			   fail();
		   }
	   }
	   
   }
   private void testEncoder(NetInfMessage message) {
	  //TODO: Comparing a string created by the encoder with the same string decoded seems to not make sense. Use a set of fixed values 
      byte[] encodedMessage = this.messageEncoder.encodeMessage(message);
      NetInfMessage decodedMessage = this.messageEncoder.decodeMessage(encodedMessage);
      assertEquals(message, decodedMessage);
   }
}
