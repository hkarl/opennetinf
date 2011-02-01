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
package netinf.node.resolution.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import netinf.common.datamodel.DeleteMode;
import netinf.common.datamodel.Identifier;
import netinf.common.datamodel.InformationObject;
import netinf.common.datamodel.attribute.Attribute;
import netinf.common.datamodel.attribute.DefinedAttributeIdentification;
import netinf.common.datamodel.identity.ResolutionServiceIdentityObject;
import netinf.common.exceptions.NetInfResolutionException;
import netinf.common.exceptions.NetInfUncheckedException;
import netinf.common.log.demo.DemoLevel;
import netinf.common.messages.NetInfMessage;
import netinf.common.messages.RSGetRequest;
import netinf.common.messages.RSGetResponse;
import netinf.common.messages.RSGetServicesRequest;
import netinf.common.messages.RSGetServicesResponse;
import netinf.common.messages.RSPutRequest;
import netinf.common.messages.RSPutResponse;
import netinf.common.security.SecurityManager;
import netinf.node.resolution.ResolutionController;
import netinf.node.resolution.ResolutionInterceptor;
import netinf.node.resolution.ResolutionService;
import netinf.node.resolution.ResolutionServiceSelector;

import org.apache.log4j.Logger;

import com.google.inject.Inject;

/**
 * This is a representative implementation of the {@link ResolutionController}.
 * 
 * @see ResolutionController
 * @author PG Augnet 2, University of Paderborn
 */
public class ResolutionControllerImpl implements ResolutionController {

   private static final Logger LOG = Logger.getLogger(ResolutionControllerImpl.class);

   private final Map<ResolutionServiceIdentityObject, ResolutionService> identityToResolutionServices;

   private final List<ResolutionInterceptor> interceptors;

   private final ResolutionServiceSelector resolutionServiceSelector;

   private final SecurityManager securityManager;

   private ArrayList<Class<? extends NetInfMessage>> supportedOperations;

   @Inject
   public ResolutionControllerImpl(ResolutionServiceSelector selector, SecurityManager securityManager) {
      this.identityToResolutionServices = new HashMap<ResolutionServiceIdentityObject, ResolutionService>();
      this.resolutionServiceSelector = selector;
      this.interceptors = new ArrayList<ResolutionInterceptor>();
      this.securityManager = securityManager;
   }

   @Override
   public void delete(Identifier identifier) {
      List<ResolutionServiceIdentityObject> resolutionServicesToUse = this.resolutionServiceSelector.getRSForDelete();
      delete(identifier, resolutionServicesToUse);
   }

   @Override
   public void delete(Identifier identifier, List<ResolutionServiceIdentityObject> resolutionServicesToUse) {
      boolean deletedAtLeastOnce = false;

      for (ResolutionServiceIdentityObject identity : resolutionServicesToUse) {
         ResolutionService rs = this.identityToResolutionServices.get(identity);
         if (rs == null) {
            throw new NetInfResolutionException("The requested resolution service is not"
                  + " registered in the resolution controller");
         }
         rs.delete(identifier);
         deletedAtLeastOnce = true;
      }

      if (!deletedAtLeastOnce) {
         throw new NetInfUncheckedException("Could not delete the information object");
      }
   }

   @Override
   public InformationObject get(Identifier identifier) {
      List<ResolutionServiceIdentityObject> resolutionServicesToUse = this.resolutionServiceSelector.getRSForGet();
      return get(identifier, resolutionServicesToUse, null, null);
   }

   @Override
   public InformationObject get(Identifier identifier, String userName, String privateKey) {
      List<ResolutionServiceIdentityObject> resolutionServicesToUse = this.resolutionServiceSelector.getRSForGet();
      return get(identifier, resolutionServicesToUse, userName, privateKey);
   }

   @Override
   public InformationObject get(Identifier identifier, List<ResolutionServiceIdentityObject> resolutionServicesToUse) {
      return get(identifier, resolutionServicesToUse, null, null);
   }

