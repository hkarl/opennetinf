/**
 * 
 */
package netinf.node.transferDeluxe.streamprovider;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * @author PG NetInf 3
 */
public class HTTPStreamProvider implements StreamProvider {

   @Override
   public InputStream getStream(String url) throws IOException {
      URL conn = new URL(url);
      return conn.openStream();
   }

   @Override
   public boolean canHandle(String url) {
      if (url.startsWith("http://")) {
         return true;
      }
      return false;
   }

}
