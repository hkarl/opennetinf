/**
 * 
 */
package netinf.node.resolution.mdht;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import netinf.common.datamodel.InformationObject;
import netinf.common.exceptions.NetInfUncheckedException;

import org.apache.log4j.Logger;

import rice.Continuation;
import rice.environment.Environment;
import rice.p2p.commonapi.Application;
import rice.p2p.commonapi.Endpoint;
import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.Message;
import rice.p2p.commonapi.RouteMessage;
import rice.p2p.past.Past;
import rice.p2p.past.PastContent;
import rice.p2p.past.PastContentHandle;
import rice.p2p.past.PastImpl;

import rice.pastry.NodeHandle;
import rice.pastry.NodeIdFactory;
import rice.pastry.PastryNode;
import rice.pastry.commonapi.PastryIdFactory;
import rice.pastry.routing.RouteSet;
import rice.pastry.routing.RoutingTable;
import rice.pastry.socket.SocketNodeHandle;
import rice.pastry.socket.appsocket.AppSocketPastryNodeFactory;
import rice.pastry.standard.RandomNodeIdFactory;
import rice.persistence.Cache;
import rice.persistence.LRUCache;
import rice.persistence.MemoryStorage;
import rice.persistence.Storage;
import rice.persistence.PersistentStorage;
import rice.persistence.StorageManagerImpl;

/**
 * @author PG NetInf 3
 */
public class FreePastryDHT implements DHT, Application {

   private static final Logger LOG = Logger.getLogger(FreePastryDHT.class);
   private PastryNode pastryNode;
   private int bindport;
   private Endpoint endpoint;
   private AppSocketPastryNodeFactory factory;
   private PastryIdFactory localFactory;
   private Storage stor;
   private Past app;
   private Environment env;
   private MDHTResolutionService parent;
   public volatile static InformationObject returned;
   private PastContentHandle contentHandle;
   

   /**
    * More detailed constructor for joining an existing ring
    * 
    * @param instanceID
    *           - can be randomly generated and is only relevant when running more than one node on same JVM
    * @param bootstrapAddr
    *           - the IP address of the node to be used as a bootstrap node to join the DHT
    * @param port
    *           - the port to listen on locally (should be > 1024, use lower values at your own risk)
    */
   public FreePastryDHT(final int instanceID, InetAddress bootstrapAddr, int port, MDHTResolutionService parent) {
      InetSocketAddress bootstrapAddress = bootstrapAddr == null ? null : new InetSocketAddress(bootstrapAddr, port);
      bindport = port;
      env = new Environment();      

      this.parent = parent;
      // disable the UPnP setting (in case you are testing this on a NATted LAN)
      env.getParameters().setString("nat_search_policy", "never");

      
      // used for generating PastContent object Ids.
      // this implements the "hash function" for our DHT
      localFactory = new rice.pastry.commonapi.PastryIdFactory(env);
      NodeIdFactory nidFactory = new RandomNodeIdFactory(env);
      // construct the PastryNodeFactory

      InetAddress localIP = null;
      if(parent.listenOnThisIP != null && parent.listenOnThisIP.isEmpty() == false)
      {
	try {
	 localIP = InetAddress.getByName(parent.listenOnThisIP);
        } catch (UnknownHostException e) {
	   // TODO Auto-generated catch block
	    e.printStackTrace();
        } 
      }
      try {
         factory = new AppSocketPastryNodeFactory(nidFactory, localIP, bindport, env);
      } catch (IOException e) {
         LOG.error("AppSocketPastryNodeFactory could not be started. " +
                   "This means that the node cannot be created. Please check the port settings and try again.");
      }
      pastryNode = factory.newNode();
   // create a different storage root for each node
      String storageDirectory = "./storage"+pastryNode.getId().hashCode();

   // create the persistent part
      try {
	 stor = new PersistentStorage(localFactory, storageDirectory, 4 * 1024 * 1024, pastryNode.getEnvironment());
      } catch (IOException e) {
	 LOG.error("Could not create persistent storage with root at: " + storageDirectory);
      }
      app = new PastImpl(pastryNode, new StorageManagerImpl(localFactory, this.stor, new LRUCache(
	    new MemoryStorage(localFactory), 512 * 1024, pastryNode.getEnvironment())), 3, "");	
      endpoint = pastryNode.buildEndpoint(this, "PastryInstance" + String.valueOf(instanceID));
      endpoint.register();
      join(bootstrapAddress);
   }


