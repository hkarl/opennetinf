package netinf.node.access;


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
