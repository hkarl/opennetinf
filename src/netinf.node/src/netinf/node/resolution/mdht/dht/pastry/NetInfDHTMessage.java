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
package netinf.node.resolution.mdht.dht.pastry;

import java.io.IOException;
import java.io.Serializable;

import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.Message;
import rice.p2p.commonapi.NodeHandle;
import rice.p2p.commonapi.rawserialization.OutputBuffer;
import rice.p2p.util.rawserialization.JavaSerializer;

/**
 * @author PG NetInf3
 */
public class NetInfDHTMessage implements Message, Serializable {

   private static final long serialVersionUID = 6466970856585627245L;

   // Pastry ID of the sender node
   private Id from;

   // Pastry ID of the target node
   private Id to;

   public NetInfDHTMessage(NodeHandle from, Id to) {
      this.from = from.getId();
      this.to = to;
   }

   /*
    * (non-Javadoc)
    * @see rice.p2p.commonapi.Message#getPriority()
    */
   @Override
   public int getPriority() {
      return Message.DEFAULT_PRIORITY;
   }

   public void serialize(OutputBuffer buf) throws IOException {
      JavaSerializer.serialize(this, buf);
   }

   @Override
   public String toString() {
      return "NetInfDHTMessage from " + this.from + " destined for " + this.to;
   }
}
