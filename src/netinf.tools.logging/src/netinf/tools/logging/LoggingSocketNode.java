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
package netinf.tools.logging;

import java.net.InetSocketAddress;
import java.net.Socket;

import org.apache.log4j.MDC;
import org.apache.log4j.net.SocketNode;
import org.apache.log4j.spi.LoggerRepository;

/**
 * A special {@link SocketNode}, which puts the address of the remote connection into the thread local properties. By that each
 * connection, and accordingly each running application, is uniquely identified. The thread local properties are described at
 * {@link MDC}.
 * 
 * @author PG Augnet 2, University of Paderborn
 * 
 */
public class LoggingSocketNode extends SocketNode {

   private final Socket mySocket;

   public LoggingSocketNode(Socket socket, LoggerRepository hierarchy) {
      super(socket, hierarchy);
      this.mySocket = socket;
   }

   @Override
   public void run() {
      InetSocketAddress remoteSocketAddress = (InetSocketAddress) this.mySocket.getRemoteSocketAddress();
      // String host = remoteSocketAddress.getHostName();
      String host = remoteSocketAddress.getAddress().getHostAddress();
      int port = remoteSocketAddress.getPort();

      MDC.put(LoggingConstants.HOST_KEY, host + LoggingConstants.HOSTNAME_PORT_SEPARATOR + port);
      super.run();
   }
}
