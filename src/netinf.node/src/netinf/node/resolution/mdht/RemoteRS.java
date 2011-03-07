package netinf.node.resolution.mdht;

import java.rmi.Remote;
import java.rmi.RemoteException;

import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.InformationObject;

/**
 * @author PG NetInf 3
 */
public interface RemoteRS extends Remote {

   InformationObject getRemote(Identifier identifier) throws RemoteException;

   void putRemote(InformationObject io, int fromLevel, int toLevel) throws RemoteException;

}
