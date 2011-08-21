package netinf.node.transferDeluxe.chunkstreams;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;

import netinf.node.chunking.Chunk;
import netinf.node.chunking.ChunkedBO;
import netinf.node.transferDeluxe.TransferDispatcher;
import netinf.node.transferDeluxe.streamprovider.NetInfNoStreamProviderFoundException;

import org.apache.log4j.Logger;

/**
 * @author PG NetInf 3
 */
public class URLStreamEnum implements Enumeration<InputStream> {

   private static final Logger LOG = Logger.getLogger(URLStreamEnum.class);
   private List<String> baseURLs;
   private int maxChunk;
   private int curChunk;
   private List<Chunk> chunks;

   public URLStreamEnum(ChunkedBO chunkedBO) {
      chunks = chunkedBO.getChunks();
      maxChunk = chunks.size();
      baseURLs = chunkedBO.getBaseUrls();
      curChunk = 0;
   }

   @Override
   public boolean hasMoreElements() {
      return curChunk < maxChunk;
   }

   @Override
   public InputStream nextElement() {
      for (String baseUrl : baseURLs) {
         LOG.info("Try baseURL: " + baseUrl);
         try {
            System.out.println("get chunk number: " + curChunk);
            Chunk chunk = getChunkByNumber(curChunk);
            InputStream result = TransferDispatcher.getInstance().getStream(chunk, baseUrl);
            curChunk++;
            return result;
         } catch (NetInfNoStreamProviderFoundException e) {
            LOG.warn("(StreamEnum ) No Streamprivder found:" + e.getMessage());
         } catch (IOException e) {
            e.printStackTrace();
         }
      }

      curChunk++;
      // return error stream (-1 = end)
      return new InputStream() {

         @Override
         public int read() throws IOException {
            return -1;
         }
      };
   }

   /**
    * Provides the chunk by the given number
    * 
    * @param number
    * @return The Chunk
    */
   private Chunk getChunkByNumber(int number) {
      for (Chunk chunk : chunks) {
         if (chunk.getNumber() == number) {
            return chunk;
         }
      }
      return null;
   }

}