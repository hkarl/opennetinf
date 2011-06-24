/**
 * 
 */
package netinf.node.transferDeluxe.streamprovider;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author PG NetInf 3
 */
public interface StreamProvider {

   InputStream getStream(String url) throws IOException;

   boolean canHandle(String url);
}
