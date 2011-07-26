package netinf.node.transferDeluxe;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import netinf.common.datamodel.DataObject;
import netinf.common.log.demo.DemoLevel;
import netinf.node.chunking.Chunk;
import netinf.node.chunking.ChunkedBO;
import netinf.node.chunking.NetInfNotChunkableException;
import netinf.node.transferDeluxe.chunkstreams.ConcurrentChunkStream;
import netinf.node.transferDeluxe.chunkstreams.SequentialChunkStream;
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
   private static TransferDispatcher instance;
   private int chunkingTechnique = 1;

   private TransferDispatcher() {
      addStreamProviders();
   }

   private void addStreamProviders() {
      streamProviders = new ArrayList<StreamProvider>();

      streamProviders.add(new FTPStreamProvider());
      streamProviders.add(new HTTPStreamProvider());
   }

   /**
    * Provides the singleton instance of a TransferDispatcher.
    * 
    * @return The TransferDispatcher
    */
   public static synchronized TransferDispatcher getInstance() {
      if (instance == null) {
         instance = new TransferDispatcher();
      }
      return instance;
   }

   public InputStream getStream(String url) throws IOException, NetInfNoStreamProviderFoundException {
      LOG.log(DemoLevel.DEMO, "(TransferDispatcher ) Getting Transfer-Stream from: " + url);
      StreamProvider dl = getStreamProvider(url);
      return dl.getStream(url);
   }

   public InputStream getStream(Chunk chunk, String baseUrl) throws IOException, NetInfNoStreamProviderFoundException {
      LOG.log(DemoLevel.DEMO, "(TransferDispatcher ) Getting Transfer-Stream for Chunk from: " + baseUrl);
      StreamProvider dl = getStreamProvider(baseUrl);
      return dl.getStream(chunk, baseUrl);
   }

   public InputStream getStream(DataObject dataObj) throws IOException {
      LOG.log(DemoLevel.DEMO, "(TransferDispatcher ) Getting Transfer-Stream from IO: " + dataObj.getIdentifier());

      // try to use at first chunks/ranges
      try {
         ChunkedBO chunkedBO = new ChunkedBO(dataObj);
         LOG.log(DemoLevel.DEMO, "(TransferDispatcher ) Chunks exist, use them...");

         switch (chunkingTechnique) {
         case 1:
            LOG.info("(TransferDispatcher ) Using ConcurrentChunkStream...");
            return new ConcurrentChunkStream(chunkedBO);
         case 2:
            LOG.info("(TransferDispatcher ) SequentialChunkStream...");
            return new SequentialChunkStream(chunkedBO);
         default:
            LOG.info("(TransferDispatcher ) Nothing selected... Using ConcurrentChunkStream");
            return new ConcurrentChunkStream(chunkedBO);
         }

      } catch (NetInfNotChunkableException e1) {
         LOG.info("(TransferDispatcher ) Chunking can not be used for this DO: " + e1.getMessage());
      }

      // then try normal locators
      LOG.info("(TransferDispatcher ) Try to get stream over normal locators");
      LocatorSelector locSel = new LocatorSelector(dataObj);
      while (locSel.hasNext()) {
         try {
            return this.getStream(locSel.next());
         } catch (NetInfNoStreamProviderFoundException e) {
            LOG.warn("(TransferDispatcher ) NoStreamProviderFoundException: " + e.getMessage());
         } catch (IOException e) {
            LOG.warn("(TransferDispatcher ) IOException: " + e.getMessage());
         }
      }

      throw new IOException("Stream could not be provided");
   }

   public void getStreamAndSave(String url, String destination, boolean withContentType)
         throws NetInfNoStreamProviderFoundException, IOException {

      File file = new File(destination);
      if (file.exists() && file.isFile()) {
         return;
      }

      LOG.log(DemoLevel.DEMO, "(TransferDispatcher ) Starting Download from: " + url);
      InputStream is = getStream(url);
      DataOutputStream dos = null;
      try {
         dos = new DataOutputStream(new FileOutputStream(destination));
         IOUtils.copy(is, dos);
      } catch (Exception e) {
         LOG.warn("(TransferDispatcher ) Could not download data from: " + url + " - " + e.getMessage());
      } finally {
         IOUtils.closeQuietly(is);
         IOUtils.closeQuietly(dos);
      }
   }

   /**
    * Provides the appropriate StreamProvider.
    * 
    * @param url
    *           The URL to which a stream shall be povided.
    * @return The specific StreamProvider.
    * @throws NetInfNoStreamProviderFoundException
    *            If no provider could be found.
    */
   StreamProvider getStreamProvider(String url) throws NetInfNoStreamProviderFoundException {
      for (StreamProvider sp : streamProviders) {
         if (sp.canHandle(url)) {
            LOG.log(DemoLevel.DEMO, "(TransferDispatcher ) Using " + sp.describe());
            return sp;
         }
      }
      throw new NetInfNoStreamProviderFoundException(url + " not supported");
   }

}