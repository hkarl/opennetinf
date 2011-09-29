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
package netinf.node.transfer.gp;

import netinf.node.transfer.ExecutableTransferJob;

import org.apache.log4j.Logger;

/**
 * A simple transfer Job for GP.
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class TransferJobGP extends ExecutableTransferJob {

   private static final Logger LOG = Logger.getLogger(TransferJobGP.class);
   private boolean completed;

   public TransferJobGP(String jobId, String source, String destination) {
      super(jobId, source, destination);
      completed = false;
   }

   @Override
   public boolean isCompleted() {
      return completed;
   }

   /*
    * In case of GP, the transfer has already been started earlier.
    * @see netinf.node.transfer.TransferJob#startTransferJob()
    */
   @Override
   public void startTransferJob() {
      LOG.trace(null);
      // Nothing happens here, the transfer job is always directly started.
   }

   public void setCompleted(boolean completed) {
      this.completed = completed;
      notifyListeners();
   }
}
