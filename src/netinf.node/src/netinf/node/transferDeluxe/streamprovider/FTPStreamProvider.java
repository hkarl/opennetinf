package netinf.node.transferDeluxe.streamprovider;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author PG NetInf 3
 */
public class FTPStreamProvider implements StreamProvider {

   private String providerName = "FTP Streamprovider";
   
   @Override
   public InputStream getStream(String url) throws IOException {
      URL conn = new URL(url);
      URLConnection urlc = conn.openConnection();
      return urlc.getInputStream();
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