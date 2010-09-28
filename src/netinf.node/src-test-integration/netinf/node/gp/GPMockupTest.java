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

import java.io.IOException;
import java.util.Properties;

import junit.framework.Assert;
import netinf.common.application.module.SecuredApplicationModule;
import netinf.common.communication.RemoteNodeConnection;
import netinf.common.communication.SerializeFormat;
import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.DatamodelTest;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.InformationObject;
import netinf.common.exceptions.NetInfCheckedException;
import netinf.common.transfer.TransferJob;
import netinf.common.utils.Utils;
import netinf.node.StarterNode;

import org.apache.log4j.PropertyConfigurator;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * This test creates an instance of the NetInfNode itself. <b>Not necessary to start a node before.</b>
 * <p>
 * The tests main purpose is to test the setup and the communication via the {@link RemoteNodeConnection}
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class GPMockupTest {

   private static RemoteNodeConnection remoteNodeConnection;
   private static String jobId;
   private static String source;
   private static MockGPServer mockGPServerResolveName;
   private static MockGPServer mockGPServerAddName;

   private static DatamodelFactory datamodelFactory;

   @BeforeClass
   public static void setup() throws IOException {
      Properties properties = Utils
            .loadProperties("../netinf.node/src-test-integration/netinf/node/gp/communicator.properties");
      PropertyConfigurator.configure(properties);
      int resolveNamePort = Integer.parseInt(properties.getProperty("netinf.gp.interface.port.resolveNamePort"));
      int addNamePort = Integer.parseInt(properties.getProperty("netinf.gp.interface.port.addNamePort"));
      int netinfPort = Integer.parseInt(properties.getProperty("cc.tcp.port"));
      String netinfhost = properties.getProperty("cc.tcp.host");

      // Unfortunately, we have to give the GPNodeModule hiere
      Injector injector = Guice.createInjector(new SecuredApplicationModule("../configs_official/createIOs.properties"));
      datamodelFactory = injector.getInstance(DatamodelFactory.class);

      remoteNodeConnection = injector.getInstance(RemoteNodeConnection.class);

      remoteNodeConnection.setHostAndPort(netinfhost, netinfPort);
      remoteNodeConnection.setSerializeFormat(SerializeFormat.RDF);

      mockGPServerResolveName = new MockGPServer(resolveNamePort, properties);
      mockGPServerResolveName.start();

      mockGPServerAddName = new MockGPServer(addNamePort, properties);
      mockGPServerAddName.start();

      // Start the node
      StarterNode.main(new String[] { "netinf.node.gp.AddNameNodeMockupModule" });
      StarterNode.main(new String[] { "netinf.node.gp.ResolveNameNodeMockupModule" });

      // Wait a little bit, until the node is completely started. The node sends some pieces of information to the
      // GP counterpart. After that it is ready to handle the requests.
      // try {
      // Thread.sleep(10000);
      // } catch (InterruptedException e) {
      // e.printStackTrace();
      // }
   }

   @AfterClass
   public static void tearDown() throws IOException {
      mockGPServerResolveName.stop();
      mockGPServerAddName.stop();
   }

   @Test
   /**
    * This test is desired to show that a remote resolution service is found. This resolution service is than contacted to store
    * the desired {@link InformationObject}. The search for the remote resolution service is done with the help of GP.
    * 
    * @throws NetInfCheckedException
    */
   public void putObject() throws NetInfCheckedException {
      InformationObject informationObject = DatamodelTest.createDummyInformationObject(datamodelFactory);

      remoteNodeConnection.putIO(informationObject);
   }

   @Test
   public void getObject() throws NetInfCheckedException {
      InformationObject dummyIO = DatamodelTest.createDummyInformationObject(datamodelFactory);
      Identifier dummyIdentifier = dummyIO.getIdentifier();

      InformationObject io = remoteNodeConnection.getIO(dummyIdentifier);

      Assert.assertEquals(io, dummyIO);
   }

   @Test
   public void startTransfer() throws NetInfCheckedException {
      String testSource = "http://www.google.de";
      TransferJob transferJob = remoteNodeConnection.startTransfer(testSource, null);

      Assert.assertEquals(transferJob.getSource(), testSource);
      jobId = transferJob.getJobId();
      source = transferJob.getSource();
   }

   @Test
   public void changeTransfer() throws NetInfCheckedException {
      String testDestination = "new_entity_name";
      TransferJob changeTransfer = remoteNodeConnection.changeTransfer(jobId, testDestination, true);

      Assert.assertEquals(jobId, changeTransfer.getJobId());
      Assert.assertEquals(source, changeTransfer.getSource());
      Assert.assertEquals(testDestination, changeTransfer.getDestination());
   }
}
