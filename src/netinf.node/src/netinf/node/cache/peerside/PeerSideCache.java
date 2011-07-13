package netinf.node.cache.peerside;
import netinf.common.datamodel.DataObject;
import netinf.common.datamodel.NetInfObjectWrapper;

/**
 * Interface of peer-side caching.
 * 
 * @author PG NetInf 3, University of Paderborn
 */

public interface PeerSideCache {

   /**
    * Checks if this cache contains the <Code>DataObject</code>
    * 
    * @param dataObject
    * @return
    */

   boolean contains(DataObject dataObject);

   /**
    * Will get the content of the DataObject and store it in the cache. Blocks until the content has been stored. Will insert a
    * locator to the cached data in case of success
    * 
    * @return true if a valid Object could be stored, false otherwise
    * @param io
    */

   boolean cache(NetInfObjectWrapper io);
   

}
