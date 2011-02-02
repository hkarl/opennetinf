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
package netinf.node.transfer.impl;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import netinf.common.exceptions.NetInfNoSuchServiceException;
import netinf.common.exceptions.NetInfUncheckedException;
import netinf.common.messages.NetInfMessage;
import netinf.common.messages.TCChangeTransferRequest;
import netinf.common.messages.TCChangeTransferResponse;
import netinf.common.messages.TCGetServicesRequest;
import netinf.common.messages.TCGetServicesResponse;
import netinf.common.messages.TCStartTransferRequest;
import netinf.common.messages.TCStartTransferResponse;
import netinf.common.transfer.TransferJob;
import netinf.node.transfer.ExecutableTransferJob;
import netinf.node.transfer.TransferController;
import netinf.node.transfer.TransferJobIdGenerator;
import netinf.node.transfer.TransferService;

import org.apache.log4j.Logger;

import com.google.inject.Inject;

/**
 * This is a simple implementation of a {@link TransferController}. For most of the requests it simply selects a suitable
 * {@link TransferService} and delegates the task to it.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class TransferControllerImpl implements TransferController {

   private static final Logger LOG = Logger.getLogger(TransferControllerImpl.class);
   private ArrayList<Class<? extends NetInfMessage>> supportedOperations;

   private final Hashtable<String, TransferService> transferServices;
   private final TransferJobIdGenerator transferJobIdGenerator;

   @Inject
   public TransferControllerImpl(TransferJobIdGenerator transferJobIdGenerator) {
      this.transferJobIdGenerator = transferJobIdGenerator;
      transferServices = new Hashtable<String, TransferService>();
   }

   @Override
   public List<Class<? extends NetInfMessage>> getSupportedOperations() {
      LOG.trace(null);

      if (supportedOperations == null) {
         supportedOperations = new ArrayList<Class<? extends NetInfMessage>>();
         supportedOperations.add(TCGetServicesRequest.class);
         supportedOperations.add(TCStartTransferRequest.class);
         supportedOperations.add(TCChangeTransferRequest.class);
      }

      return supportedOperations;
   }

   @Override
   public NetInfMessage processNetInfMessage(NetInfMessage netInfMessage) {
      LOG.trace(null);

      if (netInfMessage instanceof TCGetServicesRequest) {
         TCGetServicesRequest tcServicesRequest = (TCGetServicesRequest) netInfMessage;
         return processTCGetServicesRequest(tcServicesRequest);
      }

      if (netInfMessage instanceof TCStartTransferRequest) {
         TCStartTransferRequest tcStartTransferRequest = (TCStartTransferRequest) netInfMessage;
         return processTCStartTransferRequest(tcStartTransferRequest);
      }

      if (netInfMessage instanceof TCChangeTransferRequest) {
         TCChangeTransferRequest tcChangeTransferRequest = (TCChangeTransferRequest) netInfMessage;
         return processTCChangeTransferRequest(tcChangeTransferRequest);
      }

      LOG.error("Could not find suitable handling of message: '" + netInfMessage.getClass().getCanonicalName() + "'");
      return null;
   }

   private NetInfMessage processTCGetServicesRequest(TCGetServicesRequest tcServicesRequest) {
      LOG.trace(null);

      TCGetServicesResponse tcGetServicesResponse = new TCGetServicesResponse();

      for (TransferService transferService : transferServices.values()) {
         tcGetServicesResponse.addTransferService(transferService.getIdentity());
      }

      return tcGetServicesResponse;
   }

   private NetInfMessage processTCStartTransferRequest(TCStartTransferRequest tcStartTransferRequest) {
      LOG.trace(null);

      String source = tcStartTransferRequest.getSource();
      String destination = tcStartTransferRequest.getDestination();
      String transferServiceToUse = tcStartTransferRequest.getTransferServiceToUse();

      TCStartTransferResponse tcStartTransferResponse = new TCStartTransferResponse();

      TransferJob transferJob = null;

      try {
         if (transferServiceToUse != null) {
            TransferService transferService = transferServices.get(transferServiceToUse);
            if (transferService != null) {
               transferJob = startTransfer(source, destination, transferService);
            } else {
               LOG.error("Could not find TransferService with the name '" + transferServiceToUse + "'");
               throw new NetInfNoSuchServiceException("No TransferService with the name '" + transferServiceToUse + "'");
            }
         } else {
            transferJob = startTransfer(source, destination, null);
         }

         tcStartTransferResponse.setSource(transferJob.getSource());
         tcStartTransferResponse.setDestination(transferJob.getDestination());
         tcStartTransferResponse.setJobId(transferJob.getJobId());
      } catch (NetInfUncheckedException e) {
         // Default case, if something went wrong.
         tcStartTransferResponse.setErrorMessage(e.getMessage());
         tcStartTransferResponse.setSource("null");
         tcStartTransferResponse.setDestination("null");
         tcStartTransferResponse.setJobId("null");
      }

      return tcStartTransferResponse;
   }

   private NetInfMessage processTCChangeTransferRequest(TCChangeTransferRequest tcChangeTransferRequest) {
      LOG.trace(null);

      String jobId = tcChangeTransferRequest.getJobId();
      String newDestination = tcChangeTransferRequest.getNewDestination();
      boolean proceed = tcChangeTransferRequest.isProceed();
      String transferServiceToUse = tcChangeTransferRequest.getTransferServiceToUse();

      TCChangeTransferResponse tcChangeTransferResponse = new TCChangeTransferResponse();

      try {
         TransferJob transferJob = null;

         if (transferServiceToUse != null) {
            TransferService transferService = transferServices.get(transferServiceToUse);
            if (transferService != null) {
               transferJob = changeTransfer(jobId, newDestination, proceed, transferService);
            } else {
               LOG.error("Could not find TransferService with the name '" + transferServiceToUse + "'");
               throw new NetInfNoSuchServiceException("No TransferService with the name '" + transferServiceToUse + "'");
            }
         } else {
            transferJob = changeTransfer(jobId, newDestination, proceed, null);
         }

         tcChangeTransferResponse.setJobId(transferJob.getJobId());
         tcChangeTransferResponse.setNewDestination(transferJob.getDestination());
         tcChangeTransferResponse.setSource(transferJob.getSource());
      } catch (NetInfUncheckedException e) {
         tcChangeTransferResponse.setErrorMessage(e.getMessage());
         tcChangeTransferResponse.setJobId("null");
         tcChangeTransferResponse.setNewDestination("null");
         tcChangeTransferResponse.setSource("null");
      }

      return tcChangeTransferResponse;
   }

   @Override
   public void addTransferService(TransferService transferService) {
      transferServices.put(transferService.getIdentity(), transferService);
   }

   @Override
   public void removeTransferService(TransferService transferService) {
      transferServices.remove(transferService.getIdentity());
   }

   @Override
   public ExecutableTransferJob changeTransfer(String jobId, String newDestination, boolean proceed, TransferService toUse) {
      LOG.trace(null);

      if (proceed) {
         TransferService transferService = getTransferServiceForTransferJob(jobId);

         if (transferService != null) {
            return transferService.changeTransfer(jobId, newDestination, proceed);
         } else {
            throw new NetInfUncheckedException("Could not change the transfer, since the jobId '" + jobId
                  + "' could not be associated with a TransferService");
         }
      } else {
         // proceed == false
         TransferService oldTransferService = getTransferServiceForTransferJob(jobId);
         ExecutableTransferJob executableTransferJob = getTransferJob(jobId);

         if (toUse == null || oldTransferService == toUse) {
            return oldTransferService.changeTransfer(jobId, newDestination, proceed);
         } else {
            // Start new one, but remove old one
            oldTransferService.stopTransfer(jobId);

            return toUse.startTransfer(jobId, executableTransferJob.getSource(), newDestination);
         }
      }
   }

   @Override
   public ExecutableTransferJob startTransfer(String source, String destination, TransferService toUse) {
      TransferService internal = null;

      if (toUse != null) {
         internal = toUse;
      } else {
         if (this.transferServices.size() > 0) {

            // TODO: Here a special selector might be used, in order to determine the best transferService.
            internal = transferServices.values().iterator().next();
         } else {
            throw new NetInfUncheckedException("Could not start the transfer, since no transferService given");
         }
      }

      String idToUse = transferJobIdGenerator.getNextId();
      return internal.startTransfer(idToUse, source, destination);
   }

   @Override
   public ExecutableTransferJob getTransferJob(String jobId) {
      TransferService containingTransferService = getTransferServiceForTransferJob(jobId);

      if (containingTransferService != null) {
         return containingTransferService.getTransferJob(jobId);
      }
      return null;
   }

   private TransferService getTransferServiceForTransferJob(String jobId) {
      TransferService containingTransferService = null;

      for (TransferService transferService : transferServices.values()) {
         if (transferService.containsTransferJob(jobId)) {
            containingTransferService = transferService;
         }
      }

      return containingTransferService;
   }
}
