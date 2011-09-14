package netinf.node.module;

import netinf.access.HTTPServer;
import netinf.access.NetInfServer;
import netinf.access.TCPServer;
import netinf.common.communication.MessageEncoder;
import netinf.common.communication.MessageEncoderXML;
import netinf.common.datamodel.rdf.module.DatamodelRdfModule;
import netinf.common.datamodel.translation.module.DatamodelTranslationModule;
import netinf.common.utils.Utils;
import netinf.node.access.AccessServer;
import netinf.node.access.rest.RESTAccessServer;
import netinf.node.access.rest.module.RESTModule;
import netinf.node.cache.BOCacheServer;
import netinf.node.cache.CachingInterceptor;
import netinf.node.cache.network.NetworkCache;
import netinf.node.cache.peerside.PeersideCache;
import netinf.node.resolution.ResolutionInterceptor;
import netinf.node.resolution.ResolutionService;
import netinf.node.resolution.mdht.MDHTResolutionService;
import netinf.node.resolution.mdht.module.MDHTResolutionModule;
import netinf.node.resolution.remote.RemoteResolutionFactory;
import netinf.node.search.SearchService;
import netinf.node.search.rdf.SearchServiceRDF;
import netinf.node.search.rdf.module.SearchServiceRDFModule;
import netinf.node.transfer.TransferService;

import com.google.inject.Provides;
import com.google.inject.Singleton;

/**
 * This is a node module with the MDHT resolution service activated.
 * By that we reach a maximum possible encapsulation.
 * 
 * @author PG NetInf 3, University of Paderborn
 */
public class MDHTRSNodeModule extends AbstractNodeModule {
	 public static final String NODE_PROPERTIES = "../configs/netinfnode_standard.properties";

	   public MDHTRSNodeModule() {
	      super(Utils.loadProperties(NODE_PROPERTIES));
	   }

	   @Override
	   protected void configure() {
	      super.configure();
	      bind(MessageEncoder.class).to(MessageEncoderXML.class).in(Singleton.class);

	      // RDF data model
	      install(new DatamodelRdfModule());
	      install(new DatamodelTranslationModule());

	      // ResolutionServices
	      install(new MDHTResolutionModule());

	      // SearchServices
	      install(new SearchServiceRDFModule());

	      // RESTful API
	      install(new RESTModule());
	   }

	   /**
	    * This method provides all the {@link ResolutionService}s which are automatically inserted into the node. In order to get an
	    * instance of the according {@link ResolutionService}, add an additional parameter to this method, since this puts GUICE in
	    * charge of creating the correct instance of the according service.
	    * 
	    * @param localResolutionService
	    * @param rdfResolutionService
	    * @return
	    */
	   @Singleton
	   @Provides
	   ResolutionService[] provideResolutionServices(RemoteResolutionFactory remoteResolutionFactory,
	         MDHTResolutionService mdhtResolutionService) {
	      ResolutionService[] otherRS = { mdhtResolutionService };
	      //ResolutionService[] remoteRS = remoteResolutionFactory.getRemoteResolutionServices().toArray(new ResolutionService[] {});
	      //return (ResolutionService[]) ArrayUtils.addAll(remoteRS, otherRS);
	      return otherRS;
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
	   ResolutionInterceptor[] provideResolutionInterceptors(CachingInterceptor caching) {
	      return new ResolutionInterceptor[] { caching };
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

	   @Singleton
	   @Provides
	   AccessServer[] provideAccessServers(RESTAccessServer rest) {
	      return new AccessServer[] { rest };
	   }

	   @Singleton
	   @Provides
	   BOCacheServer[] provideBOCaches(NetworkCache nw, PeersideCache ps) {
	      return new BOCacheServer[] { ps, nw };
	   }
}
