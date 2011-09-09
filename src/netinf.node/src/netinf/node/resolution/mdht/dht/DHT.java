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
package netinf.node.resolution.mdht.dht;

import rice.p2p.commonapi.Id;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.InformationObject;

/**
 * Generic interface which specifies common functionalities for all used DHT systems.
 * 
 * @author PG NetInf 3
 */
public interface DHT {

   /**
    * Attempts to join a DHT. If boot address is the same as the node's own address then the node will initialize a new ring.
    * 
    * @throws InterruptedException
    */
   void join() throws InterruptedException;

   /**
    * Attempts to put the given InformationObject into the DHT.
    * 
    * @param io
    *           The InformationObject to be stored
    */
   void put(InformationObject io, int level, int maxlevels, byte[] sourceAddress);

   /**
    * Attempts to get the InformationObject by the specified NetInf Identifier.
    * 
    * @param id
    *           The NetInf Identifier of the desired InformationObject
    * @param level
    *           The level/ring at which this request should take place
    * @return The corresponding InformationObject if stored in DHT, {@code null} otherwise
    */
   InformationObject get(Identifier id, int level);

   /**
    * Attempts to get the InformationObject by the specified Commonapi Identifier.
    * 
    * @param id
    *           The Id (e.g. FreePastry ID) of the desired InformationObject
    * @param level
    *           The level/ring at which this request should take place
    * @return The corresponding InformationObject if stored in DHT, {@code null} otherwise
    */
   InformationObject get(Id id, int level);

   /**
    * Leave the current DHT.
    */
   void leave();
}
