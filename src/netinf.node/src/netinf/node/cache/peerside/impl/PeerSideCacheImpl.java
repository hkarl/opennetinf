package netinf.node.cache.peerside.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import netinf.common.datamodel.NetInfObjectWrapper;
import netinf.common.security.Hashing;
import netinf.common.utils.Utils;
import netinf.node.cache.peerside.PeerSideCache;
import netinf.node.cache.peerside.PeerSideCacheServer;

import org.apache.log4j.Logger;

import com.google.inject.Inject;

/**
 * Implementation of peer-side caching.
 * 
 * @author PG NetInf 3, University of Paderborn
 */

public class PeerSideCacheImpl implements PeerSideCache {

   private static final Logger LOG = Logger.getLogger(PeerSideCacheImpl.class);
   private PeerSideCacheServer server;

   /**
    * Constructor
    */
   @Inject
   public PeerSideCacheImpl(PeerSideCacheServer server) {
      this.server = server;
   }

   /**
    * Method checks to see if NetInfObject has previously been cached
    */
   @Override
   public boolean contains(NetInfObjectWrapper dataObject) {
	  if(null == dataObject)
		  return false;
	  byte[] ioByteForm = dataObject.serializeToBytes();
      LOG.trace(null);

      String hash;
	try {
		hash = Utils.hexStringFromBytes(Hashing.hashSHA1(new ByteArrayInputStream(ioByteForm)));
		LOG.debug("Hash of dataobject is " + hash);
		return server.contains(hash);
	} catch (IOException e) {
		LOG.error("(PSCACHE )Could not open byte array to hash IO. Will not proceed with check");
		return false;
	}     
         
      
   }

   /**
    * Cache a general IO (represented by its most general Interface). If the object 
    * has previously been cached, the method will check and return true.
    */
@Override
public boolean cache(NetInfObjectWrapper io) {
	if (!contains(io)) {
	byte[] ioByteForm = io.serializeToBytes();
	try {
		String ioHash = Utils.hexStringFromBytes(Hashing.hashSHA1(new ByteArrayInputStream(ioByteForm)));
		server.cache(ioByteForm, ioHash);
		return true;
	} catch (IOException e) {
		LOG.error("(PSCACHE )Error hashing general IO, will not store");
		return false;
	}
	} else {
		LOG.warn("(PSCACHE ) NetInf Object has already been cached.");
		return true;
	}
}
}
