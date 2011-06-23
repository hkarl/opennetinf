package netinf.node.chunking;

/**
 * The basic part for Chunking.
 * 
 * @author PG NetInf 3, University of Paderborn
 */

public class Chunk {

   private byte[] data;
   private float chunkSize;
   private float number;

   private Unit unit;

   /**
    * Default constructor.
    */
   public Chunk() {

   }

   /**
    * Constructor.
    * 
    * @param currentChunkedBOsTransferring
    */
   public Chunk(Unit unit, byte[] data, long chunk_size, long number) {

      this.unit = unit;
      this.data = data;
      this.chunkSize = chunk_size;
      this.number = number;

   }

   public byte[] getData() {
      return data;
   }
   
   public void setData(byte[] data) {
      this.data = data;
   }

   public float getChunk_size() {
      return chunkSize;
   }

   public void setChunk_size(long chunk_size) {
      this.chunkSize = chunk_size;
   }

   public float getNumber() {
      return number;
   }

   public void setNumber(long number) {
      this.number = number;
   }

   public Unit getUnit() {
      return unit;
   }

   public void setUnit(Unit unit) {
      this.unit = unit;
   }

}