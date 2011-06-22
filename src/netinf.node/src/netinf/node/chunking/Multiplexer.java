package netinf.node.chunking;

public class Multiplexer {

   private ChunkedBitlevelObject[] currentChunkedBOsTransferring;

   public Multiplexer() {

   }

   public Multiplexer(ChunkedBitlevelObject[] currentChunkedBOsTransferring) {

      this.currentChunkedBOsTransferring = currentChunkedBOsTransferring;
   }

   public void createChunkedBitlevelObject(int i, Unit transfer_type) {

      ChunkedBitlevelObject obj = new ChunkedBitlevelObject();
      obj.setTransfer_type(transfer_type);

   }

   public void getChunkNumberStartOffset() {
   
   }

   public void getChunkNumberEndOffset() {
   
   }

   private int translateInKB(int i, Unit unit) {

      int d;

      if (unit == Unit.BIT)

         d = i / (1024 * 1024);

      else if (unit == Unit.BYTE)

         d = i / 1024;

      else if (unit == Unit.KILOBYTE)

         d = i;

      else if (unit == Unit.MEGABYTE)

         d = i * 1024;

      else if (unit == Unit.GIGABYTE)

         d = i * 1024 * 1024;

      else

         d = i * 1024 * 1024 * 1024;

      return d;
   }
}
