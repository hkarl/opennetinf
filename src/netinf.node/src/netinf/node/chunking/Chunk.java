package netinf.node.chunking;

import netinf.common.datamodel.attribute.Attribute;
import netinf.common.datamodel.attribute.DefinedAttributeIdentification;
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
    *           The hash as string.
    * @param number
    *           The number as Integer
    */
   public Chunk(String hash, int number) {
      this.hash = hash;
      this.number = number;

      LOG.log(DemoLevel.DEMO, "(Chunk ) Chunk created with HASH: " + hash + " NUMBER: " + number);
   }

   /**
    * Constructor
    * 
    * @param chunkAttr
    *           An attribute representing a chunk.
    */
   public Chunk(Attribute chunkAttr) {
      Attribute hashAttr = chunkAttr.getSingleSubattribute(DefinedAttributeIdentification.HASH_OF_CHUNK.getURI());
      hash = hashAttr.getValue(String.class);
      number = chunkAttr.getValue(Integer.class);

      LOG.log(DemoLevel.DEMO, "(Chunk ) Chunk created with HASH: " + hash + " NUMBER: " + number);
   }

   /**
    * Provides the number of this chunk.
    * 
    * @return The number as Integer.
    */
   public int getNumber() {
      return number;
   }

   /**
    * Provides the hash value.
    * 
    * @return The hash as string.
    */
   public String getHash() {
      return hash;
   }

}