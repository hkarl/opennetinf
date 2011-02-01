/*
 * Copyright (C) 2009,2010 University of Paderborn, Computer Networks Group
 * Copyright (C) 2009,2010 Christian Dannewitz <christian.dannewitz@upb.de>
 * Copyright (C) 2009,2010 Thorsten Biermann <thorsten.biermann@upb.de>
 * 
 * Copyright (C) 2009,2010 Eduard Bauer <edebauer@mail.upb.de>
 * Copyright (C) 2009,2010 Matthias Becker <matzeb@mail.upb.de>
 * Copyright (C) 2009,2010 Frederic Beister <azamir@zitmail.uni-paderborn.de>
 * Copyright (C) 2009,2010 Nadine Dertmann <ndertmann@gmx.de>
 * Copyright (C) 2009,2010 Michael Kionka <mkionka@mail.upb.de>
 * Copyright (C) 2009,2010 Mario Mohr <mmohr@mail.upb.de>
 * Copyright (C) 2009,2010 Felix Steffen <felix.steffen@gmx.de>
 * Copyright (C) 2009,2010 Sebastian Stey <sebstey@mail.upb.de>
 * Copyright (C) 2009,2010 Steffen Weber <stwe@mail.upb.de>
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of Paderborn nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package netinf.access;

import java.io.IOException;
import java.net.InetSocketAddress;

import netinf.common.communication.Communicator;
import netinf.common.exceptions.NetInfCheckedException;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * Provides access to a NetInf Node via HTTP
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class HTTPServer extends NetInfServer {
   private static final Logger LOG = Logger.getLogger(HTTPServer.class);
   private Provider<Communicator> communicatorProvider;

   private int port;
   private HttpServer server;

   @Inject
   public HTTPServer(@Named("access.http.port") int port) throws IOException {
      this.port = port;
   }

   @Inject
   public void injectProviderCommunicator(Provider<Communicator> provider) {
      this.communicatorProvider = provider;
   }

   @Override
   public void start() throws NetInfCheckedException {
      LOG.trace(null);

      try {
         // The second parameter represents the number of maximum tcp-connections.
         this.server = HttpServer.create(new InetSocketAddress(this.port), 10);
         this.server.createContext("/", new HTTPNetInfHandler());
      } catch (IOException e) {
         LOG.error("Error encountered while initializing the HTTPServer on port: " + this.port, e);
         throw new NetInfCheckedException(e);
      }

      // start server to listen for requests
      this.server.start();
   }

   @Override
   public void stop() {
      this.server.stop(0);
   }

   /**
    * The Class HTTPNetInfHandler.
    * 
    * @author PG Augnet 2, University of Paderborn
    */
   private class HTTPNetInfHandler implements HttpHandler {
	   
      @Override
      public void handle(HttpExchange httpExchange) throws IOException {
         LOG.trace(null);

         LOG.debug("Creating new HTTPServerConnection on the Server");
         HTTPServerConnection newConnection = new HTTPServerConnection(httpExchange);
         Communicator newCommunicator = HTTPServer.this.communicatorProvider.get();
         newCommunicator.setConnection(newConnection);

         startCommunicator(newCommunicator, false);
      }
   }

   @Override
   public String describe() {
      return "HTTP on port " + this.port;
   }
}
