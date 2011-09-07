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
