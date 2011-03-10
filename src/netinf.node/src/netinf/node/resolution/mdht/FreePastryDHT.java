/**
 * 
 */
package netinf.node.resolution.mdht;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import netinf.common.datamodel.Identifier;
import netinf.common.exceptions.NetInfUncheckedException;

import org.apache.log4j.Logger;

import rice.environment.Environment;
import rice.p2p.commonapi.Application;
import rice.p2p.commonapi.Endpoint;
import rice.p2p.commonapi.Message;
import rice.p2p.commonapi.RouteMessage;
import rice.p2p.past.PastContent;
import rice.pastry.Id;
import rice.pastry.NodeHandle;
import rice.pastry.NodeIdFactory;
import rice.pastry.PastryNode;
import rice.pastry.commonapi.PastryIdFactory;
import rice.pastry.routing.RouteSet;
import rice.pastry.routing.RoutingTable;
import rice.pastry.socket.SocketNodeHandle;
import rice.pastry.socket.appsocket.AppSocketPastryNodeFactory;
import rice.pastry.standard.RandomNodeIdFactory;

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
   public FreePastryDHT(final int instanceID, InetAddress bootstrapAddr, int port) {
      InetSocketAddress bootstrapAddress = new InetSocketAddress(bootstrapAddr, port);
      bindport = port;
      Environment env = new Environment();
      // disable the UPnP setting (in case you are testing this on a NATted LAN)
      env.getParameters().setString("nat_search_policy", "never");

      // used for generating PastContent object Ids.
      // this implements the "hash function" for our DHT
      localFactory = new rice.pastry.commonapi.PastryIdFactory(env);
      NodeIdFactory nidFactory = new RandomNodeIdFactory(env);
      // construct the PastryNodeFactory

      try {
         factory = new AppSocketPastryNodeFactory(nidFactory, bindport, env);
      } catch (IOException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      pastryNode = factory.newNode();
      endpoint = pastryNode.buildEndpoint(this, "PastryInstance" + String.valueOf(instanceID));
      endpoint.register();
      joinRing(bootstrapAddress);
   }

   /**
    * Constructor used for creating a new ring and then joining it.
    * 
    * @param instanceID
    *           - can be randomly generated and is only relevant when running more than one node on
    * @param port
    *           - the port used for this node (on which it listens to). Used at the same time to identify rings/levels the same
    *           JVM
    */
   public FreePastryDHT(final int instanceID, int port) {
      bindport = port;
      Environment env = new Environment();
      // disable the UPnP setting (in case you are testing this on a NATted LAN)
      env.getParameters().setString("nat_search_policy", "never");
      // Manually set the bootup address
      // env.getParameters().setInetSocketAddress("pastry.bootupaddress", );
      // used for generating PastContent object Ids.
      // this implements the "hash function" for our DHT
      localFactory = new rice.pastry.commonapi.PastryIdFactory(env);
      NodeIdFactory nidFactory = new RandomNodeIdFactory(env);
      // construct the PastryNodeFactory

      try {
         factory = new AppSocketPastryNodeFactory(nidFactory, bindport, env);
         // get the SocketFactory
         // SocketFactory sFactory = factory.getSocketFactory();
      } catch (IOException e) {
         // TODO Auto-generated catch block
         LOG.error("Could not read from socket, AppSocketPastryNodeFactory could not be initialized", e);
      }
      pastryNode = factory.newNode();
      endpoint = pastryNode.buildEndpoint(this, "PastryInstance" + String.valueOf(instanceID));
      /*
       * endpoint.accept(new AppSocketReceiver() { /** When we accept a new socket.
       */
      /*
       * public void receiveSocket(AppSocket socket) { LOG.debug("The socket:" + socket.toString()); // it's critical to call this
       * to be able to accept multiple times endpoint.accept(this); }
       * @Override public void receiveException(AppSocket arg0, Exception arg1) { // TODO Auto-generated method stub }
       * @Override public void receiveSelectResult(AppSocket arg0, boolean arg1, boolean arg2) throws IOException { // TODO
       * Auto-generated method stub } });
       */

      endpoint.register();

      joinRing(null);
   }

   /*
    * (non-Javadoc)
    * @see netinf.node.resolution.mdht.DHT#getResponsibleNode(netinf.common.datamodel.Identifier) Method returns a String of the
    * form HOSTNAME/IP:PORT, if a corresponding node is found, null otherwise
    */
   @Override
   public InetSocketAddress getResponsibleNode(Identifier id) {
      // build the past content
      final PastContent myContent = new DummyPastContent(localFactory.buildId(id.toString()), id.toString());

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
      RouteSet rs = rt.getBestEntry((Id) myContent.getId());
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
   public void joinRing(InetSocketAddress bootstrapAddress) {
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
   public void leaveRing() {
      // TODO Auto-generated method stub
   }

   @Override
   public void deliver(rice.p2p.commonapi.Id arg0, Message arg1) {
      // TODO Auto-generated method stub
   }

   @Override
   public boolean forward(RouteMessage arg0) {
      // TODO Auto-generated method stub
      return false;
   }

   @Override
   public void update(rice.p2p.commonapi.NodeHandle arg0, boolean arg1) {
      // TODO Auto-generated method stub
   }

}
