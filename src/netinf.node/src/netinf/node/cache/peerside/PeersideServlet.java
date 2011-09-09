/*
 * Copyright (C) 2009-2011 University of Paderborn, Computer Networks Group
 * (Full list of owners see http://www.netinf.org/about-2/license)
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * 
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *       this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of Paderborn nor the names of its contributors may be used to endorse
 *       or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDERS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package netinf.node.cache.peerside;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.apache.commons.io.IOUtils;

/**
 * Simple HttpServlet serving elements stored in the PeersideCache. Allowed HTTP methods are HEAD and GET. The Range header is
 * supported as well.
 * 
 * @author PG NetInf 3, University of Paderborn.
 */
public class PeersideServlet extends HttpServlet {

   private static final long serialVersionUID = 2029221260528771761L;
   private Cache cache;

   /**
    * Constructor.
    * 
    * @param cache
    *           The adapted cache.
    */
   public PeersideServlet(Cache cache) {
      this.cache = cache;
   }

   /**
    * HEAD and GET operations.
    * 
    * @param req
    *           The Request.
    * @param resp
    *           The response.
    * @param writeContent
    * @throws IOException
    */
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
                  offset = Integer.parseInt(range.substring(0, range.length() - 1));
               } else {
                  String[] rangeParts = range.split("-");
                  offset = Integer.parseInt(rangeParts[0]);
                  length = Integer.parseInt(rangeParts[1]) + 1;
               }
               if (offset <= length && offset <= contentLength) {
                  length = length > contentLength ? contentLength : length;
                  resp.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
                  resp.setContentLength(length - offset);
                  resp.setHeader("Accept-Ranges", "bytes");
                  resp.setHeader("Content-Range", offset + "-" + (length - 1) + "/" + contentLength);
                  if (writeContent) {
                     IOUtils.copy(new ByteArrayInputStream(content, offset, length - offset), resp.getOutputStream());
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
   protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
      doHeadOrGet(req, resp, false);
   }

   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
      doHeadOrGet(req, resp, true);
   }

   @Override
   protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
      String elementKey = req.getPathInfo();
      if (elementKey.startsWith("/") && elementKey.length() >= 1) {
         elementKey = elementKey.substring(1);
      }
      if (elementKey.equals("*")) {
         cache.removeAll();
      } else {
         cache.remove(elementKey);
      }
      resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
   }
}
