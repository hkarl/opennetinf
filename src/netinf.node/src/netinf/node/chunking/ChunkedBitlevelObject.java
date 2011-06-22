package netinf.node.chunking;

package netinf.node.chunking;

/**
 * The Objects, which are chunked and consists of several chunks.
 * 
 * @author PG NetInf 3, University of Paderborn
 */

public class ChunkedBitlevelObject {

   private Chunk first_chunk;
   private Unit transfer_type;
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
   public ChunkedBitlevelObject(Chunk first_chunk, Unit transfer_type, byte[] buffer, float sizeInKB,
         Chunk[] orderedChunkContainer, Unit unit) {
      super();
      this.first_chunk = first_chunk;
      this.transfer_type = transfer_type;
      this.buffer = buffer;
      this.sizeInKB = sizeInKB;
      this.orderedChunkContainer = orderedChunkContainer;
      this.unit = unit;
   }

   public Chunk getFirst_chunk() {
      return first_chunk;
   }

   public void setFirst_chunk(Chunk first_chunk) {
      this.first_chunk = first_chunk;
   }

   public Unit getTransfer_type() {
      return transfer_type;
   }

   public void setTransfer_type(Unit transfer_type) {
      this.transfer_type = transfer_type;
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
