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
package netinf.node.resolution.pastry;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.InformationObject;
import netinf.common.exceptions.NetInfCheckedException;
import netinf.common.exceptions.NetInfResolutionException;
import netinf.node.resolution.AbstractResolutionService;
import netinf.node.resolution.InformationObjectHelper;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;

/**
 * The Class MultipleNodesIntegrationTest.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
@Ignore("Long runtime")
public class MultipleNodesIntegrationTest extends InformationObjectHelper {

   @Inject
   public MultipleNodesIntegrationTest() {
      super(null);
   }

   /**
    * The Class NodePutCommand.
    * 
    * @author PG Augnet 2, University of Paderborn
    */
   private class NodePutCommand implements Runnable {

      private final AbstractResolutionService resolutionService;

      private final InformationObject io;

      NodePutCommand(AbstractResolutionService node, InformationObject io) {
         resolutionService = node;
         this.io = io;
      }

      @Override
      public void run() {
         try {
            resolutionService.put(io);
         } catch (NetInfResolutionException e) {
            throw new RuntimeException(e);
         }

      }

   }

   /**
    * The Class NodeGetCommand.
    * 
    * @author PG Augnet 2, University of Paderborn
    */
   private class NodeGetCommand implements Runnable {

      private final AbstractResolutionService resolutionService;

      private final InformationObject io;

      private boolean isCorrect = false;

      NodeGetCommand(AbstractResolutionService node, InformationObject io) {
         resolutionService = node;
         this.io = io;
      }

      public boolean isCorrect() {
         return isCorrect;
      }

      @Override
      public void run() {
         try {
            InformationObject retrievedIO = resolutionService.get(io.getIdentifier());
            isCorrect = io.equals(retrievedIO);

         } catch (NetInfResolutionException ex) {
            throw new RuntimeException(ex);
         }

      }

   }

   private static final int NODE_NUMBER = 15;

   private static final int IOS_PER_NODE = 10;

   private List<PastryResolutionService> resolutionServices;

   private static List<NodeGetCommand> getterCommands;

   @Before
   public void setUp() throws Exception {
      // Injector injector = Guice.createInjector(##new PastryTestModule(), new DatamodelImplModule());
      Injector injector = Guice.createInjector(new PastryTestModule());
      datamodelFactory = injector.getInstance(DatamodelFactory.class);

      Provider<PastryResolutionService> provider = injector.getProvider(PastryResolutionService.class);
      resolutionServices = new ArrayList<PastryResolutionService>();
      for (int i = 0; i < NODE_NUMBER; i++) {
         resolutionServices.add(provider.get());
      }
      getterCommands = new ArrayList<NodeGetCommand>();
   }

   @Test
   public void testPutGet() throws NetInfCheckedException {
      List<InformationObject> insertedIOs = new ArrayList<InformationObject>();

      for (int i = 0; i < NODE_NUMBER; i++) {
         for (int j = 0; j < IOS_PER_NODE; j++) {
            InformationObject io = createUniqueIO();
            resolutionServices.get(i).put(io);
            insertedIOs.add(io);
         }
      }

      for (InformationObject io : insertedIOs) {
         InformationObject retrievedIO = resolutionServices.get(0).get(io.getIdentifier());
         Assert.assertEquals(io, retrievedIO);
      }
   }

   @Test
   public void testPutDeleteGet() throws NetInfCheckedException {
      InformationObject obj = createUniqueIO();
      resolutionServices.get(0).put(obj);
      InformationObject retrievedIo = resolutionServices.get(NODE_NUMBER - 1).get(obj.getIdentifier());
      Assert.assertEquals(obj, retrievedIo);
      resolutionServices.get(NODE_NUMBER - 1).delete(obj.getIdentifier());
      retrievedIo = resolutionServices.get(0).get(obj.getIdentifier());
      Assert.assertNull(retrievedIo);
   }

   @Test
   public void testConcurrentPutGet() throws NetInfCheckedException, InterruptedException {
      List<InformationObject> insertedIOs = new ArrayList<InformationObject>();
      ThreadPoolExecutor executor = new ThreadPoolExecutor(5, 10, 10, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(10000));

      for (int i = 0; i < NODE_NUMBER; i++) {
         for (int j = 0; j < IOS_PER_NODE; j++) {
            InformationObject io = createUniqueIO();
            executor.execute(new NodePutCommand(resolutionServices.get(i), io));
            insertedIOs.add(io);
         }
      }

      for (InformationObject io : insertedIOs) {
         NodeGetCommand getter = new NodeGetCommand(resolutionServices.get(0), io);
         executor.execute(getter);
         getterCommands.add(getter);
      }
      executor.shutdown();
      executor.awaitTermination(30, TimeUnit.SECONDS);

      for (NodeGetCommand getter : getterCommands) {
         Assert.assertTrue(getter.isCorrect());
      }
   }

   @After
   public void tearDown() throws Exception {
      for (PastryResolutionService prs : resolutionServices) {
         prs.getPastryNode().destroy();
      }
      getterCommands = null;
   }

}
