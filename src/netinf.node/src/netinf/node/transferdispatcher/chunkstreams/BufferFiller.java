/*
 * Copyright (C) 2009-2011 University of Paderborn, Computer Networks Group
 * (Full list of owners see http://www.netinf.org/about-2/license)
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * 
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *       this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of Paderborn nor the names of its contributors may be used to endorse
 *       or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDERS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
