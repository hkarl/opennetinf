package netinf.node.cache.network.impl;

import java.io.IOException;

import netinf.common.datamodel.DataObject;
import netinf.common.datamodel.DefinedAttributePurpose;
import netinf.common.datamodel.attribute.Attribute;
import netinf.common.datamodel.attribute.DefinedAttributeIdentification;
import netinf.common.utils.DatamodelUtils;
import netinf.common.utils.Utils;
import netinf.node.cache.network.NetworkCache;

import org.apache.log4j.Logger;

import com.google.inject.Inject;

/**
 * Implementations of the NetInfCache interface
 * 
 * @author PG NetInf 3, University of Paderborn
 */
public class NetInfCacheImpl implements NetworkCache {

   private static final Logger LOG = Logger.getLogger(NetInfCacheImpl.class);
   private CacheServer cacheServer;
   private String cacheName = "NetworkCache";

   @Inject
   public void setCacheServer(CacheServer cs) {
      cacheServer = cs;
   }

   @Override
   public boolean cache(DataObject dataObject, String downloadedTmpFile) {
      String hashOfBO = DatamodelUtils.getHash(dataObject);
      String urlPath = cacheServer.getURL(hashOfBO);

      if (!contains(hashOfBO)) {
         try {
            boolean success = cacheServer.cacheBO(Utils.getByteArray(downloadedTmpFile), hashOfBO); // TODO: stream instead byteArray
            if (success) {
               addLocator(dataObject, urlPath);
               return true;
            }
         } catch (IOException e) {
            e.printStackTrace();
         }
      } else {
         LOG.info("DO already in cache, but locator not in DO - adding locator entry...");
         addLocator(dataObject, urlPath);
         return true;
      }

      return false;
   }

   @Override
   public byte[] getBObyIO(DataObject dataObject) {
      String hashOfBO = DatamodelUtils.getHash(dataObject);

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
    * Adds a new locator to the DataObject
    * 
    * @param dataObject
    *           The given DataObject
    * @param url
    *           The URL of the locator
    */
   private void addLocator(DataObject dataObject, String url) {
      // Locator - http_url
      Attribute attribute = dataObject.getDatamodelFactory().createAttribute();
      attribute.setAttributePurpose(DefinedAttributePurpose.LOCATOR_ATTRIBUTE.toString());
      attribute.setIdentification(DefinedAttributeIdentification.HTTP_URL.getURI());
      attribute.setValue(url);

      // Cache marker
      Attribute cacheMarker = dataObject.getDatamodelFactory().createAttribute();
      cacheMarker.setAttributePurpose(DefinedAttributePurpose.SYSTEM_ATTRIBUTE.getAttributePurpose());
      cacheMarker.setIdentification(DefinedAttributeIdentification.CACHE.getURI());
      cacheMarker.setValue("true");
      attribute.addSubattribute(cacheMarker);

      // add chunk/range enabled flag
      Attribute chunkFlag = dataObject.getDatamodelFactory().createAttribute();
      chunkFlag.setAttributePurpose(DefinedAttributePurpose.SYSTEM_ATTRIBUTE.getAttributePurpose());
      chunkFlag.setIdentification(DefinedAttributeIdentification.CHUNKED.getURI());
      chunkFlag.setValue("true");
      attribute.addSubattribute(chunkFlag);

      if (!dataObject.getAttributes().contains(attribute)) {
         dataObject.addAttribute(attribute);
      }
   }

   @Override
   public String getAddress() {
      return cacheServer.getAddress();
   }

   @Override
   public String getName() {
      return cacheName;
   }

}