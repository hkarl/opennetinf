package netinf.node.chunking;

import netinf.common.log.demo.DemoLevel;

import org.apache.log4j.Logger;

/**
 * This class represents a single chunk.
 * 
 * @author PG NetInf 3, University of Paderborn
 */
public class Chunk {

   private static final Logger LOG = Logger.getLogger(Chunk.class);
   private int number;
   private String hash;

   /**
    * Constructor
    * 
    * @param hash
    * @param number
    */
   public Chunk(String hash, int number) {
      this.hash = hash;
      this.number = number;

      LOG.log(DemoLevel.DEMO, "(Chunk ) Chunk created with HASH: " + hash + " NUMBER: " + number);
   }

   public int getNumber() {
      return number;
   }

   public String getHash() {
      return hash;
   }

}