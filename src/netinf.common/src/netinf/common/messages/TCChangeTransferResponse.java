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
package netinf.common.messages;

import netinf.common.transfer.TransferJob;

/**
 * <p>
 * This {@link NetInfMessage} is a response to the message {@link TCChangeTransferRequest}. Every {@link TCChangeTransferRequest}
 * entails exactly one {@link TCChangeTransferResponse}. The {@link TCChangeTransferResponse#getSource()} is similar to the
 * initial {@link TCStartTransferRequest#getSource()} that was send in the beginning.
 * </p>
 * <p>
 * The {@link TCChangeTransferResponse#getNewDestination()} might be <code>null</code>, in which case the initiator of the change
 * is not informed about the new destination.
 * </p>
 * <p>
 * The {@link TCChangeTransferResponse#getJobId()} determines the {@link TransferJob} that should be modified.
 * </p>
 * 
 * @author PG Augnet 2, University of Paderborn
 */
public class TCChangeTransferResponse extends NetInfMessage {

   private String source;
   private String newDestination;
   private String jobId;

   public String getSource() {
      return source;
   }

   public void setSource(String source) {
      this.source = source;
   }

   public String getNewDestination() {
      return newDestination;
   }

   public void setNewDestination(String destination) {
      this.newDestination = destination;
   }

   public String getJobId() {
      return jobId;
   }

   public void setJobId(String jobId) {
      this.jobId = jobId;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((newDestination == null) ? 0 : newDestination.hashCode());
      result = prime * result + ((jobId == null) ? 0 : jobId.hashCode());
      result = prime * result + ((source == null) ? 0 : source.hashCode());
      return result;
   }

   /**
    * The overridden equals method. It generally holds: For any non-null reference value x, x.equals(null) should return false.
    * That check is implemented in the NetInfMessage class
    * @param obj The object comparing the message to. If it's not of the same type, the superclass method will take care of it
    * @see NetInfMessage
    */
   @Override
   public boolean equals(Object obj) {
     
       if (!super.equals(obj)) {
	         return false;
	      }
      TCChangeTransferResponse other = (TCChangeTransferResponse) obj;
      if (newDestination == null) {
         if (other.newDestination != null) {
            return false;
         }
      } else if (!newDestination.equals(other.newDestination)) {
         return false;
      }
      if (jobId == null) {
         if (other.jobId != null) {
            return false;
         }
      } else if (!jobId.equals(other.jobId)) {
         return false;
      }
      if (source == null) {
         if (other.source != null) {
            return false;
         }
      } else if (!source.equals(other.source)) {
         return false;
      }
      return true;
   }

   @Override
   public String toString() {
      String superString = super.toString();
      StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append(superString);

      stringBuilder.append("\nSource: " + source);
      stringBuilder.append("\nNew Destination: " + newDestination);
      stringBuilder.append("\nJobId: " + jobId);

      return stringBuilder.toString();
   }
}
