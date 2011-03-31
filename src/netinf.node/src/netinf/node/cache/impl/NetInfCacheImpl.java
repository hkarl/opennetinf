package netinf.node.cache.impl;

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
import netinf.node.cache.NetInfCache;
import netinf.node.transfer.http.TransferJobHttp;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.google.inject.Inject;

/**
 * Implementations of the NetInfCache interface
 * 
 * @author PG NetInf 3, University of Paderborn
 */
public class NetInfCacheImpl implements NetInfCache {

   private static final Logger LOG = Logger.getLogger(NetInfCacheImpl.class); // Logger
   private CacheServer cacheServer; // adapter for the used cache server

   // decisions:
   // key = hash of BO

   /**
    * Constructor
    */
   NetInfCacheImpl() {

   }

   @Inject
   public void setCacheServer(CacheServer cs) {
      cacheServer = cs;
   }

   @Override
   public void cache(DataObject dataObject) {
      String hashOfBO = getHash(dataObject);

      if (hashOfBO != null) {
         if (!contains(hashOfBO)) {
            List<Attribute> locators = dataObject.getAttributesForPurpose(DefinedAttributePurpose.LOCATOR_ATTRIBUTE.toString());
            for (Attribute attribute : locators) {
               String url = attribute.getValue(String.class);

               try {
                  if (url.startsWith("http://")) {
                     String destination = getTmpFolder() + File.separator + hashOfBO + ".tmp";
                     TransferJobHttp job = new TransferJobHttp(hashOfBO, url, destination);
                     job.startCacheJob();

                     FileInputStream fis = new FileInputStream(destination);
                     byte[] hashBytes = Hashing.hashSHA1(fis);
                     IOUtils.closeQuietly(fis);

                     if (hashOfBO.equalsIgnoreCase(Utils.hexStringFromBytes(hashBytes))) {
                        boolean success = cacheServer.cacheBO(hashBytes, hashOfBO);
                        if (success) {
                           String urlPath = cacheServer.getURL(hashOfBO);
                           addLocator(dataObject, urlPath);
                           LOG.info("DO cached...");
                        }
                     } else {
                        LOG.info("Hash of file: " + hashOfBO + " -- Other: " + Utils.hexStringFromBytes(hashBytes));
                        LOG.log(DemoLevel.DEMO, "(NODE ) Hash of downloaded file is invalid. Trying next locator");
                     }
                  }
               } catch (FileNotFoundException ex) {
                  LOG.warn("FileNotFound:" + url);
               } catch (IOException e) {
                  LOG.warn("IOException:" + url);
               }
            } // end for
         } else {
            LOG.info("DO already in cache");
         }
      } else {
         LOG.info("Hash is null, will not be cached");
      }
   }

   /**
    * Get system tmp folder
    * 
    * @return path to netinf tmp folder
    */
   private String getTmpFolder() {
      String pathToTmp = System.getProperty("java.io.tmpdir") + File.separator + "netinfcache";
      File folder = new File(pathToTmp);
      if (folder.exists() && folder.isDirectory()) {
         return pathToTmp;
      } else {
         folder.mkdir();
         return pathToTmp;
      }
   }

   @Override
   public byte[] getBObyIO(DataObject dataObject) {
      String hashOfBO = getHash(dataObject);

      if (hashOfBO != null) {
         if (contains(hashOfBO)) {
            // get from adapter and return
            byte[] bo = cacheServer.getBO(hashOfBO);
            return bo;
         } else {
            LOG.info("DO not in cache");
         }
      } else {
         LOG.info("Hash is null, will not be in cache");
      }

      return null;
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

   /**
    * @param dataObject
    * @param url
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

}