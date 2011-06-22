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
package netinf.node.resolution.local;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import netinf.common.datamodel.DatamodelFactory;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.identity.ResolutionServiceIdentityObject;
import netinf.common.datamodel.translation.DatamodelTranslator;
import netinf.common.exceptions.NetInfResolutionException;
import netinf.node.resolution.AbstractResolutionService;
import netinf.node.resolution.ResolutionService;

import org.apache.log4j.Logger;

import rice.Continuation.ExternalContinuation;
import rice.p2p.commonapi.IdFactory;
import rice.persistence.Storage;

import com.google.inject.Inject;

/**
 * Local resolution service
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class LocalResolutionService extends AbstractResolutionService implements ResolutionService {

   private static final Logger LOG = Logger.getLogger(LocalResolutionService.class);

   private final Storage storage;

   private DatamodelFactory datamodelFactory;

   private DatamodelTranslator translator;

   @Inject
   public LocalResolutionService(Storage storage, IdFactory idFactory) {
      super();
      this.storage = storage;
      setIdFactory(idFactory);

   }

   @Inject
   public void setDatamodelFactory(DatamodelFactory factory) {
      this.datamodelFactory = factory;
   }

   @Inject
   public void setDatamodelTranslator(DatamodelTranslator translator) {
      this.translator = translator;
   }

   @Override
   public void delete(Identifier id) {
      LOG.trace(null);
      Identifier identifier = this.translator.toImpl(id);
      LOG.info("Deleting IO with identifier");
      if (identifier.isVersioned()) {
         throw new NetInfResolutionException("Cannot delete versioned IO");
      }
      InformationObject ioToDelete = get(identifier);

      ExternalContinuation<Object, Exception> command = new ExternalContinuation<Object, Exception>();
      this.storage.unstore(buildId(identifier), command);
      command.sleep();
      if (command.getException() != null) {
         LOG.warn("Error deleting Object", command.getException());
         throw new NetInfResolutionException(command.getException());
      }
      publishDelete(ioToDelete);
   }

   @Override
   public InformationObject get(Identifier identifier) {
      LOG.info("Getting IO with identifier " + identifier.toString());
      Identifier idToLookup = getIdToLookup(identifier);
      Object result = getObject(idToLookup);
      if (result instanceof InformationObject || result == null) {
         return (InformationObject) result;
      } else {
         LOG.error("Received wrong type from store");
         throw new NetInfResolutionException("Received wrong type from store");
      }
   }

   private Object getObject(Identifier identifier) {
      LOG.trace(null);
      Identifier id = this.translator.toImpl(identifier);
      ExternalContinuation<Object, Exception> command = new ExternalContinuation<Object, Exception>();
      this.storage.getObject(buildId(id), command);
      command.sleep();
      if (command.getException() != null) {
         LOG.warn("Error getting Object", command.getException());
         throw new NetInfResolutionException(command.getException());
      }
      return command.getResult();
   }

   @SuppressWarnings("unchecked")
   @Override
   public List<Identifier> getAllVersions(Identifier id) {
      LOG.trace(null);
      Identifier identifier = this.translator.toImpl(id);
      if (!identifier.isVersioned()) {
         throw new NetInfResolutionException("Trying to get versions for unversioned identifier");
      }
      ExternalContinuation<List<Identifier>, Exception> command = new ExternalContinuation<List<Identifier>, Exception>();
      Identifier idWithoutVersion = createIdentifierWithoutVersion(identifier);
      this.storage.getObject(buildId(idWithoutVersion), command);
      command.sleep();
      if (command.getException() != null) {
         LOG.warn("Error getting Object", command.getException());
         throw new NetInfResolutionException(command.getException());
      }
      return (List<Identifier>) command.getResult();

   }

   @SuppressWarnings("unchecked")
   @Override
   public void put(InformationObject io) {
      LOG.trace(null);
      InformationObject informationObject = this.translator.toImpl(io);
      try {
         validateIOForPut(informationObject);
      } catch (IllegalArgumentException ex) {
         throw new NetInfResolutionException("Trying to put invalid Information Object", ex);
      }

      // check if this is a creating or modifying put
      InformationObject oldIo = get(informationObject.getIdentifier());
      boolean modifyingPut;
      if (oldIo == null) {
         modifyingPut = false;
      } else {
         modifyingPut = true;
      }

      putObject(buildId(informationObject), (Serializable) informationObject, buildId(informationObject));
      if (informationObject.getIdentifier().isVersioned()) {
         Identifier idWithoutVersion = createIdentifierWithoutVersion(informationObject.getIdentifier());
         List<Identifier> idList = (List<Identifier>) getObject(idWithoutVersion);
         if (idList == null) {
            idList = new ArrayList<Identifier>();
         }
         idList.add(informationObject.getIdentifier());
         putObject(buildId(idWithoutVersion), (Serializable) idList, buildId(idWithoutVersion));
      }

      if (modifyingPut) {
         publishPut(oldIo, informationObject);
      } else {
         publishPut(null, informationObject);
      }
   }

   private void putObject(rice.p2p.commonapi.Id id, Serializable object, Serializable metadata) {
      LOG.trace(null);
      ExternalContinuation<Object, Exception> storeCommand = new ExternalContinuation<Object, Exception>();
      this.storage.store(id, metadata, object, storeCommand);
      storeCommand.sleep();
      if (storeCommand.exceptionThrown()) {
         LOG.warn("Erorr during storage of IO", storeCommand.getException());
         throw new NetInfResolutionException(storeCommand.getException());
      }

      if (!(Boolean) storeCommand.getResult()) {
         LOG.warn("Could not save Object");
         throw new NetInfResolutionException("Could not save IO");
      }
   }

   @Override
   protected ResolutionServiceIdentityObject createIdentityObject() {
      ResolutionServiceIdentityObject identity = this.datamodelFactory
            .createDatamodelObject(ResolutionServiceIdentityObject.class);
      identity.setName("LocalResolutionService");
      identity.setDefaultPriority(100);
      identity.setDescription("This is a local resolution Service");
      return identity;
   }

   @Override
   public String describe() {
      return "a local, volatile Pastry storage";
   }

}
