/**
 * 
 */
package netinf.node.transferDeluxe;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import netinf.node.transferDeluxe.streamprovider.FTPStreamProvider;
import netinf.node.transferDeluxe.streamprovider.HTTPStreamProvider;
import netinf.node.transferDeluxe.streamprovider.NetInfNoStreamProviderFoundException;
import netinf.node.transferDeluxe.streamprovider.StreamProvider;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/**
 * @author PG NetInf 3
 */
public class TransferDispatcher {

   private static final Logger LOG = Logger.getLogger(TransferDispatcher.class);
   private List<StreamProvider> streamProviders;

   public TransferDispatcher() {
      streamProviders = new ArrayList<StreamProvider>();

      // add available StreamProviders
      streamProviders.add(new HTTPStreamProvider());
      streamProviders.add(new FTPStreamProvider());
   }

   public InputStream getStream(String url) throws IOException, NetInfNoStreamProviderFoundException {
      LOG.info("Getting Stream from: " + url);
      StreamProvider dl = getStreamProvider(url);
      return dl.getStream(url);
   }

   public void getStreamAndSave(String url, String destination, boolean withContentType)
         throws NetInfNoStreamProviderFoundException, IOException {
      LOG.info("Starting Download from: " + url);
      InputStream is = getStream(url);
      DataOutputStream dos = null;
      try {
         dos = new DataOutputStream(new FileOutputStream(destination));

         // only for BOCaching and HTTPFileServer
         if (withContentType) {
            URL ur = new URL(url);
            URLConnection urc = ur.openConnection();
            byte[] contentTypeBytes = urc.getContentType().getBytes();
            dos.writeInt(contentTypeBytes.length);
            dos.write(contentTypeBytes);
         }

         byte[] buffer = new byte[4096];
         int readBytes = -1;
         while ((readBytes = is.read(buffer)) != -1) {
            dos.write(buffer, 0, readBytes);
         }
      } catch (MalformedURLException e) {
         LOG.warn("Could not download data from: " + url);
      } catch (IOException e) {
         LOG.warn("Could not download data from: " + url);
      } finally {
         IOUtils.closeQuietly(is);
         IOUtils.closeQuietly(dos);
      }
   }

   StreamProvider getStreamProvider(String url) throws NetInfNoStreamProviderFoundException {
      for (StreamProvider sp : streamProviders) {
         if (sp.canHandle(url)) {
            return sp;
         }
      }

      // no StreamProvider found
      throw new NetInfNoStreamProviderFoundException(url + "not supported");
   }

}
