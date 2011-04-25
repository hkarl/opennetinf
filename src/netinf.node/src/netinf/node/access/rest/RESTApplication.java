package netinf.node.access.rest;

import netinf.node.access.rest.resources.BOResource;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;

/**
 * Application providing a router for resources.
 * 
 * @author mmuehe
 *
 */
public class RESTApplication extends Application {

   public RESTApplication(Context context) {
      setContext(context);
   }
   
   @Override
   public Restlet createInboundRoot() {
      Router router = new Router(getContext());

      router.attach("/{hash}/{label}", BOResource.class);
      
      return router;
   }

}
