package netinf.node.transferDeluxe.streamprovider;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

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
      if (url.startsWith("ftp://")) {
         return true;
      }
      return false;
   }

   @Override
   public String describe() {
      return providerName;
   }

}