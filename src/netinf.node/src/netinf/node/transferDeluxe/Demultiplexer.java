package netinf.node.transferDeluxe;

import java.io.InputStream;
import java.util.List;

import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.attribute.Attribute;
import netinf.common.datamodel.attribute.DefinedAttributeIdentification;

import org.apache.log4j.Logger;

/**
 * @author PG NetInf 3
 */
public class Demultiplexer {

   private static final Logger LOG = Logger.getLogger(Demultiplexer.class);

   private List<Attribute> chunks;
   private InformationObject io;

   public Demultiplexer(InformationObject io) {
      this.io = io;
      chunks = io.getAttribute(DefinedAttributeIdentification.CHUNK.getURI());
      LOG.debug("(Demultiplexer ) Number of chunks: " + chunks.size());
   }

   public InputStream getCombinedStream() {
      InputStream inStream = null;
      TransferDispatcher disp = TransferDispatcher.getInstance();
      int numberOfChunks = getTotalNumberOfChunks(io);
      int iter = 1;
      while (iter <= numberOfChunks) {
         Attribute chunk = getChunk(iter);
         if (chunk != null) {
            // TODO...
         }
         iter++;
      }
      return inStream;

   }

   private int getTotalNumberOfChunks(InformationObject io) {
      List<Attribute> allChunks = io.getAttribute(DefinedAttributeIdentification.CHUNK.getURI());
      if (allChunks.size() > 0) {
         List<Attribute> subAttrs = allChunks.get(0).getSubattribute(
               DefinedAttributeIdentification.TOTAL_NUMBER_OF_CHUNKS.getURI());
         if (subAttrs.size() > 0) {
            subAttrs.get(0).getValue(Integer.class);
         }
      }
      return 0;
   }

   private Attribute getChunk(int number) {
      List<Attribute> allChunks = io.getAttribute(DefinedAttributeIdentification.CHUNK.getURI());
      for (Attribute chunk : allChunks) {
         List<Attribute> attr = chunk.getSubattribute(DefinedAttributeIdentification.NUMBER_OF_CHUNK.getURI());
         if (attr.size() > 0) {
            return attr.get(0);
         }
      }
      return null;
   }

}
