package netinf.node.transferDeluxe.chunkstreams;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import netinf.node.chunking.ChunkedBO;

/**
 * @author PG NetInf 3
 */
public class ConcurrentChunkStream extends InputStream {

   private List<BlockingQueue<byte[]>> bufferQueues;
   private List<BufferFiller> bufferFillers;
   private int noOfFiller = 7;

   private int currQueue;
   private ByteArrayInputStream in;
   private int max;
   private int cur;

   public ConcurrentChunkStream(ChunkedBO chunkedBO) {
      max = chunkedBO.getTotalNoOfChunks() - 1;
      cur = 0;
      currQueue = 0;

      // init queues and bufferfillers
      bufferQueues = new ArrayList<BlockingQueue<byte[]>>(noOfFiller);
      bufferFillers = new ArrayList<BufferFiller>(noOfFiller);
      for (int i = 0; i < noOfFiller; i++) {
         BlockingQueue<byte[]> buffer = new ArrayBlockingQueue<byte[]>(noOfFiller); 
         bufferQueues.add(buffer);
         BufferFiller filler = new BufferFiller(chunkedBO, i, noOfFiller, buffer); 
         bufferFillers.add(filler);
         filler.start();
      }

      // start taking
      nextChunk();
   }

   /**
    * loads the next chunk into the stream
    */
   private void nextChunk() {
      try {
         in = new ByteArrayInputStream(bufferQueues.get(currQueue).take());
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
   
   @Override
   public void close() throws IOException {
      for (BufferFiller filler : bufferFillers) {
         filler.interruptAndClear();
      }
   }

}