package netinf.node.chunking;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import netinf.common.security.Hashing;
import netinf.common.utils.Utils;

import org.apache.commons.io.IOUtils;

/**
 * The Objects, which are chunked and consists of several chunks.
 * 
 * @author PG NetInf 3, University of Paderborn
 */

public class ChunkedBO {

   private Unit transferType;
   private byte[] buffer;
   private int chunkSizeInBytes;
   private List<Chunk> orderedChunkContainer;

   /**
    * Constructor
    * @param filePath
    * @param sizeInBytes
    * @throws FileNotFoundException
    */
   public ChunkedBO(String filePath, int sizeInBytes) throws FileNotFoundException {
      orderedChunkContainer = new ArrayList<Chunk>();
      buffer = new byte[sizeInBytes];
      chunkSizeInBytes = sizeInBytes;

      // generate Chunks
      File file = new File(filePath);
      DataInputStream readStream = null;
      if (file.exists()) {
         try {
            readStream = new DataInputStream(new FileInputStream(file));
            int byteCount = 0;
            Chunk chunk = null;
            String hash = null;
            int chunkCount = 0;
            while ((byteCount = readStream.read(buffer)) >= 0) {
               chunkCount++;
               hash = Utils.hexStringFromBytes(Hashing.hashSHA1(new ByteArrayInputStream(buffer)));
               chunk = new Chunk(hash, buffer.clone(), buffer.length, chunkCount);
               orderedChunkContainer.add(chunk);
            }
            // set total number
            for (Chunk ch : orderedChunkContainer) {
               ch.setTotalNumberOfChunks(chunkCount);
            }
         } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         } finally {
            IOUtils.closeQuietly(readStream);
         }
      } else {
         throw new FileNotFoundException("File not found: " + filePath);
      }
   }

   public List<Chunk> getChunks() {
      return orderedChunkContainer;
   }

   public Unit getTransferType() {
      return transferType;
   }

   public void setTransferType(Unit transferType) {
      this.transferType = transferType;
   }

   public int getChunkSize() {
      return chunkSizeInBytes;
   }

   public void setChunkSize(int chunkSize) {
      this.chunkSizeInBytes = chunkSize;
   }

}