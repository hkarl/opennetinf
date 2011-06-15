/**
 * 
 */
package netinf.node.transferDeluxe;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * @author indy
 *
 */
public class HTTPStreamProvider implements StreamProvider {

   @Override
   public InputStream getStream(String url) throws IOException {
         URL conn = new URL(url);
         return conn.openStream();
   }

}
