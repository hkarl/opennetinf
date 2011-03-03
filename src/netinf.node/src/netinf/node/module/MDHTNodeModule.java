package netinf.node.module;

import netinf.access.NetInfServer;
import netinf.access.TCPServer;
import netinf.common.datamodel.impl.module.DatamodelImplModule;
import netinf.common.datamodel.translation.module.DatamodelTranslationModule;
import netinf.common.utils.Utils;
import netinf.node.resolution.ResolutionInterceptor;
import netinf.node.resolution.ResolutionService;
import netinf.node.resolution.locator.impl.LocatorSelectorImpl;
import netinf.node.resolution.mdht.MDHTResolutionService;
import netinf.node.resolution.mdht.module.MDHTModule;
import netinf.node.search.SearchService;

import com.google.inject.Provides;
import com.google.inject.Singleton;

/**
 * @author PG Netinf 3, University of Paderborn
 */
public class MDHTNodeModule extends AbstractNodeModule {

   public static final String NODE_PROPERTIES = "../configs/netinfnode_standard.properties";

   public MDHTNodeModule() {
      super(Utils.loadProperties(NODE_PROPERTIES));
   }

   @Override
   protected void configure() {
      super.configure();

      // The datamodel
      install(new DatamodelImplModule());
      install(new DatamodelTranslationModule());

      // The ResolutionServices
      install(new MDHTModule(getProperties()));
   }

   @Singleton
   @Provides
   ResolutionService[] provideResolutionServices(MDHTResolutionService pastryResolutionService) {
      return new ResolutionService[] { pastryResolutionService };
   }

   @Singleton
   @Provides
   ResolutionInterceptor[] provideResolutionInterceptors(LocatorSelectorImpl locatorSelector) {
      return new ResolutionInterceptor[] { locatorSelector };
   }

   @Singleton
   @Provides
   SearchService[] provideSearchServices() {
      return new SearchService[0];
   }

   @Singleton
   @Provides
   NetInfServer[] providesAccess(TCPServer tcpServer) {
      return new NetInfServer[] { tcpServer };
   }
}
