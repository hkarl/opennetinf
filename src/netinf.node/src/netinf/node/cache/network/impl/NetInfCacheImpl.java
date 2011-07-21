package netinf.node.cache.network.impl;

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
import netinf.node.cache.network.NetworkCache;
import netinf.node.chunking.Chunk;
import netinf.node.chunking.ChunkedBO;
import netinf.node.transferDeluxe.TransferDispatcher;
import netinf.node.transferDeluxe.streamprovider.NetInfNoStreamProviderFoundException;

import org.apache.commons.io.IOUtils;
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
   private TransferDispatcher transferDispatcher;

   private boolean doChunking = true; // enables/disables chunking

   /**
    * Constructor
    */
   NetInfCacheImpl() {
      transferDispatcher = TransferDispatcher.getInstance();
   }

   @Inject
   public void setCacheServer(CacheServer cs) {
      cacheServer = cs;
   }

   @Override
   public void cache(DataObject dataObject) {
      String hashOfBO = getHash(dataObject);

      if (hashOfBO != null) {
         String urlPath = cacheServer.getURL(hashOfBO);
         if (!contains(hashOfBO)) {
            List<Attribute> locators = dataObject.getAttributesForPurpose(DefinedAttributePurpose.LOCATOR_ATTRIBUTE.toString());
            for (Attribute attribute : locators) {
               String url = attribute.getValue(String.class);

               try {
                  // download
                  String destination = getTmpFolder() + File.separator + hashOfBO + ".tmp";
                  transferDispatcher.getStreamAndSave(url, destination, false);

                  // get hash
                  FileInputStream fis = new FileInputStream(destination);
                  byte[] hashBytes = Hashing.hashSHA1(fis);
                  IOUtils.closeQuietly(fis);

                  if (hashOfBO.equalsIgnoreCase(Utils.hexStringFromBytes(hashBytes))) {
                     boolean success = cacheServer.cacheBO(this.getByteArray(destination), hashOfBO); // TODO: stream instead byteArray
                     if (success) {
                        addLocator(dataObject, urlPath, destination);
                        deleteTmpFile(destination); // deleting tmp file
                        LOG.info("DO cached...");
                        return;
                     }
                  } else {
                     LOG.info("Hash of file: " + hashOfBO + " -- Other: " + Utils.hexStringFromBytes(hashBytes));
                     LOG.log(DemoLevel.DEMO, "(NODE ) Hash of downloaded file is invalid. Trying next locator");
                  }

               } catch (FileNotFoundException ex) {
                  LOG.warn("FileNotFound: " + url);
               } catch (IOException e) {
                  LOG.warn("IOException:" + url);
               } catch (NetInfNoStreamProviderFoundException no) {
                  LOG.warn("No StreamProvider found for: " + url);
               }
            } // end for
         } else {
            LOG.info("DO already in cache, but locator not in DO - adding locator entry...");
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
    * Adds a new locator to the DataObject
    * 
    * @param dataObject
    *           The given DataObject
    * @param url
    *           The URL of the locator
    */
   private void addLocator(DataObject dataObject, String url, String pathOfLocalTmpFile) {
      
      for (Attribute attr : dataObject.getAttributes()) {
         if (attr.getAttributePurpose().toString() == DefinedAttributePurpose.LOCATOR_ATTRIBUTE.toString()
               && attr.getValue(String.class) == url) {
            // already in DO
            return;
         }
      }
      
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
 
      dataObject.addAttribute(attribute);

      // Added chunks/ranges
      if (doChunking && !this.containsChunks(dataObject)) {
         try {
            ChunkedBO chunkedBO = new ChunkedBO(pathOfLocalTmpFile);
            
            // Chunks
            Attribute chunksAttr = dataObject.getDatamodelFactory().createAttribute();
            chunksAttr.setAttributePurpose(DefinedAttributePurpose.SYSTEM_ATTRIBUTE.toString());
            chunksAttr.setIdentification(DefinedAttributeIdentification.CHUNKS.getURI());
            chunksAttr.setValue(chunkedBO.getTotalNoOfChunks());
            
            
            for (Chunk chunk : chunkedBO.getChunks()) {
               // Subattribute Chunk
               Attribute singleChunkAttr = dataObject.getDatamodelFactory().createAttribute();
               singleChunkAttr.setAttributePurpose(DefinedAttributePurpose.SYSTEM_ATTRIBUTE.toString());
               singleChunkAttr.setIdentification(DefinedAttributeIdentification.CHUNK.getURI());
               singleChunkAttr.setValue(chunk.getNumber());
               
               // SubSubattribute
               Attribute hashOfChunkAttr = dataObject.getDatamodelFactory().createAttribute();
               hashOfChunkAttr.setAttributePurpose(DefinedAttributePurpose.SYSTEM_ATTRIBUTE.toString());
               hashOfChunkAttr.setIdentification(DefinedAttributeIdentification.HASH_OF_CHUNK.getURI());
               hashOfChunkAttr.setValue(chunk.getHash());
               
               // add atributes
               singleChunkAttr.addSubattribute(hashOfChunkAttr);
               chunksAttr.addSubattribute(singleChunkAttr);
            }
            
            // add chunk list
            dataObject.addAttribute(chunksAttr);
            
            // add chunk flag
            Attribute chunkFlag = dataObject.getDatamodelFactory().createAttribute();
            chunkFlag.setAttributePurpose(DefinedAttributePurpose.SYSTEM_ATTRIBUTE.getAttributePurpose());
            chunkFlag.setIdentification(DefinedAttributeIdentification.CHUNKED.getURI());
            chunkFlag.setValue("true");
            attribute.addSubattribute(chunkFlag);
            
         } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
      }
   }

   private boolean containsChunks(DataObject dataObject) {
      for (Attribute attr : dataObject.getAttributes()) {
         if (attr.getIdentification().toString().equals(DefinedAttributeIdentification.CHUNKS.getURI())) {
            LOG.info("(NetworkCache ) Chunklist already exists");
            return true;
         }
      }
      LOG.info("(NetworkCache ) Chunklist does not exist");
      return false;
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
   
   private byte[] getByteArray(String filePath) throws IOException {
      FileInputStream fis = new FileInputStream(filePath);
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      byte[] buffer = new byte[16384];

      //TODO: use IOutils?
      for (int len = fis.read(buffer); len > 0; len = fis.read(buffer)) {
          bos.write(buffer, 0, len);
      }
      fis.close();
      
      return bos.toByteArray();
  }

   @Override
   public String getAddress() {
      return cacheServer.getAddress();
   }

}