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
package netinf.node.resolution.remote.gp;

import java.util.Hashtable;
import java.util.List;

import netinf.common.communication.SerializeFormat;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.NetInfObjectWrapper;
import netinf.common.datamodel.identity.ResolutionServiceIdentityObject;
import netinf.common.exceptions.NetInfUncheckedException;
import netinf.node.api.NetInfNode;
import netinf.node.gp.datamodel.Capability;
import netinf.node.gp.datamodel.Resolution;
import netinf.node.resolution.ResolutionService;
import netinf.node.resolution.eventprocessing.EventPublisher;
import netinf.node.resolution.remote.RemoteResolutionService;
import netinf.node.resolution.remote.gp.capabilities.CapabilityDeterminator;
import netinf.node.resolution.remote.gp.selector.ResolutionSelector;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * The gp resolution service tries to resolve prior to issueing the according request. In case exactly the same request was
 * already made, the second one is ignored. This means, i.e. if a get-request (containing an identifier) was send to a specific
 * host and exactly the same request would be have to be send a second time, the second request is ignored. This prevents two
 * nodes from live-locking, were they ask each other infinitely recursively.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class GPResolutionService implements ResolutionService {

   private static final Logger LOG = Logger.getLogger(GPResolutionService.class);

   private static final String DESTINATION_NAME = "*";
   private final CapabilityDeterminator capabilityDeterminator;
   private final NetInfNode netInfNode;
   private final ResolutionSelector resolutionSelector;

   private final Provider<RemoteResolutionService> remoteResolutionServiceProvider;
   private final Hashtable<Integer, RemoteResolutionService> runningRequests;

   @Inject
   public GPResolutionService(CapabilityDeterminator capabilityDeterminator, NetInfNode netInfNode,
         ResolutionSelector resolutionSelector, Provider<RemoteResolutionService> remoteRSProvider) {
      this.capabilityDeterminator = capabilityDeterminator;
      this.netInfNode = netInfNode;
      this.resolutionSelector = resolutionSelector;
      this.remoteResolutionServiceProvider = remoteRSProvider;
      runningRequests = new Hashtable<Integer, RemoteResolutionService>();
   }

   @Override
   public void delete(Identifier identifier) {
      LOG.trace(null);
      List<Capability> capabilities = this.capabilityDeterminator.getCapabilitiesForDelete();
      List<Resolution> resolutions = this.netInfNode.resolveCapabilities(capabilities, DESTINATION_NAME);

      Resolution resolutionForDelete = this.resolutionSelector.getResolutionForDelete(resolutions);
      String targetAddress = resolutionForDelete.getTargetAddress();

      if (getRemoteResolutionService(identifier, targetAddress) == null) {
         LOG.debug("Found a resolution service to use. Using that resolution service for deleting information object");

         RemoteResolutionService remoteResolutionService = createRemoteResolutionService(identifier, targetAddress);
         remoteResolutionService.delete(identifier);

         // Delete the remoteResolutionService finally
         removeRemoteResolutionService(identifier, targetAddress);
      } else {
         // A request is currently running, do not issue a second one.
         LOG.debug("A request to '" + targetAddress + "' for '" + identifier.toString() + " is currently running");
         return;
      }
   }

   @Override
   public InformationObject get(Identifier identifier) {
      LOG.trace(null);
      List<Capability> capabilities = this.capabilityDeterminator.getCapabilitiesForGet();
      List<Resolution> resolutions = this.netInfNode.resolveCapabilities(capabilities, DESTINATION_NAME);

      Resolution resolutionForGet = this.resolutionSelector.getResolutionForGet(resolutions);
      String targetAddress = resolutionForGet.getTargetAddress();

      if (getRemoteResolutionService(identifier, targetAddress) == null) {
         LOG.debug("Found a resolution service to use. Using that resolution service for getting information object");

         RemoteResolutionService remoteResolutionService = createRemoteResolutionService(identifier, targetAddress);
         InformationObject result = remoteResolutionService.get(identifier);

         // Delete the remoteResolutionService finally
         removeRemoteResolutionService(identifier, targetAddress);

         return result;
      } else {
         // A request is currently running, do not issue a second one.
         LOG.debug("A request to '" + targetAddress + "' for '" + identifier.toString() + " is currently running");
         return null;
      }
   }

   @Override
   public List<Identifier> getAllVersions(Identifier identifier) {
      LOG.trace(null);
      List<Capability> capabilities = this.capabilityDeterminator.getCapabilitiesForGet();
      List<Resolution> resolutions = this.netInfNode.resolveCapabilities(capabilities, DESTINATION_NAME);

      Resolution resolutionForGet = this.resolutionSelector.getResolutionForGet(resolutions);
      String targetAddress = resolutionForGet.getTargetAddress();

      if (getRemoteResolutionService(identifier, targetAddress) == null) {
         LOG.debug("Found a resolution service to use. Using that resolution service for getting information object");

         RemoteResolutionService remoteResolutionService = createRemoteResolutionService(identifier, targetAddress);
         List<Identifier> result = remoteResolutionService.getAllVersions(identifier);

         // Delete the remoteResolutionService finally
         removeRemoteResolutionService(identifier, targetAddress);

         return result;
      } else {
         // A request is currently running, do not issue a second one.
         LOG.debug("A request to '" + targetAddress + "' for '" + identifier.toString() + " is currently running");
         return null;
      }
   }

   @Override
   public void put(InformationObject informationObject) {
      LOG.trace(null);
      List<Capability> capabilities = this.capabilityDeterminator.getCapabilitiesForPut();
      List<Resolution> resolutions = this.netInfNode.resolveCapabilities(capabilities, DESTINATION_NAME);

      Resolution resolutionForPut = this.resolutionSelector.getResolutionForPut(resolutions);

      String targetAddress = resolutionForPut.getTargetAddress();

      if (getRemoteResolutionService(informationObject, targetAddress) == null) {
         LOG.debug("Found a resolution service to use. Using that resolution service for putting information object");

         RemoteResolutionService remoteResolutionService = createRemoteResolutionService(informationObject, targetAddress);
         remoteResolutionService.put(informationObject);

         // Delete the remoteResolutionService finally
         removeRemoteResolutionService(informationObject, targetAddress);
      } else {
         // A request is currently running, do not issue a second one.
         LOG.debug("A request to '" + targetAddress + "' for '" + informationObject + " is currently running");
         return;
      }
   }

   /*
    * It is never possible to use an event service for the ResolutionServiceGP
    * @see netinf.node.resolution.ResolutionService#addEventService(netinf.node.resolution.eventprocessing.EventPublisher)
    */
   @Override
   public void addEventService(EventPublisher eventPublisher) {
   }

   @Override
   public ResolutionServiceIdentityObject getIdentity() {
      // TODO: We should decide one identity object for all the local resolutionService,
      return null;
   }

   /**
    * Assumes always that <code>address</code> is of the kind host:port, with the colon as the delimiter.
    * 
    * @param address
    *           Assumes always that <code>address</code> is of the kind host:port, with the colon as the delimiter.
    * @return two element containing array, return[0] ist the host name, return[1] is the port.
    */
   private String[] parseAddress(String address) {
      int indexOf = address.indexOf(":");

      if (indexOf <= 0) {
         throw new NetInfUncheckedException("Could not parse the address");
      }

      String[] result = new String[2];

      result[0] = address.substring(0, indexOf);
      result[1] = address.substring(indexOf + 1);

      return result;
   }

   private synchronized RemoteResolutionService createRemoteResolutionService(NetInfObjectWrapper netInfObjectWrapper,
         String address) {
      LOG.trace(null);
      RemoteResolutionService remoteResolutionService = setupRemoteResolutionService(address, netInfObjectWrapper
            .getDatamodelFactory().getSerializeFormat());
      int realhash = createHash(netInfObjectWrapper, address);
      runningRequests.put(realhash, remoteResolutionService);

      return remoteResolutionService;
   }

   private synchronized RemoteResolutionService getRemoteResolutionService(NetInfObjectWrapper netInfObjectWrapper, String address) {
      int realhash = createHash(netInfObjectWrapper, address);

      return runningRequests.get(realhash);
   }

   private synchronized void removeRemoteResolutionService(NetInfObjectWrapper netInfObjectWrapper, String address) {
      LOG.trace(null);

      int realhash = createHash(netInfObjectWrapper, address);
      RemoteResolutionService remoteResolutionService = runningRequests.get(realhash);

      if (remoteResolutionService != null) {
         LOG.debug("Removing remote Resolution Service for '" + netInfObjectWrapper + "' to address '" + address + "'");
         tearDownRemoteResolutionService(remoteResolutionService);
      }
   }

   private RemoteResolutionService setupRemoteResolutionService(String address, SerializeFormat serializeFormat) {
      LOG.trace("Setting up remote connection to '" + address + "'.");
      String[] parseAddress = parseAddress(address);

      RemoteResolutionService remoteResolutionService = remoteResolutionServiceProvider.get();
      remoteResolutionService.setUp(parseAddress[0], Integer.parseInt(parseAddress[1]), serializeFormat);

      return remoteResolutionService;
   }

   private void tearDownRemoteResolutionService(RemoteResolutionService remoteResolutionService) {
      String host = remoteResolutionService.getHost();
      Integer integer = remoteResolutionService.getPort();

      if (host != null && integer != null) {
         remoteResolutionService.tearDown();
      }
   }

   private int createHash(NetInfObjectWrapper netInfObjectWrapper, String address) {
      int hashCode = netInfObjectWrapper.hashCode();
      int hashCode2 = address.hashCode();

      int realhash = hashCode ^ hashCode2;
      return realhash;
   }

   @Override
   public String describe() {
      return "Generic Path";
   }
}
