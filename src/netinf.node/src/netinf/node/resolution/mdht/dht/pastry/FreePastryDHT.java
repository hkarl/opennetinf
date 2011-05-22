package netinf.node.resolution.mdht.dht.pastry;

import java.io.IOException;
import java.net.InetSocketAddress;

import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.InformationObject;
import netinf.node.resolution.mdht.MDHTResolutionService;
import netinf.node.resolution.mdht.dht.DHT;
import netinf.node.resolution.mdht.dht.DHTConfiguration;
import netinf.node.resolution.mdht.dht.NetInfPast;

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
import rice.pastry.NodeIdFactory;
import rice.pastry.PastryNode;
import rice.pastry.PastryNodeFactory;
import rice.pastry.commonapi.PastryEndpointMessage;
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
public class FreePastryDHT implements DHT, Application{

   private static final Logger LOG = Logger.getLogger(FreePastryDHT.class);
   private PastryNode pastryNode;
   private Environment environment;
   private PastryIdFactory pastryIdFactory;
   private NetInfPast past;
   private InetSocketAddress bootAddress;
   private MDHTResolutionService parent;
   /**
   	   * The Endpoint represents the underlying node.  By making calls on the
   	   * Endpoint, it assures that the message will be delivered to a MyApp on whichever
   	   * node the message is intended for.
   	   */
   private Endpoint endpoint;
   

   public FreePastryDHT(int listenPort, String bootHost, int bootPort, String pastName, MDHTResolutionService pParent) throws IOException {
	   // Set the reference to the parent MDHT
	  this.parent = pParent;
	  
	  // PastryNode setup
      environment = new Environment();
      NodeIdFactory nodeIdFactory = new RandomNodeIdFactory(environment);
      PastryNodeFactory pastryNodeFactory = new SocketPastryNodeFactory(nodeIdFactory, listenPort, environment);
      pastryNode = pastryNodeFactory.newNode();
      // Past setup
      pastryIdFactory = new PastryIdFactory(environment);
      Storage storage = new MemoryStorage(pastryIdFactory);
      StorageManager storageManager = new StorageManagerImpl(pastryIdFactory, storage, new EmptyCache(pastryIdFactory));
      
      
      // boot address
      bootAddress = new InetSocketAddress(bootHost, bootPort);
      
      // We are only going to use one instance of this application on each PastryNode
      this.endpoint = pastryNode.buildEndpoint(this, "NetInfMDHTNode");      	   
      this.endpoint.register();
      
      past = new NetInfPast(pastryNode, storageManager, 0, pastName, this);

   }
   
   public FreePastryDHT(DHTConfiguration config, MDHTResolutionService pParent) throws IOException {
      this(config.getListenPort(), config.getBootHost(), config.getBootPort(), "Level-" + config.getLevel(),pParent);
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

   public InformationObject get(Identifier id, int level) {
      ExternalContinuation<PastContent, Exception> lookupCont = new ExternalContinuation<PastContent, Exception>();
      InformationObject retIO = null;
      Id lookupId = pastryIdFactory.buildId(id.toString());
      past.lookup(lookupId, level, false, lookupCont);
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
    //Not found, instruct parent to look in next ring
      retIO = parent.get(lookupId, level+1);
      return retIO;
   }
   
   //Version of the get function for looking up p2p.commonapi.Ids directly
   public InformationObject get(Id id, int level) {
	      ExternalContinuation<PastContent, Exception> lookupCont = new ExternalContinuation<PastContent, Exception>();
	      InformationObject retIO = null;
	      past.lookup(id, level, false, lookupCont);
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
	      //Not found, instruct parent to look in next ring
	      retIO = parent.get(id, level+1);
	      return retIO;
	   }

   public void NotifyParent(Id id, int level)
   {
	   InformationObject retIO = null;
	   LOG.info("Parent to be notified. Level is " + level);
	 //Not found, instruct parent to look in next ring
	 //retIO = parent.get(id, level+1);
	 
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
	} else if (msg instanceof PastryEndpointMessage) {
		LOG.info("(FreePastryDHT) Received Endpoint message " + msg + " on node with id " + id);
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
