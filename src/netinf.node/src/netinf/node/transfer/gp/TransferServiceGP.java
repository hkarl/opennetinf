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
package netinf.node.transfer.gp;

import java.util.Hashtable;

import netinf.common.exceptions.NetInfUncheckedException;
import netinf.common.transfer.TransferJob;
import netinf.node.gp.GPNetInfInterface;
import netinf.node.gp.GPNetInfInterfaceImpl;
import netinf.node.transfer.ExecutableTransferJob;
import netinf.node.transfer.TransferListener;
import netinf.node.transfer.TransferService;

import org.apache.log4j.Logger;

import com.google.inject.Inject;

/**
 * A special {@link TransferService} for GP. Is capable to communicate with GP over the {@link GPNetInfInterfaceImpl}. The methods
 * {@link TransferServiceGP#startTransfer(String, String, String)} and
 * {@link TransferServiceGP#changeTransfer(String, String, boolean)} are pretty much adjusted for the integrated Scenario.
 * 
 * TODO: We are not allowed to store the TransferJobs, after they have been moved, since this is simply not necessary for GP. It
 * might lead to additional resource usage, although not necessary.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class TransferServiceGP implements TransferService, TransferListener {

   public static final String TRANSFER_SERVICE_NAME = "TransferServiceGP";

   private static final Logger LOG = Logger.getLogger(TransferServiceGP.class);

   private final GPNetInfInterface gpNetInfInterface;
   private final Hashtable<String, TransferJobGP> transferJobs;

   @Inject
   public TransferServiceGP(GPNetInfInterface gpNetInfInterface) {
      this.gpNetInfInterface = gpNetInfInterface;
      this.transferJobs = new Hashtable<String, TransferJobGP>();
   }

   /*
    * This method is currently exclusively implemented for the use with GP and the vlc-scenario.
    * @see netinf.node.transfer.TransferService#startTransfer(java.lang.String, java.lang.String, java.lang.String)
    */
   @Override
   public ExecutableTransferJob startTransfer(String jobIdToUse, String source, String destination) {
      LOG.trace(null);

      if (destination != null) {
         LOG.error("Destination for transferService given, although no destination expected");
      }

      // The destination is unimportant for us.
      TransferJobGP transferJob = new TransferJobGP(jobIdToUse, source, null);

      transferJob.addListener(this);
      this.transferJobs.put(transferJob.getJobId(), transferJob);

      this.gpNetInfInterface.prepareGP(transferJob.getJobId(), transferJob.getSource());

      LOG.debug("Started TransferJob:\n" + transferJob.toString());
      return transferJob;
   }

   @Override
   public ExecutableTransferJob changeTransfer(String jobId, String newDestination, boolean proceed) {
      LOG.trace(null);

      if (!proceed) {
         throw new NetInfUncheckedException("Only proceed = true is implemented currently");
      }

      TransferJobGP transferJob = this.transferJobs.get(jobId);

      if (newDestination == null) {
         throw new NetInfUncheckedException("Could not move transferJob with id " + jobId + " to null-destination");
      }

      this.gpNetInfInterface.moveGP(jobId, newDestination);
      transferJob.setDestination(newDestination);

      return transferJob;
   }

   @Override
   public void notifyChanged(ExecutableTransferJob transferJob) {
      LOG.trace(null);

      if (transferJob.isCompleted()) {
         LOG.debug("Removed TransferJob\n" + transferJob.toString());
         this.transferJobs.remove(transferJob.getJobId());
      }
   }

   @Override
   public boolean containsTransferJob(String jobId) {
      TransferJob transferJob = this.transferJobs.get(jobId);

      return transferJob != null;
   }

   @Override
   public ExecutableTransferJob getTransferJob(String jobId) {
      return this.transferJobs.get(jobId);
   }

   @Override
   public String getIdentity() {
      return TRANSFER_SERVICE_NAME;
   }

   @Override
   public String describe() {
      // TODO add some more info?
      return "Generic Path";
   }

   @Override
   public boolean stopTransfer(String jobId) {
      TransferJobGP transferJob = (TransferJobGP) getTransferJob(jobId);

      if (transferJob != null) {
         transferJob.setCompleted(true);
         return true;
      } else {
         return false;
      }

   }
}
