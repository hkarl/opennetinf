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

import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.NodeHandle;
import rice.p2p.past.messaging.LookupMessage;

/***
 * Custom LookupMessage for the NetInfPast application. 
 * Triggered when an object (IO) is sought for in the PAST layer.
 * For usage see the @see get(Identifier id, int level) method of FreePastryDHT.
 * @author PG NetInf3, University of Paderborn
 * @since 2011
 */
public class NetInfLookupMessage extends LookupMessage {

   private static final long serialVersionUID = 6146689282945987009L;
   private int level;

   /**
    * Constructs a NetInfLookupMessage with the specified options.
    * @param uid The UID of the PAST application. @see NetInfPast lookup for an example.
    * @param id The Pastry (commonAPI) Id of the stored content.
    * @param source The NodeHandle of the node starting the search.
    * @param dest The Pastry (commonAPI) Id to be searched for. ATTENTION: This can be a content ID!
    * @param level The MDHT level on which the message is created.
    */
   public NetInfLookupMessage(int uid, Id id, NodeHandle source, Id dest, int level) {
      super(uid, id, source, dest);
      this.level = level;
   }

   /**
    * Get the level at which this IO is stored.
    * @return The level number. Numbering starts at 0.
    */
   public int getLevel() {
      return level;
   }

}
