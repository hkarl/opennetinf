package netinf.node.resolution.mdht.module;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Properties;

import netinf.common.datamodel.rdf.module.DatamodelRdfModule;
import netinf.node.resolution.mdht.MDHTResolutionService;
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
import rice.persistence.PersistentStorage;
import rice.persistence.Storage;
import rice.persistence.StorageManager;
import rice.persistence.StorageManagerImpl;

import com.google.inject.PrivateModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

/**
 * @author PG NetInf 3
 */
public class MDHTModule extends PrivateModule {

   private static final String PASTRY_LOCAL_PORT_PROPERTY = "pastry.localPort";

   private final Properties properties;

   public MDHTModule(Properties properties) {
      this.properties = properties;
   }

   @Override
   protected void configure() {
      bind(LogManager.class).to(NetInfPastryLogManager.class).in(Singleton.class);
      bind(StorageManager.class).to(StorageManagerImpl.class);
      bind(IdFactory.class).to(PastryIdFactory.class);
      bind(NodeIdFactory.class).to(RandomNodeIdFactory.class).in(Singleton.class);
      bind(PastNodePair.class).toProvider(PastNodePairProvider.class);

      bind(MDHTResolutionService.class);
      expose(MDHTResolutionService.class);

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