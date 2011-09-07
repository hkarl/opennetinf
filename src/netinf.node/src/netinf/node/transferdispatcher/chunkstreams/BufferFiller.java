package netinf.node.transferdispatcher.chunkstreams;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import netinf.node.chunking.Chunk;
import netinf.node.chunking.ChunkedBO;
import netinf.node.transferdispatcher.TransferDispatcher;
import netinf.node.transferdispatcher.streamprovider.NetInfNoStreamProviderFoundException;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/**
 * BufferFiller. Used by {@link ConcurrentChunkStream}.
 * 
 * @author PG NetInf 3, University of Paderborn.
 */
public class BufferFiller extends Thread {

   private static final Logger LOG = Logger.getLogger(BufferFiller.class);
   private List<String> baseUrls;
   private int curr;
   private int incr;
   private int max;
   private List<Chunk> chunks;
   private BlockingQueue<byte[]> bufferQueue;

   /**
    * Constructor.
    */
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
      while (curr <= max && !isInterrupted()) {
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
         } catch (InterruptedException e) {
            interrupt();
            LOG.info("(BufferFiller ) Thread interrupted");
         } catch (IOException e) {
            LOG.warn("(BufferFiller ) IOException:" + e.getMessage());
         } finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(out);
         }
      }

      // signal end
      try {
         bufferQueue.put(new byte[] { -1 });
      } catch (InterruptedException e) {
         LOG.info("(BufferFiller ) Thread interrupted");
      }
      LOG.debug("(BufferFiller ) End of Thread");
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

   /**
    * Interrupts the buffer thread and clears all remaining bytes in buffer.
    */
   public void interruptAndClear() {
      LOG.info("(BufferFiller ) Interrupt and clear buffer");
      interrupt();
      bufferQueue.clear();
   }

}