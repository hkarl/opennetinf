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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import netinf.node.chunking.ChunkedBO;

/**
 * Concurrent streaming of chunks.
 * 
 * @author PG NetInf 3, University of Paderborn.
 */
public class ConcurrentChunkStream extends InputStream {

   private List<BlockingQueue<byte[]>> bufferQueues;
   private List<BufferFiller> bufferFillers;
   private int noOfFiller = 7;

   private int currQueue;
   private ByteArrayInputStream in;
   private int max;
   private int cur;

   /**
    * Constructor.
    */
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
