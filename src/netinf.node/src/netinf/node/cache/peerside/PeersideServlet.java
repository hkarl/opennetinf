package netinf.node.cache.peerside;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

/**
 * Simple HttpServlet serving elements stored in the PeersideCache. Allowed HTTP
 * methods are HEAD and GET. The Range header is supported as well.
 * 
 * @author PG NetInf 3, University of Paderborn
 */
public class PeersideServlet extends HttpServlet {

   private static final long serialVersionUID = 2029221260528771761L;
   private Cache cache;
   
   public PeersideServlet(Cache cache) {
      this.cache = cache;
   }
   
   private void doHeadOrGet(HttpServletRequest req, HttpServletResponse resp, boolean writeContent) throws IOException {
      String elementKey = req.getPathInfo();
      if (elementKey.startsWith("/") && elementKey.length() >= 1) {
         elementKey = elementKey.substring(1);
      }
      Element element = cache.get(elementKey);
      if (element == null) {
         resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
      } else {
         byte[] content = (byte[]) element.getValue();
         String range = req.getHeader("Range");
         if (range != null) {
            if (range.matches("^bytes=(\\d+-\\d*|-\\d+)$")) {
               int contentLength = content.length;
               int offset = 0;
               int length = contentLength;
               range = range.split("=")[1];
               if (range.startsWith("-")) {
                  offset = contentLength - Integer.parseInt(range.substring(1));
               } else if (range.endsWith("-")) {
                  offset = Integer.parseInt(range.substring(0, range.length()-1));
               } else {
                  String[] rangeParts = range.split("-");
                  offset = Integer.parseInt(rangeParts[0]);
                  length = Integer.parseInt(rangeParts[1]) + 1;
               }
               if (offset <= length && offset <= contentLength) {
                  length = length > contentLength ? contentLength : length;
                  resp.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
                  resp.setContentLength(length-offset);
                  resp.setHeader("Accept-Ranges", "bytes");
                  resp.setHeader("Content-Range", offset + "-" + (length-1) + "/" + contentLength);
                  if (writeContent) {
                     IOUtils.copy(new ByteArrayInputStream(content, offset, length-offset), resp.getOutputStream());
                  }
               } else {
                  resp.setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
               }
            } else {
               resp.setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
            }
         } else {
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentLength(content.length);
            resp.setHeader("Accept-Ranges", "bytes");
            if (writeContent) {
               IOUtils.copy(new ByteArrayInputStream(content), resp.getOutputStream());
            }
         }
      }
   }
   
   @Override
   protected void doHead(HttpServletRequest req, HttpServletResponse resp)
         throws ServletException, IOException {
      doHeadOrGet(req, resp, false);
   }
   
   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse resp)
         throws ServletException, IOException {
      doHeadOrGet(req, resp, true);
   }
}
