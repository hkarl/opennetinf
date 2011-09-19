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
import netinf.node.cache.BOCacheServer;
import netinf.node.cache.CachingInterceptor;
import netinf.node.cache.network.NetworkCache;
import netinf.node.module.AbstractNodeModule;
import netinf.node.resolution.ResolutionInterceptor;
import netinf.node.resolution.ResolutionService;
import netinf.node.resolution.locator.impl.LocatorSelectorImpl;
import netinf.node.resolution.remote.RemoteResolutionFactory;
import netinf.node.search.SearchService;
import netinf.node.search.rdf.SearchServiceRDF;
import netinf.node.search.rdf.module.SearchServiceRDFModule;

import com.google.inject.Provides;
import com.google.inject.Singleton;

/**
 * @author PG Netinf 3, University of Paderborn
 */
public class NoChunkNetCacheModule extends AbstractNodeModule {

   public static final String NODE_PROPERTIES = "../configs/veclientconfigs/nconly/nochunknetcache.properties";

   public NoChunkNetCacheModule() {
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

      // The SearchServices
      install(new SearchServiceRDFModule());

      // Peer Caching
      // install(new LocalIOCachingModule());

      // install(new NetworkCacheModule());

      // bind(StorageService.class).to(MDHTResolutionService.class).in(Singleton.class);
   }

   @Singleton
   @Provides
   ResolutionService[] provideResolutionServices(RemoteResolutionFactory remoteResolutionFactory) {
      return remoteResolutionFactory.getRemoteResolutionServices().toArray(new ResolutionService[] {});
   }

   /**
    * This method provides all the {@link ResolutionInterceptor}s which are automatically inserted into the node. In order to get
    * an instance of the according {@link ResolutionInterceptor}, add an additional parameter to this method, since this puts
    * GUICE in charge of creating the correct instance of the according service. The {@link ResolutionInterceptor}s will be called
    * in the given order.
    * 
    * @param localResolutionService
    * @param rdfResolutionService
    * @return
    */
   @Singleton
   @Provides
   ResolutionInterceptor[] provideResolutionInterceptors(CachingInterceptor cache /* IOCacheImpl ioCache */,
         LocatorSelectorImpl locatorSelector) {
      return new ResolutionInterceptor[] { cache, locatorSelector };
   }

   /**
    * This method provides all the {@link SearchService}s which are automatically inserted into the node. In order to get an
    * instance of the according {@link SearchService}, add an additional parameter to this method, since this puts GUICE in charge
    * of creating the correct instance of the according service.
    * 
    * @param searchServiceRdf
    * @return
    */
   @Singleton
   @Provides
   SearchService[] provideSearchServices(SearchServiceRDF searchServiceRdf) {
      return new SearchService[] { searchServiceRdf };
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

   /***** Uncomment below to provide Peerside and NetworkCaches ****/
   @Singleton
   @Provides
   BOCacheServer[] provideBOCaches(NetworkCache nw /* ,PeersideCache ps */) {
      return new BOCacheServer[] { /* ps, */nw };
   }

}
