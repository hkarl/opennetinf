package netinf.node.resolution.mdht.module;

import java.util.ArrayList;
import java.util.List;

import netinf.node.resolution.mdht.MDHTResolutionService;
import netinf.node.resolution.mdht.dht.DHTConfiguration;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

/**
 * @author PG NetInf 3
 */
public class MDHTResolutionModule extends AbstractModule {

   @Override
   protected void configure() {
      bind(MDHTResolutionService.class);
   }
   
   @Provides
   List<DHTConfiguration> provideDHTConfigurations() {
      List<DHTConfiguration> configs = new ArrayList<DHTConfiguration>();
      // Level 0
      DHTConfiguration config0 = new DHTConfiguration("agk-lpc7.cs.uni-paderborn.de", 7000, 7000, 0);
      configs.add(config0);
      return configs;
   }
}