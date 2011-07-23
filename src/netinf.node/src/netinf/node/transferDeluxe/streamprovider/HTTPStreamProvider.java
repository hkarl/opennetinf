/**
 * 
 */
package netinf.node.transferDeluxe.streamprovider;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import netinf.common.security.Hashing;
import netinf.common.utils.Utils;
import netinf.node.chunking.Chunk;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;

/**
 * @author PG NetInf 3
 */
public class HTTPStreamProvider implements StreamProvider {
   private static final Logger LOG = Logger.getLogger(HTTPStreamProvider.class);
   private String providerName = "HTTP Streamprovider";

   @Override
   public InputStream getStream(String url) throws IOException {
      URL conn = new URL(url);
      return conn.openStream();
   }

   @Override
   public InputStream getStream(Chunk chunk, String baseUrl) throws IOException {
      // determine range
      int chunkSizeInBytes = 256 * 1024;
      int from = chunkSizeInBytes * (chunk.getNumber() - 1);
      int to = (chunkSizeInBytes * chunk.getNumber()) - 1;

      HttpClient client = new DefaultHttpClient();
      HttpGet httpGet = new HttpGet(baseUrl); // TODO: handling of last chunk...
      httpGet.setHeader("Range", "bytes=" + from + "-" + to); // TODO !!!!
      try {
         HttpResponse response = client.execute(httpGet);
         int status = response.getStatusLine().getStatusCode();
         if (status == HttpStatus.SC_PARTIAL_CONTENT) {
            return response.getEntity().getContent();
            
            // TODO: check integrity
            // check integrity
            // InputStream inStream = response.getEntity().getContent();
            // if (!isValid(chunk.getHash(), inStream)) {
            // throw new IOException("Hash is not valid...");
            // }
            // return stream
         } else {
            throw new IOException("Error at ranges request...");
         }
      } catch (ClientProtocolException e) {
         throw new IOException("Error at ranges request...");
      } catch (IOException e) {
         throw new IOException("Error at ranges request...");
      }
   }

   private boolean isValid(String hashOfBO, InputStream inStream) {
      try {
         byte[] hashBytes = Hashing.hashSHA1(inStream);
         IOUtils.closeQuietly(inStream);

         if (hashOfBO.equalsIgnoreCase(Utils.hexStringFromBytes(hashBytes))) {
            return true;
         }
      } catch (IOException e) {
         e.printStackTrace();
      }
      return false;
   }

   @Override
   public boolean canHandle(String url) {
      if (url.startsWith("http://")) {
         return true;
      }
      return false;
   }

   @Override
   public String describe() {
      return providerName;
   }

}
