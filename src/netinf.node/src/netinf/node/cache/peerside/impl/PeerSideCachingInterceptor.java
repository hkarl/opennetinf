package netinf.node.cache.peerside.impl;

import netinf.common.datamodel.DataObject;
import netinf.common.datamodel.InformationObject;
import netinf.node.cache.peerside.PeerSideCache;
import netinf.node.resolution.ResolutionInterceptor;

import com.google.inject.Inject;

public class PeerSideCachingInterceptor implements ResolutionInterceptor {

	private PeerSideCache cache;

	@Inject
	public PeerSideCachingInterceptor(PeerSideCache cache) {
		this.cache = cache;
	}

	@Override
	public InformationObject interceptGet(InformationObject io) {
		if (io instanceof DataObject) {
			cache.cache((DataObject) io);
		}
		return io;
	}

}
