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

import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.attribute.Attribute;
import netinf.common.datamodel.attribute.DefinedAttributeIdentification;
import netinf.node.transferDeluxe.streamprovider.FTPStreamProvider;
import netinf.node.transferDeluxe.streamprovider.HTTPStreamProvider;
import netinf.node.transferDeluxe.streamprovider.NetInfNoStreamProviderFoundException;
import netinf.node.transferDeluxe.streamprovider.StreamProvider;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/**
 * @author PG NetInf 3
 * @pat.name Singleton
 * @pat.task Forces that only one instance of this class exists
 */
public final class TransferDispatcher {

   private static final Logger LOG = Logger.getLogger(TransferDispatcher.class);
   private List<StreamProvider> streamProviders;

   // singleton
   private static TransferDispatcher instance;

   private TransferDispatcher() {
      addStreamProviders();
   }

   private void addStreamProviders() {
      streamProviders = new ArrayList<StreamProvider>();

      streamProviders.add(new FTPStreamProvider());
      streamProviders.add(new HTTPStreamProvider());
   }

   public static synchronized TransferDispatcher getInstance() {
      if (instance == null) {
         instance = new TransferDispatcher();
      }
      return instance;
   }

   public InputStream getStream(String url) throws IOException, NetInfNoStreamProviderFoundException {
      LOG.info("(TransferDispatcher ) Getting Transfer-Stream from: " + url);
      StreamProvider dl = getStreamProvider(url);
      return dl.getStream(url);
   }

   public InputStream getStream(InformationObject io) throws Exception {
      // check if chunks exist
      List<Attribute> chunks = getChunkList(io);
      // prefer chunks
      if (chunks != null) {
         Demultiplexer demu = new Demultiplexer(chunks);
         return demu.getInputs();
      } else {
         // Use normal locators
         LocatorSelector locSel = new LocatorSelector(io);
         while (locSel.hasNextLocator()) {
            try {

               return this.getStream(locSel.getNextLocator());

            } catch (NetInfNoStreamProviderFoundException e) {
               LOG.warn("(TransferDispatcher ) NoStreamProviderFoundException: " + e.getMessage());
            } catch (IOException e) {
               LOG.warn("(TransferDispatcher ) IOException: " + e.getMessage());
            }
         }

      }

      // TODO: custom Exception
      throw new Exception();
   }

   private List<Attribute> getChunkList(InformationObject io) {
      // TODO: check better, chunk list cemplete, ?
      List<Attribute> allChunks = io.getAttribute(DefinedAttributeIdentification.CHUNK.getURI());
      if (allChunks.size() > 0) {
         return allChunks;
      }
      return null;
   }

   public void getStreamAndSave(String url, String destination, boolean withContentType)
         throws NetInfNoStreamProviderFoundException, IOException {
      LOG.info("(TransferDispatcher ) Starting Download from: " + url);
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

         IOUtils.copy(is, dos);

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
            LOG.info("(TransferDispatcher ) Using " + sp.describe());
            return sp;
         }
      }

      // no StreamProvider found
      throw new NetInfNoStreamProviderFoundException(url + "not supported");
   }

}