package netinf.node.cache.peerside;

import net.sf.ehcache.Cache;
import netinf.node.access.AccessServer;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * The PeersideAccessServer allows to access the elements that are stored in the PeersideCache via an HTTP interface.
 * 
 * @author PG NetInf 3, University of Paderborn.
 */
public class PeersideAccessServer implements AccessServer {

   private static final Logger LOG = Logger.getLogger(PeersideAccessServer.class);
   private Server server;

   /**
    * Constructor
    * 
    * @param cache
    *           The adapted cache.
    * @param port
    *           The port of the cache.
    */
   public PeersideAccessServer(Cache cache, int port) {
      server = new Server(port);
      ServletContextHandler context = new ServletContextHandler();
      context.addServlet(new ServletHolder(new PeersideServlet(cache)), "/*");
      server.setHandler(context);
   }

   @Override
   public void start() {
      try {
         server.start();
      } catch (Exception e) {
         LOG.error("Could not start PeersideAccessServer", e);
      }
   }

   @Override
   public void stop() {
      try {
         server.stop();
      } catch (Exception e) {
         LOG.error("Could not stop PeersideAccessServer", e);
      }
   }

}