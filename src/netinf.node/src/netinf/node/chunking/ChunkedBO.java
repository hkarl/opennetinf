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

import netinf.common.security.Hashing;
import netinf.common.utils.Utils;

import org.apache.commons.io.IOUtils;

/**
 * This class represents a chunked BO.
 * 
 * @author PG NetInf 3, University of Paderborn
 */
public class ChunkedBO {

   private int chunkSizeInBytes = 256 * 1024;
   private List<Chunk> orderedChunkContainer;
   private int totalNoOfChunks;
   
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
//            int skipSize = readStream.readInt();
//            for (int i = 0; i < skipSize; i++) {
//               readStream.read();
//            }

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
            this.totalNoOfChunks = chunkCount;
            
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

   public int getChunkSize() {
      return chunkSizeInBytes;
   }
   
   public int getTotalNoOfChunks() {
      return totalNoOfChunks;
   }

}