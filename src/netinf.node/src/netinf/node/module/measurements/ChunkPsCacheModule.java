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
package netinf.node.module.measurements;

import netinf.access.HTTPServer;
import netinf.access.NetInfServer;
import netinf.access.TCPServer;
import netinf.common.datamodel.rdf.module.DatamodelRdfModule;
import netinf.common.datamodel.translation.module.DatamodelTranslationModule;
import netinf.common.utils.Utils;
import netinf.node.access.AccessServer;
import netinf.node.access.rest.module.RESTModule;
import netinf.node.cache.CachingInterceptor;
import netinf.node.cache.peerside.PeersideCacheModule;
import netinf.node.module.AbstractNodeModule;
import netinf.node.resolution.ResolutionInterceptor;
import netinf.node.resolution.ResolutionService;
import netinf.node.resolution.remote.RemoteResolutionFactory;
import netinf.node.search.SearchService;

import com.google.inject.Provides;
import com.google.inject.Singleton;

/**
 * @author PG Netinf 3, University of Paderborn
 */
public class ChunkPsCacheModule extends AbstractNodeModule {

   public static final String NODE_PROPERTIES = "../configs/veclientconfigs/psonly/chunkpscache.properties";

   public ChunkPsCacheModule() {
      super(Utils.loadProperties(NODE_PROPERTIES));
   }

   @Override
   protected void configure() {
      super.configure();

      // The Datamodel
      install(new DatamodelRdfModule());
      install(new DatamodelTranslationModule());

      // Need a binding to AccessServer
      install(new RESTModule());

      // Peerside Cache
      install(new PeersideCacheModule(NODE_PROPERTIES));
   }

   @Singleton
   @Provides
   ResolutionService[] provideResolutionServices(RemoteResolutionFactory remoteResolutionFactory) {
      return remoteResolutionFactory.getRemoteResolutionServices().toArray(new ResolutionService[] {});
   }

   @Singleton
   @Provides
   ResolutionInterceptor[] provideResolutionInterceptors(CachingInterceptor cache) {
      return new ResolutionInterceptor[] { cache };
   }

   @Singleton
   @Provides
   SearchService[] provideSearchServices() {
      return new SearchService[] {};
   }

   @Singleton
   @Provides
   NetInfServer[] providesAccess(TCPServer tcpServer, HTTPServer httpServer) {
      return new NetInfServer[] { tcpServer, httpServer };
   }

   @Singleton
   @Provides
   AccessServer[] providesAccessServer(AccessServer accServ) {
      return new AccessServer[] { accServ };
   }
}
