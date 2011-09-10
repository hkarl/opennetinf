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

import java.net.InetAddress;

import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.NodeHandle;
import rice.p2p.past.PastContent;
import rice.p2p.past.messaging.InsertMessage;

/***
 * Custom InsertMessage for the NetInfPast application. 
 * Triggered when an object (IO) is inserted in the PAST layer.
 * The MDHT system currently takes care of putting an IO at every level, which is why a new NetInfInsertMessage
 * will be created. @see MDHTResolutionService put method for more details.
 * @author PG NetInf3, University of Paderborn
 * @since 2011
 */
public class NetInfInsertMessage extends InsertMessage {

   private static final long serialVersionUID = 283952122270522339L;
   /**
    * The address of the source node. Used for sending the ACK message after storing IO.
    */
   private InetAddress sourceAddr;
   
   /**
    * Parameter indicating the level AT WHICH the message should be inserted.
    */
   private int level;
   
   /**
    * Parameter indicating the level UP TO WHICH this message is to be stored.
    */
   private int maxlevels;

   
   public NetInfInsertMessage(int uid, PastContent content, NodeHandle source, Id dest, InetAddress sAddr, int level,
         int maxLevels) {
      super(uid, content, source, dest);
      this.sourceAddr = sAddr;
      this.level = level;
      this.maxlevels = maxLevels;
   }

   /**
    * Get the source address.
    * @return The address of the sending node.
    */
   public InetAddress getAddress() {
      return this.sourceAddr;
   }

   /**
    * Get the level at which this IO is stored.
    * @return The level number. Numbering starts at 0.
    */
   public int getLevel() {
      return this.level;
   }

   /**
    * Get the maximum number of levels in the MDHT.
    * @return The stored max number of levels. 
    */
   public int getMaxLevels() {
      return this.maxlevels;
   }

}
