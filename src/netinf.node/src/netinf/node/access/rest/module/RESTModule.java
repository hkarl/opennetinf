package netinf.node.access.rest.module;

import netinf.node.access.AccessServer;
import netinf.node.access.rest.RESTAccessServer;

import com.google.inject.AbstractModule;

/**
 * Module to be installed to access a NetInf node via a RESTful interface.
 * 
 * @author PG NetInf 3, University of Paderborn
 */
public class RESTModule extends AbstractModule {

   @Override
   protected void configure() {
      bind(AccessServer.class).to(RESTAccessServer.class);
      // bindConstant().annotatedWith(Names.named("node.access.rest.port")).to(8081);
   }

}