   @Override
   public InformationObject get(Identifier identifier, List<ResolutionServiceIdentityObject> resolutionServicesToUse,
         String userName, String privateKey) {
      boolean gotAtLeastOne = false;

      for (ResolutionServiceIdentityObject identity : resolutionServicesToUse) {
         ResolutionService rs = this.identityToResolutionServices.get(identity);
         if (rs == null) {
            throw new NetInfResolutionException("The requested resolution service is not "
                  + "registered in the resolution controller");
         }
         gotAtLeastOne = true;
         InformationObject io = null;
         try {
            LOG.log(DemoLevel.DEMO, "(NODE ) Trying to resolve over " + rs.describe());
            io = rs.get(identifier);
         } catch (Exception ex) {
            LOG.warn("Could not use Resolution service " + rs + " Exception: " + ex);
         }

         if (io != null) {
            try {
               io = this.securityManager.checkIncommingInformationObject(io, userName, privateKey);

               for (ResolutionInterceptor interceptor : this.interceptors) {
                  io = interceptor.interceptGet(io);
               }
               return io;
            } catch (Exception e) {
               LOG.warn("Security properties of IO could not be verified successfully. " + e.getMessage(), e);
            }
         }
      }

      if (!gotAtLeastOne) {
         throw new NetInfUncheckedException("No resolution service found to resolve the query");
      }

      return null;
   }

   @Override
   public List<InformationObject> getAllVersions(Identifier identifier) {
      List<ResolutionServiceIdentityObject> resolutionServicesToUse = this.resolutionServiceSelector.getRSForGet();
      return getAllVersions(identifier, resolutionServicesToUse);
   }

   @Override
   public List<InformationObject> getAllVersions(Identifier identifier,
         List<ResolutionServiceIdentityObject> resolutionServicesToUse) {
      Set<Identifier> identifiers = new HashSet<Identifier>();
      List<InformationObject> ios = new ArrayList<InformationObject>();
      for (ResolutionServiceIdentityObject identity : resolutionServicesToUse) {
         ResolutionService rs = this.identityToResolutionServices.get(identity);
         if (rs == null) {
            throw new NetInfResolutionException("The requested resolution service is not"
                  + "registered in the resolution controller");
         }
         List<Identifier> identitiesToGet = rs.getAllVersions(identifier);
         for (Identifier ident : identitiesToGet) {
            if (!identifiers.contains(ident)) {
               ios.add(rs.get(ident));
            }
         }
         identifiers.addAll(rs.getAllVersions(identifier));
      }
      return ios;
   }

   @Override
   public List<ResolutionServiceIdentityObject> getResolutionServices() {
      return new ArrayList<ResolutionServiceIdentityObject>(this.identityToResolutionServices.keySet());
   }

   @Override
   public ArrayList<Class<? extends NetInfMessage>> getSupportedOperations() {
      LOG.trace(null);

      if (this.supportedOperations == null) {
         this.supportedOperations = new ArrayList<Class<? extends NetInfMessage>>();
         this.supportedOperations.add(RSGetRequest.class);
         this.supportedOperations.add(RSPutRequest.class);
         this.supportedOperations.add(RSGetServicesRequest.class);
      }

      return this.supportedOperations;
   }

   @Override
   public void put(InformationObject informationObject) {
      List<ResolutionServiceIdentityObject> resolutionServicesToUse = this.resolutionServiceSelector.getRSForPut();
      put(informationObject, resolutionServicesToUse, null, null);
   }

   @Override
   public void put(InformationObject informationObject, String userName, String privateKey) {
      List<ResolutionServiceIdentityObject> resolutionServicesToUse = this.resolutionServiceSelector.getRSForPut();
      put(informationObject, resolutionServicesToUse, userName, privateKey);
   }

   @Override
   public void put(InformationObject informationObject, List<ResolutionServiceIdentityObject> resolutionServicesToUse) {
      put(informationObject, resolutionServicesToUse, null, null);
   }

