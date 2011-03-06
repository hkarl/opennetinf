/**
 * 
 */
package netinf.node.resolution.mdht;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import org.apache.log4j.Logger;
import org.mpisws.p2p.transport.TransportLayer;

import rice.environment.Environment;
import rice.p2p.commonapi.Application;
import rice.p2p.commonapi.Endpoint;
import rice.p2p.commonapi.Message;
import rice.p2p.commonapi.RouteMessage;
import rice.p2p.commonapi.appsocket.AppSocket;
import rice.p2p.commonapi.appsocket.AppSocketReceiver;
import rice.p2p.past.PastContent;
import rice.pastry.Id;
import rice.pastry.NodeHandle;
import rice.pastry.NodeIdFactory;
import rice.pastry.PastryNode;
import rice.pastry.commonapi.PastryIdFactory;
import rice.pastry.routing.RouteSet;
import rice.pastry.routing.RoutingTable;
import rice.pastry.socket.appsocket.AppSocketPastryNodeFactory;
import rice.pastry.standard.RandomNodeIdFactory;

import netinf.common.datamodel.Identifier;
import netinf.common.exceptions.NetInfUncheckedException;
import netinf.node.resolution.pastry.PastryResolutionService;

/**
 * @author PG NetInf 3
 */
public class FreePastryDHT implements DHT, Application {
	
	private static final Logger LOG = Logger.getLogger(PastryResolutionService.class);
	private PastryNode pastryNode;
	private int bindport;
	protected Endpoint endpoint;
	protected AppSocketPastryNodeFactory factory;
	protected PastryIdFactory localFactory;
	
	public FreePastryDHT(int instanceID)
	{
		Environment env = new Environment();
		// disable the UPnP setting (in case you are testing this on a NATted LAN)
	      env.getParameters().setString("nat_search_policy","never");
		//Manually set the bootup address
		//env.getParameters().setInetSocketAddress("pastry.bootupaddress", );
		// used for generating PastContent object Ids.
	    //this implements the "hash function" for our DHT
	    localFactory = new rice.pastry.commonapi.PastryIdFactory(env);	    
	    NodeIdFactory nidFactory = new RandomNodeIdFactory(env);
		// construct the PastryNodeFactory
	    
		try {
			factory = new AppSocketPastryNodeFactory(nidFactory, bindport, env);				      		   
		   // get the SocketFactory
		      //SocketFactory sFactory = factory.getSocketFactory();		      
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.pastryNode = factory.newNode();
		this.endpoint = pastryNode.buildEndpoint(this, "PastryInstance"+ String.valueOf(instanceID));
		endpoint.accept(new AppSocketReceiver() {
		      /**
		       * When we accept a new socket.
		       */
		      public void receiveSocket(AppSocket socket) {
		       LOG.debug("The socket:" + socket.toString());
		    	
		        // it's critical to call this to be able to accept multiple times
		        endpoint.accept(this);
		      }

			@Override
			public void receiveException(AppSocket arg0, Exception arg1) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void receiveSelectResult(AppSocket arg0, boolean arg1,
					boolean arg2) throws IOException {
				// TODO Auto-generated method stub
				
			}
		});
		    
		this.endpoint.register();
		InetSocketAddress bootstrapAddr = this.getBootstrapAddress();
		this.joinRing(null);
	}

   @Override
   
   public String getResponsibleNode(Identifier id) {	      	      
	// 	build the past content
	      final PastContent myContent = new DummyPastContent(localFactory.buildId(id.toString()), id.toString());
	      
	    RoutingTable rt = pastryNode.getRoutingTable();
	    //Variable nh will be null if node not found
	    NodeHandle nh = pastryNode.getLocalHandle();//rt.get(pastryNode.getNodeId()/*(Id) myContent.getId()*/);
	    if(nh != null)
	    {
	    	nh.ping();
	    }
	    RouteSet rs = rt.getBestEntry((Id) myContent.getId());
	    
	    PastryNode pn = nh.getLocalNode();
	    
	    
	    try {
			
			if(this.factory != null){
				@SuppressWarnings("unused")
				TransportLayer<InetSocketAddress, ByteBuffer> res = this.factory.getBottomLayers(pn, null);
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	      return null;
   }

   @Override
   public void joinRing(InetSocketAddress bootstrapAddress) {
	   	  LOG.debug("Starting Pastry node of resolution Service");
	      
	      pastryNode.boot(bootstrapAddress);
	      waitForStartupOfNode();
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
	               throw new NetInfUncheckedException("Could not join the Pastry ring.  Reason:" + pastryNode.joinFailedReason());
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
private InetSocketAddress getBootstrapAddress() {
    InetSocketAddress bootstrapAddress = null;
    
       bootstrapAddress = new InetSocketAddress("10.9.8.7", 11111);
    if(bootstrapAddress.isUnresolved()){
       LOG.warn("Could not resolve bootup address. Will initiate new Ring ");
    }
    return bootstrapAddress;
 }
}