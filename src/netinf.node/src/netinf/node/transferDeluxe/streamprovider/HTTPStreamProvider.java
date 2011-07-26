/**
 * 
 */
package netinf.node.transferDeluxe.streamprovider;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import netinf.common.utils.Utils;
import netinf.node.chunking.Chunk;

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
      int from = chunkSizeInBytes * chunk.getNumber();
      int to = (chunkSizeInBytes * (chunk.getNumber() + 1)) - 1;

      HttpClient client = new DefaultHttpClient();
      HttpGet httpGet = new HttpGet(baseUrl);
      httpGet.setHeader("Range", "bytes=" + from + "-" + to);
      try {
         HttpResponse response = client.execute(httpGet);
         int status = response.getStatusLine().getStatusCode();
         if (status == HttpStatus.SC_PARTIAL_CONTENT) {

            InputStream inStream = response.getEntity().getContent();
            // check integrity
            String destination = Utils.getTmpFolder("chunkproviding") + File.separator + chunk.getHash() + ".tmp";
            boolean success = Utils.saveTemp(inStream, destination);

            if (success) {
               if (Utils.isValidHash(chunk.getHash(), destination)) {
                  return new FileInputStream(destination);
               } else {
                  throw new IOException("Hash of chunk no. " + chunk.getNumber() + " is NOT valid...");
               }
            }

         } else {
            throw new IOException("Error at ranges request... status: " + status);
         }
      } catch (ClientProtocolException e) {
         throw new IOException("Error at ranges request...");
      } catch (IOException e) {
         throw new IOException("Error at ranges request...: ");
      }

      return new InputStream() {
         @Override
         public int read() throws IOException {
            LOG.warn("(HTTPStreamProvider ) returning an error stream (-1)");
            return -1;
         }
      };
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
