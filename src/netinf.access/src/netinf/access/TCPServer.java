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
import java.net.ServerSocket;
import java.net.Socket;

import javax.sql.ConnectionEvent;

import netinf.common.communication.Communicator;
import netinf.common.communication.TCPConnection;
import netinf.common.exceptions.NetInfCheckedException;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

/**
 * Provides access to a NetInf Node via TCP
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class TCPServer extends NetInfServer {
   private static final Logger LOG = Logger.getLogger(TCPServer.class);
   private Provider<Communicator> communicatorProvider;

   private ServerSocket serverSocket;
   private ConnectionListener connectionListener;

   private final int port;

   @Inject
   public TCPServer(@Named("access.tcp.port") int port) {
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
         this.serverSocket = new ServerSocket(this.port);
      } catch (IOException e) {
         LOG.error("Error encountered while initializing the TCPServer on port: " + this.port, e);
         throw new NetInfCheckedException(e);
      }

      this.connectionListener = new ConnectionListener();
      this.connectionListener.start();
   }

   @Override
   public void stop() throws IOException {
      LOG.trace(null);

      this.connectionListener.interrupt();
      try {
    	  if(this.serverSocket != null)
    		  this.serverSocket.close();
      } catch (IOException e) {
         throw e;
      }
   }

   public int getPort() {
      return this.port;
   }

   /**
    * The listener interface for receiving connection events. The class that is interested in processing a connection event
    * implements this interface, and the object created with that class is registered with a component using the component's
    * <code>addConnectionListener<code> method. When
    * the connection event occurs, that object's appropriate
    * method is invoked.
    * 
    * @see ConnectionEvent
    * @author PG Augnet 2, University of Paderborn
    */
   class ConnectionListener extends Thread {
      private boolean running;

      public ConnectionListener() {
         this.running = true;
      }

      @Override
      public void run() {
         LOG.trace(null);

         try {
            LOG.debug("Starting to listen for new connection within the TCPServer on port "
                  + TCPServer.this.serverSocket.getLocalPort());

            while (this.running) {
               LOG.debug("In listen loop");
               Socket socket = TCPServer.this.serverSocket.accept();
               LOG.debug("Accepted new connection.");
               TCPConnection newConnection = new TCPConnection(socket);
               Communicator newCommunicator = TCPServer.this.communicatorProvider.get();
               newCommunicator.setConnection(newConnection);

               startCommunicator(newCommunicator, true);
            }
         } catch (IOException e) {
            LOG.error("The TCP Server encountered an error", e);
         } finally {
            try {
               TCPServer.this.serverSocket.close();
            } catch (IOException e) {
               LOG.error("The TCP Server encountered an error while closing", e);
            }
         }
      }

      @Override
      public void interrupt() {
         LOG.trace(null);

         this.running = false;
         super.interrupt();
      }
   }

   public String getAddress() {
      return this.serverSocket.getInetAddress().toString();
   }

   @Override
   public String describe() {
      return "TCP on port " + this.port;
   }
}
