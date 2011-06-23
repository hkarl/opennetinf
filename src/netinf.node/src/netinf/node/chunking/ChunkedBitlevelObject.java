package netinf.node.chunking;

/**
 * The Objects, which are chunked and consists of several chunks.
 * 
 * @author PG NetInf 3, University of Paderborn
 */

public class ChunkedBitlevelObject {

   private Chunk firstChunk;
   private Unit transferType;
   private byte[] buffer;
   private float sizeInKB;
   private Chunk[] orderedChunkContainer;

   private Unit unit;

   /**
    * Default constructor.
    */
   public ChunkedBitlevelObject() {

   }

   /**
    * Constructor.
    * 
    * @param currentChunkedBOsTransferring
    */
   public ChunkedBitlevelObject(Chunk firstChunk, Unit transferType, byte[] buffer, float sizeInKB,
         Chunk[] orderedChunkContainer, Unit unit) {
      super();
      this.firstChunk = firstChunk;
      this.transferType = transferType;
      this.buffer = buffer;
      this.sizeInKB = sizeInKB;
      this.orderedChunkContainer = orderedChunkContainer;
      this.unit = unit;
   }

   public Chunk getFirstChunk() {
      return firstChunk;
   }

   public void setFirstChunk(Chunk firstChunk) {
      this.firstChunk = firstChunk;
   }

   public Unit getTransferType() {
      return transferType;
   }

   public void setTransferType(Unit transferType) {
      this.transferType = transferType;
   }

   public byte[] getBuffer() {
      return buffer;
   }

   public void setBuffer(byte[] buffer) {
      this.buffer = buffer;
   }

   public float getSizeInKB() {
      return sizeInKB;
   }

   public void setSizeInKB(float sizeInKB) {
      this.sizeInKB = sizeInKB;
   }

   public Chunk[] getOrderedChunkContainer() {
      return orderedChunkContainer;
   }

   public void setOrderedChunkContainer(Chunk[] orderedChunkContainer) {
      this.orderedChunkContainer = orderedChunkContainer;
   }

}
