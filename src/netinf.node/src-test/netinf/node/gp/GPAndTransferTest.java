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
package netinf.node.gp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;
import netinf.common.messages.NetInfMessage;
import netinf.common.messages.TCChangeTransferRequest;
import netinf.common.messages.TCChangeTransferResponse;
import netinf.common.messages.TCStartTransferRequest;
import netinf.common.messages.TCStartTransferResponse;
import netinf.common.transfer.TransferJob;
import netinf.common.utils.Utils;
import netinf.node.api.NetInfNode;
import netinf.node.gp.datamodel.Capability;
import netinf.node.gp.datamodel.GPFactory;
import netinf.node.gp.datamodel.Property;
import netinf.node.gp.datamodel.Resolution;
import netinf.node.gp.messages.GPNetInfMessages.NIResolution;
import netinf.node.gp.messages.GPNetInfMessages.NIaddName;
import netinf.node.gp.messages.GPNetInfMessages.NImoveEP;
import netinf.node.gp.messages.GPNetInfMessages.NIprepareGP;
import netinf.node.gp.messages.GPNetInfMessages.NIresolve;
import netinf.node.gp.messages.GPNetInfMessages.NIresolveCallback;
import netinf.node.transfer.ExecutableTransferJob;
import netinf.node.transfer.TransferJobIdGenerator;
import netinf.node.transfer.gp.TransferJobGP;
import netinf.node.transfer.gp.TransferServiceGP;
import netinf.node.transfer.impl.TransferControllerImpl;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests the transfer and gp related parts of the {@link NetInfNode}. The node does not have to be started for running the tests.
 * All parts of the node, which are necessary for the execution are initialized on their own.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class GPAndTransferTest {
   private static final String NAME_OF_NODE = "name_of_node";

   private static final String TARGET_ADDRESS = "localhost";
   private static final String DESTINATION_NAME = "NetInfNode:12345";

   private static final String TEST_ID = "testID";

   private static final Logger LOG = Logger.getLogger(GPAndTransferTest.class);

   private static int portNumber = 6666;
   private static DataInputStream in;
   private static DataOutputStream out;
   private static Socket socket;
   private static TransferControllerImpl transferController;
   private static TransferServiceGP transferServiceGP;
   private static GPCommunicator communicator;

   private static final String SOURCE = "abcde";
   private static final String NEW_DESTINATION = "name_of_end_point";

   private static GPNetInfInterfaceImpl gpNInfInterface;

   private static GPListener gpListener;

   @BeforeClass
   public static void setup() throws IOException {
      PropertyConfigurator.configure(Utils.loadProperties("../configs/testing.properties"));

      final ServerSocket serverSocket = new ServerSocket(portNumber);
      Thread thread = new Thread() {
         @Override
         public void run() {
            try {
               socket = serverSocket.accept();
               in = new DataInputStream(socket.getInputStream());
               out = new DataOutputStream(socket.getOutputStream());
            } catch (IOException ioException) {
               LOG.error("Could not create connection");
            }
         }
      };
      thread.start();

      transferController = new TransferControllerImpl(new TransferJobIdGenerator() {
         @Override
         public String getNextId() {
            return TEST_ID;
         }
      });

      GPFactory gpFactory = new GPFactory();
      GPCommunicationBuffer gpCommunicationBuffer = new GPCommunicationBuffer();
      gpListener = new GPListener(gpCommunicationBuffer);

      gpNInfInterface = new GPNetInfInterfaceImpl(gpFactory, "localhost", String.valueOf(portNumber), gpListener,
            gpCommunicationBuffer);
      gpListener.start(gpNInfInterface);
      transferServiceGP = new TransferServiceGP(gpNInfInterface);
      transferController.addTransferService(transferServiceGP);

      communicator = new GPCommunicator(in, out);
   }

   @AfterClass
   public static void tearDown() throws IOException {
      in.close();
      out.close();
      socket.close();
      communicator.close();

      gpNInfInterface.tearDown();
   }

   @Test
   public void testStartDownload() throws IOException, InterruptedException {
      transferController.startTransfer(SOURCE, null, null);

      // Receive the message
      NIprepareGP nIprepareVLCGP = communicator.receiveNIprepareGP();
      LOG.debug("Received message \n" + nIprepareVLCGP);

      // Send the vlcready message
      TransferJob transferJob = transferController.getTransferJob(TEST_ID);
      Assert.assertEquals(transferJob.getSource(), SOURCE);
   }

   @Test
   public void testChangeDownload() throws IOException {
      ExecutableTransferJob changeTransfer = transferController.changeTransfer(TEST_ID, NEW_DESTINATION, true, null);

      NImoveEP nImoveEP = communicator.receiveNImoveEP();
      LOG.debug("Received message \n" + nImoveEP);

      Assert.assertEquals(NEW_DESTINATION, nImoveEP.getTargetEntity());

      TransferJob transferJob = transferController.getTransferJob(TEST_ID);

      Assert.assertSame(changeTransfer, transferJob);
      Assert.assertEquals(NEW_DESTINATION, transferJob.getDestination());
   }

   @Test
   public void testCompleteDownload() {
      TransferJobGP transferJob = (TransferJobGP) transferController.getTransferJob(TEST_ID);
      transferJob.setCompleted(true);

      ExecutableTransferJob transferJob2 = transferController.getTransferJob(TEST_ID);
      Assert.assertNull(transferJob2);
   }

   // The message Tests

   @Test
   public void testStartDownloadMessage() throws IOException, InterruptedException {
      final TCStartTransferRequest tcStartTransferRequest = new TCStartTransferRequest();
      tcStartTransferRequest.setSource(SOURCE);
      tcStartTransferRequest.setTransferServiceToUse(TransferServiceGP.TRANSFER_SERVICE_NAME);

      NetInfMessage processNetInfMessage = transferController.processNetInfMessage(tcStartTransferRequest);

      // Receive the message
      NIprepareGP nIprepareGP = communicator.receiveNIprepareGP();
      LOG.debug("Received message \n" + nIprepareGP);

      // Send the vlcready message

      TransferJob transferJob = transferController.getTransferJob(TEST_ID);
      Assert.assertEquals(transferJob.getSource(), SOURCE);
      Assert.assertEquals(transferJob.getDestination(), null);

      TCStartTransferResponse response = (TCStartTransferResponse) processNetInfMessage;
      Assert.assertEquals(response.getSource(), response.getSource());
      Assert.assertEquals(response.getDestination(), null);
      Assert.assertEquals(response.getJobId(), TEST_ID);
      Assert.assertNull(response.getErrorMessage());
   }

   @Test
   public void testWrongTransferService() {
      final TCStartTransferRequest tcStartTransferRequest = new TCStartTransferRequest();
      tcStartTransferRequest.setSource(SOURCE);
      tcStartTransferRequest.setTransferServiceToUse("not existing");

      NetInfMessage processNetInfMessage = transferController.processNetInfMessage(tcStartTransferRequest);
      TCStartTransferResponse tcStartTransferResponse = (TCStartTransferResponse) processNetInfMessage;

      Assert.assertNotNull(tcStartTransferResponse.getErrorMessage());
   }

   @Test
   public void testChangeDownloadMessage() throws IOException {
      TCChangeTransferRequest tcChangeTransferRequest = new TCChangeTransferRequest();
      tcChangeTransferRequest.setJobId(TEST_ID);
      tcChangeTransferRequest.setNewDestination(NEW_DESTINATION);
      tcChangeTransferRequest.setProceed(true);

      TCChangeTransferResponse response = (TCChangeTransferResponse) transferController
            .processNetInfMessage(tcChangeTransferRequest);
      NImoveEP nImoveEP = communicator.receiveNImoveEP();

      Assert.assertEquals(NEW_DESTINATION, nImoveEP.getTargetEntity());

      TransferJob transferJob = transferController.getTransferJob(TEST_ID);
      Assert.assertEquals(NEW_DESTINATION, transferJob.getDestination());

      Assert.assertEquals(response.getJobId(), TEST_ID);
      Assert.assertEquals(response.getSource(), SOURCE);
      Assert.assertEquals(response.getNewDestination(), NEW_DESTINATION);
   }

   @SuppressWarnings("unchecked")
   @Test
   public void testResolve() throws IOException, InterruptedException {
      Capability capability = new Capability();
      capability.addProperty(new Property("name", "value"));
      capability.setName("netinf.common");
      final ArrayList<Capability> list = new ArrayList<Capability>();
      list.add(capability);

      gpNInfInterface.addName(NAME_OF_NODE, list);
      NIaddName receiveNIaddName = communicator.receiveNIaddName();
      Assert.assertEquals(NAME_OF_NODE, receiveNIaddName.getName());

      final List<Resolution>[] resolve = new List[1];

      Thread thread = new Thread() {
         @Override
         public void run() {
            resolve[0] = gpNInfInterface.resolve("", list);
         }
      };
      thread.start();

      NIresolve receiveNIresolve = communicator.receiveNIresolve();

      netinf.node.gp.messages.GPNetInfMessages.NIresolveCallback.Builder builder = NIresolveCallback.newBuilder();
      builder.setCallbackId(receiveNIresolve.getCallbackId());
      builder.addResolutions(NIResolution.newBuilder().addCapabilities(receiveNIresolve.getCapabilities(0))
            .setTargetAddress(TARGET_ADDRESS).setDestinationName(DESTINATION_NAME).build());

      NIresolveCallback build = builder.build();
      communicator.sendMessage(build);

      thread.join();

      List<Resolution> resolutionList = resolve[0];
      Assert.assertNotNull(resolutionList);
      Assert.assertTrue(resolutionList.size() > 0);

      Resolution resolution = resolutionList.get(0);
      String targetAddress = resolution.getTargetAddress();

      Assert.assertTrue(targetAddress.startsWith(TARGET_ADDRESS));
   }
}
