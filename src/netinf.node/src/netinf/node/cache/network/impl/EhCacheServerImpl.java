package netinf.node.cache.network.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * Ehcache-Server adapter for NetInfCache
 * 
 * @author PG NetInf 3, University of Paderborn
 */
public class EhCacheServerImpl implements CacheServer {

   private static final Logger LOG = Logger.getLogger(EhCacheServerImpl.class);
   private String cacheAddress = "";
   private boolean isConnected;

   /**
    * Constructor
    * 
    * @param host
    *           address where the server is hosted
    */
   @Inject
   public EhCacheServerImpl(@Named("cache_address") final String host, @Named("cache_port") final String port,
         @Named("cache_tablename") final String tablename) {
      // create address of cache
      cacheAddress = buildCacheAddress(host, port, tablename);

      // create/check Ehcache tables
      if (!cacheExists(cacheAddress)) {
         boolean success = createCache(cacheAddress);
         if (success) {
            isConnected = true;
         } else {
            isConnected = false;
         }
      } else {
         isConnected = true;
      }
   }

   @Override
   public boolean cacheBO(byte[] bo, String hashOfBO) {
      if (isConnected()) {
         HttpClient client = new DefaultHttpClient();
         HttpPut httpPut = new HttpPut(cacheAddress + "/" + hashOfBO);
         ByteArrayEntity entity = new ByteArrayEntity(bo);
         httpPut.setEntity(entity);
         try {
            // execute request
            HttpResponse response = client.execute(httpPut);
            int status = response.getStatusLine().getStatusCode();
            if (status == HttpStatus.SC_CREATED || status == HttpStatus.SC_OK) {
               return true;
            }
            return false;
         } catch (ClientProtocolException e) {
            LOG.error("ProtocolException in EhCache");
            return false;
         } catch (IOException e) {
            LOG.error("IOException in EhCache");
            return false;
         }
      }

      return false; // not connected
   }

   @Override
   public byte[] getBO(String hashOfBO) {
      if (isConnected()) {
         HttpClient client = new DefaultHttpClient();
         HttpGet httpGet = new HttpGet(cacheAddress + "/" + hashOfBO);
         try {
            // execute request
            HttpResponse response = client.execute(httpGet);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
               // TODO: eddy, replace with IOUtils?
               InputStream is = entity.getContent(); // message body
               ByteArrayOutputStream byteOS = new ByteArrayOutputStream();
               byte[] bo = null;
               try {
                  // getting BO;
                  int line = 0;
                  while ((line = is.read()) != -1) {
                     byteOS.write((char) line);
                  }
                  bo = byteOS.toByteArray();
               } finally {
                  is.close();
                  byteOS.close();
               }
               return bo;
            }
         } catch (ClientProtocolException e) {
            LOG.error("ProtocolException in EhCache");
            return null;
         } catch (IOException e) {
            LOG.error("IOException in EhCache");
            return null;
         }
      }

      return null; // not connected
   }

   @Override
   public boolean containsBO(String hashOfBO) {
      if (isConnected()) {
         HttpClient client = new DefaultHttpClient();
         HttpHead httpHead = new HttpHead(cacheAddress + "/" + hashOfBO);
         try {
            HttpResponse response = client.execute(httpHead);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_NOT_FOUND) {
               return false;
            }
            LOG.info("Cache contains BO with hash: " + hashOfBO);
            return true;
         } catch (ClientProtocolException e) {
            LOG.error("ProtocolException in EhCache");
            return false;
         } catch (IOException e) {
            LOG.error("IOException in EhCache");
            return false;
         }
      }
      return false; // not connected
   }

   @Override
   public boolean isConnected() {
      if (isConnected) {
         return true;
      }
      return false;
   }

   /**
    * checks if the cache exists
    * 
    * @param pathToCache
    *           URL to the desired cache
    * @return true if the cache exists, otherwise false
    */
   private boolean cacheExists(String pathToCache) {
      HttpClient client = new DefaultHttpClient();
      HttpHead httpHead = new HttpHead(pathToCache);
      try {
         HttpResponse response = client.execute(httpHead);
         int statusCode = response.getStatusLine().getStatusCode();
         LOG.debug("(EhCache ) Status of cache: " + statusCode);
         if (statusCode == HttpStatus.SC_NOT_FOUND) {
            LOG.info("Cache server running - tables do not exist");
            return false;
         }
         LOG.info("Cache server running - tables exist");
         return true;
      } catch (ClientProtocolException e) {
         LOG.error("ProtocolException in EhCache");
         return false;
      } catch (IOException e) {
         LOG.error("IOException - Cache server not running...");
         return false;
      }
   }

   /**
    * Creates the necessary Ehcache tables
    * 
    * @param pathOfCache
    *           URL to the cache
    */
   private boolean createCache(String pathOfCache) {
      HttpClient client = new DefaultHttpClient();
      HttpPut httpPut = new HttpPut(pathOfCache);
      try {
         HttpResponse response = client.execute(httpPut);
         int statusCode = response.getStatusLine().getStatusCode();
         if (statusCode == HttpStatus.SC_CREATED) {
            LOG.info("Cache server running - created cache tables");
            return true;
         }
         return false;
      } catch (ClientProtocolException e) {
         LOG.error("ProtocolException in EhCache");
         return false;
      } catch (IOException e) {
         LOG.error("IOException - Cache server not running...");
         return false;
      }
   }

   /**
    * Build the whole address to the cache
    * 
    * @param host
    *           the address of the host that runs the cache server
    * @return the URL of the cache
    */
   private String buildCacheAddress(String host, String port, String tablename) {
      String cacheUrl = "http://" + host + ":" + port;
      try {
         URL url = new URL(cacheUrl);
         LOG.info("Using cache server on IP: " + url.getHost());
      } catch (MalformedURLException e) {
         LOG.warn("Wrong cache address - trying with localhost...");
         cacheUrl = "http://127.0.0.1:8080";
      }

      return cacheUrl + tablename;
   }

   @Override
   public String getURL(String hashOfBO) {
      if (isConnected()) {
         return cacheAddress + "/" + hashOfBO;
      }
      return null;
   }

   @Override
   public String getAddress() {
      return cacheAddress;
   }

}
