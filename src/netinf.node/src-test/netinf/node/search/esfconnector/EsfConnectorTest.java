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
package netinf.node.search.esfconnector;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;

import junit.framework.Assert;
import netinf.access.TCPServer;
import netinf.common.communication.Communicator;
import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.DefinedAttributePurpose;
import netinf.common.datamodel.DefinedLabelName;
import netinf.common.datamodel.IdentifierLabel;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.attribute.DefinedAttributeIdentification;
import netinf.common.datamodel.creator.ValidCreator;
import netinf.common.datamodel.identity.IdentityObject;
import netinf.common.eventservice.MessageReceiver;
import netinf.common.exceptions.NetInfCheckedException;
import netinf.common.messages.ESFEventMessage;
import netinf.common.messages.ESFFetchMissedEventsResponse;
import netinf.common.security.SignatureAlgorithm;
import netinf.common.utils.Utils;
import netinf.node.rdf.ClearSDBDatabase;
import netinf.node.resolution.ResolutionController;
import netinf.node.resolution.rdf.RDFResolutionService;
import netinf.node.search.rdf.SearchServiceRDFTestModule;

import org.apache.log4j.PropertyConfigurator;
import org.junit.Before;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provider;

/**
 * Unit tests for the {@link MessageReceiver} and {@link SearchEsfMessageProcessor}
 * 
 * @author PG Augnet 2, University of Paderborn
 * @see SearchServiceRDFTestModule
 */
public class EsfConnectorTest {

   private static final String CONFIGS_TESTING_PROPERTIES = "../configs/testing.properties";
   private RDFResolutionService rdfResolutionService;
   private MessageReceiver messageReceiver;
   private SearchEsfMessageProcessor searchEsfMessageProcessor;
   private ResolutionController resController;
   private LinkedBlockingQueue<ESFEventMessage> messageQueue;
   private Properties properties;
   private Communicator comm;
   private DatamodelFactory dmFactory;
   private TCPServer server;
   private Provider<Communicator> provider;
   private Injector injector;

   @Before
   public void setUp() {
      PropertyConfigurator.configure(Utils.loadProperties(CONFIGS_TESTING_PROPERTIES));
      ClearSDBDatabase.main(null);

      this.properties = Utils.loadProperties(CONFIGS_TESTING_PROPERTIES);
      this.injector = Guice.createInjector(new SearchServiceRDFTestModule(this.properties));
      this.rdfResolutionService = this.injector.getInstance(RDFResolutionService.class);
      this.messageReceiver = this.injector.getInstance(MessageReceiver.class);
      this.searchEsfMessageProcessor = this.injector.getInstance(SearchEsfMessageProcessor.class);
      this.resController = this.injector.getInstance(ResolutionController.class);
      this.resController.addResolutionService(this.rdfResolutionService);
      this.dmFactory = this.injector.getInstance(DatamodelFactory.class);
      this.provider = this.injector.getProvider(Communicator.class);

      this.messageQueue = new LinkedBlockingQueue<ESFEventMessage>();

      ValidCreator.setDatamodelFactory(this.injector.getInstance(DatamodelFactory.class));
      ValidCreator.setSignatureAlgorithm(this.injector.getInstance(SignatureAlgorithm.class));
   }

   @Test
   public void messageReceiverGetsESFEventMessages() {

      // prepare and start server (as message receiver) and communicator (as event service)
      prepareMessageReceiverTest();

      ESFEventMessage message = buildEventMessageCreation1();

      try {
         this.comm.send(message);
      } catch (NetInfCheckedException e1) {
         e1.printStackTrace();
      }

      // wait some time till the message receiver thread has presumably finished its processing
      try {
         Thread.sleep(1000);
      } catch (InterruptedException e) {
         e.printStackTrace();
      }

      Assert.assertEquals(1, this.messageQueue.size());
      Assert.assertTrue(this.messageQueue.contains(message));

      tearDownMessageReceiverTest();
   }

   @Test
   public void messageReceiverGetsESFFetchMissedEventsResponse() {

      // prepare and start server (as message receiver) and communicator (as event service)
      prepareMessageReceiverTest();

      ESFFetchMissedEventsResponse message = buildFetchMissedEventsResponse();

      try {
         this.comm.send(message);
      } catch (NetInfCheckedException e1) {
         e1.printStackTrace();
      }

      // wait some time till the message receiver thread has presumably finished its processing
      try {
         Thread.sleep(1000);
      } catch (InterruptedException e) {
         e.printStackTrace();
      }

      Assert.assertEquals(2, this.messageQueue.size());
      for (ESFEventMessage eventMsg : message.getEventMessages()) {
         Assert.assertTrue(this.messageQueue.contains(eventMsg));
      }

      tearDownMessageReceiverTest();
   }

   @Test
   public void messageProcessorHandlesDeleteNotification() {
      ESFEventMessage message = buildEventMessageDeletion();

      this.resController.put(message.getOldInformationObject());

      InformationObject result = this.rdfResolutionService.get(message.getOldInformationObject().getIdentifier());
      Assert.assertEquals(message.getOldInformationObject(), result);

      try {
         this.messageQueue.put(message);
      } catch (InterruptedException e) {
         e.printStackTrace();
      }

      this.searchEsfMessageProcessor.setMessageQueue(this.messageQueue);
      this.searchEsfMessageProcessor.start();

      // wait some time till the message processor thread has presumably finished its processing
      try {
         Thread.sleep(1000);
      } catch (InterruptedException e) {
         e.printStackTrace();
      }

      InformationObject result2 = this.rdfResolutionService.get(message.getOldInformationObject().getIdentifier());
      Assert.assertNull(result2);
   }

