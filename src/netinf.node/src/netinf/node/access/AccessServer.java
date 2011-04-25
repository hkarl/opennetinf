package netinf.node.access;

import netinf.node.access.rest.RESTAccessServer;

import com.google.inject.ImplementedBy;

/**
 * An AccessServer provides an interface to a NetInfNode.
 * 
 * @author mmuehe
 */
public interface AccessServer {

   /**
    * Starts the AccessServer
    */
   public void start();

   /**
    * Stops the AccessServer
    */
   public void stop();

}
