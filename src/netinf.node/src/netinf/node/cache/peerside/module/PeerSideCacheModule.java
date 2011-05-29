package netinf.node.cache.peerside.module;

import netinf.node.cache.peerside.PeerSideCache;
import netinf.node.cache.peerside.PeerSideCacheServer;
import netinf.node.cache.peerside.impl.PeerSideCacheImpl;
import netinf.node.cache.peerside.impl.PeerSideCacheServerImpl;

import com.google.inject.PrivateModule;
import com.google.inject.Singleton;

public class PeerSideCacheModule extends PrivateModule {

	@Override
	protected void configure() {
		bind(PeerSideCache.class).to(PeerSideCacheImpl.class).in(
				Singleton.class);
		bind(PeerSideCacheServer.class).to(PeerSideCacheServerImpl.class).in(
				Singleton.class);
		expose(PeerSideCache.class);
	}

}
