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
package netinf.node.resolution.pastry.module;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Properties;

import netinf.node.resolution.pastry.NetInfPastryParameters;
import netinf.node.resolution.pastry.PastNodePair;
import netinf.node.resolution.pastry.PastryResolutionService;
import netinf.node.resolution.pastry.logging.NetInfPastryLogManager;
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
import rice.persistence.PersistentStorage;
import rice.persistence.Storage;
import rice.persistence.StorageManager;
import rice.persistence.StorageManagerImpl;

import com.google.inject.PrivateModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

/**
 * The Class PastryModule.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class PastryModule extends PrivateModule {

   private static final String PASTRY_LOCAL_PORT_PROPERTY = "pastry.localPort";

   private final Properties properties;

   public PastryModule(Properties properties) {
      this.properties = properties;
   }

   @Override
   protected void configure() {
      bind(LogManager.class).to(NetInfPastryLogManager.class).in(Singleton.class);
      bind(StorageManager.class).to(StorageManagerImpl.class);
      bind(IdFactory.class).to(PastryIdFactory.class);
      bind(NodeIdFactory.class).to(RandomNodeIdFactory.class).in(Singleton.class);
      bind(PastNodePair.class).toProvider(PastNodePairProvider.class);
      bind(PastryResolutionService.class);
      expose(PastryResolutionService.class);
      expose(PastNodePair.class);
   }

   @Singleton
   @Provides
   PastryNodeFactory providePastryNodeFactory(NodeIdFactory idFactory, Environment env) throws IOException {
      int bindport = env.getParameters().getInt(PASTRY_LOCAL_PORT_PROPERTY);
      return new SocketPastryNodeFactory(idFactory, bindport, env);
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
   MemoryStorage provideMemoryStorage(IdFactory idf) {
      return new MemoryStorage(idf);
   }

   @Provides
   Storage provideStorage(IdFactory idf, Environment env) throws IOException {
      return new PersistentStorage(idf, "node", "data", -1, env);
   }


   @Provides
   Cache provideCache(MemoryStorage storage, Environment env) {
      return new LRUCache(storage, 4 * 1024 * 1024, env);
   }

   @Singleton
   @Provides
   Environment provideEnvironment(LogManager logManager) throws UnknownHostException {
      return new Environment(null, null, null, null, logManager, new NetInfPastryParameters(properties), null);
   }

   @Provides
   RandomNodeIdFactory provideRandomNodeIdFactory(Environment env) {
      return new RandomNodeIdFactory(env);
   }

}
