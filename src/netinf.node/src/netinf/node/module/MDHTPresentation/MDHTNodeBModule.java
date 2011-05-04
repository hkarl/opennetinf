package netinf.node.module.MDHTPresentation;

import netinf.access.HTTPServer;
import netinf.access.NetInfServer;
import netinf.access.TCPServer;
import netinf.common.datamodel.impl.module.DatamodelImplModule;
import netinf.common.datamodel.translation.module.DatamodelTranslationModule;
import netinf.common.utils.Utils;
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
import netinf.node.transfer.TransferService;

import com.google.inject.Provides;
import com.google.inject.Singleton;

/**
 * @author PG Netinf 3, University of Paderborn
 */
public class MDHTNodeBModule extends AbstractNodeModule {

   public static final String NODE_PROPERTIES = "../configs/MDHT-Presentation/NodeB.properties";

   public MDHTNodeBModule() {
      super(Utils.loadProperties(NODE_PROPERTIES));
   }

   @Override
   protected void configure() {
      super.configure();

      // The Datamodel
      install(new DatamodelImplModule());
      install(new DatamodelTranslationModule());

      // The ResolutionServices
      install(new MDHTResolutionModule());

      // The SearchServices
      install(new SearchServiceRDFModule());

      // Caching Storage
      install(new LocalIOCachingModule());
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
   ResolutionInterceptor[] provideResolutionInterceptors(IOCacheImpl ioCache, LocatorSelectorImpl locatorSelector) {
      return new ResolutionInterceptor[] { ioCache, locatorSelector };
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

   /**
    * This method provides all the {@link TransferService}s which are automatically inserted into the node. In order to get an
    * instance of the according {@link TransferService}, add an additional parameter to this method, since this puts GUICE in
    * charge of creating the correct instance of the according service.
    * 
    * @param searchServiceRdf
    * @return
    */
   @Singleton
   @Provides
   TransferService[] provideTransferServices() {
      return new TransferService[] {};
   }

   @Singleton
   @Provides
   NetInfServer[] providesAccess(TCPServer tcpServer, HTTPServer httpServer) {
      return new NetInfServer[] { tcpServer, httpServer };
   }
}
