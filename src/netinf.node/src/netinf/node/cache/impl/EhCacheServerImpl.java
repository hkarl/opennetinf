package netinf.node.cache.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;

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
   public EhCacheServerImpl(InetAddress host) {
      // create address of cache
      cacheAddress = buildCacheAddress(host);

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
            if (status == HttpStatus.SC_CREATED) {
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
            return false;
         }
         return true;
      } catch (ClientProtocolException e) {
         LOG.error("ProtocolException in EhCache");
         return false;
      } catch (IOException e) {
         LOG.error("IOException in EhCache");
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

   /**
    * Build the whole address to the cache
    * 
    * @param host
    *           the address of the host that runs the cache server
    * @return the URL of the cache
    */
   private String buildCacheAddress(InetAddress host) {
      return "http://" + host.getHostAddress() + ":8080/ehcache/rest/netinf";
   }

}
