package netinf.node.transferDeluxe.chunkstreams;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import netinf.node.chunking.Chunk;
import netinf.node.chunking.ChunkedBO;
import netinf.node.transferDeluxe.TransferDispatcher;
import netinf.node.transferDeluxe.streamprovider.NetInfNoStreamProviderFoundException;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/**
 * @author PG NetInf 3
 */
public class BufferFiller extends Thread {

   private static final Logger LOG = Logger.getLogger(BufferFiller.class);
   private List<String> baseUrls;
   private int curr;
   private int incr;
   private int max;
   private List<Chunk> chunks;
   private BlockingQueue<byte[]> bufferQueue;

   public BufferFiller(ChunkedBO chunkedBO, int first, int incr, BlockingQueue<byte[]> queue) {
      chunks = chunkedBO.getChunks();
      baseUrls = chunkedBO.getBaseUrls();
      curr = first;
      this.incr = incr;
      max = chunkedBO.getTotalNoOfChunks() - 1;
      bufferQueue = queue;
   }

   @Override
   public void run() {
      while (curr <= max) {
         InputStream in = null;
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         try {
            for (String baseUrl : baseUrls) {
               try {
                  Chunk chunk = getChunkByNumber(curr);
                  System.out.println("curr = " + curr + " chunk: " + chunk + " max:" + max);
                  in = TransferDispatcher.getInstance().getStream(chunk, baseUrl);
                  curr += incr;

                  IOUtils.copy(in, out);
                  bufferQueue.put(out.toByteArray());
                  Thread.yield();
                  break; // break for loop over base urls
               } catch (NetInfNoStreamProviderFoundException e) {
                  LOG.warn("(BufferFiller ) No Streamprivder found: " + e.getMessage());
               }
            }
         } catch (MalformedURLException e) {
            LOG.warn("(BufferFiller ) MalformedURLException: " + e.getMessage());
         } catch (IOException e) {
            LOG.warn("(BufferFiller ) IOException:" + e.getMessage());
         } catch (InterruptedException e) {
            LOG.warn("(BufferFiller ) InterruptedException: " + e.getMessage());
         } finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(out);
         }
      }

      // signal end
      try {
         bufferQueue.put(new byte[] { -1 });
      } catch (InterruptedException e) {
         LOG.warn("(BufferFiller ) InterruptedException: " + e.getMessage());
      }
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