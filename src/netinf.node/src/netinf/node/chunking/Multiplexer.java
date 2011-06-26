package netinf.node.chunking;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * Seperator for chunking.
 * 
 * @author PG NetInf 3, University of Paderborn
 */

public class Multiplexer {

   private static final Logger LOG = Logger.getLogger(Multiplexer.class);

   private List<ChunkedBO> currentChunkedBOsTransferring = new ArrayList<ChunkedBO>();

   /**
    * Default constructor.
    */
   public Multiplexer() {

      LOG.debug("no chunked BO.");
   }

   /**
    * Constructor.
    * 
    * @param currentChunkedBOsTransferring
    */
   public Multiplexer(List<ChunkedBO> currentChunkedBOsTransferring) {

      this.currentChunkedBOsTransferring = currentChunkedBOsTransferring;
   }

   /**
    * Creates a chunked object.
    * 
    * @param chunkSize
    * @param transfer_type
    */
   public void createChunkedBO(String filePath, int chunkSize) {

      try {

         ChunkedBO chunkedBO = new ChunkedBO(filePath, chunkSize);
         currentChunkedBOsTransferring.add(chunkedBO);

      } catch (FileNotFoundException e) {

         LOG.debug("Chunked BO created failed.");
         e.printStackTrace();
      }

   }

   /**
    * Gets the start offset with the given number and unit.
    * 
    * @param chunkedBO
    * @param numberOfChunk
    * @return startOffset
    */
   public int getChunkNumberStartOffset(ChunkedBO chunkedBO, int numberOfChunk) {

      return (numberOfChunk - 1) * chunkedBO.getChunkSize();
   }

   /**
    * Gets the end offset with the given number and unit.
    * 
    * @param chunkedBO
    * @param numberOfChunk
    * @return endOffset
    */
   public int getChunkNumberEndOffset(ChunkedBO chunkedBO, int numberOfChunk) {

      return numberOfChunk * chunkedBO.getChunkSize() - 1;
   }

   /**
    * Gets currentChunkedBOsTransferring.
    * 
    * @return currentChunkedBOsTransferring
    */
   public List<ChunkedBO> getCurrentChunkedBOsTransferring() {
      return currentChunkedBOsTransferring;
   }

   /**
    * Sets currentChunkedBOsTransferring.
    * 
    * @param currentChunkedBOsTransferring
    */
   /*
    * public void setCurrentChunkedBOsTransferring(ChunkedBO[] currentChunkedBOsTransferring) { this.currentChunkedBOsTransferring
    * = currentChunkedBOsTransferring; }
    */
   /*
    * public void setChunkSize(int chunkSize){ this.chunkSize = chunkSize ; } public int getChunkSize(){ return this.chunkSize ; }
    */
   /**
    * Translates the given number into KB
    * 
    * @param i
    * @param unit
    * @return d: the result of translation
    */
   private int translateIntoKB(int i, Unit unit) {

      int d;

      if (unit == Unit.BIT) {
         d = i / (8 * 1024);
      } else if (unit == Unit.BYTE) {
         d = i / 1024;
      } else if (unit == Unit.KILOBYTE) {
         d = i;
      } else if (unit == Unit.MEGABYTE) {
         d = i * 1024;
      } else if (unit == Unit.GIGABYTE) {
         d = i * 1024 * 1024;
      } else {
         d = i * 1024 * 1024 * 1024;
      }

      return d;
   }

   /**
    * Translates the given number into Byte
    * 
    * @param i
    * @param unit
    * @return d: the result of translation
    */
   private int translateIntoByte(int i, Unit unit) {

      int d;

      if (unit == Unit.BIT) {
         d = i / 8;
      } else if (unit == Unit.BYTE) {
         d = i;
      } else if (unit == Unit.KILOBYTE) {
         d = i * 1024;
      } else if (unit == Unit.MEGABYTE) {
         d = i * 1024 * 1024;
      } else if (unit == Unit.GIGABYTE) {
         d = i * 1024 * 1024 * 1024;
      } else {
         d = i * 1024 * 1024 * 1024 * 1024;
      }

      return d;
   }
}
