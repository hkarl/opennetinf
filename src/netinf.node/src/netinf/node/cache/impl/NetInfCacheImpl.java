/**
 * 
 */
package netinf.node.cache.impl;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import netinf.common.datamodel.DataObject;
import netinf.common.datamodel.attribute.Attribute;
import netinf.common.datamodel.attribute.DefinedAttributeIdentification;
import netinf.node.cache.NetInfCache;

import org.apache.log4j.Logger;

/**
 * Implementations of the NetInfCache interface
 * 
 * @author PG NetInf 3, University of Paderborn
 */
public class NetInfCacheImpl implements NetInfCache {

   private static final Logger LOG = Logger.getLogger(NetInfCacheImpl.class); // Logger
   private CacheServer cacheServer; // adapter for the used cache server

   /**
    * Constructor
    */
   NetInfCacheImpl() {
      try {
         cacheServer = new EhCacheServerImpl(InetAddress.getLocalHost());
      } catch (UnknownHostException e) {
         LOG.error("Could not create EhCacheServer");
      }
   }

   @Override
   public void storeBObyIO(DataObject io) {
      String hashOfBO = getHash(io);

      if (hashOfBO != null) {
         if (!contains(hashOfBO)) {
            // TODO:
            // 1. download file
            // 2. check hash
            // 3. put to caching
            LOG.info("DO stored");
         } else {
            LOG.info("DO already in cache");
         }
      } else {
         LOG.info("Hash is null, will not be cached");
      }
   }

   @Override
   public boolean contains(String hashOfBO) {
      if (isConnected()) {
         return cacheServer.containsBO(hashOfBO);
      }
      return false;
   }

   @Override
   public boolean isConnected() {
      if (cacheServer != null && cacheServer.isConnected()) {
         return true;
      }
      return false;
   }

   // private byte[] loadBO(String path) throws IOException {
   // File file = new File(path);
   // try {
   // InputStream stream = new FileInputStream(file);
   //
   // } catch (FileNotFoundException e) {
   // // TODO Auto-generated catch block
   // e.printStackTrace();
   // }
   // return null;
   // }

   /**
    * Gets the hash-value of a DataObject
    * 
    * @param dataO
    *           the DataObject
    * @return hash-value of the DO
    */
   private String getHash(DataObject dataO) {
      List<Attribute> attributes = dataO.getAttribute(DefinedAttributeIdentification.HASH_OF_DATA.getURI());
      if (!attributes.isEmpty()) {
         String hash = attributes.get(0).getValue(String.class);
         return hash;
      }
      return null;
   }

   // public static byte[] getBytesFromFile(File file) throws IOException {
   // InputStream is = new FileInputStream(file);
   //
   // // Get the size of the file
   // long length = file.length();
   //
   // if (length > Integer.MAX_VALUE) {
   // // File is too large
   // }
   //
   // // Create the byte array to hold the data
   // byte[] bytes = new byte[(int) length];
   //
   // // Read in the bytes
   // int offset = 0;
   // int numRead = 0;
   // while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
   // offset += numRead;
   // }
   //
   // // Ensure all the bytes have been read in
   // if (offset < bytes.length) {
   // throw new IOException("Could not completely read file " + file.getName());
   // }
   //
   // // Close the input stream and return bytes
   // is.close();
   // return bytes;
   // }

   // decisions:
   // key = hash of BO
}