/**
 * 
 */
package netinf.node.transferDeluxe;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author indy
 *
 */
public class TransferDispatcher {
   
   /**
    * 
    * @param url
    * @return
    * @throws IOException 
    */
   public InputStream getStream(String url) throws IOException{
      StreamProvider dl = getStreamProvider(url);
      if (dl != null) {
         return dl.getStream(url);
      } else {
         throw new IOException();
      }
   }
   
   StreamProvider getStreamProvider(String url){
      if (this.isHTTP(url)) {
         return new HTTPStreamProvider();
      } else if (isFTP(url)) {
         return new FTPStreamProvider();
      }
      
      return null;
   }
   
   /**
    * 
    * @param url
    * @return
    */
   boolean isHTTP(String url){
      if (url.startsWith("http://")) { // regex!?
         return true;
      }
      return false;
   }
   
   /**
    * 
    * @param url
    * @return
    */
   boolean isFTP(String url){
      if (url.startsWith("ftp://")) { // regex!?
         return true;
      }
      return false;
   }
   
}

