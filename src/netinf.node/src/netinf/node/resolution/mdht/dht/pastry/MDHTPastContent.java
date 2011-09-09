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

import netinf.common.datamodel.InformationObject;
import rice.p2p.commonapi.Id;
import rice.p2p.past.ContentHashPastContentHandle;
import rice.p2p.past.Past;
import rice.p2p.past.PastContent;
import rice.p2p.past.PastContentHandle;
import rice.p2p.past.PastException;

/***
 * The extension of the PastContent object to use with own NetInfPast. This represents the IOs stored in the MDHT
 * 
 * @author PG NetInf 3
 * @since May 2011
 */
public class MDHTPastContent implements PastContent {

   private static final long serialVersionUID = -1631388806403566496L;
   private Id id;
   private InformationObject informationObject;

   public MDHTPastContent(Id id, InformationObject io) {
      this.id = id;
      this.informationObject = io;
   }

   @Override
   public PastContent checkInsert(Id id, PastContent existingContent) throws PastException {
      return this;
   }

   @Override
   public PastContentHandle getHandle(Past local) {
      return new ContentHashPastContentHandle(local.getLocalNodeHandle(), getId());
   }

   @Override
   public Id getId() {
      return id;
   }

   @Override
   public boolean isMutable() {
      return !informationObject.getIdentifier().isVersioned();
   }

   public InformationObject getInformationObject() {
      return informationObject;
   }
}
