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
package netinf.common.eventservice;

import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;

import netinf.common.communication.NetInfNodeConnection;
import netinf.common.communication.RemoteNodeConnection;
import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.attribute.DefinedAttributeIdentification;
import netinf.common.datamodel.impl.module.DatamodelImplModule;
import netinf.common.log.module.LogModule;
import netinf.common.messages.ESFEventMessage;
import netinf.common.security.impl.module.SecurityModule;
import netinf.common.utils.Utils;

import org.apache.log4j.PropertyConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.name.Names;

/**
 * @author PG NetInf
 */
public class AbstractEsfConnectorImpTest {

   public static final String IDENTIFICATION_1 = DefinedAttributeIdentification.AMOUNT.getURI();
   public static final String IDENTIFICATION_2 = DefinedAttributeIdentification.PUBLIC_KEY.getURI();
   public static final String IDENTIFICATION_3 = DefinedAttributeIdentification.OWNER.getURI();

   private static final String CONFIGS_TESTING_PROPERTIES = "../configs/testing.properties";
   private MessageReceiver messageReceiver;
   private MessageProcessorImp messageProcessor;
   private LinkedBlockingQueue<ESFEventMessage> messageQueue;
   private Properties properties;
   private MockErrorCommunicator comm;
   private DatamodelFactory dmFactory;
   private Provider<MockErrorCommunicator> provider;
   private Injector injector;

   private String port = "5000";
   private String host = "127.0.0.1";
   private AbstractEsfConnectorImp esfConnector;

   @Before
   public void setUp() throws Exception {

      PropertyConfigurator.configure(Utils.loadProperties(CONFIGS_TESTING_PROPERTIES));

      this.properties = Utils.loadProperties(CONFIGS_TESTING_PROPERTIES);

      injector = Guice.createInjector(new LogModule(properties), new SecurityModule(), new DatamodelImplModule(),
            new TestModule(), new AbstractModule() {

               @Override
               protected void configure() {
                  Names.bindProperties(binder(), properties);
                  bind(NetInfNodeConnection.class).annotatedWith(SecurityModule.Security.class).to(RemoteNodeConnection.class);
               }
            }

      );

      this.messageReceiver = this.injector.getInstance(MessageReceiver.class);
      this.messageProcessor = this.injector.getInstance(MessageProcessorImp.class);
      this.dmFactory = this.injector.getInstance(DatamodelFactory.class);
      // this.provider = this.injector.getProvider(MockErrorCommunicator.class);

      this.messageQueue = new LinkedBlockingQueue<ESFEventMessage>();

      esfConnector = new AbstractEsfConnectorImp(dmFactory, messageReceiver, messageProcessor, host, port);
   }

   @After
   public void tearDown() throws Exception {
   }

   @Test
   public void testRun() {

      // esfConnector.setProvider(provider);

      // esfConnector.setCommunicatorProvider((Provider) provider);

      esfConnector.run();
      esfConnector.stop();
   }

   @Test
   public void testSystemReadyToHandleReceivedMessage() {

   }

   @Test
   public void testAbstractEsfConnectorImp() {

   }

   @Test
   public void testSetIdentityIdentifier() {

   }

   @Test
   public void testSetInitialSubscriptionInformation() {

   }

   @Test
   public void testTearDown() {

   }

   @Test
   public void testSendSubscription() {

   }

   @Test
   public void testSendUnsubscription() {

   }

}
