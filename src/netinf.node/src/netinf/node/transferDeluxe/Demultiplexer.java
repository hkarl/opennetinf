package netinf.node.transferDeluxe;

import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.Enumeration;

import netinf.common.log.demo.DemoLevel;
import netinf.node.chunking.ChunkedBO;

import org.apache.log4j.Logger;

/**
 * @author PG NetInf 3, University of Paderborn
 */
public class Demultiplexer {

   private static final Logger LOG = Logger.getLogger(Demultiplexer.class);

   public static InputStream getCombinedStream(ChunkedBO chunkedBO) {
      LOG.log(DemoLevel.DEMO, "(Demultiplexer ) Number of chunks: " + chunkedBO.getTotalNoOfChunks());
      Enumeration<InputStream> urlEnum = new URLStreamEnum(chunkedBO);
      return new SequenceInputStream(urlEnum);
   }

}