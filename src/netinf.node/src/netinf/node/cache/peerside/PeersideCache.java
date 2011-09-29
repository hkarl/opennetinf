/*
 * Copyright (C) 2009-2011 University of Paderborn, Computer Networks Group
 * (Full list of owners see http://www.netinf.org/about-2/license)
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * 
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *       this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of Paderborn nor the names of its contributors may be used to endorse
 *       or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDERS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package netinf.node.cache.peerside;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.Status;
import netinf.node.access.AccessServer;
import netinf.node.cache.BOCacheServer;

import org.apache.log4j.Logger;

/**
 * The PeersideCache.
 * 
 * @author PG NetInf 3, University of Paderborn.
 */
public class PeersideCache implements BOCacheServer {

   private static final Logger LOG = Logger.getLogger(PeersideCache.class);

   private Cache cache;
   private CacheManager manager;
   private final String ehcacheConfigPath = "../configs/PeersideEhcacheConfig.xml";

   private AccessServer accessServer;
   private String host;
   private int port;

   private int mdhtScope;

   /**
    * Constructor
    */
   public PeersideCache(int scope, String host, int port) {

      // create manager with default config and init cache
      manager = CacheManager.create(ehcacheConfigPath);
      cache = manager.getCache("PeersideCache");

      // register shutdown listener
      Runtime.getRuntime().addShutdownHook(new Thread() {
         @Override
         public void run() {
            LOG.info("(Ehcache ) shutdown!");
            manager.shutdown();
         }
      });

      mdhtScope = scope;

      this.host = host;
      this.port = port;
      accessServer = new PeersideAccessServer(cache, port);
      accessServer.start();
   }

   @Override
   public boolean containsBO(String hash) {
      boolean flag = cache.isKeyInCache(hash);
      LOG.info("(PeersideCache ) BO already in cache: " + flag);
      return cache.isKeyInCache(hash);
   }

   @Override
   public boolean cacheBO(byte[] bo, String hashOfBO) {
      Element element = new Element(hashOfBO, bo);
      try {
         cache.put(element);
         return true;
      } catch (Exception ex) {
         LOG.warn("Put not succeeded");
         return false;
      }
   }

   @Override
   public String getURL(String hash) {
      return getAddress() + "/" + hash;
   }

   @Override
   public boolean isConnected() {
      if (cache.getStatus().equals(Status.STATUS_ALIVE)) {
         return true;
      }
      return false;
   }

   @Override
   public String getAddress() {
      return "http://" + host + ":" + port;
   }

   @Override
   public int getScope() {
      return mdhtScope;
   }

}
