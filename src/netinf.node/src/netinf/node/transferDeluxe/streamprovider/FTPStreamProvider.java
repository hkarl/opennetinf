package netinf.node.transferDeluxe.streamprovider;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import netinf.node.chunking.Chunk;

/**
 * @author PG NetInf 3
 */
public class FTPStreamProvider implements StreamProvider {

   private String providerName = "FTP Streamprovider";
   
   @Override
   public InputStream getStream(String url) throws IOException {
      URL conn = new URL(url);
      return conn.openStream();
   }

   @Override
   public boolean canHandle(String url) {
      if (url == null) {
         return false;
      }
      
      if (url.startsWith("ftp://")) {
         return true;
      }
      
      return false;
   }

   @Override
   public String describe() {
      return providerName;
   }

   @Override
   public InputStream getStream(Chunk chunk, String baseUrl) throws IOException {
      // TODO Auto-generated method stub
      return new InputStream() {
         
         @Override
         public int read() throws IOException {
            return -1;
         }
      };
   }

}