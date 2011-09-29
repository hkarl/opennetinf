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
package netinf.node.cache.peerside;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import netinf.common.utils.Utils;

import org.apache.log4j.Logger;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;

/**
 * Module for the PeersideCaches
 * 
 * @author PG NetInf 3, University of Paderborn.
 */
public class PeersideCacheModule extends AbstractModule {

   private static final Logger LOG = Logger.getLogger(PeersideCacheModule.class);

   private Properties properties;

   public PeersideCacheModule(String pathToProperties) {
      properties = Utils.loadProperties(pathToProperties);
   }

   @Override
   protected void configure() {

   }

   @Provides
   List<PeersideCache> provideNetworkCaches(@Named("peerside.cache.numberOfCaches") int numberOfcaches) {
      List<PeersideCache> peerCaches = new ArrayList<PeersideCache>();
      for (int i = 0; i < numberOfcaches; i++) {
         String host = properties.getProperty("peerside.cache." + i + ".access.host");
         if (host.equals("localhost") || host.equals("127.0.0.1")) {
            try {
               host = InetAddress.getLocalHost().getHostName();
               LOG.info("(PeersideCache ) Using hostname '" + host + "' instead of 'localhost'");
            } catch (UnknownHostException e) {
               LOG.warn("(PeersideCache ) Could not determine hostname alternative to 'localhost'");
            }
         }
         String port = properties.getProperty("peerside.cache." + i + ".access.port");
         String scope = properties.getProperty("peerside.cache." + i + ".scope");
         PeersideCache peerCache = new PeersideCache(Integer.parseInt(scope), host, Integer.parseInt(port));

         peerCaches.add(peerCache);
      }
      return peerCaches;
   }

}