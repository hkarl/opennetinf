package netinf.node.chunking;

/**
 * Seperator for chunking.
 * 
 * @author PG NetInf 3, University of Paderborn
 */

public class Multiplexer {

   private ChunkedBitlevelObject[] currentChunkedBOsTransferring;

   /**
    * Default constructor.
    */
   public Multiplexer() {

   }

   /**
    * Constructor.
    * 
    * @param currentChunkedBOsTransferring
    */
   public Multiplexer(ChunkedBitlevelObject[] currentChunkedBOsTransferring) {

      this.currentChunkedBOsTransferring = currentChunkedBOsTransferring;
   }

   /**
    * Creates a chunked object.
    * 
    * @param i
    * @param transfer_type
    */
   public void createChunkedBitlevelObject(int i, Unit transfer_type) {

   }

   /**
    * Gets the start offset with the given number and unit.
    * 
    * @param unit
    * @param i
    * @return startOffset
    */
   public int getChunkNumberStartOffset(Unit unit, int i) {

      return (Integer) null;
   }

   /**
    * Gets the end offset with the given number and unit.
    * 
    * @param unit
    * @param i
    * @return endOffset
    */
   public int getChunkNumberEndOffset(Unit unit, int i) {

      return (Integer) null;
   }

   /**
    * Gets currentChunkedBOsTransferring.
    * 
    * @return currentChunkedBOsTransferring
    */
   public ChunkedBitlevelObject[] getCurrentChunkedBOsTransferring() {
      return currentChunkedBOsTransferring;
   }

   /**
    * Sets currentChunkedBOsTransferring.
    * 
    * @param currentChunkedBOsTransferring
    */
   public void setCurrentChunkedBOsTransferring(ChunkedBitlevelObject[] currentChunkedBOsTransferring) {
      this.currentChunkedBOsTransferring = currentChunkedBOsTransferring;
   }

   /**
    * Translates the given number into KB
    * 
    * @param i
    * @param unit
    * @return d: the result of translation
    */
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
