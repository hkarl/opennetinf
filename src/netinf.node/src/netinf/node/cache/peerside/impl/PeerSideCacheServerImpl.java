package netinf.node.cache.peerside.impl;

import java.io.File;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import netinf.common.datamodel.DataObject;
import netinf.node.cache.peerside.CacheMemoryStoreEvictionPolicy;
import netinf.node.cache.peerside.PeerSideCacheServer;

import org.apache.log4j.Logger;

public class PeerSideCacheServerImpl implements PeerSideCacheServer {

	private static final Logger LOG = Logger
			.getLogger(PeerSideCacheServerImpl.class);

	// private static String DEFAULT_CACHE_CONFIG_PATH =
	// "configs/ehcache_config.xml";

	// private CacheManager cacheManager;

	private Cache cache;

	private CacheConfiguration cacheConfig;

	public PeerSideCacheServerImpl() {

		// this.cacheManager = new CacheManager(DEFAULT_CACHE_CONFIG_PATH);

		cacheConfig = new CacheConfiguration("peerSideCacheConfig", 10000);
		cacheConfig.setDiskStorePath(System.getProperty("java.io.tmpdir")
				+ File.separator + "peerSideCache");
		cacheConfig.setMaxElementsOnDisk(10000);
		cacheConfig.setEternal(false);
		cacheConfig.setMemoryStoreEvictionPolicy("LRU");
		cacheConfig.setOverflowToDisk(true);

		this.cache = new Cache(cacheConfig);

	}

	public PeerSideCacheServerImpl(String cacheName, int maxElementsInMemory,
			String path, Boolean eternal,
			CacheMemoryStoreEvictionPolicy policy, int maxElementsOnDisk,
			Boolean overflowToDisk) {

		// this.cacheManager = new CacheManager(configPath);

		cacheConfig = new CacheConfiguration(cacheName, maxElementsInMemory);

		cacheConfig.setDiskStorePath(path);
		cacheConfig.setMaxElementsOnDisk(maxElementsOnDisk);
		cacheConfig.setEternal(eternal);
		cacheConfig.setMemoryStoreEvictionPolicy(policy.toString());
		cacheConfig.setOverflowToDisk(overflowToDisk);

		this.cache = new Cache(cacheConfig);

	}

	@Override
	public boolean contains(DataObject dataObject, String hash) {

		Element element = cache.getQuiet(hash);

		if (element == null)

			return false;

		else
			return true;
	}

	@Override
	public void cache(byte[] hashBytes, String hash) {

		Element element = new Element(hash, hashBytes);
		cache.put(element);

	}

	public String getURL(String hash) {

		return cacheConfig.getDiskStorePath() + "/" + hash;
	}

}