   @Override
   public void put(InformationObject informationObject, List<ResolutionServiceIdentityObject> resolutionServicesToUse,
         String userName, String privateKey) {
      try {
         informationObject = this.securityManager.checkOutgoingInformationObject(informationObject, userName, privateKey);
      } catch (Exception e) {
         LOG.warn("Security properties of IO to push could not be verified successfully. " + e.getMessage());
         throw new NetInfUncheckedException("The information object could not be stored, due to security property issues");
      }

      // If it is a delete command, all previous versions are deleted.
      List<Attribute> deleteAttr = informationObject.getAttribute(DefinedAttributeIdentification.DELETE.getURI());
      if (!deleteAttr.isEmpty() && deleteAttr.get(0).getValue(String.class).equals(DeleteMode.DELETE_DATA.getMode())) {
         delete(informationObject.getIdentifier(), resolutionServicesToUse);
      } else {
         // The actual putting
         boolean putAtLeastOnce = false;

         for (ResolutionServiceIdentityObject rsIdentity : resolutionServicesToUse) {
            ResolutionService rs = this.identityToResolutionServices.get(rsIdentity);
            if (rs == null) {
               throw new NetInfResolutionException(
               "The requested resolution service is not registered in the resolution controller");
            }
            try {
               rs.put(informationObject);
               putAtLeastOnce = true;
            } catch (Exception ex) {
               LOG.warn("Could not use Resolution service " + rs + " Exception: " + ex);
            }

         }

         if (!putAtLeastOnce) {
            throw new NetInfUncheckedException("The information object could not be stored");
         }
      }
   }

   @Override
   public void addResolutionService(ResolutionService resolutionService) {
      this.resolutionServiceSelector.addResolutionService(resolutionService.getIdentity());
      this.identityToResolutionServices.put(resolutionService.getIdentity(), resolutionService);
   }

   @Override
   public void removeResolutionService(ResolutionService resolutionService) {
      this.identityToResolutionServices.remove(resolutionService);
   }

   @Override
   public void removeResolutionService(ResolutionServiceIdentityObject resolutionServiceInformation) {
      this.identityToResolutionServices.remove(resolutionServiceInformation);

   }

   @Override
   public NetInfMessage processNetInfMessage(NetInfMessage netInfMessage) {
      LOG.trace(null);

      if (netInfMessage instanceof RSPutRequest) {
         RSPutRequest rsGetRequest = (RSPutRequest) netInfMessage;
         return processRSPutRequest(rsGetRequest);
      }
      if (netInfMessage instanceof RSGetRequest) {
         RSGetRequest rsGetRequest = (RSGetRequest) netInfMessage;
         return processRSGetRequest(rsGetRequest);
      }
      if (netInfMessage instanceof RSGetServicesRequest) {
         RSGetServicesRequest rsGetServicesRequest = (RSGetServicesRequest) netInfMessage;
         return processRSGetServicesRequest(rsGetServicesRequest);
      }

      return null;
   }

   private NetInfMessage processRSGetServicesRequest(RSGetServicesRequest rsGetServicesRequest) {
      LOG.trace(null);

      RSGetServicesResponse rsGetServicesResponse = new RSGetServicesResponse();
      List<ResolutionServiceIdentityObject> resolutionServices = getResolutionServices();

      for (ResolutionServiceIdentityObject resolutionServiceIdentityObject : resolutionServices) {
         if (resolutionServiceIdentityObject != null) {

            Identifier identifier = resolutionServiceIdentityObject.getIdentifier();
            rsGetServicesResponse.addResolutionService(identifier);
         }
      }

      return null;
   }

