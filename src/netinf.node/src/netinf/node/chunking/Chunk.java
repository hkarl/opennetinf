package netinf.node.chunking;

public class Chunk {

   private byte[] data;
   private float chunk_size;
   private float number;

   private Unit unit;

   public Chunk(Unit unit, byte[] data, long chunk_size, long number) {

      this.unit = unit;
      this.data = data;
      this.chunk_size = chunk_size;
      this.number = number;

   }

   public byte[] getData() {
      return data;
   }
   
   public void setData(byte[] data) {
      this.data = data;
   }

   public float getChunk_size() {
      return chunk_size;
   }

   public void setChunk_size(long chunk_size) {
      this.chunk_size = chunk_size;
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
