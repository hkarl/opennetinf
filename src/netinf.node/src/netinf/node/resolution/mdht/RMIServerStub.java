package netinf.node.resolution.mdht;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.InformationObject;

import org.apache.log4j.Logger;

/**
 * @author PG NetInf 3
 */
public class RMIServerStub extends UnicastRemoteObject implements RemoteRS {

   private static final long serialVersionUID = -7843557270838353074L;
   private static final Logger LOG = Logger.getLogger(RMIServerStub.class);
   private MDHTResolutionService mdht;

   protected RMIServerStub(MDHTResolutionService mdht) throws RemoteException {
      super();
      this.mdht = mdht;
   }

   @Override
   public InformationObject getRemote(Identifier identifier) throws RemoteException {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public void putRemote(InformationObject io, int fromLevel, int toLevel) throws RemoteException {
      LOG.info("Storing IO: " + io.getIdentifier());
      mdht.storeIO(io);
      if (fromLevel < toLevel) {
         mdht.put(io, fromLevel + 1, toLevel);
      }
   }
}
