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
package netinf.node.resolution.pastry.past;

import netinf.common.datamodel.InformationObject;
import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.IdFactory;
import rice.p2p.past.ContentHashPastContentHandle;
import rice.p2p.past.Past;
import rice.p2p.past.PastContent;
import rice.p2p.past.PastContentHandle;
import rice.p2p.past.PastException;

/**
 * The Class IOPastContent.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class IOPastContent implements PastContent {

   /**
    * 
    */
   private static final long serialVersionUID = 2542033889771560179L;

   private InformationObject io;
   private Id id;

   public IOPastContent(InformationObject io, Id id) {
      this.io = io;
      this.id = id;
   }

   public IOPastContent(InformationObject io, IdFactory idFactory) {
      this(io, idFactory.buildId(io.getIdentifier().serializeToBytes()));
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
      return !io.getIdentifier().isVersioned();
   }

   public InformationObject getInformationObject() {
      return io;
   }

   // private void writeObject(ObjectOutputStream out) throws IOException {
   // }
   //
   // private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
   //
   // }

}
