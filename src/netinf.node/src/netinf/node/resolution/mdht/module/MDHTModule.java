package netinf.node.resolution.mdht.module;

import java.util.Properties;

import netinf.common.datamodel.rdf.module.DatamodelRdfModule;
import netinf.node.resolution.mdht.MDHTResolutionService;

import com.google.inject.PrivateModule;

/**
 * @author PG NetInf 3
 */
public class MDHTModule extends PrivateModule {

   private static final String PASTRY_LOCAL_PORT_PROPERTY = "pastry.localPort";

   private final Properties properties;

   public MDHTModule(Properties properties) {
      this.properties = properties;
   }

   @Override
   protected void configure() {
      bind(MDHTResolutionService.class);
      expose(MDHTResolutionService.class);
   }
}