   private NetInfMessage processRSPutRequest(RSPutRequest rsPutRequest) {
      LOG.trace(null);

      InformationObject informationObject = rsPutRequest.getInformationObject();
      Identifier identifier = rsPutRequest.getInformationObject().getIdentifier();

      RSPutResponse rsPutResponse = new RSPutResponse();

      // try {
      // informationObject = securityManager.checkOutgoingInformationObject(informationObject, rsPutRequest.getUserName(),
      // rsPutRequest.getPrivateKey());
      // } catch (Exception e) {
      // LOG.warn("Security properties of IO to push could not be verified successfully. Identifier: " + identifier);
      // rsPutResponse.setErrorMessage("Security properties of IO to push could not be verified successfully. Identifier: "
      // + identifier);
      // return rsPutResponse;
      // }
      // TODO: We must somehow find out, whether the above statements were correctly done, or not (Ede)

      try {

         // Delete moved into put by Felix
         // List<Attribute> deleteAttr = informationObject.getAttribute(DefinedAttributeIdentification.DELETE.getURI());
         // if (!deleteAttr.isEmpty() && deleteAttr.get(0).getValue(String.class).equals(DeleteMode.DELETE_DATA.getMode())) {
         //
         // // Felix: tztz didn't check security before deleting IOs. This whole section should be moved into put
         // try {
         // informationObject = securityManager.checkIncommingInformationObject(informationObject, false);
         // } catch (Exception e) {
         // LOG.warn("Security properties of IO could not be verified successfully. " + e.getMessage());
         // }
         // List<ResolutionServiceIdentityObject> resolutionServicesToUse = rsPutRequest.getResolutionServicesToUse();
         // if (resolutionServicesToUse == null || resolutionServicesToUse.isEmpty()) {
         // delete(identifier);
         // } else {
         // this.delete(identifier, resolutionServicesToUse);
         // }
         // } else {
         // Normal put
         List<ResolutionServiceIdentityObject> resolutionServicesToUse = rsPutRequest.getResolutionServicesToUse();
         if (resolutionServicesToUse == null || resolutionServicesToUse.isEmpty()) {
            put(informationObject, rsPutRequest.getUserName(), rsPutRequest.getPrivateKey());
         } else {
            put(informationObject, rsPutRequest.getResolutionServicesToUse(), rsPutRequest.getUserName(), rsPutRequest
                  .getPrivateKey());
         }
         // }

      } catch (NetInfUncheckedException e) {
         LOG.error("Could not store InformationObject with Identifier: " + identifier, e);

         rsPutResponse.setErrorMessage("Could not store the InformationObject with the Identifier: " + identifier);
      }

      return rsPutResponse;
   }

   private NetInfMessage processRSGetRequest(RSGetRequest rsGetRequest) {
      LOG.trace(null);

      Identifier identifier = rsGetRequest.getIdentifier();
      String userName = rsGetRequest.getUserName();
      String privateKey = rsGetRequest.getPrivateKey();

      InformationObject informationObject = null;

      List<ResolutionServiceIdentityObject> resolutionServicesToUse = rsGetRequest.getResolutionServicesToUse();
      if (resolutionServicesToUse == null || resolutionServicesToUse.isEmpty()) {
         if (userName != null && !(userName.isEmpty()) && privateKey != null && !(privateKey.isEmpty())) {
            informationObject = get(identifier, userName, privateKey);
         } else {
            informationObject = get(identifier);
         }
      } else {
         if (userName != null && !(userName.isEmpty()) && privateKey != null && !(privateKey.isEmpty())) {
            informationObject = get(identifier, resolutionServicesToUse, userName, privateKey);
         } else {
            informationObject = get(identifier, resolutionServicesToUse);
         }
      }

      // TODO: Ede: Think of a better place to perform this check.
      // try {
      // informationObject = securityManager.checkIncommingInformationObject(informationObject, false);
      // } catch (Exception e) {
      // LOG.warn("Security properties of IO to get could not be verified successfully. Identifier: " + identifier);
      // rsGetResponse.setErrorMessage("Security properties of IO to get could not be verified successfully. Identifier: "
      // + identifier);
      // return rsGetResponse;
      // }

      RSGetResponse rsGetResponse = new RSGetResponse();

      if (informationObject == null) {
         LOG.error("Could not fetch InformationObject with Identifier: " + identifier);
         rsGetResponse.setErrorMessage("Could not fetch InformationObject with Identifier: " + identifier);
         return rsGetResponse;
      }

      rsGetResponse.addInformationObject(informationObject);
      return rsGetResponse;
   }

   @Inject
   public void initReslolutionInterceptors(ResolutionInterceptor[] resolutionInterceptors) {
      for (ResolutionInterceptor interceptor : resolutionInterceptors) {
         addResolutionInterceptor(interceptor);
      }
   }

   @Override
   public void addResolutionInterceptor(ResolutionInterceptor interceptor) {
      this.interceptors.add(interceptor);

   }

   @Override
   public void removeResolutionInterceptor(ResolutionInterceptor interceptor) {
      this.interceptors.remove(interceptor);

   }

}
