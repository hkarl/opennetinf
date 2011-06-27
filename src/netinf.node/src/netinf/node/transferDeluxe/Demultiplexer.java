package netinf.node.transferDeluxe;

import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.lang.reflect.Array;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import netinf.common.datamodel.attribute.Attribute;
import netinf.common.datamodel.attribute.DefinedAttributeIdentification;
import netinf.node.transferDeluxe.streamprovider.NetInfNoStreamProviderFoundException;

import org.apache.log4j.Logger;

/**
 * @author PG NetInf 3, University of Paderborn
 */
public class Demultiplexer extends InputStream {

   private static final Logger LOG = Logger.getLogger(Demultiplexer.class);

   private List<Attribute> chunks;
   private int totalNumberOfChunks;

   public Demultiplexer(List<Attribute> chunks) throws NetInfNoStreamProviderFoundException, IOException {
      this.chunks = chunks;
      totalNumberOfChunks = chunks.size();
      openStreamCount = new AtomicInteger(totalNumberOfChunks);
      LOG.debug("(Demultiplexer ) Number of chunks: " + totalNumberOfChunks);

      // TODO: a lot ...
    
      sources = new InputStream[chunks.size()];
      Attribute chunkAttr;
      String chunkUrl = "";
      // generate List of InputStreams
      LOG.debug("(Demultiplexer ) Building InputStreams...");
      for (int i = 0; i < sources.length; i++) {
         chunkAttr = getChunk(i + 1);
         chunkUrl = chunkAttr.getValue(String.class);
         LOG.debug("(Demultiplexer ) Building InputStream of chunk: " + chunkUrl);
         sources[i] = TransferDispatcher.getInstance().getStream(chunkUrl);
      }
      
//      LOG.debug("(Demultiplexer ) Starting ReaderThread");
//      new ChunkReader().start();
   }
   
   public SequenceInputStream getInputs() {
      SequenceInputStream sin = new SequenceInputStream(makeEnumeration(sources));
      return sin;
   }

   private Attribute getChunk(int number) {
      for (Attribute chunk : chunks) {
         List<Attribute> attr = chunk.getSubattribute(DefinedAttributeIdentification.NUMBER_OF_CHUNK.getURI());
         if (attr.size() > 0) {
            if (attr.get(0).getValue(Integer.class) == number) {
               return chunk;
            }
         }
      }
      return null;
   }

   @Override
   public void close() throws IOException {
      String ex = "";
      for (InputStream is : sources) {
         try {
            is.close();
         } catch (IOException e) {
            ex += e.getMessage() + " ";
         }
      }
      if (ex.length() > 0) {
         throw new IOException(ex.substring(0, ex.length() - 1));
      }
   }

   private AtomicInteger openStreamCount;
   private BlockingQueue<Integer> buf = new ArrayBlockingQueue<Integer>(1);
   private InputStream[] sources;

   @Override
   public int read() throws IOException {
      for (int i = 0; i < sources.length; i++) {
         while (sources[i].available() != 0) {
            return sources[i].read();
         }
      }
      return -1;
      
      
//      if (openStreamCount.get() == 0) {
//         return -1;
//      }
//
//      try {
//         return buf.take();
//      } catch (InterruptedException e) {
//         throw new IOException(e);
//      }
   }
   
   class ChunkReader extends Thread {

      @Override
      public void run() {
         try {
            int data;
            for (int i = 0; i < sources.length; i++) {
               while ((data = sources[i].read()) != -1) {
                  buf.put(data);
               }
              
            }
         } catch (IOException e) {
            e.printStackTrace();
         } catch (InterruptedException e) {
            e.printStackTrace();
         }

         openStreamCount.decrementAndGet();
      }
   }
      public Enumeration makeEnumeration(final Object obj) {
        Class type = obj.getClass();
        if (!type.isArray()) {
          throw new IllegalArgumentException(obj.getClass().toString());
        } else {
          return (new Enumeration() {
            int size = Array.getLength(obj);

            int cursor;

            @Override
            public boolean hasMoreElements() {
              return (cursor < size);
            }

            @Override
            public Object nextElement() {
              return Array.get(obj, cursor++);
            }
          });
        }
      }

}