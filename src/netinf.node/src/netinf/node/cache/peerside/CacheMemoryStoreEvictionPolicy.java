package netinf.node.cache.peerside;

/**
 * the policy to evict objects out of the cache memory. In EhCache there are three such policies: LRU, LFU and FIFO.
 * 
 * @author PG NetInf 3, University of Paderborn
 */

public enum CacheMemoryStoreEvictionPolicy {

   LRU, LFU, FIFO
}
