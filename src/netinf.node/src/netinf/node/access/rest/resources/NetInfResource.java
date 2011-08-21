package netinf.node.access.rest.resources;

import netinf.common.communication.NetInfNodeConnection;
import netinf.common.datamodel.DatamodelFactory;
import netinf.node.access.rest.RESTApplication;

import org.restlet.resource.ServerResource;

/**
 * Abstract resource that provides a NetInfNodeConnection and a DatamodelFactory.
 * 
 * @author PG NetInf 3, University of Paderborn
 *
 */
public abstract class NetInfResource extends ServerResource {
	
   /**
    * Yields a connection to a NetInfNode.
    * 
    * @return A NetInfNodeConnection of the parent application
    */
	protected NetInfNodeConnection getNodeConnection() {
	   return ((RESTApplication) getApplication()).getNodeConnection();
	}
	
	/**
	 * Yields an implementation of a DatamodelFactory.
	 * 
	 * @return A concrete DatamodelFactory
	 */
	protected DatamodelFactory getDatamodelFactory() {
	   return ((RESTApplication) getApplication()).getDatamodelFactory();
	}

}