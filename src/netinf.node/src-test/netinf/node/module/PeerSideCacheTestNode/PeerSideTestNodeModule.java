package netinf.node.module.PeerSideCacheTestNode;

import netinf.access.HTTPServer;
import netinf.access.NetInfServer;
import netinf.access.TCPServer;
import netinf.common.datamodel.impl.module.DatamodelImplModule;
import netinf.common.datamodel.translation.module.DatamodelTranslationModule;
import netinf.common.utils.Utils;
import netinf.node.access.AccessServer;
import netinf.node.access.rest.module.RESTModule;
import netinf.node.cache.network.module.NetworkCacheModule;
import netinf.node.cache.peerside.impl.PeerSideCachingInterceptor;
import netinf.node.cache.peerside.module.PeerSideCacheModule;
import netinf.node.module.AbstractNodeModule;
import netinf.node.resolution.ResolutionInterceptor;
import netinf.node.resolution.ResolutionService;
import netinf.node.resolution.iocaching.impl.IOCacheImpl;
import netinf.node.resolution.iocaching.module.LocalIOCachingModule;
import netinf.node.resolution.locator.impl.LocatorSelectorImpl;
import netinf.node.resolution.mdht.MDHTResolutionService;
import netinf.node.resolution.mdht.module.MDHTResolutionModule;
import netinf.node.search.SearchService;
import netinf.node.search.rdf.SearchServiceRDF;
import netinf.node.search.rdf.module.SearchServiceRDFModule;

import com.google.inject.Provides;
import com.google.inject.Singleton;

/**
 * @author PG Netinf 3, University of Paderborn
 */
public class PeerSideTestNodeModule extends AbstractNodeModule {

   public static final String NODE_PROPERTIES = "../configs/IntegrationTestingStarterConfigs/PeerSideTestNode.properties";

   public PeerSideTestNodeModule() {
      super(Utils.loadProperties(NODE_PROPERTIES));
   }

   @Override
   protected void configure() {
      super.configure();

      // The Datamodel
      install(new DatamodelImplModule());
      install(new DatamodelTranslationModule());

      // The ResolutionServices - binds the config and DHT instances
      install(new MDHTResolutionModule());
      
      //Need a binding to AccessServer
      install (new RESTModule());
      
      // The SearchServices
      install(new SearchServiceRDFModule());

      // Peer Caching
      install(new PeerSideCacheModule());
      //install(new LocalIOCachingModule());
      
      //install(new NetworkCacheModule());
      
      //bind(StorageService.class).to(MDHTResolutionService.class).in(Singleton.class);
   }

   @Singleton
   @Provides
   ResolutionService[] provideResolutionServices(MDHTResolutionService mdhtResolutionService) {
      return new ResolutionService[] { mdhtResolutionService };
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
   ResolutionInterceptor[] provideResolutionInterceptors(PeerSideCachingInterceptor psCache /*IOCacheImpl ioCache*/, LocatorSelectorImpl locatorSelector) {
      return new ResolutionInterceptor[] { /*psCache, */locatorSelector };
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
   
   
}
