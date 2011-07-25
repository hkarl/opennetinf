package netinf.node.chunking;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import netinf.common.datamodel.DataObject;
import netinf.common.datamodel.DefinedAttributePurpose;
import netinf.common.datamodel.attribute.Attribute;
import netinf.common.datamodel.attribute.DefinedAttributeIdentification;
import netinf.common.security.Hashing;
import netinf.common.utils.Utils;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;

/**
 * This class represents a chunked BO.
 * 
 * @author PG NetInf 3, University of Paderborn
 */
public class ChunkedBO {

   private static final Logger LOG = Logger.getLogger(ChunkedBO.class);
   private int chunkSizeInBytes = 256 * 1024;
   private List<Chunk> orderedChunkContainer;
   private int totalNoOfChunks;
   private List<String> baseUrls;

   public ChunkedBO(DataObject dataObject) throws NetInfNotChunkableException {
      List<Attribute> chunks = this.getChunks(dataObject);
      if (chunks == null || chunks.isEmpty()) {
         throw new NetInfNotChunkableException("DataObject has NO list of chunks");
      }

      baseUrls = getRangeEnabledLocators(dataObject);
      if (baseUrls.isEmpty()) {
         throw new NetInfNotChunkableException("DataObject has no range enabled URLs");
      }

      orderedChunkContainer = new ArrayList<Chunk>();
      for (Attribute chunkAttr : chunks) {
         orderedChunkContainer.add(new Chunk(chunkAttr));
      }

      totalNoOfChunks = getTotalNumberOfChunks(dataObject);
      if (totalNoOfChunks != orderedChunkContainer.size()) {
         throw new NetInfNotChunkableException("The number of chunks in the DO is nor valid");
      }
   }

   private List<String> getRangeEnabledLocators(DataObject obj) {
      List<String> urls = new ArrayList<String>();
      List<Attribute> attrs = obj.getAttributesForPurpose(DefinedAttributePurpose.LOCATOR_ATTRIBUTE.toString());
      for (Attribute attr : attrs) {
         String loc = attr.getValue(String.class);
         if (providesRanges(loc)) {
            LOG.info("(ChunkedBO ) URL provides RANGEs: " + loc);
            urls.add(loc);
         }
      }
      return urls;
   }

   private boolean providesRanges(String url) {
      HttpClient client = new DefaultHttpClient();
      try {
         HttpHead httpHead = new HttpHead(url);
         httpHead.setHeader("Range", "bytes=0-");
         try {
            HttpResponse response = client.execute(httpHead);
            int status = response.getStatusLine().getStatusCode();
            if (status == HttpStatus.SC_PARTIAL_CONTENT || status == HttpStatus.SC_OK) {
               return true;
            }
         } catch (ClientProtocolException e) {
            LOG.debug(e.getMessage());
         } catch (IOException e) {
            LOG.debug(e.getMessage());
         }
      } catch (IllegalArgumentException e) {
         LOG.debug(e.getMessage());
      }
      return false;
   }
   
   @SuppressWarnings("unused")
   private List<String> getLocatorsWithChunkedLabel(DataObject obj) {
      List<String> urls = new ArrayList<String>();
      List<Attribute> attrs = obj.getAttributesForPurpose(DefinedAttributePurpose.LOCATOR_ATTRIBUTE.toString());
      for (Attribute attr : attrs) {
         List<Attribute> subAttrs = attr.getSubattribute(DefinedAttributeIdentification.CHUNKED.getURI());
         for (Attribute chunkedAttr : subAttrs) {
            if (chunkedAttr.getValue(String.class).equals("true")) {
               urls.add(attr.getValue(String.class));
            }
         }
      }
      return urls;
   }

   private List<Attribute> getChunks(DataObject obj) {
      List<Attribute> attrs = obj.getAttribute(DefinedAttributeIdentification.CHUNKS.getURI());
      for (Attribute chunkList : attrs) {
         List<Attribute> chunks = chunkList.getSubattribute(DefinedAttributeIdentification.CHUNK.getURI());
         if (!chunks.isEmpty()) {
            return chunks;
         }
      }
      return null;
   }

   private int getTotalNumberOfChunks(DataObject obj) {
      List<Attribute> attrs = obj.getAttribute(DefinedAttributeIdentification.CHUNKS.getURI());
      for (Attribute chunkList : attrs) {
         return chunkList.getValue(Integer.class);
      }
      return 0;
   }

   /**
    * Constructor
    * 
    * @param filePath
    * @param sizeInBytes
    * @throws FileNotFoundException
    */
   public ChunkedBO(String filePath) throws FileNotFoundException {
      if (filePath == null) {
         throw new FileNotFoundException("path is null");
      }

      orderedChunkContainer = new ArrayList<Chunk>();

      // generate Chunks
      File file = new File(filePath);
      DataInputStream readStream = null;
      if (file.exists()) {
         try {
            readStream = new DataInputStream(new FileInputStream(file));

            // (BOcacheImpl) skip manually added content-type NOOOOOOOOOOOOOOOOOOO
            // int skipSize = readStream.readInt();
            // for (int i = 0; i < skipSize; i++) {
            // readStream.read();
            // }

            String hash = null;
            int chunkCount = 0;
            byte[] tempBuf;
            long fileSize = file.length();
            int chunkPart;
            ByteArrayOutputStream outStream = null;

            for (chunkPart = 0; chunkPart < fileSize / chunkSizeInBytes; chunkPart++) {
               outStream = new ByteArrayOutputStream(chunkSizeInBytes);

               for (int byteCount = 0; byteCount < chunkSizeInBytes; byteCount++) {
                  outStream.write(readStream.read());
               }

               chunkCount++;
               tempBuf = outStream.toByteArray();
               hash = Utils.hexStringFromBytes(Hashing.hashSHA1(new ByteArrayInputStream(tempBuf)));
               orderedChunkContainer.add(new Chunk(hash, chunkCount));
               // close the file
               outStream.close();
            }

            // loop for the last chunk (which may be smaller than the chunk size)
            if (fileSize != chunkSizeInBytes * (chunkPart - 1)) {
               // open the output file
               outStream = new ByteArrayOutputStream(chunkSizeInBytes);

               // write the rest of the file
               int b;
               while ((b = readStream.read()) != -1) {
                  outStream.write(b);
               }

               chunkCount++;
               tempBuf = outStream.toByteArray();
               hash = Utils.hexStringFromBytes(Hashing.hashSHA1(new ByteArrayInputStream(tempBuf)));
               orderedChunkContainer.add(new Chunk(hash, chunkCount));

               // close the file
               outStream.close();
            }

            // set total number
            totalNoOfChunks = chunkCount;

         } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         } finally {
            IOUtils.closeQuietly(readStream);
         }
      } else {
         throw new FileNotFoundException("(ChunkedBO ) File not found: " + filePath);
      }
   }

   public List<Chunk> getChunks() {
      return orderedChunkContainer;
   }

   /**
    * Provides the size of a single chunk
    * @return Size as integer.
    */
   public int getChunkSize() {
      return chunkSizeInBytes;
   }

   /**
    * Gets the total number of chunks.
    * @return The total number as integer.
    */
   public int getTotalNoOfChunks() {
      return totalNoOfChunks;
   }

   /**
    * Provides a list of base urls.
    * @return The list.
    */
   public List<String> getBaseUrls() {
      return baseUrls;
   }

}