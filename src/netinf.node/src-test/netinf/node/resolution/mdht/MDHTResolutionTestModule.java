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
package netinf.node.resolution.mdht;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Properties;

import netinf.common.communication.NetInfNodeConnection;
import netinf.common.communication.RemoteNodeConnection;
import netinf.common.datamodel.impl.module.DatamodelImplModule;
import netinf.common.security.impl.module.SecurityModule;
import netinf.node.resolution.pastry.NetInfPastryParameters;
import netinf.node.resolution.pastry.PastNodePair;
import netinf.node.resolution.pastry.logging.NetInfPastryLogManager;
import netinf.node.resolution.pastry.module.PastNodePairProvider;
import rice.environment.Environment;
import rice.environment.logging.LogManager;
import rice.p2p.commonapi.IdFactory;
import rice.pastry.NodeIdFactory;
import rice.pastry.PastryNode;
import rice.pastry.PastryNodeFactory;
import rice.pastry.commonapi.PastryIdFactory;
import rice.pastry.socket.SocketPastryNodeFactory;
import rice.pastry.standard.RandomNodeIdFactory;
import rice.persistence.Cache;
import rice.persistence.LRUCache;
import rice.persistence.MemoryStorage;
import rice.persistence.Storage;
import rice.persistence.StorageManager;
import rice.persistence.StorageManagerImpl;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Names;

/***
 * Test module for MDHT
 * 
 * @author razvan
 * @deprecated
 */
public class MDHTResolutionTestModule extends AbstractModule {

   private static final String PASTRY_LOCAL_PORT_PROPERTY = "pastry.localPort";

   @Override
   protected void configure() {
      // TODO Integrate with other properties
      Properties props = new Properties();
      try {
         props.load(new FileInputStream("../configs/testing/testingMDHT.properties"));
      } catch (FileNotFoundException e) {
         System.out.write(-1);
      } catch (IOException e) {
         System.out.write(-2);
      }

      Names.bindProperties(binder(), props);

      install(new DatamodelImplModule());
      install(new SecurityModule());
      bind(Properties.class).toInstance(props);
      bind(LogManager.class).to(NetInfPastryLogManager.class).in(Singleton.class);
      bind(StorageManager.class).to(StorageManagerImpl.class);
      bind(IdFactory.class).to(PastryIdFactory.class);
      bind(NodeIdFactory.class).to(RandomNodeIdFactory.class).in(Singleton.class);
      bind(PastNodePair.class).toProvider(PastNodePairProvider.class);
      bind(MDHTResolutionService.class);
      bind(NetInfNodeConnection.class).annotatedWith(SecurityModule.Security.class).to(RemoteNodeConnection.class);
      // expose(MDHTResolutionService.class);

      // expose(PastNodePair.class);

   }

   @Singleton
   @Provides
   PastryNodeFactory providePastryNodeFactory(NodeIdFactory idFactory, Environment env) throws IOException {
      int bindport = env.getParameters().getInt(PASTRY_LOCAL_PORT_PROPERTY);
      PastryNodeFactory factory = new SocketPastryNodeFactory(idFactory, bindport, env);
      return factory;

   }

   @Singleton
   @Provides
   PastryNode providePastryNode(PastryNodeFactory factory) throws IOException {
      return factory.newNode();
   }

   @Provides
   StorageManagerImpl provideStorageManager(IdFactory idf, Storage storage, Cache cache) {
      return new StorageManagerImpl(idf, storage, cache);
   }

   @Provides
   PastryIdFactory providePastryIdFactory(Environment env) {
      return new PastryIdFactory(env);
   }

   @Provides
   Storage provideStorage(IdFactory idf) {
      return new MemoryStorage(idf);
   }

   @Provides
   Cache provideCache(Storage storage, Environment env) {
      return new LRUCache(storage, 4 * 1024 * 1024, env);
   }

   @Singleton
   @Provides
   Environment provideEnvironment(Properties properties, LogManager logManager) throws UnknownHostException {
      Environment env = new Environment(null, null, null, null, logManager, new NetInfPastryParameters(properties), null);
      return env;
   }

   @Provides
   RandomNodeIdFactory provideRandomNodeIdFactory(Environment env) {
      return new RandomNodeIdFactory(env);
   }

}
