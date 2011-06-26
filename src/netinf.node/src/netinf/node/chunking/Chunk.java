package netinf.node.chunking;

import org.apache.log4j.Logger;

/**
 * The basic part for Chunking.
 * 
 * @author PG NetInf 3, University of Paderborn
 */

public class Chunk {

   private static final Logger LOG = Logger.getLogger(Chunk.class);

   private byte[] data;
   private int size;
   private int number;
   private int totalNumberOfChunks;
   private String hash;

   /**
    * Constructor.
    * 
    * @param currentChunkedBOsTransferring
    */
   public Chunk(String hash, byte[] data, int size, int number) {
      this.hash = hash;
      this.data = data;
      this.size = size;
      this.number = number;

      LOG.debug("Chunk created with HASH: " + hash + " SIZE: " + size + " NUMBER: " + number);
   }

   public byte[] getData() {
      return data;
   }

   public int getSize() {
      return size;
   }

   public int getNumber() {
      return number;
   }

   public String getHash() {
      return hash;
   }

   public void setTotalNumberOfChunks(int total) {
      totalNumberOfChunks = total;
   }

   public int getTotalNumberOfChunks() {
      return totalNumberOfChunks;
   }

}