   @Test
   public void messageProcessorHandlesCreationModificationNotification() {
      ESFEventMessage message1 = buildEventMessageCreation1();

      try {
         this.messageQueue.put(message1);
      } catch (InterruptedException e) {
         e.printStackTrace();
      }

      this.searchEsfMessageProcessor.setMessageQueue(this.messageQueue);
      this.searchEsfMessageProcessor.start();

      // wait some time till the message processor thread has presumably finished its processing
      try {
         Thread.sleep(1000);
      } catch (InterruptedException e) {
         e.printStackTrace();
      }

      InformationObject result = this.rdfResolutionService.get(message1.getNewInformationObject().getIdentifier());
      InformationObject io = buildEventMessageCreation1().getNewInformationObject();
      Assert.assertEquals(io, result);
   }

   @Test
   public void handleMessageInReceiverAndProcessor() {

      prepareMessageReceiverTest();

      this.searchEsfMessageProcessor.setMessageQueue(this.messageQueue);
      this.searchEsfMessageProcessor.start();

      ESFFetchMissedEventsResponse message = buildFetchMissedEventsResponse();

      try {
         this.comm.send(message);
      } catch (NetInfCheckedException e1) {
         e1.printStackTrace();
      }

      // wait some time till the message processor thread has presumably finished its processing
      try {
         Thread.sleep(1000);
      } catch (InterruptedException e) {
         e.printStackTrace();
      }

      InformationObject result = null;
      for (ESFEventMessage eventMsg : buildFetchMissedEventsResponse().getEventMessages()) {
         result = this.rdfResolutionService.get(eventMsg.getNewInformationObject().getIdentifier());
         Assert.assertEquals(eventMsg.getNewInformationObject(), result);
         result = null;
      }

      tearDownMessageReceiverTest();

   }

   private void prepareMessageReceiverTest() {
      // TCP server represents message receiver of the esf connector
      this.server = new TCPServer(6000);
      this.server.injectProviderCommunicator(this.provider);
      this.messageReceiver.setMessageQueue(this.messageQueue);
      this.server.setAsyncReceiveHandler(this.messageReceiver);
      try {
         this.server.start();
      } catch (NetInfCheckedException e1) {
         e1.printStackTrace();
      }

      // start communicator (represents event service)
      try {
         this.comm = this.injector.getInstance(Communicator.class);
         this.comm.setup("localhost", 6000);
      } catch (IOException e) {
         System.out.println("error while setting up communicator");
      }
      this.comm.setSerializeFormat(this.dmFactory.getSerializeFormat());
   }

   private void tearDownMessageReceiverTest() {
      try {
         this.comm.close();
      } catch (NetInfCheckedException e) {
         e.printStackTrace();
      }
      try {
         this.server.stop();
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   private ESFEventMessage buildEventMessageDeletion() {
      ESFEventMessage message = new ESFEventMessage();
      InformationObject ioOld = ValidCreator.createValidIdentityObject(Utils.stringToPublicKey(this.properties
            .getProperty("search_rdf_publicKey")));
      message.setOldInformationObject(ioOld);
      message.setMatchedSubscriptionIdentification("test");
      return message;
   }

   private ESFEventMessage buildEventMessageCreation1() {
      ESFEventMessage message = new ESFEventMessage();
      InformationObject ioNew = ValidCreator.createValidIdentityObject(Utils.stringToPublicKey(this.properties
            .getProperty("search_rdf_publicKey")));
      ioNew.addAttribute(ValidCreator.createValidAttribute(DefinedAttributeIdentification.PERSON_NAME, "Musterfrau",
            DefinedAttributePurpose.USER_ATTRIBUTE));
      message.setNewInformationObject(ioNew);
      message.setMatchedSubscriptionIdentification("test");
      return message;
   }

   private ESFEventMessage buildEventMessageCreation2() {
      ESFEventMessage message = new ESFEventMessage();
      IdentityObject ioNew = ValidCreator.createValidIdentityObject(Utils.stringToPublicKey(this.properties
            .getProperty("search_rdf_publicKey")));
      IdentifierLabel label = this.dmFactory.createIdentifierLabel();
      label.setLabelName(DefinedLabelName.UNIQUE_LABEL.getLabelName());
      label.setLabelValue("dfhskjf");
      ioNew.getIdentifier().addIdentifierLabel(label);
      ioNew.addAttribute(ValidCreator.createValidAttribute(DefinedAttributeIdentification.PERSON_NAME, "Mustermann",
            DefinedAttributePurpose.USER_ATTRIBUTE));
      message.setNewInformationObject(ioNew);
      message.setMatchedSubscriptionIdentification("test");
      return message;
   }

   private ESFFetchMissedEventsResponse buildFetchMissedEventsResponse() {
      ESFFetchMissedEventsResponse responseMessage = new ESFFetchMissedEventsResponse();
      responseMessage.addEventMessage(buildEventMessageCreation1());
      responseMessage.addEventMessage(buildEventMessageCreation2());
      return responseMessage;
   }
}
