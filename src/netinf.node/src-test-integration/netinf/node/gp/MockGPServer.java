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
package netinf.node.gp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import netinf.node.gp.messages.GPNetInfMessages.NICapability;
import netinf.node.gp.messages.GPNetInfMessages.NIResolution;
import netinf.node.gp.messages.GPNetInfMessages.NIaddName;
import netinf.node.gp.messages.GPNetInfMessages.NImoveEP;
import netinf.node.gp.messages.GPNetInfMessages.NIprepareGP;
import netinf.node.gp.messages.GPNetInfMessages.NIresolve;
import netinf.node.gp.messages.GPNetInfMessages.NIresolveCallback;
import netinf.node.gp.messages.GPNetInfMessages.NIMessageContainer.NIMessageType;

import org.apache.log4j.Logger;

/**
 * A server that represents the GP counterpart for the communication. Might be started alone via
 * {@link MockGPServer#main(String[])}, or instantiated along with other operations.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class MockGPServer implements Runnable {

   private static final int GP_PORT = 6666;

   /**
    * The mockup server might be started alone
    * 
    * @param args
    * @throws IOException
    */
   public static void main(String[] args) throws IOException {
      MockGPServer myServer = new MockGPServer(GP_PORT, null);

      myServer.start();
   }

   private static final Logger LOG = Logger.getLogger(MockGPServer.class);

   public static final int DESTINATION_PORT = 12345;

   private Socket socket;
   private final int port;
   private boolean interrupted;

   private GPCommunicator gpCommunicator;
   private Thread runner;

   private final List<NIaddName> names;

   private final Properties properties;

   public MockGPServer(int port, Properties properties) {
      this.properties = properties;
      interrupted = false;
      this.port = port;

      names = new ArrayList<NIaddName>();
   }

   /**
    * Is blocking.
    * 
    * @throws IOException
    */
   public void start() throws IOException {
      LOG.trace(null);

      LOG.debug("Starting to listen for messages on port '" + port + "'");
      runner = new Thread(this);
      runner.start();
   }

   public void run() {
      try {
         final ServerSocket serverSocket = new ServerSocket(port);

         socket = serverSocket.accept();
         DataInputStream in = new DataInputStream(socket.getInputStream());
         DataOutputStream out = new DataOutputStream(socket.getOutputStream());

         LOG.debug("Received remote connection on port '" + port + "'");
         gpCommunicator = new GPCommunicator(in, out);

         while (!interrupted) {
            try {
               gpCommunicator.receive();
               NIMessageType messageType = gpCommunicator.getMessageType();

               if (messageType == NIMessageType.ADDNAME) {
                  handleAddName(gpCommunicator.getAddName());
               } else if (messageType == NIMessageType.MOVEEP) {
                  handleMoveEP(gpCommunicator.getMoveEP());
               } else if (messageType == NIMessageType.PREPAREGP) {
                  handlePrepareGP(gpCommunicator.getPrepareGP());
               } else if (messageType == NIMessageType.RESOLVE) {
                  handleResolve(gpCommunicator.getResolve());
               }

            } catch (IOException e) {
               stop();
            }
         }
      } catch (IOException ioException) {
         LOG.error("Something really bad happend!!!");
      }
   }

   private void handleResolve(NIresolve resolve) {
      LOG.trace(null);

      netinf.node.gp.messages.GPNetInfMessages.NIresolveCallback.Builder builder = NIresolveCallback.newBuilder();
      builder.setCallbackId(resolve.getCallbackId());

      netinf.node.gp.messages.GPNetInfMessages.NIResolution.Builder resolutionBuilder = NIResolution.newBuilder();
      resolutionBuilder.setTargetAddress(properties.getProperty("netinf.gp.interface.test.node"));
      resolutionBuilder.setDestinationName("SomeNetInfNode" + ":" + properties.getProperty("netinf.gp.interface.test.node.port"));

      // Set Capability
      NICapability capabilities = resolve.getCapabilities(0);
      netinf.node.gp.messages.GPNetInfMessages.NICapability.Builder capabilityBuilder = capabilities.toBuilder();
      resolutionBuilder.addCapabilities(capabilityBuilder.build());

      builder.addResolutions(resolutionBuilder.build());
      gpCommunicator.sendMessage(builder.build());
   }

   private void handlePrepareGP(NIprepareGP prepareVLCGP) {
      LOG.trace(null);
   }

   private void handleMoveEP(NImoveEP moveEP) {
      LOG.trace(null);
      LOG.debug("Moved to targetEntity '" + moveEP.getTargetEntity() + "'");
   }

   private void handleAddName(NIaddName addName) {
      LOG.trace(null);

      names.add(addName);
   }

   public void stop() throws IOException {
      gpCommunicator.close();
      runner.interrupt();
      interrupted = true;
   }
}
