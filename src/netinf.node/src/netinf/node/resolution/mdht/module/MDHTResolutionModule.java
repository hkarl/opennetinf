package netinf.node.resolution.mdht.module;

import netinf.node.resolution.mdht.MDHTResolutionService;

import com.google.inject.PrivateModule;

/**
 * @author PG NetInf 3
 */
public class MDHTResolutionModule extends PrivateModule {

   @Override
   protected void configure() {
      bind(MDHTResolutionService.class);
      expose(MDHTResolutionService.class);
   }
}