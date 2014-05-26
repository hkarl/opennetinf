/*
 * Copyright (C) 2009-2011 University of Paderborn, Computer Networks Group
 * (Full list of owners see http://www.netinf.org/about-2/license)
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * 
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *       this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of Paderborn nor the names of its contributors may be used to endorse
 *       or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDERS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package netinf.node.resolution.mdht.module;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import netinf.common.utils.Utils;
import netinf.node.resolution.mdht.MDHTResolutionService;
import netinf.node.resolution.mdht.dht.DHTConfiguration;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;

/**
 * This module is designed to instantiate an MDHT resolution service to be used with other NetInf modules. It will attempt to read
 * its configuration from files in the /configs/mdht subfolder. If not found it will just use the default.properties file i.e.
 * create a 3-Level MDHT and be a seed node.
 * 
 * @author PG NetInf 3, University of Paderborn
 * @since 2011
 */
public class MDHTResolutionModule extends AbstractModule {
   
   private Properties properties;
   
   public MDHTResolutionModule(String pathToProperties) {
      properties = Utils.loadProperties(pathToProperties);
   }

   @Override
   protected void configure() {
      bind(MDHTResolutionService.class);
   }

   @Provides
   List<DHTConfiguration> provideDHTConfigurations(@Named("mdht.numberoflevels") int mdhtlevels) {
      List<DHTConfiguration> configs = new ArrayList<DHTConfiguration>();
      for (int i = 0; i < mdhtlevels; i++) {
         String bootHost = properties.getProperty("mdht." + i + ".boothost");
         String listenAddress = properties.getProperty("mdht." + i + ".listenaddress");
         int bootPort = Integer.parseInt(properties.getProperty("mdht." + i + ".bootport"));
         int listenPort = Integer.parseInt(properties.getProperty("mdht." + i + ".listenport"));
         DHTConfiguration config = new DHTConfiguration(bootHost, bootPort, listenPort, i, listenAddress);
         configs.add(config);
      }
      return configs;
   }
}
