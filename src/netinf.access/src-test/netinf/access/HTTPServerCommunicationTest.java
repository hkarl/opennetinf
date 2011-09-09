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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

import junit.framework.Assert;
import netinf.common.communication.AsyncReceiveHandler;
import netinf.common.communication.Communicator;
import netinf.common.exceptions.NetInfCheckedException;
import netinf.common.messages.NetInfMessage;
import netinf.common.utils.Utils;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Testing class for the HTTPServer
 * 
 * @author PG NetInf 3, University of Paderborn
 */
public class HTTPServerCommunicationTest implements AsyncReceiveHandler {

   private static final Logger LOG = Logger.getLogger(HTTPServerCommunicationTest.class);
   private static final String PROPERTIES_PATH = "../configs/testing.properties";
   private static Injector injector;
   private static Properties properties;
   private static final String ENCODING_HEADER_NAME = "X-NetInf-Encoding";

   private NetInfMessage receivedMessage;
   private static NetInfServer server;

   @BeforeClass
   public static void setup() {
      properties = Utils.loadProperties(PROPERTIES_PATH);
      injector = Guice.createInjector(new AccessTestModule(properties));
      server = injector.getInstance(HTTPServer.class);
   }

   @After
   public void stopServer() throws IOException {
      server.stop();
   }

   @Test
   public void testReceiveMessageHTTPServer() throws IOException, NetInfCheckedException, InterruptedException {
      LOG.info("TEST - testReceiveMessageHTTPServer()");

      server.setAsyncReceiveHandler(this);
      server.start();

      // first message
      sendSampleHTTPRequest(null);
      receivedMessage = pollForIncomingMessage();
      Assert.assertNotNull(receivedMessage);

      // second message
      sendSampleHTTPRequest("2");
      receivedMessage = pollForIncomingMessage();
      Assert.assertNotNull(receivedMessage);
   }

   @Test
   public void testHTTPDescribe() {
      LOG.info("TEST - testHTTPDescribe()");

      int port = Integer.parseInt(properties.getProperty("access.http.port"));
      String describe = server.describe();
      Assert.assertTrue(describe.contains(port + ""));
      Assert.assertTrue(describe.contains("HTTP"));
   }

   @Test
   public void testHTTPStop() throws NetInfCheckedException, InterruptedException, IOException {
      LOG.info("TEST - testHTTPStop()");

      server.setAsyncReceiveHandler(this);
      server.start();
      Assert.assertTrue(server.isRunning());

      Thread.sleep(100);
      server.stop();
      Assert.assertFalse(server.isRunning());
   }

   @Test(expected = NetInfCheckedException.class)
   public void testHTTPStartWithError() throws NetInfCheckedException, InterruptedException, IOException {
      LOG.info("TEST - testHTTPStartWithError()");

      server.start();
      Thread.sleep(100);
      // start again
      server.start();
   }

   @Override
   public void receivedMessage(NetInfMessage message, Communicator communicator) {
      receivedMessage = message;

      // answer to prevent a timeout (just the same message back ;)
      try {
         communicator.send(message);
      } catch (NetInfCheckedException e) {
         LOG.debug("Sending message back failed");
      }
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

   /**
    * helper method to send a HTTPrequest
    */
   private void sendSampleHTTPRequest(String encoding) {
      try {
         // Construct data (stolen from infox plugin)
         String data = "<?xml version='1.0' encoding='UTF-8'?>\n"
               + "<RSGetRequest>\n"
               + "\t<SerializeFormat>RDF</SerializeFormat>\n"
               + "\t<Identifier>ni:HASH_OF_PK=8c4e559d464e38c68ac6a9760f4aad371470ccf9"
               + "~HASH_OF_PK_IDENT=SHA1~VERSION_KIND=UNVERSIONED~UNIQUE_LABEL=DEMOPAGE</Identifier>\n"
               + "\t<UserName>testUser</UserName>\n" + "\t<PrivateKey>testKey</PrivateKey>\n"
               + "\t<FetchAllVersions>true</FetchAllVersions>\n" + "\t<DownloadBinaryObject>true</DownloadBinaryObject>\n"
               + "</RSGetRequest>";

         // Send data
         int port = Integer.parseInt(properties.getProperty("access.http.port"));
         URL url = new URL("http://127.0.0.1:" + port);
         URLConnection conn = url.openConnection();
         conn.setConnectTimeout(3000); // 3 seconds
         conn.setReadTimeout(3000);

         // Header
         if (encoding != null) {
            conn.setRequestProperty(ENCODING_HEADER_NAME, encoding);
         }
         conn.setRequestProperty("Content-type", "text/xml");
         conn.setRequestProperty("Content-length", data.length() + "");
         conn.setRequestProperty("Connection", "close");

         // send
         conn.setDoOutput(true);
         OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
         wr.write(data);
         wr.flush();

         // Get the response
         try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            rd.close();
         } catch (SocketTimeoutException e) {
            LOG.debug("HTTPRequest timeout");
         }
         wr.close();
      } catch (Exception e) {
         LOG.debug("HTTPRequest failed");
      }
   }
}
