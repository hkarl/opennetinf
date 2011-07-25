package netinf.node.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import netinf.common.datamodel.DataObject;
import netinf.common.datamodel.DefinedAttributePurpose;
import netinf.common.datamodel.attribute.Attribute;
import netinf.common.datamodel.attribute.DefinedAttributeIdentification;
import netinf.common.exceptions.NetInfCheckedException;
import netinf.common.log.demo.DemoLevel;
import netinf.common.security.Hashing;
import netinf.common.utils.DatamodelUtils;
import netinf.common.utils.Utils;
import netinf.node.api.impl.LocalNodeConnection;
import netinf.node.chunking.Chunk;
import netinf.node.chunking.ChunkedBO;
import netinf.node.transferDeluxe.LocatorSelector;
import netinf.node.transferDeluxe.TransferDispatcher;
import netinf.node.transferDeluxe.streamprovider.NetInfNoStreamProviderFoundException;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/**
 * @author PG NetInf 3
 */
public class CacheJob extends Thread {

   private static final Logger LOG = Logger.getLogger(CacheJob.class);
   private List<BOCache> usedCaches;
   private LocalNodeConnection connection;
   private DataObject toBeCached;
   private TransferDispatcher transferDispatcher;
   private boolean useChunking;

   /**
    * Constructor
    * 
    * @param dO
    *           The DO that should be used for caching.
    * @param caches
    *           The to be used caches.
    * @param conn
    *           The connection to the local node.
    * @param useChunking
    *           The flag if chunking should be used.
    */
   public CacheJob(DataObject dO, List<BOCache> caches, LocalNodeConnection conn, boolean useChunking) {
      transferDispatcher = TransferDispatcher.getInstance();
      usedCaches = caches;
      connection = conn;
      toBeCached = dO;
      this.useChunking = useChunking;
   }

   @Override
   public void run() {
      // if it should be cached is already checked in CachingInterceptor

      String cachedTmpFile = downloadAndCheckHashOfFile();
      if (cachedTmpFile == null) {
         return;
      }

      for (BOCache useCache : usedCaches) {
         LOG.info("(CacheJob ) CachingJob started...: " + useCache.getName());
         boolean success = useCache.cache(toBeCached, cachedTmpFile);
         if (success) {
            LOG.info("(CacheJob ) CachingJob finished...");
         } else {
            LOG.info("(CacheJob ) CachingJob FAILED...");
         }
      }

      if (useChunking) {
         addChunkList(cachedTmpFile);
      }

      // delete after caching
      deleteTmpFile(cachedTmpFile);

      // call RS
      try {
         LOG.info("(CacheJob ) Putting back to RS (+ new locators from caching)...");
         connection.putIO(toBeCached);
      } catch (NetInfCheckedException e) {
         LOG.warn("(CacheJob ) Error during putting back... " + e.getMessage());
      }
   }

   /**
    * Downloads the BO of the underlying DO and checks the hash value.
    * 
    * @return The path to the downloaded file, otherwise null.
    */
   private String downloadAndCheckHashOfFile() {
      String hashOfBO = DatamodelUtils.getHash(toBeCached);
      LocatorSelector locSel = new LocatorSelector(toBeCached);
      while (locSel.hasNext()) {
         String url = locSel.next();
         try {
            String destination = Utils.getTmpFolder("netinfCache") + File.separator + hashOfBO + ".tmp";
            transferDispatcher.getStreamAndSave(url, destination, false);

            // get hash
            FileInputStream fis = new FileInputStream(destination);
            byte[] hashBytes = Hashing.hashSHA1(fis);
            IOUtils.closeQuietly(fis);

            if (hashOfBO.equalsIgnoreCase(Utils.hexStringFromBytes(hashBytes))) {
               return destination;
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
      }
      return null;
   }

   /**
    * Adds the chunk list to the underlying DO
    * 
    * @param cachedTmpFile
    *           Path to the temp file
    */
   private void addChunkList(String cachedTmpFile) {
      if (containsChunkList()) {
         return;
      }

      try {
         ChunkedBO chunkedBO = new ChunkedBO(cachedTmpFile);

         // Chunks
         Attribute chunksAttr = toBeCached.getDatamodelFactory().createAttribute();
         chunksAttr.setAttributePurpose(DefinedAttributePurpose.SYSTEM_ATTRIBUTE.toString());
         chunksAttr.setIdentification(DefinedAttributeIdentification.CHUNKS.getURI());
         chunksAttr.setValue(chunkedBO.getTotalNoOfChunks());

         for (Chunk chunk : chunkedBO.getChunks()) {
            // Subattribute Chunk
            Attribute singleChunkAttr = toBeCached.getDatamodelFactory().createAttribute();
            singleChunkAttr.setAttributePurpose(DefinedAttributePurpose.SYSTEM_ATTRIBUTE.toString());
            singleChunkAttr.setIdentification(DefinedAttributeIdentification.CHUNK.getURI());
            singleChunkAttr.setValue(chunk.getNumber());

            // SubSubattribute
            Attribute hashOfChunkAttr = toBeCached.getDatamodelFactory().createAttribute();
            hashOfChunkAttr.setAttributePurpose(DefinedAttributePurpose.SYSTEM_ATTRIBUTE.toString());
            hashOfChunkAttr.setIdentification(DefinedAttributeIdentification.HASH_OF_CHUNK.getURI());
            hashOfChunkAttr.setValue(chunk.getHash());

            // add attributes
            singleChunkAttr.addSubattribute(hashOfChunkAttr);
            chunksAttr.addSubattribute(singleChunkAttr);
         }

         // add chunk list
         toBeCached.addAttribute(chunksAttr);

      } catch (FileNotFoundException e) {
         e.printStackTrace();
      }
   }

   /**
    * Checks if a chunk list already exists.
    * 
    * @return true if chunk list exists, otherwise false.
    */
   private boolean containsChunkList() {
      for (Attribute chunkLists : toBeCached.getAttribute(DefinedAttributeIdentification.CHUNKS.getURI())) {
         List<Attribute> chunks = chunkLists.getSubattribute(DefinedAttributeIdentification.CHUNK.getURI());
         if (!chunks.isEmpty()) {
            LOG.info("(CacheJob ) Chunklist already exists");
            return true;
         }
      }
      LOG.info("(CacheJob ) Chunklist does not exist");
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

   /**
    * Provides the name of this CacheJob
    * 
    * @return Name as string.
    */
   public String getThreadName() {
      return toBeCached.getIdentifier().toString();
   }
}