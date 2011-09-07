package netinf.node.transferdispatcher.streamprovider;

import netinf.common.exceptions.NetInfCheckedException;

/**
 * Exception if no correct StreamProvider has been found.
 * 
 * @author PG NetInf 3, University of Paderborn.
 */
public class NetInfNoStreamProviderFoundException extends NetInfCheckedException {

   private static final long serialVersionUID = 8889841563395034966L;

   public NetInfNoStreamProviderFoundException(Exception cause) {
      super(cause);
   }

   public NetInfNoStreamProviderFoundException(String description, Exception cause) {
      super(description, cause);
   }

   public NetInfNoStreamProviderFoundException(String str) {
      super(str);
   }

}