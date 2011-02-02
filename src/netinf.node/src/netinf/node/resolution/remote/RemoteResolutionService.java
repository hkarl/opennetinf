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
package netinf.node.resolution.remote;

import java.util.ArrayList;
import java.util.List;

import netinf.common.communication.NetInfDeletedIOException;
import netinf.common.communication.RemoteNodeConnection;
import netinf.common.communication.SerializeFormat;
import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.identity.ResolutionServiceIdentityObject;
import netinf.common.exceptions.NetInfCheckedException;
import netinf.common.exceptions.NetInfUncheckedException;
import netinf.node.resolution.AbstractResolutionService;
import netinf.node.resolution.ResolutionService;
import netinf.node.resolution.eventprocessing.EventPublisher;

import org.apache.log4j.Logger;

import com.google.inject.Inject;

/**
 * The Class RemoteResolutionService.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class RemoteResolutionService extends AbstractResolutionService implements ResolutionService {

   private static final Logger LOG = Logger.getLogger(RemoteResolutionService.class);

   private final RemoteNodeConnection communicator;

   private DatamodelFactory datamodelFactory;

   private String host;

   private Integer port;

   private boolean connected;

   @Inject
   public RemoteResolutionService(RemoteNodeConnection communicator) {
      super();
      this.communicator = communicator;
   }

   public void setUp(String host, Integer port, SerializeFormat serializeFormat) {
      LOG.trace(null);
      this.host = host;
      this.port = port;
      this.connected = true;
      this.communicator.setHostAndPort(host, port);
      this.communicator.setSerializeFormat(serializeFormat);
   }

   public void tearDown() {
      this.communicator.tearDown();
   }

   @Inject
   public void setDatamodelFactory(DatamodelFactory factory) {
      this.datamodelFactory = factory;
   }

   public boolean isConnected() {
      return this.connected;
   }

   @Override
   public void delete(Identifier identifier) {
      throw new UnsupportedOperationException("Deletion can by now not be done on remote resolution service");
   }

   @Override
   public InformationObject get(Identifier identifier) {
      try {
         return this.communicator.getIO(identifier);
      } catch (NetInfDeletedIOException ex) {
         // FIXME The information that the IO has been deleted is lost here!
         return null;
      } catch (NetInfCheckedException ex) {
         LOG.debug("Error getting the Information Object with Identifier: " + identifier, ex);
         throw new NetInfUncheckedException(ex);
      }
   }

   @Override
   public List<Identifier> getAllVersions(Identifier identifier) {
      try {
         List<Identifier> identifiers = new ArrayList<Identifier>();
         List<InformationObject> ios = this.communicator.getIOs(identifier);
         for (InformationObject io : ios) {
            identifiers.add(io.getIdentifier());
         }
         return identifiers;
      } catch (NetInfCheckedException ex) {
         LOG.debug("Error getting the Information Objects with Identifier: " + identifier, ex);
         throw new NetInfUncheckedException(ex);
      }
   }

   @Override
   public void put(InformationObject informationObject) {
      try {
         this.communicator.putIO(informationObject);
      } catch (NetInfCheckedException e) {
         LOG.debug("Error putting the Information Object" + informationObject, e);
         throw new NetInfUncheckedException(e);
      }

   }

   @Override
   protected ResolutionServiceIdentityObject createIdentityObject() {
      ResolutionServiceIdentityObject identity = this.datamodelFactory.createResolutionServiceIdentityObject();
      identity.setName("Remote Resolution Service: " + this.host + ':' + this.port);
      identity.setDescription("This Resolution service uses a remote resolution service");
      identity.setDefaultPriority(10);
      return identity;
   }

   /**
    * This method is not supported for RemoteResolutionService
    */
   @Override
   public void addEventService(EventPublisher eventPublisher) {
      throw new UnsupportedOperationException("RemoteResolutionService will not publish events");
   }

   public String getHost() {
      return this.host;
   }

   public Integer getPort() {
      return this.port;
   }

   @Override
   public String describe() {
      return "a TCP connection to the remote node " + this.host + ":" + this.port;
   }

}
