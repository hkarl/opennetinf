package netinf.node.transferDeluxe.streamprovider;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author PG NetInf 3
 */
public interface StreamProvider {

   /**
    * Provides an appropriate stream for the fiven URL
    * 
    * @param url
    *           The URL of the file.
    * @return The Stream to that URL
    * @throws IOException
    *            When the URL can not be opened.
    */
   InputStream getStream(String url) throws IOException;

   /**
    * Decides wether this URL can be handled or not.
    * 
    * @param url
    *           The URL of the file.
    * @return True if the Provider can handle this URL, otherwise false.
    */
   boolean canHandle(String url);

   /**
    * Describes the Stream Provider.
    * 
    * @return The name of the Stream Provider.
    */
   String describe();
}