   /*
    * (non-Javadoc)
    * @see netinf.node.resolution.mdht.DHT#getResponsibleNode(netinf.common.datamodel.Identifier) Method returns a String of the
    * form HOSTNAME/IP:PORT, if a corresponding node is found, null otherwise
    */
   @Override
   public InetSocketAddress getResponsibleNode(InformationObject io) {
      // build the past content
      final PastContent myContent = new DummyPastContent(localFactory.buildId(io.toString()), io);

      final RoutingTable rt = pastryNode.getRoutingTable();
      NodeHandle nh2;
      InetSocketAddress skt = null;
      // Variable nh will be null if node not found
      NodeHandle nh = pastryNode.getLocalHandle(); // rt.get(pastryNode.getNodeId()/*(Id) myContent.getId()*/);
      if (nh != null) {
         nh.ping();
      }

      // Verify if there are better candidate nodes for storage, other than the current node
      // If return value is null then use local node
      RouteSet rs = rt.getBestEntry((rice.pastry.Id) myContent.getId());
      if (rs != null) {
         nh2 = rs.closestNode();
         if (nh2 instanceof SocketNodeHandle) {
            skt = ((SocketNodeHandle) nh2).getInetSocketAddress();
         }
      } else {
         if (nh instanceof SocketNodeHandle) {
            skt = ((SocketNodeHandle) pastryNode.getLocalHandle()).getInetSocketAddress();
         }
      }

      return skt;
   }

   @Override
   public void join(InetSocketAddress bootstrapAddress) {
      LOG.debug("Starting Pastry node of resolution Service");

      pastryNode.boot(bootstrapAddress);
      // int attempts = 0;
      try {
         waitForStartupOfNode();
      } catch (NetInfUncheckedException nEx) {
         /*
          * if (attempts < 3) { attempts++; }
          */
         pastryNode.boot(null);
         waitForStartupOfNode();
      }

      LOG.info("Finished starting pastry node" + pastryNode);
   }

   public String get(PastContentHandle contentKey )
   {
   // let's do the "get" operation
     	    LOG.info("Looking up the IO stored with key " + contentKey);
       	    
       	   this.contentHandle = contentKey;
       	   
       	 NetInfDHTMessage msg = new NetInfDHTMessage(endpoint.getLocalNodeHandle().getId(), null , null);
       	 msg.isGetRequest = true;
       	 msg.requester = endpoint.getLocalNodeHandle();
         this.routeNetInfDHTMessage(msg);

         return null;
   }
   
   public PastContentHandle put(InformationObject o){
      

      // build the past content
      final PastContent myContent = new DummyPastContent(localFactory.buildId(o.serializeToBytes()), o);

      System.out.println("Inserting " + myContent + " at node "+app.getLocalNodeHandle());
      
     //this.getResponsibleNode(o);
      NetInfDHTMessage msg = new NetInfDHTMessage(endpoint.getLocalNodeHandle().getId(), null , myContent);
      this.routeNetInfDHTMessage(msg);

      return myContent.getHandle(app);
      
   }
   private void waitForStartupOfNode() {
      synchronized (pastryNode) {
         while (!(pastryNode.isReady() || pastryNode.joinFailed())) {
            try {
               pastryNode.wait(500);
            } catch (InterruptedException e) {
               throw new NetInfUncheckedException("Pastry thread is interrupted", e);
            }
            if (pastryNode.joinFailed()) {
               // throw new NetInfUncheckedException("Could not join the Pastry ring.  Reason:" +
               // pastryNode.joinFailedReason());
               LOG.info("Could not join the Pastry ring.  Reason:" + pastryNode.joinFailedReason());
               LOG.info("Creating new ring");
            }
         }
      }

   }

