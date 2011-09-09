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
package netinf.node.transferdispatcher.streamprovider;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import netinf.common.utils.Utils;
import netinf.node.chunking.Chunk;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;

/**
 * StreamProvider for HTTP protocol
 * 
 * @author PG NetInf 3, University of Paderborn.
 */
public class HTTPStreamProvider implements StreamProvider {
   private static final Logger LOG = Logger.getLogger(HTTPStreamProvider.class);
   private String providerName = "HTTP Streamprovider";

   @Override
   public InputStream getStream(String url) throws IOException {
      URL conn = new URL(url);
      return conn.openStream();
   }

   @Override
   public InputStream getStream(Chunk chunk, String chunkUrl) throws IOException {
      // determine range
      int chunkSizeInBytes = 256 * 1024;
      int from = chunkSizeInBytes * chunk.getNumber();
      int to = (chunkSizeInBytes * (chunk.getNumber() + 1)) - 1;

      HttpClient client = new DefaultHttpClient();
      HttpGet httpGet = new HttpGet(chunkUrl);
      httpGet.setHeader("Range", "bytes=" + from + "-" + to);
      try {
         HttpResponse response = client.execute(httpGet);
         int status = response.getStatusLine().getStatusCode();
         if (status == HttpStatus.SC_PARTIAL_CONTENT) {

            InputStream inStream = response.getEntity().getContent();
            // check integrity
            String destination = Utils.getTmpFolder("chunkproviding") + File.separator + chunk.getHash() + ".tmp";
            boolean success = Utils.saveTemp(inStream, destination);

            if (success) {
               if (Utils.isValidHash(chunk.getHash(), destination)) {
                  return new FileInputStream(destination);
               } else {
                  throw new IOException("Hash of chunk no. " + chunk.getNumber() + " is NOT valid...");
               }
            }

         } else {
            throw new IOException("Error at ranges request... status: " + status);
         }
      } catch (ClientProtocolException e) {
         throw new IOException("Error at ranges request...");
      } catch (IOException e) {
         throw new IOException("Error at ranges request...: ");
      }

      return new InputStream() {
         @Override
         public int read() throws IOException {
            LOG.warn("(HTTPStreamProvider ) returning an error stream (-1)");
            return -1;
         }
      };
   }

   @Override
   public boolean canHandle(String url) {
      if (url.startsWith("http://")) {
         return true;
      }
      return false;
   }

   @Override
   public String describe() {
      return providerName;
   }

}
