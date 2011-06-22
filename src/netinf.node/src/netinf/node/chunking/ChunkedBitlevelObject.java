package netinf.node.chunking;

public class ChunkedBitlevelObject {

   private Chunk first_chunk;
   private Unit transfer_type;
   private byte[] buffer;
   private Unit sizeInKB;
   private Chunk[] orderedChunkContainer;

   public ChunkedBitlevelObject() {

   }

   public ChunkedBitlevelObject(Chunk first_chunk, Unit transfer_type, byte[] buffer, Unit sizeInKB,
         Chunk[] orderedChunkContainer) {
      super();
      this.first_chunk = first_chunk;
      this.transfer_type = transfer_type;
      this.buffer = buffer;
      this.sizeInKB = sizeInKB;
      this.orderedChunkContainer = orderedChunkContainer;
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

   public Unit getSizeInKB() {
      return sizeInKB;
   }

   public void setSizeInKB(Unit sizeInKB) {
      this.sizeInKB = sizeInKB;
   }

   public Chunk[] getOrderedChunkContainer() {
      return orderedChunkContainer;
   }

   public void setOrderedChunkContainer(Chunk[] orderedChunkContainer) {
      this.orderedChunkContainer = orderedChunkContainer;
   }

}
