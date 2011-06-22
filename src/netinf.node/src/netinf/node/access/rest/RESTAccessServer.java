package netinf.node.access.rest;

import netinf.common.datamodel.DatamodelFactory;
import netinf.node.access.AccessServer;
import netinf.node.api.impl.LocalNodeConnection;

import org.apache.log4j.Logger;
import org.restlet.Application;
import org.restlet.Component;
import org.restlet.data.Protocol;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * A RESTAccessServer is a AccessServer providing a RESTful interface to a NetInf node.
 * 
 * @author PG NetInf 3, University of Paderborn
 *
 */
public class RESTAccessServer implements AccessServer {

   private static final Logger LOG = Logger.getLogger(RESTAccessServer.class);
   
   private Component component;

   @Inject
   public RESTAccessServer(@Named("node.access.rest.port") int port, LocalNodeConnection connection, DatamodelFactory factory) {
      component = new Component();
      component.getServers().add(Protocol.HTTP, port);
      
      Application application = new RESTApplication(connection, factory);

      component.getDefaultHost().attach(application);
   }

   /**
    * Starts the RESTAccessServer.
    */
   @Override
   public void start() {
      try {
         component.start();
      } catch (Exception e) {
         LOG.error("Could not start RESTAccessServer", e);
      }
   }

   /**
    * Stops the RESTAccessServer.
    */
   @Override
   public void stop() {
      try {
         component.stop();
      } catch (Exception e) {
         LOG.error("Could not stop RESTAccessServer", e);
      }
   }

}
