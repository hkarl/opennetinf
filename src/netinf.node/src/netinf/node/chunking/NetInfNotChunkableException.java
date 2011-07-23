package netinf.node.chunking;

import netinf.common.exceptions.NetInfCheckedException;

/**
 * @author PG NetInf 3
 */
public class NetInfNotChunkableException extends NetInfCheckedException {

   private static final long serialVersionUID = -7633559296438822198L;

   public NetInfNotChunkableException(Exception cause) {
      super(cause);
   }

   public NetInfNotChunkableException(String description, Exception cause) {
      super(description, cause);
   }

   public NetInfNotChunkableException(String str) {
      super(str);
   }

}