package netinf.node.resolution.mdht;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.InformationObject;
import netinf.common.log.demo.DemoLevel;

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
   public InformationObject getRemote(byte[] ident, int level, int maxLevel) throws RemoteException {
      Identifier identifier = datamodelFactory.createIdentifierFromBytes(ident);
      LOG.log(DemoLevel.DEMO, "(MDHT-RMI ) Getting IO: " + identifier + " on level " + level);
      InformationObject result = mdht.getFromStorage(identifier);
      if (result != null) {
         LOG.log(DemoLevel.DEMO, "(MDHT-RMI ) Returning IO: " + identifier + " on level " + level);
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
      InformationObject io = datamodelFactory.createInformationObjectFromBytes(ion);
      LOG.log(DemoLevel.DEMO, "(MDHT-RMI ) Storing IO: " + io.getIdentifier() + " on level " + fromLevel);
      mdht.storeIO(io);
      if (fromLevel < toLevel) {
         mdht.put(io, fromLevel + 1, toLevel);
      } else {
         LOG.log(DemoLevel.DEMO, "(MDHT-RMI ) Highest level reached");
      }
   }
}