   @Override
   public void leave() {
      this.pastryNode.destroy();
   }

   
   @SuppressWarnings({ "unchecked", "rawtypes" })
   @Override
   /**
    * Method called when receiving a message
    */
   public void deliver(rice.p2p.commonapi.Id id, Message msgReceived) {
     LOG.info("DHT-SUBSYSTEM: Received the messsage " + msgReceived + "with id " + id);
     if(msgReceived != null && msgReceived instanceof NetInfDHTMessage)
     {
    	 
	LOG.info("Got NetInfDHTMessage. Storing IO in PAST on this node.");
	 final NetInfDHTMessage msgUnpacked = (NetInfDHTMessage)msgReceived;
     if(false == msgUnpacked.isGetRequest)
     {
	 //This needs to be final to be used inside the Continuation
	 final DummyPastContent myContent = msgUnpacked.getContent();
         
	// insert the data
         app.insert(myContent, new Continuation() {
         //the result is an Array of Booleans for each insert
           public void receiveResult(Object result) {          
             Boolean[] results = ((Boolean[]) result);
             int numSuccessfulStores = 0;
             for (int ctr = 0; ctr < results.length; ctr++) {
               if (results[ctr].booleanValue()) 
                 numSuccessfulStores++;
            }
              LOG.info(myContent + " successfully stored at " + 
                 numSuccessfulStores + " locations.");
         }

         @Override
         public void receiveException(Exception result) {
    	 Exception ex = (Exception)result;
	 LOG.error("Error storing " + myContent + ".Complete error message: " + ex.getMessage());
	 
         }
         });
     }
     else
     {
    	//This is a get request.
    	 if(msgUnpacked.requester.getId() == msgUnpacked.getTo())
    	 {
    		 //This is the original requestor node. Return IO, cache etc. here
    		 this.parent.cacheObtainedIO(msgUnpacked.getContent().getIO());
    	 }
    	 else{
    	 
    	 //This is the node responsible for the content. Return the answer
    	 LOG.info("Looking up the IO stored with key " + this.contentHandle);
     	   
    	    // for each stored key     
    	      final PastContentHandle lookupKey = this.contentHandle;
    	      
    	       	            	
    	      LOG.info("Looking up " + lookupKey + " at node " + app.getLocalNodeHandle());
    	 
    	    app.fetch(lookupKey, new Continuation<PastContent, Exception>() {
   	        public void receiveResult(PastContent result) {
     	          LOG.info("Successfully looked up " + result + " for key " + lookupKey.getId().toString() + ".");
     	          if(result instanceof DummyPastContent)
     	          {
     	             returned = ((DummyPastContent)result).getIO();
     	             NetInfDHTMessage replyMsg = new NetInfDHTMessage(endpoint.getLocalNodeHandle().getId(), msgUnpacked.requester.getId(), result);
     	             routeNetInfDHTMessage(replyMsg);
     	          }
     	          else
     	          {
     	             LOG.error("Retrieved object had invalid contents and was discarded.");
     	          }
     	        }
     	 
     	        public void receiveException(Exception result) {
     	          LOG.error("Error looking up " + lookupKey.getId().toString());
     	          result.printStackTrace();
     	        }
     	      });
    	 }
     
      }
     }
     
   }

   @Override
   /**
    * Method called each time when a message is received by the current node and is about to forward it.
    * Returning true will forward the message.
    */
   public boolean forward(RouteMessage msgToForward) {
      try {
	     if(msgToForward instanceof NetInfDHTMessage)
	     {
	         NetInfDHTMessage msg = (NetInfDHTMessage)msgToForward.getMessage(endpoint.getDeserializer());
	         msg.addHop(endpoint.getLocalNodeHandle());
	     }
	  } 
      catch (IOException ioe) 
      {
          LOG.error("Exception thrown: " + ioe.getMessage() + ". Could not get a deserializer for the NetInfDHTMessage, aborted operation."); 
      }
      //Always forward the message
      return true;
   }

   @Override
   public void update(rice.p2p.commonapi.NodeHandle arg0, boolean arg1) {
      // TODO Auto-generated method stub
   }


   /**
    * @return the returned
    */
   public InformationObject getReturnedIOFromPast() {
      return returned;
   }
   
   /**
    * Method used to route a message of type {@link NetInfDHTMessage} using the DHT-internal forwarding mechanism
    * @param destId The node Id of the message destination
    */
   public void routeNetInfDHTMessage(NetInfDHTMessage msg)
   {
      LOG.info("DHT-SUBSYSTEM: "+ this.pastryNode.getId() + "sending NetInfDHTMessage to " + msg.getTo());
      
      
      //Route using the DHT-internal mechanism with no hints (3rd argument null = no routing hints)
      this.endpoint.route(msg.getTo(), msg, null);
   }

   /**
    * Method used to route a message of type {@link NetInfDHTMessage} using a direct route.
    * @param destNodeHandle The NodeHandle object of the destination node
    */
   public void routeNetInfDHTMessageDirect(NodeHandle destNodeHandle, NetInfDHTMessage msg)
   {
      if(destNodeHandle == null)
      {
	 LOG.error("DHT-SUBSYSTEM: Cannot route to node with empty NodeHandle! Aborted routing operation.");
	 return;
      }
      LOG.info("DHT-SUBSYSTEM: "+ this.pastryNode.getId() + "sending NetInfDHTMessage directly to " + destNodeHandle.getId());
      
      
      //Route directly to the node given here as an argument (3rd argument is the routing hint)
      this.endpoint.route(null, msg, destNodeHandle);
   }
   

}
