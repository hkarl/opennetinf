package netinf.node.cache.network;

import netinf.common.datamodel.DataObject;
import netinf.node.cache.Cache;

/**
 * Interface for an in-network-cache that can be used by a NetInf node
 * 
 * @author PG NetInf 3, University of Paderborn
 */
public interface NetworkCache extends Cache {

   /**
    * Gets the BO related to the IO
    * 
    * @param dataObject
    *           the given DataObject
    * @return the corresponding BO
    */
   byte[] getBObyIO(DataObject dataObject);

}