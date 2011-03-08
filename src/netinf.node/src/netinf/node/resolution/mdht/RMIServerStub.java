package netinf.node.resolution.mdht;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import netinf.common.datamodel.DatamodelFactory;
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
   private DatamodelFactory datamodelFactory;

   protected RMIServerStub(MDHTResolutionService mdht, DatamodelFactory dm) throws RemoteException {
      super();
      this.mdht = mdht;
      this.datamodelFactory = dm;
   }

   @Override
   public InformationObject getRemote(Identifier identifier, int level, int maxLevel) throws RemoteException {
      LOG.debug(null);
      InformationObject result = mdht.getFromStorage(identifier);
      if (result != null) {
         return result;
      } else {
         if (level < maxLevel) {
            return mdht.get(identifier, level + 1, maxLevel);
         }
      }
      return null;
   }

   @Override
   public void putRemote(byte[] ion, int fromLevel, int toLevel) throws RemoteException {
      LOG.debug(null);
      InformationObject io = datamodelFactory.createInformationObjectFromBytes(ion);
      LOG.info("Storing IO: " + io.getIdentifier());
      mdht.storeIO(io);
      if (fromLevel < toLevel) {
         mdht.put(io, fromLevel + 1, toLevel);
      }
   }
}
