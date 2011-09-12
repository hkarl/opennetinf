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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import netinf.node.resolution.mdht.MDHTResolutionService;
import netinf.node.resolution.mdht.dht.DHTConfiguration;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

/**
 * This module is designed to instantiate an MDHT resolution service to be used with other NetInf
 * modules. It will attempt to read its configuration from files in the /configs/mdht subfolder. If
 * not found it will just use the default.properties file i.e. create a 3-Level MDHT and be a seed node.
 * @author PG NetInf 3, University of Paderborn
 * @since 2011
 */
public class MDHTResolutionModule extends AbstractModule {

   @Override
   protected void configure() {
      bind(MDHTResolutionService.class);
   }

   @Provides
   List<DHTConfiguration> provideDHTConfigurations() {
      List<DHTConfiguration> configs = new ArrayList<DHTConfiguration>();

      String configFileName;
      try {
         /*** Each MDHT node determines its own configuration file to use ***/
         configFileName = getConfigFileForHostname(); // This never really throws SocketException

         Properties configFile = new Properties();

         if (!fileExists(configFileName)) {
            configFileName = "../configs/mdht/default.properties"; // default
         }
         FileInputStream in;

         in = new FileInputStream(configFileName);

         configFile.load(in);

         int noOfLevels = Integer.parseInt(configFile.getProperty("mdht.numberoflevels"));
         for (int i = 0; i < noOfLevels; i++) {
            String bootHost = configFile.getProperty("mdht." + i + ".boothost"); // Use localhost to start own node
            int bootPort = Integer.parseInt(configFile.getProperty("mdht." + i + ".bootport"));
            int listenPort = Integer.parseInt(configFile.getProperty("mdht." + i + ".listenport"));
            DHTConfiguration config = new DHTConfiguration(bootHost, bootPort, listenPort, i);
            configs.add(config);
         }
         in.close();
      } catch (IOException e) {
         return null;
      }
      return configs;
   }

   /**
    * Gets the hostname associated with a local/LAN address.
    * @return The name of the configuration file which corresponds to this local node.
    * @throws SocketException If the network interfaces cannot be enumerated.
    */
   private String getConfigFileForHostname() throws SocketException {
      String configFileName = "../configs/mdht/default.properties"; // default
      // Retrieve all the network interfaces for the local host
      for (final Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces(); interfaces.hasMoreElements();) {
         final NetworkInterface cur = interfaces.nextElement();

         // Not interested in loopback interfaces
         if (cur.isLoopback()) {
            continue;
         }

         for (final InterfaceAddress addr : cur.getInterfaceAddresses()) {
            final InetAddress inetAddr = addr.getAddress();

            if (!(inetAddr instanceof Inet4Address)) {
               continue;
            }
            configFileName = "../configs/mdht/" + inetAddr.getHostName() + ".properties";
         }
      }
      return configFileName;
   }

   /**
    * Checks if a specific file exists.
    * @param fileName The file name to check.
    * @return {@code true} if the file exists, otherwise {@code false}
    */
   private boolean fileExists(String fileName) {
      File f = new File(fileName);
      return f.exists();
   }
}
