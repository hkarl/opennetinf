package netinf.node.resolution.mdht.dht.pastry;

import java.io.IOException;
import java.net.InetSocketAddress;

import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.InformationObject;
import netinf.node.resolution.mdht.dht.DHT;
import netinf.node.resolution.mdht.dht.DHTConfiguration;

import org.apache.log4j.Logger;

import rice.Continuation.ExternalContinuation;
import rice.environment.Environment;
import rice.p2p.commonapi.Application;
import rice.p2p.commonapi.Endpoint;
import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.Message;
import rice.p2p.commonapi.NodeHandle;
import rice.p2p.commonapi.NodeHandleSet;
import rice.p2p.commonapi.RouteMessage;
import rice.p2p.past.Past;
import rice.p2p.past.PastContent;
import rice.p2p.past.PastImpl;
import rice.p2p.past.messaging.InsertMessage;
import rice.pastry.NodeIdFactory;
import rice.pastry.PastryNode;
import rice.pastry.PastryNodeFactory;
import rice.pastry.commonapi.PastryIdFactory;
import rice.pastry.socket.SocketPastryNodeFactory;
import rice.pastry.standard.RandomNodeIdFactory;
import rice.persistence.EmptyCache;
import rice.persistence.MemoryStorage;
import rice.persistence.Storage;
import rice.persistence.StorageManager;
import rice.persistence.StorageManagerImpl;

/**
 * @author PG NetInf 3
 */
public class FreePastryDHT implements DHT, Application {

   private static final Logger LOG = Logger.getLogger(FreePastryDHT.class);
   private PastryNode pastryNode;
   private Environment environment;
   private PastryIdFactory pastryIdFactory;
   private Past past;
   private InetSocketAddress bootAddress;
   /**
   	   * The Endpoint represents the underlieing node.  By making calls on the
   	   * Endpoint, it assures that the message will be delivered to a MyApp on whichever
   	   * node the message is intended for.
   	   */
   private Endpoint endpoint;

   public FreePastryDHT(int listenPort, String bootHost, int bootPort, String pastName) throws IOException {
      // PastryNode setup
      environment = new Environment();
      NodeIdFactory nodeIdFactory = new RandomNodeIdFactory(environment);
      PastryNodeFactory pastryNodeFactory = new SocketPastryNodeFactory(nodeIdFactory, listenPort, environment);
      pastryNode = pastryNodeFactory.newNode();
      // Past setup
      pastryIdFactory = new PastryIdFactory(environment);
      Storage storage = new MemoryStorage(pastryIdFactory);
      StorageManager storageManager = new StorageManagerImpl(pastryIdFactory, storage, new EmptyCache(pastryIdFactory));
      past = new PastImpl(pastryNode, storageManager, 0, pastName);
      // boot address
      bootAddress = new InetSocketAddress(bootHost, bootPort);
      
      // We are only going to use one instance of this application on each PastryNode
      this.endpoint = pastryNode.buildEndpoint(this, "NetInfMDHTNode");      	   
      this.endpoint.register();

   }
   
   public FreePastryDHT(DHTConfiguration config) throws IOException {
      this(config.getListenPort(), config.getBootHost(), config.getBootPort(), "Level-"+config.getLevel());
   }

   @Override
   public void join() throws InterruptedException {
      LOG.debug("Starting Pastry node of resolution Service");

      pastryNode.boot(bootAddress);
      synchronized (pastryNode) {
         while (!(pastryNode.isReady() || pastryNode.joinFailed())) {
            pastryNode.wait(500);
            if (pastryNode.joinFailed()) {
               LOG.info("(FreePastryDHT) Could not join the Pastry ring.  Reason:" + pastryNode.joinFailedReason());
            }
         }
      }

      LOG.info("(FreePastryDHT) Finished starting pastry node" + pastryNode);
   }

   public InformationObject get(Identifier id) {
      ExternalContinuation<PastContent, Exception> lookupCont = new ExternalContinuation<PastContent, Exception>();
      past.lookup(pastryIdFactory.buildId(id.toString()), lookupCont);
      lookupCont.sleep();
      if (lookupCont.exceptionThrown()) {
         Exception ex = lookupCont.getException();
         LOG.error(ex.getMessage());
      } else {
         MDHTPastContent result = (MDHTPastContent) lookupCont.getResult();
         if (result != null) {
            return result.getInformationObject();
         }
      }
      return null;
   }

   @Override
   public void put(InformationObject io) {
      // build the past content
      // PastContent content = new DummyPastContent(pastryIdFactory.buildId(io.getIdentifier().toString()), (Serializable) io);
      MDHTPastContent content = new MDHTPastContent(pastryIdFactory.buildId(io.getIdentifier().toString()), io);

      ExternalContinuation<Boolean[], Exception> insertCont = new ExternalContinuation<Boolean[], Exception>();
      past.insert(content, insertCont);
      insertCont.sleep();

      if (insertCont.exceptionThrown()) {
         Exception exception = insertCont.getException();
         LOG.error(exception.getMessage());
      } else {
         Boolean[] result = (Boolean[]) insertCont.getResult();
         LOG.info("(FreePastryDHT) " + result.length + " objects have been inserted at node ");
         NodeHandleSet set = endpoint.replicaSet(content.getId(), 1);
         if (set.size() > 0) {
        	 LOG.info("(FreePastryDHT) Replica set contains " + set.size() + " copies: " + set);
        	 LOG.info("(FreePastryDHT) Sending ACK messages to all nodes in replica set.");
        	 for(int i = 0; i < set.size(); i++)
        	 {
        		 NodeHandle nh = set.getHandle(i);
        		 this.routeMyMsgDirect(nh);
        	 }
         } else {
        	 LOG.error("(FreePastryDHT) Replica set contains no copies!");
         }
      }
      
   }

   @Override
   public void leave() {
      pastryNode.destroy();
      environment.destroy();
   }
   
   public void routeMyMsgDirect(NodeHandle nh) {
	   LOG.info("(FreePastryDHT) Sending direct ACK to node " + nh);   
	   Message msg = new NetInfDHTMessage(endpoint.getLocalNodeHandle(), nh.getId());
	   endpoint.route(null, msg, nh);
   }

@Override
public void deliver(Id id, Message msg) {
	
	if(msg instanceof NetInfDHTMessage) {
		LOG.info("(FreePastryDHT) Received ACK message " + msg + " on node with id " + id);
	} else {
		LOG.info("(FreePastryDHT) Received generic message " + msg + " on node with id " + id);
	}
}

@Override
public boolean forward(RouteMessage arg0) {
	// Always forward messages
	return true;
}

@Override
public void update(NodeHandle arg0, boolean arg1) {
	// TODO Auto-generated method stub
	
}

}
