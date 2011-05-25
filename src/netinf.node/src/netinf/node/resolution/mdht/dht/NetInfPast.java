package netinf.node.resolution.mdht.dht;


import java.util.logging.*;

import netinf.node.resolution.mdht.dht.pastry.FreePastryDHT;
import netinf.node.resolution.mdht.dht.pastry.NetInfDHTMessage;
import netinf.node.resolution.mdht.dht.pastry.NetInfLookupMessage;
import rice.p2p.commonapi.*;
import rice.p2p.past.*;
import rice.*;
import rice.Continuation.*;
import rice.p2p.past.messaging.*;
import rice.p2p.past.rawserialization.SocketStrategy;
import rice.pastry.commonapi.PastryEndpoint;
import rice.pastry.commonapi.PastryEndpointMessage;
import rice.persistence.Cache;
import rice.persistence.StorageManager;

public class NetInfPast extends PastImpl {

	private Application application;
	public NetInfPast(Node node, StorageManager manager, int replicas,
			String instance, Application app) {
		super(node, manager, replicas, instance);
		this.application = app;
	}

	public NetInfPast(Node node, StorageManager manager, int replicas,
			String instance, PastPolicy policy) {
		super(node, manager, replicas, instance, policy);
		// TODO Auto-generated constructor stub
	}

	public NetInfPast(Node node, StorageManager manager, Cache backup,
			int replicas, String instance, PastPolicy policy,
			StorageManager trash) {
		super(node, manager, backup, replicas, instance, policy, trash);
		// TODO Auto-generated constructor stub
	}

	public NetInfPast(Node node, StorageManager manager, Cache backup,
			int replicas, String instance, PastPolicy policy,
			StorageManager trash, boolean useOwnSocket) {
		super(node, manager, backup, replicas, instance, policy, trash,
				useOwnSocket);
		// TODO Auto-generated constructor stub
	}

	public NetInfPast(Node node, StorageManager manager, Cache backup,
			int replicas, String instance, PastPolicy policy,
			StorageManager trash, SocketStrategy strategy) {
		super(node, manager, backup, replicas, instance, policy, trash,
				strategy);
		// TODO Auto-generated constructor stub
	}
	
	public void deliver(Id id, Message message) {
		final PastMessage msg = (PastMessage) message;
		if (msg.isResponse() == false && msg instanceof NetInfLookupMessage) {		
				//Get the level
			NetInfLookupMessage niMsg = (NetInfLookupMessage)msg;
			int level = niMsg.getLevel();
				if(this.application instanceof FreePastryDHT) {
					FreePastryDHT parent = (FreePastryDHT) application;
					parent.NotifyParent(id, level);
				}
			 
			}
		super.deliver(id, message);
		}
	@SuppressWarnings("unchecked")
	public void lookup(final Id id, final int level, final boolean cache, final Continuation command) {
	    logger.log(" Performing lookup on " + id.toStringFull());
	    
	    storage.getObject(id, new StandardContinuation(command) {
	      public void receiveResult(Object o) {
	        if (o != null) {
	          command.receiveResult(o);
	        } else {
	          // send the request across the wire, and see if the result is null or not
	          sendRequest(id, new NetInfLookupMessage(getUID(), id, getLocalNodeHandle(), id, level), new NamedContinuation("NetInf LookupMessage for " + id, this) {
	            public void receiveResult(final Object o) {
	              // if we have an object, we return it
	              // otherwise, we must check all replicas in order to make sure that
	              // the object doesn't exist anywhere
	              if (o != null) {
	                // lastly, try and cache object locally for future use
	                if (cache) {
	                  cache((PastContent) o, new SimpleContinuation()  {
	                    public void receiveResult(Object object) {
	                      command.receiveResult(o);
	                    }
	                  });
	                } else {
	                  command.receiveResult(o);                            
	                }
	              } else {
	                lookupHandles(id, replicationFactor+1, new Continuation() {
	                  public void receiveResult(Object o) {
	                    PastContentHandle[] handles = (PastContentHandle[]) o;

	                    for (int i=0; i<handles.length; i++) {
	                      if (handles[i] != null) {
	                        fetch(handles[i], new StandardContinuation(parent) {
	                          public void receiveResult(final Object o) {
	                            // lastly, try and cache object locally for future use
	                            if (cache) {
	                              cache((PastContent) o, new SimpleContinuation()  {
	                                public void receiveResult(Object object) {
	                                  command.receiveResult(o);
	                                }
	                              });
	                            } else {
	                              command.receiveResult(o);                            
	                            }
	                          }
	                        });
	                        
	                        return;
	                      }
	                    }

	                    // there were no replicas of the object
	                    command.receiveResult(null);
	                  }
	                  
	                  public void receiveException(Exception e) {
	                    command.receiveException(e);
	                  }
	                });
	              }
	            }
	            
	            public void receiveException(Exception e) {
	              // If the lookup message failed , we then try to fetch all of the handles, just
	              // in case.  This may fail too, but at least we tried.
	              receiveResult(null);
	            }
	          });
	        }
	      }
	    });
	  }
}
