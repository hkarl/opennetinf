package netinf.node.resolution.mdht.module;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

      String configFileName;
      try {
         /*** Each MDHT node determines its own configuration file to use ***/
         configFileName = getLocalHostname(); // This never really throws SocketException

         Properties configFile = new Properties();

         if (fileExists(configFileName) == false) {
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

   private String getLocalHostname() throws SocketException {
      String configFileName = "../configs/mdht/default.properties"; // default
      for (final Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces(); interfaces.hasMoreElements();) {
         final NetworkInterface cur = interfaces.nextElement();

         if (cur.isLoopback()) {
            continue;
         }

         for (final InterfaceAddress addr : cur.getInterfaceAddresses()) {
            final InetAddress inet_addr = addr.getAddress();

            if (!(inet_addr instanceof Inet4Address)) {
               continue;
            }
            configFileName = "../configs/mdht/" + inet_addr.getHostName() + ".properties";
         }
      }
      return configFileName;
   }

   private boolean fileExists(String fileName) {
      File f = new File(fileName);
      return f.exists();
   }
}