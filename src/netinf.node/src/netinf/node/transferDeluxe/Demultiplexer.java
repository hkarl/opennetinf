package netinf.node.transferDeluxe;

import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.List;
import java.util.Vector;

import netinf.common.datamodel.attribute.Attribute;
import netinf.common.log.demo.DemoLevel;
import netinf.node.transferDeluxe.streamprovider.NetInfNoStreamProviderFoundException;

import org.apache.log4j.Logger;

/**
 * @author PG NetInf 3, University of Paderborn
 */
public class Demultiplexer {

   private static final Logger LOG = Logger.getLogger(Demultiplexer.class);

   private List<Attribute> chunks;
   private int totalNumberOfChunks;

   private Vector<InputStream> sources;

   public Demultiplexer(List<Attribute> chunks) throws NetInfNoStreamProviderFoundException, IOException {
      this.chunks = chunks;
      totalNumberOfChunks = chunks.size();
      sources = new Vector<InputStream>();

      LOG.log(DemoLevel.DEMO, "(Demultiplexer ) Number of chunks: " + totalNumberOfChunks);

      // TODO: a lot ...
   }

   public SequenceInputStream getCombinedStreams() throws NetInfNoStreamProviderFoundException, IOException {
      Attribute chunkAttr;
      String chunkUrl = "";
      // generate List of InputStreams
      LOG.debug("(Demultiplexer ) Building/Merging InputStreams...");
      for (int i = 1; i <= totalNumberOfChunks; i++) {
         chunkAttr = getChunk(i);
         chunkUrl = chunkAttr.getValue(String.class);
         LOG.log(DemoLevel.DEMO, "(Demultiplexer ) Building InputStream of chunk: " + chunkUrl);
         sources.add(TransferDispatcher.getInstance().getStream(chunkUrl));
      }

      return new SequenceInputStream(sources.elements());
   }

   private Attribute getChunk(int number) {
//      for (Attribute chunk : chunks) {
//         List<Attribute> attr = chunk.getSubattribute(DefinedAttributeIdentification.NUMBER_OF_CHUNK.getURI());
//         if (attr.size() > 0) {
//            if (attr.get(0).getValue(Integer.class) == number) {
//               return chunk;
//            }
//         }
//      }
      return null;
   }

}