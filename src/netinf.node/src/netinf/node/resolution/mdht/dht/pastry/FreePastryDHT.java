package netinf.node.resolution.mdht.dht.pastry;



import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.InformationObject;
import netinf.node.resolution.ResolutionService;
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
import rice.p2p.commonapi.RouteMessage;
import rice.p2p.past.PastContent;
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

public class FreePastryDHT implements DHT, Application {


   /**
    * The local log4j logger;
    */
   private static final Logger LOG = Logger.getLogger(FreePastryDHT.class);

   /**
    * Constant representing the maximum number of times the node
    * will attempt to join a ring.
    */
   private static final int MAX_JOIN_ATTEMPTS = 3;

   /**
    * Constant representing the number of milliseconds to wait
    * between attempts to join a ring.
    */
   private static final int TIME_BETWEEN_JOIN_ATTEMPTS = 500;

   /**
    *  Reference to the local PastryNode object
    */
   private PastryNode pastryNode;

   /**
    *  The PASTRY environment. This is where all the configuration
    *  settings are kept.
    */
   private Environment environment;

   /**
    * Used to generate random IDs for the PASTRY node.
    */
   private PastryIdFactory pastryIdFactory;

   /**
    * The NetInfPast application layer on top of FreePastry.
    */
   private NetInfPast past;

   /**
    * The network address to use as a bootstrap.
    */
   private InetSocketAddress bootAddress;

   /**
    * Reference to the parent resolution service.
    */
   private MDHTResolutionService parent;

   /**
    * The Endpoint represents the underlying node.  By making calls
    * on the Endpoint, it assures that the message will be delivered
    * to a MyApp on whichever node the message is intended for.
    * */
   private Endpoint endpoint;

   /**
    * Constructor - only used internally
    * @param listenPort The port to listen on for this DHT
    * @param bootHost The bootstrap node (hostname)
    * @param bootPort The port on which to contact the boot host
    * @param pastName The name of the PAST application
    * @param pParent  The MDHT Resolution Service parent
    * @throws IOException Thrown when unable to resolve hosts
    */
   public FreePastryDHT(final int listenPort, final String bootHost,
		   final int bootPort,
		   final String pastName,
		   final MDHTResolutionService pParent)

   throws IOException {

	   // Set the reference to the parent MDHT

	  this.parent = pParent;

	  // PastryNode setup

      environment = new Environment();

      environment.getParameters().setString("logging_enable", "true");

      environment.getParameters().setString("loglevel", "WARNING");

      if (bootHost == "localhost") {

    	  environment.getParameters().setString("rice_socket_seed", "true");

      }

      NodeIdFactory nodeIdFactory = new RandomNodeIdFactory(environment);

      PastryNodeFactory pastryNodeFactory =
    	  new SocketPastryNodeFactory(nodeIdFactory, listenPort, environment);

      pastryNode = pastryNodeFactory.newNode();

      // Past setup

      pastryIdFactory = new PastryIdFactory(environment);

      Storage storage = new MemoryStorage(pastryIdFactory);

      StorageManager storageManager = new StorageManagerImpl(pastryIdFactory,
    		  storage, new EmptyCache(pastryIdFactory));


      // boot address

      bootAddress = new InetSocketAddress(bootHost, bootPort);

      // We are only going to use one instance of this application on each PastryNode
      this.endpoint = pastryNode.buildEndpoint(this, "NetInfMDHTNode");
      this.endpoint.register();

      past = new NetInfPast(pastryNode, storageManager, 0, pastName, this);
   }

   public FreePastryDHT(DHTConfiguration config, MDHTResolutionService pParent) throws IOException {
		  this(config.getListenPort(), config.getBootHost(),
			config.getBootPort(), "Level-" + config.getLevel(), pParent);
   }


   @Override
   public void join() throws InterruptedException {
      LOG.debug("Starting Pastry node of resolution Service");
      int noOfAttempts = 0;

      pastryNode.boot(bootAddress);

      synchronized (pastryNode) {
         while (!pastryNode.isReady() || pastryNode.joinFailed()) {
            pastryNode.wait(TIME_BETWEEN_JOIN_ATTEMPTS);
            if (pastryNode.joinFailed() && noOfAttempts < MAX_JOIN_ATTEMPTS) {
             LOG.warn("(FreePastryDHT) Could not join the Pastry ring.  Reason:"
            		   + pastryNode.joinFailedReason());
             noOfAttempts++;
            } else
            	if (pastryNode.joinFailed()
            	 && noOfAttempts >= MAX_JOIN_ATTEMPTS) {
             	LOG.error("(FreePastryDHT) Reached the max number of join attempts. Join failed.");
            	break;
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
      retIO = parent.get(lookupId, level + 1);
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
	      retIO = parent.get(id, level + 1);
	      return retIO;
	   }



   /***
    * Method used to notify parent to jump one ring above for the GET request.
    * @param id The Id of the stored PastContent
    * @param level The level we are currently on
    */
   public final void notifyParent(final Id id, final int level) {
	   /*InformationObject retIO = */parent.get(id, level + 1);
	   LOG.info("Parent to be notified. Level is " + level);
	   parent.switchRingUpwards(level);
	 //Not found, instruct parent to look in next ring
	 //retIO = parent.get(id, level+1);
   }


   /**
    * Send a notification to the parent - a so-called ACK.
    * This needs to be perfected.
    * @see MDHTResolutionService
    * @param id The Identifier
    * @param target The address of the node to send ACK to
    */
   public final void notifyParentAck(final Id id,
		                final InetAddress target) {
	   parent.sendRemoteAck(target);
   }



   @Override
	public final void put(InformationObject io, int level, int maxlevels,
			byte[] sourceAddr) {

		MDHTPastContent content = new MDHTPastContent(
	        pastryIdFactory.buildId(io.getIdentifier().toString()), io);

		ExternalContinuation<Boolean[], Exception> insertCont =
			new ExternalContinuation<Boolean[], Exception>();

		InetAddress inetAddr;
		try {

			// Convert IP Address from string to InetAddress
			inetAddr = InetAddress.getByAddress(sourceAddr);

			// Create NetInfInsertMessage
			past.insert(content, insertCont, level, maxlevels, inetAddr);
			insertCont.sleep();

			if (insertCont.exceptionThrown()) {
				Exception exception = insertCont.getException();
				LOG.error(exception.getMessage());
			} else {
				Boolean[] result = (Boolean[]) insertCont.getResult();
				LOG.info("(FreePastryDHT) " + result.length
						+ " objects have been inserted at node ");

			}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	if (msg instanceof NetInfDHTMessage) {
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

