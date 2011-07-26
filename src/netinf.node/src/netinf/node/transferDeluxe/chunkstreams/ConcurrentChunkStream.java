package netinf.node.transferDeluxe.chunkstreams;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import netinf.node.chunking.ChunkedBO;

/**
 * @author PG NetInf 3
 */
public class ConcurrentChunkStream extends InputStream {

   private BlockingQueue<byte[]>[] bufferQueues;
   private int noOfFiller = 7;

   private int currQueue;
   private ByteArrayInputStream in;
   private int max;
   private int cur;

   @SuppressWarnings("unchecked")
   public ConcurrentChunkStream(ChunkedBO chunkedBO) {
      max = chunkedBO.getTotalNoOfChunks() - 1;
      cur = 0;
      currQueue = 0;

      // init queues and bufferfillers
      bufferQueues = new BlockingQueue[noOfFiller];
      for (int i = 0; i < noOfFiller; i++) {
         bufferQueues[i] = new ArrayBlockingQueue<byte[]>(7);
         new BufferFiller(chunkedBO, i, noOfFiller, bufferQueues[i]).start();
      }

      // start taking
      nextChunk();
   }

   /**
    * loads the next chunk into the stream
    */
   private void nextChunk() {
      try {
         in = new ByteArrayInputStream(bufferQueues[currQueue].take());
         currQueue = (currQueue + 1) % noOfFiller;
         cur++;
      } catch (InterruptedException e) {
         e.printStackTrace();
      }
   }

   @Override
   public int read() throws IOException {
      if (cur > max + 1) {
         return -1;
      }

      if (in.available() > 0) {
         return in.read();
      } else {
         nextChunk();
         return read();
      }
   }

}