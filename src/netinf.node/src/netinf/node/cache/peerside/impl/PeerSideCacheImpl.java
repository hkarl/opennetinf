package netinf.node.cache.peerside.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import netinf.common.datamodel.DataObject;
import netinf.common.datamodel.DefinedAttributePurpose;
import netinf.common.datamodel.attribute.Attribute;
import netinf.common.datamodel.attribute.DefinedAttributeIdentification;
import netinf.common.log.demo.DemoLevel;
import netinf.common.security.Hashing;
import netinf.common.utils.Utils;
import netinf.node.cache.peerside.PeerSideCache;
import netinf.node.cache.peerside.PeerSideCacheServer;
import netinf.node.transferDeluxe.TransferDispatcher;
import netinf.node.transferDeluxe.streamprovider.NetInfNoStreamProviderFoundException;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.google.inject.Inject;

/**
 * Implementation of peer-side caching.
 * 
 * @author PG NetInf 3, University of Paderborn
 */

public class PeerSideCacheImpl implements PeerSideCache {

   private static final Logger LOG = Logger.getLogger(PeerSideCacheImpl.class);
   private PeerSideCacheServer server;
   private TransferDispatcher transferDispatcher;

   /**
    * Constructor
    */
   @Inject
   public PeerSideCacheImpl(PeerSideCacheServer server) {
      this.server = server;
      transferDispatcher = TransferDispatcher.getInstance();
   }

   @Override
   public boolean contains(DataObject dataObject) {

      if (null == dataObject) {
         return false;
      }

      String hashOfBO = getHash(dataObject);
      return server.contains(hashOfBO);
   }

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

   @Override
   public boolean isConnected() {
      if (server == null) {
         return false;
      }

      return server.isConnected();
   }

   @Override
   public boolean cache(DataObject dataObject) {
      if (dataObject == null) {
         return false;
      }

      String hashOfBO = getHash(dataObject);

      if (hashOfBO != null) {
         String urlPath = server.getURL(hashOfBO);
         if (!contains(dataObject)) {
            List<Attribute> locators = dataObject.getAttributesForPurpose(DefinedAttributePurpose.LOCATOR_ATTRIBUTE.toString());
            for (Attribute attribute : locators) {
               String url = attribute.getValue(String.class);

               try {
                  // Download from some locator
                  String destination = getTmpFolder() + File.separator + hashOfBO + ".tmp";
                  transferDispatcher.getStreamAndSave(url, destination, false);

                  // get hash
                  FileInputStream fis = new FileInputStream(destination);
                  byte[] hashBytes = Hashing.hashSHA1(fis);
                  IOUtils.closeQuietly(fis);

                  if (hashOfBO.equalsIgnoreCase(Utils.hexStringFromBytes(hashBytes))) {
                     boolean success = server.cache(getByteArray(destination), hashOfBO);
                     if (success) {
                        addLocator(dataObject, urlPath);
                        deleteTmpFile(destination); // deleting tmp file
                        LOG.info("DO cached...");
                        return true;
                     }
                  } else {
                     LOG.info("Hash of file: " + hashOfBO + " -- Other: " + Utils.hexStringFromBytes(hashBytes));
                     LOG.log(DemoLevel.DEMO, "(PSCACHE ) Hash of downloaded file is invalid. Trying next locator");
                  }
               } catch (FileNotFoundException ex) {
                  LOG.warn("FileNotFound:" + url);
               } catch (IOException e) {
                  LOG.warn("IOException:" + url);
               } catch (NetInfNoStreamProviderFoundException no) {
                  LOG.warn("No StreamProvider found for: " + url);
               }
            } // end for
         } else {
            LOG.info("DO already in cache, but locator not in DO - adding locator entry...");
            addLocator(dataObject, urlPath);
            return true;
         }
      } else {
         LOG.info("Hash is null, will not be cached");
      }
      return false;
   }

   /**
    * Get system tmp folder
    * 
    * @return path to netinf tmp folder
    */
   private String getTmpFolder() {
      String pathToTmp = System.getProperty("java.io.tmpdir") + File.separator + "peersidecache";
      File folder = new File(pathToTmp);
      if (folder.exists() && folder.isDirectory()) {
         return pathToTmp;
      } else {
         folder.mkdir();
         return pathToTmp;
      }
   }

   /**
    * Deletes the given file
    * 
    * @param path
    *           The path of the file
    */
   private void deleteTmpFile(String path) {
      File file = new File(path);
      file.delete();
   }

   /**
    * Adds a new locator to the DataObject
    * 
    * @param dataObject
    *           The given DataObject
    * @param url
    *           The URL of the locator
    */
   private void addLocator(DataObject dataObject, String url) {
      Attribute attribute = dataObject.getDatamodelFactory().createAttribute();
      attribute.setAttributePurpose(DefinedAttributePurpose.LOCATOR_ATTRIBUTE.toString());
      attribute.setIdentification(DefinedAttributeIdentification.HTTP_URL.getURI());
      attribute.setValue(url);
      if (!dataObject.getAttributes().contains(attribute)) {
         dataObject.addAttribute(attribute);
      }
   }

   private byte[] getByteArray(String filePath) throws IOException {
      FileInputStream fis = new FileInputStream(filePath);
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      byte[] buffer = new byte[16384];

      for (int len = fis.read(buffer); len > 0; len = fis.read(buffer)) {
         bos.write(buffer, 0, len);
      }
      fis.close();

      return bos.toByteArray();
   }

}