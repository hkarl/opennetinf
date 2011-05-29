package netinf.node.cache.peerside;

import netinf.common.datamodel.DataObject;

public interface PeerSideCacheServer {

	boolean contains(DataObject dataObject, String hash);

	void cache(byte[] hashBytes, String hash);

	String getURL(String hash);
}
