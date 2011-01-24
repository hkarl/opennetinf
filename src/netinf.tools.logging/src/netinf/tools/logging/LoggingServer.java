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
package netinf.tools.logging;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import netinf.common.log.demo.DemoLevel;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.net.SimpleSocketServer;
import org.apache.log4j.xml.DOMConfigurator;

/**
 * This is a logger server adjusted to NetInf. It uses the {@link LoggingConstants#APPLICATION_KEY} and the
 * {@link LoggingConstants#HOST_KEY}. It is inspired by {@link SimpleSocketServer}.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class LoggingServer extends Thread {

   private static final Logger LOG = Logger.getLogger(LoggingServer.class);

   private final int port;
   private final ServerSocket serverSocket;

   public LoggingServer(int port, String configurationFile) throws IOException {
      this.port = port;
      this.serverSocket = new ServerSocket(port);

      initConfigFile(configurationFile);
   }

   private void initConfigFile(String configFile) {
      if (configFile.endsWith(".xml")) {
         DOMConfigurator.configure(configFile);
      } else {
         PropertyConfigurator.configure(configFile);
      }
   }

   @Override
   public void run() {
      MDC.put(LoggingConstants.APPLICATION_KEY, LoggingConstants.LOGGING_APPLICATION_NAME);
      MDC.put(LoggingConstants.HOST_KEY, LoggingConstants.LOGGING_ADDRESS + LoggingConstants.HOSTNAME_PORT_SEPARATOR + this.port);
      LOG.log(DemoLevel.DEMO, "(LOG  ) Logging server started");
      while (true) {
         try {
            while (true) {
               LOG.debug("Waiting to accept a new client.");
               Socket socket = this.serverSocket.accept();

               InetSocketAddress remoteSocketAddress = (InetSocketAddress) socket.getRemoteSocketAddress();
               // String host = remoteSocketAddress.getHostName();
               String host = remoteSocketAddress.getAddress().getHostAddress();
               int port = remoteSocketAddress.getPort();
               String remoteClient = host + LoggingConstants.HOSTNAME_PORT_SEPARATOR + port;

               LOG.debug("Connected to client at " + remoteClient);
               LOG.debug("Starting new socket node.");
               LOG.log(DemoLevel.DEMO, "(LOG  ) Connection from " + remoteClient);

               new Thread(new LoggingSocketNode(socket, LogManager.getLoggerRepository())).start();
            }
         } catch (Exception e) {
            e.printStackTrace();
         }
      }
   }
}
