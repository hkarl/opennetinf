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
package netinf.access;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Properties;

import junit.framework.Assert;
import netinf.common.communication.AsyncReceiveHandler;
import netinf.common.communication.Communicator;
import netinf.common.exceptions.NetInfCheckedException;
import netinf.common.messages.NetInfMessage;
import netinf.common.messages.RSGetPriorityResponse;
import netinf.common.utils.Utils;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Testing class for the TCPServer
 * 
 * @author PG NetInf 3, University of Paderborn
 */
public class TCPServerCommunicationTest implements AsyncReceiveHandler {

   private static final String PROPERTIES_PATH = "../configs/testing.properties";
   private static Injector injector;
   private static Properties properties;

   private static final String HOST = "127.0.0.1";

   private static final int LOOPS = 10;
   private NetInfMessage receivedMessage;

   private static NetInfServer server;

   @BeforeClass
   public static void setup() {
      properties = Utils.loadProperties(PROPERTIES_PATH);
      injector = Guice.createInjector(new AccessTestModule(properties));
      server = injector.getInstance(TCPServer.class);
   }

   @After
   public void stopServer() throws IOException {
      server.stop();
   }

   @Test
   public void setupTCPServer() throws IOException, NetInfCheckedException, InterruptedException {
      server.setAsyncReceiveHandler(this);
      server.start();

      int port = Integer.parseInt(properties.getProperty("access.tcp.port"));
      Communicator client = injector.getInstance(Communicator.class);
      client.setup(HOST, port);

      // Send and receive messages
      for (int i = 0; i < LOOPS; i++) {
         RSGetPriorityResponse message = new RSGetPriorityResponse(i);
         client.send(message);

         NetInfMessage receivedMessage = pollForIncomingMessage();
         assertTrue(receivedMessage instanceof RSGetPriorityResponse);
         assertEquals(((RSGetPriorityResponse) receivedMessage).getPriority(), i);
      }
   }

   @Test
   public void testTCPGetPort() throws IOException {
      int port = Integer.parseInt(properties.getProperty("access.tcp.port"));
      Assert.assertEquals(port, ((TCPServer) server).getPort());
   }

   @Test
   public void testTCPDescribe() throws IOException {
      Assert.assertTrue(server.describe().contains("TCP"));
   }

   @Test
   public void testTCPGetAddress() throws IOException, NetInfCheckedException {
      server.start();
      Assert.assertEquals(((TCPServer) server).getAddress(), "0.0.0.0/0.0.0.0");
   }

   @Test
   public void testTCPStop() throws NetInfCheckedException, InterruptedException, IOException {
      server.setAsyncReceiveHandler(this);
      server.start();

      Thread.sleep(100);
      server.stop();

      Assert.assertFalse(server.isRunning());
   }

   @Test(expected = NetInfCheckedException.class)
   public void testTCPStartWithError() throws NetInfCheckedException, InterruptedException, IOException {
      server.setAsyncReceiveHandler(this);
      server.start();

      Thread.sleep(100);
      // start again
      server.start();

      server.stop();
   }

   @Override
   public void receivedMessage(NetInfMessage message, Communicator communicator) {
      receivedMessage = message;
   }

   /**
    * helper method to poll for an incoming message
    */
   private NetInfMessage pollForIncomingMessage() throws InterruptedException {
      NetInfMessage returnValue = null;

      int i = 0;
      while (receivedMessage == null && i++ < 20) {
         Thread.sleep(100);
      }

      returnValue = receivedMessage;
      receivedMessage = null;
      return returnValue;
   }

}
