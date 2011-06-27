package netinf.node.chunking;

import org.apache.log4j.Logger;

/**
 * This class represents a single chunk.
 * 
 * @author PG NetInf 3, University of Paderborn
 */
public class Chunk {

   private static final Logger LOG = Logger.getLogger(Chunk.class);

   private byte[] data;
   private int sizeInBytes;
   private int number;
   private int totalNumberOfChunks;
   private String hash;

   /**
    * Constructor
    * 
    * @param hash
    * @param data
    * @param sizeInBytes
    * @param number
    */
   public Chunk(String hash, byte[] data, int sizeInBytes, int number) {
      this.hash = hash;
      this.data = data;
      this.sizeInBytes = sizeInBytes;
      this.number = number;

      LOG.debug("Chunk created with HASH: " + hash + " SIZE: " + sizeInBytes + " NUMBER: " + number);
   }

   public byte[] getData() {
      return data;
   }

   public int getSize() {
      return sizeInBytes;
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