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

import java.io.IOException;
import java.io.InputStream;

import netinf.node.chunking.Chunk;

/**
 * Interface for all StreamProviders (FTP, HTTP,...)
 * 
 * @author PG NetInf 3, University of Paderborn.
 */
public interface StreamProvider {

   /**
    * Provides an appropriate stream for the given URL.
    * 
    * @param url
    *           The URL of the file.
    * @return The Stream to that URL
    * @throws IOException
    *            When the URL can not be opened.
    */
   InputStream getStream(String url) throws IOException;

   /**
    * Provides an appropriate stream for the given Chunk.
    * 
    * @param chunk
    *           The Chunk.
    * @param chunkUrl
    *           The BaseURL to the Chunk.
    * @return The Stream to that single Chunk.
    * @throws IOException
    */
   InputStream getStream(Chunk chunk, String chunkUrl) throws IOException;

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
