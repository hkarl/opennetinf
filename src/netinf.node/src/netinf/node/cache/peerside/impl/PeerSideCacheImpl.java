package netinf.node.cache.peerside.impl;

import java.io.IOException;

import netinf.common.datamodel.DataObject;
import netinf.common.datamodel.DefinedAttributePurpose;
import netinf.common.datamodel.attribute.Attribute;
import netinf.common.datamodel.attribute.DefinedAttributeIdentification;
import netinf.common.utils.DatamodelUtils;
import netinf.common.utils.Utils;
import netinf.node.cache.peerside.PeerSideCache;
import netinf.node.cache.peerside.PeerSideCacheServer;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * @author PG NetInf 3, University of Paderborn
 */
public class PeerSideCacheImpl implements PeerSideCache {

   private static final Logger LOG = Logger.getLogger(PeerSideCacheImpl.class);
   private PeerSideCacheServer server;
   private String cacheName = "PeersideCache";
   private int mdhtLevel;

   /**
    * Constructor
    * @param server The PeersideServer.
    * @param mdhtLevel The injected mdht level (how far to be published).
    */
   @Inject
   public PeerSideCacheImpl(PeerSideCacheServer server, @Named("mdht_level") final int mdhtLevel) {
      this.server = server;
      this.mdhtLevel = mdhtLevel;
   }

   @Override
   public boolean contains(String hashOfBO) {
      if (isConnected()) {
         return server.contains(hashOfBO);
      }
      return false;
   }

   @Override
   public boolean isConnected() {
      if (server != null && server.isConnected()) {
         return true;
      }
      return false;
   }

   @Override
   public boolean cache(DataObject dataObject, String downloadedTmpFile) {
      String hashOfBO = DatamodelUtils.getHash(dataObject);
      String urlPath = server.getURL(hashOfBO);

      if (!contains(hashOfBO)) {
         try {
            boolean success = server.cache(Utils.getByteArray(downloadedTmpFile), hashOfBO);
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

      // TODO: add flag if ranging is available...
      // add chunk/range enabled flag
//      Attribute chunkFlag = dataObject.getDatamodelFactory().createAttribute();
//      chunkFlag.setAttributePurpose(DefinedAttributePurpose.SYSTEM_ATTRIBUTE.getAttributePurpose());
//      chunkFlag.setIdentification(DefinedAttributeIdentification.CHUNKED.getURI());
//      chunkFlag.setValue("true");
//      attribute.addSubattribute(chunkFlag);
      
      if (!dataObject.getAttributes().contains(attribute)) {
         dataObject.addAttribute(attribute);
      }
      
      // level attribute
      Attribute level = dataObject.getDatamodelFactory().createAttribute();
      level.setAttributePurpose(DefinedAttributePurpose.SYSTEM_ATTRIBUTE.toString());
      level.setIdentification(DefinedAttributeIdentification.MDHT_LEVEL.getURI());
      level.setValue(mdhtLevel);
      
      if (!dataObject.getAttributes().contains(level)) {
         dataObject.addAttribute(level);
      }
   }

   @Override
   public String getAddress() {
      return server.getAddress();
   }

   @Override
   public String getName() {
      return cacheName;
   }

}